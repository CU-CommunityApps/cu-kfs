package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.batch.service.impl.CemiCsvReader;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;

public class CemiSupplierFileAppenderCsvImpl extends CemiSupplierFileAppenderBase {

    private final String baseFilePath;

    public CemiSupplierFileAppenderCsvImpl(final CemiOutputDefinition outputDefinition,
            final String baseFilePath) {
        super(outputDefinition);
        Validate.notBlank(baseFilePath, "baseFilePath cannot be blank");
        this.baseFilePath = baseFilePath;
    }

    @Override
    protected Stream<String[]> getCloseableSheetDataStreamFromIntermediateStorage(
            final CemiSheetDefinition sheetDefinition) throws IOException {
        final File csvFile = getFileForIntermediateDataStorage(sheetDefinition.getName());
        Validate.validState(csvFile.exists(), "%s sheet CSV file not found", sheetDefinition.getName());
    
        CemiCsvReader csvReader = null;
        boolean setupSuccessful = false;
        try {
            csvReader = new CemiCsvReader(csvFile);
            final CemiCsvReader csvReaderForOnCloseHandler = csvReader;
            final Spliterator<String[]> spliterator = Spliterators.spliteratorUnknownSize(csvReader.iterator(), 0);
            final Stream<String[]> sheetDataStream = StreamSupport.stream(() -> spliterator, 0, false)
                    .onClose(() -> IOUtils.closeQuietly(csvReaderForOnCloseHandler));
            setupSuccessful = true;
            return sheetDataStream;
        } finally {
            if (!setupSuccessful) {
                IOUtils.closeQuietly(csvReader);
            }
        }
    }

    @Override
    public void cleanUpIntermediateStorage() throws IOException {
        
    }

    private final File getFileForIntermediateDataStorage(final String sheetName) {
        final String fullFileName = StringUtils.join(baseFilePath, CUKFSConstants.SLASH, sheetName,
                FileExtensions.CSV);
        return new File(fullFileName);
    }

}
