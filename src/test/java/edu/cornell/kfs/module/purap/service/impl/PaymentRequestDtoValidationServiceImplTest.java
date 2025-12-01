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
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.rest.jsonOnjects.fixture.PaymentRequestDtoFixture;

public class PaymentRequestDtoValidationServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    private PaymentRequestDtoValidationServiceImpl validationService;

    @BeforeEach
    private void setUp() throws Exception {
        validationService = new PaymentRequestDtoValidationServiceImpl();
        validationService.setConfigurationService(buildMockConfigurationService());
        validationService.setVendorService(buildMockVendorService());
        validationService.setPurchaseOrderService(buildMockPurchaseOrderService());
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED)).thenReturn("{0} is a required field.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_AT_LEAST_ONE_MUST_BE_ENTERED))
            .thenReturn("At lest one {0} must be entered.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_INVALID_VENDOR_NUMBER))
            .thenReturn("Vendor Number {0} is not valid.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERORR_PAYMENTREQUEST_INVALID_PO))
            .thenReturn("PO Number {0} is not valid.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERORR_PAYMENTREQUEST_PO_NOT_MATCH_VENDOR))
            .thenReturn("PO Number {0} has a vendor number {1} but the vendor number supplied was {2}.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERORR_PAYMENTREQUEST_PO_NOT_OPEN))
            .thenReturn("PO Number {0} is not open, it has a status of {1}.");
        return service;
    }

    private VendorService buildMockVendorService() {
        VendorService service = Mockito.mock(VendorService.class);
        VendorDetail detail = new VendorDetail();
        Mockito.when(service.getByVendorNumber("1234-1")).thenReturn(detail);
        return service;
    }

    private PurchaseOrderService buildMockPurchaseOrderService() {
        PurchaseOrderService service = Mockito.mock(PurchaseOrderService.class);
        Mockito.when(service.getCurrentPurchaseOrder(98765)).thenReturn(null);
        
        PurchaseOrderDocument docForPo98766 = buildMockPurchaseOrderDocument("987-32", PurchaseOrderStatuses.APPDOC_CANCELLED);
        Mockito.when(service.getCurrentPurchaseOrder(98766)).thenReturn(docForPo98766);

        PurchaseOrderDocument docForPo98767 = buildMockPurchaseOrderDocument("1234-1", PurchaseOrderStatuses.APPDOC_AWAIT_PURCHASING_REVIEW);
        Mockito.when(service.getCurrentPurchaseOrder(98767)).thenReturn(docForPo98767);

        PurchaseOrderDocument docForPo98768 = buildMockPurchaseOrderDocument("1234-1", PurchaseOrderStatuses.APPDOC_OPEN);
        Mockito.when(service.getCurrentPurchaseOrder(98768)).thenReturn(docForPo98768);
        return service;
    }

    private PurchaseOrderDocument buildMockPurchaseOrderDocument(String vendorNumber, String appDocStatus) {
        PurchaseOrderDocument doc = Mockito.mock(PurchaseOrderDocument.class);
        Mockito.when(doc.getVendorNumber()).thenReturn(vendorNumber);
        Mockito.when(doc.getApplicationDocumentStatus()).thenReturn(appDocStatus);
        return doc;
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
