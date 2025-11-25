package edu.cornell.kfs.module.purap.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.fixture.PaymentRequestDtoFixture;

public class PaymentRequestDtoValidationServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    private PaymentRequestDtoValidationServiceImpl validationService;

    @BeforeEach
    private void setUp() throws Exception {
        validationService = new PaymentRequestDtoValidationServiceImpl();
        validationService.setConfigurationService(buildMockConfigurationService());
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED)).thenReturn("{0} is a required field.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_AT_LEAST_ONE_MUST_BE_ENTERED))
            .thenReturn("At lest one {0} must be entered.");
        return service;
    }

    @AfterEach
    private void tearDown() throws Exception {
        validationService = null;
    }

    @ParameterizedTest
    @MethodSource("validationFixtures")
    public void testReadJsonToPaymentRequestDto(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException {
        PaymentRequestResultsDto actualResults = validationService.validatePaymentRequestDto(paymentRequestDtoFixture.toPaymentRequestDto());
        LOG.info("testReadJsonToPaymentRequestDto, actual results: " + actualResults);
        assertEquals(paymentRequestDtoFixture.expectedValidation, actualResults.isValid());
        assertEquals(paymentRequestDtoFixture.expectedErrorMessages, actualResults.getErrorMessages());
        assertEquals(paymentRequestDtoFixture.expectedSuccessMessages, actualResults.getSuccessMessages());
    }

    private static java.util.stream.Stream<PaymentRequestDtoFixture> validationFixtures() {
        return java.util.Arrays.stream(PaymentRequestDtoFixture.values())
            .filter(dto -> dto.name().startsWith("VALIDATION_TEST_"));
    }
    
}
