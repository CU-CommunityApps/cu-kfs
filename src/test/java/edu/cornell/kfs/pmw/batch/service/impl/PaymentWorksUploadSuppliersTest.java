package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.localserver.LocalServerTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.businessobject.fixture.PaymentWorksVendorFixture;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksUploadSuppliersBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.web.mock.MockPaymentWorksUploadSuppliersEndpoint;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class PaymentWorksUploadSuppliersTest extends LocalServerTestBase {

    private static final String TEMP_SUPPLIER_UPLOAD_DIRECTORY = "test/pmw/suppliers/";

    private TestPaymentWorksUploadSuppliersService uploadSuppliersService;
    private PaymentWorksWebServiceCallsServiceImpl webServiceCallsService;
    private MockPaymentWorksUploadSuppliersEndpoint uploadSuppliersEndpoint;
    private String baseServerUrl;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        FileUtils.forceMkdir(uploadedSuppliersDirectory);
        this.uploadSuppliersEndpoint = new MockPaymentWorksUploadSuppliersEndpoint(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        
        serverBootstrap.registerHandler(uploadSuppliersEndpoint.getRelativeUrlPatternForHandlerRegistration(), uploadSuppliersEndpoint);
        HttpHost httpHost = start();
        
        this.baseServerUrl = httpHost.toURI();
        String mockPaymentWorksUrl = baseServerUrl + CUKFSConstants.SLASH;
        
        this.webServiceCallsService = new PaymentWorksWebServiceCallsServiceImpl();
        webServiceCallsService.setWebServiceCredentialService(buildMockWebServiceCredentialService(mockPaymentWorksUrl));
        
        this.uploadSuppliersService = new TestPaymentWorksUploadSuppliersService();
        uploadSuppliersService.setPaymentWorksWebServiceCallsService(webServiceCallsService);
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService(String mockPaymentWorksUrl) {
        WebServiceCredentialService webServiceCredentialService = mock(WebServiceCredentialService.class);
        
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_API_URL))
                .thenReturn(mockPaymentWorksUrl);
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_USER_ID))
                .thenReturn("userId01");
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN))
                .thenReturn("Token01");
        
        return webServiceCredentialService;
    }

    /**
     * Overridden to force a zero-second grace period on the test server shutdown, and to include some other test-specific cleanup.
     * 
     * @see org.apache.http.localserver.LocalServerTestBase#shutDown()
     */
    @Override
    @After
    public void shutDown() throws Exception {
        if (this.httpclient != null) {
            this.httpclient.close();
        }
        if (this.server != null) {
            this.server.shutdown(0L, TimeUnit.SECONDS);
        }
        deleteTemporaryFileDirectory();
    }

    private void deleteTemporaryFileDirectory() throws Exception {
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        if (uploadedSuppliersDirectory.exists() && uploadedSuppliersDirectory.isDirectory()) {
            FileUtils.forceDelete(uploadedSuppliersDirectory.getAbsoluteFile());
        }
    }

    @Test
    public void testUploadSingleVendor() throws Exception {
        assertUploadSucceeds(PaymentWorksVendorFixture.JOHN_DOE);
    }

    @Test
    public void testUploadMultipleVendors() throws Exception {
        assertUploadSucceeds(PaymentWorksVendorFixture.JOHN_DOE, PaymentWorksVendorFixture.MARY_SMITH, PaymentWorksVendorFixture.WIDGET_MAKERS);
    }

    @Test
    public void testHandleErrorResponseFromEndpoint() throws Exception {
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        uploadSuppliersEndpoint.setExpectedVendorsForNextUpload(new PaymentWorksVendorFixture[0]);
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(Collections.emptyList(), reportData);
        assertFalse("The web service call should have failed", result);
        assertEquals("Wrong number of global errors recorded", 1, reportData.getSharedErrorMessages().size());
    }

    private void assertUploadSucceeds(PaymentWorksVendorFixture... fixtures) {
        List<PaymentWorksVendor> vendors = Arrays.stream(fixtures)
                .map(PaymentWorksVendorFixture::toPaymentWorksVendor)
                .collect(Collectors.toCollection(ArrayList::new));
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        uploadSuppliersEndpoint.setExpectedVendorsForNextUpload(fixtures);
        
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(vendors, reportData);
        assertTrue("Supplier upload should have succeeded", result);
        assertEquals("Wrong vendor count received from server's response",
                fixtures.length, reportData.getRecordsProcessedByPaymentWorksSummary().getRecordCount());
    }

    private static class TestPaymentWorksUploadSuppliersService extends PaymentWorksUploadSuppliersServiceImpl {
        @Override
        public boolean uploadVendorsToPaymentWorks(Collection<PaymentWorksVendor> vendors, PaymentWorksUploadSuppliersBatchReportData reportData) {
            return super.uploadVendorsToPaymentWorks(vendors, reportData);
        }
    }

}
