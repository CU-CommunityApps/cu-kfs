package edu.cornell.kfs.coa.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.service.impl.SubAccountTrickleDownInactivationServiceImpl;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.coa.COAKeyConstants;

public class CuSubAccountTrickleDownInactivationServiceImpl extends SubAccountTrickleDownInactivationServiceImpl {
	private static final Logger LOG = LogManager.getLogger();
	
    /**
     * @see org.kuali.kfs.coa.service.impl.SubAccountTrickleDownInactivationServiceImpl#trickleDownInactivateSubAccounts(org.kuali.kfs.coa.businessobject.Account, java.lang.String)
     */
	@Override
    public void trickleDownInactivateSubAccounts(final Account inactivatedAccount, final String documentNumber) {
        final List<SubAccount> inactivatedSubAccounts = new ArrayList<>();
        final Map<SubAccount, String> alreadyLockedSubAccounts = new HashMap<>();
        final List<SubAccount> errorPersistingSubAccounts = new ArrayList<>();
        
        final Maintainable subAccountMaintainable;
        try {
            subAccountMaintainable = (Maintainable) maintenanceDocumentDictionaryService.getMaintainableClass(SubAccount.class.getName()).newInstance();
            subAccountMaintainable.setDataObjectClass(SubAccount.class);
            subAccountMaintainable.setDocumentNumber(documentNumber);
        }
        catch (Exception e) {
            LOG.error("Unable to instantiate SubAccount Maintainable" , e);
            throw new RuntimeException("Unable to instantiate SubAccount Maintainable" , e);
        }
        
        inactivatedAccount.refreshReferenceObject(KFSPropertyConstants.SUB_ACCOUNTS);
		if (ObjectUtils.isNotNull(inactivatedAccount.getSubAccounts()) 
		        && !inactivatedAccount.getSubAccounts().isEmpty()) {
			for (final Object entry : inactivatedAccount.getSubAccounts()) {
				final SubAccount subAccount = (SubAccount) entry;
                if (subAccount.isActive()) {
                    subAccountMaintainable.setBusinessObject(subAccount);
                    final List<MaintenanceLock> subAccountLocks = subAccountMaintainable.generateMaintenanceLocks();
                    
                    final MaintenanceLock failedLock = verifyAllLocksFromThisDocument(subAccountLocks, documentNumber);
                    if (failedLock != null) {
                        // another document has locked this sub account, so we don't try to inactivate the account
                        alreadyLockedSubAccounts.put(subAccount, failedLock.getDocumentNumber());
                    }
                    else {
                        // no locks other than our own (but there may have been no locks at all), just go ahead and
                        // try to update
                        subAccount.setActive(false);
                        
                        try {
                            subAccountMaintainable.saveBusinessObject();
                            inactivatedSubAccounts.add(subAccount);
                        }
                        catch (RuntimeException e) {
                            LOG.error("Unable to trickle-down inactivate sub-account {}", subAccount, e);
                            errorPersistingSubAccounts.add(subAccount);
                        }
                    }
                }
            }
            // KFSPTS-3877 add note to object level instead of document
            addNotesToAccountObject(documentNumber, inactivatedAccount, inactivatedSubAccounts, alreadyLockedSubAccounts, errorPersistingSubAccounts);
        }
    }

    /**
     * Adds an inactivation note at account object level
     * 
     * @param documentNumber
     * @param inactivatedAccount
     * @param inactivatedSubAccounts
     * @param alreadyLockedSubAccounts
     * @param errorPersistingSubAccounts
     */
    protected void addNotesToAccountObject(final String documentNumber, final Account inactivatedAccount,
            final List<SubAccount> inactivatedSubAccounts, final Map<SubAccount, String> alreadyLockedSubAccounts,
            final List<SubAccount> errorPersistingSubAccounts) {
        if (inactivatedSubAccounts.isEmpty() && alreadyLockedSubAccounts.isEmpty() && errorPersistingSubAccounts.isEmpty()) {
            // if we didn't try to inactivate any sub-accounts, then don't bother
            return;
        }

        final Note newNote = new Note();
        
        addNotes(documentNumber, inactivatedSubAccounts, COAKeyConstants.SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION, inactivatedAccount, newNote);
        addNotes(documentNumber, errorPersistingSubAccounts,COAKeyConstants.SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE, inactivatedAccount, newNote);
        addMaintenanceLockedNotes(documentNumber, alreadyLockedSubAccounts, COAKeyConstants.SUB_ACCOUNT_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED, inactivatedAccount, newNote);
    }

}
