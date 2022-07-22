/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.coa.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.AccountDelegate;
import org.kuali.kfs.coa.dataaccess.AccountDelegateDao;
import org.kuali.kfs.coa.dataaccess.AccountDelegateGlobalDao;
import org.kuali.kfs.coa.document.AccountDelegateGlobalMaintainableImpl;
import org.kuali.kfs.coa.document.AccountDelegateMaintainableImpl;
import org.kuali.kfs.coa.service.AccountDelegateService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AccountDelegateServiceImpl implements AccountDelegateService {

    private AccountDelegateDao accountDelegateDao;
    private AccountDelegateGlobalDao accountDelegateGlobalDao;
    private BusinessObjectService businessObjectService;
    private DocumentDictionaryService documentDictionaryService;
    protected DateTimeService dateTimeService;

    @Override
    public String getLockingDocumentId(AccountDelegateGlobalMaintainableImpl global, String docNumber) {
        String lockingDocId = null;
        List<MaintenanceLock> maintenanceLocks = global.generateMaintenanceLocks();
        for (MaintenanceLock maintenanceLock : maintenanceLocks) {
            lockingDocId = accountDelegateGlobalDao.getLockingDocumentNumber(maintenanceLock.getLockingRepresentation(),
                    docNumber);
            if (StringUtils.isNotBlank(lockingDocId)) {
                break;
            }
        }
        return lockingDocId;
    }

    @Override
    public String getLockingDocumentId(AccountDelegateMaintainableImpl delegate, String docNumber) {
        String lockingDocId = null;
        List<MaintenanceLock> maintenanceLocks = delegate.generateMaintenanceLocks();
        maintenanceLocks.add(delegate.createGlobalAccountLock());

        for (MaintenanceLock maintenanceLock : maintenanceLocks) {
            lockingDocId = accountDelegateDao.getLockingDocumentNumber(maintenanceLock.getLockingRepresentation(),
                    docNumber);
            if (StringUtils.isNotBlank(lockingDocId)) {
                break;
            }
        }
        return lockingDocId;
    }

    @Override
    public FinancialSystemMaintainable buildMaintainableForAccountDelegate(AccountDelegate delegate) {
        FinancialSystemMaintainable maintainable = getAccountDelegateMaintainable();
        maintainable.setDataObjectClass(delegate.getClass());
        maintainable.setBusinessObject(delegate);
        return maintainable;
    }

    /**
     * @return the proper class for the Maintainable associated with AccountDelegate maintenance documents
     */
    protected Class getAccountDelegateMaintainableClass() {
        return documentDictionaryService.getMaintenanceDocumentEntry(AccountDelegate.class.getName())
                .getMaintainableClass();
    }

    /**
     * @return a new instance of the proper maintainable for AccountDelegate maintenance documents
     */
    protected FinancialSystemMaintainable getAccountDelegateMaintainable() {
        final Class maintainableClazz = getAccountDelegateMaintainableClass();
        final FinancialSystemMaintainable maintainable;
        try {
            maintainable = (FinancialSystemMaintainable) maintainableClazz.newInstance();
        } catch (Exception ie) {
            throw new RuntimeException("Could not instantiate maintainable for AccountDelegate maintenance document",
                    ie);
        }
        return maintainable;
    }

    @Override
    public Iterator<AccountDelegate> retrieveAllActiveDelegationsForPerson(String principalId, boolean primary) {
        return accountDelegateDao.getAccountDelegationsForPerson(principalId, primary);
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormPrimaryAccountDelegate(String principalId) {
        return accountDelegateDao.isPrincipalInAnyWayShapeOrFormPrimaryAccountDelegate(principalId,
                dateTimeService.getCurrentSqlDate());
    }

    @Override
    public boolean isPrincipalInAnyWayShapeOrFormSecondaryAccountDelegate(String principalId) {
        return accountDelegateDao.isPrincipalInAnyWayShapeOrFormSecondaryAccountDelegate(principalId,
                dateTimeService.getCurrentSqlDate());
    }

    /**
     * Saves the given account delegate to the persistence store
     *
     * @param accountDelegate the account delegate to save
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveForMaintenanceDocument(AccountDelegate accountDelegate) {
        businessObjectService.linkAndSave(accountDelegate);
    }

    /**
     * Persists the given account delegate global maintenance document inactivations
     *
     * @param delegatesToInactivate the List of delegates to inactivate
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInactivationsForGlobalMaintenanceDocument(List<PersistableBusinessObject> delegatesToInactivate) {
        if (delegatesToInactivate != null && !delegatesToInactivate.isEmpty()) {
            businessObjectService.save(delegatesToInactivate);
        }
    }

    /**
     * Persists the given account delegate global maintenance document changes
     *
     * @param delegatesToChange the List of delegates to change
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveChangesForGlobalMaintenanceDocument(List<PersistableBusinessObject> delegatesToChange) {
        if (delegatesToChange != null && !delegatesToChange.isEmpty()) {
            businessObjectService.save(delegatesToChange);
        }
    }

    @Override
    @Transactional
    public void updateDelegationRole(
            final String docIdToIgnore,
            final Set<String> accountNumbers,
            final Set<String> documentTypes
    ) {
        final RoleService roleManagementService = KimApiServiceLocator.getRoleService();
        final String roleId = roleManagementService.getRoleIdByNamespaceCodeAndName(
                KFSConstants.CoreModuleNamespaces.KFS,
                KFSConstants.SysKimApiConstants.FISCAL_OFFICER_KIM_ROLE_NAME
        );
        if (StringUtils.isNotBlank(roleId)) {
            final List<RoleResponsibility> newRoleResp = roleManagementService.getRoleResponsibilities(roleId);
            KEWServiceLocator.getActionRequestService().updateActionRequestsForResponsibilityChange(
                    getChangedRoleResponsibilityIds(newRoleResp), docIdToIgnore, accountNumbers, documentTypes);
        }
    }

    protected Set<String> getChangedRoleResponsibilityIds(List<RoleResponsibility> newRoleResp) {
        Set<String> lRet = new HashSet<>();
        if (ObjectUtils.isNotNull(newRoleResp)) {
            for (RoleResponsibility roleResp : newRoleResp) {
                lRet.add(roleResp.getResponsibilityId());
            }
        }
        return lRet;
    }

    public void setAccountDelegateDao(AccountDelegateDao accountDelegateDao) {
        this.accountDelegateDao = accountDelegateDao;
    }

    public void setAccountDelegateGlobalDao(AccountDelegateGlobalDao accountDelegateGlobalDao) {
        this.accountDelegateGlobalDao = accountDelegateGlobalDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDocumentDictionaryService(DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }
}
