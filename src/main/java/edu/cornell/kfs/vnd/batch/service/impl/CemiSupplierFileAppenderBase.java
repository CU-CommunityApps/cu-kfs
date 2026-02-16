package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.vnd.batch.service.CemiSupplierFileAppender;

public abstract class CemiSupplierFileAppenderBase implements CemiSupplierFileAppender {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;

    protected CemiSupplierFileAppenderBase(final CemiOutputDefinition outputDefinition, final LocalDateTime jobRunDate) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(jobRunDate, "jobRunDate cannot be null");
        this.outputDefinition = outputDefinition;
        this.jobRunDate = jobRunDate;
    }

    @Override
    public void populateSupplierFileFromIntermediateDataStorage(final CemiExcelWriter fileWriter) throws IOException {
        for (final CemiSheetDefinition sheetDefinition : outputDefinition.getSheets()) {
            LOG.info("populateSupplierFileFromIntermediateDataStorage, Writing sheet: {}", sheetDefinition.getName());
            populateSheetFromIntermediateDataStorage(sheetDefinition, fileWriter);
        }
    }

    protected void populateSheetFromIntermediateDataStorage(
            final CemiSheetDefinition sheetDefinition, final CemiExcelWriter fileWriter) throws IOException {
        try (
            final Stream<String[]> sheetData = getCloseableSheetDataStreamFromIntermediateStorage(sheetDefinition);
        ) {
            final String sheetName = sheetDefinition.getName();
            final Iterator<String[]> sheetDataIterator = sheetData.iterator();
            for (final String[] sheetRow : IteratorUtils.asIterable(sheetDataIterator)) {
                fileWriter.writeRow(sheetName, sheetRow);
            }
        }
    }

    // This is what needs to be implemented in a subclass when switching storage from CSV to temp table.
    // JDBCTemplate and/or CuOjbUtils should have methods for returning DB data as a stream.
    protected abstract Stream<String[]> getCloseableSheetDataStreamFromIntermediateStorage(
            final CemiSheetDefinition sheetDefinition) throws IOException;

}
