package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class SprintaxPayee extends TaxPayeeBase {

    private String rowId;
    private String paymentAddressLine1;
    private String formattedSSNValue;
    private String formattedITINValue;
    private String chapter3StatusCode;
    private String chapter4ExemptionCode;
    private String payerEIN;
    private String stateCode;
    private Date endDate;
    private boolean demographicRowWritten;

    private SprintaxPayment currentPayment;
    private TaxBoxUpdates currentTaxBoxUpdates;

    public String getRowId() {
        return rowId;
    }

    public void setRowId(final String rowId) {
        this.rowId = rowId;
    }

    public String getPaymentAddressLine1() {
        return paymentAddressLine1;
    }

    public void setPaymentAddressLine1(final String paymentAddressLine1) {
        this.paymentAddressLine1 = paymentAddressLine1;
    }

    public String getFormattedSSNValue() {
        return formattedSSNValue;
    }

    public void setFormattedSSNValue(final String formattedSSNValue) {
        this.formattedSSNValue = formattedSSNValue;
    }

    public String getFormattedITINValue() {
        return formattedITINValue;
    }

    public void setFormattedITINValue(final String formattedITINValue) {
        this.formattedITINValue = formattedITINValue;
    }

    public String getChapter3StatusCode() {
        return chapter3StatusCode;
    }

    public void setChapter3StatusCode(final String chapter3StatusCode) {
        this.chapter3StatusCode = chapter3StatusCode;
    }

    public String getChapter4ExemptionCode() {
        return chapter4ExemptionCode;
    }

    public void setChapter4ExemptionCode(final String chapter4ExemptionCode) {
        this.chapter4ExemptionCode = chapter4ExemptionCode;
    }

    public String getPayerEIN() {
        return payerEIN;
    }

    public void setPayerEIN(final String payerEIN) {
        this.payerEIN = payerEIN;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(final String stateCode) {
        this.stateCode = stateCode;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public boolean isDemographicRowWritten() {
        return demographicRowWritten;
    }

    public void setDemographicRowWritten(final boolean demographicRowWritten) {
        this.demographicRowWritten = demographicRowWritten;
    }

    public SprintaxPayment getPayment() {
        return currentPayment;
    }

    public SprintaxPayment getCurrentPayment() {
        return currentPayment;
    }

    public void setCurrentPayment(final SprintaxPayment currentPayment) {
        this.currentPayment = currentPayment;
    }

    public TaxBoxUpdates getCurrentTaxBoxUpdates() {
        return currentTaxBoxUpdates;
    }

    public void setCurrentTaxBoxUpdates(final TaxBoxUpdates currentTaxBoxUpdates) {
        this.currentTaxBoxUpdates = currentTaxBoxUpdates;
    }

    public String getForm1042SBox() {
        return (currentTaxBoxUpdates != null) ? currentTaxBoxUpdates.getForm1042SBoxToUse() : null;
    }

    public String getForm1042SOverriddenBox() {
        return (currentTaxBoxUpdates != null) ? currentTaxBoxUpdates.getForm1042SOverriddenBox() : null;
    }

    public String getCanadianProvinceName() {
        return StringUtils.equalsIgnoreCase(getVendorForeignCountryCode(), CUTaxConstants.CANADA_FIPS_COUNTRY_CODE)
                ? getVendorForeignProvinceName() : null;
    }



    public enum SprintaxField implements TaxDtoFieldEnum {
        vendorFirstName,
        vendorLastName,
        vendorEmailAddress,
        payeeId,
        formattedSSNValue,
        formattedITINValue,
        vendorLine1Address,
        vendorLine2Address,
        vendorCityName,
        vendorStateCode,
        vendorZipCode,
        vendorForeignLine1Address,
        vendorForeignLine2Address,
        vendorForeignCityName,
        vendorForeignProvinceName,
        vendorForeignZipCode,
        vendorForeignCountryCode,
        canadianProvinceName,
        chapter4ExemptionCode,
        chapter3StatusCode,
        vendorChapter4StatusCode,
        vendorGIIN,
        payerEIN,
        payment_uniqueFormId("payment.uniqueFormId"),
        payment_incomeCodeForOutput("payment.incomeCodeForOutput"),
        payment_grossAmount("payment.grossAmount"),
        payment_chapter3ExemptionCode("payment.chapter3ExemptionCode"),
        payment_chapter3TaxRate("payment.chapter3TaxRate"),
        payment_federalTaxWithheldAmount("payment.federalTaxWithheldAmount"),
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
