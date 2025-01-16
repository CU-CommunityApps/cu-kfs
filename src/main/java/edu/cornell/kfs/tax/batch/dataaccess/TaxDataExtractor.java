package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;

public interface TaxDataExtractor<T> {

    boolean moveToNextRow() throws SQLException;

    T getCurrentRow() throws SQLException;

}
