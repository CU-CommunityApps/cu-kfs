package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;

public enum IWantAccountFixture {
	I_WANT_ACCOUNT(1, "IT", "1000710", "6000", null, null, null, null, 1,
			2014, new KualiDecimal(100), "PERCENT");

	private Integer lineNumber;
	private String chartOfAccountsCode;
	private String accountNumber;
	private String financialObjectCode;
	private String subAccountNumber;
	private String financialSubObjectCode;
	private String projectCode;
	private String organizationReferenceId;
	private Integer accountLineIdentifier;
	private Integer postingYear;
	private KualiDecimal amountOrPercent;
	private String useAmountOrPercent;

	private IWantAccountFixture(Integer lineNumber, String chartOfAccountsCode,
			String accountNumber, String financialObjectCode,
			String subAccountNumber, String financialSubObjectCode,
			String projectCode, String organizationReferenceId,
			Integer accountLineIdentifier, Integer postingYear,
			KualiDecimal amountOrPercent, String useAmountOrPercent) {
		this.lineNumber = lineNumber;
		this.chartOfAccountsCode = chartOfAccountsCode;
		this.accountNumber = accountNumber;
		this.financialObjectCode = financialObjectCode;
		this.subAccountNumber = subAccountNumber;
		this.financialSubObjectCode = financialSubObjectCode;
		this.projectCode = projectCode;
		this.organizationReferenceId = organizationReferenceId;
		this.accountLineIdentifier = accountLineIdentifier;
		this.postingYear = postingYear;
		this.amountOrPercent = amountOrPercent;
		this.useAmountOrPercent = useAmountOrPercent;
	}

	public IWantAccount createIWantAccount(String documentNumber) {
		IWantAccount account = new IWantAccount();

		account.setDocumentNumber(documentNumber);
		account.setLineNumber(lineNumber);
		account.setChartOfAccountsCode(chartOfAccountsCode);
		account.setAccountNumber(accountNumber);
		account.setFinancialObjectCode(financialObjectCode);
		account.setSubAccountNumber(subAccountNumber);
		account.setFinancialSubObjectCode(financialSubObjectCode);
		account.setProjectCode(projectCode);
		account.setOrganizationReferenceId(organizationReferenceId);
		account.setAccountLineIdentifier(accountLineIdentifier);
		account.setPostingYear(postingYear);
		account.setAmountOrPercent(amountOrPercent);
		account.setUseAmountOrPercent(useAmountOrPercent);

		return account;

	}

}
