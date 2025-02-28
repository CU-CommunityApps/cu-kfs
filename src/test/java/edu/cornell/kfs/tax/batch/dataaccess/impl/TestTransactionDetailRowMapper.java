package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.SQLException;
import java.util.List;

import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapper;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public class TestTransactionDetailRowMapper implements TransactionDetailRowMapper<TransactionDetail> {

    private final List<TransactionDetail> transactionDetails;
    private int currentIndex;

    public TestTransactionDetailRowMapper(final List<TransactionDetail> transactionDetails) {
        this.transactionDetails = transactionDetails;
        this.currentIndex = -1;
    }

    @Override
    public boolean moveToNextRow() throws SQLException {
        if (currentIndex == transactionDetails.size()) {
            throw new IllegalStateException("Mapper already returned false on a previous call");
        }
        currentIndex++;
        return currentIndex < transactionDetails.size();
    }

    @Override
    public TransactionDetail readCurrentRow() throws SQLException {
        return transactionDetails.get(currentIndex);
    }

    @Override
    public void updateCurrentRow(final TransactionDetail dtoContainingUpdates) throws SQLException {
        throw new UnsupportedOperationException("This implementation does not support updates");
    }

}
