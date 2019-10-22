package edu.cornell.kfs.tax.businessobject;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

/**
 * Convenience lightweight BO to allow for ORM tools to map to the
 * transaction detail table easily. This class is not meant for
 * persisting or retrieving detail rows; it is only meant for
 * configuring the table column mappings via an ORM tool like OJB.
 */
public class TransactionDetail extends TransientBusinessObjectBase {
    private static final long serialVersionUID = -2558957331012161515L;

    private String transactionDetailId;
    private Integer reportYear;
    private String documentNumber;
    private String documentType;
    private Integer financialDocumentLineNumber;
    private String finObjectCode;
    private KualiDecimal netPaymentAmount;
    private String documentTitle;
    private String vendorTaxNumber;
    private String incomeCode;
    private String incomeCodeSubType;
    private String dvCheckStubText;
    private String payeeId;
    private String vendorName;
    private String parentVendorName;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private Boolean vendorForeignIndicator;
    private String vendorEmailAddress;
    private String vendorChapter4StatusCode;
    private String vendorGIIN;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorZipCode;
    private String vendorForeignLine1Address;
    private String vendorForeignLine2Address;
    private String vendorForeignCityName;
    private String vendorForeignZipCode;
    private String vendorForeignProvinceName;
    private String vendorForeignCountryCode;
    private Boolean nraPaymentIndicator;
    private java.sql.Date paymentDate;
    private String paymentPayeeName;
    private String incomeClassCode;
    private Boolean incomeTaxTreatyExemptIndicator;
    private Boolean foreignSourceIncomeIndicator;
    private KualiDecimal federalIncomeTaxPercent;
    private String paymentDescription;
    private String paymentLine1Address;
    private String paymentCountryName;
    private String chartCode;
    private String accountNumber;
    private String initiatorNetId;
    private String form1099Box;
    private String form1099OverriddenBox;
    private String form1042SBox;
    private String form1042SOverriddenBox;
    private String paymentReasonCode;

    public String getTransactionDetailId() {
        return transactionDetailId;
    }

    public void setTransactionDetailId(String transactionDetailId) {
        this.transactionDetailId = transactionDetailId;
    }

    public Integer getReportYear() {
        return reportYear;
    }

