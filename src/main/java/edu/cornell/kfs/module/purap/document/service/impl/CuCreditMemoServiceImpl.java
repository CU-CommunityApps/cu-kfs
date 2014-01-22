package edu.cornell.kfs.module.purap.document.service.impl;


import java.sql.Date;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.CreditMemoStatuses;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.dataaccess.CreditMemoDao;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.impl.CreditMemoServiceImpl;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;

public class CuCreditMemoServiceImpl extends CreditMemoServiceImpl {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuCreditMemoServiceImpl.class);
    private AccountsPayableService accountsPayableService;
    private CreditMemoDao creditMemoDao;
    private DataDictionaryService dataDictionaryService;
    private DocumentService documentService;
    private ConfigurationService kualiConfigurationService;
    private NoteService noteService;
    private PaymentRequestService paymentRequestService;
    private PurapAccountingService purapAccountingService;
    private PurapGeneralLedgerService purapGeneralLedgerService;
    private PurapService purapService;
    private PurchaseOrderService purchaseOrderService;
    private VendorService vendorService;
    private WorkflowDocumentService workflowDocumentService;


    public void setAccountsPayableService(AccountsPayableService accountsPayableService) {
        this.accountsPayableService = accountsPayableService;
        super.setAccountsPayableService(accountsPayableService);
    }

    public void setCreditMemoDao(CreditMemoDao creditMemoDao) {
        this.creditMemoDao = creditMemoDao;
        super.setCreditMemoDao(creditMemoDao);
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
        super.setDataDictionaryService(dataDictionaryService);
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
        super.setDocumentService(documentService);
    }

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
        super.setConfigurationService(kualiConfigurationService);
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
        super.setNoteService(noteService);
    }

    public void setPaymentRequestService(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
        super.setPaymentRequestService(paymentRequestService);
    }

    public void setPurapAccountingService(PurapAccountingService purapAccountingService) {
        this.purapAccountingService = purapAccountingService;
        super.setPurapAccountingService(purapAccountingService);
    }

    public void setPurapGeneralLedgerService(PurapGeneralLedgerService purapGeneralLedgerService) {
        this.purapGeneralLedgerService = purapGeneralLedgerService;
        super.setPurapGeneralLedgerService(purapGeneralLedgerService);
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
        super.setPurapService(purapService);
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
        super.setPurchaseOrderService(purchaseOrderService);
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
        super.setVendorService(vendorService);
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService){
        this.workflowDocumentService = workflowDocumentService;
        super.setWorkflowDocumentService(workflowDocumentService);
    }

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

}
