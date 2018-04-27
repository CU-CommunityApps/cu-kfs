package edu.cornell.kfs.pmw.web.mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.kfs.pmw.PaymentWorksTestConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants.PaymentWorksTokenRefreshConstants;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;

/**
 * Utility class for mocking the behavior of the PaymentWorks endpoint for refreshing authorization tokens.
 * Note that the returned tokens, HTTP codes and response messages do not necessarily match the actual ones
 * that PaymentWorks would return under similar circumstances.
 */
public class MockPaymentWorksRefreshTokenEndpoint extends MockServiceEndpointBase {

    private static final String REFRESH_TOKEN_ENDPOINT_HANDLER_PATTERN = "/users/*";
    private static final String RELATIVE_REFRESH_TOKEN_ENDPOINT_URL_REGEX = "^/users/([a-zA-Z0-9]+)/refresh_auth_token/$";
    private static final int USERID_REGEX_GROUP_INDEX = 1;
    private static final int TOKEN_LENGTH = 40;

    private ConcurrentMap<String, String> authorizationTokenMap = new ConcurrentHashMap<>();
    private AtomicInteger idCounter = new AtomicInteger(0);
    private Pattern refreshTokenUrlPattern = Pattern.compile(RELATIVE_REFRESH_TOKEN_ENDPOINT_URL_REGEX);

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return REFRESH_TOKEN_ENDPOINT_HANDLER_PATTERN;
    }

    public String generateNewTokenForUser(String userId) {
        if (!StringUtils.isAlphanumeric(userId)) {
            throw new IllegalArgumentException("userId should have been alphanumeric");
        }
        String newToken = generateRandomToken();
        authorizationTokenMap.put(userId, newToken);
        return newToken;
    }

    protected String generateRandomToken() {
        String baseToken = StringUtils.lowerCase(RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH - 1));
        int nextId = idCounter.updateAndGet((value) -> (value + 1) % 10);
        return baseToken + String.valueOf(nextId);
    }

    public String getCurrentTokenForUser(String userId) {
        return authorizationTokenMap.get(userId);
    }

    @Override
    protected void processRequest(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        assertRequestHasCorrectHttpMethod(request, HttpMethod.PUT);
        assertRequestHasCorrectContentType(request, ContentType.APPLICATION_JSON);
        assertJsonContentIsWellFormed(request);
        
        String userId = getNonBlankValueFromRequestUrl(request, refreshTokenUrlPattern, USERID_REGEX_GROUP_INDEX);
        String authToken = getNonBlankHeaderValue(request, PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY);
        assertTrue("Authorization token header value is missing the expected prefix",
                StringUtils.startsWith(authToken, PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER));
        
        String authTokenWithoutPrefix = StringUtils.substringAfter(authToken, PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER);
        assertTrue("Authorization token header value should have contained more than just the prefix",
                StringUtils.isNotBlank(authTokenWithoutPrefix));
        
        if (!authorizationTokenMap.containsKey(userId)) {
            setupRefreshFailureResponse(response, PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_USER_ID);
        } else if (!StringUtils.equals(authTokenWithoutPrefix, authorizationTokenMap.get(userId))) {
            setupRefreshFailureResponse(response, PaymentWorksTestConstants.RefreshTokenErrorMessages.INVALID_AUTHORIZATION_TOKEN);
        } else {
            String newToken = generateNewTokenForUser(userId);
            setupRefreshSuccessResponse(response, newToken);
        }
    }

    private void assertJsonContentIsWellFormed(HttpRequest request) {
        JsonNode rootNode = getRequestContentAsJsonNodeTree(request);
        assertNotNull("Request should have contained well-formed JSON content", rootNode);
    }

    private void setupRefreshSuccessResponse(HttpResponse response, String authorizationToken) {
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksTokenRefreshConstants.STATUS_FIELD, PaymentWorksTokenRefreshConstants.STATUS_OK);
            rootNode.put(PaymentWorksTokenRefreshConstants.AUTH_TOKEN_FIELD, authorizationToken);
        });
        
        response.setStatusCode(HttpStatus.SC_OK);
        response.setEntity(new StringEntity(jsonText, ContentType.APPLICATION_JSON));
    }

    private void setupRefreshFailureResponse(HttpResponse response, String message) {
        String jsonText = buildJsonTextFromNode((rootNode) -> {
            rootNode.put(PaymentWorksTokenRefreshConstants.DETAIL_FIELD, message);
        });
        
        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        response.setEntity(new StringEntity(jsonText, ContentType.APPLICATION_JSON));
    }

    private String buildJsonTextFromNode(Consumer<ObjectNode> jsonNodeConfigurer) {
        String jsonResponse = null;
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
            ObjectNode rootNode = nodeFactory.objectNode();
            jsonNodeConfigurer.accept(rootNode);
            jsonResponse = objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            fail("Unexpected error when preparing JSON output: " + e.getMessage());
        }
        
        return jsonResponse;
    }

}
