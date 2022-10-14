package edu.cornell.kfs.coa.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.businessobject.AccountDelegateGlobal;
import org.kuali.kfs.coa.businessobject.AccountDelegateGlobalDetail;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;

/**
 * This class simply acts as a container to hold the List of Delegate Changes and the list of Account entries, for the Global
 * Delegate Change Document.
 */
public class CuAccountDelegateGlobal extends AccountDelegateGlobal implements GlobalBusinessObject {
    
    /**
     * @see org.kuali.kfs.krad.document.GlobalBusinessObject#applyGlobalChanges(org.kuali.kfs.krad.bo.BusinessObject)
     */
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {

        BusinessObjectService boService = SpringContext.getBean(BusinessObjectService.class);
        List<AccountDelegate> persistables = new ArrayList();

        List<AccountDelegateGlobalDetail> changeDocuments = this.getDelegateGlobals();
        List<AccountGlobalDetail> accountDetails = this.getAccountGlobalDetails();

        for (AccountDelegateGlobalDetail changeDocument : changeDocuments) {
            for (AccountGlobalDetail accountDetail : accountDetails) {

                Account account = (Account) boService.findByPrimaryKey(Account.class, accountDetail.getPrimaryKeys());

                // if the account doesn't exist, fail fast, as this should never happen, the business rules for this
                // document should have caught this.
                if (account == null) {
                    throw new RuntimeException("Account [" + accountDetail.getChartOfAccountsCode() + "-" + accountDetail.getAccountNumber() 
                          + "] was not present in the database. " + "This should never happen under normal circumstances, as an invalid account should have "
                          + "been caught by the Business Rules infrastructure.");
                }

                // attempt to load the existing Delegate from the DB, if it exists. we do this to avoid
                // versionNumber conflicts if we tried to just insert a new record that already existed.
                // chartOfAccountsCode & accountNumber
                Map<String, Object> pkMap = new HashMap<>(accountDetail.getPrimaryKeys());
                pkMap.put("financialDocumentTypeCode", changeDocument.getFinancialDocumentTypeCode());
                pkMap.put("accountDelegateSystemId", changeDocument.getAccountDelegateUniversalId());
                AccountDelegate delegate = (AccountDelegate) boService.findByPrimaryKey(AccountDelegate.class, pkMap);

                // if there is no existing Delegate with these primary keys, then we're creating a new one,
                // so lets populate it with the primary keys
                if (delegate == null)  {
                    delegate = new AccountDelegate();
                    delegate.setChartOfAccountsCode(accountDetail.getChartOfAccountsCode());
                    delegate.setAccountNumber(accountDetail.getAccountNumber());
                    delegate.setAccountDelegateSystemId(changeDocument.getAccountDelegateUniversalId());
                    delegate.setFinancialDocumentTypeCode(changeDocument.getFinancialDocumentTypeCode());
                    delegate.setActive(true);
                } else {
                    delegate.setActive(true);
                }
                
                //  APPROVAL FROM AMOUNT
                delegate.setFinDocApprovalFromThisAmt(changeDocument.getApprovalFromThisAmount());
                //  APPROVAL TO AMOUNT
                delegate.setFinDocApprovalToThisAmount(changeDocument.getApprovalToThisAmount());
 

                // PRIMARY ROUTING
                delegate.setAccountsDelegatePrmrtIndicator(changeDocument.getAccountDelegatePrimaryRoutingIndicator());

                // START DATE
                if (changeDocument.getAccountDelegateStartDate() != null) {
                    delegate.setAccountDelegateStartDate(new Date(changeDocument.getAccountDelegateStartDate()
                            .getTime()));
                }

                persistables.add(delegate);

            }
        }

        return new ArrayList<PersistableBusinessObject>(persistables);
    }
    
}
