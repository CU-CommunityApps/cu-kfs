package edu.cornell.kfs.tax.batch.service;

import java.io.Closeable;
import java.io.IOException;

public interface TaxFileRowWriter<T> extends Closeable {

    void writeHeaderRow(final String sectionName) throws IOException;

    void writeDataRow(final String sectionName, final T taxFileRowDto) throws IOException;

}
