package edu.cornell.kfs.module.cg.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;

public class AwardAccountExtendedAttribute extends PersistableBusinessObjectExtensionBase {
    private static final long serialVersionUID = 4036900233631513201L;

    private String proposalNumber;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String accountPurchaseOrderNumber;

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountPurchaseOrderNumber() {
        return accountPurchaseOrderNumber;
    }

    public void setAccountPurchaseOrderNumber(String accountPurchaseOrderNumber) {
        this.accountPurchaseOrderNumber = accountPurchaseOrderNumber;
    }

}
