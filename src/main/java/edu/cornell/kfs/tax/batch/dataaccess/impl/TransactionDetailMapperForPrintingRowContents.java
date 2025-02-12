package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public class TransactionDetailMapperForPrintingRowContents extends TransactionDetailMapperBase<TransactionDetail> {

    public TransactionDetailMapperForPrintingRowContents(final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet) {
        super(encryptionService, metadata, resultSet);
    }

    @Override
    public void updateCurrentRow(final TransactionDetail dtoContainingUpdates) throws SQLException {
        throw new UnsupportedOperationException("This implementation does not allow updating Transaction Details");
    }

    @Override
    protected void prepareCurrentRowForUpdate(final TransactionDetail currentTransactionDetail) throws SQLException {
        throw new UnsupportedOperationException("This implementation does not allow updating Transaction Details");
    }

    @Override
    protected void updateString(final Enum<?> fieldDefinition, final String value) throws SQLException {
        throw new UnsupportedOperationException("This implementation does not allow updating Transaction Details");
    }

}
