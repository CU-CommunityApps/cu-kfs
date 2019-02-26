package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.ActionItemExtension;
import org.kuali.rice.kew.actionlist.service.ActionListService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
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
            
           for (ActionItemNoteDetailDto detailDto : noteDetailsForDocument) {
               List<ActionItem> actionItems = findActionItem(detailDto);
               if (CollectionUtils.isNotEmpty(actionItems)) {
                   ActionItem item  = actionItems.get(0);
                   if (ObjectUtils.isNotNull(item)) {
                       LOG.info("requeueDocuments, avount to add an action item extension for action item ID " + item.getId());
                       ActionItemExtension actionItemExtension = new ActionItemExtension();
                       actionItemExtension.setActionItemId(item.getId());
                       actionItemExtension.setLockVerNbr(new Integer(1));
                       actionItemExtension.setActionNote(detailDto.getActionNote());
                       actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
                   }
               }
           }
        }
        return true;
    }
    
    private List<ActionItem> findActionItem(ActionItemNoteDetailDto detailDto) {
        QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal("principalId", detailDto.getPrincipleId()),
                PredicateFactory.equal("documentId", detailDto.getDocHeaderId()));
        
        return KRADServiceLocator.getDataObjectService().findMatching(ActionItem.class, query).getResults();
        
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

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

}

