package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.QueryTableAliases;
import edu.cornell.kfs.tax.batch.annotation.TaxBusinessObjectMapping;
import edu.cornell.kfs.tax.batch.annotation.TaxDto;
import edu.cornell.kfs.tax.batch.annotation.TaxDtoField;

@TaxDto(mappedBusinessObjects = {
        @TaxBusinessObjectMapping(businessObjectClass = VendorHeader.class,
                tableAliasForQuery = QueryTableAliases.VENDOR_HEADER),
        @TaxBusinessObjectMapping(businessObjectClass = VendorDetail.class,
                tableAliasForQuery = QueryTableAliases.VENDOR_DETAIL)
})
public class VendorDetailLite {

    @TaxDtoField(mappedBusinessObject = VendorDetail.class)
    private Integer vendorHeaderGeneratedIdentifier;

    @TaxDtoField(mappedBusinessObject = VendorDetail.class)
    private Integer vendorDetailAssignedIdentifier;

    @TaxDtoField(mappedBusinessObject = VendorDetail.class)
    private boolean vendorParentIndicator;

    @TaxDtoField(mappedBusinessObject = VendorDetail.class)
    private boolean vendorFirstLastNameIndicator;

    @TaxDtoField(mappedBusinessObject = VendorDetail.class)
    private String vendorName;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorTaxNumber;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorTypeCode;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorOwnershipCode;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorOwnershipCategoryCode;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private boolean vendorForeignIndicator;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorGIIN;

    @TaxDtoField(mappedBusinessObject = VendorHeader.class)
    private String vendorChapter4StatusCode;

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(final Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(final Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public boolean isVendorParentIndicator() {
        return vendorParentIndicator;
    }

    public void setVendorParentIndicator(final boolean vendorParentIndicator) {
        this.vendorParentIndicator = vendorParentIndicator;
    }

    public boolean isVendorFirstLastNameIndicator() {
        return vendorFirstLastNameIndicator;
    }

    public void setVendorFirstLastNameIndicator(final boolean vendorFirstLastNameIndicator) {
        this.vendorFirstLastNameIndicator = vendorFirstLastNameIndicator;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(final String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(final String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(final String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getVendorOwnershipCategoryCode() {
        return vendorOwnershipCategoryCode;
    }

    public void setVendorOwnershipCategoryCode(final String vendorOwnershipCategoryCode) {
        this.vendorOwnershipCategoryCode = vendorOwnershipCategoryCode;
    }

    public boolean isVendorForeignIndicator() {
        return vendorForeignIndicator;
    }

    public void setVendorForeignIndicator(final boolean vendorForeignIndicator) {
        this.vendorForeignIndicator = vendorForeignIndicator;
    }

    public String getVendorGIIN() {
        return vendorGIIN;
    }

    public void setVendorGIIN(final String vendorGIIN) {
        this.vendorGIIN = vendorGIIN;
    }

    public String getVendorChapter4StatusCode() {
        return vendorChapter4StatusCode;
    }

    public void setVendorChapter4StatusCode(final String vendorChapter4StatusCode) {
        this.vendorChapter4StatusCode = vendorChapter4StatusCode;
    }

}