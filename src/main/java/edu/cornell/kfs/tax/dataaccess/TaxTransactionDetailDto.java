package edu.cornell.kfs.tax.dataaccess;

import edu.cornell.kfs.tax.dataaccess.impl.SprintaxPaymentRowProcessor;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.KRADConstants;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Map;

public class TaxTransactionDetailDto {
    
    private String[] detailStrings;
    private Integer[] detailInts;
    private BigDecimal[] detailBigDecimals;
    private java.sql.Date[] detailDates;

    // Variables pertaining to detail fields that always need to be retrieved for processing.
    private String rowId;
    private String taxId;
    private String payeeId;
    private String incomeCode;
    private String incomeCodeSubType;
    private String taxTreatyExemptIncomeYesNo;
    private String foreignSourceIncomeYesNo;
    private String vendorOwnershipCode;
    private String paymentAddressLine1;
    private String objectCode;
    private String chartCode;
    private String accountNumber;
    private String docType;
    private String docNumber;
    private String paymentReasonCode;
    private String dvCheckStubText;
    private Integer docLineNumber;
    private BigDecimal fedIncomeTaxPct;
    private BigDecimal paymentAmount;
    private java.sql.Date paymentDate;

    // Variables pertaining to fields that are derived from the processing of other fields.
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
    private String chapter3ExemptionCode;
    private String chapter4ExemptionCode;
    private String incomeCodeForOutput;
    private String taxEINValue;
    private String stateCode;
    private java.sql.Date endDate;
    private BigDecimal chapter3TaxRate;
    private BigDecimal grossAmount;
    private BigDecimal ftwAmount;
    private BigDecimal sitwAmount;

    String rowKey;

    public void initialize(Map<String, SprintaxPaymentRowProcessor.RecordPiece> complexPieces, TaxTableRow.TransactionDetailRow detailRow) {
        rowId = complexPieces.get(detailRow.transactionDetailId.propertyName).getValue();
        docNumber = complexPieces.get(detailRow.documentNumber.propertyName).getValue();
        docType = complexPieces.get(detailRow.documentType.propertyName).getValue();
        docLineNumber = Integer.valueOf(complexPieces.get(detailRow.financialDocumentLineNumber.propertyName).getValue());
        objectCode = complexPieces.get(detailRow.finObjectCode.propertyName).getValue();
        paymentAmount = complexPieces.get(detailRow.netPaymentAmount.propertyName).getNumericValue();
        taxId = complexPieces.get(detailRow.vendorTaxNumber.propertyName).getValue();
        incomeCode = complexPieces.get(detailRow.incomeCode.propertyName).getValue();
        incomeCodeSubType = complexPieces.get(detailRow.incomeCodeSubType.propertyName).getValue();
        dvCheckStubText = complexPieces.get(detailRow.dvCheckStubText.propertyName).getValue();
        payeeId = complexPieces.get(detailRow.payeeId.propertyName).getValue();
        vendorOwnershipCode = complexPieces.get(detailRow.vendorOwnershipCode.propertyName).getValue();
        paymentDate = complexPieces.get(detailRow.paymentDate.propertyName).getDateValue();
        taxTreatyExemptIncomeYesNo = complexPieces.get(detailRow.incomeTaxTreatyExemptIndicator.propertyName).getValue();
        foreignSourceIncomeYesNo = complexPieces.get(detailRow.foreignSourceIncomeIndicator.propertyName).getValue();
        fedIncomeTaxPct = complexPieces.get(detailRow.federalIncomeTaxPercent.propertyName).getNumericValue();
        paymentAddressLine1 = complexPieces.get(detailRow.paymentLine1Address.propertyName).getValue();
        chartCode = complexPieces.get(detailRow.chartCode.propertyName).getValue();
        accountNumber = complexPieces.get(detailRow.accountNumber.propertyName).getValue();
        paymentReasonCode = complexPieces.get(detailRow.paymentReasonCode.propertyName).getValue();

        rowKey =  new StringBuilder(100)
                .append(rowId)
                .append(' ').append(docNumber)
                .append(' ').append(docLineNumber)
                .append(' ').append(payeeId)
                .append(' ').append(docType)
                .append(' ').append(objectCode)
                .append(' ').append(incomeCode)
                .append(' ').append(incomeCodeSubType)
                .append(' ').append(paymentAmount)
                .append(' ').append(paymentDate)
                .toString();

        // Retrieve the various derived "pieces" that will be needed for the processing.
//        vendorLastName = complexPieces.get(derivedValues.vendorLastName.propertyName);
//        vendorFirstName = complexPieces.get(derivedValues.vendorFirstName.propertyName);
//        vendorEmailAddress = complexPieces.get(derivedValues.vendorEmailAddress.propertyName);
//        vendorUSAddressLine1 = complexPieces.get(derivedValues.vendorUSAddressLine1.propertyName);
//        vendorForeignAddressLine1 = complexPieces.get(derivedValues.vendorForeignAddressLine1.propertyName);
//        vendorForeignAddressLine2 = complexPieces.get(derivedValues.vendorForeignAddressLine2.propertyName);
//        vendorForeignCityName = complexPieces.get(derivedValues.vendorForeignCityName.propertyName);
//        vendorForeignZipCode = complexPieces.get(derivedValues.vendorForeignZipCode.propertyName);
//        vendorForeignProvinceName = complexPieces.get(derivedValues.vendorForeignProvinceName.propertyName);
//        vendorForeignCountryCode = complexPieces.get(derivedValues.vendorForeignCountryCode.propertyName);
//        formattedSSNValue = complexPieces.get(derivedValues.ssn.propertyName);
//        formattedITINValue = complexPieces.get(derivedValues.itin.propertyName);
//        chapter3StatusCode = complexPieces.get(derivedValues.chapter3StatusCode.propertyName);
//        chapter3ExemptionCode = complexPieces.get(derivedValues.chapter3ExemptionCode.propertyName);
//        chapter4ExemptionCode = complexPieces.get(derivedValues.chapter4ExemptionCode.propertyName);
//        incomeCodeForOutput = complexPieces.get(derivedValues.incomeCode.propertyName);
//        taxEINValue = complexPieces.get(derivedValues.ein.propertyName);
//        chapter3TaxRate = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.chapter3TaxRate.propertyName);
//        grossAmount = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.grossAmount.propertyName);
//        ftwAmount = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.fedTaxWithheldAmount.propertyName);
//        ftwAmountP.negateStringValue = true;
//        sitwAmount = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.stateIncomeTaxWithheldAmount.propertyName);
//        sitwAmountP.negateStringValue = true;
//        stateCode = complexPieces.get(derivedValues.stateCode.propertyName);
//        endDate = (SprintaxPaymentRowProcessor.RecordPiece1042SDate) complexPieces.get(derivedValues.endDate.propertyName);
    }

