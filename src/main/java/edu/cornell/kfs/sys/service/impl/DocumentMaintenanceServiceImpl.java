package edu.cornell.kfs.sys.service.impl;

import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.CoreConfigHelper;
import org.kuali.rice.core.api.criteria.PredicateFactory;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.ActionItemExtension;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.DocumentRefreshQueue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.ksb.api.KsbApiServiceLocator;
import org.kuali.rice.ksb.api.messaging.AsynchronousCallback;
import org.kuali.rice.ksb.api.messaging.MessageHelper;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger(DocumentMaintenanceServiceImpl.class);

    public static final int WAIT_TIME_NO_WAIT = 0;
    public static final int WAIT_ONE_MINUTE = 60000;

    private DocumentMaintenanceDao documentMaintenanceDao;

    @Transactional
    public boolean requeueDocuments() {
        Collection<String> docIds = documentMaintenanceDao.getDocumentRequeueValues();
        LOG.info("requeueDocuments: Total number of documents flagged for requeuing: " + docIds.size());

        List<ActionItemNoteDetailDto> noteDetails = documentMaintenanceDao.getActionNotesToBeRequeued();
        LOG.info("requeueDocuments: Total number of action note details: " + noteDetails.size());

        for (String docId : docIds) {
            LOG.info("requeueDocuments: Requesting requeue for document: " + docId);
            //MessageHelper messageHelper = KsbApiServiceLocator.getMessageHelper();
            //AsynchronousCallback callback;
            //DocumentRefreshQueue documentRequeuer = messageHelper.getServiceAsynchronously(KewApiServiceLocator.DOCUMENT_REFRESH_QUEUE, callback);
            
            //DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(CoreConfigHelper.getApplicationId(), docId, WAIT_TIME_NO_WAIT);
            DocumentRefreshQueue documentRequeuer = (DocumentRefreshQueue) SpringContext.getService("rice.kew.documentRefreshQueue");
                    
            
            
            documentRequeuer.refreshDocument(docId);
            
            
            List<ActionItemNoteDetailDto> noteDetailsForDocument = findNoteDetailsForDocument(noteDetails, docId);
            for (ActionItemNoteDetailDto detailDto : noteDetailsForDocument) {
                List<ActionItem> actionItems = findActionItem(detailDto);
                if (CollectionUtils.isNotEmpty(actionItems)) {
                    ActionItem item = actionItems.get(0);
                    if (ObjectUtils.isNotNull(item)) {
                        
                        ActionItemExtension actionItemExtension = findActionItemEtencsion(item.getId());
                        if (ObjectUtils.isNull(actionItemExtension)) {
                            actionItemExtension = buildActionItemExtension(detailDto, item);
                            LOG.info("requeueDocuments, adding note details " + detailDto.toString() + " to action item " + item.getId());
                        } else {
                            actionItemExtension.setActionNote(detailDto.getActionNote());
                            actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
                            //actionItemExtension.setLockVerNbr(actionItemExtension.getLockVerNbr() + 1);
                            LOG.info("requeueDocuments, updating note details " + detailDto.toString() + " to action item " + item.getId());
                        }
                        KRADServiceLocator.getDataObjectService().save(actionItemExtension);
                    }
                }
            }
        }
        return true;
    }
    
    private ActionItemExtension findActionItemEtencsion(String actionItemId) {
        return KRADServiceLocator.getDataObjectService().find(ActionItemExtension.class, actionItemId);
    }

    private ActionItemExtension buildActionItemExtension(ActionItemNoteDetailDto detailDto, ActionItem item) {
        ActionItemExtension actionItemExtension = new ActionItemExtension();
        actionItemExtension.setActionItemId(item.getId());
        //actionItemExtension.setLockVerNbr(new Integer(1));
        actionItemExtension.setActionNote(detailDto.getActionNote());
        actionItemExtension.setNoteTimeStamp(detailDto.getNoteTimeStamp());
        return actionItemExtension;
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
            }
        }
        return noteDetailsForDocument;
    }

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

}
