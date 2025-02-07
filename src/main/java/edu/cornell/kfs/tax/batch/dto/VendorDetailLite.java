package edu.cornell.kfs.tax.batch.dto;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class VendorDetailLite {

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private boolean vendorParentIndicator;
    private boolean vendorFirstLastNameIndicator;
    private String vendorName;
    private String vendorTaxNumber;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private boolean vendorForeignIndicator;
    private String vendorGIIN;
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



    public enum VendorField implements TaxDtoFieldEnum {
        vendorHeaderGeneratedIdentifier_forHeader(
                VendorHeader.class, VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID),
        vendorHeaderGeneratedIdentifier_forDetail(
                VendorDetail.class, VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID),
        vendorDetailAssignedIdentifier(VendorDetail.class),
        vendorParentIndicator(VendorDetail.class),
        vendorFirstLastNameIndicator(VendorDetail.class),
        vendorName(VendorDetail.class),
        vendorTaxNumber(VendorHeader.class),
        vendorTypeCode(VendorHeader.class),
        vendorOwnershipCode(VendorHeader.class),
        vendorOwnershipCategoryCode(VendorHeader.class),
        vendorForeignIndicator(VendorHeader.class),
        vendorGIIN(VendorHeader.class),
        vendorChapter4StatusCode(VendorHeader.class);

        private final Class<? extends BusinessObject> mappedClass;
        private final String fieldName;

        private VendorField(final Class<? extends BusinessObject> mappedClass) {
            this(mappedClass, null);
        }

        private VendorField(final Class<? extends BusinessObject> mappedClass, final String fieldName) {
            this.mappedClass = mappedClass;
            this.fieldName = StringUtils.defaultIfBlank(fieldName, name());
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return mappedClass;
        }

    }

}
