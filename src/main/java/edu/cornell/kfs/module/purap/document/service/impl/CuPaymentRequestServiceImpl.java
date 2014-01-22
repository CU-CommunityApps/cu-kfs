package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.businessobject.AutoApproveExclude;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuPaymentRequestServiceImpl extends PaymentRequestServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPaymentRequestServiceImpl.class);

    protected boolean isEligibleForAutoApproval(PaymentRequestDocument document, KualiDecimal defaultMinimumLimit) {
        // Check if vendor is foreign.
        if (document.getVendorDetail().getVendorHeader().getVendorForeignIndicator().booleanValue()) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] skipped due to a Foreign Vendor.");
            }
            return false;
        }

        // check to make sure the payment request isn't scheduled to stop in tax review.
        if (purapWorkflowIntegrationService.willDocumentStopAtGivenFutureRouteNode(document, PaymentRequestStatuses.NODE_VENDOR_TAX_REVIEW)) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] skipped due to requiring Tax Review.");
            }
            return false;
        }

        // Change to not auto approve if positive approval required indicator set to Yes
        if (document.isPaymentRequestPositiveApprovalIndicator()) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] skipped due to a Positive Approval Required Indicator set to Yes.");
            }
            return false;
        }

        // This minimum will be set to the minimum limit derived from all
        // accounting lines on the document. If no limit is determined, the
        // default will be used.
        KualiDecimal minimumAmount = null;

        // Iterate all source accounting lines on the document, deriving a
        // minimum limit from each according to chart, chart and account, and
        // chart and organization.
        for (SourceAccountingLine line : purapAccountingService.generateSummary(document.getItems())) {
            // check to make sure the account is in the auto approve exclusion list
            Map<String, Object> autoApproveMap = new HashMap<String, Object>();
            autoApproveMap.put("chartOfAccountsCode", line.getChartOfAccountsCode());
            autoApproveMap.put("accountNumber", line.getAccountNumber());
            autoApproveMap.put("active", true);
            AutoApproveExclude autoApproveExclude = businessObjectService.findByPrimaryKey(AutoApproveExclude.class, autoApproveMap);
            if (autoApproveExclude != null) {
                if ( LOG.isInfoEnabled() ) {
                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] skipped due to source accounting line " + line.getSequenceNumber() +
                        " using Chart/Account [" + line.getChartOfAccountsCode() + "-" + line.getAccountNumber() +
                        "], which is excluded in the Auto Approve Exclusions table.");
                }
                return false;
            }

            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChart(line.getChartOfAccountsCode()), minimumAmount);
            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChartAndAccount(line.getChartOfAccountsCode(), line.getAccountNumber()), minimumAmount);
            minimumAmount = getMinimumLimitAmount(negativePaymentRequestApprovalLimitService.findByChartAndOrganization(line.getChartOfAccountsCode(), line.getOrganizationReferenceId()), minimumAmount);
        }

        // If Receiving required is set, it's not needed to check the negative payment request approval limit
        // KFSUPGRADE-362
