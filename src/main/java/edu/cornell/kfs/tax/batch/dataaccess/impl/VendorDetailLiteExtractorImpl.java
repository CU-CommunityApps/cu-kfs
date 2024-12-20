package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.TaxColumns.VendorDetailColumn;
import edu.cornell.kfs.tax.businessobject.VendorDetailLite;

public class VendorDetailLiteExtractorImpl extends TaxDataExtractorBase<VendorDetailLite> {

    public VendorDetailLiteExtractorImpl(final ResultSet resultSet, final EncryptionService encryptionService) {
        super(resultSet, encryptionService);
    }

    @Override
    public VendorDetailLite getCurrentRow() throws SQLException {
        final VendorDetailLite vendorDetail = new VendorDetailLite();
        vendorDetail.setVendorHeaderGeneratedIdentifier(getInt(VendorDetailColumn.VNDR_HDR_GNRTD_ID));
        vendorDetail.setVendorDetailAssignedIdentifier(getInt(VendorDetailColumn.VNDR_DTL_ASND_ID));
        vendorDetail.setVendorParentIndicator(
                getAsBooleanOrDefault(VendorDetailColumn.VNDR_PARENT_IND, Boolean.FALSE));
        vendorDetail.setVendorFirstLastNameIndicator(
                getAsBooleanOrDefault(VendorDetailColumn.VNDR_1ST_LST_NM_IND, Boolean.FALSE));
        vendorDetail.setVendorName(getString(VendorDetailColumn.VNDR_NM));
        vendorDetail.setVendorTaxNumber(getString(VendorDetailColumn.VNDR_US_TAX_NBR));
        vendorDetail.setVendorTypeCode(getString(VendorDetailColumn.VNDR_TYP_CD));
        vendorDetail.setVendorOwnershipCode(getString(VendorDetailColumn.VNDR_OWNR_CD));
        vendorDetail.setVendorOwnershipCategoryCode(getString(VendorDetailColumn.VNDR_OWNR_CTGRY_CD));
        vendorDetail.setVendorForeignIndicator(
                getAsBooleanOrDefault(VendorDetailColumn.VNDR_FRGN_IND, Boolean.FALSE));
        vendorDetail.setVendorGIIN(getString(VendorDetailColumn.VNDR_GIIN));
        vendorDetail.setVendorChapter4StatusCode(getString(VendorDetailColumn.VNDR_CHAP_4_STAT_CD));
        return vendorDetail;
    }

}
