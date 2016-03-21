package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
import org.kuali.rice.krad.util.KRADConstants;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;

public class SubAccountGlobalMaintainableImpl extends FinancialSystemGlobalMaintainable {
	private static final String REQUIRES_CG_APPROVAL_NODE = "RequiresCGResponsibilityApproval";

    /**
     * This creates the particular locking representation for this global document.
     * 
     * @see org.kuali.rice.kns.maintenance.Maintainable#generateMaintenanceLocks()
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        SubAccountGlobal subAccountGlobal = (SubAccountGlobal) getBusinessObject();
        List<MaintenanceLock> maintenanceLocks = new ArrayList();

        for (SubAccountGlobalDetail detail : subAccountGlobal.getSubAccountGlobalDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            StringBuffer lockrep = new StringBuffer();

            lockrep.append(Account.class.getName() + KFSConstants.Maintenance.AFTER_CLASS_DELIM);
            lockrep.append("chartOfAccountsCode" + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getChartOfAccountsCode() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
            lockrep.append("accountNumber" + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getAccountNumber());
            lockrep.append("subAccountNumber" + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getSubAccountNumber());

            maintenanceLock.setDocumentNumber(subAccountGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(lockrep.toString());
            maintenanceLocks.add(maintenanceLock);
        }
        return maintenanceLocks;
    }
    
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
        	detail.refreshReferenceObject("subAccount");
        	SubAccount subAccount = detail.getSubAccount();
        	if(subAccount.getA21SubAccount().getSubAccountTypeCode().equals(KFSConstants.SubAccountType.COST_SHARE)){
        		retval = true;
        		subAccount.refreshReferenceObject("account");
        		break;
        	}
        }

        return retval; 
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return SubAccount.class;
    }
}
