package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.ReportingCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.coa.service.GlobalObjectWithIndirectCostRecoveryAccountsService;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;


public class SubAccountGlobal extends PersistableBusinessObjectBase implements GlobalObjectWithIndirectCostRecoveryAccounts {

    protected String documentNumber;

    protected boolean inactivate;
    protected String subAccountName;
    protected String financialReportChartCode;
    protected String finReportOrganizationCode;
    protected String financialReportingCode;

    protected Long nextNewAccountDetailSequenceNumber;
    protected String newSubAccountName;
    protected String newSubAccountNumber;
    protected String newSubAccountTypeCode;
    protected boolean newSubAccountOffCampusCode;
    protected boolean applyToAllNewSubAccounts;

    protected A21SubAccountChange a21SubAccount;

    protected ReportingCode reportingCode;
    protected Organization org;
    protected Chart financialReportChart;

    protected List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts;
    protected List<SubAccountGlobalDetail> subAccountGlobalDetails;
    protected List<SubAccountGlobalNewAccountDetail> subAccountGlobalNewAccountDetails;
    
    protected transient GlobalObjectWithIndirectCostRecoveryAccountsService globalObjectWithIndirectCostRecoveryAccountsService;

    /**
     * Constructs a SubAccountGlobal object.
     * 
     */
    public SubAccountGlobal() {
    	super();
    	nextNewAccountDetailSequenceNumber = Long.valueOf(1L);
    	a21SubAccount = new A21SubAccountChange();
    	subAccountGlobalDetails = new ArrayList<SubAccountGlobalDetail>();
    	subAccountGlobalNewAccountDetails = new ArrayList<>();
    	indirectCostRecoveryAccounts = new ArrayList<IndirectCostRecoveryAccountChange>();
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#generateDeactivationsToPersist()
	 */
	@Override
	public List<PersistableBusinessObject> generateDeactivationsToPersist() {
		List<PersistableBusinessObject>  objectsToDeactivate = new ArrayList<PersistableBusinessObject>();
		if(inactivate){
			for(SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails){
				subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
				objectsToDeactivate.add(subAccountGlobalDetail.getSubAccount());			
			}
		}
		
		return objectsToDeactivate;
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#generateGlobalChangesToPersist()
	 */
	@Override
	public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
		List<PersistableBusinessObject>  changesToPersist = new ArrayList<PersistableBusinessObject>();
		this.refreshReferenceObject(KFSPropertyConstants.A21_SUB_ACCOUNT);

		for (SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails) {
			subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
			SubAccount subAccount = subAccountGlobalDetail.getSubAccount();
			
			if (inactivate) {
				subAccount.setActive(false);
			}

			if (StringUtils.isNotBlank(subAccountName)) {
				subAccount.setSubAccountName(subAccountName);
			}

			if (a21SubAccount.offCampusCode) {
				subAccount.getA21SubAccount().setOffCampusCode(true);
			}

			if (StringUtils.isNotBlank(financialReportChartCode)) {
				subAccount.setFinancialReportChartCode(financialReportChartCode);
			}

			if (StringUtils.isNotBlank(finReportOrganizationCode)) {
				subAccount.setFinReportOrganizationCode(finReportOrganizationCode);
			}

			if (StringUtils.isNotBlank(financialReportingCode)) {
				subAccount.setFinancialReportingCode(financialReportingCode);
			}
	
		    if(StringUtils.isNotBlank(a21SubAccount.indirectCostRecoveryTypeCode)){
		    	subAccount.getA21SubAccount().setIndirectCostRecoveryTypeCode(a21SubAccount.indirectCostRecoveryTypeCode);
		    }
		    
		    if(StringUtils.isNotBlank(a21SubAccount.financialIcrSeriesIdentifier)){
		    	subAccount.getA21SubAccount().setFinancialIcrSeriesIdentifier(a21SubAccount.financialIcrSeriesIdentifier);
		    }

		    if(StringUtils.isNotBlank(a21SubAccount.costShareChartOfAccountCode)){
		    	subAccount.getA21SubAccount().setCostShareChartOfAccountCode(a21SubAccount.costShareChartOfAccountCode);
		    }

		    if(StringUtils.isNotBlank(a21SubAccount.costShareSourceAccountNumber)){
		    	subAccount.getA21SubAccount().setCostShareSourceAccountNumber(a21SubAccount.costShareSourceAccountNumber);
		    }
		    
		    if(StringUtils.isNotBlank(a21SubAccount.costShareSourceSubAccountNumber)){
		    	subAccount.getA21SubAccount().setCostShareSourceSubAccountNumber(a21SubAccount.costShareSourceSubAccountNumber);
		    }
		    
		    List<IndirectCostRecoveryAccount> icrAccounts = new ArrayList<IndirectCostRecoveryAccount>();

		    for(IndirectCostRecoveryAccount icrAccount : subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts()){
		    	icrAccounts.add(icrAccount);
		    }

		    updateIcrAccounts(subAccountGlobalDetail, icrAccounts);
		    
			changesToPersist.add(subAccount);
		}
		
		if (!subAccountGlobalNewAccountDetails.isEmpty()) {
		    createAndAddGlobalNewSubAccountChangesToPersist(changesToPersist::add);
		}
		
		return changesToPersist;
	}

    protected void createAndAddGlobalNewSubAccountChangesToPersist(Consumer<PersistableBusinessObject> changeConsumer) {
        subAccountGlobalNewAccountDetails.stream()
                .map(this::createSubAccount)
                .forEach(changeConsumer);
    }

    protected SubAccount createSubAccount(SubAccountGlobalNewAccountDetail newAccountDetail) {
        if (applyToAllNewSubAccounts) {
            return createSubAccount(newAccountDetail, newSubAccountNumber, newSubAccountName, newSubAccountOffCampusCode);
        } else {
            return createSubAccount(newAccountDetail, newAccountDetail.getSubAccountNumber(),
                    newAccountDetail.getSubAccountName(), newAccountDetail.isOffCampusCode());
        }
    }

    protected SubAccount createSubAccount(SubAccountGlobalNewAccountDetail newAccountDetail,
            String subAccountNumberToUse, String subAccountNameToUse, boolean offCampusCode) {
        SubAccount subAccount = new SubAccount();
        subAccount.setSubAccountName(subAccountNameToUse);
        subAccount.setChartOfAccountsCode(newAccountDetail.getChartOfAccountsCode());
        subAccount.setAccountNumber(newAccountDetail.getAccountNumber());
        subAccount.setSubAccountNumber(subAccountNumberToUse);
        subAccount.setA21SubAccount(createA21SubAccount(newAccountDetail, subAccountNumberToUse, offCampusCode));
        subAccount.setActive(true);
        
        if (StringUtils.isNotBlank(financialReportChartCode)) {
            subAccount.setFinancialReportChartCode(financialReportChartCode);
        }
        if (StringUtils.isNotBlank(finReportOrganizationCode)) {
            subAccount.setFinReportOrganizationCode(finReportOrganizationCode);
        }
        if (StringUtils.isNotBlank(financialReportingCode)) {
            subAccount.setFinancialReportingCode(financialReportingCode);
        }
        
        newAccountDetail.setSubAccount(subAccount);
        updateIcrAccounts(newAccountDetail, Collections.emptyList());
        
        return subAccount;
    }

    protected A21SubAccount createA21SubAccount(SubAccountGlobalNewAccountDetail newAccountDetail,
            String subAccountNumberToUse, boolean offCampusCode) {
        A21SubAccount newA21SubAccount = new A21SubAccount();
        newA21SubAccount.setChartOfAccountsCode(newAccountDetail.getChartOfAccountsCode());
        newA21SubAccount.setAccountNumber(newAccountDetail.getAccountNumber());
        newA21SubAccount.setSubAccountNumber(subAccountNumberToUse);
        newA21SubAccount.setOffCampusCode(offCampusCode);
        newA21SubAccount.setSubAccountTypeCode(newSubAccountTypeCode);
        
        if (StringUtils.isNotBlank(a21SubAccount.indirectCostRecoveryTypeCode)) {
            newA21SubAccount.setIndirectCostRecoveryTypeCode(a21SubAccount.indirectCostRecoveryTypeCode);
        }
        if (StringUtils.isNotBlank(a21SubAccount.financialIcrSeriesIdentifier)) {
            newA21SubAccount.setFinancialIcrSeriesIdentifier(a21SubAccount.financialIcrSeriesIdentifier);
        }
        if (StringUtils.isNotBlank(a21SubAccount.costShareChartOfAccountCode)) {
            newA21SubAccount.setCostShareChartOfAccountCode(a21SubAccount.costShareChartOfAccountCode);
        }
        if (StringUtils.isNotBlank(a21SubAccount.costShareSourceAccountNumber)) {
            newA21SubAccount.setCostShareSourceAccountNumber(a21SubAccount.costShareSourceAccountNumber);
        }
        if (StringUtils.isNotBlank(a21SubAccount.costShareSourceSubAccountNumber)) {
            newA21SubAccount.setCostShareSourceSubAccountNumber(a21SubAccount.costShareSourceSubAccountNumber);
        }
        
        return newA21SubAccount;
    }

	public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts() {
	    return getGlobalObjectWithIndirectCostRecoveryAccountsService().getActiveIndirectCostRecoveryAccounts(this);
	}

	public boolean hasIcrAccounts(){
		return ObjectUtils.isNotNull(indirectCostRecoveryAccounts) && indirectCostRecoveryAccounts.size() > 0;
	}

	@Override
	public Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> getGlobalObjectDetailsAndIcrAccountsMap() {
		Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> globalObjectDetailsAndIcrAccountsMap = new HashMap<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>>();
		List<SubAccountGlobalDetail> subAccountGlobalDetails = getSubAccountGlobalDetails();

		if (ObjectUtils.isNotNull(subAccountGlobalDetails)&& !subAccountGlobalDetails.isEmpty()) {
			for (GlobalBusinessObjectDetailBase globalDetail : subAccountGlobalDetails) {
				List<IndirectCostRecoveryAccount> existingIcrAccounts = new ArrayList<IndirectCostRecoveryAccount>();

				SubAccountGlobalDetail subAccountGlobalDetail = (SubAccountGlobalDetail) globalDetail;

				subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);

				List<A21IndirectCostRecoveryAccount> a21IcrAccounts = subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts();

				for (A21IndirectCostRecoveryAccount a21ICRAccount : a21IcrAccounts) {
					IndirectCostRecoveryAccount icrAcct = new IndirectCostRecoveryAccount();
					icrAcct.setChartOfAccountsCode(a21ICRAccount.getChartOfAccountsCode());
					icrAcct.setAccountNumber(a21ICRAccount.getAccountNumber());
					icrAcct.setIndirectCostRecoveryAccountNumber(a21ICRAccount.getIndirectCostRecoveryAccountNumber());
					icrAcct.setIndirectCostRecoveryFinCoaCode(a21ICRAccount.getIndirectCostRecoveryFinCoaCode());
					icrAcct.setAccountLinePercent(a21ICRAccount.getAccountLinePercent());
					icrAcct.setActive(a21ICRAccount.isActive());
					existingIcrAccounts.add(icrAcct);
				}
				globalObjectDetailsAndIcrAccountsMap.put(globalDetail,existingIcrAccounts);
			}

		}
		return globalObjectDetailsAndIcrAccountsMap;
	}

