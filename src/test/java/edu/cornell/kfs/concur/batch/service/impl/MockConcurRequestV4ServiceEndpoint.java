package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.ParameterTestValues;
import edu.cornell.kfs.concur.batch.fixture.ConcurFixtureUtils;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.web.mock.MockServiceCore5EndpointBase;
import edu.cornell.kfs.sys.web.mock.MockServiceEndpointBase;

public class MockConcurRequestV4ServiceEndpoint extends MockServiceCore5EndpointBase {

    private static final String REQUESTS_ENDPOINT_HANDLER_PATTERN = "/travelrequest/v4/*";

    private static final Pattern REQUESTS_ENDPOINT_REGEX = Pattern.compile(
            "^/travelrequest/v4/requests("
                    + "(?<getRequestList>\\?(([\\w%\\-\\.:]+=[\\w%\\-\\.:]*)(\\&[\\w%\\-\\.:]+=[\\w%\\-\\.:]*)*))|"
                    + "(?<getRequest>/(?<requestUuid>\\w+)/?)"
            + ")$");

    private static final String GET_REQUEST_LIST_GROUP = "getRequestList";
    private static final String GET_REQUEST_GROUP = "getRequest";
    private static final String REQUEST_UUID_GROUP = "requestUuid";

    private static final String ERRORS_PROPERTY = "errors";
    private static final String ERROR_CODE_PROPERTY = "errorCode";
    private static final String ERROR_MESSAGE_PROPERTY = "errorMessage";

    private static final String TEST_ERROR_CODE = "testError";
    private static final String NOT_FOUND_MESSAGE = "Not Found";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    private ObjectMapper objectMapper;
    private MockConcurRequestV4Backend mockBackendServer;
    private String expectedAccessToken;
    private RequestV4DetailFixture[] initialRequestDetails;
    private String baseRequestV4Url;
    private boolean forceServerError;

    public MockConcurRequestV4ServiceEndpoint(String expectedAccessToken,
            RequestV4DetailFixture... initialRequestDetails) {
        this.objectMapper = buildObjectMapper();
        this.mockBackendServer = null;
        this.expectedAccessToken = expectedAccessToken;
        this.initialRequestDetails = initialRequestDetails;
        this.forceServerError = false;
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setTimeZone(TimeZone.getTimeZone(CUKFSConstants.TIME_ZONE_UTC));
        jsonObjectMapper.setDateFormat(new SimpleDateFormat(
                CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_Z, Locale.US));
        return jsonObjectMapper;
    }

    public void setForceServerError(boolean forceServerError) {
        this.forceServerError = forceServerError;
    }

    @Override
    public String getRelativeUrlPatternForHandlerRegistration() {
        return REQUESTS_ENDPOINT_HANDLER_PATTERN;
    }

    @Override
    protected void onServerInitialized(String baseUrl) {
        super.onServerInitialized(baseUrl);
        baseRequestV4Url = baseUrl + ParameterTestValues.REQUEST_V4_RELATIVE_ENDPOINT;
        mockBackendServer = new MockConcurRequestV4Backend(baseRequestV4Url, initialRequestDetails);
    }

    public String getBaseRequestV4Url() {
        if (StringUtils.isBlank(baseRequestV4Url)) {
            throw new IllegalStateException("Base URL for the Request V4 endpoint was null; this endpoint may not "
                    + "have been initialized yet by the HTTP server, or this endpoint may have already been closed");
        }
        return baseRequestV4Url;
    }
    
