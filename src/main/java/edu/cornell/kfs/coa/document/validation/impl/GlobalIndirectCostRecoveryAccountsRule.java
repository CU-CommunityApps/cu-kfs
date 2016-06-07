package edu.cornell.kfs.coa.document.validation.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.validation.impl.GlobalDocumentRuleBase;
import org.kuali.kfs.coa.service.SubFundGroupService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class GlobalIndirectCostRecoveryAccountsRule extends GlobalDocumentRuleBase {

	protected static final BigDecimal ONE_HUNDRED_PERCENT = new BigDecimal(100);

	protected SubFundGroupService subFundGroupService;

	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument, java.lang.String, org.kuali.rice.krad.bo.PersistableBusinessObject)
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;
		success &= super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
		line.refreshNonUpdateableReferences();

		if (line instanceof IndirectCostRecoveryAccountChange) {
			IndirectCostRecoveryAccountChange account = (IndirectCostRecoveryAccountChange) line;
			success &= validateIndirectCostRecoveryAccount(account);
		}

		return success;
	}
	
	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomSaveDocumentBusinessRules(document);
		GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts = (GlobalObjectWithIndirectCostRecoveryAccounts) getNewBo();
		
		if( ObjectUtils.isNotNull(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts()) && globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts().size() >0){
			validateIndirectCostRecoveryAccounts(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts());
		}
		return success;
	}

	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomRouteDocumentBusinessRules(document);
		GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts = (GlobalObjectWithIndirectCostRecoveryAccounts)  getNewBo();
		
		if( ObjectUtils.isNotNull(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts()) && globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts().size() >0){
			success &= validateIndirectCostRecoveryAccounts(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts());
			if(success){
				success &= checkICRAccountsTotalDistributionIs100PercentOnAllDetails(globalObjectWithIndirectCostRecoveryAccounts);
			}
		}
		
		return success;
	}
	
	/**
	 * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomApproveDocumentBusinessRules(document);
		GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts = (GlobalObjectWithIndirectCostRecoveryAccounts) getNewBo();
		
		if( ObjectUtils.isNotNull(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts()) && globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts().size() >0){
			success &= validateIndirectCostRecoveryAccounts(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts());
			if(success){
				success &= checkICRAccountsTotalDistributionIs100PercentOnAllDetails(globalObjectWithIndirectCostRecoveryAccounts);
			}
		}
		
		return success;
	}

	
	protected boolean validateIndirectCostRecoveryAccounts(List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts){
		boolean success = true;
		int index = 0;
        for (IndirectCostRecoveryAccountChange icrAccount : indirectCostRecoveryAccounts) {
            String errorPath = MAINTAINABLE_ERROR_PREFIX + KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS + "[" + index + "]";
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);
            success &= validateIndirectCostRecoveryAccount(icrAccount);             
            GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            index++;
        }
        return success;
	}


	protected boolean validateIndirectCostRecoveryAccount(IndirectCostRecoveryAccountChange icrAccount) {

		boolean success = true;

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

		if (icraAccountLinePercentage.compareTo(BigDecimal.ZERO) <= 0 || icraAccountLinePercentage.compareTo(ONE_HUNDRED_PERCENT) == 1) {
			GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ICR_ACCOUNT_LINE_PERCENT, KFSKeyConstants.ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_INVALID_LINE_PERCENT);
			success &= false;
		}

		return success;
	}


	protected boolean checkICRAccountsTotalDistributionIs100PercentOnAllDetails(GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts) {
		boolean result = true;

		if (!globalObjectWithIndirectCostRecoveryAccounts.hasIcrAccounts()) {
			return true;
		} else {

			for (GlobalBusinessObjectDetailBase globalDetail : globalObjectWithIndirectCostRecoveryAccounts.getGlobalObjectDetailsAndIcrAccountsMap().keySet()) {
				if (!checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts(), globalObjectWithIndirectCostRecoveryAccounts.getGlobalObjectDetailsAndIcrAccountsMap().get(globalDetail), globalDetail, globalObjectWithIndirectCostRecoveryAccounts)) {
					putFieldError(globalObjectWithIndirectCostRecoveryAccounts.getGlobalDetailsPropertyName(), CUKFSKeyConstants.ERROR_DOCUMENT_GLB_MAINT_ICR_ACCOUNT_TOTAL_NOT_100_PERCENT, new String[] { buildMessageFromPrimaryKey(globalDetail) });
					result = false;
				}
			}
		}

		return result;
	}
	

	protected boolean checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(List<IndirectCostRecoveryAccountChange> icrUpdates, List<IndirectCostRecoveryAccount> existingICRs, GlobalBusinessObjectDetailBase globalDetail, GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts) {
		List<IndirectCostRecoveryAccount> currentActiveIndirectCostRecoveryAccountList = buildCurrentActiveICRAccountsAfterICRUpdates(icrUpdates, existingICRs, globalDetail, globalObjectWithIndirectCostRecoveryAccounts);

		if ((ObjectUtils.isNull(currentActiveIndirectCostRecoveryAccountList) || (currentActiveIndirectCostRecoveryAccountList.size() == 0))) {
			return true;
		} else {

			if (totalDistributionOnUpdatedActiveICRsIs100Percent(currentActiveIndirectCostRecoveryAccountList)) {
				return true;
			} else {
				return false;
			}
		}
	}
	

	protected boolean totalDistributionOnUpdatedActiveICRsIs100Percent(List<IndirectCostRecoveryAccount> currentActiveIndirectCostRecoveryAccountList) {
		BigDecimal totalDistribution =  computeTotalDistribution(currentActiveIndirectCostRecoveryAccountList);
		if (totalDistribution.compareTo(ONE_HUNDRED_PERCENT) == 0) {
			return true;
		}
		else{
			return false;
		}
	}
	

	protected BigDecimal computeTotalDistribution(List<IndirectCostRecoveryAccount> currentActiveIndirectCostRecoveryAccountList) {
		BigDecimal totalDistribution = BigDecimal.ZERO;
		if (ObjectUtils.isNotNull(currentActiveIndirectCostRecoveryAccountList) && (currentActiveIndirectCostRecoveryAccountList.size() > 0)) {
			for (IndirectCostRecoveryAccount icra : currentActiveIndirectCostRecoveryAccountList) {
				totalDistribution = totalDistribution.add(icra.getAccountLinePercent());
			}
		}
		return totalDistribution;
	}
	

    protected List<IndirectCostRecoveryAccount> buildCurrentActiveICRAccountsAfterICRUpdates(List<IndirectCostRecoveryAccountChange> icrUpdates, List<IndirectCostRecoveryAccount> existingIcrAccounts, GlobalBusinessObjectDetailBase globalDetail, GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts){
    	List<IndirectCostRecoveryAccount> currentActiveIndirectCostRecoveryAccountList = new ArrayList<IndirectCostRecoveryAccount>();				
		List<IndirectCostRecoveryAccount> existingICRsToBeUpdated = new ArrayList<IndirectCostRecoveryAccount>(); 
		List<IndirectCostRecoveryAccount> icrAccountsNotUpdated = new ArrayList<IndirectCostRecoveryAccount>();

		for (IndirectCostRecoveryAccount icrAccount : existingIcrAccounts) {
			icrAccountsNotUpdated.add(icrAccount);
		}
		
		List<IndirectCostRecoveryAccount> icrAccountsToBeUpdatedToActive = new ArrayList<IndirectCostRecoveryAccount>();
		List<IndirectCostRecoveryAccount> icrAccountsToBeAddedAsActive = new ArrayList<IndirectCostRecoveryAccount>();


		if (icrUpdates.size() > 0) {
			//for each new icr on account global doc search for a matching icr on account to be updated
			for (IndirectCostRecoveryAccountChange newICR : icrUpdates) {
				boolean found = false;
				existingICRsToBeUpdated.clear();
				for (IndirectCostRecoveryAccount icrAccount : icrAccountsNotUpdated) {
					existingICRsToBeUpdated.add(icrAccount);
				}
				
				IndirectCostRecoveryAccount tempIcrAccount = null;
				int i = 0;
				
				while(i < existingICRsToBeUpdated.size() && (!found || (found && ObjectUtils.isNotNull(tempIcrAccount))) ){
					IndirectCostRecoveryAccount existingICR = existingICRsToBeUpdated.get(i);
					
					if (newICR.matchesICRAccount(existingICR)) {
						
						found = true;
						if (newICR.isActive() == existingICR.isActive()) {
							// both have the same active indicator, save in
							// a temp and keep looking
							tempIcrAccount = existingICR;
						} else {
							// done stop looking
							if (newICR.isActive()) {
								
								// add to update active
								IndirectCostRecoveryAccount icrAccount =  globalObjectWithIndirectCostRecoveryAccounts.createIndirectCostRecoveryAccountFromChange(globalDetail, newICR);
								icrAccountsToBeUpdatedToActive.add(icrAccount);
							}
							
							icrAccountsNotUpdated.remove(existingICR);
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
						IndirectCostRecoveryAccount icrAccount = globalObjectWithIndirectCostRecoveryAccounts.createIndirectCostRecoveryAccountFromChange(globalDetail, newICR);
						icrAccountsToBeUpdatedToActive.add(icrAccount);
					} 

					icrAccountsNotUpdated.remove(tempIcrAccount);
				}

				if (!found) {
					// done stop looking
					if (newICR.isActive()) {
						// add to update active
						IndirectCostRecoveryAccount icrAccount = globalObjectWithIndirectCostRecoveryAccounts.createIndirectCostRecoveryAccountFromChange(globalDetail, newICR);
						icrAccountsToBeAddedAsActive.add(icrAccount);
					}
				}
			}
			
			if (icrAccountsToBeUpdatedToActive.size() > 0) {
				for (IndirectCostRecoveryAccount icrAccount : icrAccountsToBeUpdatedToActive) {
					currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
				}
			}

			if (icrAccountsToBeAddedAsActive.size() > 0) {
				for (IndirectCostRecoveryAccount icrAccount : icrAccountsToBeAddedAsActive) {
					currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
				}
			}

			if (ObjectUtils.isNotNull(icrAccountsNotUpdated) && !icrAccountsNotUpdated.isEmpty()) {
				for (IndirectCostRecoveryAccount icrAccount : icrAccountsNotUpdated) {
					if (icrAccount.isActive()) {
						currentActiveIndirectCostRecoveryAccountList.add(icrAccount);
					}
				}
			}
		}
		
		return  currentActiveIndirectCostRecoveryAccountList;
    }

	private String buildMessageFromPrimaryKey(GlobalBusinessObjectDetailBase detail){
		StringBuffer message = new StringBuffer();
		if(detail instanceof AccountGlobalDetail){
			AccountGlobalDetail accountGlobalDetail = (AccountGlobalDetail)detail;
			message.append(accountGlobalDetail.getChartOfAccountsCode());
			message.append("-");
			message.append(accountGlobalDetail.getAccountNumber());
		}
		
		if(detail instanceof SubAccountGlobalDetail){
			SubAccountGlobalDetail subAccountGlobalDetail = (SubAccountGlobalDetail)detail;
			message.append(subAccountGlobalDetail.getChartOfAccountsCode());
			message.append("-");
			message.append(subAccountGlobalDetail.getAccountNumber());
			message.append("-");
			message.append(subAccountGlobalDetail.getSubAccountNumber());
		}
		return message.toString();
	}	

	public SubFundGroupService getSubFundGroupService() {
		if (subFundGroupService == null) {
			subFundGroupService = SpringContext.getBean(SubFundGroupService.class);
		}
		return subFundGroupService;
	}

	public void setSubFundGroupService(SubFundGroupService subFundGroupService) {
		this.subFundGroupService = subFundGroupService;
	}

	protected String getDDAttributeLabel(String attribute) {
		return ddService.getAttributeLabel(IndirectCostRecoveryAccount.class, attribute);
	}

}
