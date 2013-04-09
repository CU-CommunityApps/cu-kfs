/*
 * Copyright 2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.coa.document;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.service.A21SubAccountService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;

/**
 * This class...
 */
public class SubAccountMaintainableImpl extends FinancialSystemMaintainable {
	
	 private static final String REQUIRES_CG_APPROVAL_NODE = "RequiresCGResponsibilityApproval";  //KFSPTS-1740
	 private AccountService accountService;                       //KFSPTS-1740
	 private A21SubAccountService a21SubAccountService;           //KFSPTS-1740
	 

    /**
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#refresh(java.lang.String, java.util.Map,
     *      org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        super.refresh(refreshCaller, fieldValues, document);

        Person person = GlobalVariables.getUserSession().getPerson();
        MaintenanceDocumentRestrictions restrictions = getBusinessObjectAuthorizationService().getMaintenanceDocumentRestrictions(document, person);
        boolean canEdit = !restrictions.isHiddenSectionId(KFSConstants.SUB_ACCOUNT_EDIT_CG_ICR_SECTION_ID) && !restrictions.isReadOnlySectionId(KFSConstants.SUB_ACCOUNT_EDIT_CG_ICR_SECTION_ID);
        
        // after account lookup, refresh the CG ICR account fields
        if (StringUtils.equals(refreshCaller, "accountLookupable") && fieldValues.containsKey("document.newMaintainableObject.accountNumber") && canEdit) {
            SubAccount subAccount = (SubAccount) this.getBusinessObject();
            this.populateCGIcrFields(subAccount);
        }
    }

    // populate the CG ICR fields if any
    private void populateCGIcrFields(SubAccount subAccount) {
        A21SubAccount a21SubAccount = subAccount.getA21SubAccount();
        String chartOfAccountsCode = subAccount.getChartOfAccountsCode();
        String accountNumber = subAccount.getAccountNumber();
        
        if (ObjectUtils.isNotNull(a21SubAccount) && (!StringUtils.equals(chartOfAccountsCode, a21SubAccount.getChartOfAccountsCode()) || !StringUtils.equals(accountNumber, a21SubAccount.getAccountNumber()))) {                  
            A21SubAccountService a21SubAccountService = SpringContext.getBean(A21SubAccountService.class);
            a21SubAccountService.populateCgIcrAccount(a21SubAccount, chartOfAccountsCode, accountNumber);
        }
    }
    
    /**
     * KFSPTS-1740 : New split node routing
     * 
     * Enforce routing to Award split-node based on account.contractsAndGrantsAccountResponsibilityId 
     * for specific business rule conditions. 
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemMaintainable#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        
    	if (nodeName.equals(REQUIRES_CG_APPROVAL_NODE)) {
    		return isCAndGReviewRequired();
    	}    	
    	//this is not a node we recognize
    	throw new UnsupportedOperationException("SubAccountMaintainableImpl.answerSplitNodeQuestion cannot answer split node question for the node called('" + nodeName + "')");
        
    }
    
    //KFSPTS-1740 added
    /**
     * Perform subAccount type code cost share check and CG ICR tab data change check 
     * and enforce route to Award node when either one returns true.
     * 
     * @return true when doc should route to Award node
     */
    private boolean isCAndGReviewRequired (){
    	if (isSubAccountTypeCodeCostShare() || hasCgIcrDataChanged()) {
    		return true;
    	}
    	return false;
    }
    
