package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.pmw.PaymentWorksTestConstants.ParameterTestValues;
import edu.cornell.kfs.pmw.PaymentWorksTestConstants.SupplierUploadErrorMessages;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.businessobject.fixture.PaymentWorksVendorFixture;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksSupplierUploadConstants;
import edu.cornell.kfs.pmw.web.mock.MockPaymentWorksUploadSuppliersEndpoint;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.web.mock.CuLocalServerTestBase;

public class PaymentWorksUploadSuppliersTest extends CuLocalServerTestBase {
    private static final String TEMP_SUPPLIER_UPLOAD_DIRECTORY = "test/pmw/suppliers/";
    private static final String DUMMY_AUTHORIZATION_TOKEN = "111122223333444455556666777788889999AAAA";

    private TestPaymentWorksUploadSuppliersService uploadSuppliersService;
    private TestPaymentWorksWebServiceCallsService webServiceCallsService;
    private MockPaymentWorksUploadSuppliersEndpoint uploadSuppliersEndpoint;
    private String baseServerUrl;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        FileUtils.forceMkdir(uploadedSuppliersDirectory);
        this.uploadSuppliersEndpoint = new MockPaymentWorksUploadSuppliersEndpoint(TEMP_SUPPLIER_UPLOAD_DIRECTORY, DUMMY_AUTHORIZATION_TOKEN);
        
        String pattern = uploadSuppliersEndpoint.getRelativeUrlPatternForHandlerRegistration();
        HttpRequestHandler handler = uploadSuppliersEndpoint;
        server.registerHandler(pattern, handler);
        HttpHost httpHost = start();
        
        this.baseServerUrl = httpHost.toURI();
        String mockPaymentWorksUrl = baseServerUrl + CUKFSConstants.SLASH;
        
        this.webServiceCallsService = new TestPaymentWorksWebServiceCallsService();
        webServiceCallsService.setWebServiceCredentialService(buildMockWebServiceCredentialService(mockPaymentWorksUrl));
        
