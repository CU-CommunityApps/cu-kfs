package edu.cornell.kfs.krad.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.dao.MaintenanceDocumentDao;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.impl.MaintenanceDocumentServiceImpl;

import edu.cornell.kfs.krad.dao.CuMaintenanceDocumentDao;

public class CuMaintenanceDocumentServiceImpl extends MaintenanceDocumentServiceImpl {

    @Override
    public String getLockingDocumentId(Maintainable maintainable, String documentNumber) {
        List<MaintenanceLock> maintenanceLocks = maintainable.generateMaintenanceLocks();
        List<String> lockingRepresentations = maintenanceLocks.stream()
                .map(MaintenanceLock::getLockingRepresentation)
                .collect(Collectors.toUnmodifiableList());
        return getCuMaintenanceDocumentDao().getAnyLockingDocumentNumber(lockingRepresentations, documentNumber);
    }

    private CuMaintenanceDocumentDao getCuMaintenanceDocumentDao() {
        return (CuMaintenanceDocumentDao) getMaintenanceDocumentDao();
    }

    @Override
    public void setMaintenanceDocumentDao(MaintenanceDocumentDao maintenanceDocumentDao) {
        if (!(maintenanceDocumentDao instanceof CuMaintenanceDocumentDao)) {
            throw new IllegalArgumentException(
                    "maintenanceDocumentDao was not an instance of CuMaintenanceDocumentDao");
        }
        super.setMaintenanceDocumentDao(maintenanceDocumentDao);
    }

}
