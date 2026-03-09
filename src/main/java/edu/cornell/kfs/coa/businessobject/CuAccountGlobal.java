package edu.cornell.kfs.coa.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.AccountType;
import org.kuali.kfs.coa.businessobject.BudgetRecordingLevel;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryType;
import org.kuali.kfs.coa.businessobject.RestrictedStatus;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.coa.service.GlobalObjectWithIndirectCostRecoveryAccountsService;
import edu.cornell.kfs.module.cg.businessobject.InvoiceFrequency;
import edu.cornell.kfs.module.cg.businessobject.InvoiceType;

public class CuAccountGlobal extends AccountGlobal implements GlobalObjectWithIndirectCostRecoveryAccounts{
    private static final long serialVersionUID = 1L;

    protected transient DateTimeService dateTimeService;
    protected transient BusinessObjectService businessObjectService;
    protected transient GlobalObjectWithIndirectCostRecoveryAccountsService globalObjectWithIndirectCostRecoveryAccountsService;

    protected String majorReportingCategoryCode;
    protected String accountPhysicalCampusCode;
    protected Date accountEffectiveDate;
    protected Boolean accountOffCampusIndicator;
    protected Boolean closed;
    protected String accountTypeCode;
    protected String appropriationAccountNumber;
    protected Boolean accountsFringesBnftIndicator;
    protected String reportsToChartOfAccountsCode;
    protected String reportsToAccountNumber;
    protected String accountRestrictedStatusCode;
    protected Date accountRestrictedStatusDate;
    protected String endowmentIncomeAcctFinCoaCd;
    protected String endowmentIncomeAccountNumber;
    protected String programCode;
    protected String budgetRecordingLevelCode;
    protected Boolean financialObjectivePrsctrlIndicator;
    protected String contractControlFinCoaCode;
    protected String contractControlAccountNumber;
    protected String acctIndirectCostRcvyTypeCd;
    protected String invoiceFrequencyCode;
    protected String invoiceTypeCode;
    protected Long costShareForProjectNumber;
    protected boolean removeAccountExpirationDate;
    protected boolean removeContinuationChartAndAccount;
    protected String financialIcrSeriesIdentifier;
    protected Boolean everify;
    protected boolean removeIncomeStreamChartAndAccount;  
    
    protected MajorReportingCategory majorReportingCategory;
    protected Campus accountPhysicalCampus;  
    protected Account contractControlAccount;
    protected Chart contractControlChartOfAccounts;
    protected IndirectCostRecoveryType acctIndirectCostRcvyType;
    protected InvoiceFrequency invoiceFrequency;
    protected InvoiceType invoiceType;
    protected AccountType accountType;
    protected AppropriationAccount appropriationAccount;
    protected Chart fringeBenefitsChartOfAccount;
    protected Account reportsToAccount;
    protected RestrictedStatus accountRestrictedStatus;
    protected Chart endowmentIncomeChartOfAccounts;
    protected Account endowmentIncomeAccount;
    protected BudgetRecordingLevel budgetRecordingLevel;
    protected SubFundProgram subFundProgram;

    protected List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts;

    public CuAccountGlobal() {
    	super();
    	indirectCostRecoveryAccounts = new ArrayList<IndirectCostRecoveryAccountChange>();
	}

    
	@Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        List<PersistableBusinessObject> persistables = new ArrayList<>();

        for (final AccountGlobalDetail accountGlobalDetail : accountGlobalDetails) {
        	updateAccountValuesAndAddToPersistablesList(persistables, accountGlobalDetail);
        }

