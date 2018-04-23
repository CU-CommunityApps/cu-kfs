package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.localserver.LocalServerTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.pmw.PaymentWorksTestConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksCredentialKeys;
import edu.cornell.kfs.pmw.web.mock.MockPaymentWorksRefreshTokenEndpoint;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class PaymentWorksWebServiceCallsServiceRefreshTokenTest extends LocalServerTestBase {

    private static final String TEST_USERID_1 = "123abc";
    private static final String TEST_USERID_2 = "555555";
    private static final String INVALID_TOKEN = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    private TestPaymentWorksWebServiceCallsServiceImpl webServiceCallsService;
    private String userId;
    private String oldAuthorizationToken;
    private String currentAuthorizationToken;

    private MockPaymentWorksRefreshTokenEndpoint refreshTokenEndpoint;
    private String baseServerUrl;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.refreshTokenEndpoint = new MockPaymentWorksRefreshTokenEndpoint();
        
        serverBootstrap.registerHandler(
                refreshTokenEndpoint.getRelativeUrlPatternForHandlerRegistration(), refreshTokenEndpoint);
        HttpHost httpHost = start();
        
        this.baseServerUrl = httpHost.toURI();
        
        webServiceCallsService = new TestPaymentWorksWebServiceCallsServiceImpl();
        webServiceCallsService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        webServiceCallsService.setPaymentWorksUrl(baseServerUrl + CUKFSConstants.SLASH);
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

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService webServiceCredentialService = mock(WebServiceCredentialService.class);
        
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_USER_ID))
                .then(this::getUserId);
        when(webServiceCredentialService.getWebServiceCredentialValue(
                PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE, PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN))
                .then(this::getCurrentAuthorizationToken);
        doAnswer(this::setCurrentAuthorizationToken)
                .when(webServiceCredentialService).updateWebServiceCredentialValue(
                        eq(PaymentWorksConstants.PAYMENTWORKS_WEB_SERVICE_GROUP_CODE),
                        eq(PaymentWorksCredentialKeys.PAYMENTWORKS_AUTHORIZATION_TOKEN),
                        any(String.class));
        
        return webServiceCredentialService;
    }

    private String getUserId(InvocationOnMock invocation) {
        return userId;
    }

    private String getCurrentAuthorizationToken(InvocationOnMock invocation) {
        return currentAuthorizationToken;
    }

    private String setCurrentAuthorizationToken(InvocationOnMock invocation) {
        this.currentAuthorizationToken = invocation.getArgument(2);
        return null;
    }

    private void setupInitialTokenForCurrentUser() {
        currentAuthorizationToken = refreshTokenEndpoint.generateNewTokenForUser(userId);
        oldAuthorizationToken = currentAuthorizationToken;
    }

    @Test
    public void testRefreshTokenForValidUser() throws Exception {
        userId = TEST_USERID_1;
        setupInitialTokenForCurrentUser();
        
        webServiceCallsService.refreshPaymentWorksAuthorizationToken();
        assertTrue("The mock endpoint should have returned a successful response",
                StringUtils.isBlank(webServiceCallsService.getLastDetailMessage()));
        assertEquals("Current token does not match the one configured on the service endpoint",
                refreshTokenEndpoint.getCurrentTokenForUser(userId), currentAuthorizationToken);
        assertNotEquals("Token should have been updated by the service call", oldAuthorizationToken, currentAuthorizationToken);
    }

    @Test
    public void testRefreshTokenForUninitializedUser() throws Exception {
        userId = TEST_USERID_1;
        oldAuthorizationToken = INVALID_TOKEN;
        currentAuthorizationToken = INVALID_TOKEN;
        
        assertTokenRefreshFailsAsIntended(PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_USER_ID);
    }

    @Test
    public void testRefreshTokenForValidUserAndInvalidToken() throws Exception {
        userId = TEST_USERID_1;
        setupInitialTokenForCurrentUser();
        oldAuthorizationToken = INVALID_TOKEN;
        currentAuthorizationToken = INVALID_TOKEN;
        
        assertTokenRefreshFailsAsIntended(PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_AUTHORIZATION_TOKEN);
    }

    @Test
    public void testRefreshTokenForCorrectTokenButIncorrectUninitializedUser() throws Exception {
        userId = TEST_USERID_1;
        setupInitialTokenForCurrentUser();
        userId = TEST_USERID_2;
        
        assertTokenRefreshFailsAsIntended(PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_USER_ID);
    }

    @Test
    public void testRefreshTokenForMismatchedUserAndToken() throws Exception {
        userId = TEST_USERID_1;
        setupInitialTokenForCurrentUser();
        userId = TEST_USERID_2;
        setupInitialTokenForCurrentUser();
        currentAuthorizationToken = refreshTokenEndpoint.getCurrentTokenForUser(TEST_USERID_1);
        oldAuthorizationToken = currentAuthorizationToken;
        
        assertTokenRefreshFailsAsIntended(PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_AUTHORIZATION_TOKEN);
    }

    protected void assertTokenRefreshFailsAsIntended(String expectedMessage) throws Exception {
        try {
            webServiceCallsService.refreshPaymentWorksAuthorizationToken();
            fail("The web service call should have failed");
        } catch (RuntimeException e) {
            assertEquals("Wrong value for failure message", expectedMessage, webServiceCallsService.getLastDetailMessage());
            if (StringUtils.isNotBlank(oldAuthorizationToken)) {
                assertEquals("Current token should not have changed as a result of the service failure",
                        oldAuthorizationToken, currentAuthorizationToken);
            } else {
                assertTrue("A new token should not have been initialized as a result of the service failure",
                        StringUtils.isBlank(currentAuthorizationToken));
            }
        }
    }

    private static class TestPaymentWorksWebServiceCallsServiceImpl extends PaymentWorksWebServiceCallsServiceImpl {
        private static final long serialVersionUID = 1L;
        
        private String lastDetailMessage;
        
        @Override
        protected void handleDetailMessageFromTokenRefreshFailure(String detailMessage) {
            this.lastDetailMessage = detailMessage;
        }
        
        public String getLastDetailMessage() {
            return lastDetailMessage;
        }
    }

}
