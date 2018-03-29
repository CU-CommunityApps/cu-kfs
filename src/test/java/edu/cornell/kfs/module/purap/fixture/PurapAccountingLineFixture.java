package edu.cornell.kfs.module.purap.fixture;

import java.math.BigDecimal;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.businessobject.TestableRequisitionAccount;

public enum PurapAccountingLineFixture {
	
	REQ_ITEM_ACCT_LINE(new BigDecimal(100), "1000710", "IT", "6100", new KualiDecimal(1)),
	REQ_ITEM_ACCT_LINE2(new BigDecimal(100), "U008721", "IT", "6100", new KualiDecimal(1)),
	REQ_ITEM_ACCT_LINE3(new BigDecimal(100), "1000710", "IT", "6100", new KualiDecimal(1)),

    REQ_ITEM_ACCT_WITHOUT_CFDA_NUMBER(100.00, "G123400", "IT", "6100", 1.00, null),
    REQ_ITEM_ACCT_WITH_CFDA_NUMBER(100.00, "R5556666", "IT", "6100", 1.00, "2222");

	public final BigDecimal accountLinePercent;
	public final String accountNumber;
	public final String chartOfAccountsCode;
	public final String financialObjectCode;
	public final KualiDecimal amount;
	public final String accountCfdaNumber;
	public Integer accountIdentifier;

    private PurapAccountingLineFixture(
            double accountLinePercent, String accountNumber, String chartOfAccountsCode, String financialObjectCode,
            double amount, String accountCfdaNumber) {
        this(new BigDecimal(accountLinePercent), accountNumber, chartOfAccountsCode, financialObjectCode, new KualiDecimal(amount), accountCfdaNumber);
    }

    private PurapAccountingLineFixture(
            BigDecimal accountLinePercent, String accountNumber, String chartOfAccountsCode, String financialObjectCode, KualiDecimal amount) {
        this(accountLinePercent, accountNumber, chartOfAccountsCode, financialObjectCode, amount, null);
    }

    private PurapAccountingLineFixture(
            BigDecimal accountLinePercent, String accountNumber, String chartOfAccountsCode, String financialObjectCode,
            KualiDecimal amount, String accountCfdaNumber) {
        this.accountLinePercent = accountLinePercent;
        this.accountNumber = accountNumber;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.financialObjectCode = financialObjectCode;
        this.amount = amount;
        this.accountCfdaNumber = accountCfdaNumber;
    }

	public PurApAccountingLine createRequisitionAccount(Integer itemIdentifier){
		PurApAccountingLine purapAcctLine = new RequisitionAccount();
		
		purapAcctLine.setItemIdentifier(new Integer(SpringContext.getBean(org.kuali.kfs.krad.service.SequenceAccessorService.class).getNextAvailableSequenceNumber("REQS_ACCT_ID").toString()));
		purapAcctLine.setAccountLinePercent(accountLinePercent);
		purapAcctLine.setAccountNumber(accountNumber);
		purapAcctLine.setChartOfAccountsCode(chartOfAccountsCode);
		purapAcctLine.setFinancialObjectCode(financialObjectCode);
		purapAcctLine.setAmount(amount);
		purapAcctLine.setItemIdentifier(itemIdentifier);
	
		
		return purapAcctLine;
	}

    @SuppressWarnings("deprecation")
    public PurApAccountingLine createRequisitionAccountForMicroTest(Integer itemIdentifier) {
        TestableRequisitionAccount accountingLine = new TestableRequisitionAccount();
        
        accountingLine.setChartOfAccountsCode(chartOfAccountsCode);
        TestableRequisitionAccount.setAccountNumberForMicroTest(accountingLine, accountNumber);
        accountingLine.setFinancialObjectCode(financialObjectCode);
        accountingLine.setAmount(amount);
        accountingLine.setAccountLinePercent(accountLinePercent);
        accountingLine.setItemIdentifier(itemIdentifier);
        accountingLine.setAccount(createAccountForMicroTest());
        
        return accountingLine;
    }

    private Account createAccountForMicroTest() {
        Account account = new Account();
        account.setChartOfAccountsCode(chartOfAccountsCode);
        account.setAccountNumber(accountNumber);
        account.setAccountCfdaNumber(accountCfdaNumber);
        return account;
    }

}
