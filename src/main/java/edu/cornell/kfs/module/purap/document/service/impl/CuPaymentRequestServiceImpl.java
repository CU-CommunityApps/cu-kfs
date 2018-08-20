package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.InfrastructureException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;
import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import edu.cornell.kfs.sys.service.CUBankService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuPaymentRequestServiceImpl extends PaymentRequestServiceImpl implements CuPaymentRequestService {
    private static final Logger LOG = LogManager.getLogger(CuPaymentRequestServiceImpl.class);
    
    private CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;


    @Override
    @NonTransactional
    public void removeIneligibleAdditionalCharges(PaymentRequestDocument document) {
        List<PaymentRequestItem> itemsToRemove = new ArrayList<>();

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
            }
        }

        // remove items marked for removal
        for (PaymentRequestItem item : itemsToRemove) {
            document.getItems().remove(item);
        }
    }

    @Override
    @NonTransactional
    public PaymentRequestDocument addHoldOnPaymentRequest(PaymentRequestDocument document, String note) {
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
    public PaymentRequestDocument removeHoldOnPaymentRequest(PaymentRequestDocument document, String note) {
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
    public void requestCancelOnPaymentRequest(PaymentRequestDocument document, String note) {
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
    public void removeRequestCancelOnPaymentRequest(PaymentRequestDocument document, String note) {
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

        accountsPayableService.cancelAccountsPayableDocument(paymentRequest, ""); // Performs save, so
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
    
    @Override
    protected void addTaxItem(PaymentRequestDocument preq, String itemTypeCode, BigDecimal taxableAmount) {

        PurApItem taxItem;

        try {
            taxItem = (PurApItem) preq.getItemClass().newInstance();
        } catch (IllegalAccessException e) {
            throw new InfrastructureException("Unable to access itemClass", e);
        } catch (InstantiationException e) {
            throw new InfrastructureException("Unable to instantiate itemClass", e);
        }

        // add item to preq before adding the accounting line
        taxItem.setItemTypeCode(itemTypeCode);
        preq.addItem(taxItem);

        // generate and add tax accounting line
        PurApAccountingLine taxLine = addTaxAccountingLine(taxItem, taxableAmount);

        // set extended price amount as now it's calculated when accounting line is generated
        taxItem.setItemUnitPrice(taxLine.getAmount().bigDecimalValue());
        taxItem.setExtendedPrice(taxLine.getAmount());
        // KFSPTS-1891.  added to fix validation required field error. especially after calculate tax
        if (taxLine.getAccountLinePercent() == null) {
        		taxLine.setAccountLinePercent(BigDecimal.ZERO);
        	}

        // use item type description as the item description
        ItemType itemType = new ItemType();
        itemType.setItemTypeCode(itemTypeCode);
        itemType = (ItemType) businessObjectService.retrieve(itemType);
        taxItem.setItemType(itemType);
        taxItem.setItemDescription(itemType.getItemTypeDescription());
    }
    
    @Override
    @NonTransactional
    public void populatePaymentRequest(PaymentRequestDocument paymentRequestDocument) {
    	super.populatePaymentRequest(paymentRequestDocument);
    	
    	//KFSUPGRADE-779
    	//reset bank code
    	 paymentRequestDocument.setBankCode(null);
         paymentRequestDocument.setBank(null);
         
        // set bank code to default bank code in the system parameter
        Bank defaultBank = null;
        if (StringUtils.equals(PaymentMethod.PM_CODE_WIRE, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode()) || StringUtils.equals(PaymentMethod.PM_CODE_FOREIGN_DRAFT, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode())) {
        	defaultBank = SpringContext.getBean(CUBankService.class).getDefaultBankByDocType(CuPaymentRequestDocument.DOCUMENT_TYPE_NON_CHECK);
        } else if (!StringUtils.equals(PaymentMethod.PM_CODE_INTERNAL_BILLING, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode())) {
            defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(PaymentRequestDocument.class);
        }
        if (defaultBank != null) {
            paymentRequestDocument.setBankCode(defaultBank.getBankCode());
            paymentRequestDocument.setBank(defaultBank);
        }
    }
    
    @Override
    @NonTransactional
    public void changeVendor(PaymentRequestDocument preq, Integer headerId, Integer detailId) {
    	super.changeVendor(preq, headerId, detailId);
    	// KFSPTS-1891
        if ( preq instanceof PaymentRequestDocument ) {
            VendorDetail vdDetail = vendorService.getVendorDetail(headerId, detailId);
            if (vdDetail != null
                    && ObjectUtils.isNotNull(vdDetail.getExtension()) ) {
                if ( vdDetail.getExtension() instanceof VendorDetailExtension
                        && StringUtils.isNotBlank( ((VendorDetailExtension)vdDetail.getExtension()).getDefaultB2BPaymentMethodCode() ) ) {
                    ((CuPaymentRequestDocument)preq).setPaymentMethodCode(
                            ((VendorDetailExtension)vdDetail.getExtension()).getDefaultB2BPaymentMethodCode() );
                }
            }
        }
    }
    
    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        return paymentMethodGeneralLedgerPendingEntryService;
    }

    public void setPaymentMethodGeneralLedgerPendingEntryService(CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
        this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
    }

    public void clearTax(PaymentRequestDocument document) {
        // remove all existing tax items added by previous calculation
        removeTaxItems(document);
        // reset values
        document.setTaxClassificationCode(null);
        document.setTaxFederalPercent(null);
        document.setTaxStatePercent(null);
        document.setTaxCountryCode(null);
        document.setTaxNQIId(null);

        document.setTaxForeignSourceIndicator(false);
        document.setTaxExemptTreatyIndicator(false);
        document.setTaxOtherExemptIndicator(false);
        document.setTaxGrossUpIndicator(false);
        document.setTaxUSAIDPerDiemIndicator(false);
        document.setTaxSpecialW4Amount(null);

    }
 
    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#getPaymentRequestsByStatusAndPurchaseOrderId(java.lang.String, java.lang.Integer)
     */
    @Override
    public Map <String, String> getPaymentRequestsByStatusAndPurchaseOrderId(String applicationDocumentStatus, Integer purchaseOrderId) {
    	Map<String, String> paymentRequestResults = new HashMap<>();
    	paymentRequestResults.put("hasInProcess", "N");
    	paymentRequestResults.put("checkInProcess", "N");

    	final int numInProcess = ((CuPaymentRequestDao) paymentRequestDao).countDocumentsByPurchaseOrderId(purchaseOrderId, applicationDocumentStatus);
    	final int numTotal = ((CuPaymentRequestDao)paymentRequestDao).countDocumentsByPurchaseOrderId(purchaseOrderId, "");

    	if (numInProcess > 0) {
    	    paymentRequestResults.put("hasInProcess", "Y");
    	}

    	if (numTotal > 0 && numTotal != numInProcess) {
    	    paymentRequestResults.put("checkInProcess", "Y");
    	}
    	
    	return paymentRequestResults;
    }
    
    /**
     * Overridden to use the Net Due Number for the relevant calculations if the Discount Due Number is zero.
     * 
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#calculatePayDate(java.sql.Date,
     *      org.kuali.kfs.vnd.businessobject.PaymentTermType)
     */
    @Override
    @NonTransactional
    public java.sql.Date calculatePayDate(Date invoiceDate, PaymentTermType terms) {
        LOG.debug("calculatePayDate() started");
        // calculate the invoice + processed calendar
        Calendar invoicedDateCalendar = dateTimeService.getCalendar(invoiceDate);
        Calendar processedDateCalendar = dateTimeService.getCurrentCalendar();

        // add default number of days to processed
        String defaultDays = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PREQ_PAY_DATE_DEFAULT_NUMBER_OF_DAYS);
        processedDateCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(defaultDays));

        if (ObjectUtils.isNull(terms) || StringUtils.isEmpty(terms.getVendorPaymentTermsCode())) {
            invoicedDateCalendar.add(Calendar.DAY_OF_MONTH, PurapConstants.PREQ_PAY_DATE_EMPTY_TERMS_DEFAULT_DAYS);
            return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
        }

        // Retrieve pay date variation parameter (currently defined as 2).  See parameter description for explanation of it's use.
        String payDateVariance = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PREQ_PAY_DATE_VARIANCE);
        Integer payDateVarianceInt = Integer.valueOf(payDateVariance);

        Integer discountDueNumber = terms.getVendorDiscountDueNumber();
        Integer netDueNumber = terms.getVendorNetDueNumber();
        // ==== CU Customization: If Discount Due Number is zero, use the Net Due Number instead. ====
        if (ObjectUtils.isNotNull(discountDueNumber) && !discountDueNumber.equals(Integer.valueOf(0))) {
            // Decrease discount due number by the pay date variance
            discountDueNumber -= payDateVarianceInt;
            if (discountDueNumber < 0) {
                discountDueNumber = 0;
            }
            String discountDueTypeDescription = terms.getVendorDiscountDueTypeDescription();
            paymentTermsDateCalculation(discountDueTypeDescription, invoicedDateCalendar, discountDueNumber);
        }
        else if (ObjectUtils.isNotNull(netDueNumber)) {
            // Decrease net due number by the pay date variance
            netDueNumber -= payDateVarianceInt;
            if (netDueNumber < 0) {
                netDueNumber = 0;
            }
            String netDueTypeDescription = terms.getVendorNetDueTypeDescription();
            paymentTermsDateCalculation(netDueTypeDescription, invoicedDateCalendar, netDueNumber);
        }
        else {
            throw new RuntimeException("Neither discount or net number were specified for this payment terms type");
        }

        // return the later date
        return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
    }

    /**
     * This implementation returns the object ID of the payment request document itself.
     * 
     * @see edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService#getPaymentRequestNoteTargetObjectId(java.lang.String)
     */
    @Override
    public String getPaymentRequestNoteTargetObjectId(String documentNumber) {
        return ((CuPaymentRequestDao) paymentRequestDao).getObjectIdByPaymentRequestDocumentNumber(documentNumber);
    }

    /**
     * This implementation uses the "DEFAULT_PURCHASE_ORDER_POS_APRVL_LMT_FOR_PREQ" parameter
     * to get the cutoff value for which the PO amount can allow for automatic PREQ approval.
     * 
     * @see edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService#isPurchaseOrderWithinAmountLimitForPaymentRequestAutoApprove(
     * org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    @Override
    public boolean purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(PaymentRequestDocument document) {
        PurchaseOrderDocument po = document.getPurchaseOrderDocument();
        String amountParameterValue = parameterService.getParameterValueAsString(
                PaymentRequestDocument.class, CUPurapParameterConstants.DEFAULT_PURCHASE_ORDER_POS_APRVL_LMT_FOR_PREQ);
        KualiDecimal amountLimit = new KualiDecimal(amountParameterValue);
        boolean withinLimit = po.getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount().isLessThan(amountLimit);
        String messagePattern = createLogMessagePatternForPOAmountLimitResult(withinLimit);
        LOG.info(MessageFormat.format(messagePattern, document.getDocumentNumber(), amountLimit.toString()));
        return withinLimit;
    }

    protected String createLogMessagePatternForPOAmountLimitResult(boolean withinLimit) {
        if (withinLimit) {
            return "PayReq {0} has the potential for auto-approval because the amount on the associated PO is below the limit of {1}";
        }
        return "PayReq {0} cannot be auto-approved because the amount on the associated PO matches or exceeds the limit of {1}";
    }

    /**
     * Overridden to also require that the associated PO's amount must be within auto-approval limits.
     * 
     * @see org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl#isEligibleForAutoApproval(
     * org.kuali.kfs.module.purap.document.PaymentRequestDocument, org.kuali.rice.core.api.util.type.KualiDecimal)
     */
    @Override
    protected boolean isEligibleForAutoApproval(PaymentRequestDocument document, KualiDecimal defaultMinimumLimit) {
        return purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(document)
                && super.isEligibleForAutoApproval(document, defaultMinimumLimit);
    }

}