    @Override
    protected void processRequest(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
            throws HttpException, IOException {
        if (forceServerError) {
            markResponseAsEncounteringInternalServerError(response);
            return;
        }
        String actualAuthorizationHeader = getNonBlankHeaderValue(request, ConcurConstants.AUTHORIZATION_PROPERTY);
        assertEquals(getExpectedAuthorizationHeader(), actualAuthorizationHeader,
                "Request has a malformed authorization header and/or an invalid token");
        
        String url = request.getRequestUri();
        Matcher endpointMatcher = REQUESTS_ENDPOINT_REGEX.matcher(url);
        if (!endpointMatcher.matches()) {
            fail("Invalid endpoint was invoked: " + url);
        }
        
        if (urlRepresentsSearchForRequestListing(endpointMatcher)) {
            handleSearchForRequestListing(request, response, url);
        } else if (urlRepresentsSearchForSingleRequest(endpointMatcher)) {
            handleSearchForSingleRequest(request, response, endpointMatcher);
        } else {
            fail("The URL did not match one of the expected operations. Attempted endpoint: " + url);
        }
        
    }

    private String getExpectedAuthorizationHeader() {
        return ConcurConstants.BEARER_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE + expectedAccessToken;
    }

    private boolean urlRepresentsSearchForRequestListing(Matcher endpointMatcher) {
        return matcherFoundContentForNamedCapturingGroup(endpointMatcher, GET_REQUEST_LIST_GROUP);
    }

    private boolean urlRepresentsSearchForSingleRequest(Matcher endpointMatcher) {
        return matcherFoundContentForNamedCapturingGroup(endpointMatcher, GET_REQUEST_GROUP);
    }

    private boolean matcherFoundContentForNamedCapturingGroup(Matcher matcher, String groupName) {
        int substringStartIndex = matcher.start(groupName);
        return substringStartIndex >= 0;
    }

    private void handleSearchForRequestListing(ClassicHttpRequest request, ClassicHttpResponse response, String url) {
        assertRequestHasCorrectHttpMethod(request, HttpMethod.GET);
        Map<String, String> queryParameters = ConcurFixtureUtils.getQueryParametersFromUrl(url);
        try {
            ConcurRequestV4ListingDTO result = mockBackendServer.findRequests(queryParameters);
            String jsonResult = convertObjectToJsonString(result);
            response.setEntity(new StringEntity(jsonResult, ContentType.APPLICATION_JSON));
            response.setCode(HttpStatus.SC_OK);
        } catch (IllegalArgumentException e) {
            String errorResponse = buildErrorResponse(e.getMessage());
            response.setEntity(new StringEntity(errorResponse, ContentType.APPLICATION_JSON));
            response.setCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private void handleSearchForSingleRequest(ClassicHttpRequest request, ClassicHttpResponse response, Matcher endpointMatcher) {
        assertRequestHasCorrectHttpMethod(request, HttpMethod.GET);
        String requestUuid = endpointMatcher.group(REQUEST_UUID_GROUP);
        assertTrue(StringUtils.isNotBlank(requestUuid), "Path parameter for Request UUID should have been non-blank");
        
        Optional<ConcurRequestV4ReportDTO> result = mockBackendServer.findRequest(requestUuid);
        if (result.isPresent()) {
            String jsonResult = convertObjectToJsonString(result.get());
            response.setEntity(new StringEntity(jsonResult, ContentType.APPLICATION_JSON));
            response.setCode(HttpStatus.SC_OK);
        } else {
            String notFoundResponse = buildErrorResponse(NOT_FOUND_MESSAGE);
            response.setEntity(new StringEntity(notFoundResponse, ContentType.APPLICATION_JSON));
            response.setCode(HttpStatus.SC_NOT_FOUND);
        }
    }

    private String convertObjectToJsonString(Object dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unexpected error encountered while converting DTO to String", e);
        }
    }

    private void markResponseAsEncounteringInternalServerError(ClassicHttpResponse response) {
        String errorResponse = buildErrorResponse(INTERNAL_SERVER_ERROR_MESSAGE);
        response.setEntity(new StringEntity(errorResponse, ContentType.APPLICATION_JSON));
        response.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private String buildErrorResponse(String message) {
        return buildJsonTextFromNode(rootNode -> {
            ObjectNode errorNode = rootNode.objectNode();
            errorNode.put(ERROR_CODE_PROPERTY, TEST_ERROR_CODE);
            errorNode.put(ERROR_MESSAGE_PROPERTY, message);
            
            ArrayNode errorsNode = rootNode.arrayNode(1);
            errorsNode.add(errorNode);
            
            rootNode.set(ERRORS_PROPERTY, errorsNode);
        });
    }

}
