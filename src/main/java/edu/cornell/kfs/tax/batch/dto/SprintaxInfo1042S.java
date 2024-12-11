package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

public class SprintaxInfo1042S {

    private String rowId;
    private String taxId;
    private String payeeId;
    private Integer vendorHeaderId;
    private Integer vendorDetailId;
    //private String incomeCode;
    //private String incomeCodeSubType;
    //private String taxTreatyExemptIncomeYesNo;
    //private String foreignSourceIncomeYesNo;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String paymentAddressLine1;
    //private String objectCode;
    //private String chartCode;
    //private String accountNumber;
    //private String docType;
    //private String docNumber;
    //private String paymentReasonCode;
    //private String dvCheckStubText;
    //private Integer docLineNumber;
    //private KualiDecimal fedIncomeTaxPercent;
    //private KualiDecimal paymentAmount;
    //private Date paymentDate;
    private String vendorNameForOutput;
    private String parentVendorNameForOutput;
    private String vendorLastName;
    private String vendorFirstName;
    private String vendorEmailAddress;
    private String vendorUSAddressLine1;
    private String vendorForeignAddressLine1;
    private String vendorForeignAddressLine2;
    private String vendorForeignCityName;
    private String vendorForeignZipCode;
    private String vendorForeignProvinceName;
    private String vendorForeignCountryCode;
    private String formattedSSNValue;
    private String formattedITINValue;
    private String chapter3StatusCode;
    //private String chapter3ExemptionCode;
    private String chapter4ExemptionCode;
    //private String incomeCodeForOutput;
    private String taxEINValue;
    private String stateCode;
    private Date endDate;
    //private KualiDecimal chapter3TaxRate;
    //private KualiDecimal grossAmount;
    //private KualiDecimal federalTaxWithheldAmount;
    //private KualiDecimal stateIncomeTaxWithheldAmount;

    private SprintaxPayment1042S currentPayment;

    /*public SprintaxFileRow1042S() {
        super(TaxUtils.buildDefaultAmountFormatForFileOutput(), TaxUtils.buildDefaultPercentFormatForFileOutput(),
                TaxUtils.buildDefaultDateFormatForFileOutput());
    }*/

    /*@Override
    public Map<String, String> generateFileRowValues(final String sectionName) {
        Validate.notBlank(sectionName, "sectionName cannot be blank");
        switch (sectionName) {
            case TaxFileSections.SPRINTAX_BIOGRAPHIC_ROW_1042S :
                return generateValuesForBiographicRow();

            case TaxFileSections.SPRINTAX_DETAIL_ROW_1042S :
                return generateValuesForDetailRow();

            default :
                throw new IllegalArgumentException("Unrecognized Sprintax file section: " + sectionName);
        }
    }

    private Map<String, String> generateValuesForBiographicRow() {
        return Map.ofEntries(
        );
    }

    private Map<String, String> generateValuesForDetailRow() {
        return Map.ofEntries(
        );
    }*/

    public String getRowId() {
        return rowId;
    }