//        if (document.isReceivingDocumentRequiredIndicator()) {
//            if ( LOG.isInfoEnabled() ) {
//                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] auto-approved (ignored dollar limit) due to Receiving Document Required Indicator set to Yes.");
//            }
//            return true;
//        }

        // If no limit was found or the default is less than the limit, the default limit is used.
        if (ObjectUtils.isNull(minimumAmount) || defaultMinimumLimit.compareTo(minimumAmount) < 0) {
            minimumAmount = defaultMinimumLimit;
        }

        // The document is eligible for auto-approval if the document total is below the limit.
        if (document.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount().isLessThan(minimumAmount)) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] auto-approved due to document Total [" +
                        document.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount() + "] being less than " +
                        (minimumAmount == defaultMinimumLimit ? "Default Auto-Approval Limit " : "Configured Auto-Approval Limit ") +
                        "of " + (minimumAmount == null ? "null" : minimumAmount.toString()) + ".");
            }
            return true;
        }

        if ( LOG.isInfoEnabled() ) {
            LOG.info(" -- PayReq ["+document.getDocumentNumber()+"] skipped due to document Total [" +
                    document.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount() + "] being greater than " +
                    (minimumAmount == defaultMinimumLimit ? "Default Auto-Approval Limit " : "Configured Auto-Approval Limit ") +
                    "of " + (minimumAmount == null ? "null" : minimumAmount.toString()) + ".");
        }

        return false;
    }

    @Override
    @NonTransactional
    public void removeIneligibleAdditionalCharges(PaymentRequestDocument document) {

        List<PaymentRequestItem> itemsToRemove = new ArrayList<PaymentRequestItem>();

        for (PaymentRequestItem item : (List<PaymentRequestItem>) document.getItems()) {

        	// KFSUPGRADE-473
            //if no extended price or purchase order item unit price, and its an order discount or trade in, remove
            if ((ObjectUtils.isNull(item.getPurchaseOrderItemUnitPrice()) && ObjectUtils.isNull(item.getExtendedPrice())) &&
                    (ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(item.getItemTypeCode()) || ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(item.getItemTypeCode())) ){            
                itemsToRemove.add(item);
                continue;
            }

            // if a payment terms discount exists but not set on teh doc, remove
            if (StringUtils.equals(item.getItemTypeCode(), PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                PaymentTermType pt = document.getVendorPaymentTerms();
                if ((pt != null) && (pt.getVendorPaymentTermsPercent() != null) && (BigDecimal.ZERO.compareTo(pt.getVendorPaymentTermsPercent()) != 0)) {
                    // discount ok
                }
                else {
                    // remove discount
                    itemsToRemove.add(item);
                }
                continue;
            }

        }

        // remove items marked for removal
        for (PaymentRequestItem item : itemsToRemove) {
            document.getItems().remove(item);
        }
    }

    @Override
    @NonTransactional
    public PaymentRequestDocument addHoldOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

        return document;
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#removeHoldOnPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    @Override
    @NonTransactional
    public PaymentRequestDocument removeHoldOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(false);
        document.setLastActionPerformedByPersonId(null);
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

        return document;
    }

    @Override
    @NonTransactional
    public void requestCancelOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setPaymentRequestedCancelIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        document.setAccountsPayableRequestCancelIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#removeHoldOnPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    @Override
    @NonTransactional
    public void removeRequestCancelOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        clearRequestCancelFields(document);

        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

    }

    @Override
    @NonTransactional
    public void cancelExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("cancelExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("cancelExtractedPaymentRequest() ended");
            return;
        }

        try {
            Note cancelNote = documentService.createNoteFromDocument(paymentRequest, note);
            paymentRequest.addNote(cancelNote);
            noteService.save(cancelNote);
        }
        catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE, e);
        }

        // cancel extracted should not reopen PO
        paymentRequest.setReopenPurchaseOrderIndicator(false);

        getAccountsPayableService().cancelAccountsPayableDocument(paymentRequest, ""); // Performs save, so
        // no explicit save
        // is necessary
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelExtractedPaymentRequest() PREQ " + paymentRequest.getPurapDocumentIdentifier() + " Cancelled Without Workflow");
            LOG.debug("cancelExtractedPaymentRequest() ended");
        }
        //force reindexing
        reIndexDocument(paymentRequest);
   }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#resetExtractedPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument,
     *      java.lang.String)
     */
    @Override
    @NonTransactional
    public void resetExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("resetExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("resetExtractedPaymentRequest() ended");
            return;
        }
        paymentRequest.setExtractedTimestamp(null);
        paymentRequest.setPaymentPaidTimestamp(null);
        String noteText = "This Payment Request is being reset for extraction by PDP " + note;
        try {
            Note resetNote = documentService.createNoteFromDocument(paymentRequest, noteText);
            paymentRequest.addNote(resetNote);
            noteService.save(resetNote);
        }
        catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE + " " + e);
        }
        purapService.saveDocumentNoValidation(paymentRequest);
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetExtractedPaymentRequest() PREQ " + paymentRequest.getPurapDocumentIdentifier() + " Reset from Extracted status");
        }
        //force reindexing
        reIndexDocument(paymentRequest);
    }

    @Override
    @NonTransactional
    public void markPaid(PaymentRequestDocument pr, Date processDate) {
        LOG.debug("markPaid() started");

        pr.setPaymentPaidTimestamp(new Timestamp(processDate.getTime()));
        purapService.saveDocumentNoValidation(pr);
        //force reindexing
        reIndexDocument(pr);
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
