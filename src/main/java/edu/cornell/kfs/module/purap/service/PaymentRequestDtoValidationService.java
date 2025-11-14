package edu.cornell.kfs.module.purap.service;

import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestResultsDto;

public interface PaymentRequestDtoValidationService {
    PaymentRequestResultsDto validatePaymentRequestDto(PaymentRequestDto paymentRequestDto);
}
