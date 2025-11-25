package edu.cornell.kfs.module.purap.service.impl;

import java.text.MessageFormat;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestLineItemDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestNoteDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.service.PaymentRequestDtoValidationService;
import edu.cornell.kfs.module.purap.util.PaymentRequestUtil.PaymentRequestDtoFields;

public class PaymentRequestDtoValidationServiceImpl implements PaymentRequestDtoValidationService {

    private ConfigurationService configurationService;

    @Override
    public PaymentRequestResultsDto validatePaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        PaymentRequestResultsDto results = new PaymentRequestResultsDto();
        results.setValid(true);
        validateRequiredFields(paymentRequestDto, results);
        return results;
    }

    private void validateRequiredFields(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        if (StringUtils.isBlank(paymentRequestDto.getVendorNumber())) {
            updateResultsWithRequiredFieldError(PaymentRequestDtoFields.VENDOR_NUMBER, results);
        }

        if (StringUtils.isBlank(paymentRequestDto.getPoNumber())) {
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
            results.getErrorMessages().add(buildAtLestOneElementError("item"));
        } else {
            paymentRequestDto.getItems().stream()
                    .forEach(itemDto -> validatePaymentRequestLineItemDtoRequiredFields(itemDto, results));
        }

        if (CollectionUtils.isEmpty(paymentRequestDto.getNotes())) {
            results.setValid(false);
            results.getErrorMessages().add(buildAtLestOneElementError("note"));
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

    private String buildAtLestOneElementError(String fieldName) {
        String messageBase = configurationService.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_AT_LEAST_ONE_MUST_BE_ENTERED);
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

        if (StringUtils.isBlank(itemDto.getLineNumber())) {
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
        }
    }

    private boolean isValidNoteType(String noteType) {
        String scrubbedNoteType = StringUtils.trimToNull(noteType);
        return StringUtils.isBlank(scrubbedNoteType) ||
                StringUtils.equals(PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_OTHER, scrubbedNoteType) ||
                StringUtils.equals(PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE, scrubbedNoteType);
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
