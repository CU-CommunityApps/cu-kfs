package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class SprintaxPayment1042S {

    private String incomeCode;
    private String incomeCodeSubType;
    private String incomeCodeForOutput;
    private Boolean taxTreatyExemptIncome;
    private Boolean foreignSourceIncome;
    private String objectCode;
    private String chartCode;
    private String accountNumber;
    private String docType;
    private String docNumber;
    private String paymentReasonCode;
    private String dvCheckStubText;
    private Integer docLineNumber;
    private KualiDecimal fedIncomeTaxPercent;
    private KualiDecimal paymentAmount;
    private Date paymentDate;
    private String chapter3ExemptionCode;
    private KualiDecimal chapter3TaxRate;
    private KualiDecimal grossAmount;
    private KualiDecimal federalTaxWithheldAmount;
    private KualiDecimal stateIncomeTaxWithheldAmount;

    public String getIncomeCode() {
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

    public String getIncomeCodeForOutput() {
        return incomeCodeForOutput;
    }

    public void setIncomeCodeForOutput(final String incomeCodeForOutput) {
        this.incomeCodeForOutput = incomeCodeForOutput;
    }

    public Boolean getTaxTreatyExemptIncome() {
        return taxTreatyExemptIncome;
    }

    public void setTaxTreatyExemptIncome(final Boolean taxTreatyExemptIncome) {
        this.taxTreatyExemptIncome = taxTreatyExemptIncome;
    }

    public Boolean getForeignSourceIncome() {
        return foreignSourceIncome;
    }

    public void setForeignSourceIncome(final Boolean foreignSourceIncome) {
        this.foreignSourceIncome = foreignSourceIncome;
    }

    public String getObjectCode() {
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
    }

    public String getChapter3ExemptionCode() {
        return chapter3ExemptionCode;
    }

    public void setChapter3ExemptionCode(final String chapter3ExemptionCode) {
        this.chapter3ExemptionCode = chapter3ExemptionCode;
    }

    public KualiDecimal getChapter3TaxRate() {
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

    public void addToGrossAmount(final KualiDecimal grossAmount) {
        if (this.grossAmount != null) {
            this.grossAmount = this.grossAmount.add(grossAmount);
        } else {
            setGrossAmount(grossAmount);
        }
    }

    public KualiDecimal getFederalTaxWithheldAmount() {
        return federalTaxWithheldAmount;
    }

    public void setFederalTaxWithheldAmount(final KualiDecimal federalTaxWithheldAmount) {
        this.federalTaxWithheldAmount = federalTaxWithheldAmount;
    }

    public void addToFederalTaxWithheldAmount(final KualiDecimal federalTaxWithheldAmount) {
        if (this.federalTaxWithheldAmount != null) {
            this.federalTaxWithheldAmount = this.federalTaxWithheldAmount.add(federalTaxWithheldAmount);
        } else {
            setFederalTaxWithheldAmount(federalTaxWithheldAmount);
        }
    }

    public KualiDecimal getStateIncomeTaxWithheldAmount() {
        return stateIncomeTaxWithheldAmount;
    }

    public void setStateIncomeTaxWithheldAmount(final KualiDecimal stateIncomeTaxWithheldAmount) {
        this.stateIncomeTaxWithheldAmount = stateIncomeTaxWithheldAmount;
    }

    public void addToStateIncomeTaxWithheldAmount(final KualiDecimal stateIncomeTaxWithheldAmount) {
        if (this.stateIncomeTaxWithheldAmount != null) {
            this.stateIncomeTaxWithheldAmount = this.stateIncomeTaxWithheldAmount.add(stateIncomeTaxWithheldAmount);
        } else {
            setStateIncomeTaxWithheldAmount(stateIncomeTaxWithheldAmount);
        }
    }

}
