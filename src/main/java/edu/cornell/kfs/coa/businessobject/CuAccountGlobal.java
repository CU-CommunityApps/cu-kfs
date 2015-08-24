package edu.cornell.kfs.coa.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.AccountType;
import org.kuali.kfs.coa.businessobject.BudgetRecordingLevel;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryType;
import org.kuali.kfs.coa.businessobject.RestrictedStatus;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.ObjectUtils;
import org.kuali.rice.location.framework.campus.CampusEbo;

import edu.cornell.kfs.module.cg.businessobject.InvoiceFrequency;
import edu.cornell.kfs.module.cg.businessobject.InvoiceType;

public class CuAccountGlobal extends AccountGlobal implements GlobalBusinessObject{
    
    private static final long serialVersionUID = 1L;

    protected String majorReportingCategoryCode;
    protected String accountPhysicalCampusCode;
    protected Date accountEffectiveDate;
    protected boolean accountOffCampusIndicator;
    protected boolean closed;
    protected String accountTypeCode;
    protected String appropriationAccountNumber;
    protected boolean accountsFringesBnftIndicator;
    protected String reportsToChartOfAccountsCode;
    protected String reportsToAccountNumber;
    protected String accountRestrictedStatusCode;
    protected Date accountRestrictedStatusDate;
    protected String endowmentIncomeAcctFinCoaCd;
    protected String endowmentIncomeAccountNumber;
    protected String programCode;
    protected String budgetRecordingLevelCode;
    protected boolean extrnlFinEncumSufficntFndIndicator;
    protected boolean intrnlFinEncumSufficntFndIndicator;
    protected boolean finPreencumSufficientFundIndicator;
    protected boolean financialObjectivePrsctrlIndicator;
    protected String contractControlFinCoaCode;
    protected String contractControlAccountNumber;
    protected String acctIndirectCostRcvyTypeCd;
    protected Integer contractsAndGrantsAccountResponsibilityId;
    protected String invoiceFrequencyCode;
    protected String invoiceTypeCode;
    protected Long costShareForProjectNumber;
    protected boolean removeAccountExpirationDate;
    protected boolean removeContinuationChartAndAccount;
    
	protected MajorReportingCategory majorReportingCategory;
    protected CampusEbo accountPhysicalCampus;
    
    protected Account contractControlAccount;
    protected Chart contractControlChartOfAccounts;
    protected IndirectCostRecoveryType acctIndirectCostRcvyType;
    private InvoiceFrequency invoiceFrequency;
    private InvoiceType invoiceType;
    protected AccountType accountType;
    private AppropriationAccount appropriationAccount;
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

        // the list of persist-ready BOs
        List<PersistableBusinessObject> persistables = new ArrayList<PersistableBusinessObject>();
    
        // walk over each change detail record
        for (AccountGlobalDetail detail : accountGlobalDetails) {
    
            // load the object by keys
            Account account = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Account.class, detail.getPrimaryKeys());
    
