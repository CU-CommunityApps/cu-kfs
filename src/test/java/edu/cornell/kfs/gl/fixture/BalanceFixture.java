package edu.cornell.kfs.gl.fixture;

import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.service.BusinessObjectService;

public enum BalanceFixture {

	BALANCE_CHART_IT_COUNT_BALANCES("IT", "1000710", "sub", "5000", "123",
			"AC", "AS", new KualiDecimal(1), new KualiDecimal(1),
			KualiDecimal.ZERO),

	BALANCE_CHART_CS_COUNT_BALANCES("CS", "J971800", "sub", "8000", "123",
			"AC", "TE", new KualiDecimal(1), new KualiDecimal(1),
			KualiDecimal.ZERO),

	BALANCE_CHART_IT_NOMINAL_ACTIVITY_BALANCES("IT", "1000710", "sub", "5000",
			"123", "AC", "EX", new KualiDecimal(1), new KualiDecimal(1),
			KualiDecimal.ZERO),

	BALANCE_CHART_CS_NOMINAL_ACTIVITY_BALANCES("CS", "J971800", "sub", "8000",
			"123", "AC", "TE", new KualiDecimal(1), new KualiDecimal(1),
			KualiDecimal.ZERO),

	BALANCE_CHART_IT_GENERAL_BALANCES_FORWARD("IT", "1000710", "sub", "5000",
			"123", "AC", "AS", new KualiDecimal(1), new KualiDecimal(2),
			new KualiDecimal(3)),

	BALANCE_CHART_CS_GENERAL_BALANCES_FORWARD("CS", "J971800", "sub", "8000",
			"123", "AC", "AS", new KualiDecimal(1), new KualiDecimal(2),
			new KualiDecimal(3)), 
			
	BALANCE_CHART_IT_CUMULATIVE_BALANCES_FORWARD(
			"IT", "1006094", "-----", "4430", "123", "AC", "IN",
			new KualiDecimal(1), new KualiDecimal(2), new KualiDecimal(3)), 
			
	BALANCE_CHART_CS_CUMULATIVE_BALANCES_FORWARD(
			"CS", "J971800", "-----", "4430", "123", "AC", "IN",
			new KualiDecimal(1), new KualiDecimal(2), new KualiDecimal(3)),
	
	BALANCE_CHART_FS_CUMULATIVE_BALANCES_FORWARD(
			"FS", "G051647", "-----", "4430", "123", "AC", "IN",
			new KualiDecimal(1), new KualiDecimal(2), new KualiDecimal(3));
	;

	private String chartOfAccountsCode;
	private String accountNumber;
	private String subAccountNumber;
	private String objectCode;
	private String subObjectCode;
	private String balanceTypeCode;
	private String objectTypeCode;
	private KualiDecimal accountLineAnnualBalanceAmount;

	private KualiDecimal beginningBalanceLineAmount;
	private KualiDecimal contractsGrantsBeginningBalanceAmount;

	private BalanceFixture(String chartOfAccountsCode, String accountNumber,
			String subAccountNumber, String objectCode, String subObjectCode,
			String balanceTypeCode, String objectTypeCode,
			KualiDecimal accountLineAnnualBalanceAmount,
			KualiDecimal beginningBalanceLineAmount,
			KualiDecimal contractsGrantsBeginningBalanceAmount) {
		this.chartOfAccountsCode = chartOfAccountsCode;
		this.accountNumber = accountNumber;
		this.subAccountNumber = subAccountNumber;
		this.objectCode = objectCode;
		this.subObjectCode = subObjectCode;
		this.balanceTypeCode = balanceTypeCode;
		this.objectTypeCode = objectTypeCode;
		this.accountLineAnnualBalanceAmount = accountLineAnnualBalanceAmount;
		this.beginningBalanceLineAmount = beginningBalanceLineAmount;
		this.contractsGrantsBeginningBalanceAmount = contractsGrantsBeginningBalanceAmount;
	}

	public Balance createBalance(Integer universityFiscalYear) {

		Balance balance = new Balance();

		balance.setUniversityFiscalYear(universityFiscalYear);
		balance.setChartOfAccountsCode(chartOfAccountsCode);
		balance.setAccountNumber(accountNumber);
		balance.setSubAccountNumber(subAccountNumber);
		balance.setObjectCode(objectCode);
		balance.setSubObjectCode(subObjectCode);
		balance.setBalanceTypeCode(balanceTypeCode);
		balance.setObjectTypeCode(objectTypeCode);

		balance.setAccountLineAnnualBalanceAmount(accountLineAnnualBalanceAmount);
		balance.setBeginningBalanceLineAmount(beginningBalanceLineAmount);
		balance.setContractsGrantsBeginningBalanceAmount(contractsGrantsBeginningBalanceAmount);

		saveBalance(balance);

		return balance;
	}

	/**
	 * Saves the created balance.
	 */
	private void saveBalance(Balance balance) {
		BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
		businessObjectService.save(balance);
	}

	/**
	 * Retrieves the given balance.
	 * @param balance
	 * @return
	 */
	public Balance retrieveBalance(Balance balance) {
		BusinessObjectService businessObjectService = SpringContext.getBean(BusinessObjectService.class);
		return (Balance) businessObjectService.retrieve(balance);

	}

}