        return persistables;
    }

	private void updateAccountValuesAndAddToPersistablesList(
			final List<PersistableBusinessObject> persistables,
			final AccountGlobalDetail accountGlobalDetail) {
		final Account account = updateAccountValuesOnAccountGlobalDetailWithNewValuesFromAccountGlobalDoc(accountGlobalDetail);

		if (ObjectUtils.isNotNull(account)) {
			persistables.add(account);
		}
	}
    
	private Account updateAccountValuesOnAccountGlobalDetailWithNewValuesFromAccountGlobalDoc(
			final GlobalBusinessObjectDetailBase globalDetail) {
		final AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail) globalDetail;
		final Account account = getBusinessObjectService().findByPrimaryKey(Account.class,accountGlobalDetail.getPrimaryKeys());

		if (ObjectUtils.isNotNull(account)) {
			updateAccountBasicFields(account);
			updateAccountExtendedAttribute(account);

			if(ObjectUtils.isNotNull(this.getIndirectCostRecoveryAccounts()) && this.getIndirectCostRecoveryAccounts().size() > 0){
				updateIcrAccounts(globalDetail, account.getIndirectCostRecoveryAccounts());
			}
		}

		return account;
	}

	/**
	 *
	 * @param subFundGroup
	 * @return true when a default account restricted status code set on the sub-fund; false otherwise
	 */
	private boolean isDefaultAccountRestrictedStatusCodeSetOnSubFundGroup(final SubFundGroup subFundGroup) {
		return ObjectUtils.isNotNull(subFundGroup) && StringUtils.isNotBlank(subFundGroup.getAccountRestrictedStatusCode());
	}

	private void updateAccountBasicFields(final Account account) {
		if (StringUtils.isNotBlank(accountFiscalOfficerSystemIdentifier)) {
		    account.setAccountFiscalOfficerSystemIdentifier(accountFiscalOfficerSystemIdentifier);
		}

		if (StringUtils.isNotBlank(accountsSupervisorySystemsIdentifier)) {
		    account.setAccountsSupervisorySystemsIdentifier(accountsSupervisorySystemsIdentifier);
		}

		if (StringUtils.isNotBlank(accountManagerSystemIdentifier)) {
		    account.setAccountManagerSystemIdentifier(accountManagerSystemIdentifier);
		}

		if (StringUtils.isNotBlank(organizationCode)) {
		    account.setOrganizationCode(organizationCode);
		}

		if (StringUtils.isNotBlank(subFundGroupCode)) {
		    account.setSubFundGroupCode(subFundGroupCode);
		}

		if (StringUtils.isNotBlank(accountCityName)) {
		    account.setAccountCityName(accountCityName);
		}

		if (StringUtils.isNotBlank(accountStateCode)) {
		    account.setAccountStateCode(accountStateCode);
		}

		if (StringUtils.isNotBlank(accountStreetAddress)) {
		    account.setAccountStreetAddress(accountStreetAddress);
		}

		if (StringUtils.isNotBlank(accountZipCode)) {
		    account.setAccountZipCode(accountZipCode);
		}

		if (accountExpirationDate != null) {
		    account.setAccountExpirationDate(new Date(accountExpirationDate.getTime()));
		}

		if (StringUtils.isNotBlank(continuationFinChrtOfAcctCd)) {
		    account.setContinuationFinChrtOfAcctCd(continuationFinChrtOfAcctCd);
		}

		if (StringUtils.isNotBlank(continuationAccountNumber)) {
		    account.setContinuationAccountNumber(continuationAccountNumber);
		}

		if (StringUtils.isNotBlank(incomeStreamFinancialCoaCode)) {
		    account.setIncomeStreamFinancialCoaCode(incomeStreamFinancialCoaCode);
		}

		if (StringUtils.isNotBlank(incomeStreamAccountNumber)) {
		    account.setIncomeStreamAccountNumber(incomeStreamAccountNumber);
		}

		if (StringUtils.isNotBlank(accountCfdaNumber)) {
		    account.setAccountCfdaNumber(accountCfdaNumber);
		}

		if (StringUtils.isNotBlank(financialHigherEdFunctionCd)) {
		    account.setFinancialHigherEdFunctionCd(financialHigherEdFunctionCd);
		}

		if (StringUtils.isNotBlank(accountSufficientFundsCode)) {
		    account.setAccountSufficientFundsCode(accountSufficientFundsCode);
		}

		if (StringUtils.isNotBlank(getLaborBenefitRateCategoryCode())) {
		    account.setLaborBenefitRateCategoryCode(getLaborBenefitRateCategoryCode());
		}

		if (pendingAcctSufficientFundsIndicator != null) {
		    account.setPendingAcctSufficientFundsIndicator(pendingAcctSufficientFundsIndicator);
		}

		if (StringUtils.isNotBlank(majorReportingCategoryCode)) {
		    ((AccountExtendedAttribute) account.getExtension()).setMajorReportingCategoryCode(majorReportingCategoryCode);
		}

		if (StringUtils.isNotBlank(accountPhysicalCampusCode)) {
		    account.setAccountPhysicalCampusCode(accountPhysicalCampusCode);
		}

		if (ObjectUtils.isNotNull(accountEffectiveDate)) {
		    account.setAccountEffectiveDate(accountEffectiveDate);
		}

		if(accountOffCampusIndicator != null){
			account.setAccountOffCampusIndicator(accountOffCampusIndicator);
		}

		if(closed != null){
			account.setClosed(closed);
		}

		if (StringUtils.isNotBlank(accountTypeCode)) {
		    account.setAccountTypeCode(accountTypeCode);
		}

		if (StringUtils.isNotBlank(appropriationAccountNumber)) {
		    ((AccountExtendedAttribute) account.getExtension()).setAppropriationAccountNumber(appropriationAccountNumber);
		}

		if(accountsFringesBnftIndicator != null){
			account.setAccountsFringesBnftIndicator(accountsFringesBnftIndicator);
		}

		if (StringUtils.isNotBlank(reportsToChartOfAccountsCode)) {
		    account.setReportsToChartOfAccountsCode(reportsToChartOfAccountsCode);
		}

		if (StringUtils.isNotBlank(reportsToAccountNumber)) {
		    account.setReportsToAccountNumber(reportsToAccountNumber);
		}

		if (StringUtils.isNotBlank(accountRestrictedStatusCode)) {
		    account.setAccountRestrictedStatusCode(accountRestrictedStatusCode);
		}

		if (ObjectUtils.isNotNull(accountRestrictedStatusDate)) {
		    account.setAccountRestrictedStatusDate(accountRestrictedStatusDate);
		}

		if (StringUtils.isNotBlank(endowmentIncomeAcctFinCoaCd)) {
		    account.setEndowmentIncomeAcctFinCoaCd(endowmentIncomeAcctFinCoaCd);
		}

		if (StringUtils.isNotBlank(endowmentIncomeAccountNumber)) {
		    account.setEndowmentIncomeAccountNumber(endowmentIncomeAccountNumber);
		}

		if (StringUtils.isNotBlank(programCode)) {
		    ((AccountExtendedAttribute) account.getExtension()).setProgramCode(programCode);
		}

		if (StringUtils.isNotBlank(budgetRecordingLevelCode)) {
		    account.setBudgetRecordingLevelCode(budgetRecordingLevelCode);
		}

		if(financialObjectivePrsctrlIndicator != null){
			account.setFinancialObjectivePrsctrlIndicator(financialObjectivePrsctrlIndicator);
		}

		if (StringUtils.isNotBlank(contractControlFinCoaCode)) {
		    account.setContractControlFinCoaCode(contractControlFinCoaCode);
		}

		if (StringUtils.isNotBlank(contractControlAccountNumber)) {
		    account.setContractControlAccountNumber(contractControlAccountNumber);
		}

		if (StringUtils.isNotBlank(acctIndirectCostRcvyTypeCd)) {
		    account.setAcctIndirectCostRcvyTypeCd(acctIndirectCostRcvyTypeCd);
		}

		if (StringUtils.isNotBlank(financialIcrSeriesIdentifier)) {
		    account.setFinancialIcrSeriesIdentifier(financialIcrSeriesIdentifier);
		}

		if (StringUtils.isNotBlank(invoiceFrequencyCode)) {
		    ((AccountExtendedAttribute) account.getExtension()).setInvoiceFrequencyCode(invoiceFrequencyCode);
		}

		if (StringUtils.isNotBlank(invoiceTypeCode)) {
		    ((AccountExtendedAttribute) account.getExtension()).setInvoiceTypeCode(invoiceTypeCode);
		}

		if (everify != null) {
		    ((AccountExtendedAttribute) account.getExtension()).setEverify(everify);
		}

		if (ObjectUtils.isNotNull(costShareForProjectNumber)) {
		    ((AccountExtendedAttribute) account.getExtension()).setCostShareForProjectNumber(costShareForProjectNumber);
		}

		if(removeAccountExpirationDate){
			account.setAccountExpirationDate(null);
		}

		if(removeContinuationChartAndAccount){
			account.setContinuationFinChrtOfAcctCd(null);
			account.setContinuationAccountNumber(null);
		}

		if(removeIncomeStreamChartAndAccount){
			account.setIncomeStreamFinancialCoaCode(null);
			account.setIncomeStreamAccountNumber(null);
		}
	}

	private void updateAccountExtendedAttribute(final Account account) {
	    final AccountExtendedAttribute aea = (AccountExtendedAttribute) (account.getExtension());
	    if (this.getClosed() != null && this.getClosed() && aea.getAccountClosedDate() == null) {
	        aea.setAccountClosedDate(this.getDateTimeService().getCurrentSqlDate());
	    } else if (this.getClosed() != null && !this.getClosed() && aea.getAccountClosedDate() != null) {
	        aea.setAccountClosedDate(null);
	    }
	    aea.setSubFundGroupCode(account.getSubFundGroupCode());
	}

	public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts() {
	    return getGlobalObjectWithIndirectCostRecoveryAccountsService().getActiveIndirectCostRecoveryAccounts(this);
	}

	public boolean hasIcrAccounts(){
		return ObjectUtils.isNotNull(indirectCostRecoveryAccounts) && indirectCostRecoveryAccounts.size() > 0;
	}

	@Override
	public List<? extends GlobalBusinessObjectDetailBase> getGlobalObjectDetails() {
		return getAccountGlobalDetails();
	}

	@Override
	public Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> getGlobalObjectDetailsAndIcrAccountsMap() {
		final Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> globalObjectDetailsAndIcrAccountsMap = new HashMap<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>>();
		final List<AccountGlobalDetail> accountGlobalDetails = getAccountGlobalDetails();

		if (ObjectUtils.isNotNull(accountGlobalDetails)&& !accountGlobalDetails.isEmpty()) {
			for (final GlobalBusinessObjectDetailBase globalDetail : accountGlobalDetails) {

				final AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail) globalDetail;
				accountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
				final List<IndirectCostRecoveryAccount> icrAccounts = accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts();
				globalObjectDetailsAndIcrAccountsMap.put(globalDetail,icrAccounts);
			}

		}
		return globalObjectDetailsAndIcrAccountsMap;
	}

	@Override
	public IndirectCostRecoveryAccount createIndirectCostRecoveryAccountFromChange(final GlobalBusinessObjectDetailBase globalDetail, final IndirectCostRecoveryAccountChange newICR) {
		final AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail) globalDetail;
		final String chart = accountGlobalDetail.getChartOfAccountsCode();
		final String account = accountGlobalDetail.getAccountNumber();

		final IndirectCostRecoveryAccount icrAccount = new IndirectCostRecoveryAccount();
		icrAccount.setAccountNumber(account);
		icrAccount.setChartOfAccountsCode(chart);
		icrAccount.setIndirectCostRecoveryAccountNumber(newICR.getIndirectCostRecoveryAccountNumber());
		icrAccount.setIndirectCostRecoveryFinCoaCode(newICR.getIndirectCostRecoveryFinCoaCode());
		icrAccount.setActive(newICR.isActive());
		icrAccount.setAccountLinePercent(newICR.getAccountLinePercent());

		return icrAccount;
	}


	@Override
	public void updateGlobalDetailICRAccountCollection(
			final GlobalBusinessObjectDetailBase globalDetail, final List<IndirectCostRecoveryAccount> updatedIcrAccounts) {
		final AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail) globalDetail;
		accountGlobalDetail.getAccount().setIndirectCostRecoveryAccounts(updatedIcrAccounts);

	}

	public void updateIcrAccounts(final GlobalBusinessObjectDetailBase globalDetail, final List<IndirectCostRecoveryAccount> icrAccounts){
		getGlobalObjectWithIndirectCostRecoveryAccountsService().updateIcrAccounts(this, globalDetail, icrAccounts);
	}

	@Override
	public String getGlobalDetailsPropertyName() {
		return KFSPropertyConstants.ACCOUNT_CHANGE_DETAILS;
	}

	public String getMajorReportingCategoryCode() {
        return majorReportingCategoryCode;
    }

    public void setMajorReportingCategoryCode(final String majorReportingCategoryCode) {
        this.majorReportingCategoryCode = majorReportingCategoryCode;
    }
    public MajorReportingCategory getMajorReportingCategory() {
        return majorReportingCategory;
    }

    public void setMajorReportingCategory(
            final MajorReportingCategory majorReportingCategory) {
        this.majorReportingCategory = majorReportingCategory;
    }

    public String getAccountPhysicalCampusCode() {
        return accountPhysicalCampusCode;
    }

    public void setAccountPhysicalCampusCode(final String accountPhysicalCampusCode) {
        this.accountPhysicalCampusCode = accountPhysicalCampusCode;
    }

    public Campus getAccountPhysicalCampus() {
        return accountPhysicalCampus;
    }

    public void setAccountPhysicalCampus(final Campus accountPhysicalCampus) {
        this.accountPhysicalCampus = accountPhysicalCampus;
    }

    public Date getAccountEffectiveDate() {
        return accountEffectiveDate;
    }

    public void setAccountEffectiveDate(final Date accountEffectiveDate) {
        this.accountEffectiveDate = accountEffectiveDate;
    }

    public Boolean getAccountOffCampusIndicator() {
        return accountOffCampusIndicator;
    }

    public void setAccountOffCampusIndicator(final Boolean accountOffCampusIndicator) {
        this.accountOffCampusIndicator = accountOffCampusIndicator;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(final String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getAppropriationAccountNumber() {
        return appropriationAccountNumber;
    }

    public void setAppropriationAccountNumber(final String appropriationAccountNumber) {
        this.appropriationAccountNumber = appropriationAccountNumber;
    }

    public Boolean getAccountsFringesBnftIndicator() {
        return accountsFringesBnftIndicator;
    }

    public void setAccountsFringesBnftIndicator(final Boolean accountsFringesBnftIndicator) {
        this.accountsFringesBnftIndicator = accountsFringesBnftIndicator;
    }

    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    public void setReportsToChartOfAccountsCode(final String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    public String getReportsToAccountNumber() {
        return reportsToAccountNumber;
    }

    public void setReportsToAccountNumber(final String reportsToAccountNumber) {
        this.reportsToAccountNumber = reportsToAccountNumber;
    }

    public String getAccountRestrictedStatusCode() {
        return accountRestrictedStatusCode;
    }

    public void setAccountRestrictedStatusCode(final String accountRestrictedStatusCode) {
        this.accountRestrictedStatusCode = accountRestrictedStatusCode;
    }

    public Date getAccountRestrictedStatusDate() {
        return accountRestrictedStatusDate;
    }

    public void setAccountRestrictedStatusDate(final Date accountRestrictedStatusDate) {
        this.accountRestrictedStatusDate = accountRestrictedStatusDate;
    }

    public String getEndowmentIncomeAcctFinCoaCd() {
        return endowmentIncomeAcctFinCoaCd;
    }

    public void setEndowmentIncomeAcctFinCoaCd(final String endowmentIncomeAcctFinCoaCd) {
        this.endowmentIncomeAcctFinCoaCd = endowmentIncomeAcctFinCoaCd;
    }

    public String getEndowmentIncomeAccountNumber() {
        return endowmentIncomeAccountNumber;
    }

    public void setEndowmentIncomeAccountNumber(final String endowmentIncomeAccountNumber) {
        this.endowmentIncomeAccountNumber = endowmentIncomeAccountNumber;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(final String programCode) {
        this.programCode = programCode;
    }

    public String getBudgetRecordingLevelCode() {
        return budgetRecordingLevelCode;
    }

    public void setBudgetRecordingLevelCode(final String budgetRecordingLevelCode) {
        this.budgetRecordingLevelCode = budgetRecordingLevelCode;
    }

    public Boolean getFinancialObjectivePrsctrlIndicator() {
        return financialObjectivePrsctrlIndicator;
    }

    public void setFinancialObjectivePrsctrlIndicator(final Boolean financialObjectivePrsctrlIndicator) {
        this.financialObjectivePrsctrlIndicator = financialObjectivePrsctrlIndicator;
    }

    public String getContractControlFinCoaCode() {
        return contractControlFinCoaCode;
    }

    public void setContractControlFinCoaCode(final String contractControlFinCoaCode) {
        this.contractControlFinCoaCode = contractControlFinCoaCode;
    }

    public String getContractControlAccountNumber() {
        return contractControlAccountNumber;
    }

    public void setContractControlAccountNumber(final String contractControlAccountNumber) {
        this.contractControlAccountNumber = contractControlAccountNumber;
    }

    public String getAcctIndirectCostRcvyTypeCd() {
        return acctIndirectCostRcvyTypeCd;
    }

    public void setAcctIndirectCostRcvyTypeCd(final String acctIndirectCostRcvyTypeCd) {
        this.acctIndirectCostRcvyTypeCd = acctIndirectCostRcvyTypeCd;
    }

    public String getInvoiceFrequencyCode() {
        return invoiceFrequencyCode;
    }

    public void setInvoiceFrequencyCode(final String invoiceFrequencyCode) {
        this.invoiceFrequencyCode = invoiceFrequencyCode;
    }

    public String getInvoiceTypeCode() {
        return invoiceTypeCode;
    }

    public void setInvoiceTypeCode(final String invoiceTypeCode) {
        this.invoiceTypeCode = invoiceTypeCode;
    }

    public Long getCostShareForProjectNumber() {
        return costShareForProjectNumber;
    }

    public void setCostShareForProjectNumber(final Long costShareForProjectNumber) {
        this.costShareForProjectNumber = costShareForProjectNumber;
    }

    public Account getContractControlAccount() {
		return contractControlAccount;
	}

	public void setContractControlAccount(final Account contractControlAccount) {
		this.contractControlAccount = contractControlAccount;
	}

    public Chart getContractControlChartOfAccounts() {
		return contractControlChartOfAccounts;
	}

	public void setContractControlChartOfAccounts(
			final Chart contractControlChartOfAccounts) {
		this.contractControlChartOfAccounts = contractControlChartOfAccounts;
	}

	public IndirectCostRecoveryType getAcctIndirectCostRcvyType() {
		return acctIndirectCostRcvyType;
	}

	public void setAcctIndirectCostRcvyType(
			final IndirectCostRecoveryType acctIndirectCostRcvyType) {
		this.acctIndirectCostRcvyType = acctIndirectCostRcvyType;
	}

	public InvoiceFrequency getInvoiceFrequency() {
		return invoiceFrequency;
	}

	public void setInvoiceFrequency(final InvoiceFrequency invoiceFrequency) {
		this.invoiceFrequency = invoiceFrequency;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(final AccountType accountType) {
		this.accountType = accountType;
	}

	public AppropriationAccount getAppropriationAccount() {
		return appropriationAccount;
	}

	public void setAppropriationAccount(final AppropriationAccount appropriationAccount) {
		this.appropriationAccount = appropriationAccount;
	}

	public Chart getFringeBenefitsChartOfAccount() {
		return fringeBenefitsChartOfAccount;
	}

	public void setFringeBenefitsChartOfAccount(final Chart fringeBenefitsChartOfAccount) {
		this.fringeBenefitsChartOfAccount = fringeBenefitsChartOfAccount;
	}

	public Account getReportsToAccount() {
		return reportsToAccount;
	}

	public void setReportsToAccount(final Account reportsToAccount) {
		this.reportsToAccount = reportsToAccount;
	}

	public RestrictedStatus getAccountRestrictedStatus() {
		return accountRestrictedStatus;
	}

	public void setAccountRestrictedStatus(final RestrictedStatus accountRestrictedStatus) {
		this.accountRestrictedStatus = accountRestrictedStatus;
	}

	public Chart getEndowmentIncomeChartOfAccounts() {
		return endowmentIncomeChartOfAccounts;
	}

	public void setEndowmentIncomeChartOfAccounts(
			final Chart endowmentIncomeChartOfAccounts) {
		this.endowmentIncomeChartOfAccounts = endowmentIncomeChartOfAccounts;
	}

	public Account getEndowmentIncomeAccount() {
		return endowmentIncomeAccount;
	}

	public void setEndowmentIncomeAccount(final Account endowmentIncomeAccount) {
		this.endowmentIncomeAccount = endowmentIncomeAccount;
	}

	public BudgetRecordingLevel getBudgetRecordingLevel() {
		return budgetRecordingLevel;
	}

	public void setBudgetRecordingLevel(final BudgetRecordingLevel budgetRecordingLevel) {
		this.budgetRecordingLevel = budgetRecordingLevel;
	}

	public InvoiceType getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(final InvoiceType invoiceType) {
		this.invoiceType = invoiceType;
	}

	public SubFundProgram getSubFundProgram() {
		return subFundProgram;
	}

	public void setSubFundProgram(final SubFundProgram subFundProgram) {
		this.subFundProgram = subFundProgram;
	}


	public boolean isRemoveAccountExpirationDate() {
		return removeAccountExpirationDate;
	}


	public void setRemoveAccountExpirationDate(final boolean removeAccountExpirationDate) {
		this.removeAccountExpirationDate = removeAccountExpirationDate;
	}


	public boolean isRemoveContinuationChartAndAccount() {
		return removeContinuationChartAndAccount;
	}


	public void setRemoveContinuationChartAndAccount(final boolean removeContinuationChartAndAccount) {
		this.removeContinuationChartAndAccount = removeContinuationChartAndAccount;
	}


	public String getFinancialIcrSeriesIdentifier() {
		return financialIcrSeriesIdentifier;
	}


	public void setFinancialIcrSeriesIdentifier(final String financialIcrSeriesIdentifier) {
		this.financialIcrSeriesIdentifier = financialIcrSeriesIdentifier;
	}


	public Boolean getEverify() {
		return everify;
	}


	public void setEverify(final Boolean everify) {
		this.everify = everify;
	}


	public boolean isRemoveIncomeStreamChartAndAccount() {
		return removeIncomeStreamChartAndAccount;
	}


	public void setRemoveIncomeStreamChartAndAccount(
			final boolean removeIncomeStreamChartAndAccount) {
		this.removeIncomeStreamChartAndAccount = removeIncomeStreamChartAndAccount;
	}


	public List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts() {
		return indirectCostRecoveryAccounts;
	}


	public void setIndirectCostRecoveryAccounts(
			final List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts) {
		this.indirectCostRecoveryAccounts = indirectCostRecoveryAccounts;
	}

	public DateTimeService getDateTimeService() {
        if (this.dateTimeService == null) {
            this.setDateTimeService(SpringContext.getBean(DateTimeService.class));
        }
        return this.dateTimeService;
    }

	public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

	public BusinessObjectService getBusinessObjectService() {
        if (this.businessObjectService == null) {
            this.setBusinessObjectService(SpringContext.getBean(BusinessObjectService.class));
        }
        return this.businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public GlobalObjectWithIndirectCostRecoveryAccountsService getGlobalObjectWithIndirectCostRecoveryAccountsService() {
        if (this.globalObjectWithIndirectCostRecoveryAccountsService == null) {
            this.setGlobalObjectWithIndirectCostRecoveryAccountsService(SpringContext.getBean(GlobalObjectWithIndirectCostRecoveryAccountsService.class));
        }
        return this.globalObjectWithIndirectCostRecoveryAccountsService;
    }

    public void setGlobalObjectWithIndirectCostRecoveryAccountsService(final GlobalObjectWithIndirectCostRecoveryAccountsService globalObjectWithIndirectCostRecoveryAccountsService) {
        this.globalObjectWithIndirectCostRecoveryAccountsService = globalObjectWithIndirectCostRecoveryAccountsService;
    }
}
