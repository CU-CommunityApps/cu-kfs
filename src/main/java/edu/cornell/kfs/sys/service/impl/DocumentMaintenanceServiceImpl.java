package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger(DocumentMaintenanceServiceImpl.class);

    public static final int WAIT_TIME_NO_WAIT = 0;

    private DocumentMaintenanceDao documentMaintenanceDao;

    @Transactional
    public boolean requeueDocuments() {
        Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
        LOG.info("requeueDocuments: Total number of documents flagged for requeuing: " + docIds.size());
        
        List<ActionItemNoteDetailDto> noteDetails = documentMaintenanceDao.getActionNotesToBeRequeued();
        LOG.info("requeueDocuments: Total number of action note details: " + noteDetails.size());

        for (String docId : docIds) {
            LOG.info("requeueDocuments: Requesting requeue for document: " + docId);
            DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(CoreConfigHelper.getApplicationId(), docId, WAIT_TIME_NO_WAIT);
            //documentRequeuer.refreshDocument(docId);
            
            List<ActionItemNoteDetailDto> noteDetailsForDocument = findNoteDetailsForDocument(noteDetails, docId);
        }

        return true;
    }
    
    private List<ActionItemNoteDetailDto> findNoteDetailsForDocument(List<ActionItemNoteDetailDto> noteDetails, String documentId) {
        List<ActionItemNoteDetailDto> noteDetailsForDocument = new ArrayList<ActionItemNoteDetailDto>();
        for (ActionItemNoteDetailDto detail : noteDetails) {
            if (StringUtils.equalsIgnoreCase(documentId, detail.getDocHeaderId())) {
                noteDetailsForDocument.add(detail);
                LOG.info("findNoteDetailsForDocument: note detail: " + detail);
            }
        }
        return noteDetailsForDocument;
    }

    public DocumentMaintenanceDao getDocumentMaintenanceDao() {
        return documentMaintenanceDao;
    }

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

}

