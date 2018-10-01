package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.coa.businessobject.A21SubAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalNewAccountDetail;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

public class SubAccountGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {
	private static final String REQUIRES_CG_APPROVAL_NODE = "RequiresCGResponsibilityApproval";

    /**
     * This creates the particular locking representation for this global document.
     * 
     * @see org.kuali.kfs.kns.maintenance.Maintainable#generateMaintenanceLocks()
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getBusinessObject();
        List<MaintenanceLock> maintenanceLocks = new ArrayList();

        for (SubAccountGlobalDetail detail : subAccountGlobal.getSubAccountGlobalDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            StringBuffer lockrep = new StringBuffer();

            lockrep.append(Account.class.getName() + KFSConstants.Maintenance.AFTER_CLASS_DELIM);
            lockrep.append(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getChartOfAccountsCode() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
            lockrep.append(KFSPropertyConstants.ACCOUNT_NUMBER + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getAccountNumber());
            lockrep.append(KFSPropertyConstants.SUB_ACCOUNT_NUMBER + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getSubAccountNumber());

            maintenanceLock.setDocumentNumber(subAccountGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(lockrep.toString());
            maintenanceLocks.add(maintenanceLock);
        }
        
        for (SubAccountGlobalNewAccountDetail newAccountDetail : subAccountGlobal.getSubAccountGlobalNewAccountDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            maintenanceLock.setDocumentNumber(subAccountGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(buildLockRepForNewSubAccount(subAccountGlobal, newAccountDetail));
            maintenanceLocks.add(maintenanceLock);
        }
        
        return maintenanceLocks;
    }
    
    protected String buildLockRepForNewSubAccount(SubAccountGlobal subAccountGlobal, SubAccountGlobalNewAccountDetail newAccountDetail) {
        String subAccountNumber = subAccountGlobal.isApplyToAllNewSubAccounts()
                ? subAccountGlobal.getNewSubAccountNumber() : newAccountDetail.getSubAccountNumber();
        StringBuilder lockrep = new StringBuilder();
        lockrep.append(Account.class.getName() + KFSConstants.Maintenance.AFTER_CLASS_DELIM);
        lockrep.append(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
        lockrep.append(newAccountDetail.getChartOfAccountsCode() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
        lockrep.append(KFSPropertyConstants.ACCOUNT_NUMBER + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
        lockrep.append(newAccountDetail.getAccountNumber() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
        lockrep.append(KFSPropertyConstants.SUB_ACCOUNT_NUMBER + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
        lockrep.append(subAccountNumber + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
        return lockrep.toString();
    }
    
    /**
     * @see org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable#answerSplitNodeQuestion(java.lang.String)
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
    
    private boolean isCAndGReviewRequired() {
        if ( isSubAccountTypeCodeCostShare()) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @return true when code is CS; otherwise return false
     */
    private boolean isSubAccountTypeCodeCostShare() { 
        boolean retval = false;
        
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getBusinessObject();

        for (SubAccountGlobalDetail detail : subAccountGlobal.getSubAccountGlobalDetails()) {
        	detail.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNT);
        	SubAccount subAccount = detail.getSubAccount();
        	if(subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)){
        		retval = true;
        		subAccount.refreshReferenceObject(KFSPropertyConstants.ACCOUNT);
        		break;
        	}
        }

        if (StringUtils.equals(KFSConstants.SubAccountType.COST_SHARE, subAccountGlobal.getNewSubAccountTypeCode())) {
            retval = true;
        }

        return retval; 
    }

    /**
     * @see org.kuali.kfs.kns.maintenance.KualiGlobalMaintainableImpl#getPrimaryEditedBusinessObjectClass()
     */
    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return SubAccount.class;
    }
    
    @Override
    public void addNewLineToCollection(String collectionName) {
        super.addNewLineToCollection(collectionName);
        if (isNewAccountDetailsCollection(collectionName)) {
            SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getBusinessObject();
            int newItemIndex = subAccountGlobal.getSubAccountGlobalNewAccountDetails().size() - 1;
            setSequenceNumbersOnNewAccountDetails(subAccountGlobal, newItemIndex);
        }
    }
    
    @Override
    public void addMultipleValueLookupResults(MaintenanceDocument document, String collectionName,
            Collection<PersistableBusinessObject> rawValues, boolean needsBlank, PersistableBusinessObject bo) {
        if (isNewAccountDetailsCollection(collectionName)) {
            SubAccountGlobal subAccountGlobal = (SubAccountGlobal) bo;
            int oldCollectionSizeAsStartIndex = subAccountGlobal.getSubAccountGlobalNewAccountDetails().size();
            super.addMultipleValueLookupResults(document, collectionName, rawValues, needsBlank, bo);
            setSequenceNumbersOnNewAccountDetails(subAccountGlobal, oldCollectionSizeAsStartIndex);
        } else {
            super.addMultipleValueLookupResults(document, collectionName, rawValues, needsBlank, bo);
        }
    }
    
    private void setSequenceNumbersOnNewAccountDetails(SubAccountGlobal subAccountGlobal, int newItemsStartIndex) {
        List<SubAccountGlobalNewAccountDetail> newAccountDetails = subAccountGlobal.getSubAccountGlobalNewAccountDetails();
        if (newItemsStartIndex >= newAccountDetails.size()) {
            return;
        }
        
        long nextSequenceNumber = subAccountGlobal.getNextNewAccountDetailSequenceNumber().longValue();
        for (int i = newItemsStartIndex; i < newAccountDetails.size(); i++) {
            SubAccountGlobalNewAccountDetail newAccountDetail = newAccountDetails.get(i);
            newAccountDetail.setSequenceNumber(Long.valueOf(nextSequenceNumber));
            nextSequenceNumber++;
        }
        
        subAccountGlobal.setNextNewAccountDetailSequenceNumber(Long.valueOf(nextSequenceNumber));
    }
    
    private boolean isNewAccountDetailsCollection(String collectionName) {
        return StringUtils.equals(CUKFSPropertyConstants.SUB_ACCOUNT_GLOBAL_NEW_ACCOUNT_DETAILS, collectionName);
    }
    
	/**
	 * Overriden to set the document number on the a21SubAccount.
	 * 
	 * @see org.kuali.kfs.kns.maintenance.KualiGlobalMaintainableImpl#prepareGlobalsForSave()
	 */
	@Override
	protected void prepareGlobalsForSave() {
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) businessObject;
		super.prepareGlobalsForSave();

		if (businessObject != null) {
			A21SubAccountChange a21SubAccount = subAccountGlobal.getA21SubAccount();
			if (a21SubAccount != null) {
				a21SubAccount.setDocumentNumber(getDocumentNumber());
			}
		}
	}
}
