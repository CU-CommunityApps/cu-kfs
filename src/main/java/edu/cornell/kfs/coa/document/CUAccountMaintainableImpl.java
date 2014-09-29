/**
 * 
 */
package edu.cornell.kfs.coa.document;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.document.KualiAccountMaintainableImpl;
import org.kuali.kfs.coa.service.SubAccountTrickleDownInactivationService;
import org.kuali.kfs.coa.service.SubObjectTrickleDownInactivationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
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
        boolean isClosingAccount = isClosingAccount();
        
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
        
        // trickle down Account Reversion inactivation
        if (isClosingAccount) {
            SpringContext.getBean(AccountReversionTrickleDownInactivationService.class).trickleDownInactivateAccountReversions((Account) getBusinessObject(), getDocumentNumber());
        }
    }

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String,String[]> parameters) {
        System.out.println("Inside processAfterEdit");
    }
    
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> maintenanceLocks = super.generateMaintenanceLocks();

        if (isClosingAccount()) {
            maintenanceLocks.addAll(SpringContext.getBean(AccountReversionTrickleDownInactivationService.class).generateTrickleDownMaintenanceLocks((Account) getBusinessObject(), getDocumentNumber()));
        }
        return maintenanceLocks;
    }
}
