/**
 * 
 */
package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.document.KualiAccountMaintainableImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;

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
        
        if (account.isClosed() && aea.getAccountClosedDate() == null) {
            aea.setAccountClosedDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
        } else if (!account.isClosed() && aea.getAccountClosedDate() != null) {
            aea.setAccountClosedDate(null);           
        }
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

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> maintenanceLocks = super.generateMaintenanceLocks();

        if (isClosingAccount()) {
            maintenanceLocks.addAll(SpringContext.getBean(AccountReversionTrickleDownInactivationService.class).generateTrickleDownMaintenanceLocks((Account) getBusinessObject(), getDocumentNumber()));
        }
        return maintenanceLocks;
    }

    /**
     * Overridden to force the old maintenance object to include the relevant ICR account sections
     * if the new object has them. This is necessary to work around a section size mismatch issue
     * on certain ACCT maintenance documents.
     * 
     * @see org.kuali.rice.kns.maintenance.KualiMaintainableImpl#getSections(org.kuali.rice.kns.document.MaintenanceDocument, Maintainable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List getSections(org.kuali.rice.kns.document.MaintenanceDocument document, Maintainable oldMaintainable) {
        // The special handling only applies to the old maintainable.
        if (this == document.getOldMaintainableObject()) {
            Account oldAccount = (Account) getDataObject();
            Account newAccount = (Account) document.getNewMaintainableObject().getDataObject();
            
            if (oldAccount.getIndirectCostRecoveryAccounts().size() < newAccount.getIndirectCostRecoveryAccounts().size()) {
                // If necessary, add ICR accounts on the old account to match the quantity on the new account.
                List sections;
                List<IndirectCostRecoveryAccount> oldIcrAccounts = oldAccount.getIndirectCostRecoveryAccounts();
                oldAccount.setIndirectCostRecoveryAccounts(new ArrayList<IndirectCostRecoveryAccount>());
                
                for (IndirectCostRecoveryAccount oldIcrAccount : oldIcrAccounts) {
                    oldAccount.getIndirectCostRecoveryAccounts().add((IndirectCostRecoveryAccount) ObjectUtils.deepCopy(oldIcrAccount));
                }
                for (int i = newAccount.getIndirectCostRecoveryAccounts().size() - oldAccount.getIndirectCostRecoveryAccounts().size() - 1; i >= 0; i--) {
                    oldAccount.getIndirectCostRecoveryAccounts().add(new IndirectCostRecoveryAccount());
                }
                
                // Generate the sections using the temporarily-overridden list.
                sections = super.getSections(document, oldMaintainable);
                oldAccount.setIndirectCostRecoveryAccounts(oldIcrAccounts);
                
                return sections;
            }
        }
        
        return super.getSections(document, oldMaintainable);
    }

}
