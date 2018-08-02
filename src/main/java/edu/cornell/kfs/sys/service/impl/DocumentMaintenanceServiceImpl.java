package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Iterator;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentMaintenanceServiceImpl.class);

    public static final int WAIT_TIME_NO_WAIT = 0;

    private DocumentMaintenanceDao documentMaintenanceDao;

    @Transactional
    public boolean requeueDocuments() {
        Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
        LOG.info("requeueDocuments: Total number of documents flagged for requeuing: " + docIds.size());

        for (String docId : docIds) {
            LOG.info("requeueDocuments: Requesting requeue for document: " + docId);
            DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(CoreConfigHelper.getApplicationId(), docId, WAIT_TIME_NO_WAIT);
            documentRequeuer.refreshDocument(docId);
        }

        return true;
    }

    public DocumentMaintenanceDao getDocumentMaintenanceDao() {
        return documentMaintenanceDao;
    }

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

}

