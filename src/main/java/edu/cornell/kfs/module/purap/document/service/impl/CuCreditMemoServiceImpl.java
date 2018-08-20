package edu.cornell.kfs.module.purap.document.service.impl;


import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.CreditMemoStatuses;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.impl.CreditMemoServiceImpl;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuCreditMemoServiceImpl extends CreditMemoServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuCreditMemoServiceImpl.class);

    // KFSPTS-1891
    private CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;

    @Override
    public VendorCreditMemoDocument addHoldOnCreditMemo(VendorCreditMemoDocument cmDocument, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(cmDocument, note);
        cmDocument.addNote(noteObj);
        noteService.save(noteObj);

        // retrieve and save with hold indicator set to true
        VendorCreditMemoDocument cmDoc = getCreditMemoDocumentById(cmDocument.getPurapDocumentIdentifier());
        cmDoc.setHoldIndicator(true);
        cmDoc.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(cmDoc);
        //force reindexing
        reIndexDocument(cmDoc);

        // must also save it on the incoming document
        cmDocument.setHoldIndicator(true);
        cmDocument.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());

        //force reindexing
        reIndexDocument(cmDocument);
        return cmDoc;
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.CreditMemoService#removeHoldOnCreditMemo(org.kuali.kfs.module.purap.document.CreditMemoDocument,
     *      java.lang.String)
     */
    @Override
    public VendorCreditMemoDocument removeHoldOnCreditMemo(VendorCreditMemoDocument cmDocument, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(cmDocument, note);
        cmDocument.addNote(noteObj);
        noteService.save(noteObj);

        // retrieve and save with hold indicator set to false
        VendorCreditMemoDocument cmDoc = getCreditMemoDocumentById(cmDocument.getPurapDocumentIdentifier());
        cmDoc.setHoldIndicator(false);
        cmDoc.setLastActionPerformedByPersonId(null);
        purapService.saveDocumentNoValidation(cmDoc);
        //force reindexing
        reIndexDocument(cmDoc);

        // must also save it on the incoming document
        cmDocument.setHoldIndicator(false);
        cmDocument.setLastActionPerformedByPersonId(null);
        //force reindexing
        reIndexDocument(cmDocument);

        return cmDoc;
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.CreditMemoService#resetExtractedCreditMemo(org.kuali.kfs.module.purap.document.CreditMemoDocument,
     *      java.lang.String)
     */
    @Override
    public void resetExtractedCreditMemo(VendorCreditMemoDocument cmDocument, String note) {
        LOG.debug("resetExtractedCreditMemo() started");
        if (CreditMemoStatuses.CANCELLED_STATUSES.contains(cmDocument.getApplicationDocumentStatus())) {
            LOG.debug("resetExtractedCreditMemo() ended");
            return;
        }
        cmDocument.setExtractedTimestamp(null);
        cmDocument.setCreditMemoPaidTimestamp(null);

        Note noteObj;
        try {
            noteObj = documentService.createNoteFromDocument(cmDocument, note);
            cmDocument.addNote(noteObj);
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        purapService.saveDocumentNoValidation(cmDocument);
        //force reindexing
        reIndexDocument(cmDocument);

        if (LOG.isDebugEnabled()) {
            LOG.debug("resetExtractedCreditMemo() CM " + cmDocument.getPurapDocumentIdentifier() + " Cancelled Without Workflow");
        }
        LOG.debug("resetExtractedCreditMemo() ended");
    }

    protected String updateStatusByNode(String currentNodeName, VendorCreditMemoDocument cmDoc) {
        // update the status on the document

        String cancelledStatusCode = "";
        if (StringUtils.isEmpty(currentNodeName)) {
            cancelledStatusCode = PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_POST_AP_APPROVE;
        }
        else {
            cancelledStatusCode = CreditMemoStatuses.getCreditMemoAppDocDisapproveStatuses().get(currentNodeName);
        }

        if (StringUtils.isNotBlank(cancelledStatusCode)) {
            try {
                cmDoc.updateAndSaveAppDocStatus(cancelledStatusCode);
            }
            catch (WorkflowException we) {
                throw new RuntimeException("Unable to save the workflow document with document id: " + cmDoc.getDocumentNumber());
            }
            purapService.saveDocumentNoValidation(cmDoc);
            //force reindexing
            reIndexDocument(cmDoc);
            return cancelledStatusCode;
        }
        else {
            logAndThrowRuntimeException("No status found to set for document being disapproved in node '" + currentNodeName + "'");
        }
        return cancelledStatusCode;
    }


    @Override
    public void markPaid(VendorCreditMemoDocument cm, Date processDate) {
        LOG.debug("markPaid() started");

        cm.setCreditMemoPaidTimestamp(new Timestamp(processDate.getTime()));
        purapService.saveDocumentNoValidation(cm);
        //force reindexing
        reIndexDocument(cm);
    }

    /**
     * KFSUPGRADE-508 : this is happened in multi-node env. also see KFSUPGRADE_347
      * This method is being added to handle calls to perform re-indexing of documents following change events performed on the documents.  This is necessary to correct problems
      * with searches not returning accurate results due to changes being made to documents, but those changes not be indexed.
      * 
      * @param document - The document to be re-indexed.
      */
     private void reIndexDocument(AccountsPayableDocument document) {
         //force reindexing
          final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();

         documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());

     }
     
     /**
      * @see org.kuali.kfs.module.purap.document.service.CreditMemoCreateService#populateDocumentAfterInit(org.kuali.kfs.module.purap.document.CreditMemoDocument)
      */
     @Override
     public void populateDocumentAfterInit(VendorCreditMemoDocument cmDocument) {

         // make a call to search for expired/closed accounts
         HashMap<String, ExpiredOrClosedAccountEntry> expiredOrClosedAccountList = accountsPayableService.getExpiredOrClosedAccountList(cmDocument);

         if (cmDocument.isSourceDocumentPaymentRequest()) {
             populateDocumentFromPreq(cmDocument, expiredOrClosedAccountList);
         }
         else if (cmDocument.isSourceDocumentPurchaseOrder()) {
             populateDocumentFromPO(cmDocument, expiredOrClosedAccountList);
         }
         else {
             populateDocumentFromVendor(cmDocument);
         }

         // KFSPTS-1891
         
         VendorDetail vendorDetail = vendorService.getVendorDetail(cmDocument.getVendorHeaderGeneratedIdentifier(), cmDocument.getVendorDetailAssignedIdentifier());
         if ( ObjectUtils.isNotNull(vendorDetail)
                 && ObjectUtils.isNotNull(vendorDetail.getExtension()) ) {
             if ( vendorDetail.getExtension() instanceof VendorDetailExtension
                     && StringUtils.isNotBlank( ((VendorDetailExtension)vendorDetail.getExtension()).getDefaultB2BPaymentMethodCode() ) ) {
             	((CuVendorCreditMemoDocument)cmDocument).setPaymentMethodCode(
                         ((VendorDetailExtension)vendorDetail.getExtension()).getDefaultB2BPaymentMethodCode() );
             }
         }

         populateDocumentDescription(cmDocument);

         // write a note for expired/closed accounts if any exist and add a message stating there were expired/closed accounts at the
         // top of the document
         accountsPayableService.generateExpiredOrClosedAccountNote(cmDocument, expiredOrClosedAccountList);

         // set indicator so a message is displayed for accounts that were replaced due to expired/closed status
         if (ObjectUtils.isNotNull(expiredOrClosedAccountList) && !expiredOrClosedAccountList.isEmpty()) {
             cmDocument.setContinuationAccountIndicator(true);
         }

    }
     
 	public CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
 		return paymentMethodGeneralLedgerPendingEntryService;
 	}

 	public void setPaymentMethodGeneralLedgerPendingEntryService(
 			CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
 		this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
 	}

}