    public String[] getDetailStrings() {
        return detailStrings;
    }

    public void setDetailStrings(String[] detailStrings) {
        this.detailStrings = detailStrings;
    }

    public Integer[] getDetailInts() {
        return detailInts;
    }

    public void setDetailInts(Integer[] detailInts) {
        this.detailInts = detailInts;
    }

    public BigDecimal[] getDetailBigDecimals() {
        return detailBigDecimals;
    }

    public void setDetailBigDecimals(BigDecimal[] detailBigDecimals) {
        this.detailBigDecimals = detailBigDecimals;
    }

    public Date[] getDetailDates() {
        return detailDates;
    }

    public void setDetailDates(Date[] detailDates) {
        this.detailDates = detailDates;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
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

    public String getTaxTreatyExemptIncomeYesNo() {
        return taxTreatyExemptIncomeYesNo;
    }

    public void setTaxTreatyExemptIncomeYesNo(String taxTreatyExemptIncomeYesNo) {
        this.taxTreatyExemptIncomeYesNo = taxTreatyExemptIncomeYesNo;
    }

    public String getForeignSourceIncomeYesNo() {
        return foreignSourceIncomeYesNo;
    }

    public void setForeignSourceIncomeYesNo(String foreignSourceIncomeYesNo) {
        this.foreignSourceIncomeYesNo = foreignSourceIncomeYesNo;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getPaymentAddressLine1() {
        return paymentAddressLine1;
    }

    public void setPaymentAddressLine1(String paymentAddressLine1) {
        this.paymentAddressLine1 = paymentAddressLine1;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
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

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getPaymentReasonCode() {
        return paymentReasonCode;
    }

    public void setPaymentReasonCode(String paymentReasonCode) {
        this.paymentReasonCode = paymentReasonCode;
    }

    public String getDvCheckStubText() {
        return dvCheckStubText;
    }

    public void setDvCheckStubText(String dvCheckStubText) {
        this.dvCheckStubText = dvCheckStubText;
    }

    public Integer getDocLineNumber() {
        return docLineNumber;
    }

    public void setDocLineNumber(Integer docLineNumber) {
        this.docLineNumber = docLineNumber;
    }

    public BigDecimal getFedIncomeTaxPct() {
        return fedIncomeTaxPct;
    }

    public void setFedIncomeTaxPct(BigDecimal fedIncomeTaxPct) {
        this.fedIncomeTaxPct = fedIncomeTaxPct;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getVendorLastName() {
        return vendorLastName;
    }

    public void setVendorLastName(String vendorLastName) {
        this.vendorLastName = vendorLastName;
    }

    public String getVendorFirstName() {
        return vendorFirstName;
    }

    public void setVendorFirstName(String vendorFirstName) {
        this.vendorFirstName = vendorFirstName;
    }

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorUSAddressLine1() {
        return vendorUSAddressLine1;
    }

    public void setVendorUSAddressLine1(String vendorUSAddressLine1) {
        this.vendorUSAddressLine1 = vendorUSAddressLine1;
    }

    public String getVendorForeignAddressLine1() {
        return vendorForeignAddressLine1;
    }

    public void setVendorForeignAddressLine1(String vendorForeignAddressLine1) {
        this.vendorForeignAddressLine1 = vendorForeignAddressLine1;
    }

    public String getVendorForeignAddressLine2() {
        return vendorForeignAddressLine2;
    }

    public void setVendorForeignAddressLine2(String vendorForeignAddressLine2) {
        this.vendorForeignAddressLine2 = vendorForeignAddressLine2;
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

    public String getFormattedSSNValue() {
        return formattedSSNValue;
    }

    public void setFormattedSSNValue(String formattedSSNValue) {
        this.formattedSSNValue = formattedSSNValue;
    }

    public String getFormattedITINValue() {
        return formattedITINValue;
    }

    public void setFormattedITINValue(String formattedITINValue) {
        this.formattedITINValue = formattedITINValue;
    }

    public String getChapter3StatusCode() {
        return chapter3StatusCode;
    }

    public void setChapter3StatusCode(String chapter3StatusCode) {
        this.chapter3StatusCode = chapter3StatusCode;
    }

    public String getChapter3ExemptionCode() {
        return chapter3ExemptionCode;
    }

    public void setChapter3ExemptionCode(String chapter3ExemptionCode) {
        this.chapter3ExemptionCode = chapter3ExemptionCode;
    }

    public String getChapter4ExemptionCode() {
        return chapter4ExemptionCode;
    }

    public void setChapter4ExemptionCode(String chapter4ExemptionCode) {
        this.chapter4ExemptionCode = chapter4ExemptionCode;
    }

    public String getIncomeCodeForOutput() {
        return incomeCodeForOutput;
    }

    public void setIncomeCodeForOutput(String incomeCodeForOutput) {
        this.incomeCodeForOutput = incomeCodeForOutput;
    }

    public String getTaxEINValue() {
        return taxEINValue;
    }

    public void setTaxEINValue(String taxEINValue) {
        this.taxEINValue = taxEINValue;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getChapter3TaxRate() {
        return chapter3TaxRate;
    }

    public void setChapter3TaxRate(BigDecimal chapter3TaxRate) {
        this.chapter3TaxRate = chapter3TaxRate;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getFtwAmount() {
        return ftwAmount;
    }

    public void setFtwAmount(BigDecimal ftwAmount) {
        this.ftwAmount = ftwAmount;
    }

    public BigDecimal getSitwAmount() {
        return sitwAmount;
    }

    public void setSitwAmount(BigDecimal sitwAmount) {
        this.sitwAmount = sitwAmount;
    }

    public String getRowKey() {
        return rowKey;
    }

    public Boolean isTaxTreatyExemptIncome() {
        Boolean taxTreatyExemptIncomeInd = null;
        if (StringUtils.isNotBlank(taxTreatyExemptIncomeYesNo)) {
            taxTreatyExemptIncomeInd = Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(taxTreatyExemptIncomeYesNo));
        }
        return taxTreatyExemptIncomeInd;
    }

    public Boolean isForeignSourceIncome() {
        Boolean foreignSourceIncomeInd = null;
        if (StringUtils.isNotBlank(foreignSourceIncomeYesNo)) {
            foreignSourceIncomeInd = Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(foreignSourceIncomeYesNo));
        }
        return foreignSourceIncomeInd;
    }

    public String getChartAndAccountCombo() {
        return chartCode + "-" + accountNumber;
    }

}