        this.uploadSuppliersService = new TestPaymentWorksUploadSuppliersService();
        uploadSuppliersService.setPaymentWorksWebServiceCallsService(webServiceCallsService);
        uploadSuppliersService.setPaymentWorksBatchUtilityService(buildMockBatchUtilityService());
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService(String mockPaymentWorksUrl) {
        WebServiceCredentialService webServiceCredentialService = mock(WebServiceCredentialService.class);
        
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_API_URL))
                .thenReturn(mockPaymentWorksUrl);
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN))
                .thenReturn(DUMMY_AUTHORIZATION_TOKEN);
        
        return webServiceCredentialService;
    }

    private PaymentWorksBatchUtilityService buildMockBatchUtilityService() {
        PaymentWorksBatchUtilityService batchUtilityService = mock(PaymentWorksBatchUtilityService.class);
        when(batchUtilityService.retrievePaymentWorksParameterValue(any(String.class)))
                .then(this::getTestParameterValue);
        return batchUtilityService;
    }

    private String getTestParameterValue(InvocationOnMock invocation) {
        String parameterKey = invocation.getArgument(0);
        if (StringUtils.isBlank(parameterKey)) {
            return KFSConstants.EMPTY_STRING;
        }
        
        switch (parameterKey) {
            case PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_COUNT_MISMATCH_MESSAGE_PREFIX :
                return ParameterTestValues.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_COUNT_MISMATCH_MESSAGE_PREFIX;
            case PaymentWorksParameterConstants.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE_PREFIX :
                return ParameterTestValues.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE_PREFIX;
            default :
                return KFSConstants.EMPTY_STRING;
        }
    }

    /**
     * Overridden to force a zero-second grace period on the test server shutdown, and to include some other test-specific cleanup.
     * 
     * @see org.apache.http.localserver.LocalServerTestBase#shutDown()
     */
    @Override
    @After
    public void shutDown() throws Exception {
        super.shutDown();
        deleteTemporaryFileDirectory();
        webServiceCallsService.destroy();
    }

    private void deleteTemporaryFileDirectory() throws Exception {
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        if (uploadedSuppliersDirectory.exists() && uploadedSuppliersDirectory.isDirectory()) {
            FileUtils.forceDelete(uploadedSuppliersDirectory.getAbsoluteFile());
        }
    }

    @Test
    public void testUploadSingleVendor() throws Exception {
        assertUploadSucceedsCompletely(PaymentWorksVendorFixture.JOHN_DOE);
    }

    @Test
    public void testUploadMultipleVendors() throws Exception {
        assertUploadSucceedsCompletely(
                PaymentWorksVendorFixture.JOHN_DOE, PaymentWorksVendorFixture.MARY_SMITH, PaymentWorksVendorFixture.WIDGET_MAKERS);
    }

    @Test
    public void testLargeVendorUploadContainsContentLengthHeader() throws Exception {
        int rowCount = 200;
        PaymentWorksVendorFixture[] fixtures = new PaymentWorksVendorFixture[rowCount];
        Arrays.fill(fixtures, PaymentWorksVendorFixture.JOHN_DOE);
        assertUploadSucceedsCompletely(fixtures);
    }

    private void assertUploadSucceedsCompletely(PaymentWorksVendorFixture... fixtures) {
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        assertUploadSucceeds(reportData, fixtures);
        assertEquals("Wrong vendor count received from server's response",
                fixtures.length, reportData.getRecordsProcessedByPaymentWorksSummary().getRecordCount());
        assertEquals("No global messages should have been recorded", 0, reportData.getGlobalMessages().size());
    }

    private void assertUploadSucceeds(
            PaymentWorksUploadSuppliersBatchReportData reportData, PaymentWorksVendorFixture... fixtures) {
        List<PaymentWorksVendor> vendors = buildVendorsFromFixtures(fixtures);
        uploadSuppliersEndpoint.setExpectedVendorsForNextUpload(fixtures);
        
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(vendors, reportData);
        assertTrue("Supplier upload should have succeeded", result);
        assertTrue("The Supplier Upload endpoint should have been invoked", uploadSuppliersEndpoint.isCalledUploadSuppliersService());
        assertTrue("The server should not have sent an error message field",
                StringUtils.isBlank(webServiceCallsService.getLastSupplierUploadErrorFieldName()));
        assertTrue("The server should not have sent an error message",
                StringUtils.isBlank(webServiceCallsService.getLastSupplierUploadErrorMessage()));
    }

    @Test
    public void testHandleErrorResponseFromEndpoint() throws Exception {
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        uploadSuppliersEndpoint.setExpectedVendorsForNextUpload(new PaymentWorksVendorFixture[0]);
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(Collections.emptyList(), reportData);
        assertFalse("The web service call should have failed", result);
        assertEquals("Wrong number of global messages recorded", 1, reportData.getGlobalMessages().size());
        assertTrue("Wrong global message prefix recorded in report", StringUtils.startsWith(
                reportData.getGlobalMessages().get(0), ParameterTestValues.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE_PREFIX));
        assertTrue("The Supplier Upload endpoint should have been invoked", uploadSuppliersEndpoint.isCalledUploadSuppliersService());
        assertEquals("Wrong error message field returned by endpoint",
                PaymentWorksSupplierUploadConstants.ERROR_FIELD, webServiceCallsService.getLastSupplierUploadErrorFieldName());
        assertEquals("Wrong error message returned by endpoint",
                SupplierUploadErrorMessages.FILE_CONTAINED_ZERO_ROWS, webServiceCallsService.getLastSupplierUploadErrorMessage());
    }

    @Test
    public void testHandleDetectionOfVendorDeletionAttempts() throws Exception {
        List<PaymentWorksVendor> vendors = buildVendorsFromFixtures(
                PaymentWorksVendorFixture.JOHN_DOE, PaymentWorksVendorFixture.MARY_SMITH_DELETE, PaymentWorksVendorFixture.WIDGET_MAKERS);
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(vendors, reportData);
        assertFalse("The processing should have failed", result);
        assertEquals("Wrong number of global messages recorded", 1, reportData.getGlobalMessages().size());
        assertTrue("Wrong global message prefix recorded in report", StringUtils.startsWith(
                reportData.getGlobalMessages().get(0), ParameterTestValues.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE_PREFIX));
        assertFalse("The Supplier Upload endpoint should not have been invoked", uploadSuppliersEndpoint.isCalledUploadSuppliersService());
    }

    @Test
    public void testHandleDetectionOfVendorCountMismatch() throws Exception {
        PaymentWorksVendorFixture[] fixtures = { PaymentWorksVendorFixture.JOHN_DOE, PaymentWorksVendorFixture.WIDGET_MAKERS };
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        uploadSuppliersEndpoint.setForceVendorCountMismatch(true);
        assertUploadSucceeds(reportData, fixtures);
        
        assertNotEquals("There should have been a mismatch between the number of vendors uploaded and the server's number of received vendors",
                fixtures.length, reportData.getRecordsProcessedByPaymentWorksSummary().getRecordCount());
        assertEquals("Wrong number of global messages recorded", 1, reportData.getGlobalMessages().size());
        assertTrue("Wrong global message prefix recorded in report", StringUtils.startsWith(
                reportData.getGlobalMessages().get(0), ParameterTestValues.PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_COUNT_MISMATCH_MESSAGE_PREFIX));
    }

    private List<PaymentWorksVendor> buildVendorsFromFixtures(PaymentWorksVendorFixture... fixtures) {
        return Arrays.stream(fixtures)
                .map(PaymentWorksVendorFixture::toPaymentWorksVendor)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static class TestPaymentWorksUploadSuppliersService extends PaymentWorksUploadSuppliersServiceImpl {
        @Override
        public boolean uploadVendorsToPaymentWorks(Collection<PaymentWorksVendor> vendors, PaymentWorksUploadSuppliersBatchReportData reportData) {
            return super.uploadVendorsToPaymentWorks(vendors, reportData);
        }
    }

    private static class TestPaymentWorksWebServiceCallsService extends PaymentWorksWebServiceCallsServiceImpl {
        private static final long serialVersionUID = 1L;
        
        private String lastSupplierUploadErrorFieldName = null;
        private String lastSupplierUploadErrorMessage = null;
        
        @Override
        protected void handleErrorMessageFromSupplierUploadFailure(String errorFieldName, String errorMessage) {
            this.lastSupplierUploadErrorFieldName = errorFieldName;
            this.lastSupplierUploadErrorMessage = errorMessage;
        }
        
        public String getLastSupplierUploadErrorFieldName() {
            return lastSupplierUploadErrorFieldName;
        }
        
        public String getLastSupplierUploadErrorMessage() {
            return lastSupplierUploadErrorMessage;
        }
    }

}
