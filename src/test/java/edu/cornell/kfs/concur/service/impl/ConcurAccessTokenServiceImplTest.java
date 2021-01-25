package edu.cornell.kfs.concur.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Application;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.MockLegacyAuthConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.rest.application.MockConcurLegacyAuthenticationServerApplication;
import edu.cornell.kfs.concur.rest.resource.MockConcurLegacyAuthenticationServerResource;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.sys.rest.util.ApacheHttpJerseyTestBase;

public class ConcurAccessTokenServiceImplTest extends ApacheHttpJerseyTestBase.ForJUnit5 {

    private MockConcurLegacyAuthenticationServerResource mockAuthenticationResource;
    private DateTimeFormatter dateTimeFormatter;
    private AccessTokenDTO initialToken;
    private ConcurAccessTokenServiceImpl concurAccessTokenService;

    @Override
    protected Application configure() {
        return new MockConcurLegacyAuthenticationServerApplication();
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.dateTimeFormatter = DateTimeFormat.forPattern(MockLegacyAuthConstants.EXPIRATION_DATE_FORMAT);
        this.mockAuthenticationResource = getAndConfigureMockAuthenticationResource(dateTimeFormatter);
        this.initialToken = mockAuthenticationResource.buildAndStoreNewTokenDTO();
    }

    private MockConcurLegacyAuthenticationServerResource getAndConfigureMockAuthenticationResource(
            DateTimeFormatter expirationDateTimeFormatter) {
        String decodedCredentials = MockLegacyAuthConstants.MOCK_USERNAME
                + ConcurConstants.USERNAME_PASSWORD_SEPARATOR + MockLegacyAuthConstants.MOCK_PASSWORD;
        String encodedCredentials = ConcurUtils.base64Encode(decodedCredentials);
        
        MockConcurLegacyAuthenticationServerResource mockResource = getSingletonFromApplication(
                MockConcurLegacyAuthenticationServerResource.class);
        mockResource.setBaseUri(getBaseUri().toString());
        mockResource.setDateTimeFormatter(expirationDateTimeFormatter);
        mockResource.setCurrentTokens(new ConcurrentHashMap<>());
        mockResource.setValidClientId(MockLegacyAuthConstants.MOCK_CLIENT_ID);
        mockResource.setValidClientSecret(MockLegacyAuthConstants.MOCK_SECRET_KEY);
        mockResource.setValidEncodedCredentials(encodedCredentials);
        return mockResource;
    }

}