    public void setRowId(final String rowId) {
        this.rowId = rowId;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(final String taxId) {
        this.taxId = taxId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(final String payeeId) {
        this.payeeId = payeeId;
    }

    public Integer getVendorHeaderId() {
        return vendorHeaderId;
    }

    public void setVendorHeaderId(Integer vendorHeaderId) {
        this.vendorHeaderId = vendorHeaderId;
    }

    public Integer getVendorDetailId() {
        return vendorDetailId;
    }

    public void setVendorDetailId(Integer vendorDetailId) {
        this.vendorDetailId = vendorDetailId;
    }

    /*public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(final String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public String getIncomeCodeSubType() {
        return incomeCodeSubType;
    }

    public void setIncomeCodeSubType(final String incomeCodeSubType) {
        this.incomeCodeSubType = incomeCodeSubType;
    }

    public String getTaxTreatyExemptIncomeYesNo() {
        return taxTreatyExemptIncomeYesNo;
    }

    public void setTaxTreatyExemptIncomeYesNo(final String taxTreatyExemptIncomeYesNo) {
        this.taxTreatyExemptIncomeYesNo = taxTreatyExemptIncomeYesNo;
    }

    public String getForeignSourceIncomeYesNo() {
        return foreignSourceIncomeYesNo;
    }

    public void setForeignSourceIncomeYesNo(final String foreignSourceIncomeYesNo) {
        this.foreignSourceIncomeYesNo = foreignSourceIncomeYesNo;
    }*/

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

    public String getPaymentAddressLine1() {
        return paymentAddressLine1;
    }

    public void setPaymentAddressLine1(final String paymentAddressLine1) {
        this.paymentAddressLine1 = paymentAddressLine1;
    }

    /*public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(final String objectCode) {
        this.objectCode = objectCode;
    }

    public String getChartCode() {
        return chartCode;
    }

    public void setChartCode(final String chartCode) {
        this.chartCode = chartCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(final String docType) {
        this.docType = docType;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(final String docNumber) {
        this.docNumber = docNumber;
    }

    public String getPaymentReasonCode() {
        return paymentReasonCode;
    }

    public void setPaymentReasonCode(final String paymentReasonCode) {
        this.paymentReasonCode = paymentReasonCode;
    }

    public String getDvCheckStubText() {
        return dvCheckStubText;
    }

    public void setDvCheckStubText(final String dvCheckStubText) {
        this.dvCheckStubText = dvCheckStubText;
    }

    public Integer getDocLineNumber() {
        return docLineNumber;
    }

    public void setDocLineNumber(final Integer docLineNumber) {
        this.docLineNumber = docLineNumber;
    }

    public KualiDecimal getFedIncomeTaxPercent() {
        return fedIncomeTaxPercent;
    }

    public void setFedIncomeTaxPercent(final KualiDecimal fedIncomeTaxPercent) {
        this.fedIncomeTaxPercent = fedIncomeTaxPercent;
    }

    public KualiDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final KualiDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(final Date paymentDate) {
        this.paymentDate = paymentDate;
    }*/

    public String getVendorNameForOutput() {
        return vendorNameForOutput;
    }

    public void setVendorNameForOutput(final String vendorNameForOutput) {
        this.vendorNameForOutput = vendorNameForOutput;
    }

    public String getParentVendorNameForOutput() {
        return parentVendorNameForOutput;
    }

    public void setParentVendorNameForOutput(final String parentVendorNameForOutput) {
        this.parentVendorNameForOutput = parentVendorNameForOutput;
    }

    public String getVendorLastName() {
        return vendorLastName;
    }

    public void setVendorLastName(final String vendorLastName) {
        this.vendorLastName = vendorLastName;
    }

    public String getVendorFirstName() {
        return vendorFirstName;
    }

    public void setVendorFirstName(final String vendorFirstName) {
        this.vendorFirstName = vendorFirstName;
    }

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(final String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorUSAddressLine1() {
        return vendorUSAddressLine1;
    }

    public void setVendorUSAddressLine1(final String vendorUSAddressLine1) {
        this.vendorUSAddressLine1 = vendorUSAddressLine1;
    }

    public String getVendorForeignAddressLine1() {
        return vendorForeignAddressLine1;
    }

    public void setVendorForeignAddressLine1(final String vendorForeignAddressLine1) {
        this.vendorForeignAddressLine1 = vendorForeignAddressLine1;
    }

    public String getVendorForeignAddressLine2() {
        return vendorForeignAddressLine2;
    }

    public void setVendorForeignAddressLine2(final String vendorForeignAddressLine2) {
        this.vendorForeignAddressLine2 = vendorForeignAddressLine2;
    }

    public String getVendorForeignCityName() {
        return vendorForeignCityName;
    }

    public void setVendorForeignCityName(final String vendorForeignCityName) {
        this.vendorForeignCityName = vendorForeignCityName;
    }

    public String getVendorForeignZipCode() {
        return vendorForeignZipCode;
    }

    public void setVendorForeignZipCode(final String vendorForeignZipCode) {
        this.vendorForeignZipCode = vendorForeignZipCode;
    }

    public String getVendorForeignProvinceName() {
        return vendorForeignProvinceName;
    }

    public void setVendorForeignProvinceName(final String vendorForeignProvinceName) {
        this.vendorForeignProvinceName = vendorForeignProvinceName;
    }

    public String getVendorForeignCountryCode() {
        return vendorForeignCountryCode;
    }

    public void setVendorForeignCountryCode(final String vendorForeignCountryCode) {
        this.vendorForeignCountryCode = vendorForeignCountryCode;
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

    /*public String getChapter3ExemptionCode() {
        return chapter3ExemptionCode;
    }

    public void setChapter3ExemptionCode(final String chapter3ExemptionCode) {
        this.chapter3ExemptionCode = chapter3ExemptionCode;
    }*/

    public String getChapter4ExemptionCode() {
        return chapter4ExemptionCode;
    }

    public void setChapter4ExemptionCode(final String chapter4ExemptionCode) {
        this.chapter4ExemptionCode = chapter4ExemptionCode;
    }

    /*public String getIncomeCodeForOutput() {
        return incomeCodeForOutput;
    }

    public void setIncomeCodeForOutput(final String incomeCodeForOutput) {
        this.incomeCodeForOutput = incomeCodeForOutput;
    }*/

    public String getTaxEINValue() {
        return taxEINValue;
    }

    public void setTaxEINValue(final String taxEINValue) {
        this.taxEINValue = taxEINValue;
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

    /*public KualiDecimal getChapter3TaxRate() {
        return chapter3TaxRate;
    }

    public void setChapter3TaxRate(final KualiDecimal chapter3TaxRate) {
        this.chapter3TaxRate = chapter3TaxRate;
    }

    public KualiDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(final KualiDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public KualiDecimal getFederalTaxWithheldAmount() {
        return federalTaxWithheldAmount;
    }

    public void setFederalTaxWithheldAmount(final KualiDecimal federalTaxWithheldAmount) {
        this.federalTaxWithheldAmount = federalTaxWithheldAmount;
    }

    public KualiDecimal getStateIncomeTaxWithheldAmount() {
        return stateIncomeTaxWithheldAmount;
    }

    public void setStateIncomeTaxWithheldAmount(final KualiDecimal stateIncomeTaxWithheldAmount) {
        this.stateIncomeTaxWithheldAmount = stateIncomeTaxWithheldAmount;
    }*/

    public SprintaxPayment1042S getCurrentPayment() {
        return currentPayment;
    }

    public void setCurrentPayment(final SprintaxPayment1042S currentPayment) {
        this.currentPayment = currentPayment;
    }

}
