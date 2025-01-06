package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.TaxColumns.VendorAddressColumn;
import edu.cornell.kfs.tax.businessobject.VendorAddressLite;

public class VendorAddressLiteExtractorImpl extends TaxDataExtractorBase<VendorAddressLite> {

    public VendorAddressLiteExtractorImpl(final ResultSet resultSet, final EncryptionService encryptionService) {
        super(resultSet, encryptionService);
    }

    @Override
    public VendorAddressLite getCurrentRow() throws SQLException {
        final VendorAddressLite vendorAddress = new VendorAddressLite();
        vendorAddress.setVendorAddressGeneratedIdentifier(getInt(VendorAddressColumn.VNDR_ADDR_GNRTD_ID));
        vendorAddress.setVendorHeaderGeneratedIdentifier(getInt(VendorAddressColumn.VNDR_HDR_GNRTD_ID));
        vendorAddress.setVendorDetailAssignedIdentifier(getInt(VendorAddressColumn.VNDR_DTL_ASND_ID));
        vendorAddress.setVendorAddressTypeCode(getString(VendorAddressColumn.VNDR_ADDR_TYP_CD));
        vendorAddress.setVendorLine1Address(getString(VendorAddressColumn.VNDR_LN1_ADDR));
        vendorAddress.setVendorLine2Address(getString(VendorAddressColumn.VNDR_LN2_ADDR));
        vendorAddress.setVendorCityName(getString(VendorAddressColumn.VNDR_CTY_NM));
        vendorAddress.setVendorStateCode(getString(VendorAddressColumn.VNDR_ST_CD));
        vendorAddress.setVendorZipCode(getString(VendorAddressColumn.VNDR_ZIP_CD));
        vendorAddress.setVendorCountryCode(getString(VendorAddressColumn.VNDR_CNTRY_CD));
        vendorAddress.setVendorAttentionName(getString(VendorAddressColumn.VNDR_ATTN_NM));
        vendorAddress.setVendorAddressInternationalProvinceName(getString(VendorAddressColumn.VNDR_ADDR_INTL_PROV_NM));
        vendorAddress.setVendorAddressEmailAddress(getString(VendorAddressColumn.VNDR_ADDR_EMAIL_ADDR));
        vendorAddress.setActive(getAsBooleanOrDefault(VendorAddressColumn.DOBJ_MAINT_CD_ACTV_IND, Boolean.FALSE));
        return vendorAddress;
    }

}
