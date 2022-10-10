/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.coa.service;

import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.document.AccountDelegateGlobalMaintainableImpl;
import org.kuali.kfs.coa.document.AccountDelegateMaintainableImpl;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An interface of services to support account delegate logic
 */
/* Cornell customization: backport FINP-8341 */
public interface AccountDelegateService {

    /**
     * This method checks for any MaintenanceLocks that would block the creation of this document
     *
     * @param global    The AccountDelegateGlobalMaintainableImpl to check against.
     * @param docNumber The document number of the AccountDelegateGlobalMaintainableImpl in question.
     * @return the documentNumber of the locking record or null if none.
     */
    String getLockingDocumentId(AccountDelegateGlobalMaintainableImpl global, String docNumber);

    /**
     * This method checks for any MaintenanceLocks that would block the creation of this document
     *
     * @param delegate  The AccountDelegateMaintainableImpl to check against.
     * @param docNumber The document number of the AccountDelegateMaintainableImpl in question.
     * @return the documentNumber of the locking record or null if none.
     */
    String getLockingDocumentId(AccountDelegateMaintainableImpl delegate, String docNumber);

    /**
     * Builds an appropriate maintainable with the given account delegate as the business object
     *
     * @param delegate the account delegate to wrap in a maintainable
     * @return an appropriate maintainable
     */
    FinancialSystemMaintainable buildMaintainableForAccountDelegate(AccountDelegate delegate);

    /**
     * Retrieves all active account delegations which delegate to the given Person
     *
     * @param principalId a principal id of the person to find account delegations for
     * @param primary     whether the account delegates returned should be primary or not
     * @return a List of AccountDelegate business objects, representing that person's delegations
     */
    Iterator<AccountDelegate> retrieveAllActiveDelegationsForPerson(String principalId, boolean primary);

    /**
     * Determines if the given principal is an active delegate for any non-closed account
     *
     * @param principalId the principal ID to check primary account delegations for
     * @return true if the principal is a primary account delegate, false otherwise
     */
    boolean isPrincipalInAnyWayShapeOrFormPrimaryAccountDelegate(String principalId);

    /**
     * Determines if the given principal is an active delegate for any non-closed account
     *
     * @param principalId the principal ID to check secondary account delegations for
     * @return true if the principal is a secondary account delegate, false otherwise
     */
    boolean isPrincipalInAnyWayShapeOrFormSecondaryAccountDelegate(String principalId);

    /**
     * Saves the given account delegate to the persistence store
     *
     * @param accountDelegate the account delegate to save
     */
    void saveForMaintenanceDocument(AccountDelegate accountDelegate);

    /**
     * Persists the given account delegate global maintenance document inactivations
     *
     * @param delegatesToInactivate the List of delegates to inactivate
     */
    void saveInactivationsForGlobalMaintenanceDocument(List<PersistableBusinessObject> delegatesToInactivate);

    /**
     * Persists the given account delegate global maintenance document changes
     *
     * @param delegatesToChange the List of delegates to change
     */
    void saveChangesForGlobalMaintenanceDocument(List<PersistableBusinessObject> delegatesToChange);

    /**
     * Updates the role that this delegate is part of, to account for the changes in this delegate
     * 
     * @param docIdToIgnore id of maint doc that spawned this request so it can be avoid being requeued
     * @param accountNumbers account numbers used to find docs to requeue
     * @param documentTypes document types used to find docs to requeue
     */
    void updateDelegationRole(
            String docIdToIgnore,
            Set<String> accountNumbers,
            Set<String> documentTypes
    );

}
