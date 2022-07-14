package edu.cornell.kfs.module.purap.document.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.POTransmissionMethods;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.impl.PurchaseOrderServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.service.CuPurapService;
import edu.cornell.kfs.sys.businessobject.NoteExtendedAttribute;

public class CuPurchaseOrderServiceImpl extends PurchaseOrderServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    private AttachmentService attachmentService;

    @Override
    public void performPurchaseOrderFirstTransmitViaPrinting(PurchaseOrderDocument po) {
        if (ObjectUtils.isNotNull(po.getPurchaseOrderFirstTransmissionTimestamp())) {
            // should not call this method for first transmission if document has already been transmitted
            String errorMsg = "Method to perform first transmit was called on document (doc id " + po.getDocumentNumber() + ") with already filled in 'first transmit date'";
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        Timestamp currentDate = dateTimeService.getCurrentTimestamp();
        po.setPurchaseOrderFirstTransmissionTimestamp(currentDate);
        po.setPurchaseOrderLastTransmitTimestamp(currentDate);
        po.setOverrideWorkflowButtons(Boolean.FALSE);
        // KFSUPGRADE-336
        boolean performedAction = purapWorkflowIntegrationService.takeAllActionsForGivenCriteria(po, "Action taken automatically as part of document initial print transmission", CUPurapConstants.PurchaseOrderStatuses.NODE_DOCUMENT_TRANSMISSION, GlobalVariables.getUserSession().getPerson(), null);
        if (!performedAction) {
            Person systemUserPerson = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
            purapWorkflowIntegrationService.takeAllActionsForGivenCriteria(po, "Action taken automatically as part of document initial print transmission by user " + GlobalVariables.getUserSession().getPerson().getName(), CUPurapConstants.PurchaseOrderStatuses.NODE_DOCUMENT_TRANSMISSION, systemUserPerson, KFSConstants.SYSTEM_USER);
        }
        po.setOverrideWorkflowButtons(Boolean.TRUE);
        if (!po.getApplicationDocumentStatus().equals(PurchaseOrderStatuses.APPDOC_OPEN)) {
            attemptSetupOfInitialOpenOfDocument(po);
            if (shouldAdhocFyi(po.getRequisitionSourceCode())) {
                sendAdhocFyi(po);
            }
        }
        purapService.saveDocumentNoValidation(po);
    }

    private boolean shouldAdhocFyi(String reqSourceCode) {
        Collection<String> excludeList = new ArrayList<String>();
        if (parameterService.parameterExists(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS)) {
            excludeList = parameterService.getParameterValuesAsString(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS);
        }
        return !excludeList.contains(reqSourceCode);
    }

	@Override
	public void completePurchaseOrderAmendment(PurchaseOrderDocument poa) {
		// TODO Auto-generated method stub
		super.completePurchaseOrderAmendment(poa);
        if (PurchaseOrderStatuses.APPDOC_PENDING_CXML.equals(poa.getApplicationDocumentStatus())) {
            completeB2BPurchaseOrderAmendment(poa);
        }

	}

    protected boolean completeB2BPurchaseOrderAmendment(PurchaseOrderDocument poa) {
        String errors = b2bPurchaseOrderService.sendPurchaseOrder(poa);
        if (StringUtils.isEmpty(errors)) {
            //POA sent successfully; change status to OPEN
            LOG.info("Setting poa document id " + poa.getDocumentNumber() + " status from '" + poa.getApplicationDocumentStatus() + "' to '" + PurchaseOrderStatuses.APPDOC_OPEN + "'");
   //         purapService.updateStatus(poa, PurchaseOrderStatuses.OPEN);
            poa.setPurchaseOrderLastTransmitTimestamp(dateTimeService.getCurrentTimestamp());
            return true;
        }
        else {
            //POA transmission failed; record errors and change status to "cxml failed"
            try {
                Note note = documentService.createNoteFromDocument(poa, "Unable to transmit the PO for the following reasons:\n" + errors);
                poa.addNote(note);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            
 //           purapService.updateStatus(poa, PurchaseOrderStatuses.CXML_ERROR);
            return false;
        }
    }

    private Note truncateNoteTextTo800(Note note) {
    	if (note.getNoteText().length() > 800) {
    		note.setNoteText(note.getNoteText().substring(0, 799));
    	}
    	return note;
    }

    protected void setupDocumentForPendingFirstTransmission(PurchaseOrderDocument po) {
        if (POTransmissionMethods.PRINT.equals(po.getPurchaseOrderTransmissionMethodCode()) || POTransmissionMethods.FAX.equals(po.getPurchaseOrderTransmissionMethodCode()) || POTransmissionMethods.ELECTRONIC.equals(po.getPurchaseOrderTransmissionMethodCode())) {
            // Leaving conditional code in place here to ensure that we can exclude some transmission methods from routing to SciQuest if we want.
//            String newStatusCode = PurchaseOrderStatuses.STATUSES_BY_TRANSMISSION_TYPE.get(po.getPurchaseOrderTransmissionMethodCode());
          // Forcing all the POs to transmit via Electronic, so they all route to SciQuest for transmission, regardless of value provided.
          String newStatusCode = PurchaseOrderStatuses.STATUSES_BY_TRANSMISSION_TYPE.get(PurapConstants.POTransmissionMethods.ELECTRONIC);
            if (LOG.isDebugEnabled()) {
                LOG.debug("setupDocumentForPendingFirstTransmission() Purchase Order Transmission Type is '" + po.getPurchaseOrderTransmissionMethodCode() + "' setting status to '" + newStatusCode + "'");
            }
          po.updateAndSaveAppDocStatus(newStatusCode);
        }
    }

    protected PurchaseOrderDocument generatePurchaseOrderFromRequisition(RequisitionDocument reqDocument) {
        PurchaseOrderDocument poDocument = super.generatePurchaseOrderFromRequisition(reqDocument);
        return copyNotesAndAttachmentsToPO(reqDocument, poDocument); 
    }

    // mjmc *************************************************************************************************
    private PurchaseOrderDocument copyNotesAndAttachmentsToPO(RequisitionDocument reqDoc, PurchaseOrderDocument poDoc) {

        purapService.saveDocumentNoValidation(poDoc);
        List<Note> notes = (List<Note>) reqDoc.getNotes();
        int noteLength = notes.size();
        if (noteLength > 0) {
            for (Note note : notes) {
                try {
                    Note copyingNote = documentService.createNoteFromDocument(poDoc, note.getNoteText());
                    purapService.saveDocumentNoValidation(poDoc);
                    copyingNote.setNotePostedTimestamp(note.getNotePostedTimestamp());
                    copyingNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyingNote.setNoteTopicText(note.getNoteTopicText());
                    Attachment originalAttachment = attachmentService.getAttachmentByNoteId(note.getNoteIdentifier());
                    NoteExtendedAttribute noteExtendedAttribute = (NoteExtendedAttribute) note.getExtension();
                    if (originalAttachment != null || (ObjectUtils.isNotNull(noteExtendedAttribute) && noteExtendedAttribute.isCopyNoteIndicator())) {
                    	if (originalAttachment != null) {
                    		Attachment newAttachment = attachmentService.createAttachment((PersistableBusinessObject)copyingNote, originalAttachment.getAttachmentFileName(), originalAttachment.getAttachmentMimeTypeCode(), originalAttachment.getAttachmentFileSize().intValue(), originalAttachment.getAttachmentContents(), originalAttachment.getAttachmentTypeCode());//new Attachment();

                    		if (ObjectUtils.isNotNull(originalAttachment) && ObjectUtils.isNotNull(newAttachment)) {
                    			copyingNote.addAttachment(newAttachment);
                    		}
                    	}
                    	
                    	poDoc.addNote(copyingNote);
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        purapService.saveDocumentNoValidation(poDoc);
        return poDoc;
    }   //  mjmc end

    /**
     * Overridden to use the PO's note target object ID to retrieve the notes, instead of always using the doc header as the target.
     * 
     * @see org.kuali.kfs.module.purap.document.service.impl.PurchaseOrderServiceImpl#getPurchaseOrderNotes(java.lang.Integer)
     */
    @Override
    public List<Note> getPurchaseOrderNotes(Integer id) {
        List<Note> notes = new ArrayList<>();
        PurchaseOrderDocument po = getPurchaseOrderByDocumentNumber(purchaseOrderDao.getOldestPurchaseOrderDocumentNumber(id));

        if (ObjectUtils.isNotNull(po)) {

            // ==== CU Customization: Use the PO's actual note target instead of assuming that the doc header is the target. ====
            notes = noteService.getByRemoteObjectId(po.getNoteTarget().getObjectId());
        }
        return notes;
    }

    @Override
    public KualiDecimal getInternalPurchasingDollarLimit(PurchaseOrderDocument document) {
        if (document.getVendorContract() != null && document.getContractManager() == null) {
            return ((CuPurapService) purapService).getApoLimit(document);
        } else {
            return super.getInternalPurchasingDollarLimit(document);
        }
    }

    @Override
    protected PurchaseOrderDocument createPurchaseOrderDocumentFromSourceDocument(
            PurchaseOrderDocument sourceDocument, String docType) {
        PurchaseOrderDocument newDocument = super.createPurchaseOrderDocumentFromSourceDocument(sourceDocument, docType);
        resetOverrideCodesOnItemAccountingLines(newDocument);
        return newDocument;
    }

    protected void resetOverrideCodesOnItemAccountingLines(PurchaseOrderDocument document) {
        List<PurApItem> items = (List<PurApItem>) document.getItems();
        items.stream()
                .flatMap((item) -> item.getSourceAccountingLines().stream())
                .filter(this::accountingLineHasNonDefaultOverrideCode)
                .forEach(this::resetOverrideCodeOnAccountingLine);
    }

    protected boolean accountingLineHasNonDefaultOverrideCode(PurApAccountingLine accountingLine) {
        return !StringUtils.equals(AccountingLineOverride.CODE.NONE, accountingLine.getOverrideCode());
    }

    protected void resetOverrideCodeOnAccountingLine(PurApAccountingLine accountingLine) {
        accountingLine.setOverrideCode(AccountingLineOverride.CODE.NONE);
    }

    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

}