            // if we got a valid account, do the processing
            if (account != null) {
    
                // NOTE that the list of fields that are updated may be a subset of the total
                // number of fields in this class. This is because the class may contain a superset
                // of the fields actually used in the Global Maintenance Document.
    
                // FISCAL OFFICER
                if (StringUtils.isNotBlank(accountFiscalOfficerSystemIdentifier)) {
                    account.setAccountFiscalOfficerSystemIdentifier(accountFiscalOfficerSystemIdentifier);
                }
    
                // ACCOUNT SUPERVISOR
                if (StringUtils.isNotBlank(accountsSupervisorySystemsIdentifier)) {
                    account.setAccountsSupervisorySystemsIdentifier(accountsSupervisorySystemsIdentifier);
                }
    
                // ACCOUNT MANAGER
                if (StringUtils.isNotBlank(accountManagerSystemIdentifier)) {
                    account.setAccountManagerSystemIdentifier(accountManagerSystemIdentifier);
                }
    
                // ORGANIZATION CODE
                if (StringUtils.isNotBlank(organizationCode)) {
                    account.setOrganizationCode(organizationCode);
                }
    
                // SUB FUND GROUP CODE
                if (StringUtils.isNotBlank(subFundGroupCode)) {
                    account.setSubFundGroupCode(subFundGroupCode);
                }
    
                // CITY NAME
                if (StringUtils.isNotBlank(accountCityName)) {
                    account.setAccountCityName(accountCityName);
                }
    
                // STATE CODE
                if (StringUtils.isNotBlank(accountStateCode)) {
                    account.setAccountStateCode(accountStateCode);
                }
    
                // STREET ADDRESS
                if (StringUtils.isNotBlank(accountStreetAddress)) {
                    account.setAccountStreetAddress(accountStreetAddress);
                }
    
                // ZIP CODE
                if (StringUtils.isNotBlank(accountZipCode)) {
                    account.setAccountZipCode(accountZipCode);
                }
    
                // EXPIRATION DATE
                if (accountExpirationDate != null) {
                    account.setAccountExpirationDate(new Date(accountExpirationDate.getTime()));
                }
    
                // CONTINUATION CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(continuationFinChrtOfAcctCd)) {
                    account.setContinuationFinChrtOfAcctCd(continuationFinChrtOfAcctCd);
                }
    
                // CONTINUATION ACCOUNT NUMBER
                if (StringUtils.isNotBlank(continuationAccountNumber)) {
                    account.setContinuationAccountNumber(continuationAccountNumber);
                }
    
