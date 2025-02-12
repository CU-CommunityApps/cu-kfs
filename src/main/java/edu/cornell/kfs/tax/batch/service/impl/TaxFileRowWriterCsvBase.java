package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;

public abstract class TaxFileRowWriterCsvBase<T> extends TaxFileRowWriterBase<T> {

    protected final WrappedCsvWriter wrappedCsvWriter;

    protected TaxFileRowWriterCsvBase(final TaxOutputDefinitionV2 taxOutputDefinition,
            final Class<? extends TaxDtoFieldEnum> fieldEnumClass, final WrappedCsvWriter wrappedCsvWriter) {
        super(taxOutputDefinition, fieldEnumClass);
        Validate.notNull(wrappedCsvWriter, "wrappedCsvWriter cannot be null");
        this.wrappedCsvWriter = wrappedCsvWriter;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(wrappedCsvWriter);
    }

    @Override
    protected void writeHeaderRow(final String sectionName, final Stream<String> headerData) throws IOException {
        writeCsvRow(sectionName, headerData);
    }

    @Override
    protected void writeDataRow(final String sectionName, final Stream<String> rowData) throws IOException {
        writeCsvRow(sectionName, rowData);
    }

    protected void writeCsvRow(final String sectionName, final Stream<String> rowData) throws IOException {
        final String[] csvRow = rowData.toArray(String[]::new);
        wrappedCsvWriter.getCsvWriter().writeNext(csvRow);
    }

}
