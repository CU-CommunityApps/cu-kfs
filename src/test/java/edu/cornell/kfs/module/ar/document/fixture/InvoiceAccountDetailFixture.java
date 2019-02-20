package edu.cornell.kfs.module.ar.document.fixture;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;

import edu.cornell.kfs.module.ar.CuArTestConstants;

public enum InvoiceAccountDetailFixture {
    ACCOUNT_IT_1122333_IT_9000000(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_IT_9988777_IT_9000000(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_9988777,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_PZ_1122333_JJ_9000000(CuArTestConstants.CHART_PZ, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_JJ, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_JJ_1122333_PZ_9000000(CuArTestConstants.CHART_JJ, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_JJ_9988777_PZ_9000000(CuArTestConstants.CHART_JJ, CuArTestConstants.ACCOUNT_9988777,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_PZ_5555555_PZ_9000000(CuArTestConstants.CHART_PZ, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_IT_2000000_IT_3575357(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_2000000,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_JJ_2000000_JJ_3575357(CuArTestConstants.CHART_JJ, CuArTestConstants.ACCOUNT_2000000,
            CuArTestConstants.CHART_JJ, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_JJ_5555555_JJ_3575357(CuArTestConstants.CHART_JJ, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_JJ, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_HG_2000000_HG_3575357(CuArTestConstants.CHART_HG, CuArTestConstants.ACCOUNT_2000000,
            CuArTestConstants.CHART_HG, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_BC_2000000_BC_3575357(CuArTestConstants.CHART_BC, CuArTestConstants.ACCOUNT_2000000,
            CuArTestConstants.CHART_BC, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_BC_5555555_BC_3575357(CuArTestConstants.CHART_BC, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_BC, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_IT_5555555_BC_9000000(CuArTestConstants.CHART_IT, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_BC, CuArTestConstants.CC_ACCOUNT_9000000),
    ACCOUNT_BC_1122333_IT_2244668(CuArTestConstants.CHART_BC, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_IT, CuArTestConstants.CC_ACCOUNT_2244668),
    ACCOUNT_HG_1122333_PZ_3575357(CuArTestConstants.CHART_HG, CuArTestConstants.ACCOUNT_1122333,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_HG_5555555_PZ_3575357(CuArTestConstants.CHART_HG, CuArTestConstants.ACCOUNT_5555555,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_3575357),
    ACCOUNT_PZ_2000000_PZ_2244668(CuArTestConstants.CHART_PZ, CuArTestConstants.ACCOUNT_2000000,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_2244668),
    ACCOUNT_HG_9988777_PZ_3575357(CuArTestConstants.CHART_HG, CuArTestConstants.ACCOUNT_9988777,
            CuArTestConstants.CHART_PZ, CuArTestConstants.CC_ACCOUNT_3575357);

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

    public boolean hasChartAndAccount(String detailChartCode, String detailAccountNumber) {
        return StringUtils.equals(chartOfAccountsCode, detailChartCode) && StringUtils.equals(accountNumber, detailAccountNumber);
    }

    public boolean hasContractControlChartAndAccount(String ccChartCode, String ccAccountNumber) {
        return StringUtils.equals(contractControlChartOfAccountsCode, ccChartCode)
                && StringUtils.equals(contractControlAccountNumber, ccAccountNumber);
    }

}
