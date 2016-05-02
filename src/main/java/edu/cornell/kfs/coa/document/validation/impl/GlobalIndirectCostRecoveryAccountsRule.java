package edu.cornell.kfs.coa.document.validation.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class GlobalIndirectCostRecoveryAccountsRule extends GlobalDocumentRuleBase {

	protected static final BigDecimal BD100 = new BigDecimal(100);

	protected SubFundGroupService subFundGroupService;

	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument, java.lang.String, org.kuali.kfs.krad.bo.PersistableBusinessObject)
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;
		success &= super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
		line.refreshNonUpdateableReferences();

		if (line instanceof IndirectCostRecoveryAccountChange) {
			IndirectCostRecoveryAccountChange account = (IndirectCostRecoveryAccountChange) line;
			success &= checkIndirectCostRecoveryAccount(account);
		}

		return success;
	}
	
	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomSaveDocumentBusinessRules(document);
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getNewBo();
		
		if( ObjectUtils.isNotNull(subAccountGlobal.getIndirectCostRecoveryAccounts()) && subAccountGlobal.getIndirectCostRecoveryAccounts().size() >0){
			validateIndirectCostRecoveryAccounts(subAccountGlobal.getIndirectCostRecoveryAccounts());
		}
		return success;
	}

	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomRouteDocumentBusinessRules(document);
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getNewBo();
		
		if( ObjectUtils.isNotNull(subAccountGlobal.getIndirectCostRecoveryAccounts()) && subAccountGlobal.getIndirectCostRecoveryAccounts().size() >0){
			success &= validateIndirectCostRecoveryAccounts(subAccountGlobal.getIndirectCostRecoveryAccounts());
			if(success){
				success &= checkIndirectCostRecoveryAccountDistributions(subAccountGlobal.getSubAccountGlobalDetails(), subAccountGlobal.getIndirectCostRecoveryAccounts());
			}
		}
		
		return success;
	}
	
	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomApproveDocumentBusinessRules(document);
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getNewBo();
		
		if( ObjectUtils.isNotNull(subAccountGlobal.getIndirectCostRecoveryAccounts()) && subAccountGlobal.getIndirectCostRecoveryAccounts().size() >0){
			success &= validateIndirectCostRecoveryAccounts(subAccountGlobal.getIndirectCostRecoveryAccounts());
			if(success){
				success &= checkIndirectCostRecoveryAccountDistributions(subAccountGlobal.getSubAccountGlobalDetails(), subAccountGlobal.getIndirectCostRecoveryAccounts());
			}
		}
		
		return success;
	}
	
	/**
	 * Validates indirect cost recovery accounts
	 * 
	 * @param indirectCostRecoveryAccounts
	 * @return
	 */
	protected boolean validateIndirectCostRecoveryAccounts(List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts){
		boolean success = true;
		int index = 0;
        for (IndirectCostRecoveryAccountChange icrAccount : indirectCostRecoveryAccounts) {
            String errorPath = MAINTAINABLE_ERROR_PREFIX + KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS + "[" + index + "]";
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);
            success &= checkIndirectCostRecoveryAccount(icrAccount);             
            GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            index++;
        }
        return success;
	}

	/**
	 * Validates indirect cost recovery account.
	 * 
	 * @param icrAccount
	 * @return true if valid, false otherwise
	 */
	protected boolean checkIndirectCostRecoveryAccount(IndirectCostRecoveryAccountChange icrAccount) {

		boolean success = true;

		// The chart and account must exist in the database.
		String chartOfAccountsCode = icrAccount.getIndirectCostRecoveryFinCoaCode();
		String accountNumber = icrAccount.getIndirectCostRecoveryAccountNumber();
		BigDecimal icraAccountLinePercentage = ObjectUtils.isNotNull(icrAccount.getAccountLinePercent()) ? icrAccount.getAccountLinePercent() : BigDecimal.ZERO;
		boolean active = icrAccount.isActive();
		
		if (StringUtils.isBlank(chartOfAccountsCode)) {
			GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_CHART_OF_ACCOUNTS_CODE, KFSKeyConstants.ERROR_REQUIRED, getDDAttributeLabel(KFSPropertyConstants.ICR_CHART_OF_ACCOUNTS_CODE));
			success &= false;
		}

		if (StringUtils.isBlank(accountNumber)) {
			GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_REQUIRED, getDDAttributeLabel(KFSPropertyConstants.ICR_ACCOUNT_NUMBER));
			success &= false;
		}

		if (StringUtils.isNotBlank(chartOfAccountsCode) && StringUtils.isNotBlank(accountNumber)) {
			Map<String, String> chartAccountMap = new HashMap<String, String>();
			chartAccountMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
			if (getBoService().countMatching(Chart.class, chartAccountMap) < 1) {
				GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_CHART_OF_ACCOUNTS_CODE, KFSKeyConstants.ERROR_EXISTENCE, getDDAttributeLabel(KFSPropertyConstants.ICR_CHART_OF_ACCOUNTS_CODE));
				success &= false;
			}
			chartAccountMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
			if (getBoService().countMatching(Account.class, chartAccountMap) < 1) {
				GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getDDAttributeLabel(KFSPropertyConstants.ICR_ACCOUNT_NUMBER));
				success &= false;
			}

			// check if account is closed
			if (success && active) {
				Collection<Account> accounts = getBoService().findMatching(Account.class, chartAccountMap);
				if (ObjectUtils.isNotNull(accounts) && accounts.size() > 0) {
					Account account = accounts.iterator().next();
					if (account.isClosed()) {
						GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_CANNOT_BE_INACTIVE, chartOfAccountsCode + "-" + accountNumber);
						success &= false;
					}
				}
			}
		}

		// check the percent line
		if (icraAccountLinePercentage.compareTo(BigDecimal.ZERO) <= 0 || icraAccountLinePercentage.compareTo(BD100) == 1) {
			GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_ACCOUNT_LINE_PERCENT, KFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_INVALID_LINE_PERCENT);
			success &= false;
		}

		return success;
	}


	/**
	 * Checks the total distribution is 100.
	 * 
	 * @param globalDetails
	 * @param newIcrAccounts
	 * @return true if valid, false otherwise
	 */
	protected boolean checkIndirectCostRecoveryAccountDistributions(List<SubAccountGlobalDetail> globalDetails, List<IndirectCostRecoveryAccountChange> newIcrAccounts) {

		boolean result = true;
		
		if(ObjectUtils.isNull(newIcrAccounts) || (newIcrAccounts.size() == 0)){
			return true;
		}
		
		for (GlobalBusinessObjectDetailBase globalDetail : globalDetails) {
			List<IndirectCostRecoveryAccount> existingIcrAccounts = new ArrayList<IndirectCostRecoveryAccount>();

			SubAccountGlobalDetail subAccountGlobalDetail = (SubAccountGlobalDetail) globalDetail;

			subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
			
			List<A21IndirectCostRecoveryAccount> a21IcrAccounts = subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts();
			List<IndirectCostRecoveryAccount> currentActiveIndirectCostRecoveryAccountList = new ArrayList<IndirectCostRecoveryAccount>();
			List<IndirectCostRecoveryAccount> existingICRs = new ArrayList<IndirectCostRecoveryAccount>(); 
			List<IndirectCostRecoveryAccount> existingICRsWorkList = new ArrayList<IndirectCostRecoveryAccount>();
			
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

			for (IndirectCostRecoveryAccount icrAccount : existingIcrAccounts) {
				existingICRsWorkList.add(icrAccount);
			}
			
			List<IndirectCostRecoveryAccountChange> newICRs = newIcrAccounts;
			List<IndirectCostRecoveryAccount> updateActive = new ArrayList<IndirectCostRecoveryAccount>();
			List<IndirectCostRecoveryAccount> addActive = new ArrayList<IndirectCostRecoveryAccount>();


			if (newIcrAccounts.size() > 0) {
				for (IndirectCostRecoveryAccountChange newICR : newICRs) {
					boolean found = false;
					existingICRs.clear();
					for (IndirectCostRecoveryAccount icrAccount : existingICRsWorkList) {
						existingICRs.add(icrAccount);
					}
					
					IndirectCostRecoveryAccount tempIcrAccount = null;
					int i = 0;
					
					while(i < existingICRs.size() && (!found || (found && ObjectUtils.isNotNull(tempIcrAccount))) ){
						IndirectCostRecoveryAccount existingICR = existingICRs.get(i);
						
						if (existingICR.getIndirectCostRecoveryFinCoaCode().equalsIgnoreCase(newICR.getIndirectCostRecoveryFinCoaCode()) 
								&& existingICR.getIndirectCostRecoveryAccountNumber().equalsIgnoreCase(newICR.getIndirectCostRecoveryAccountNumber()) 
								&& existingICR.getAccountLinePercent().equals(newICR.getAccountLinePercent())) {
							
							found = true;
							if (newICR.isActive() == existingICR.isActive()) {
								// both have the same active indicator, save in
								// a temp and keep looking
								tempIcrAccount = existingICR;
							} else {
								// done stop looking
								if (newICR.isActive()) {
									// add to update active
									IndirectCostRecoveryAccount icrAccount = createIndirectCostRecoveryAccountFromChange(
											subAccountGlobalDetail, newICR);
									updateActive.add(icrAccount);
								}
								
								existingICRsWorkList.remove(existingICR);
								if(ObjectUtils.isNotNull(tempIcrAccount)){
									tempIcrAccount = null;
								}
							}
						}
						
						i++;
					}

					if (found && ObjectUtils.isNotNull(tempIcrAccount)) {
						// done stop looking
						if (tempIcrAccount.isActive()) {
							// add to update active
							IndirectCostRecoveryAccount icrAccount = createIndirectCostRecoveryAccountFromChange(subAccountGlobalDetail, newICR);
							updateActive.add(icrAccount);
						} 

						existingICRsWorkList.remove(tempIcrAccount);
					}

					if (!found) {
						// done stop looking
						if (newICR.isActive()) {
							// add to update active
							IndirectCostRecoveryAccount icrAccount = createIndirectCostRecoveryAccountFromChange(subAccountGlobalDetail, newICR);
							addActive.add(icrAccount);
						}
					}
				}
				
				if (updateActive.size() > 0) {
					for (IndirectCostRecoveryAccount icrAccount : updateActive) {
						currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
					}
				}

				if (addActive.size() > 0) {
					for (IndirectCostRecoveryAccount icrAccount : addActive) {
						currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
					}
				}

				if (ObjectUtils.isNotNull(existingICRsWorkList) && !existingICRsWorkList.isEmpty()) {
					for (IndirectCostRecoveryAccount icrAccount : existingICRsWorkList) {
						if (icrAccount.isActive()) {
							currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
						}
					}
				}

				if ((ObjectUtils.isNull(currentActiveIndirectCostRecoveryAccountList) || (currentActiveIndirectCostRecoveryAccountList.size() == 0))) {
					return true;
				}

				BigDecimal totalDistribution = BigDecimal.ZERO;

				if (ObjectUtils.isNotNull(currentActiveIndirectCostRecoveryAccountList) && (currentActiveIndirectCostRecoveryAccountList.size() > 0)) {
					for (IndirectCostRecoveryAccount icra : currentActiveIndirectCostRecoveryAccountList) {
						totalDistribution = totalDistribution.add(icra.getAccountLinePercent());
					}
				}

				// check the total distribution is 100
				if (totalDistribution.compareTo(BD100) != 0) {
					putFieldError(CUKFSPropertyConstants.SUB_ACCOUNT_GLBL_CHANGE_DETAILS, CUKFSKeyConstants.ERROR_DOCUMENT_GLB_MAINT_ICR_ACCOUNT_TOTAL_NOT_100_PERCENT, new String[] { subAccountGlobalDetail.getChartOfAccountsCode() + "-"+ subAccountGlobalDetail.getAccountNumber() + "-" + subAccountGlobalDetail.getSubAccountNumber() });
					result &= false;
				}
			}
		}

		return result;
	}
	
	/**
	 * Creates an IndirectCostRecoveryAccount from the global icr change object.
	 * 
	 * @param subAccountGlobalDetail
	 * @param newICR
	 * @return an IndirectCostRecoveryAccount
	 */
	private IndirectCostRecoveryAccount createIndirectCostRecoveryAccountFromChange(SubAccountGlobalDetail subAccountGlobalDetail, IndirectCostRecoveryAccountChange newICR){
		String chart = subAccountGlobalDetail.getChartOfAccountsCode();
		String account = subAccountGlobalDetail.getAccountNumber();
		
		IndirectCostRecoveryAccount icrAccount = new IndirectCostRecoveryAccount();
		icrAccount.setAccountNumber(account);
		icrAccount.setChartOfAccountsCode(chart);
		icrAccount.setIndirectCostRecoveryAccountNumber(newICR.getIndirectCostRecoveryAccountNumber());
		icrAccount.setIndirectCostRecoveryFinCoaCode(newICR.getIndirectCostRecoveryFinCoaCode());
		icrAccount.setActive(newICR.isActive());
		icrAccount.setAccountLinePercent(newICR.getAccountLinePercent());
		
		return icrAccount;
	}

	/**
	 * Gets the subFundGroupService.
	 * 
	 * @return subFundGroupService
	 */
	public SubFundGroupService getSubFundGroupService() {
		if (subFundGroupService == null) {
			subFundGroupService = SpringContext.getBean(SubFundGroupService.class);
		}
		return subFundGroupService;
	}

	/**
	 * Sets the subFundGroupService.
	 * 
	 * @param subFundGroupService
	 */
	public void setSubFundGroupService(SubFundGroupService subFundGroupService) {
		this.subFundGroupService = subFundGroupService;
	}

	/**
	 * Gets the DD attribute label.
	 * 
	 * @param attribute the DD attribute label
	 * @return
	 */
	protected String getDDAttributeLabel(String attribute) {
		return ddService.getAttributeLabel(IndirectCostRecoveryAccount.class, attribute);
	}

}
