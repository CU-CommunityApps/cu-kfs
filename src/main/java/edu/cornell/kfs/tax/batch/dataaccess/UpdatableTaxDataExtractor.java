package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;

public interface UpdatableTaxDataExtractor<T, U> extends TaxDataExtractor<T> {

    void updateCurrentRow(final U dtoContainingUpdates) throws SQLException;

}
