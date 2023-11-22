package edu.cornell.kfs.sys.service.impl;

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
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.kew.actionitem.ActionItemExtension;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

public class DocumentMaintenanceServiceImpl implements DocumentMaintenanceService {
    private static final Logger LOG = LogManager.getLogger();

    private DocumentMaintenanceDao documentMaintenanceDao;
    private BusinessObjectService businessObjectService;

    @Override
    public List<ActionItemNoteDetailDto> getActionNotesToBeRequeuedForDocument(String documentId) {
        return documentMaintenanceDao.getActionNotesToBeRequeuedForDocument(documentId);
    }

    @Override
    public void restoreActionNotesForRequeuedDocument(String documentId, List<ActionItemNoteDetailDto> actionNotes) {
        if (CollectionUtils.isEmpty(actionNotes)) {
            LOG.info("restoreActionNotesForRequeuedDocument, There are no action notes to restore for document {}",
                    documentId);
            return;
        } else {
            LOG.info("restoreActionNotesForRequeuedDocument, Restoring {} action notes for document {}",
                    actionNotes.size(), documentId);
        }
        
        for (ActionItemNoteDetailDto detailDto : actionNotes) {
            if (!StringUtils.equals(detailDto.getDocHeaderId(), documentId)) {
                LOG.warn("restoreActionNotesForRequeuedDocument, Skipping note detail {} because it is not "
                        + "associated with document {}", detailDto, documentId);
                continue;
            }
            
            ActionItem actionItem = findActionItem(detailDto);
            if (ObjectUtils.isNotNull(actionItem)) {
                ActionItemExtension actionItemExtension = findActionItemExtension(actionItem.getId());
                if (ObjectUtils.isNull(actionItemExtension)) {
                    actionItemExtension = buildActionItemExtension(detailDto, actionItem);
                    LOG.info("restoreActionNotesForRequeuedDocument, Adding note detail {} to action item {}",
                            detailDto, actionItem.getId());
                } else {
                    actionItemExtension.setActionNote(detailDto.getActionNote());
                    LOG.info("restoreActionNotesForRequeuedDocument, Updating note detail {} for action item {}",
                            detailDto, actionItem.getId());
                }
                businessObjectService.save(actionItemExtension);
            } else {
                LOG.info("restoreActionNotesForRequeuedDocument, Skipping note detail {} because a matching "
                        + "action item no longer exists", detailDto);
            }
        }
        
        LOG.info("restoreActionNotesForRequeuedDocument, Finished restoring action notes for document {}", documentId);
    }
    
    private ActionItem findActionItem(ActionItemNoteDetailDto detailDto) {
        Map<String, Object> query = Map.ofEntries(
                Map.entry(KFSPropertyConstants.PRINCIPAL_ID, detailDto.getPrincipalId()),
                Map.entry(CUKFSConstants.DOCUMENT_ID, detailDto.getDocHeaderId()));
        Collection<ActionItem> actionItems = businessObjectService.findMatching(ActionItem.class, query);
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
        return businessObjectService.findBySinglePrimaryKey(ActionItemExtension.class, actionItemId);
    }

    private ActionItemExtension buildActionItemExtension(ActionItemNoteDetailDto detailDto, ActionItem item) {
        ActionItemExtension actionItemExtension = new ActionItemExtension();
        actionItemExtension.setActionItemId(item.getId());
        actionItemExtension.setActionNote(detailDto.getActionNote());
        return actionItemExtension;
    }

    public void setDocumentMaintenanceDao(DocumentMaintenanceDao documentMaintenanceDao) {
        this.documentMaintenanceDao = documentMaintenanceDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
