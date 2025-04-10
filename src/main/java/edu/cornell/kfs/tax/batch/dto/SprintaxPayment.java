package edu.cornell.kfs.tax.batch.dto;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class SprintaxPayment {

    private String uniqueFormId;
    private String incomeCode;
    private String incomeCodeSubType;
    private String incomeCodeForOutput;
    private Boolean taxTreatyExemptIncome;
    private Boolean foreignSourceIncome;
    private KualiDecimal fedIncomeTaxPercent;
    private String chapter3ExemptionCode;
    private KualiDecimal chapter3TaxRate;
    private KualiDecimal grossAmount;
    private KualiDecimal federalTaxWithheldAmount;
    private KualiDecimal stateIncomeTaxWithheldAmount;
    private boolean foundAtLeastOneProcessableTransaction;

    public String getUniqueFormId() {
        return uniqueFormId;
    }

    public void setUniqueFormId(final String uniqueFormId) {
        this.uniqueFormId = uniqueFormId;
    }

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

    public boolean isExplicitlyMarkedAsTaxTreatyExemptIncome() {
        return taxTreatyExemptIncome != null && taxTreatyExemptIncome.booleanValue();
    }

    public boolean isExplicitlyMarkedAsNotTaxTreatyExemptIncome() {
        return taxTreatyExemptIncome != null && !taxTreatyExemptIncome.booleanValue();
    }

    public Boolean getForeignSourceIncome() {
        return foreignSourceIncome;
    }

    public void setForeignSourceIncome(final Boolean foreignSourceIncome) {
        this.foreignSourceIncome = foreignSourceIncome;
    }

    public boolean isExplicitlyMarkedAsForeignSourceIncome() {
        return foreignSourceIncome != null && foreignSourceIncome.booleanValue();
    }

    public KualiDecimal getFedIncomeTaxPercent() {
        return fedIncomeTaxPercent;
    }

    public void setFedIncomeTaxPercent(final KualiDecimal fedIncomeTaxPercent) {
        this.fedIncomeTaxPercent = fedIncomeTaxPercent;
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

    public boolean isFoundAtLeastOneProcessableTransaction() {
        return foundAtLeastOneProcessableTransaction;
    }

    public void setFoundAtLeastOneProcessableTransaction(final boolean foundAtLeastOneProcessableTransaction) {
        this.foundAtLeastOneProcessableTransaction = foundAtLeastOneProcessableTransaction;
    }

}
