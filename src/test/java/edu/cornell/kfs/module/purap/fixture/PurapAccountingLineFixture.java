package edu.cornell.kfs.module.purap.fixture;

import java.math.BigDecimal;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public enum PurapAccountingLineFixture {
	
	REQ_ITEM_ACCT_LINE(new BigDecimal(100), "1000710", "IT", "6100", new KualiDecimal(1)),
	REQ_ITEM_ACCT_LINE2(new BigDecimal(100), "U008721", "IT", "6100", new KualiDecimal(1)),
	REQ_ITEM_ACCT_LINE3(new BigDecimal(100), "1000710", "IT", "6100", new KualiDecimal(1)),;
	
	public final BigDecimal accountLinePercent;
	public final String accountNumber;
	public final String chartOfAccountsCode;
	public final String financialObjectCode;
	public final KualiDecimal amount;
	public Integer accountIdentifier;
	
	private PurapAccountingLineFixture(BigDecimal accountLinePercent, String accountNumber, String chartOfAccountsCode, String financialObjectCode, KualiDecimal amount) {
		this.accountLinePercent = accountLinePercent;
		this.accountNumber = accountNumber;
		this.chartOfAccountsCode = chartOfAccountsCode;
		this.financialObjectCode = financialObjectCode;
		this.amount = amount;
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

}