    public void setReportYear(Integer reportYear) {
        this.reportYear = reportYear;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public Integer getFinancialDocumentLineNumber() {
        return financialDocumentLineNumber;
    }

    public void setFinancialDocumentLineNumber(Integer financialDocumentLineNumber) {
        this.financialDocumentLineNumber = financialDocumentLineNumber;
    }

    public String getFinObjectCode() {
        return finObjectCode;
    }

    public void setFinObjectCode(String finObjectCode) {
        this.finObjectCode = finObjectCode;
    }

    public KualiDecimal getNetPaymentAmount() {
        return netPaymentAmount;
    }

    public void setNetPaymentAmount(KualiDecimal netPaymentAmount) {
        this.netPaymentAmount = netPaymentAmount;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public String getIncomeCodeSubType() {
        return incomeCodeSubType;
    }

    public void setIncomeCodeSubType(String incomeCodeSubType) {
        this.incomeCodeSubType = incomeCodeSubType;
    }

    public String getDvCheckStubText() {
        return dvCheckStubText;
    }

    public void setDvCheckStubText(String dvCheckStubText) {
        this.dvCheckStubText = dvCheckStubText;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getParentVendorName() {
        return parentVendorName;
    }

    public void setParentVendorName(String parentVendorName) {
        this.parentVendorName = parentVendorName;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getVendorOwnershipCategoryCode() {
        return vendorOwnershipCategoryCode;
    }

    public void setVendorOwnershipCategoryCode(String vendorOwnershipCategoryCode) {
        this.vendorOwnershipCategoryCode = vendorOwnershipCategoryCode;
    }

    public Boolean getVendorForeignIndicator() {
        return vendorForeignIndicator;
    }

    public void setVendorForeignIndicator(Boolean vendorForeignIndicator) {
        this.vendorForeignIndicator = vendorForeignIndicator;
    }

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorChapter4StatusCode() {
        return vendorChapter4StatusCode;
    }

    public void setVendorChapter4StatusCode(String vendorChapter4StatusCode) {
        this.vendorChapter4StatusCode = vendorChapter4StatusCode;
    }

    public String getVendorGIIN() {
        return vendorGIIN;
    }

    public void setVendorGIIN(String vendorGIIN) {
        this.vendorGIIN = vendorGIIN;
    }

    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    public void setVendorLine1Address(String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    public void setVendorLine2Address(String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    public String getVendorCityName() {
        return vendorCityName;
    }

    public void setVendorCityName(String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    public String getVendorStateCode() {
        return vendorStateCode;
    }

    public void setVendorStateCode(String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    public String getVendorZipCode() {
        return vendorZipCode;
    }

    public void setVendorZipCode(String vendorZipCode) {
        this.vendorZipCode = vendorZipCode;
    }

    public String getVendorForeignLine1Address() {
        return vendorForeignLine1Address;
    }

    public void setVendorForeignLine1Address(String vendorForeignLine1Address) {
        this.vendorForeignLine1Address = vendorForeignLine1Address;
    }

    public String getVendorForeignLine2Address() {
        return vendorForeignLine2Address;
    }

    public void setVendorForeignLine2Address(String vendorForeignLine2Address) {
        this.vendorForeignLine2Address = vendorForeignLine2Address;
    }

    public String getVendorForeignCityName() {
        return vendorForeignCityName;
    }

    public void setVendorForeignCityName(String vendorForeignCityName) {
        this.vendorForeignCityName = vendorForeignCityName;
    }

    public String getVendorForeignZipCode() {
        return vendorForeignZipCode;
    }

    public void setVendorForeignZipCode(String vendorForeignZipCode) {
        this.vendorForeignZipCode = vendorForeignZipCode;
    }

    public String getVendorForeignProvinceName() {
        return vendorForeignProvinceName;
    }

    public void setVendorForeignProvinceName(String vendorForeignProvinceName) {
        this.vendorForeignProvinceName = vendorForeignProvinceName;
    }

    public String getVendorForeignCountryCode() {
        return vendorForeignCountryCode;
    }

    public void setVendorForeignCountryCode(String vendorForeignCountryCode) {
        this.vendorForeignCountryCode = vendorForeignCountryCode;
    }

    public Boolean getNraPaymentIndicator() {
        return nraPaymentIndicator;
    }

    public void setNraPaymentIndicator(Boolean nraPaymentIndicator) {
        this.nraPaymentIndicator = nraPaymentIndicator;
    }

    public java.sql.Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(java.sql.Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentPayeeName() {
        return paymentPayeeName;
    }

    public void setPaymentPayeeName(String paymentPayeeName) {
        this.paymentPayeeName = paymentPayeeName;
    }

    public String getIncomeClassCode() {
        return incomeClassCode;
    }

    public void setIncomeClassCode(String incomeClassCode) {
        this.incomeClassCode = incomeClassCode;
    }

    public Boolean getIncomeTaxTreatyExemptIndicator() {
        return incomeTaxTreatyExemptIndicator;
    }

    public void setIncomeTaxTreatyExemptIndicator(Boolean incomeTaxTreatyExemptIndicator) {
        this.incomeTaxTreatyExemptIndicator = incomeTaxTreatyExemptIndicator;
    }

    public Boolean getForeignSourceIncomeIndicator() {
        return foreignSourceIncomeIndicator;
    }

    public void setForeignSourceIncomeIndicator(Boolean foreignSourceIncomeIndicator) {
        this.foreignSourceIncomeIndicator = foreignSourceIncomeIndicator;
    }

    public KualiDecimal getFederalIncomeTaxPercent() {
        return federalIncomeTaxPercent;
    }

    public void setFederalIncomeTaxPercent(KualiDecimal federalIncomeTaxPercent) {
        this.federalIncomeTaxPercent = federalIncomeTaxPercent;
    }

    public String getPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }

    public String getPaymentLine1Address() {
        return paymentLine1Address;
    }

    public void setPaymentLine1Address(String paymentLine1Address) {
        this.paymentLine1Address = paymentLine1Address;
    }

    public String getPaymentCountryName() {
        return paymentCountryName;
    }

    public void setPaymentCountryName(String paymentCountryName) {
        this.paymentCountryName = paymentCountryName;
    }

    public String getChartCode() {
        return chartCode;
    }

    public void setChartCode(String chartCode) {
        this.chartCode = chartCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getInitiatorNetId() {
        return initiatorNetId;
    }

    public void setInitiatorNetId(String initiatorNetId) {
        this.initiatorNetId = initiatorNetId;
    }

    public String getForm1099Box() {
        return form1099Box;
    }

    public void setForm1099Box(String form1099Box) {
        this.form1099Box = form1099Box;
    }

    public String getForm1099OverriddenBox() {
        return form1099OverriddenBox;
    }

    public void setForm1099OverriddenBox(String form1099OverriddenBox) {
        this.form1099OverriddenBox = form1099OverriddenBox;
    }

    public String getForm1042SBox() {
        return form1042SBox;
    }

    public void setForm1042SBox(String form1042sBox) {
        form1042SBox = form1042sBox;
    }

    public String getForm1042SOverriddenBox() {
        return form1042SOverriddenBox;
    }

    public void setForm1042SOverriddenBox(String form1042sOverriddenBox) {
        form1042SOverriddenBox = form1042sOverriddenBox;
    }

    public String getPaymentReasonCode() {
        return paymentReasonCode;
    }

    public void setPaymentReasonCode(String paymentReasonCode) {
        this.paymentReasonCode = paymentReasonCode;
    }

}
