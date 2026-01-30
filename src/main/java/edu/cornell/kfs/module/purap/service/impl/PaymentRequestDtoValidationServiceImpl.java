package edu.cornell.kfs.module.purap.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestLineItemDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestNoteDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.service.PaymentRequestDtoValidationService;
import edu.cornell.kfs.module.purap.util.PaymentRequestUtil.PaymentRequestDtoFields;

public class PaymentRequestDtoValidationServiceImpl implements PaymentRequestDtoValidationService {
    private static final Logger LOG = LogManager.getLogger();
    private ConfigurationService configurationService;
    private VendorService vendorService;
    private PurchaseOrderService purchaseOrderService;
    private CuPaymentRequestDao paymentRequestDao;

    @Override
    public PaymentRequestResultsDto validatePaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        PaymentRequestResultsDto results = new PaymentRequestResultsDto();
        results.setValid(true);
        validateRequiredFields(paymentRequestDto, results);
        if (results.isValid()) {
            validateVendorNumber(paymentRequestDto, results);
        }
        if (results.isValid()) {
            validatePO(paymentRequestDto, results);
        }

        if (results.isValid()) {
            validatePOandInvoiceUnique(paymentRequestDto, results);
        }

        LOG.debug("validatePaymentRequestDto, validation results: {}", results);
        return results;
    }

    private void validateRequiredFields(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        if (StringUtils.isBlank(paymentRequestDto.getVendorNumber())) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.VENDOR_NUMBER, results);
        }

        if (paymentRequestDto.getPoNumber() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.PO_NUMBER, results);
        }

        if (paymentRequestDto.getInvoiceDate() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.INVOICE_DATE, results);
        }

        if (paymentRequestDto.getReceivedDate() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.RECEIVED_DATE, results);
        }

        if (StringUtils.isBlank(paymentRequestDto.getInvoiceNumber())) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.INVOICE_NUMBER, results);
        }

        if (paymentRequestDto.getInvoiceAmount() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.INVOICE_AMOUNT, results);
        }

        if (CollectionUtils.isEmpty(paymentRequestDto.getItems())) {
            results.setValid(false);
            results.getErrorMessages().add(buildAtLeastOneElementError("item"));
        } else {
            paymentRequestDto.getItems().stream()
                    .forEach(itemDto -> validatePaymentRequestLineItemDtoRequiredFields(itemDto, results));
        }

        if (CollectionUtils.isEmpty(paymentRequestDto.getNotes())) {
            results.setValid(false);
            results.getErrorMessages().add(buildAtLeastOneElementError("note"));
        } else {
            paymentRequestDto.getNotes().stream()
                    .forEach(noteDto -> validatePaymentRequestNoteDtoRequiredFields(noteDto, results));
        }
    }

    private void updateResultsWithRequiredFieldError(PaymentRequestDtoFields field, PaymentRequestResultsDto results) {
        results.setValid(false);
        results.getErrorMessages().add(buildRequiredFieldError(field.friendlyName));
    }

    private String buildRequiredFieldError(String fieldName) {
        String messageBase = configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED);
        return MessageFormat.format(messageBase, fieldName);
    }

    private String buildAtLeastOneElementError(String fieldName) {
        String messageBase = configurationService
                .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_AT_LEAST_ONE_MUST_BE_ENTERED);
        return MessageFormat.format(messageBase, fieldName);
    }

    private void validatePaymentRequestLineItemDtoRequiredFields(PaymentRequestLineItemDto itemDto,
            PaymentRequestResultsDto results) {
        if (itemDto.getItemPrice() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.ITEM_PRICE, results);
        }

        if (itemDto.getItemQuantity() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.ITEM_QUANTITY, results);
        }

        if (itemDto.getLineNumber() == null) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.ITEM_LINE_NUMBER, results);
        }

    }

    private void validatePaymentRequestNoteDtoRequiredFields(PaymentRequestNoteDto noteDto,
            PaymentRequestResultsDto results) {
        if (StringUtils.isBlank(noteDto.getNoteText())) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.NOTE_TEXT, results);
        }

        if (!isValidNoteType(noteDto.getNoteType())) {
            results.setValid(false);
            String errorMessage = "If a note type is entered, it must be '" +
                    PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER + "' or '" +
                    PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE + "'.";
            results.getErrorMessages().add(errorMessage);
        }
    }

    private boolean isValidNoteType(String noteType) {
        String scrubbedNoteType = StringUtils.trimToNull(noteType);
        return StringUtils.isBlank(scrubbedNoteType) ||
                StringUtils.equals(PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER, scrubbedNoteType) ||
                StringUtils.equals(PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE, scrubbedNoteType);
    }

    private void validateVendorNumber(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        VendorDetail vendorDetail = vendorService.getByVendorNumber(paymentRequestDto.getVendorNumber());
        if (ObjectUtils.isNull(vendorDetail)) {
            results.setValid(false);
            String messageBase = configurationService
                    .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_INVALID_VENDOR_NUMBER);
            results.getErrorMessages().add(MessageFormat.format(messageBase, paymentRequestDto.getVendorNumber()));
        }
    }

    private void validatePO(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        paymentRequestDto.getPoNumber();
        PurchaseOrderDocument poDoc = purchaseOrderService.getCurrentPurchaseOrder(paymentRequestDto.getPoNumber());
        if (poDoc == null) {
            results.setValid(false);
            String messageBase = configurationService
                    .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_INVALID_PO);
            results.getErrorMessages().add(MessageFormat.format(messageBase, paymentRequestDto.getPoNumberString()));
        } else if (!StringUtils.equalsIgnoreCase(poDoc.getVendorNumber(), paymentRequestDto.getVendorNumber())) {
            results.setValid(false);
            String messageBase = configurationService
                    .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_NOT_MATCH_VENDOR);
            results.getErrorMessages().add(MessageFormat.format(messageBase, paymentRequestDto.getPoNumberString(),
                    poDoc.getVendorNumber(), paymentRequestDto.getVendorNumber()));
        } else if (!StringUtils.equals(poDoc.getApplicationDocumentStatus(), PurchaseOrderStatuses.APPDOC_OPEN)) {
            results.setValid(false);
            String messageBase = configurationService
                    .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_NOT_OPEN);
            results.getErrorMessages().add(MessageFormat.format(messageBase, paymentRequestDto.getPoNumberString(),
                    poDoc.getApplicationDocumentStatus()));
        } else {
            validatePoLine(paymentRequestDto, results, poDoc);
        }
    }

    private void validatePoLine(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results,
            PurchaseOrderDocument poDoc) {
        for (PaymentRequestLineItemDto line : paymentRequestDto.getItems()) {
            PurApItem item = poDoc.getItemByLineNumber(line.getLineNumber());
            if (ObjectUtils.isNull(item)) {
                results.setValid(false);
                String messageBase = configurationService
                        .getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_INVALID_LINE);
                results.getErrorMessages().add(MessageFormat.format(messageBase,
                        String.valueOf(paymentRequestDto.getPoNumber()), String.valueOf(line.getLineNumber())));
            }
        }
    }

    private void validatePOandInvoiceUnique(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        final Integer poNumber = paymentRequestDto.getPoNumber();
        final String invoiceNumber = paymentRequestDto.getInvoiceNumber();
        final List<String> documentNumbers = paymentRequestDao
                .getDocumentNumbersForPurchaseOrderInvoiceNumberNotCanceled(poNumber, invoiceNumber);
        if (CollectionUtils.isNotEmpty(documentNumbers)) {
            results.setValid(false);
            final String docNumber = IterableUtils.first(documentNumbers);
            
            String messageBase = configurationService.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_INVOICE_ALREADY_USED);
            LOG.info("validatePOandInvoiceUnique, messageBase: " + messageBase);

            results.getErrorMessages().add(MessageFormat.format(messageBase, String.valueOf(poNumber), invoiceNumber, docNumber));
        }
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setPaymentRequestDao(CuPaymentRequestDao paymentRequestDao) {
        this.paymentRequestDao = paymentRequestDao;
    }

}
