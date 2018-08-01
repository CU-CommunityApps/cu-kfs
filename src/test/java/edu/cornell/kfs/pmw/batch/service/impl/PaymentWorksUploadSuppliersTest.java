package edu.cornell.kfs.pmw.batch.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private TestPaymentWorksUploadSuppliersService uploadSuppliersService;
    private PaymentWorksWebServiceCallsServiceImpl webServiceCallsService;
    private MockPaymentWorksUploadSuppliersEndpoint uploadSuppliersEndpoint;
    private String baseServerUrl;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.uploadSuppliersEndpoint = new MockPaymentWorksUploadSuppliersEndpoint();
        
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
     * Overridden to force a zero-second grace period on the test server shutdown.
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
    }

    @Test
    public void testSomething() throws Exception {
        List<PaymentWorksVendor> vendors = Collections.singletonList(PaymentWorksVendorFixture.JOHN_DOE.toPaymentWorksVendor());
        PaymentWorksUploadSuppliersBatchReportData reportData = new PaymentWorksUploadSuppliersBatchReportData();
        boolean result = uploadSuppliersService.uploadVendorsToPaymentWorks(vendors, reportData);
        if (result) {
            
        }
    }

    private static class TestPaymentWorksUploadSuppliersService extends PaymentWorksUploadSuppliersServiceImpl {
        @Override
        public boolean uploadVendorsToPaymentWorks(Collection<PaymentWorksVendor> vendors, PaymentWorksUploadSuppliersBatchReportData reportData) {
            return super.uploadVendorsToPaymentWorks(vendors, reportData);
        }
    }

}
