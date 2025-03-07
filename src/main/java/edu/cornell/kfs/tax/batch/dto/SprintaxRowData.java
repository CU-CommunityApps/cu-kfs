package edu.cornell.kfs.tax.batch.dto;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

/*
 * NOTE: This object will be fully implemented in a follow-up user story.
 */
public class SprintaxRowData {

    private String vendorName;
    private String form1042SBox;

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public String getForm1042SBox() {
        return form1042SBox;
    }

    public void setForm1042SBox(final String form1042sBox) {
        form1042SBox = form1042sBox;
    }



    /*
     * This enum will be utilized and adjusted further in a follow-up user story.
     */
    public enum SprintaxField implements TaxDtoFieldEnum {
        vendorFirstName,
        vendorLastName,
        vendorEmailAddress,
        payeeId,
        formattedSSNValue,
        formattedITINValue,
        vendorUSAddressLine1,
        vendorUSAddressLine2,
        vendorUSCityName,
        vendorUSStateCode,
        vendorUSZipCode,
        vendorForeignAddressLine1,
        vendorForeignCityName,
        vendorForeignProvinceName,
        vendorForeignZipCode,
        vendorForeignCountryCode,
        payment_uniqueFormId("payment.uniqueFormId"),
        payment_incomeCodeForOutput("payment.incomeCodeForOutput"),
        payment_grossAmount("payment.grossAmount"),
        payment_chapter3ExemptionCode("payment.chapter3ExemptionCode"),
        payment_chapter3TaxRate("payment.chapter3TaxRate"),
        chapter4ExemptionCode,
        payment_federalTaxWithheldAmount("payment.federalTaxWithheldAmount"),
        chapter3StatusCode,
        chapter4StatusCode,
        vendorGIIN,
        payerEIN,
        payment_stateIncomeTaxWithheldAmount("payment.stateIncomeTaxWithheldAmount");

        private final String fieldName;

        private SprintaxField() {
            this(null);
        }

        private SprintaxField(final String fieldName) {
            this.fieldName = StringUtils.defaultIfBlank(fieldName, name());
        }

        @Override
        public String getFieldName() {
            return fieldName;
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            throw new UnsupportedOperationException("This enum is for file output only, not for database mappings");
        }
    }

}
