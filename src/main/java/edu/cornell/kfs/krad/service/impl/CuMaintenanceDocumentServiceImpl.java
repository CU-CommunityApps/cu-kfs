package edu.cornell.kfs.krad.service.impl;

import java.util.List;
import java.util.stream.Collectors;


import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.dao.MaintenanceDocumentDao;
import org.kuali.kfs.krad.exception.DocumentTypeAuthorizationException;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.impl.MaintenanceDocumentServiceImpl;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.krad.dao.CuMaintenanceDocumentDao;
import edu.cornell.kfs.krad.service.CuMaintenanceDocumentService;

public class CuMaintenanceDocumentServiceImpl extends MaintenanceDocumentServiceImpl implements CuMaintenanceDocumentService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    @SuppressWarnings("unchecked")
    public MaintenanceDocument setupNewMaintenanceDocument(String objectClassName, String documentTypeName,
            String maintenanceAction) {
        if (StringUtils.isEmpty(objectClassName) && StringUtils.isEmpty(documentTypeName)) {
            throw new IllegalArgumentException("Document type name or bo class not given!");
        }

        // get document type if not passed
        if (StringUtils.isEmpty(documentTypeName)) {
            try {
                documentTypeName = getDocumentDictionaryService().getMaintenanceDocumentTypeName(
                        Class.forName(objectClassName));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (StringUtils.isEmpty(documentTypeName)) {
                throw new RuntimeException("documentTypeName is empty; does this Business Object have a" +
                        " maintenance document definition? " + objectClassName);
            }
        }

        // check doc type allows new or copy if that action was requested
        if (KRADConstants.MAINTENANCE_NEW_ACTION.equals(maintenanceAction) ||
            KRADConstants.MAINTENANCE_COPY_ACTION.equals(maintenanceAction)) {
            Class<?> boClass = getDocumentDictionaryService().getMaintenanceDataObjectClass(documentTypeName);
            boolean allowsNewOrCopy = getDataObjectAuthorizationService()
                .canCreate(boClass, GlobalVariables.getUserSession().getPerson(), documentTypeName);
            if (!allowsNewOrCopy) {
                LOG.error("Document type {} does not allow new or copy actions.", documentTypeName);
                throw new DocumentTypeAuthorizationException(
                    GlobalVariables.getUserSession().getPerson().getPrincipalId(), "newOrCopy", documentTypeName);
            }
        }

        // get new document from service
        return (MaintenanceDocument) getDocumentService().getNewDocument(documentTypeName);
    }
    
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