	@Override
	public IndirectCostRecoveryAccount createIndirectCostRecoveryAccountFromChange(GlobalBusinessObjectDetailBase globalDetail, IndirectCostRecoveryAccountChange newICR) {
		String chart;
		String account;
		if (globalDetail instanceof SubAccountGlobalDetail) {
		    SubAccountGlobalDetail subAccountGlobalDetail = (SubAccountGlobalDetail) globalDetail;
	        chart = subAccountGlobalDetail.getChartOfAccountsCode();
	        account = subAccountGlobalDetail.getAccountNumber();
		} else if (globalDetail instanceof SubAccountGlobalNewAccountDetail) {
		    SubAccountGlobalNewAccountDetail newAccountDetail = (SubAccountGlobalNewAccountDetail) globalDetail;
            chart = newAccountDetail.getChartOfAccountsCode();
            account = newAccountDetail.getAccountNumber();
		} else {
		    throw new IllegalArgumentException("Unexpected globalDetail implementation for creating ICR Account: " + globalDetail.getClass());
		}

		A21IndirectCostRecoveryAccount icrAccount = new A21IndirectCostRecoveryAccount();
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
			GlobalBusinessObjectDetailBase globalDetail,
			List<IndirectCostRecoveryAccount> updatedIcrAccounts) {
		List<A21IndirectCostRecoveryAccount> updatedA21IcrAccounts = new ArrayList<A21IndirectCostRecoveryAccount>();
		for(IndirectCostRecoveryAccount icrAccount : updatedIcrAccounts){
			updatedA21IcrAccounts.add(A21IndirectCostRecoveryAccount.copyICRAccount(icrAccount));
		}
		if (globalDetail instanceof SubAccountGlobalDetail) {
		    SubAccountGlobalDetail subAccountGlobalDetail = (SubAccountGlobalDetail) globalDetail;
		    subAccountGlobalDetail.getSubAccount().getA21SubAccount().setA21IndirectCostRecoveryAccounts(updatedA21IcrAccounts);
		} else if (globalDetail instanceof SubAccountGlobalNewAccountDetail) {
		    SubAccountGlobalNewAccountDetail newAccountDetail = (SubAccountGlobalNewAccountDetail) globalDetail;
		    newAccountDetail.getSubAccount().getA21SubAccount().setA21IndirectCostRecoveryAccounts(updatedA21IcrAccounts);
		} else {
		    throw new IllegalArgumentException("Unexpected globalDetail implementation for updating ICR Account collection: " + globalDetail.getClass());
		}
	}

