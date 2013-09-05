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

/**
 * @author kwk43
 *
 */

@SuppressWarnings("deprecation")
public class CUAccountMaintainableImpl extends KualiAccountMaintainableImpl {

    private static final long serialVersionUID = 1L;
    private static final String SUB_FUND_GROUP_CODE = "subFundGroupCode";
    
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
}
