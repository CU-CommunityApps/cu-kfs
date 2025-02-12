package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public class TransactionDetailMapperForSprintax extends TransactionDetailMapperBase<SprintaxInfo1042S> {

    public TransactionDetailMapperForSprintax(final EncryptionService encryptionService,
            final TaxDtoDbMetadata metadata, final ResultSet resultSet) {
        super(encryptionService, metadata, resultSet);
    }

    /*
     * This method will be fully implemented in a future user story. Only some very minimal coding
     * has been added thus far for demonstration purposes.
     */
    @Override
    protected void prepareCurrentRowForUpdate(final SprintaxInfo1042S sprintaxInfo) throws SQLException {
        updateString(TransactionDetailField.vendorName, sprintaxInfo.getVendorNameForOutput());
        updateString(TransactionDetailField.form1042SBox, CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
    }

}