    //KFSPTS-1740 added
    /**
     * Answers true for the following conditions:
     * a) New or Copy of SubAccount with a CS SubAccount type enforce route to Award split-node.
     * b) Any Edit of an existing SubAccount type code to change it from-or-to a cost share enforces split-node route to Award (i.e. old=EX to new=CS OR old=CS to new=EX OR old=CS to new=CS).
     * 
     * @return true when code is CS for old or new value; otherwise return false
     */
    private boolean isSubAccountTypeCodeCostShare (){    	
    	String maintAction = super.getMaintenanceAction();    	
    	if ( (maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_NEW_ACTION)) || 
    		 (maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_COPY_ACTION)) ) {
    		     		
    		//need "new" bo for data comparisons
    		SubAccount subAccount = (SubAccount)super.getBusinessObject();
    		    		
    		if (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)) {    			
    			//retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is not a sub-account attribute and we must populate the account attribute on sub-account
    			subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
    			return true;
    		}
    	}  
    	else if ((maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_EDIT_ACTION)) ) {
    		
    		//need "new" bo for data comparisons
    		SubAccount subAccount = (SubAccount)super.getBusinessObject();
    		//need "old" bo for data comparisons
    		A21SubAccount oldSubAccount = this.getA21SubAccountService().getByPrimaryKey(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber(), subAccount.getSubAccountNumber());
    		
    		if ( (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)) || 
    			 (oldSubAccount.getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)) ) {
    			//retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is not a sub-account attribute and we must populate the account attribute on sub-account
    			subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
    			return true;    			
    		}
    	}
    	return false; 
    }
    
    //KFSPTS-1740 added
    /**
     * Answers true for the following conditions:
     * a) New or Copy of SubAccount with data existing in any field on the Edit CG ICR tab of the Sub-Account maintenance document will enforce the route the Award split-node.
     * b) Edit of the SubAccount where any data change is detected on the Edit CG ICR tab of the Sub-Account maintenance document will enforce the route the Award split-node. 
     * 
     * @return true when conditions are met; otherwise return false
     */
    private boolean hasCgIcrDataChanged() {
    	String maintAction = super.getMaintenanceAction();    	
    	if ( (maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_NEW_ACTION)) || 
    		 (maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_COPY_ACTION)) ) {
    		     		
    		//need "new" bo for data comparisons
    		SubAccount subAccount = (SubAccount)super.getBusinessObject();
    		
    		if (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.EXPENSE)) { 
    			//We need to route only when the ICR data the user is submitting does NOT match the ICR data on the account
    			
        		//"new" subAccount for data comparisons
        		A21SubAccount newSubAccount = subAccount.getA21SubAccount();
        		
        		//"existing" data that would have pre-populated
        		Account account = this.getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber());
        		
        		if (ObjectUtils.isNotNull(account) && ObjectUtils.isNotNull(newSubAccount)) {
	        		       		        		
	        		//compare each field in question
        			
	        		boolean hasIcrIdChanged = !areFieldValuesTheSame(account.getFinancialIcrSeriesIdentifier(), newSubAccount.getFinancialIcrSeriesIdentifier());
	        		boolean hasIcrCoaCodeChanged = !areFieldValuesTheSame(account.getIndirectCostRcvyFinCoaCode(), newSubAccount.getIndirectCostRecoveryChartOfAccountsCode());
	        		boolean hasIcrAcctNbrChanged = !areFieldValuesTheSame(account.getIndirectCostRecoveryAcctNbr(), newSubAccount.getIndirectCostRecoveryAccountNumber());
	        		boolean hasIcrTypeCodeChanged = !areFieldValuesTheSame(account.getAcctIndirectCostRcvyTypeCd(),newSubAccount.getIndirectCostRecoveryTypeCode());
        			
	        		//when both are true OR both are false, hasOffCampusIndChanged should be false = data did not change; otherwise when they are different hasOffCampusIndChanged should be true
	        		boolean hasOffCampusIndChanged = !((newSubAccount.getOffCampusCode() && account.isAccountOffCampusIndicator()) | (!newSubAccount.getOffCampusCode() && !account.isAccountOffCampusIndicator()));
	        		
	        		if ( hasIcrIdChanged | hasIcrCoaCodeChanged | hasIcrAcctNbrChanged | hasIcrTypeCodeChanged | hasOffCampusIndChanged) {
	        			//retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is not a sub-account attribute and we must populate the account attribute on sub-account
	        			subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
	        			return true;    			
	        		}
        		}
    		}
    	}  
    	else if ((maintAction.equalsIgnoreCase(KNSConstants.MAINTENANCE_EDIT_ACTION)) ) {
    		
    		//need "new" bo for data comparisons
    		SubAccount subAccount = (SubAccount)super.getBusinessObject();
    		//"new" subAccount for data comparisons
    		A21SubAccount newSubAccount = subAccount.getA21SubAccount();
    		//"old" subAccount for data comparisons
    		A21SubAccount oldSubAccount = this.getA21SubAccountService().getByPrimaryKey(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber(), subAccount.getSubAccountNumber());
    		//compare each field in question
    		boolean hasIcrIdChanged = isFieldValueChanged(newSubAccount.getFinancialIcrSeriesIdentifier(), oldSubAccount.getFinancialIcrSeriesIdentifier());
    		boolean hasIcrCoaCodeChanged = isFieldValueChanged(newSubAccount.getIndirectCostRecoveryChartOfAccountsCode(), oldSubAccount.getIndirectCostRecoveryChartOfAccountsCode());
    		boolean hasIcrAcctNbrChanged = isFieldValueChanged(newSubAccount.getIndirectCostRecoveryAccountNumber(), oldSubAccount.getIndirectCostRecoveryAccountNumber());
    		boolean hasIcrTypeCodeChanged = isFieldValueChanged(newSubAccount.getIndirectCostRecoveryTypeCode(), oldSubAccount.getIndirectCostRecoveryTypeCode());
    		//when both are true OR both are false, hasOffCampusIndChanged should be false = data did not change; otherwise when they are different hasOffCampusIndChanged should be true
    		boolean hasOffCampusIndChanged = !(newSubAccount.getOffCampusCode() && oldSubAccount.getOffCampusCode()) | (!newSubAccount.getOffCampusCode() && !oldSubAccount.getOffCampusCode());
    		
    		if ( hasIcrIdChanged | hasIcrCoaCodeChanged | hasIcrAcctNbrChanged | hasIcrTypeCodeChanged | hasOffCampusIndChanged) {
    			//retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is not a sub-account attribute and we must populate the account attribute on sub-account
    			subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
    			return true;    			
    		}
    	}
    	return false; 
    }
    
    //KFSPTS-1740 added
    /**
     * This compares two string values to see if the newValue is the same as the oldValue
     * 
     * @param oldValue - original value
     * @param newValue - new value
     * @return true if the two fields are the same
     */
    private boolean areFieldValuesTheSame(String oldValue, String newValue) {

    	if (StringUtils.isBlank(oldValue) && StringUtils.isBlank(newValue)) {
            return true;
        }

    	if (oldValue.equalsIgnoreCase(newValue)) {
            return true;
        }

    	//fields are different from each other
        return false;
    }
    
    
    //KFSPTS-1740 added
    /**
     * This compares two string values to see if the newValue has changed from the oldValue
     * 
     * @param oldValue - original value
     * @param newValue - new value
     * @return true if the two fields are different from each other
     */
    private boolean isFieldValueChanged(String oldValue, String newValue) {

        if (StringUtils.isBlank(oldValue) && StringUtils.isBlank(newValue)) {
            return false;
        }

        if (StringUtils.isBlank(oldValue) && StringUtils.isNotBlank(newValue)) {
            return true;
        }

        if (StringUtils.isNotBlank(oldValue) && StringUtils.isBlank(newValue)) {
            return true;
        }

        if (!oldValue.trim().equalsIgnoreCase(newValue.trim())) {
            return true;
        }

        return false;
    }
    

    //KFSPTS-1740
    public AccountService getAccountService() {
    	if (accountService == null) {
    		accountService = SpringContext.getBean(AccountService.class);    	
    	}
        return accountService;
    }
    
    
    //KFSPTS-1740
    public A21SubAccountService getA21SubAccountService() {
    	if (a21SubAccountService == null) {
    		a21SubAccountService = SpringContext.getBean(A21SubAccountService.class);    	
    	}
        return a21SubAccountService;
    }
    
}

