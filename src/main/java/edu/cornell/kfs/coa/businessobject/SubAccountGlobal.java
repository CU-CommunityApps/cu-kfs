package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.ReportingCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.service.PersistenceStructureService;

public class SubAccountGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {
	protected String documentNumber;
	
	protected boolean inactivate;    
	protected String subAccountName;
	protected String financialReportChartCode;
	protected String finReportOrganizationCode;
	protected String financialReportingCode;
	
	protected A21SubAccountChange a21SubAccount;
    
	protected ReportingCode reportingCode;
	protected Organization org;
	protected Chart financialReportChart;
  
    protected List<SubAccountGlobalDetail> subAccountGlobalDetails;
    protected List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts;
    
    /**
     * Constructs a SubAccountGlobal object.
     * 
     */
    public SubAccountGlobal() {
    	super();
    	a21SubAccount = new A21SubAccountChange();
    	subAccountGlobalDetails = new ArrayList<SubAccountGlobalDetail>();
    	indirectCostRecoveryAccounts = new ArrayList<IndirectCostRecoveryAccountChange>();
	}

	/**
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#generateDeactivationsToPersist()
	 */
	@Override
	public List<PersistableBusinessObject> generateDeactivationsToPersist() {
		List<PersistableBusinessObject>  objectsToDeactivate = new ArrayList<PersistableBusinessObject>();
		if(inactivate){
			for(SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails){
				subAccountGlobalDetail.refreshReferenceObject("subAccount");
				objectsToDeactivate.add(subAccountGlobalDetail.getSubAccount());			
			}
		}
		
		return objectsToDeactivate;
	}

	/**
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#generateGlobalChangesToPersist()
	 */
	@Override
	public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
		List<PersistableBusinessObject>  changesToPersist = new ArrayList<PersistableBusinessObject>();

		for (SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails) {
			subAccountGlobalDetail.refreshReferenceObject("subAccount");
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
		    
    		// update icr account collections
		    subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
		    List<A21IndirectCostRecoveryAccount> a21IcrAccounts = subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts();

			Map<Integer, Integer> alreadyUpdatedIndexes = new HashMap<Integer, Integer>();
			List<A21IndirectCostRecoveryAccount> addList = new ArrayList<A21IndirectCostRecoveryAccount>();

			// if there are any icr accounts entered on the global doc
			if (indirectCostRecoveryAccounts.size() > 0) {
				for (IndirectCostRecoveryAccountChange newICR : indirectCostRecoveryAccounts) {
					boolean found = false;

					int temp = -1;
					int i = 0;

					while (i < a21IcrAccounts.size() && (!found || (found && temp!=-1))) {
						if (!alreadyUpdatedIndexes.containsKey(i)) {
							A21IndirectCostRecoveryAccount existingICR = a21IcrAccounts.get(i);
							// check if we have a match on chart, account and percentage
							if (existingICR.getIndirectCostRecoveryFinCoaCode().equalsIgnoreCase(newICR.getIndirectCostRecoveryFinCoaCode()) 
									&& existingICR.getIndirectCostRecoveryAccountNumber().equalsIgnoreCase(newICR.getIndirectCostRecoveryAccountNumber()) 
									&& existingICR.getAccountLinePercent().equals(newICR.getAccountLinePercent())) {
								// set this to true if we have found a match
								found = true;
								// check if the they don't already both have the same active indicator
								if (newICR.isActive() == existingICR.isActive()) {
									// both the same, save position in temp and keep looking, if an entry exists that matches on chart, account and percentage but not same active indicator then we will update that one, otherwise we will consider a match on this temp entry
									temp = i;
								} else {
									// done, stop looking and update the active indicator
									existingICR.setActive(newICR.isActive());
									alreadyUpdatedIndexes.put(i, i);
									
									// reset temp since we have found a better match
									if(temp != -1){
										temp = -1;
									}
								}
							}
						}
						i++;
					}

					if (found && temp != -1) {
						// no need to update but we will add the index in already updated since there was a match
						alreadyUpdatedIndexes.put(temp, temp);
					}

					if (!found) {
						// add to active add or inactive add list
						A21IndirectCostRecoveryAccount icrAccount = createA21IndirectCostRecoveryAccountFromChange(subAccountGlobalDetail, newICR);
						addList.add(icrAccount);

					}

				}
				
				List<A21IndirectCostRecoveryAccount> updatedA21IcrAccounts = new ArrayList<A21IndirectCostRecoveryAccount>();
				updatedA21IcrAccounts.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());
				updatedA21IcrAccounts.addAll(addList);

				subAccountGlobalDetail.getSubAccount().getA21SubAccount().setA21IndirectCostRecoveryAccounts(updatedA21IcrAccounts);
			}
		    
			changesToPersist.add(subAccount);
		}
		
		return changesToPersist;
	}

	/**
	 * Creates an A21IndirectCostRecoveryAccount from the global icr change object.
	 * 
	 * @param subAccountGlobalDetail
	 * @param newICR
	 * @return an A21IndirectCostRecoveryAccount
	 */
	private A21IndirectCostRecoveryAccount createA21IndirectCostRecoveryAccountFromChange(SubAccountGlobalDetail subAccountGlobalDetail, IndirectCostRecoveryAccountChange newICR) {
		String chart = subAccountGlobalDetail.getChartOfAccountsCode();
		String account = subAccountGlobalDetail.getAccountNumber();

		A21IndirectCostRecoveryAccount icrAccount = new A21IndirectCostRecoveryAccount();
		icrAccount.setAccountNumber(account);
		icrAccount.setChartOfAccountsCode(chart);
		icrAccount.setIndirectCostRecoveryAccountNumber(newICR.getIndirectCostRecoveryAccountNumber());
		icrAccount.setIndirectCostRecoveryFinCoaCode(newICR.getIndirectCostRecoveryFinCoaCode());
		icrAccount.setActive(newICR.isActive());
		icrAccount.setAccountLinePercent(newICR.getAccountLinePercent());

		return icrAccount;
	}	

	/** 
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#getAllDetailObjects()
	 */
	@Override
	public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
		return subAccountGlobalDetails;
	}

	/**
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#isPersistable()
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
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#getDocumentNumber()
	 */
	public String getDocumentNumber() {
		return documentNumber;
	}

	/** 
	 * @see org.kuali.rice.krad.bo.GlobalBusinessObject#setDocumentNumber(java.lang.String)
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
	 * Gets indirectCostRecoveryAccounts.
	 * 
	 * @return indirectCostRecoveryAccounts
	 */
	public List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts() {
		return indirectCostRecoveryAccounts;
	}

	/**
	 * Sets indirectCostRecoveryAccounts
	 * 
	 * @param indirectCostRecoveryAccounts
	 */
	public void setIndirectCostRecoveryAccounts(List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts) {
		this.indirectCostRecoveryAccounts = indirectCostRecoveryAccounts;
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
	
    /**
     * Gets the active indirectCostRecoveryAccounts.
     * 
     * @return the active indirectCostRecoveryAccounts
     */
    public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts() {
        List<IndirectCostRecoveryAccountChange> activeList = new ArrayList<IndirectCostRecoveryAccountChange>();
        for (IndirectCostRecoveryAccountChange icr : getIndirectCostRecoveryAccounts()){
            if (icr.isActive()){
                activeList.add(IndirectCostRecoveryAccountChange.copyICRAccount(icr));
            }
        }
        return activeList;
    }

}