                // INCOME STREAM CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(incomeStreamFinancialCoaCode)) {
                    account.setIncomeStreamFinancialCoaCode(incomeStreamFinancialCoaCode);
                }
    
                // INCOME STREAM ACCOUNT NUMBER
                if (StringUtils.isNotBlank(incomeStreamAccountNumber)) {
                    account.setIncomeStreamAccountNumber(incomeStreamAccountNumber);
                }
    
                // CG CATL FED DOMESTIC ASSIST NBR
                if (StringUtils.isNotBlank(accountCfdaNumber)) {
                    account.setAccountCfdaNumber(accountCfdaNumber);
                }
    
                // FINANCIAL HIGHER ED FUNCTION CODE
                if (StringUtils.isNotBlank(financialHigherEdFunctionCd)) {
                    account.setFinancialHigherEdFunctionCd(financialHigherEdFunctionCd);
                }
    
                // SUFFICIENT FUNDS CODE
                if (StringUtils.isNotBlank(accountSufficientFundsCode)) {
                    account.setAccountSufficientFundsCode(accountSufficientFundsCode);
                }
    
                // LABOR BENEFIT RATE CATEGORY CODE
                if (StringUtils.isNotBlank(getLaborBenefitRateCategoryCode())) {
                    account.setLaborBenefitRateCategoryCode(getLaborBenefitRateCategoryCode());
                }
    
                // PENDING ACCOUNT SUFFICIENT FUNDS CODE INDICATOR
                if (pendingAcctSufficientFundsIndicator != null) {
                    account.setPendingAcctSufficientFundsIndicator(pendingAcctSufficientFundsIndicator);
                }
    
                // MAJOR REPORTING CATEGORY CODE 
                if (StringUtils.isNotBlank(majorReportingCategoryCode)) {
                    ((AccountExtendedAttribute) account.getExtension()).setMajorReportingCategoryCode(majorReportingCategoryCode);
                }
                
                // CAMPUS CODE 
                if (StringUtils.isNotBlank(accountPhysicalCampusCode)) {
                    account.setAccountPhysicalCampusCode(accountPhysicalCampusCode);
                }
                
                // EFFECTIVE DATE
                if (ObjectUtils.isNotNull(accountEffectiveDate)) {
                    account.setAccountEffectiveDate(accountEffectiveDate);
                }
                
                // ACCOUNT OFF CAMPUS INDICATOR
                if(accountOffCampusIndicator){
                	account.setAccountOffCampusIndicator(accountOffCampusIndicator);
                }
                
                // CLOSED
                if(closed){
                	account.setClosed(closed);
                }
                
                // ACCOUNT TYPE CODE 
                if (StringUtils.isNotBlank(accountTypeCode)) {
                    account.setAccountTypeCode(accountTypeCode);
                }
                
                // APPROPRIATION ACCOUNT NUMBER
                if (StringUtils.isNotBlank(appropriationAccountNumber)) {
                    ((AccountExtendedAttribute) account.getExtension()).setAppropriationAccountNumber(appropriationAccountNumber);
                }
                
                // ACCOUNT FRINGE BENEFIT INDICATOR
                //if(accountsFringesBnftIndicator){
                	account.setAccountsFringesBnftIndicator(accountsFringesBnftIndicator);
               // }
                
                // FRINGE BENEFITS CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(reportsToChartOfAccountsCode)) {
                    account.setReportsToChartOfAccountsCode(reportsToChartOfAccountsCode);
                }
                
                // FRINGE BENEFITS ACCOUNTS NUMBER
                if (StringUtils.isNotBlank(reportsToAccountNumber)) {
                    account.setReportsToAccountNumber(reportsToAccountNumber);
                }
                
                // ACCOUNT RESTRICTED STATUS CODE
                if (StringUtils.isNotBlank(accountRestrictedStatusCode)) {
                    account.setAccountRestrictedStatusCode(accountRestrictedStatusCode);
                }

                // ACCOUNT RESTRICTED STATUS DATE
                if (ObjectUtils.isNotNull(accountRestrictedStatusDate)) {
                    account.setAccountRestrictedStatusDate(accountRestrictedStatusDate);
                }

                // ENDOWMENT CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(endowmentIncomeAcctFinCoaCd)) {
                    account.setEndowmentIncomeAcctFinCoaCd(endowmentIncomeAcctFinCoaCd);
                }
                
                // ENDOWMENT ACCOUNTS NUMBER
                if (StringUtils.isNotBlank(endowmentIncomeAccountNumber)) {
                    account.setEndowmentIncomeAccountNumber(endowmentIncomeAccountNumber);
                }    
                
                // SUB FUND PROGRAM CODE
                if (StringUtils.isNotBlank(programCode)) {
                    ((AccountExtendedAttribute) account.getExtension()).setProgramCode(programCode);
                }
                
                // BUDGET RECORD LEVEL CODE
                if (StringUtils.isNotBlank(budgetRecordingLevelCode)) {
                    account.setBudgetRecordingLevelCode(budgetRecordingLevelCode);
                }
                
                // EXTERNAL ENCUMBRANCE SUFFICIENT FUNDS INDICATOR
                if(extrnlFinEncumSufficntFndIndicator){
                	account.setExtrnlFinEncumSufficntFndIndicator(extrnlFinEncumSufficntFndIndicator);
                }
                
                // INTERNAL ENCUMBRANCE SUFFICIENT FUNDS INDICATOR
                if(intrnlFinEncumSufficntFndIndicator){
                	account.setIntrnlFinEncumSufficntFndIndicator(intrnlFinEncumSufficntFndIndicator);
                }
                
                // PRE-ENCUMBRANCE SUFFICIENT FUNDS INDICATOR
                if(finPreencumSufficientFundIndicator){
                	account.setFinPreencumSufficientFundIndicator(finPreencumSufficientFundIndicator); 
                }
                
                // OBJECT PRESENCE CONTROL INDICATOR
                if(financialObjectivePrsctrlIndicator){
                	account.setFinancialObjectivePrsctrlIndicator(financialObjectivePrsctrlIndicator);
                }
                
                // CONTRACT CONTROL CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(contractControlFinCoaCode)) {
                    account.setContractControlFinCoaCode(contractControlFinCoaCode);
                }
                
                // CONTRACT CONTROL CHART OF ACCOUNTS NUMBER
                if (StringUtils.isNotBlank(contractControlAccountNumber)) {
                    account.setContractControlAccountNumber(contractControlAccountNumber);
                }
                
                // INDIRECT COST RECOVERY TYPE
                if (StringUtils.isNotBlank(acctIndirectCostRcvyTypeCd)) {
                    account.setAcctIndirectCostRcvyTypeCd(acctIndirectCostRcvyTypeCd);
                }
                
                // CG ACCOUNT RESPONSIBILITY ID
                if (ObjectUtils.isNotNull(contractsAndGrantsAccountResponsibilityId)) {
                    account.setContractsAndGrantsAccountResponsibilityId(contractsAndGrantsAccountResponsibilityId);
                }
                
                // INVOICE FREQUENCY CODE
                if (StringUtils.isNotBlank(invoiceFrequencyCode)) {
                    ((AccountExtendedAttribute) account.getExtension()).setInvoiceFrequencyCode(invoiceFrequencyCode);
                }
                
                // INVOICE TYPE CODE
                if (StringUtils.isNotBlank(invoiceTypeCode)) {
                    ((AccountExtendedAttribute) account.getExtension()).setInvoiceTypeCode(invoiceTypeCode);
                }
                
                // COST SHARE FOR PROJECT NUMBER
                if (ObjectUtils.isNotNull(costShareForProjectNumber)) {
                    ((AccountExtendedAttribute) account.getExtension()).setCostShareForProjectNumber(costShareForProjectNumber);
                }
                
                // blank out account expiration date
                if(removeAccountExpirationDate){
                	account.setAccountExpirationDate(null);
                }
                
                // blank out continuation chart and account
                if(removeContinuationChartAndAccount){
                	account.setContinuationFinChrtOfAcctCd(null);
                	account.setContinuationAccountNumber(null);
                }

                // ACCOUNT CLOSE DATE
                // TODO : This is part of KFSPTS-3613, but 3613 will go to prod first.
                // 'accountClosedDate' is new attributes and it is not in KFSPTS-3599 branch yet.  So, comment out following update
                // After merge to 'develop, then we need to uncomment following code to update accountClosedDate
                AccountExtendedAttribute aea = (AccountExtendedAttribute) (account.getExtension());
                if (this.isClosed() && aea.getAccountClosedDate() == null) {
                    aea.setAccountClosedDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
                } else if (!this.isClosed() && aea.getAccountClosedDate() != null) {
                    aea.setAccountClosedDate(null);           
                }
                
                if(ObjectUtils.isNotNull(indirectCostRecoveryAccounts) && indirectCostRecoveryAccounts.size()>0){
                	for(IndirectCostRecoveryAccountChange icrAcctChange : indirectCostRecoveryAccounts){
                		IndirectCostRecoveryAccount icrAccount = new IndirectCostRecoveryAccount();
                		icrAccount.setAccountNumber(detail.getAccountNumber());
                		icrAccount.setChartOfAccountsCode(detail.getChartOfAccountsCode());
                		icrAccount.setIndirectCostRecoveryAccountNumber(icrAcctChange.getIndirectCostRecoveryAccountNumber());
                		icrAccount.setIndirectCostRecoveryFinCoaCode(icrAcctChange.getIndirectCostRecoveryFinCoaCode());
                		icrAccount.setAccountLinePercent(icrAcctChange.getAccountLinePercent());
                		account.getIndirectCostRecoveryAccounts().add(icrAccount);
                	}
                }

                persistables.add(account);
    
            }
        }

        return persistables;
    }
    
    public String getMajorReportingCategoryCode() {
        return majorReportingCategoryCode;
    }
    public void setMajorReportingCategoryCode(String majorReportingCategoryCode) {
        this.majorReportingCategoryCode = majorReportingCategoryCode;
    }
    public MajorReportingCategory getMajorReportingCategory() {
        return majorReportingCategory;
    }
    public void setMajorReportingCategory(
            MajorReportingCategory majorReportingCategory) {
        this.majorReportingCategory = majorReportingCategory;
    }

    public String getAccountPhysicalCampusCode() {
        return accountPhysicalCampusCode;
    }

    public void setAccountPhysicalCampusCode(String accountPhysicalCampusCode) {
        this.accountPhysicalCampusCode = accountPhysicalCampusCode;
    }

    public CampusEbo getAccountPhysicalCampus() {
        return accountPhysicalCampus;
    }

    public void setAccountPhysicalCampus(CampusEbo accountPhysicalCampus) {
        this.accountPhysicalCampus = accountPhysicalCampus;
    }
    
    public Date getAccountEffectiveDate() {
        return accountEffectiveDate;
    }

    public void setAccountEffectiveDate(Date accountEffectiveDate) {
        this.accountEffectiveDate = accountEffectiveDate;
    }

    public boolean isAccountOffCampusIndicator() {
        return accountOffCampusIndicator;
    }

    public void setAccountOffCampusIndicator(boolean accountOffCampusIndicator) {
        this.accountOffCampusIndicator = accountOffCampusIndicator;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getAccountTypeCode() {
        return accountTypeCode;
    }

    public void setAccountTypeCode(String accountTypeCode) {
        this.accountTypeCode = accountTypeCode;
    }

    public String getAppropriationAccountNumber() {
        return appropriationAccountNumber;
    }

    public void setAppropriationAccountNumber(String appropriationAccountNumber) {
        this.appropriationAccountNumber = appropriationAccountNumber;
    }

    public boolean isAccountsFringesBnftIndicator() {
        return accountsFringesBnftIndicator;
    }

    public void setAccountsFringesBnftIndicator(boolean accountsFringesBnftIndicator) {
        this.accountsFringesBnftIndicator = accountsFringesBnftIndicator;
    }

    public String getReportsToChartOfAccountsCode() {
        return reportsToChartOfAccountsCode;
    }

    public void setReportsToChartOfAccountsCode(String reportsToChartOfAccountsCode) {
        this.reportsToChartOfAccountsCode = reportsToChartOfAccountsCode;
    }

    public String getReportsToAccountNumber() {
        return reportsToAccountNumber;
    }

    public void setReportsToAccountNumber(String reportsToAccountNumber) {
        this.reportsToAccountNumber = reportsToAccountNumber;
    }

    public String getAccountRestrictedStatusCode() {
        return accountRestrictedStatusCode;
    }

    public void setAccountRestrictedStatusCode(String accountRestrictedStatusCode) {
        this.accountRestrictedStatusCode = accountRestrictedStatusCode;
    }

    public Date getAccountRestrictedStatusDate() {
        return accountRestrictedStatusDate;
    }

    public void setAccountRestrictedStatusDate(Date accountRestrictedStatusDate) {
        this.accountRestrictedStatusDate = accountRestrictedStatusDate;
    }

    public String getEndowmentIncomeAcctFinCoaCd() {
        return endowmentIncomeAcctFinCoaCd;
    }

    public void setEndowmentIncomeAcctFinCoaCd(String endowmentIncomeAcctFinCoaCd) {
        this.endowmentIncomeAcctFinCoaCd = endowmentIncomeAcctFinCoaCd;
    }

    public String getEndowmentIncomeAccountNumber() {
        return endowmentIncomeAccountNumber;
    }

    public void setEndowmentIncomeAccountNumber(String endowmentIncomeAccountNumber) {
        this.endowmentIncomeAccountNumber = endowmentIncomeAccountNumber;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public String getBudgetRecordingLevelCode() {
        return budgetRecordingLevelCode;
    }

    public void setBudgetRecordingLevelCode(String budgetRecordingLevelCode) {
        this.budgetRecordingLevelCode = budgetRecordingLevelCode;
    }

    public boolean isExtrnlFinEncumSufficntFndIndicator() {
        return extrnlFinEncumSufficntFndIndicator;
    }

    public void setExtrnlFinEncumSufficntFndIndicator(boolean extrnlFinEncumSufficntFndIndicator) {
        this.extrnlFinEncumSufficntFndIndicator = extrnlFinEncumSufficntFndIndicator;
    }

    public boolean isIntrnlFinEncumSufficntFndIndicator() {
        return intrnlFinEncumSufficntFndIndicator;
    }

    public void setIntrnlFinEncumSufficntFndIndicator(boolean intrnlFinEncumSufficntFndIndicator) {
        this.intrnlFinEncumSufficntFndIndicator = intrnlFinEncumSufficntFndIndicator;
    }

    public boolean isFinPreencumSufficientFundIndicator() {
        return finPreencumSufficientFundIndicator;
    }

    public void setFinPreencumSufficientFundIndicator(boolean finPreencumSufficientFundIndicator) {
        this.finPreencumSufficientFundIndicator = finPreencumSufficientFundIndicator;
    }

    public boolean isFinancialObjectivePrsctrlIndicator() {
        return financialObjectivePrsctrlIndicator;
    }

    public void setFinancialObjectivePrsctrlIndicator(boolean financialObjectivePrsctrlIndicator) {
        this.financialObjectivePrsctrlIndicator = financialObjectivePrsctrlIndicator;
    }

    public String getContractControlFinCoaCode() {
        return contractControlFinCoaCode;
    }

    public void setContractControlFinCoaCode(String contractControlFinCoaCode) {
        this.contractControlFinCoaCode = contractControlFinCoaCode;
    }

    public String getContractControlAccountNumber() {
        return contractControlAccountNumber;
    }

    public void setContractControlAccountNumber(String contractControlAccountNumber) {
        this.contractControlAccountNumber = contractControlAccountNumber;
    }

    public String getAcctIndirectCostRcvyTypeCd() {
        return acctIndirectCostRcvyTypeCd;
    }

    public void setAcctIndirectCostRcvyTypeCd(String acctIndirectCostRcvyTypeCd) {
        this.acctIndirectCostRcvyTypeCd = acctIndirectCostRcvyTypeCd;
    }

    public Integer getContractsAndGrantsAccountResponsibilityId() {
        return contractsAndGrantsAccountResponsibilityId;
    }

    public void setContractsAndGrantsAccountResponsibilityId(Integer contractsAndGrantsAccountResponsibilityId) {
        this.contractsAndGrantsAccountResponsibilityId = contractsAndGrantsAccountResponsibilityId;
    }

    public String getInvoiceFrequencyCode() {
        return invoiceFrequencyCode;
    }

    public void setInvoiceFrequencyCode(String invoiceFrequencyCode) {
        this.invoiceFrequencyCode = invoiceFrequencyCode;
    }

    public String getInvoiceTypeCode() {
        return invoiceTypeCode;
    }

    public void setInvoiceTypeCode(String invoiceTypeCode) {
        this.invoiceTypeCode = invoiceTypeCode;
    }

    public Long getCostShareForProjectNumber() {
        return costShareForProjectNumber;
    }

    public void setCostShareForProjectNumber(Long costShareForProjectNumber) {
        this.costShareForProjectNumber = costShareForProjectNumber;
    }
    
    public Account getContractControlAccount() {
		return contractControlAccount;
	}

	public void setContractControlAccount(Account contractControlAccount) {
		this.contractControlAccount = contractControlAccount;
	}
	
    public Chart getContractControlChartOfAccounts() {
		return contractControlChartOfAccounts;
	}

	public void setContractControlChartOfAccounts(
			Chart contractControlChartOfAccounts) {
		this.contractControlChartOfAccounts = contractControlChartOfAccounts;
	}

	public IndirectCostRecoveryType getAcctIndirectCostRcvyType() {
		return acctIndirectCostRcvyType;
	}

	public void setAcctIndirectCostRcvyType(
			IndirectCostRecoveryType acctIndirectCostRcvyType) {
		this.acctIndirectCostRcvyType = acctIndirectCostRcvyType;
	}

	public InvoiceFrequency getInvoiceFrequency() {
		return invoiceFrequency;
	}

	public void setInvoiceFrequency(InvoiceFrequency invoiceFrequency) {
		this.invoiceFrequency = invoiceFrequency;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public AppropriationAccount getAppropriationAccount() {
		return appropriationAccount;
	}

	public void setAppropriationAccount(AppropriationAccount appropriationAccount) {
		this.appropriationAccount = appropriationAccount;
	}

	public Chart getFringeBenefitsChartOfAccount() {
		return fringeBenefitsChartOfAccount;
	}

	public void setFringeBenefitsChartOfAccount(Chart fringeBenefitsChartOfAccount) {
		this.fringeBenefitsChartOfAccount = fringeBenefitsChartOfAccount;
	}

	public Account getReportsToAccount() {
		return reportsToAccount;
	}

	public void setReportsToAccount(Account reportsToAccount) {
		this.reportsToAccount = reportsToAccount;
	}

	public RestrictedStatus getAccountRestrictedStatus() {
		return accountRestrictedStatus;
	}

	public void setAccountRestrictedStatus(RestrictedStatus accountRestrictedStatus) {
		this.accountRestrictedStatus = accountRestrictedStatus;
	}

	public Chart getEndowmentIncomeChartOfAccounts() {
		return endowmentIncomeChartOfAccounts;
	}

	public void setEndowmentIncomeChartOfAccounts(
			Chart endowmentIncomeChartOfAccounts) {
		this.endowmentIncomeChartOfAccounts = endowmentIncomeChartOfAccounts;
	}

	public Account getEndowmentIncomeAccount() {
		return endowmentIncomeAccount;
	}

	public void setEndowmentIncomeAccount(Account endowmentIncomeAccount) {
		this.endowmentIncomeAccount = endowmentIncomeAccount;
	}

	public BudgetRecordingLevel getBudgetRecordingLevel() {
		return budgetRecordingLevel;
	}

	public void setBudgetRecordingLevel(BudgetRecordingLevel budgetRecordingLevel) {
		this.budgetRecordingLevel = budgetRecordingLevel;
	}

	public InvoiceType getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(InvoiceType invoiceType) {
		this.invoiceType = invoiceType;
	}

	public SubFundProgram getSubFundProgram() {
		return subFundProgram;
	}

	public void setSubFundProgram(SubFundProgram subFundProgram) {
		this.subFundProgram = subFundProgram;
	}


	public List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts() {
		return indirectCostRecoveryAccounts;
	}


	public void setIndirectCostRecoveryAccounts(
			List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts) {
		this.indirectCostRecoveryAccounts = indirectCostRecoveryAccounts;
	}
	
    public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts() {
        List<IndirectCostRecoveryAccountChange> activeList = new ArrayList<IndirectCostRecoveryAccountChange>();
        for (IndirectCostRecoveryAccountChange icr : getIndirectCostRecoveryAccounts()){
            if (icr.isActive()){
                activeList.add(IndirectCostRecoveryAccountChange.copyICRAccount(icr));
            }
        }
        return activeList;
    }


	public boolean isRemoveAccountExpirationDate() {
		return removeAccountExpirationDate;
	}


	public void setRemoveAccountExpirationDate(boolean removeAccountExpirationDate) {
		this.removeAccountExpirationDate = removeAccountExpirationDate;
	}


	public boolean isRemoveContinuationChartAndAccount() {
		return removeContinuationChartAndAccount;
	}


	public void setRemoveContinuationChartAndAccount(
			boolean removeContinuationChartAndAccount) {
		this.removeContinuationChartAndAccount = removeContinuationChartAndAccount;
	}


}
