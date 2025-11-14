package edu.cornell.kfs.module.purap.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestLineItemDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestNoteDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.service.PaymentRequestDtoValidationService;

public class PaymentRequestDtoValidationServiceImpl implements PaymentRequestDtoValidationService {

    @Override
    public PaymentRequestResultsDto validatePaymentRequestDto(PaymentRequestDto paymentRequestDto) {
        PaymentRequestResultsDto results = new PaymentRequestResultsDto();
        results.setValid(true);
        validateRequiredFields(paymentRequestDto, results);
        return results;
    }

    private void validateRequiredFields(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results) {
        if (StringUtils.isBlank(paymentRequestDto.getVendorNumber())) {
            results.setValid(false);
        }

        if (StringUtils.isBlank(paymentRequestDto.getPoNumber())) {
            results.setValid(false);
        }

        if (paymentRequestDto.getInvoiceDate() == null) {
            results.setValid(false);
        }

        if (paymentRequestDto.getReceivedDate() == null) {
            results.setValid(false);
        }

        if (StringUtils.isBlank(paymentRequestDto.getInvoiceNumber())) {
            results.setValid(false);
        }

        if (paymentRequestDto.getInvoiceAmount() == null) {
            results.setValid(false);
        }

        if (CollectionUtils.isEmpty(paymentRequestDto.getItems())) {
            results.setValid(false);
        } else {
            paymentRequestDto.getItems().stream().forEach(itemDto -> validatePaymentRequestLineItemDtoRequiredFields(itemDto, results));
        }

        if (CollectionUtils.isEmpty(paymentRequestDto.getNotes())) {
            results.setValid(false);
        } else {
            paymentRequestDto.getNotes().stream().forEach(noteDto -> validatePaymentRequestNoteDtoRequiredFields(noteDto, results));
        }

    }

    private void validatePaymentRequestLineItemDtoRequiredFields(PaymentRequestLineItemDto itemDto, PaymentRequestResultsDto results) {

    }

    private void validatePaymentRequestNoteDtoRequiredFields(PaymentRequestNoteDto noteDto, PaymentRequestResultsDto results) {
        
    }
    
}
