package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.service.impl.CemiCsvWriter;
import edu.cornell.kfs.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.dataaccess.CemiVendorDao;

public class CemiSupplierDataBuilderCsvImpl extends CemiSupplierDataBuilderBase {

    private final Map<String, CemiCsvWriter> csvWriters;
    private final Map<String, CemiSheetDefinition> sheetDefinitions;

    public CemiSupplierDataBuilderCsvImpl(final CemiOutputDefinition outputDefinition,
            final CemiVendorDao cemiVendorDao, final LocalDateTime jobRunDate,
            final String baseFileDirectory, final boolean maskSensitiveData) throws IOException {
        super(outputDefinition, cemiVendorDao, jobRunDate, maskSensitiveData);
        Validate.notBlank(baseFileDirectory, "baseFileDirectory cannot be blank");

        this.sheetDefinitions = outputDefinition.getSheets().stream()
                .collect(Collectors.toUnmodifiableMap(CemiSheetDefinition::getName, Function.identity()));

        final Map<String, CemiCsvWriter> writers = new HashMap<>();
        CemiCsvWriter nextWriter = null;
        boolean setupSucceeded = false;

        try {
            for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
                final String sheetName = sheetDefinition.getName();
                final String fileName = CemiUtils.generateFileNameContainingDateTime(
                        jobRunDate, sheetName + CUKFSConstants.UNDERSCORE, FileExtensions.CSV);
                final String fileNameWithPath = StringUtils.join(baseFileDirectory, CUKFSConstants.SLASH, fileName);
                nextWriter = new CemiCsvWriter(fileNameWithPath);
                writers.put(sheetName, nextWriter);
                nextWriter = null;
            }
            this.csvWriters = Map.copyOf(writers);
            setupSucceeded = true;
        } finally {
            if (!setupSucceeded) {
                IOUtils.closeQuietly(nextWriter);
                for (final CemiCsvWriter writer : writers.values()) {
                    IOUtils.closeQuietly(writer);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (final CemiCsvWriter csvWriter : csvWriters.values()) {
            IOUtils.closeQuietly(csvWriter);
        }
    }

    @Override
    protected void writeDataToIntermediateStorage(String sheetName, Object rowObject) throws IOException {
        final CemiSheetDefinition sheetDefinition = sheetDefinitions.get(sheetName);
        final CemiCsvWriter csvWriter = csvWriters.get(sheetName);
        Validate.validState(sheetDefinition != null, "Unexpected CEMI Supplier datasheet: %s", sheetName);
        Validate.validState(csvWriter != null, "Unexpected non-writeable Supplier datasheet: %s", sheetName);

        final String[] csvRow = new String[sheetDefinition.getFields().size()];
        int fieldIndex = 0;
        for (final CemiFieldDefinition field : sheetDefinition.getFields()) {
            final String fieldValue = getFieldValue(field, rowObject);
            csvRow[fieldIndex] = fieldValue;
            fieldIndex++;
        }

        csvWriter.writeNext(csvRow);
    }

}
