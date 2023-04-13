package edu.cornell.kfs.krad.dao;

import java.util.List;

import org.kuali.kfs.krad.dao.MaintenanceDocumentDao;

public interface CuMaintenanceDocumentDao extends MaintenanceDocumentDao {

    String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber);

}
