package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestNoteDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.ShippingTitle;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.InfrastructureException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;
import edu.cornell.kfs.module.purap.document.service.CuPaymentRequestService;
import org.springframework.util.AutoPopulatingList;

import static edu.cornell.kfs.module.purap.CUPurapConstants.Payflow.PAYFLOW;
import static edu.cornell.kfs.pdp.CUPdpConstants.PdpDocumentTypes.PAYMENT_REQUEST;
import static org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes.ITEM_TYPE_FREIGHT_CODE;
import static org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE;
import static org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes.ITEM_TYPE_SHIP_AND_HAND_CODE;

public class CuPaymentRequestServiceImpl extends PaymentRequestServiceImpl implements CuPaymentRequestService {
    private static final Logger LOG = LogManager.getLogger();
    
    private CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;


    @Override
    public void removeIneligibleAdditionalCharges(final PaymentRequestDocument document) {
        final List<PaymentRequestItem> itemsToRemove = new ArrayList<>();

        for (final PaymentRequestItem item : (List<PaymentRequestItem>) document.getItems()) {
        	// KFSUPGRADE-473
            //if no extended price or purchase order item unit price, and its an order discount or trade in, remove
			if ((ObjectUtils.isNull(item.getPurchaseOrderItemUnitPrice())
					&& ObjectUtils.isNull(item.getExtendedPrice()))
					&& (ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(item.getItemTypeCode())
							|| ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(item.getItemTypeCode()))) {
                itemsToRemove.add(item);
                continue;
            }

            // if a payment terms discount exists but not set on teh doc, remove
            if (StringUtils.equals(item.getItemTypeCode(), PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                final PaymentTermType pt = document.getVendorPaymentTerms();
                if ((pt == null) || (pt.getVendorPaymentTermsPercent() == null)
                        || (BigDecimal.ZERO.compareTo(pt.getVendorPaymentTermsPercent()) == 0)) {
                    // remove discount
                    itemsToRemove.add(item);
                }
            }
        }

        // remove items marked for removal
        for (final PaymentRequestItem item : itemsToRemove) {
            document.getItems().remove(item);
        }
    }

    @Override
    public PaymentRequestDocument addHoldOnPaymentRequest(final PaymentRequestDocument document, final String note) {
        final Note noteObj = documentService.createNoteFromDocument(document, note);
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
    public PaymentRequestDocument removeHoldOnPaymentRequest(final PaymentRequestDocument document, final String note) {
        final Note noteObj = documentService.createNoteFromDocument(document, note);
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
    public void requestCancelOnPaymentRequest(final PaymentRequestDocument document, final String note) {
        final Note noteObj = documentService.createNoteFromDocument(document, note);
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
    public void removeRequestCancelOnPaymentRequest(final PaymentRequestDocument document, final String note) {
        final Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        clearRequestCancelFields(document);
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

    }

    @Override
    public void cancelExtractedPaymentRequest(final PaymentRequestDocument paymentRequest, final String note) {
        LOG.debug("cancelExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("cancelExtractedPaymentRequest() ended");
            return;
        }

        try {
            final Note cancelNote = documentService.createNoteFromDocument(paymentRequest, note);
            paymentRequest.addNote(cancelNote);
            noteService.save(cancelNote);
        } catch (final Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE, e);
        }

        // cancel extracted should not reopen PO
        paymentRequest.setReopenPurchaseOrderIndicator(false);

        // Performs save, so no explicit save is necessary
        accountsPayableService.cancelAccountsPayableDocument(paymentRequest, ""); 

        LOG.debug("cancelExtractedPaymentRequest() PREQ {} Cancelled Without Workflow",
                paymentRequest::getPurapDocumentIdentifier);
        LOG.debug("cancelExtractedPaymentRequest() ended");

        //force reindexing
        reIndexDocument(paymentRequest);
   }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#resetExtractedPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument,
     *      java.lang.String)
     */
    @Override
    public void resetExtractedPaymentRequest(final PaymentRequestDocument paymentRequest, final String note) {
        LOG.debug("resetExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("resetExtractedPaymentRequest() ended");
            return;
        }
        paymentRequest.setExtractedTimestamp(null);
        paymentRequest.setPaymentPaidTimestamp(null);
        final String noteText = "This Payment Request is being reset for extraction by PDP " + note;
        try {
            final Note resetNote = documentService.createNoteFromDocument(paymentRequest, noteText);
            paymentRequest.addNote(resetNote);
            noteService.save(resetNote);
        } catch (final Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE + " " + e);
        }
        purapService.saveDocumentNoValidation(paymentRequest);
        LOG.debug(
                "resetExtractedPaymentRequest() PREQ {} Reset from Extracted status",
                paymentRequest::getPurapDocumentIdentifier
        );
        //force reindexing
        reIndexDocument(paymentRequest);
    }

    @Override
    public void markPaid(final PaymentRequestDocument pr, final Date processDate) {
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
    protected void addTaxItem(
            final PaymentRequestDocument preq, 
            final String itemTypeCode, 
            final BigDecimal taxableAmount) {
        final PurApItem taxItem;

        try {
        	taxItem = (PurApItem) preq.getItemClass().getDeclaredConstructor().newInstance();
        } catch (final IllegalAccessException e) {
            throw new InfrastructureException("Unable to access itemClass", e);
        } catch (final ReflectiveOperationException e) {
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
    public void changeVendor(final PaymentRequestDocument preq, final Integer headerId, final Integer detailId) {
        super.changeVendor(preq, headerId, detailId);
        
        if (preq instanceof PaymentRequestDocument) {
            final VendorDetail vdDetail = vendorService.getVendorDetail(headerId, detailId);
            if (vdDetail != null
                    && StringUtils.isNotBlank(vdDetail.getDefaultPaymentMethodCode())) {
                preq.setPaymentMethodCode(vdDetail.getDefaultPaymentMethodCode());
                preq.refreshReferenceObject("paymentMethod");
            }
        }
    }
    
    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        return paymentMethodGeneralLedgerPendingEntryService;
    }

    public void setPaymentMethodGeneralLedgerPendingEntryService(CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
        this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
    }

    @Override
    public void clearTax(final PaymentRequestDocument document) {
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
	public Map<String, String> getPaymentRequestsByStatusAndPurchaseOrderId(final String applicationDocumentStatus,
			final Integer purchaseOrderId) {
    	final Map<String, String> paymentRequestResults = new HashMap<>();
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
    public java.sql.Date calculatePayDate(final Date invoiceDate, final PaymentTermType terms) {
        LOG.debug("calculatePayDate() started");
        // calculate the invoice + processed calendar
        LocalDate invoicedLocalDate = dateTimeService.getLocalDate(invoiceDate);
        LocalDate processedLocalDate = dateTimeService.getLocalDateNow();

        // add default number of days to processed
		final String defaultDays = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
				PurapParameterConstants.PURAP_PREQ_PAY_DATE_DEFAULT_NUMBER_OF_DAYS);
        processedLocalDate = processedLocalDate.plusDays(Integer.parseInt(defaultDays));

        if (ObjectUtils.isNull(terms) || StringUtils.isEmpty(terms.getVendorPaymentTermsCode())) {
            final String defaultDaysFromInvoiceDate =
                                        parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                                        PurapParameterConstants.PAY_DATE_DEFAULT);
            invoicedLocalDate = invoicedLocalDate.plusDays(Integer.parseInt(defaultDaysFromInvoiceDate));
            return returnLaterDate(invoicedLocalDate, processedLocalDate);
        }

        // Retrieve pay date variation parameter (currently defined as 2).  See parameter description for explanation
        // of it's use.
		final String payDateVariance = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
				PurapParameterConstants.PURAP_PREQ_PAY_DATE_VARIANCE);
		final int payDateVarianceInt = Integer.parseInt(payDateVariance);

        Integer discountDueNumber = terms.getVendorDiscountDueNumber();
        Integer netDueNumber = terms.getVendorNetDueNumber();
        // ==== CU Customization: If Discount Due Number is zero, use the Net Due Number instead. ====
        if (ObjectUtils.isNotNull(discountDueNumber) && !discountDueNumber.equals(Integer.valueOf(0))) {
            // Decrease discount due number by the pay date variance
            discountDueNumber -= payDateVarianceInt;
            if (discountDueNumber < 0) {
                discountDueNumber = 0;
            }
            final String discountDueTypeDescription = terms.getVendorDiscountDueTypeDescription();
            invoicedLocalDate = paymentTermsDateCalculation(
                    discountDueTypeDescription,
                    invoicedLocalDate,
                    discountDueNumber
            );
        } else if (ObjectUtils.isNotNull(netDueNumber)) {
            // Decrease net due number by the pay date variance
            netDueNumber -= payDateVarianceInt;
            if (netDueNumber < 0) {
                netDueNumber = 0;
            }
            final String netDueTypeDescription = terms.getVendorNetDueTypeDescription();
            invoicedLocalDate = paymentTermsDateCalculation(netDueTypeDescription, invoicedLocalDate, netDueNumber);
        }
        else {
            throw new RuntimeException("Neither discount or net number were specified for this payment terms type");
        }

        // return the later date
        return returnLaterDate(invoicedLocalDate, processedLocalDate);
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
        boolean withinLimit = po.getDocumentHeader().getFinancialDocumentTotalAmount().isLessThan(amountLimit);
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
     * Checks if PREQ is of type PRNC as these should not be auto-approved.
     * 
     * @param paymentRequestDocument
     * @return true if PRNC, false otherwise
     */
    protected boolean isPRNCDocument(PaymentRequestDocument paymentRequestDocument) {
        String paymentMethodCode = ((CuPaymentRequestDocument) paymentRequestDocument).getPaymentMethodCode();
        boolean isPRNC = StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE, paymentMethodCode)
                || StringUtils.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT, paymentMethodCode);
        LOG.info(" -- PayReq [" + paymentRequestDocument.getDocumentNumber() + "] skipped as it is of type PRNC.");
        return isPRNC;
    }

    /**
     * Overridden to also require that the associated PO's amount must be within auto-approval limits.
     * 
     * @see org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl#isEligibleForAutoApproval(
     * org.kuali.kfs.module.purap.document.PaymentRequestDocument, org.kuali.kfs.core.api.util.type.KualiDecimal)
     */
    @Override
    protected boolean isEligibleForAutoApproval(PaymentRequestDocument document, KualiDecimal defaultMinimumLimit) {
        return !isPRNCDocument(document) && purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(document)
                && super.isEligibleForAutoApproval(document, defaultMinimumLimit);
    }

    public PaymentRequestDocument createPaymentRequestDocumentFromDto(PaymentRequestDto preqDto) {

        LOG.info("Creating PaymentRequestDocument from DTO for PO: {}", preqDto.getPoNumber());

        try {

            PurchaseOrderDocument poDoc = purchaseOrderService.getCurrentPurchaseOrder(preqDto.getPoNumber());
            if (ObjectUtils.isNull(poDoc)) {
                throw new Exception("Purchase Order Document not found for PO #" + preqDto.getPoNumber());
            }

            CuPaymentRequestDocument preqDoc = generateNewPaymentRequestDocument(poDoc, preqDto);
            Document savedPreqDoc = documentService.saveDocument(preqDoc);
            return (PaymentRequestDocument) savedPreqDoc;

        } catch (Exception e) {
            LOG.error("Error creating PaymentRequestDocument from DTO", e);
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();

            for (Map.Entry<String, AutoPopulatingList<ErrorMessage>> entry : errorMessages.entrySet()) {
                AutoPopulatingList<ErrorMessage> errors = entry.getValue();

                for (ErrorMessage error : errors) {
                    LOG.error("createPaymentRequestDocumentFromDto, error {} message {}", entry.getKey(), error.toString());
                }
            }

            throw new RuntimeException("createPaymentRequestDocumentFromDto, Failed to create PREQ", e);
        }
    }

    private CuPaymentRequestDocument generateNewPaymentRequestDocument(PurchaseOrderDocument poDoc, PaymentRequestDto preqDto) {
        CuPaymentRequestDocument preqDoc = (CuPaymentRequestDocument) documentService.getNewDocument(PAYMENT_REQUEST);

        preqDoc.setAccountsPayablePurchasingDocumentLinkIdentifier(poDoc.getAccountsPayablePurchasingDocumentLinkIdentifier());

        populateAndSavePaymentRequest(preqDoc); // This does not seem to populate items, other fields?

        // These fields are required for the next method to work
        preqDoc.setPurchaseOrderDocument(poDoc);
        preqDoc.setInvoiceNumber(preqDto.getInvoiceNumber());
        preqDoc.setInvoiceDate(java.sql.Date.valueOf(preqDto.getInvoiceDate()));
        preqDoc.setVendorInvoiceAmount(preqDto.getInvoiceAmount());

        // populatePaymentRequest is called when continue is clicked (prepareForSave && event instanceof AttributedContinuePurapEvent))
        populatePaymentRequest(preqDoc); //this populates vendor info, items, and many other fields

        preqDoc.setAccountsPayableProcessorIdentifier(PAYFLOW);
        preqDoc.setProcessingCampusCode(poDoc.getDeliveryCampusCode());
        preqDoc.setInvoiceReceivedDate(Date.valueOf(preqDto.getReceivedDate()));

        preqDoc.setVendorInvoiceAmount(preqDto.getInvoiceAmount());
        preqDoc.setSpecialHandlingInstructionLine1Text(preqDto.getSpecialHandlingLine1());
        preqDoc.setSpecialHandlingInstructionLine2Text(preqDto.getSpecialHandlingLine2());
        preqDoc.setSpecialHandlingInstructionLine3Text(preqDto.getSpecialHandlingLine3());

        if (CollectionUtils.isNotEmpty(preqDto.getNotes())) {
            for (PaymentRequestNoteDto noteDto : preqDto.getNotes()) {
                documentService.createNoteFromDocument(preqDoc, noteDto.getNoteText());
            }
        }
//
//        // do we need this since items are created from populatePaymentRequestFromPurchaseOrder?
////            preqDoc.setItems(createPreqItemsFromPreqDto(preqDto));

        // populate items
//
//        // do we need to add the misc items?
//        createMiscPreqItemsFromPreqDto(preqDto, preqDoc);

        return preqDoc;
    }

//    private List<PaymentRequestItem> createPreqItemsFromPreqDto(PaymentRequestDto preqDto, PaymentRequestDocument preqDocument) {
//        List<PaymentRequestItem> paymentRequestItems = new ArrayList<>();
//
//        for (PaymentRequestLineItemDto preqItemDto : preqDto.getItems()) {
//            PaymentRequestItem paymentRequestItem = new PaymentRequestItem();
//            paymentRequestItem.setItemTypeCode(ItemTypeCodes.ITEM_TYPE_ITEM_CODE);
//
//            paymentRequestItem.setItemUnitPrice(preqItemDto.getItemPrice().bigDecimalValue());
//            paymentRequestItem.setItemQuantity(preqItemDto.getItemQuantity());
//            paymentRequestItem.setItemDescription(preqItemDto.getItemDescription());
//            paymentRequestItem.setExtendedPrice(discountValueToUse);
//            paymentRequestItem.setPurapDocument(preqDocument);
//
//        }
//
//     }

    private List<PaymentRequestItem> createMiscPreqItemsFromPreqDto(PaymentRequestDto preqDto, PaymentRequestDocument preqDocument) {
        List<PaymentRequestItem> paymentRequestMiscItems = new ArrayList<>();

        if (ObjectUtils.isNotNull(preqDto.getShippingPrice()) && preqDto.getShippingPrice().isGreaterThan(new KualiDecimal(0))) {
            PaymentRequestItem shippingItem = getOrCreateMiscLine(preqDocument, ITEM_TYPE_SHIP_AND_HAND_CODE);
            shippingItem.setItemUnitPrice(preqDto.getShippingPrice().bigDecimalValue());
            shippingItem.setItemDescription(preqDto.getShippingDescription());
            paymentRequestMiscItems.add(shippingItem);
        }

        if (ObjectUtils.isNotNull(preqDto.getFreightPrice()) && preqDto.getFreightPrice().isGreaterThan(new KualiDecimal(0))) {
            PaymentRequestItem freightItem = getOrCreateMiscLine(preqDocument, ITEM_TYPE_FREIGHT_CODE);
            freightItem.setItemUnitPrice(preqDto.getFreightPrice().bigDecimalValue());
            freightItem.setItemDescription(preqDto.getFreightDescription());
            paymentRequestMiscItems.add(freightItem);
        }

        if (ObjectUtils.isNotNull(preqDto.getMiscellaneousPrice()) && preqDto.getMiscellaneousPrice().isGreaterThan(new KualiDecimal(0))) {
            PaymentRequestItem miscItem = getOrCreateMiscLine(preqDocument, ITEM_TYPE_MISC_CODE);
            miscItem.setItemUnitPrice(preqDto.getMiscellaneousPrice().bigDecimalValue());
            miscItem.setItemDescription(preqDto.getMiscellaneousDescription());
            paymentRequestMiscItems.add(miscItem);
        }

        return paymentRequestMiscItems;
     }

     private PaymentRequestItem getOrCreateMiscLine(PaymentRequestDocument preqDoc, String itemTypeCode) {
        for (Object item : preqDoc.getItems()) {
            try {
                PaymentRequestItem preqItem = (PaymentRequestItem) item;
                if (StringUtils.equals(preqItem.getItemTypeCode(), itemTypeCode)) {
                    return preqItem;
                }
            } catch (Exception e) {
                LOG.error("getOrCreateMiscLine for itemTypeCode {} failed", itemTypeCode);
            }
        }

        PaymentRequestItem paymentRequestItem = new PaymentRequestItem();
        paymentRequestItem.setItemTypeCode(itemTypeCode);
        preqDoc.addItem(paymentRequestItem);

        return paymentRequestItem;
     }

}