	public void updateIcrAccounts(GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> icrAccounts){
		getGlobalObjectWithIndirectCostRecoveryAccountsService().updateIcrAccounts(this, globalDetail, icrAccounts);
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#getAllDetailObjects()
	 */
	@Override
	public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
	    List<GlobalBusinessObjectDetail> detailObjects = new ArrayList<>();
	    detailObjects.addAll(subAccountGlobalDetails);
	    detailObjects.addAll(subAccountGlobalNewAccountDetails);
		return subAccountGlobalDetails;
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#isPersistable()
	 */
	@Override
	public boolean isPersistable() {
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        // fail if the PK for this object is emtpy
        if (StringUtils.isBlank(documentNumber)) {
            return false;
        }

        // fail if the PKs for any of the contained objects are empty
        for (SubAccountGlobalDetail subAccount : getSubAccountGlobalDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(subAccount)) {
                return false;
            }
        }

        // otherwise, its all good
        return true;
	}

	/**
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#getDocumentNumber()
	 */
	public String getDocumentNumber() {
		return documentNumber;
	}

	/** 
	 * @see org.kuali.kfs.krad.bo.GlobalBusinessObject#setDocumentNumber(java.lang.String)
	 */
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	/**
	 * Gets subAccountGlobalDetails.
	 * 
	 * @return subAccountGlobalDetails
	 */
	public List<SubAccountGlobalDetail> getSubAccountGlobalDetails() {
		return subAccountGlobalDetails;
	}

