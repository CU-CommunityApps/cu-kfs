package edu.cornell.kfs.module.purap.service;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;

public interface PaymentRequestDtoValidationService {
    PaymentRequestResultsDto validatePaymentRequestDto(PaymentRequestDto paymentRequestDto);
}
