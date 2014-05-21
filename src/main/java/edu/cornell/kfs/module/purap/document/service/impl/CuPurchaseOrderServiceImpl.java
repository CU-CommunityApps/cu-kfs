package edu.cornell.kfs.module.purap.document.service.impl;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.POTransmissionMethods;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderDocTypes;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionSources;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.AutoClosePurchaseOrderView;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.impl.PurchaseOrderServiceImpl;
import org.kuali.kfs.module.purap.util.PurApRelatedViews;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.service.AttachmentService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class CuPurchaseOrderServiceImpl extends PurchaseOrderServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPurchaseOrderServiceImpl.class);

    @Override
    public boolean autoCloseFullyDisencumberedOrders() {
        LOG.debug("autoCloseFullyDisencumberedOrders() started");

        List<AutoClosePurchaseOrderView> autoCloseList = purchaseOrderDao.getAllOpenPurchaseOrders(getExcludedVendorChoiceCodes());

        for (AutoClosePurchaseOrderView poAutoClose : autoCloseList) {
            if ((poAutoClose.getTotalAmount() != null) && ((KualiDecimal.ZERO.compareTo(poAutoClose.getTotalAmount())) != 0)) {
                // KFSUPGRADE-363
            	if (paymentRequestsStatusCanAutoClose(poAutoClose)) {
                    LOG.info("autoCloseFullyDisencumberedOrders() PO ID " + poAutoClose.getPurapDocumentIdentifier() + " with total " + poAutoClose.getTotalAmount().doubleValue() + " will be closed");
                    String newStatus = PurapConstants.PurchaseOrderStatuses.APPDOC_PENDING_CLOSE;
                    String annotation = "This PO was automatically closed in batch.";
                    String documentType = PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_CLOSE_DOCUMENT;
                    PurchaseOrderDocument document = getPurchaseOrderByDocumentNumber(poAutoClose.getDocumentNumber());
                    createNoteForAutoCloseOrders(document, annotation);
                    createAndRoutePotentialChangeDocument(poAutoClose.getDocumentNumber(), documentType, annotation, null, newStatus);
                }
            }
        }

        LOG.debug("autoCloseFullyDisencumberedOrders() ended");

        return true;
    }

    /**
     * Check to make sure all PaymentRequestViews related to the passed in AutoClosePurchaseOrderView 
     * are in statuses that allow auto close. If so, return true. If not return false.
     * 
     * @param poAutoClose The AutoClosePurchaseOrderView used to get related PaymentRequestView(s) to check
     * @return whether the PaymentRequestView(s) are in a status that will should allow auto closing the related PO.
     */
    private boolean paymentRequestsStatusCanAutoClose(AutoClosePurchaseOrderView poAutoClose) {
        PurApRelatedViews relatedViews = new PurApRelatedViews(poAutoClose.getPurapDocumentIdentifier().toString(), poAutoClose.getAccountsPayablePurchasingDocumentLinkIdentifier());
        if (relatedViews.getRelatedPaymentRequestViews() != null) {
            for (PaymentRequestView paymentRequestView : relatedViews.getRelatedPaymentRequestViews()) {
                if (!CUPurapConstants.CUPaymentRequestStatuses.STATUSES_ALLOWING_AUTO_CLOSE.contains(paymentRequestView.getApplicationDocumentStatus())) {
                    return false;
                }
            }
        }
        return true;
	}

    @Override
    public void performPurchaseOrderFirstTransmitViaPrinting(String documentNumber, ByteArrayOutputStream baosPDF) {
        PurchaseOrderDocument po = getPurchaseOrderByDocumentNumber(documentNumber);
        String environment = kualiConfigurationService.getPropertyValueAsString(KFSConstants.ENVIRONMENT_KEY);
        Collection<String> generatePDFErrors = printService.generatePurchaseOrderPdf(po, baosPDF, environment, null);
        if (!generatePDFErrors.isEmpty()) {
            addStringErrorMessagesToMessageMap(PurapKeyConstants.ERROR_PURCHASE_ORDER_PDF, generatePDFErrors);
            throw new ValidationException("printing purchase order for first transmission failed");
        }
        if (ObjectUtils.isNotNull(po.getPurchaseOrderFirstTransmissionTimestamp())) {
            // should not call this method for first transmission if document has already been transmitted
            String errorMsg = "Method to perform first transmit was called on document (doc id " + documentNumber + ") with already filled in 'first transmit date'";
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
            Person systemUserPerson = getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
            purapWorkflowIntegrationService.takeAllActionsForGivenCriteria(po, "Action taken automatically as part of document initial print transmission by user " + GlobalVariables.getUserSession().getPerson().getName(), CUPurapConstants.PurchaseOrderStatuses.NODE_DOCUMENT_TRANSMISSION, systemUserPerson, KFSConstants.SYSTEM_USER);
        }
        po.setOverrideWorkflowButtons(Boolean.TRUE);
        if (!po.getApplicationDocumentStatus().equals(PurapConstants.PurchaseOrderStatuses.APPDOC_OPEN)) {
            attemptSetupOfInitialOpenOfDocument(po);
            if (shouldAdhocFyi(po.getRequisitionSourceCode())) {
                sendAdhocFyi(po);
            }
        }
        purapService.saveDocumentNoValidation(po);
    }

    private boolean shouldAdhocFyi(String reqSourceCode) {
        Collection<String> excludeList = new ArrayList<String>();
        if (SpringContext.getBean(ParameterService.class).parameterExists(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS)) {
            excludeList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS);
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
            try {
                po.updateAndSaveAppDocStatus(newStatusCode);
            }
            catch (WorkflowException e) {
                throw new RuntimeException("Error saving routing data while saving document with id " + po.getDocumentNumber(), e);
            }
        }
    }

    protected PurchaseOrderDocument generatePurchaseOrderFromRequisition(RequisitionDocument reqDocument) throws WorkflowException {
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
                    Note copyingNote = SpringContext.getBean(DocumentService.class).createNoteFromDocument(poDoc, note.getNoteText());
                    purapService.saveDocumentNoValidation(poDoc);
                    copyingNote.setNotePostedTimestamp(note.getNotePostedTimestamp());
                    copyingNote.setAuthorUniversalIdentifier(note.getAuthorUniversalIdentifier());
                    copyingNote.setNoteTopicText(note.getNoteTopicText());
                    Attachment originalAttachment = SpringContext.getBean(AttachmentService.class).getAttachmentByNoteId(note.getNoteIdentifier());
                    if (originalAttachment != null) {
                    	Attachment newAttachment = SpringContext.getBean(AttachmentService.class).createAttachment((PersistableBusinessObject)copyingNote, originalAttachment.getAttachmentFileName(), originalAttachment.getAttachmentMimeTypeCode(), originalAttachment.getAttachmentFileSize().intValue(), originalAttachment.getAttachmentContents(), originalAttachment.getAttachmentTypeCode());//new Attachment();

                    	if (ObjectUtils.isNotNull(originalAttachment) && ObjectUtils.isNotNull(newAttachment)) {
                    		copyingNote.addAttachment(newAttachment);
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
    
}
