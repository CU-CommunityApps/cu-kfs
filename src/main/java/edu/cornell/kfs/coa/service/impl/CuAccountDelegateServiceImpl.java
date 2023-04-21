package edu.cornell.kfs.coa.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.coa.document.AccountDelegateGlobalMaintainableImpl;
import org.kuali.kfs.coa.service.impl.AccountDelegateServiceImpl;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;

import edu.cornell.kfs.coa.dataaccess.AccountDelegateGlobalLockDao;

public class CuAccountDelegateServiceImpl extends AccountDelegateServiceImpl {

    private AccountDelegateGlobalLockDao accountDelegateGlobalLockDao;

    @Override
    public String getLockingDocumentId(AccountDelegateGlobalMaintainableImpl global, String docNumber) {
        List<MaintenanceLock> maintenanceLocks = global.generateMaintenanceLocks();
        List<String> lockingRepresentations = maintenanceLocks.stream()
                .map(MaintenanceLock::getLockingRepresentation)
                .collect(Collectors.toUnmodifiableList());
        return accountDelegateGlobalLockDao.getAnyLockingDocumentNumber(lockingRepresentations, docNumber);
    }

    public void setAccountDelegateGlobalLockDao(AccountDelegateGlobalLockDao accountDelegateGlobalLockDao) {
        this.accountDelegateGlobalLockDao = accountDelegateGlobalLockDao;
    }

}
