package edu.cornell.kfs.module.purap.service.impl;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.fixture.PaymentRequestDtoFixture;
import edu.cornell.kfs.module.purap.rest.jsonObjects.fixture.PaymentRequestLineItemDtoFixture;

public class PaymentRequestDtoValidationServiceImplTest {
    private static final Logger LOG = LogManager.getLogger();
    private PaymentRequestDtoValidationServiceImpl validationService;

    @BeforeEach
    private void setUp() throws Exception {
        Configurator.setLevel(PaymentRequestDtoValidationServiceImpl.class, Level.DEBUG);

        validationService = new PaymentRequestDtoValidationServiceImpl();
        validationService.setConfigurationService(buildMockConfigurationService());
        validationService.setVendorService(buildMockVendorService());
        validationService.setPurchaseOrderService(buildMockPurchaseOrderService());
        validationService.setPaymentRequestDao(buildMockCuPaymentRequestDao());
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED))
                .thenReturn("{0} is a required field.");
        Mockito.when(
                service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_AT_LEAST_ONE_MUST_BE_ENTERED))
                .thenReturn("At least one {0} must be entered.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_INVALID_VENDOR_NUMBER))
                .thenReturn("Vendor Number {0} is not valid.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_INVALID_PO))
                .thenReturn("PO Number {0} is not valid.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_NOT_MATCH_VENDOR))
                .thenReturn("PO Number {0} has a vendor number {1} but the vendor number supplied was {2}.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_NOT_OPEN))
                .thenReturn("PO Number {0} is not open, it has a status of {1}.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_PO_INVALID_LINE))
                .thenReturn("PO Number {0} does not have a line number {1}.");
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_INTEGER))
                .thenReturn("{0} is not a valid integer");
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_BIG_DECIMAL))
                .thenReturn("{0} is not a valid decimal.");
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.ERROR_PAYMENTREQUEST_DATE_BAD_FORMAT))
                .thenReturn("{0} must be in the format of MM/DD/YYYY.");
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

        PurchaseOrderDocument docForPo98766 = buildMockPurchaseOrderDocument("987-32",
                PurchaseOrderStatuses.APPDOC_CANCELLED);
        Mockito.when(service.getCurrentPurchaseOrder(98766)).thenReturn(docForPo98766);

        PurchaseOrderDocument docForPo98767 = buildMockPurchaseOrderDocument("1234-1",
                PurchaseOrderStatuses.APPDOC_AWAIT_PURCHASING_REVIEW);
        Mockito.when(service.getCurrentPurchaseOrder(98767)).thenReturn(docForPo98767);

        Integer lineNumber = Integer.valueOf(PaymentRequestLineItemDtoFixture.ITEM_1_10_100.lineNumber);
        PurchaseOrderDocument docForPo98768 = buildMockPurchaseOrderDocument("1234-1",
                PurchaseOrderStatuses.APPDOC_OPEN);
        Mockito.when(docForPo98768.getItemByLineNumber(lineNumber))
                .thenReturn(null);
        Mockito.when(service.getCurrentPurchaseOrder(98768)).thenReturn(docForPo98768);

        PurchaseOrderDocument docForPo98769 = buildMockPurchaseOrderDocument("1234-1",
                PurchaseOrderStatuses.APPDOC_OPEN);
        PurApItem item = Mockito.mock(PurApItem.class);
        Mockito.when(docForPo98769.getItemByLineNumber(lineNumber))
                .thenReturn(item);
        Mockito.when(service.getCurrentPurchaseOrder(98769)).thenReturn(docForPo98769);

        return service;
    }

    private PurchaseOrderDocument buildMockPurchaseOrderDocument(String vendorNumber, String appDocStatus) {
        PurchaseOrderDocument doc = Mockito.mock(PurchaseOrderDocument.class);
        Mockito.when(doc.getVendorNumber()).thenReturn(vendorNumber);
        Mockito.when(doc.getApplicationDocumentStatus()).thenReturn(appDocStatus);
        return doc;
    }

    private CuPaymentRequestDao buildMockCuPaymentRequestDao() {
        CuPaymentRequestDao dao = Mockito.mock(CuPaymentRequestDao.class);
        return dao;
    }

    @AfterEach
    private void tearDown() throws Exception {
        validationService = null;
    }

    @ParameterizedTest
    @MethodSource("validationFixtures")
    public void testReadJsonToPaymentRequestDto(PaymentRequestDtoFixture paymentRequestDtoFixture) throws IOException {
        PaymentRequestResultsDto actualResults = validationService
                .validatePaymentRequestDto(paymentRequestDtoFixture.toPaymentRequestDto());
        LOG.info("testReadJsonToPaymentRequestDto, actual results: " + actualResults);
        Assertions.assertEquals(paymentRequestDtoFixture.expectedValidation, actualResults.isValid());
        Assertions.assertEquals(paymentRequestDtoFixture.expectedErrorMessages, actualResults.getErrorMessages());
        Assertions.assertEquals(paymentRequestDtoFixture.expectedSuccessMessages, actualResults.getSuccessMessages());
    }

    private static java.util.stream.Stream<PaymentRequestDtoFixture> validationFixtures() {
        return java.util.Arrays.stream(PaymentRequestDtoFixture.values())
                .filter(dto -> dto.name().startsWith("VALIDATION_TEST_"));
    }

}
