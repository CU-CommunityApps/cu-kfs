package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.A21SubAccountChange;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class SubAccountGlobalRule extends GlobalIndirectCostRecoveryAccountsRule {
	
	/**
	 * @see edu.cornell.kfs.coa.document.validation.impl.GlobalIndirectCostRecoveryAccountsRule#processCustomSaveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	protected boolean processCustomSaveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomSaveDocumentBusinessRules(document);
		
		// check sub account details
		checkSubAccountDetails();
		
        // check that the reporting fields are entered altogether or none at all
        checkForPartiallyEnteredReportingFields();

        // process CG rules if appropriate
        checkCgRules(document);
        
		return success;
	}
	
	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomRouteDocumentBusinessRules(document);
		
		// check sub account details
		success &= checkSubAccountDetails();
		
        // check that the reporting fields are entered altogether or none at all
        success &= checkForPartiallyEnteredReportingFields();

        // process CG rules if appropriate
        success &= checkCgRules(document);
        
		return success;
	}
	
	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomApproveDocumentBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument)
	 */
	@Override
	protected boolean processCustomApproveDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomApproveDocumentBusinessRules(document);
		// check sub account details
		success &= checkSubAccountDetails();
		
        // check that the reporting fields are entered altogether or none at all
        success &= checkForPartiallyEnteredReportingFields();

        // process CG rules if appropriate
        success &= checkCgRules(document);
        
		return success;
	}
	
	 /**
	 * Checks that at least one sub account is entered.
	 * 
	 * @return true if at least one sub account entered, false otherwise
	 */
	public boolean checkSubAccountDetails() {
		boolean success = true;

		SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
		List<SubAccountGlobalDetail> details = newSubAccountGlobal.getSubAccountGlobalDetails();
		// check if there are any accounts
		if (details.size() == 0) {
			putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + CUKFSPropertyConstants.SUB_ACCOUNT_GLBL_CHANGE_DETAILS + "." + KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUS_ACCOUNT_NO_SUB_ACCOUNTS);
			success = false;
		}
		return success;
	 }
	 
	
	/**
	 * @see org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomAddCollectionLineBusinessRules(org.kuali.kfs.kns.document.MaintenanceDocument, java.lang.String, org.kuali.kfs.krad.bo.PersistableBusinessObject)
	 */
	@Override
	public boolean processCustomAddCollectionLineBusinessRules(MaintenanceDocument document, String collectionName, PersistableBusinessObject line) {
		boolean success = true;
		success &= super.processCustomAddCollectionLineBusinessRules(document, collectionName, line);
		
		if (line instanceof SubAccountGlobalDetail) {
			SubAccountGlobalDetail detail = (SubAccountGlobalDetail) line;
			success &= checkSubAccountDetail(detail);
		}
		
		return success;
	}
	
    /**
     * Checks that sub account global detail fields are valid. Also checks that the chart, account, sub account are valid.
     * 
     * @param dtl
     * @return true if valid, false otherwise
     */
    public boolean checkSubAccountDetail(SubAccountGlobalDetail dtl) {
        boolean success = true;
        int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        getDictionaryValidationService().validateBusinessObject(dtl);
        if (StringUtils.isNotBlank(dtl.getAccountNumber()) && StringUtils.isNotBlank(dtl.getChartOfAccountsCode())) {
            dtl.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            if (ObjectUtils.isNull(dtl.getAccount())) {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_ACCOUNT, new String[] { dtl.getChartOfAccountsCode(), dtl.getAccountNumber() });
            }
            
            dtl.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
            if (ObjectUtils.isNull(dtl.getSubAccount())) {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_SUB_ACCOUNT, new String[] { dtl.getChartOfAccountsCode(), dtl.getAccountNumber(), dtl.getSubAccountNumber() });
            }
        }
        success &= GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;

        return success;
    }
    
    /**
     * Checks that the reporting fields are entered altogether or none at all
     * 
     * @return false if only one reporting field filled out and not all of them, true otherwise
     */
    protected boolean checkForPartiallyEnteredReportingFields() {
        boolean success = true;
        boolean allReportingFieldsEntered = false;
        boolean anyReportingFieldsEntered = false;
        SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();

        // set a flag if all three reporting fields are filled (this is separated just for readability)
        if (StringUtils.isNotEmpty(newSubAccountGlobal.getFinancialReportChartCode()) && StringUtils.isNotEmpty(newSubAccountGlobal.getFinReportOrganizationCode()) && StringUtils.isNotEmpty(newSubAccountGlobal.getFinancialReportingCode())) {
            allReportingFieldsEntered = true;
        }

        // set a flag if any of the three reporting fields are filled (this is separated just for readability)
        if (StringUtils.isNotEmpty(newSubAccountGlobal.getFinancialReportChartCode()) || StringUtils.isNotEmpty(newSubAccountGlobal.getFinReportOrganizationCode()) || StringUtils.isNotEmpty(newSubAccountGlobal.getFinancialReportingCode())) {
            anyReportingFieldsEntered = true;
        }

        // if any of the three reporting code fields are filled out, all three must be, or none
        if (anyReportingFieldsEntered && !allReportingFieldsEntered) {
            putGlobalError(KFSKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_RPTCODE_ALL_FIELDS_IF_ANY_FIELDS);
            success &= false;
        }

        return success;
    }
    
    /**
     * Checks CG rules.
     * 
     * @param document
     * @return true if valid, false otherwise
     */
    protected boolean checkCgRules(MaintenanceDocument document) {
    	boolean success = true;
    	SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
    	List<SubAccountGlobalDetail> subAccountGlobalDetails = newSubAccountGlobal.getSubAccountGlobalDetails();
    	
    	for(SubAccountGlobalDetail subAccountGlobalDetail : subAccountGlobalDetails){
    		success &= checkCgRules(newSubAccountGlobal, subAccountGlobalDetail );
    	}
    	return success;
    }
    
    /**
     * Checks to make sure that if cgAuthorized is false it succeeds immediately, otherwise it checks that all the information
     * for CG is correctly entered and identified including:
     * <ul>
     * <li>If the {@link SubFundGroup} isn't for Contracts and Grants then check to make sure that the cost share and ICR fields are
     * not empty</li>
     * <li>If it isn't a child of CG, then the SubAccount must be of type ICR</li>
     * </ul>
     * 
     * @param document
     * @return true if the user is not authorized to change CG fields, otherwise it checks the above conditions
     */
    protected boolean checkCgRules(SubAccountGlobal newSubAccountGlobal,  SubAccountGlobalDetail subAccountGlobalDetail) {

        boolean success = true;

        // short circuit if the parent account is NOT part of a CG fund group
        boolean a21SubAccountRefreshed = false;
        subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
        if (ObjectUtils.isNotNull(subAccountGlobalDetail.getAccount())) {
        	Account account = subAccountGlobalDetail.getAccount();
        	account.refreshReferenceObject(KFSPropertyConstants.SUB_FUND_GROUP); 
            if (ObjectUtils.isNotNull(account.getSubFundGroup())) {

                // compare them, exit if the account isn't for contracts and grants
                if (!getSubFundGroupService().isForContractsAndGrants(account.getSubFundGroup())) {

                    if (checkCgCostSharingIsEmpty(subAccountGlobalDetail) == false) {
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, KFSKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_CS_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
                        success = false;
                    }
                    
                    if (checkCgIcrIsEmpty() == false) {
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE_CODE, KFSKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
                        success = false;
                    }
                    
                    if (newSubAccountGlobal.getIndirectCostRecoveryAccounts().isEmpty() == false) {
                        putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, KFSKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
                        success = false;
                    }

                    return success;
                }
            }
        }

        subAccountGlobalDetail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
        if(ObjectUtils.isNull(subAccountGlobalDetail.getSubAccount())){
        	return success;
        }
        
        SubAccount subAccount = subAccountGlobalDetail.getSubAccount();
        subAccount.refreshReferenceObject(KFSPropertyConstants.A21_SUB_ACCOUNT);
        A21SubAccount a21 = subAccount.getA21SubAccount();
        
        // short circuit if there is no A21SubAccount object at all (ie, null)
        if (ObjectUtils.isNull(a21)) {
            return success;
        }

        // FROM HERE ON IN WE CAN ASSUME THERE IS A VALID A21 SUBACCOUNT OBJECT

        // since there is a ICR Collection Account object, change refresh to perform 
        // manually refresh the a21SubAccount object, as it wont have been
        // refreshed by the parent, as its updateable
        // though only refresh if we didn't refresh in the checks above
        
        if (!a21SubAccountRefreshed) {
            //preserve the ICRAccounts before refresh to prevent the list from dropping
            List<A21IndirectCostRecoveryAccount>icrAccounts =a21.getA21IndirectCostRecoveryAccounts(); 
            a21.refresh();
            a21.setA21IndirectCostRecoveryAccounts(icrAccounts);        
        }

        // get a convenience reference to this code
        String cgA21TypeCode = a21.getSubAccountTypeCode();

        // if this is a Cost Sharing SubAccount, run the Cost Sharing rules
        if (KFSConstants.SubAccountType.COST_SHARE.trim().equalsIgnoreCase(StringUtils.trim(cgA21TypeCode))) {
            success &= checkCgCostSharingRules(cgA21TypeCode);
        }

        // if this is an ICR subaccount, run the ICR rules
        if (KFSConstants.SubAccountType.EXPENSE.trim().equals(StringUtils.trim(cgA21TypeCode))) {
            success &= checkCgIcrRules(cgA21TypeCode, subAccountGlobalDetail);
        }

        return success;
    }
   
    
    /**
     * This method tests if all fields in the Cost Sharing section are empty.
     * 
     * @return true if the cost sharing values passed in are empty, otherwise false.
     */
    protected boolean checkCgCostSharingIsEmpty(SubAccountGlobalDetail subAccountGlobalDetail) {
        boolean success = true;
        SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
        A21SubAccountChange newA21SubAccount = newSubAccountGlobal.getA21SubAccount();
        
        boolean cgCostSharingEmptyOnGlobal = true;
        if (ObjectUtils.isNotNull(newA21SubAccount)) {
        	cgCostSharingEmptyOnGlobal &= StringUtils.isEmpty(newA21SubAccount.getCostShareChartOfAccountCode());
        	cgCostSharingEmptyOnGlobal &= StringUtils.isEmpty(newA21SubAccount.getCostShareSourceAccountNumber());
        	cgCostSharingEmptyOnGlobal &= StringUtils.isEmpty(newA21SubAccount.getCostShareSourceSubAccountNumber());
        }
        
        success &= cgCostSharingEmptyOnGlobal;

        return success;
    }
    
    /**
     * This method tests if all fields in the ICR section are empty.
     *
     * @return true if the ICR values passed in are empty, otherwise false.
     */
    protected boolean checkCgIcrIsEmpty() {
        boolean success = true;
        SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
        A21SubAccountChange newA21SubAccount = newSubAccountGlobal.getA21SubAccount();

        if (ObjectUtils.isNotNull(newA21SubAccount)) {
            success &= StringUtils.isEmpty(newA21SubAccount.getFinancialIcrSeriesIdentifier());
            success &= StringUtils.isEmpty(newA21SubAccount.getIndirectCostRecoveryTypeCode());
        }

        return success;
    }


    /**
     * This checks that if the cost share information is filled out that it is valid and exists, or if fields are missing (such as
     * the chart of accounts code and account number) an error is recorded
     * 
     * @return true if all cost share fields filled out correctly, false if the chart of accounts code and account number for cost
     *         share are missing
     */
    protected boolean checkCgCostSharingRules(String subAccountTypeCode) {

        boolean success = true;
        boolean allFieldsSet = false;
        
        SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();

        A21SubAccountChange a21 = newSubAccountGlobal.getA21SubAccount();

        // check to see if all required fields are set
        if (StringUtils.isNotEmpty(a21.getCostShareChartOfAccountCode()) && StringUtils.isNotEmpty(a21.getCostShareSourceAccountNumber())) {
            allFieldsSet = true;
        }

        // Cost Sharing COA Code and Cost Sharing Account Number are required
        if (!allFieldsSet && StringUtils.isNotBlank(a21.getCostShareChartOfAccountCode())) {
        	success &= checkEmptyBOField(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER, a21.getCostShareSourceAccountNumber(), getDisplayName(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER));
        }
        
        if (!allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceAccountNumber())) {
        	success &= checkEmptyBOField(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, a21.getCostShareChartOfAccountCode(),  getDisplayName(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE));
        }

        a21.refreshReferenceObject(KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT);
        // existence test on Cost Share Account
        if (allFieldsSet) {
            if (ObjectUtils.isNull(a21.getCostShareAccount())) {
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getDisplayName(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER));
                success &= false;
            }
        }
        
        a21.refreshReferenceObject(KFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT);
        // existence test on Cost Share SubAccount
        if (allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceSubAccountNumber())) {
            if (ObjectUtils.isNull(a21.getCostShareSourceSubAccount())) {
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getDisplayName(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT_NUMBER));
                success &= false;
            }
        }

        return success;
    }
    

    /**
     * This checks that if the ICR information is entered that it is valid for this fiscal year and that all of its fields are valid
     * as well (such as account)
     * 
     * @return true if the ICR information is filled in and it is valid
     */
    protected boolean checkCgIcrRules(String subAccountTypeCode, SubAccountGlobalDetail subAccountGlobalDetail) {
    	SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
        A21SubAccountChange a21 = newSubAccountGlobal.getA21SubAccount();
        
        if(ObjectUtils.isNull(a21)) {
            return true;
        }

        boolean success = true;

        // existence check for Financial Series ID
        if (StringUtils.isNotEmpty(a21.getFinancialIcrSeriesIdentifier())) {            
            String fiscalYear = StringUtils.EMPTY + SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
            String icrSeriesId = a21.getFinancialIcrSeriesIdentifier();
            
            Map<String, String> pkMap = new HashMap<String, String>();
            pkMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
            pkMap.put(KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, icrSeriesId);
            Collection<IndirectCostRecoveryRateDetail> icrRateDetails = getBoService().findMatching(IndirectCostRecoveryRateDetail.class, pkMap);
            
            if (ObjectUtils.isNull(icrRateDetails) || icrRateDetails.isEmpty()) {
                String label = SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(A21SubAccount.class, KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER);
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, KFSKeyConstants.ERROR_EXISTENCE, label + " (" + icrSeriesId + ")");
                success = false;
            }
            else {
                for(IndirectCostRecoveryRateDetail icrRateDetail : icrRateDetails) {
                    if(ObjectUtils.isNull(icrRateDetail.getIndirectCostRecoveryRate())){                                
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, KFSKeyConstants.IndirectCostRecovery.ERROR_DOCUMENT_ICR_RATE_NOT_FOUND, new String[]{fiscalYear, icrSeriesId});
                        success = false;
                        break;
                    }
                }
            }            
        }

        // existence check for ICR Account
        for (IndirectCostRecoveryAccountChange account : newSubAccountGlobal.getActiveIndirectCostRecoveryAccounts()){
            if (StringUtils.isNotBlank(account.getIndirectCostRecoveryAccountNumber())
                && StringUtils.isNotBlank(account.getIndirectCostRecoveryFinCoaCode())){
                if(ObjectUtils.isNull(account.getIndirectCostRecoveryAccount())){                                
                    putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, KFSKeyConstants.ERROR_EXISTENCE, "ICR Account: " + account.getIndirectCostRecoveryFinCoaCode() + "-" + account.getIndirectCostRecoveryAccountNumber());
                    success = false;
                    break;
                }
            }
        }

        // The cost sharing fields must be empty if the sub-account type code is for ICR
        if (checkCgCostSharingIsEmpty(subAccountGlobalDetail) == false) {
            putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + "." + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, KFSKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_COST_SHARE_SECTION_INVALID, subAccountTypeCode);

            success &= false;
        }

        return success;
    }  
    
    /**
     * Retrieves the label name for a specific property
     * 
     * @param propertyName - property to retrieve label for (from the DD)
     * @return the label
     */
    protected String getDisplayName(String propertyName) {
        return getDdService().getAttributeLabel(SubAccount.class, propertyName);
    }
}
