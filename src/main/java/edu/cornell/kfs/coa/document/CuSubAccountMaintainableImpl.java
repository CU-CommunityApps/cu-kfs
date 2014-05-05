package edu.cornell.kfs.coa.document;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.document.SubAccountMaintainableImpl;
import org.kuali.kfs.coa.service.A21SubAccountService;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

@SuppressWarnings("deprecation")
public class CuSubAccountMaintainableImpl extends SubAccountMaintainableImpl {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuSubAccountMaintainableImpl.class);
    private static final long serialVersionUID = 1L;
    private static final String REQUIRES_CG_APPROVAL_NODE = "RequiresCGResponsibilityApproval";
    private AccountService accountService;          
    private A21SubAccountService a21SubAccountService;

    /**
     * KFSPTS-1740 : New split node routing
     * 
     * Enforce routing to Award split-node based on account.contractsAndGrantsAccountResponsibilityId 
     * for specific business rule conditions. 
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemMaintainable#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(String nodeName) {
        
        if (nodeName.equals(REQUIRES_CG_APPROVAL_NODE)) {
            return isCAndGReviewRequired();
        }       
        //this is not a node we recognize
        throw new UnsupportedOperationException(
                "SubAccountMaintainableImpl.answerSplitNodeQuestion cannot answer split node question "
                + "for the node called('" + nodeName + "')");
        
    }
    
    //KFSPTS-1740 added
    /**
     * Perform subAccount type code cost share check and CG ICR tab data change check 
     * and enforce route to Award node when either one returns true.
     * 
     * @return true when doc should route to Award node
     */
    private boolean isCAndGReviewRequired() {
        if ( isSubAccountTypeCodeCostShare() || hasCgIcrDataChanged() || hasIcrSectionChanged()) {
            return true;
        }
        return false;
    }
    
    //KFSPTS-1740 added
    /**
     * Answers true for the following conditions:
     * a) New or Copy of SubAccount with a CS SubAccount type enforce route to Award split-node.
     * b) Any Edit of an existing SubAccount type code to change it from-or-to a cost share enforces
     *  split-node route to Award (i.e. old=EX to new=CS OR old=CS to new=EX OR old=CS to new=CS).
     * 
     * @return true when code is CS for old or new value; otherwise return false
     */
    private boolean isSubAccountTypeCodeCostShare() {
        String maintAction = super.getMaintenanceAction();   
        boolean retval = false;
        
        if ((maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) 
            || (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION))) {
                        
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
                        
            if (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)) {             
                //retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is
                //not a sub-account attribute and we must populate the account attribute on sub-account
                subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
                retval = true;
            }
        } else if (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_EDIT_ACTION)) {
            
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
            //need "old" bo for data comparisons
            A21SubAccount oldSubAccount = this.getA21SubAccountService().getByPrimaryKey(
                    subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber(), 
                            subAccount.getSubAccountNumber());
            
            if ((subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE))
                    || (oldSubAccount.getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE))) {
                //retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is 
                // not a sub-account attribute and we must populate the account attribute on sub-account
                subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
                retval = true;                
            }
        }
        return retval; 
    }
    
    //KFSPTS-1740 added
    /**
     * Answers true for the following conditions:
     * a) New or Copy of SubAccount with data existing in any field on the Edit CG ICR tab of the Sub-Account 
     *      maintenance document will enforce the route the Award split-node.
     * b) Edit of the SubAccount where any data change is detected on the Edit CG ICR tab of the Sub-Account 
     *      maintenance document will enforce the route the Award split-node. 
     * 
     * @return true when conditions are met; otherwise return false
     */
    private boolean hasCgIcrDataChanged() {
        String maintAction = super.getMaintenanceAction();     
        boolean retval = false;
        
        if ((maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION))
             || (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION))) {
                        
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
            
            if (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.EXPENSE)) { 
                //We need to route only when the ICR data the user is submitting does NOT match the ICR data on the account
                
                //"new" subAccount for data comparisons
                A21SubAccount newSubAccount = subAccount.getA21SubAccount();
                
                //"existing" data that would have pre-populated
                Account account = this.getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber());
                
                if (ObjectUtils.isNotNull(account) && ObjectUtils.isNotNull(newSubAccount)) {
                                                
                    //compare each field in question    
                    boolean hasIcrIdChanged = !areFieldValuesTheSame(
                            account.getFinancialIcrSeriesIdentifier(), 
                            newSubAccount.getFinancialIcrSeriesIdentifier());
                    boolean hasIcrTypeCodeChanged = !areFieldValuesTheSame(
                            account.getAcctIndirectCostRcvyTypeCd(),
                            newSubAccount.getIndirectCostRecoveryTypeCode());
                    
                    boolean hasOffCampusIndChanged = newSubAccount.getOffCampusCode() != account.isAccountOffCampusIndicator();
                    
                    if (hasIcrIdChanged || hasIcrTypeCodeChanged || hasOffCampusIndChanged) {
                        //retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId 
                        //is not a sub-account attribute and we must populate the account attribute on sub-account
                        subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(
                                subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
                        retval = true;                
                    }
                }
            }
        } else if (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_EDIT_ACTION)) {
            
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
            //"new" subAccount for data comparisons
            A21SubAccount newSubAccount = subAccount.getA21SubAccount();
            //"old" subAccount for data comparisons
            A21SubAccount oldSubAccount = this.getA21SubAccountService().getByPrimaryKey(
                    subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber(), 
                    subAccount.getSubAccountNumber());
            //compare each field in question
            boolean hasIcrIdChanged = isFieldValueChanged(newSubAccount.getFinancialIcrSeriesIdentifier(), 
                    oldSubAccount.getFinancialIcrSeriesIdentifier());
            boolean hasIcrTypeCodeChanged = isFieldValueChanged(newSubAccount.getIndirectCostRecoveryTypeCode(), 
                    oldSubAccount.getIndirectCostRecoveryTypeCode());
            
            boolean hasOffCampusIndChanged = newSubAccount.getOffCampusCode() != oldSubAccount.getOffCampusCode();
            
            if (hasIcrIdChanged || hasIcrTypeCodeChanged || hasOffCampusIndChanged) {
                //retrieve data we need for split-node route to work, contractsAndGrantsAccountResponsibilityId is 
                //not a sub-account attribute and we must populate the account attribute on sub-account
                subAccount.setAccount(getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber()));
                retval = true;                
            }
        }
        return retval; 
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
        boolean retVal = false;
        
        //both fields have the same string value
        if (StringUtils.equalsIgnoreCase(
                StringUtils.trim(oldValue),
                StringUtils.trim(newValue))) {
            retVal = true;
        }

        //fields are different from each other
        return retVal;
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
        return !areFieldValuesTheSame(oldValue, newValue);
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
    
    // KFSUPGRADE-765 :  Route edits to indirect cost to CG Resp ID
    private boolean hasIcrSectionChanged() {
        String maintAction = super.getMaintenanceAction();     
        boolean retval = false;
        
        if ((maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION))
                || (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION))) {
                        
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
            if (subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.EXPENSE)) { 
                //We need to route only when the ICR data the user is submitting does NOT match the ICR data on the account
                
                //"new" subAccount for data comparisons
                A21SubAccount newSubAccount = subAccount.getA21SubAccount();
                
                //"existing" data that would have pre-populated
                Account account = this.getAccountService().getByPrimaryIdWithCaching(subAccount.getChartOfAccountsCode(), subAccount.getAccountNumber());
                
                if (ObjectUtils.isNotNull(account) && ObjectUtils.isNotNull(newSubAccount)) {
                    List<IndirectCostRecoveryAccount> acctIcr = account.getIndirectCostRecoveryAccounts();
                    List<A21IndirectCostRecoveryAccount> subAcctIcr = newSubAccount.getA21ActiveIndirectCostRecoveryAccounts();
                    if (CollectionUtils.isEmpty(subAcctIcr)) {
                        if (CollectionUtils.isEmpty(acctIcr)) {
                            retval = false;
                        } else {
                            retval = true;
                        }
                    } else {
                        if (CollectionUtils.isEmpty(acctIcr)) {
                            retval = true;
                        } else {
                            if (subAcctIcr.size() == acctIcr.size()) {
                                retval = isIcrSectionDataChanged(subAcctIcr, acctIcr);  
                            } else {
                                retval = true;
                            }
                        }
                    }
                                                                  
                }
            }
        } else if (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_EDIT_ACTION)) {
            
            //need "new" bo for data comparisons
            SubAccount subAccount = (SubAccount) super.getBusinessObject();
            //"new" subAccount for data comparisons
            A21SubAccount newSubAccount = subAccount.getA21SubAccount();
            if (ObjectUtils.isNotNull(newSubAccount)) {
                try {
                    MaintenanceDocument oldMaintDoc = (MaintenanceDocument) SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(getDocumentNumber());
                    A21SubAccount oldSubAccount = (A21SubAccount)((SubAccount)oldMaintDoc.getOldMaintainableObject().getDataObject()).getA21SubAccount();
                    retval = isIcrSectionChanged(newSubAccount, oldSubAccount);
                } catch (Exception e) {
                    LOG.error("caught exception while getting subaccount old maintainable -> documentService.getByDocumentHeaderId(" + getDocumentNumber() + "). ", e);
                
                }
            }
        }
        return retval; 
    }
    
    private boolean isIcrSectionChanged(A21SubAccount newSubAccount, A21SubAccount oldSubAccount) {
        boolean retval = false;
        if (oldSubAccount == null) {
            if (newSubAccount != null && !CollectionUtils.isEmpty(newSubAccount.getA21ActiveIndirectCostRecoveryAccounts())) {
                retval = true;
            }
        } else {
            if (CollectionUtils.isEmpty(oldSubAccount.getA21ActiveIndirectCostRecoveryAccounts())) {
                retval = !CollectionUtils.isEmpty(newSubAccount.getA21ActiveIndirectCostRecoveryAccounts());
            } else {
                if (newSubAccount.getA21ActiveIndirectCostRecoveryAccounts().size() == oldSubAccount.getA21ActiveIndirectCostRecoveryAccounts().size()) {
                    retval = isIcrSectionDataChanged(newSubAccount.getA21ActiveIndirectCostRecoveryAccounts(),  oldSubAccount.getA21ActiveIndirectCostRecoveryAccounts());
                } else {
                    retval = true;
                }
            }
        }
        return retval;
        
    }
    
    private boolean isIcrSectionDataChanged(List<A21IndirectCostRecoveryAccount> newIcrAccounts, List<? extends IndirectCostRecoveryAccount> oldIcrAccounts) {
        boolean retval = false;
        for (A21IndirectCostRecoveryAccount newIcrAccount : newIcrAccounts) {
            boolean icrAccountMatched = false;
            for (IndirectCostRecoveryAccount oldIcrAccount : oldIcrAccounts) {
                if (StringUtils.equals(newIcrAccount.getIndirectCostRecoveryFinCoaCode(), oldIcrAccount.getIndirectCostRecoveryFinCoaCode()) &&
                        StringUtils.equals(newIcrAccount.getIndirectCostRecoveryAccountNumber(), oldIcrAccount.getIndirectCostRecoveryAccountNumber()) &&
                        newIcrAccount.getAccountLinePercent().compareTo(oldIcrAccount.getAccountLinePercent()) == 0 &&
                        ((newIcrAccount.isActive() && oldIcrAccount.isActive()) || (!newIcrAccount.isActive() && !oldIcrAccount.isActive()))) {
                    icrAccountMatched = true;
                    break;
                }
            }
            if (!icrAccountMatched) {
                retval = true;
                break;
            }
        }
        return retval;
    }
}
