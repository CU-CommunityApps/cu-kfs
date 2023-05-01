package edu.cornell.kfs.coa.document;

import org.kuali.kfs.coa.document.AccountDelegateGlobalMaintainableImpl;
import org.kuali.kfs.coa.service.AccountDelegateService;
import org.kuali.kfs.sys.context.SpringContext;

public class CuAccountDelegateGlobalMaintainableImpl extends AccountDelegateGlobalMaintainableImpl {

    private transient AccountDelegateService accountDelegateService;

    /**
     * Overridden to delegate ALL maintenance lock checks to the AccountDelegateService.
     * The superclass normally checks the locks in two phases: One for the standard search
     * and one for the account-delegate-specific search. To improve efficiency, Cornell has
     * modified the related AccountDelegateService method to perform both checks in a single
     * bulk operation.
     */
    @Override
    public String getLockingDocumentId() {
        return getAccountDelegateService().getLockingDocumentId(this, getDocumentNumber());
    }

    private AccountDelegateService getAccountDelegateService() {
        if (accountDelegateService == null) {
            accountDelegateService = SpringContext.getBean(AccountDelegateService.class);
        }
        return accountDelegateService;
    }

}
