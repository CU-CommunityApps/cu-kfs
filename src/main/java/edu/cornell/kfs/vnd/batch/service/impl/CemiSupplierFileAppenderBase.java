package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
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

    protected CemiSupplierFileAppenderBase(final CemiOutputDefinition outputDefinition) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        this.outputDefinition = outputDefinition;
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

    protected abstract Stream<String[]> getCloseableSheetDataStreamFromIntermediateStorage(
            final CemiSheetDefinition sheetDefinition) throws IOException;

}
