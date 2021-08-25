package edu.cornell.kfs.sys.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionlist.dao.impl.ActionListPriorityComparator;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.kew.actionitem.ActionItemExtension;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger(DocumentMaintenanceServiceImpl.class);
    private DocumentMaintenanceDao documentMaintenanceDao;

    @Override
    public boolean requeueDocuments() {
        Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
        LOG.info("requeueDocuments: Total number of documents flagged for requeuing: " + docIds.size());

        List<ActionItemNoteDetailDto> noteDetails = documentMaintenanceDao.getActionNotesToBeRequeued();
        LOG.info("requeueDocuments: Total number of action note details: " + noteDetails.size());
        
        for (String docId : docIds) {
            requeueDocumentByDocumentId(noteDetails, docId);
        }
        return true;
    }
    
    @Transactional
    private void requeueDocumentByDocumentId(List<ActionItemNoteDetailDto> noteDetails, String docId) {
        LOG.info("requeueDocumentByDocumentId: Requesting requeue for document: " + docId);
        DocumentRefreshQueue documentRequeuer = (DocumentRefreshQueue) SpringContext.getService(CUKFSConstants.DOCUMENT_REFRESH_QUEUE_SERVICE_SPRING_CONTEXT_NAME);
        documentRequeuer.refreshDocument(docId);
        
        List<ActionItemNoteDetailDto> noteDetailsForDocument = findNoteDetailsForDocument(noteDetails, docId);
        for (ActionItemNoteDetailDto detailDto : noteDetailsForDocument) {
            ActionItem actionItem = findActionItem(detailDto);
            if (ObjectUtils.isNotNull(actionItem)) {
                ActionItemExtension actionItemExtension = findActionItemExtension(actionItem.getId());
                if (ObjectUtils.isNull(actionItemExtension)) {
                    actionItemExtension = buildActionItemExtension(detailDto, actionItem);
                    LOG.info("requeueDocumentByDocumentId, adding note details " + detailDto.toString() + " to action item " + actionItem.getId());
                } else {
                    actionItemExtension.setActionNote(detailDto.getActionNote());
                    // TODO: KFSPTS-21563: Update or remove the timestamp setup below accordingly.
                    //actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
                    LOG.info("requeueDocumentByDocumentId, updating note details " + detailDto.toString() + " to action item " + actionItem.getId());
                }
                KRADServiceLocator.getBusinessObjectService().save(actionItemExtension);
            }
        }
    }
    
    private ActionItem findActionItem(ActionItemNoteDetailDto detailDto) {
        Map<String, Object> query = Map.ofEntries(
                Map.entry(KFSPropertyConstants.PRINCIPAL_ID, detailDto.getPrincipalId()),
                Map.entry(CUKFSConstants.DOCUMENT_ID, detailDto.getDocHeaderId()));
        Collection<ActionItem> actionItems = KRADServiceLocator.getBusinessObjectService().findMatching(ActionItem.class, query);
        ActionItem selectedActionItem = null;
        if (LOG.isDebugEnabled() ) {
            LOG.debug("findActionItem, number of action items for principal " +  detailDto.getPrincipalId() + " and document number " 
                    + detailDto.getDocHeaderId() + " is " + CollectionUtils.size(actionItems));
        }
        if (CollectionUtils.isNotEmpty(actionItems)) {
            selectedActionItem = (ActionItem) Collections.max(actionItems, new ActionListPriorityComparator());
        }
        return selectedActionItem;
    }
    
    private ActionItemExtension findActionItemExtension(String actionItemId) {
        return KRADServiceLocator.getBusinessObjectService().findBySinglePrimaryKey(ActionItemExtension.class, actionItemId);
    }

    private ActionItemExtension buildActionItemExtension(ActionItemNoteDetailDto detailDto, ActionItem item) {
        ActionItemExtension actionItemExtension = new ActionItemExtension();
        actionItemExtension.setActionItemId(item.getId());
        actionItemExtension.setActionNote(detailDto.getActionNote());
        // TODO: KFSPTS-21563: Update or remove the timestamp setup below accordingly.
        //actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
        return actionItemExtension;
    }
    
    private List<ActionItemNoteDetailDto> findNoteDetailsForDocument(List<ActionItemNoteDetailDto> noteDetails, String documentId) {
        List<ActionItemNoteDetailDto> noteDetailsForDocument = new ArrayList<ActionItemNoteDetailDto>();
        for (ActionItemNoteDetailDto detail : noteDetails) {
            if (StringUtils.equalsIgnoreCase(documentId, detail.getDocHeaderId())) {
                noteDetailsForDocument.add(detail);
            }
        }
        return noteDetailsForDocument;
    }

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

}
