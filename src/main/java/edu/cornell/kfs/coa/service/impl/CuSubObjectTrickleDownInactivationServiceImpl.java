package edu.cornell.kfs.coa.service.impl;

import java.util.Collection;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.impl.SubObjectTrickleDownInactivationServiceImpl;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.coa.COAKeyConstants;

public class CuSubObjectTrickleDownInactivationServiceImpl extends SubObjectTrickleDownInactivationServiceImpl {
	
	/**
	 * @see org.kuali.kfs.coa.service.impl.SubObjectTrickleDownInactivationServiceImpl#trickleDownInactivateSubObjects(org.kuali.kfs.coa.businessobject.Account, java.lang.String)
	 */
	@Override
	public void trickleDownInactivateSubObjects(final Account inactivatedAccount, final String documentNumber) {
        final Collection<SubObjectCode> subObjects = getAssociatedSubObjects(inactivatedAccount);
        final TrickleDownInactivationStatus trickleDownInactivationStatus = trickleDownInactivate(subObjects, documentNumber);
        // KFSPTS-3877 add note to object level instead of document
        addNotesToAccountObject(trickleDownInactivationStatus, documentNumber, inactivatedAccount);
	}
	
    /**
     * Adds an inactivation note at account object level
     * 
     * @param trickleDownInactivationStatus
     * @param documentNumber
     * @param inactivatedAccount
     */
    protected void addNotesToAccountObject(final TrickleDownInactivationStatus trickleDownInactivationStatus,
            final String documentNumber, final Account inactivatedAccount) {
        if (trickleDownInactivationStatus.inactivatedSubObjCds.isEmpty() && trickleDownInactivationStatus.alreadyLockedSubObjCds.isEmpty() && trickleDownInactivationStatus.errorPersistingSubObjCds.isEmpty()) {
            // if we didn't try to inactivate any sub-objects, then don't bother
            return;
        }
        
        final Note newNote = new Note();
        
        addNotes(documentNumber, trickleDownInactivationStatus.inactivatedSubObjCds, COAKeyConstants.SUB_OBJECT_TRICKLE_DOWN_INACTIVATION, inactivatedAccount, newNote);
        addNotes(documentNumber, trickleDownInactivationStatus.errorPersistingSubObjCds, COAKeyConstants.SUB_OBJECT_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE, inactivatedAccount, newNote);
        addMaintenanceLockedNotes(documentNumber, trickleDownInactivationStatus.alreadyLockedSubObjCds, COAKeyConstants.SUB_OBJECT_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED, inactivatedAccount, newNote);
    }

}
