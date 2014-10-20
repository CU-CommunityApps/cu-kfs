/**
 * 
 */
package edu.cornell.kfs.coa.document;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.KualiAccountMaintainableImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADConstants;

/**
 * @author kwk43
 *
 */
@SuppressWarnings("deprecation")
public class CUAccountMaintainableImpl extends KualiAccountMaintainableImpl {

    private static final long serialVersionUID = 1L;
    private static final String SUB_FUND_GROUP_CODE = "subFundGroupCode";
    protected static final String INITIATOR_ACCOUNT_FYI_SPLIT_NODE = "InitiatorAccountFYISplit";
    
    @Override
    public void saveBusinessObject() {
        
        Account account = (Account) getBusinessObject();
        AccountExtendedAttribute aea = (AccountExtendedAttribute) (account.getExtension());
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        
        HashMap<String,String> keys = new HashMap<String,String>();
        keys.put("programCode", aea.getProgramCode());
        keys.put(SUB_FUND_GROUP_CODE,aea.getSubFundGroupCode());
        SubFundProgram sfp = (SubFundProgram) bos.findByPrimaryKey(SubFundProgram.class, keys);
        aea.setSubFundProgram(sfp);
        aea.setSubFundGroupCode(account.getSubFundGroupCode());
        
        keys = new HashMap<String,String>();
        keys.put("appropriationAccountNumber", aea.getAppropriationAccountNumber());
        keys.put(SUB_FUND_GROUP_CODE,aea.getSubFundGroupCode());
        AppropriationAccount aan = (AppropriationAccount) bos.findByPrimaryKey(AppropriationAccount.class, keys);
        aea.setAppropriationAccount(aan);
        
        super.saveBusinessObject();
    }

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String,String[]> parameters) {
        System.out.println("Inside processAfterEdit");
    }
    
    /**
     * Checks if the document should be routed to initiator for FYI when a new account was created.
     * 
     * @see org.kuali.kfs.sys.document.FinancialSystemMaintainable#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    protected boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(INITIATOR_ACCOUNT_FYI_SPLIT_NODE)) {
            return isNewAccount();
        }
        // this is not a node we recognize
        throw new UnsupportedOperationException("AccountMaintainableImpl.answerSplitNodeQuestion cannot answer split node question " + "for the node called('" + nodeName + "')");
    }

    /**
     * Checks if this is a newly created account.
     * 
     * @return true if new (NEW, COPY actions), false otherwise
     */
    private boolean isNewAccount() {
        boolean retVal = false;
        String maintAction = super.getMaintenanceAction();

        if ((maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) || (maintAction.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION))) {
            retVal = true;
        }

        return retVal;
    }
}
