package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;
import java.util.Map;

import edu.cornell.kfs.tax.batch.TaxColumns.TransactionDetailColumn;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public interface TransactionDetailExtractor extends TaxDataExtractor<TransactionDetail> {

    void updateCurrentRow(final Map<TransactionDetailColumn, String> fieldsToUpdate) throws SQLException;

}