	/**
	 * Sets subAccountGlobalDetails.
	 * 
	 * @param subAccountGlobalDetails
	 */
	public void setSubAccountGlobalDetails(List<SubAccountGlobalDetail> subAccountGlobalDetails) {
		this.subAccountGlobalDetails = subAccountGlobalDetails;
	}

	/**
	 * Gets inactivate.
	 * 
	 * @return inactivate
	 */
	public boolean isInactivate() {
		return inactivate;
	}

	/**
	 * Sets inactivate.
	 * 
	 * @param inactivate
	 */
	public void setInactivate(boolean inactivate) {
		this.inactivate = inactivate;
	}

	/**
	 * Gets subAccountName.
	 * 
	 * @return subAccountName
	 */
	public String getSubAccountName() {
		return subAccountName;
	}

	/**
	 * Sets subAccountName.
	 * 
	 * @param subAccountName
	 */
	public void setSubAccountName(String subAccountName) {
		this.subAccountName = subAccountName;
	}

	/**
	 * Gets financialReportChartCode.
	 * 
	 * @return financialReportChartCode
	 */
	public String getFinancialReportChartCode() {
		return financialReportChartCode;
	}

	/**
	 * Sets financialReportChartCode.
	 * 
	 * @param financialReportChartCode
	 */
	public void setFinancialReportChartCode(String financialReportChartCode) {
		this.financialReportChartCode = financialReportChartCode;
	}

	/**
	 * Gets finReportOrganizationCode.
	 * 
	 * @return finReportOrganizationCode
	 */
	public String getFinReportOrganizationCode() {
		return finReportOrganizationCode;
	}

	/**
	 * Sets finReportOrganizationCode.
	 * 
	 * @param finReportOrganizationCode
	 */
	public void setFinReportOrganizationCode(String finReportOrganizationCode) {
		this.finReportOrganizationCode = finReportOrganizationCode;
	}

	/**
	 * Gets financialReportingCode.
	 * 
	 * @return financialReportingCode
	 */
	public String getFinancialReportingCode() {
		return financialReportingCode;
	}

	/**
	 * Sets financialReportingCode.
	 * 
	 * @param financialReportingCode
	 */
	public void setFinancialReportingCode(String financialReportingCode) {
		this.financialReportingCode = financialReportingCode;
	}

	/**
	 * Gets a21SubAccount.
	 * 
	 * @return a21SubAccount
	 */
	public A21SubAccountChange getA21SubAccount() {
		return a21SubAccount;
	}

	/**
	 * Sets a21SubAccount.
	 * 
	 * @param a21SubAccount
	 */
	public void setA21SubAccount(A21SubAccountChange a21SubAccount) {
		this.a21SubAccount = a21SubAccount;
	}

