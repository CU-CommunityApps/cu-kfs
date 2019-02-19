package edu.cornell.kfs.module.ar.document.fixture;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;

import edu.cornell.kfs.module.ar.CuArTestConstants;

public enum InvoiceAccountDetailFixture {
    ACCOUNT_IT_1122333_IT_9000000(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_IT_9988777_IT_9000000(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_9988777,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_PZ_5555555_IT_9000000(CuArTestConstants.CHART_PZ, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_9000000);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String contractControlChartOfAccountsCode;
    public final String contractControlAccountNumber;

    private InvoiceAccountDetailFixture(String chartOfAccountsCode, String accountNumber,
            String contractControlChartOfAccountsCode, String contractControlAccountNumber) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.contractControlChartOfAccountsCode = contractControlChartOfAccountsCode;
        this.contractControlAccountNumber = contractControlAccountNumber;
    }

    public InvoiceAccountDetail toInvoiceAccountDetail(String documentNumber) {
        InvoiceAccountDetail accountDetail = new InvoiceAccountDetail();
        accountDetail.setDocumentNumber(documentNumber);
        accountDetail.setChartOfAccountsCode(chartOfAccountsCode);
        accountDetail.setAccountNumber(accountNumber);
        accountDetail.setAccount(toAccount());
        return accountDetail;
    }

    public Account toAccount() {
        Account account = new Account();
        account.setChartOfAccountsCode(chartOfAccountsCode);
        account.setAccountNumber(accountNumber);
        account.setContractControlFinCoaCode(contractControlChartOfAccountsCode);
        account.setContractControlAccountNumber(contractControlAccountNumber);
        return account;
    }

}
