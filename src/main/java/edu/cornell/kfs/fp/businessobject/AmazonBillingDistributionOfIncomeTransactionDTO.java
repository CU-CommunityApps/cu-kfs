package edu.cornell.kfs.fp.businessobject;

import java.io.Serializable;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public class AmazonBillingDistributionOfIncomeTransactionDTO implements Serializable {
    
    private static final long serialVersionUID = -4516110225730719818L;
    
    private String chartCode;
    private String accountNumber;
    private String subAccountNumber;
    private String objectCodeNumber;
    private String subObjectCodeNumber;
    private String projectCodeNumber;
    private String organizationReferenceId;
    private KualiDecimal amount;
    private String lineDescription;
    private boolean transactionInputError;

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

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getObjectCodeNumber() {
        return objectCodeNumber;
    }

    public void setObjectCodeNumber(String objectCodeNumber) {
        this.objectCodeNumber = objectCodeNumber;
    }

    public String getSubObjectCodeNumber() {
        return subObjectCodeNumber;
    }

    public void setSubObjectCodeNumber(String subObjectCodeNumber) {
        this.subObjectCodeNumber = subObjectCodeNumber;
    }

    public String getProjectCodeNumber() {
        return projectCodeNumber;
    }

    public void setProjectCodeNumber(String projectCodeNumber) {
        this.projectCodeNumber = projectCodeNumber;
    }

    public String getOrganizationReferenceId() {
        return organizationReferenceId;
    }

    public void setOrganizationReferenceId(String organizationReferenceId) {
        this.organizationReferenceId = organizationReferenceId;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal amount) {
        this.amount = amount;
    }

    public String getLineDescription() {
        return lineDescription;
    }

    public void setLineDescription(String lineDescription) {
        this.lineDescription = lineDescription;
    }

    public boolean isTransactionInputError() {
        return transactionInputError;
    }

    public void setTransactionInputError(boolean transactionInputError) {
        this.transactionInputError = transactionInputError;
    }
    
}
