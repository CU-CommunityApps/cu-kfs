package edu.cornell.kfs.tax.batch.service;

import java.io.Closeable;
import java.io.IOException;

public interface TaxFileRowWriter<T> extends Closeable {

    String getTaxFileType();

    void writeHeaderRow(final String sectionName) throws IOException;

    void writeDataRow(final T taxFileRow, final String sectionName) throws IOException;

}
