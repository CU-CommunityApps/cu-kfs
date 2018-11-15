package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.A21SubAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;

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
        return maintenanceLocks;
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

        return retval; 
    }

    /**
     * @see org.kuali.kfs.kns.maintenance.KualiGlobalMaintainableImpl#getPrimaryEditedBusinessObjectClass()
     */
    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return SubAccount.class;
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