	/**
	 * Gets reportingCode.
	 * 
	 * @return reportingCode
	 */
	public ReportingCode getReportingCode() {
		return reportingCode;
	}

	/**
	 * Sets reportingCode.
	 * 
	 * @param reportingCode
	 */
	public void setReportingCode(ReportingCode reportingCode) {
		this.reportingCode = reportingCode;
	}

	/**
	 * Gets org.
	 * 
	 * @return org
	 */
	public Organization getOrg() {
		return org;
	}

	/**
	 * Sets org.
	 * 
	 * @param org
	 */
	public void setOrg(Organization org) {
		this.org = org;
	}

	/**
	 * Gets financialReportChart.
	 * 
	 * @return financialReportChart
	 */
	public Chart getFinancialReportChart() {
		return financialReportChart;
	}

	/**
	 * Sets financialReportChart.
	 * 
	 * @param financialReportChart
	 */
	public void setFinancialReportChart(Chart financialReportChart) {
		this.financialReportChart = financialReportChart;
	}
	
	@Override
	public List<? extends GlobalBusinessObjectDetailBase> getGlobalObjectDetails() {
		return getSubAccountGlobalDetails();
	}

	@Override
	public String getGlobalDetailsPropertyName() {
		return CUKFSPropertyConstants.SUB_ACCOUNT_GLBL_CHANGE_DETAILS;
	}

	public List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts() {
		return indirectCostRecoveryAccounts;
	}

	public void setIndirectCostRecoveryAccounts(
			List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts) {
		this.indirectCostRecoveryAccounts = indirectCostRecoveryAccounts;
	}

	public Long getNextNewAccountDetailSequenceNumber() {
        return nextNewAccountDetailSequenceNumber;
    }

    public void setNextNewAccountDetailSequenceNumber(Long nextNewAccountDetailSequenceNumber) {
        this.nextNewAccountDetailSequenceNumber = nextNewAccountDetailSequenceNumber;
    }

    public String getNewSubAccountName() {
        return newSubAccountName;
    }

    public void setNewSubAccountName(String newSubAccountName) {
        this.newSubAccountName = newSubAccountName;
    }

    public String getNewSubAccountNumber() {
        return newSubAccountNumber;
    }

    public void setNewSubAccountNumber(String newSubAccountNumber) {
        this.newSubAccountNumber = newSubAccountNumber;
    }

    public String getNewSubAccountTypeCode() {
        return newSubAccountTypeCode;
    }

    public void setNewSubAccountTypeCode(String newSubAccountTypeCode) {
        this.newSubAccountTypeCode = newSubAccountTypeCode;
    }

    public boolean isNewSubAccountOffCampusCode() {
        return newSubAccountOffCampusCode;
    }

    public void setNewSubAccountOffCampusCode(boolean newSubAccountOffCampusCode) {
        this.newSubAccountOffCampusCode = newSubAccountOffCampusCode;
    }

    public boolean isApplyToAllNewSubAccounts() {
        return applyToAllNewSubAccounts;
    }

    public void setApplyToAllNewSubAccounts(boolean applyToAllNewSubAccounts) {
        this.applyToAllNewSubAccounts = applyToAllNewSubAccounts;
    }

    public List<SubAccountGlobalNewAccountDetail> getSubAccountGlobalNewAccountDetails() {
        return subAccountGlobalNewAccountDetails;
    }

    public void setSubAccountGlobalNewAccountDetails(
            List<SubAccountGlobalNewAccountDetail> subAccountGlobalNewAccountDetails) {
        this.subAccountGlobalNewAccountDetails = subAccountGlobalNewAccountDetails;
    }

    public GlobalObjectWithIndirectCostRecoveryAccountsService getGlobalObjectWithIndirectCostRecoveryAccountsService() {
	    if (this.globalObjectWithIndirectCostRecoveryAccountsService == null) {
	        this.setGlobalObjectWithIndirectCostRecoveryAccountsService(SpringContext.getBean(GlobalObjectWithIndirectCostRecoveryAccountsService.class));
	    }
	    return this.globalObjectWithIndirectCostRecoveryAccountsService;
	}

	public void setGlobalObjectWithIndirectCostRecoveryAccountsService(GlobalObjectWithIndirectCostRecoveryAccountsService globalObjectWithIndirectCostRecoveryAccountsService) {
	    this.globalObjectWithIndirectCostRecoveryAccountsService = globalObjectWithIndirectCostRecoveryAccountsService;
	}

}
