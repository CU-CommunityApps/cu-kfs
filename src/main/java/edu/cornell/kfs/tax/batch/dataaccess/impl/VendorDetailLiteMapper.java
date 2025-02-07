package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public class VendorDetailLiteMapper extends ReadOnlyTaxDtoRowMapperBase<VendorDetailLite> {

    public VendorDetailLiteMapper(final EncryptionService encryptionService, final TaxDtoDbMetadata metadata,
            final ResultSet resultSet) {
        super(VendorDetailLite::new, encryptionService, metadata, resultSet);
    }

    @Override
    protected void populateDtoFromCurrentRow(final VendorDetailLite vendor) throws SQLException {
        vendor.setVendorHeaderGeneratedIdentifier(getInt(VendorField.vendorHeaderGeneratedIdentifier_forDetail));
        vendor.setVendorDetailAssignedIdentifier(getInt(VendorField.vendorDetailAssignedIdentifier));
        vendor.setVendorParentIndicator(getBoolean(VendorField.vendorParentIndicator, Boolean.FALSE));
        vendor.setVendorFirstLastNameIndicator(getBoolean(VendorField.vendorFirstLastNameIndicator, Boolean.FALSE));
        vendor.setVendorName(getString(VendorField.vendorName));
        vendor.setVendorTaxNumber(getAndDecryptString(VendorField.vendorTaxNumber));
        vendor.setVendorTypeCode(getString(VendorField.vendorTypeCode));
        vendor.setVendorOwnershipCode(getString(VendorField.vendorOwnershipCode));
        vendor.setVendorOwnershipCategoryCode(getString(VendorField.vendorOwnershipCategoryCode));
        vendor.setVendorForeignIndicator(getBoolean(VendorField.vendorForeignIndicator, Boolean.FALSE));
        vendor.setVendorGIIN(getString(VendorField.vendorGIIN));
        vendor.setVendorChapter4StatusCode(getString(VendorField.vendorChapter4StatusCode));
    }

}
