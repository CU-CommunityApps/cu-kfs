package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.COAKeyConstants;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryRateDetail;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;

import edu.cornell.kfs.coa.businessobject.A21SubAccountChange;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalNewAccountDetail;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class SubAccountGlobalRule extends GlobalIndirectCostRecoveryAccountsRule {

    protected SubAccountService subAccountService;

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
        
        checkNewSubAccountRules();
        
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
        
        success &= checkNewSubAccountRules();
        
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
        
        success &= checkNewSubAccountRules();
        
		return success;
	}
	
	 /**
	 * Checks that at least one sub account edit is entered if the new-sub-accounts section is empty.
	 * 
	 * @return true if at least one sub account entered, false otherwise
	 */
	public boolean checkSubAccountDetails() {
		boolean success = true;

		SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
		List<SubAccountGlobalDetail> details = newSubAccountGlobal.getSubAccountGlobalDetails();
		// check if there are any accounts
		if (details.size() == 0 && newSubAccountSectionHasEmptyValues(newSubAccountGlobal)) {
			putFieldError(KFSConstants.MAINTENANCE_ADD_PREFIX + CUKFSPropertyConstants.SUB_ACCOUNT_GLBL_CHANGE_DETAILS + KFSConstants.DELIMITER + KFSPropertyConstants.ACCOUNT_NUMBER, CUKFSKeyConstants.ERROR_DOCUMENT_GLOBAL_SUS_ACCOUNT_NO_SUB_ACCOUNTS);
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
            putGlobalError(COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_RPTCODE_ALL_FIELDS_IF_ANY_FIELDS);
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

                    if (!checkCgCostSharingIsEmpty()) {
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_CS_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
                        success = false;
                    }
                    
                    if (checkCgIcrIsEmpty() == false) {
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE_CODE, COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
                        success = false;
                    }
                    
                    if (newSubAccountGlobal.getIndirectCostRecoveryAccounts().isEmpty() == false) {
                        putFieldError(KFSPropertyConstants.INDIRECT_COST_RECOVERY_ACCOUNTS, COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID, new String[] { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(), getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() });
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
            success &= checkCgIcrRules(cgA21TypeCode);
        }

        return success;
    }
   
    
    /**
     * This method tests if all fields in the Cost Sharing section are empty.
     * 
     * @return true if the cost sharing values passed in are empty, otherwise false.
     */
    protected boolean checkCgCostSharingIsEmpty() {
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
        	success &= checkEmptyBOField(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER, a21.getCostShareSourceAccountNumber(), getDisplayNameForSubAccountProperty(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER));
        }
        
        if (!allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceAccountNumber())) {
        	success &= checkEmptyBOField(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, a21.getCostShareChartOfAccountCode(),  getDisplayNameForSubAccountProperty(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE));
        }

        a21.refreshReferenceObject(CUKFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT);
        // existence test on Cost Share Account
        if (allFieldsSet) {
            if (ObjectUtils.isNull(a21.getCostShareAccount())) {
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getDisplayNameForSubAccountProperty(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT_NUMBER));
                success &= false;
            }
        }
        
        a21.refreshReferenceObject(CUKFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT);
        // existence test on Cost Share SubAccount
        if (allFieldsSet && StringUtils.isNotBlank(a21.getCostShareSourceSubAccountNumber())) {
            if (ObjectUtils.isNull(a21.getCostShareSourceSubAccount())) {
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT_NUMBER, KFSKeyConstants.ERROR_EXISTENCE, getDisplayNameForSubAccountProperty(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_SUB_ACCOUNT_NUMBER));
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
    protected boolean checkCgIcrRules(String subAccountTypeCode) {
    	SubAccountGlobal newSubAccountGlobal = (SubAccountGlobal) super.getNewBo();
        A21SubAccountChange a21 = newSubAccountGlobal.getA21SubAccount();
        
        if(ObjectUtils.isNull(a21)) {
            return true;
        }

        boolean success = true;

        a21.refreshReferenceObject(KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE);
        if (StringUtils.isNotEmpty(a21.getIndirectCostRecoveryTypeCode())) {
            if (ObjectUtils.isNull(a21.getIndirectCostRecoveryType())) {
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.INDIRECT_COST_RECOVERY_TYPE_CODE,
                        KFSKeyConstants.ERROR_EXISTENCE, "ICR Type Code: " + a21.getIndirectCostRecoveryTypeCode());
                success = false;
            }
        }

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
                putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, KFSKeyConstants.ERROR_EXISTENCE, label + " (" + icrSeriesId + ")");
                success = false;
            }
            else {
                for(IndirectCostRecoveryRateDetail icrRateDetail : icrRateDetails) {
                    if(ObjectUtils.isNull(icrRateDetail.getIndirectCostRecoveryRate())){                                
                        putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.FINANCIAL_ICR_SERIES_IDENTIFIER, COAKeyConstants.ERROR_DOCUMENT_ICR_RATE_NOT_FOUND, new String[]{fiscalYear, icrSeriesId});
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
        if (!checkCgCostSharingIsEmpty()) {
            putFieldError(KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE, COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_COST_SHARE_SECTION_INVALID, subAccountTypeCode);

            success &= false;
        }

        return success;
    }  
    
    protected boolean checkNewSubAccountRules() {
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) super.getNewBo();
        
        if (newSubAccountSectionHasEmptyValues(subAccountGlobal)) {
            return true;
        }
        
        boolean newAccountFieldsAreEntered = checkAppropriateNewAccountFieldsAreEnteredBasedOnApplyAllCheckboxStatus(subAccountGlobal);
        boolean accountsExistAndAreOpen = checkAccountsExistAndAreOpen(subAccountGlobal);
        boolean subAccountTypeIsValid = checkSubAccountTypeIsValid(subAccountGlobal);
        boolean success = newAccountFieldsAreEntered && accountsExistAndAreOpen && subAccountTypeIsValid;
        success &= checkBasicGlobalFieldsAndEditSubAccountSectionAreEmpty(subAccountGlobal);
        if (accountsExistAndAreOpen) {
            if (newAccountFieldsAreEntered) {
                success &= checkDuplicateOrExistingSubAccountsAreNotPresent(subAccountGlobal);
            }
            if (subAccountTypeIsValid) {
                success &= checkContractsAndGrantsSetupForNonEmptyNewAccountsList(subAccountGlobal);
            }
        }
        
        return success;
    }
    
    protected boolean newSubAccountSectionHasEmptyValues(SubAccountGlobal subAccountGlobal) {
        return StringUtils.isBlank(subAccountGlobal.getNewSubAccountTypeCode())
                && StringUtils.isBlank(subAccountGlobal.getNewSubAccountName())
                && StringUtils.isBlank(subAccountGlobal.getNewSubAccountNumber())
                && !subAccountGlobal.isNewSubAccountOffCampusCode()
                && !subAccountGlobal.isApplyToAllNewSubAccounts()
                && subAccountGlobal.getSubAccountGlobalNewAccountDetails().isEmpty();
    }
    
    protected boolean checkBasicGlobalFieldsAndEditSubAccountSectionAreEmpty(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        if (StringUtils.isNotBlank(subAccountGlobal.getSubAccountName())) {
            putFieldErrorWithDisplayNameAddedToMessage(
                    KFSPropertyConstants.SUB_ACCOUNT_NAME, CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_INVALID_PROPERTY_FOR_NEW_SUB_ACCOUNT);
            success = false;
        }
        if (subAccountGlobal.isInactivate()) {
            putFieldErrorWithDisplayNameAddedToMessage(
                    CUKFSPropertyConstants.INACTIVATE, CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_INVALID_PROPERTY_FOR_NEW_SUB_ACCOUNT);
            success = false;
        }
        if (subAccountGlobal.getA21SubAccount().isOffCampusCode()) {
            putFieldErrorWithDisplayNameAddedToMessage(
                    KFSPropertyConstants.A21_SUB_ACCOUNT + KFSConstants.DELIMITER + KFSPropertyConstants.OFF_CAMPUS_CODE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_INVALID_PROPERTY_FOR_NEW_SUB_ACCOUNT);
            success = false;
        }
        if (!subAccountGlobal.getSubAccountGlobalDetails().isEmpty()) {
            putFieldError(CUKFSPropertyConstants.SUB_ACCOUNT_GLBL_CHANGE_DETAILS,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CANNOT_SPECIFY_CHANGES_AND_ADDITIONS);
            success = false;
        }
        return success;
    }
    
    protected boolean checkSubAccountTypeIsValid(SubAccountGlobal subAccountGlobal) {
        if (StringUtils.isBlank(subAccountGlobal.getNewSubAccountTypeCode())) {
            putFieldErrorWithDisplayNameAddedToMessage(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_TYPE_CODE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_FIELD_FOR_NEW_SUB_ACCOUNT);
            return false;
        } else if (!KFSConstants.SubAccountType.ELIGIBLE_SUB_ACCOUNT_TYPE_CODES.contains(subAccountGlobal.getNewSubAccountTypeCode())) {
            putFieldError(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_TYPE_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_INVALID_SUBACCOUNT_TYPE_CODES,
                    KFSConstants.SubAccountType.ELIGIBLE_SUB_ACCOUNT_TYPE_CODES.toString());
        }
        return true;
    }
    
    protected boolean checkAccountsExistAndAreOpen(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        int i = 0;
        for (SubAccountGlobalNewAccountDetail newAccountDetail : subAccountGlobal.getSubAccountGlobalNewAccountDetails()) {
            newAccountDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
            Account account = newAccountDetail.getAccount();
            if (ObjectUtils.isNull(account)) {
                String propertyName = buildListObjectPropertyPath(
                        CUKFSPropertyConstants.SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS, KFSPropertyConstants.ACCOUNT_NUMBER, i);
                putFieldError(propertyName, CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_ACCOUNT_FOR_NEW_SUB_ACCOUNT_NOT_FOUND,
                        new String[] {newAccountDetail.getChartOfAccountsCode(), newAccountDetail.getAccountNumber()});
                success = false;
            } else if (account.isClosed()) {
                String propertyName = buildListObjectPropertyPath(
                        CUKFSPropertyConstants.SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS, KFSPropertyConstants.ACCOUNT_NUMBER, i);
                putFieldError(propertyName, CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_ACCOUNT_FOR_NEW_SUB_ACCOUNT_CLOSED,
                        new String[] {newAccountDetail.getChartOfAccountsCode(), newAccountDetail.getAccountNumber()});
            }
            i++;
        }
        return success;
    }
    
    protected boolean checkAppropriateNewAccountFieldsAreEnteredBasedOnApplyAllCheckboxStatus(SubAccountGlobal subAccountGlobal) {
        if (subAccountGlobal.isApplyToAllNewSubAccounts()) {
            return checkCommonSubAccountFieldsAreEnteredAndIndividualizedOnesAreBlank(subAccountGlobal);
        } else {
            return checkCommonSubAccountFieldsAreBlankAndIndividualizedOnesAreEntered(subAccountGlobal);
        }
    }
    
    protected boolean checkCommonSubAccountFieldsAreEnteredAndIndividualizedOnesAreBlank(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        if (StringUtils.isBlank(subAccountGlobal.getNewSubAccountName())) {
            putGlobalFieldErrorForApplyAllCheckboxRulesFailure(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_NAME,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_GLOBAL_FIELD);
            success = false;
        }
        if (StringUtils.isBlank(subAccountGlobal.getNewSubAccountNumber())) {
            putGlobalFieldErrorForApplyAllCheckboxRulesFailure(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_NUMBER,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_GLOBAL_FIELD);
            success = false;
        }
        
        int i = 0;
        for (SubAccountGlobalNewAccountDetail newAccountDetail : subAccountGlobal.getSubAccountGlobalNewAccountDetails()) {
            if (StringUtils.isNotBlank(newAccountDetail.getSubAccountName())) {
                putLineFieldErrorForApplyAllCheckboxRulesFailure(KFSPropertyConstants.SUB_ACCOUNT_NAME,
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_LINE_FIELD, i);
                success = false;
            }
            if (StringUtils.isNotBlank(newAccountDetail.getSubAccountNumber())) {
                putLineFieldErrorForApplyAllCheckboxRulesFailure(KFSPropertyConstants.SUB_ACCOUNT_NUMBER,
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_LINE_FIELD, i);
                success = false;
            }
            if (newAccountDetail.isOffCampusCode()) {
                putLineFieldErrorForApplyAllCheckboxRulesFailure(KFSPropertyConstants.OFF_CAMPUS_CODE,
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_LINE_FIELD, i);
                success = false;
            }
            i++;
        }
        return success;
    }
    
    protected boolean checkCommonSubAccountFieldsAreBlankAndIndividualizedOnesAreEntered(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        if (StringUtils.isNotBlank(subAccountGlobal.getNewSubAccountName())) {
            putGlobalFieldErrorForApplyAllCheckboxRulesFailure(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_NAME,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_GLOBAL_FIELD);
            success = false;
        }
        if (StringUtils.isNotBlank(subAccountGlobal.getNewSubAccountNumber())) {
            putGlobalFieldErrorForApplyAllCheckboxRulesFailure(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_NUMBER,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_GLOBAL_FIELD);
            success = false;
        }
        if (subAccountGlobal.isNewSubAccountOffCampusCode()) {
            putGlobalFieldErrorForApplyAllCheckboxRulesFailure(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_OFF_CAMPUS_CODE,
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_GLOBAL_FIELD);
            success = false;
        }
        
        int i = 0;
        for (SubAccountGlobalNewAccountDetail newAccountDetail : subAccountGlobal.getSubAccountGlobalNewAccountDetails()) {
            if (StringUtils.isBlank(newAccountDetail.getSubAccountName())) {
                putLineFieldErrorForApplyAllCheckboxRulesFailure(KFSPropertyConstants.SUB_ACCOUNT_NAME,
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_LINE_FIELD, i);
                success = false;
            }
            if (StringUtils.isBlank(newAccountDetail.getSubAccountNumber())) {
                putLineFieldErrorForApplyAllCheckboxRulesFailure(KFSPropertyConstants.SUB_ACCOUNT_NUMBER,
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_LINE_FIELD, i);
                success = false;
            }
            i++;
        }
        return success;
    }
    
    protected void putGlobalFieldErrorForApplyAllCheckboxRulesFailure(String globalPropertyName, String errorConstant) {
        String propertyDisplayName = getDisplayNameForGlobalObjectProperty(globalPropertyName);
        putFieldErrorForApplyAllCheckboxRulesFailure(globalPropertyName, propertyDisplayName, errorConstant);
    }
    
    protected void putLineFieldErrorForApplyAllCheckboxRulesFailure(String linePropertyName, String errorConstant, int index) {
        String fullPropertyName = buildListObjectPropertyPath(
                CUKFSPropertyConstants.SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS, linePropertyName, index);
        String propertyDisplayName = getDisplayNameForObjectProperty(SubAccountGlobalNewAccountDetail.class, linePropertyName);
        putFieldErrorForApplyAllCheckboxRulesFailure(fullPropertyName, propertyDisplayName, errorConstant);
    }
    
    protected void putFieldErrorForApplyAllCheckboxRulesFailure(String propertyName, String propertyDisplayName, String errorConstant) {
        String checkboxDisplayName = getDisplayNameForGlobalObjectProperty(CUKFSPropertyConstants.APPLY_TO_ALL_NEW_SUB_ACCOUNTS);
        putFieldError(propertyName, errorConstant, new String[] {propertyDisplayName, checkboxDisplayName});
    }
    
    protected boolean checkDuplicateOrExistingSubAccountsAreNotPresent(SubAccountGlobal subAccountGlobal) {
        String commonSubAccountNumber = subAccountGlobal.getNewSubAccountNumber();
        String subAccountKeyFormat = "%s-%s-%s";
        Set<String> subAccountKeys = new HashSet<>();
        boolean success = true;
        
        int i = 0;
        for (SubAccountGlobalNewAccountDetail newAccountDetail: subAccountGlobal.getSubAccountGlobalNewAccountDetails()) {
            String subAccountNumber = subAccountGlobal.isApplyToAllNewSubAccounts()
                    ? commonSubAccountNumber : newAccountDetail.getSubAccountNumber();
            String subAccountKey = String.format(subAccountKeyFormat, newAccountDetail.getChartOfAccountsCode(),
                    newAccountDetail.getAccountNumber(), subAccountNumber);
            if (!subAccountKeys.add(subAccountKey)) {
                putFieldErrorForDuplicateOrExistingSubAccount(
                        CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_DUPLICATE, subAccountKey, i);
                success = false;
            } else {
                success &= checkSubAccountDoesNotExist(newAccountDetail, subAccountNumber, subAccountKey, i);
            }
            i++;
        }
        
        return success;
    }
    
    protected boolean checkSubAccountDoesNotExist(
            SubAccountGlobalNewAccountDetail newAccountDetail, String subAccountNumber, String subAccountKey, int index) {
        SubAccount subAccount = getSubAccountService().getByPrimaryId(
                newAccountDetail.getChartOfAccountsCode(), newAccountDetail.getAccountNumber(), subAccountNumber);
        if (ObjectUtils.isNotNull(subAccount)) {
            putFieldErrorForDuplicateOrExistingSubAccount(
                    CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_EXISTS, subAccountKey, index);
            return false;
        }
        return true;
    }
    
    protected void putFieldErrorForDuplicateOrExistingSubAccount(String errorConstant, String subAccountKey, int index) {
        String fullPropertyName = buildListObjectPropertyPath(
                CUKFSPropertyConstants.SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS, KFSPropertyConstants.SUB_ACCOUNT_NUMBER, index);
        putFieldError(fullPropertyName, errorConstant, subAccountKey);
    }
    
    protected boolean checkContractsAndGrantsSetupForNonEmptyNewAccountsList(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        List<SubAccountGlobalNewAccountDetail> newAccountDetails = subAccountGlobal.getSubAccountGlobalNewAccountDetails();
        int numberOfAccountsForContractsAndGrants = (int) newAccountDetails.stream()
                .filter(this::accountIsForContractsAndGrants)
                .count();
        boolean allAccountsAreForContractsAndGrants = (numberOfAccountsForContractsAndGrants == newAccountDetails.size());
        boolean noneOfAccountsAreForContractsAndGrants = (numberOfAccountsForContractsAndGrants == 0);
        
        if (allAccountsAreForContractsAndGrants) {
            if (StringUtils.equals(KFSConstants.SubAccountType.COST_SHARE, subAccountGlobal.getNewSubAccountTypeCode())) {
                success &= checkCgCostSharingForNewAccounts(subAccountGlobal);
            } else if (StringUtils.equals(KFSConstants.SubAccountType.EXPENSE, subAccountGlobal.getNewSubAccountTypeCode())) {
                success &= checkIndirectCostRecoveryFieldsHaveValuesForCgExpenseAccounts(subAccountGlobal);
                success &= checkCgIcrRules(subAccountGlobal.getNewSubAccountTypeCode());
            }
        } else if (noneOfAccountsAreForContractsAndGrants) {
            success &= checkCgFieldsAreEmptyForNonCgAccounts(subAccountGlobal);
            success &= checkSubAccountTypeIsValidForNonCgAccounts(subAccountGlobal);
        } else {
            putGlobalError(CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CG_AND_NON_CG_MIX);
            success = false;
        }
        
        return success;
    }
    
    protected boolean checkCgCostSharingForNewAccounts(SubAccountGlobal subAccountGlobal) {
        A21SubAccountChange a21SubAccount = subAccountGlobal.getA21SubAccount();
        boolean success = checkCostShareSourceChartAndAccountAreFilledIn(a21SubAccount);
        if (success) {
            success &= checkCgCostSharingRules(subAccountGlobal.getNewSubAccountTypeCode());
        }
        success &= checkIcrFieldsAreEmptyForCostShareSubAccount(subAccountGlobal);
        return success;
    }
    
    protected boolean checkCostShareSourceChartAndAccountAreFilledIn(A21SubAccountChange a21SubAccount) {
        boolean success = true;
        success &= checkEmptyBOField(
                CUKFSPropertyConstants.A21_COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE,
                a21SubAccount.getCostShareChartOfAccountCode());
        success &= checkEmptyBOField(
                CUKFSPropertyConstants.A21_COST_SHARE_SOURCE_ACCOUNT_NUMBER,
                a21SubAccount.getCostShareSourceAccountNumber());
        return success;
    }
    
    
    protected boolean checkIcrFieldsAreEmptyForCostShareSubAccount(SubAccountGlobal subAccountGlobal) {
        if (!checkCgIcrIsEmpty() || !subAccountGlobal.getIndirectCostRecoveryAccounts().isEmpty()) {
            putFieldError(CUKFSPropertyConstants.A21_INDIRECT_COST_RECOVERY_TYPE_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_ICR_SECTION_INVALID, subAccountGlobal.getNewSubAccountTypeCode());
            return false;
        }
        return true;
    }
    
    protected boolean checkEmptyBOField(String propertyName, Object valueToTest) {
        return checkEmptyBOField(propertyName, valueToTest, getDisplayNameForSubAccountProperty(propertyName));
    }
    
    protected boolean accountIsForContractsAndGrants(SubAccountGlobalNewAccountDetail newAccountDetail) {
        newAccountDetail.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
        return accountIsForContractsAndGrants(newAccountDetail.getAccount());
    }
    
    protected boolean accountIsForContractsAndGrants(A21SubAccountChange a21SubAccount) {
        a21SubAccount.refreshReferenceObject(CUKFSPropertyConstants.COST_SHARE_SOURCE_ACCOUNT);
        return accountIsForContractsAndGrants(a21SubAccount.getCostShareAccount());
    }
    
    protected boolean accountIsForContractsAndGrants(Account account) {
        if (ObjectUtils.isNotNull(account)) {
            account.refreshReferenceObject(KFSPropertyConstants.SUB_FUND_GROUP);
            SubFundGroup subFundGroup = account.getSubFundGroup();
            if (ObjectUtils.isNotNull(subFundGroup)) {
                return getSubFundGroupService().isForContractsAndGrants(subFundGroup);
            }
        }
        return false;
    }
    
    protected boolean checkIndirectCostRecoveryFieldsHaveValuesForCgExpenseAccounts(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        if (checkCgIcrIsEmpty()) {
            putGlobalError(CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CG_EXPENSE_REQUIRED_SECTION,
                    CUKFSConstants.SUB_ACCOUNT_GLOBAL_CG_ICR_SECTION_NAME);
            success = false;
        }
        if (subAccountGlobal.getIndirectCostRecoveryAccounts().isEmpty()) {
            putGlobalError(CUKFSKeyConstants.ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CG_EXPENSE_REQUIRED_SECTION,
                    CUKFSConstants.SUB_ACCOUNT_GLOBAL_CG_ICR_ACCOUNTS_SECTION_NAME);
            success = false;
        }
        return success;
    }
    
    protected boolean checkCgFieldsAreEmptyForNonCgAccounts(SubAccountGlobal subAccountGlobal) {
        boolean success = true;
        if (!checkCgCostSharingIsEmpty()) {
            putFieldErrorForMessageWithCgDenotingLabelAndValue(
                    CUKFSPropertyConstants.A21_COST_SHARE_SOURCE_CHART_OF_ACCOUNTS_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_CS_INVALID);
            success = false;
        }
        if (!checkCgIcrIsEmpty() || !subAccountGlobal.getIndirectCostRecoveryAccounts().isEmpty()) {
            putFieldErrorForMessageWithCgDenotingLabelAndValue(
                    CUKFSPropertyConstants.A21_INDIRECT_COST_RECOVERY_TYPE_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_ICR_INVALID);
            success = false;
        }
        return success;
    }
    
    protected boolean checkSubAccountTypeIsValidForNonCgAccounts(SubAccountGlobal subAccountGlobal) {
        if (!StringUtils.equals(KFSConstants.SubAccountType.EXPENSE, subAccountGlobal.getNewSubAccountTypeCode())) {
            putFieldErrorForMessageWithCgDenotingLabelAndValue(CUKFSPropertyConstants.NEW_SUB_ACCOUNT_TYPE_CODE,
                    COAKeyConstants.ERROR_DOCUMENT_SUBACCTMAINT_NON_FUNDED_ACCT_SUB_ACCT_TYPE_CODE_INVALID);
            return false;
        }
        return true;
    }
    
    protected void putFieldErrorWithDisplayNameAddedToMessage(String propertyName, String errorConstant) {
        putFieldError(propertyName, errorConstant, getDisplayNameForGlobalObjectProperty(propertyName));
    }
    
    protected void putFieldErrorForMessageWithCgDenotingLabelAndValue(String propertyName, String errorConstant) {
        String[] messageArgs = { getSubFundGroupService().getContractsAndGrantsDenotingAttributeLabel(),
                getSubFundGroupService().getContractsAndGrantsDenotingValueForMessage() };
        putFieldError(propertyName, errorConstant, messageArgs);
    }
    
    /**
     * Retrieves the label name for a specific property
     * 
     * @param propertyName - property to retrieve label for (from the DD)
     * @return the label
     */
    protected String getDisplayNameForSubAccountProperty(String propertyName) {
        return getDisplayNameForObjectProperty(SubAccount.class, propertyName);
    }
    
    protected String getDisplayNameForGlobalObjectProperty(String propertyName) {
        return getDisplayNameForObjectProperty(SubAccountGlobal.class, propertyName);
    }
    
    protected String getDisplayNameForObjectProperty(Class<?> boClass, String propertyName) {
        return getDdService().getAttributeLabel(boClass, propertyName);
    }
    
    protected String buildListObjectPropertyPath(String listPropertyName, String objectPropertyName, int index) {
        return String.format("%s[%d].%s", listPropertyName, index, objectPropertyName);
    }
    
    @Override
    protected String buildMessageFromPrimaryKey(GlobalBusinessObjectDetailBase detail) {
        if (detail instanceof SubAccountGlobalDetail) {
            return buildMessageFromPrimaryKeyForChangeDetail((SubAccountGlobalDetail) detail);
        } else if (detail instanceof SubAccountGlobalNewAccountDetail) {
            return buildMessageFromPrimaryKeyForNewDetail((SubAccountGlobalNewAccountDetail) detail);
        } else {
            return super.buildMessageFromPrimaryKey(detail);
        }
    }
    
    protected String buildMessageFromPrimaryKeyForChangeDetail(SubAccountGlobalDetail subAccountGlobalDetail) {
        StringBuilder message = new StringBuilder();
        message.append(subAccountGlobalDetail.getChartOfAccountsCode());
        message.append(KFSConstants.DASH);
        message.append(subAccountGlobalDetail.getAccountNumber());
        message.append(KFSConstants.DASH);
        message.append(subAccountGlobalDetail.getSubAccountNumber());
        return message.toString();
    }
    
    protected String buildMessageFromPrimaryKeyForNewDetail(SubAccountGlobalNewAccountDetail subAccountGlobalNewAccountDetail) {
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getNewBo();
        StringBuilder message = new StringBuilder();
        message.append(subAccountGlobalNewAccountDetail.getChartOfAccountsCode());
        message.append(KFSConstants.DASH);
        message.append(subAccountGlobalNewAccountDetail.getAccountNumber());
        message.append(KFSConstants.DASH);
        if (subAccountGlobal.isApplyToAllNewSubAccounts()) {
            message.append(subAccountGlobal.getNewSubAccountNumber());
        } else {
            message.append(subAccountGlobalNewAccountDetail.getSubAccountNumber());
        }
        return message.toString();
    }
    
    public SubAccountService getSubAccountService() {
        if (subAccountService == null) {
            subAccountService = SpringContext.getBean(SubAccountService.class);
        }
        return subAccountService;
    }
    
    public void setSubAccountService(SubAccountService subAccountService) {
        this.subAccountService = subAccountService;
    }
    
}
