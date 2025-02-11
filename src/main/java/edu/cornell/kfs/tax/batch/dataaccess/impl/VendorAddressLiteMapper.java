package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite.VendorAddressField;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;

public class VendorAddressLiteMapper extends ReadOnlyTaxDtoRowMapperBase<VendorAddressLite> {

    public VendorAddressLiteMapper(final EncryptionService encryptionService, final TaxDtoDbMetadata metadata,
            final ResultSet resultSet) {
        super(VendorAddressLite::new, encryptionService, metadata, resultSet);
    }

    @Override
    protected void populateDtoFromCurrentRow(final VendorAddressLite address) throws SQLException {
        address.setVendorAddressGeneratedIdentifier(getInt(VendorAddressField.vendorAddressGeneratedIdentifier));
        address.setVendorHeaderGeneratedIdentifier(getInt(VendorAddressField.vendorHeaderGeneratedIdentifier));
        address.setVendorDetailAssignedIdentifier(getInt(VendorAddressField.vendorDetailAssignedIdentifier));
        address.setVendorAddressTypeCode(getString(VendorAddressField.vendorAddressTypeCode));
        address.setVendorLine1Address(getString(VendorAddressField.vendorLine1Address));
        address.setVendorLine2Address(getString(VendorAddressField.vendorLine2Address));
        address.setVendorCityName(getString(VendorAddressField.vendorCityName));
        address.setVendorStateCode(getString(VendorAddressField.vendorStateCode));
        address.setVendorZipCode(getString(VendorAddressField.vendorZipCode));
        address.setVendorCountryCode(getString(VendorAddressField.vendorCountryCode));
        address.setVendorAttentionName(getString(VendorAddressField.vendorAttentionName));
        address.setVendorAddressInternationalProvinceName(
                getString(VendorAddressField.vendorAddressInternationalProvinceName));
        address.setVendorAddressEmailAddress(getString(VendorAddressField.vendorAddressEmailAddress));
        address.setActive(getBoolean(VendorAddressField.active, Boolean.FALSE));
    }

}
