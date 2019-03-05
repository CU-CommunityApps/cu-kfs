package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.ActionItemExtension;
import org.kuali.rice.kew.actionlist.dao.impl.ActionListPriorityComparator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger(DocumentMaintenanceServiceImpl.class);
    private DocumentMaintenanceDao documentMaintenanceDao;

    @Transactional
    public boolean requeueDocuments() {
        Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
        LOG.info("requeueDocuments: Total number of documents flagged for requeuing: " + docIds.size());

        List<ActionItemNoteDetailDto> noteDetails = documentMaintenanceDao.getActionNotesToBeRequeued();
        LOG.info("requeueDocuments: Total number of action note details: " + noteDetails.size());
        
        for (String docId : docIds) {
            LOG.info("requeueDocuments: Requesting requeue for document: " + docId);
            DocumentRefreshQueue documentRequeuer = (DocumentRefreshQueue) SpringContext.getService(CUKFSConstants.DOCUMENT_REFRESH_QUEUE_SERVICE_SPRING_CONTEXT_NAME);
            documentRequeuer.refreshDocument(docId);
            
            List<ActionItemNoteDetailDto> noteDetailsForDocument = findNoteDetailsForDocument(noteDetails, docId);
            for (ActionItemNoteDetailDto detailDto : noteDetailsForDocument) {
                ActionItem actionItem = findActionItem(detailDto);
                if (ObjectUtils.isNotNull(actionItem)) {
                    ActionItemExtension actionItemExtension = findActionItemExtension(actionItem.getId());
                    if (ObjectUtils.isNull(actionItemExtension)) {
                        actionItemExtension = buildActionItemExtension(detailDto, actionItem);
                        LOG.info("requeueDocuments, adding note details " + detailDto.toString() + " to action item " + actionItem.getId());
                    } else {
                        actionItemExtension.setActionNote(detailDto.getActionNote());
                        actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
                        LOG.info("requeueDocuments, updating note details " + detailDto.toString() + " to action item " + actionItem.getId());
                    }
                    KRADServiceLocator.getDataObjectService().save(actionItemExtension);
                }
            }
        }
        return true;
    }
    
    private ActionItem findActionItem(ActionItemNoteDetailDto detailDto) {
        QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KFSPropertyConstants.PRINCIPAL_ID, detailDto.getPrincipalId()),
                PredicateFactory.equal(CUKFSConstants.DOCUMENT_ID, detailDto.getDocHeaderId()));
        List<ActionItem> actionItems = KRADServiceLocator.getDataObjectService().findMatching(ActionItem.class, query).getResults();
        ActionItem selectedActionItem = null;
        if (LOG.isDebugEnabled() ) {
            LOG.info("findActionItem, number of action items for principle " +  detailDto.getPrincipalId() + " and cocument number " 
                    + detailDto.getDocHeaderId() + " is " + CollectionUtils.size(actionItems));
        }
        if (CollectionUtils.isNotEmpty(actionItems)) {
            selectedActionItem = (ActionItem) Collections.max(actionItems, new ActionListPriorityComparator());
        }
        return selectedActionItem;
    }
    
    private ActionItemExtension findActionItemExtension(String actionItemId) {
        return KRADServiceLocator.getDataObjectService().find(ActionItemExtension.class, actionItemId);
    }

    private ActionItemExtension buildActionItemExtension(ActionItemNoteDetailDto detailDto, ActionItem item) {
        ActionItemExtension actionItemExtension = new ActionItemExtension();
        actionItemExtension.setActionItemId(item.getId());
        actionItemExtension.setActionNote(detailDto.getActionNote());
        actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
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
