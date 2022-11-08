package edu.cornell.kfs.concur.web.mock;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2;
import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2.FormFieldKeys;
import edu.cornell.kfs.concur.ConcurTestConstants.CredentialTestValues;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurOAuth2TokenResponseDTO;
import edu.cornell.kfs.sys.CUKFSConstants;

@RestController
public class MockConcurAuthenticationEndpointController {

    private static final String CODE_PROPERTY = "code";
    private static final String ERROR_PROPERTY = "error";
    private static final String ERROR_DESCRIPTION_PROPERTY = "error_description";
    private static final String GEOLOCATION_PROPERTY = "geolocation";

    private static final int ERROR_CODE_1 = 1;
    private static final String ERROR_INVALID_REQUEST = "invalid_request";

    private static final String REGION_VARIABLE = "region";
    private static final String REGION_US = "us";
    private static final String REGION_US2 = "us2";

    private final AtomicReference<String> currentRefreshToken;
    private final AtomicReference<String> currentGeolocation;
    private final AtomicBoolean forceNewRefreshToken;
    private final AtomicBoolean forceInternalServerError;
    private final AtomicBoolean notifiedClientOfGeolocationChange;
    private final Set<String> activeAccessTokens;

    public MockConcurAuthenticationEndpointController() {
        this.currentRefreshToken = new AtomicReference<>();
        this.currentGeolocation = new AtomicReference<>();
        this.forceNewRefreshToken = new AtomicBoolean(false);
        this.forceInternalServerError = new AtomicBoolean(false);
        this.notifiedClientOfGeolocationChange = new AtomicBoolean(false);
        this.activeAccessTokens = ConcurrentHashMap.newKeySet();
    }

    @PostMapping(
            path = "/{region:\\w+}.api.concursolutions.com/oauth2/v0/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ConcurOAuth2TokenResponseDTO> getNewBearerToken(
            HttpServletRequest request, @PathVariable(REGION_VARIABLE) String region) {
        String currentGeolocationSuffix = CUKFSConstants.SLASH + StringUtils.substringAfterLast(
                currentGeolocation.get(), CUKFSConstants.SLASH);
        boolean requestInvokedCurrentGeolocation = StringUtils.startsWithIgnoreCase(
                request.getRequestURI(), currentGeolocationSuffix);
        
        if (!StringUtils.equalsAnyIgnoreCase(region, REGION_US, REGION_US2)
                || (!requestInvokedCurrentGeolocation && notifiedClientOfGeolocationChange.get())) {
            return ResponseEntity.notFound().build();
        } else if (forceInternalServerError.get()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Forced Server-Side Error");
        }
        
        checkRequestParameters(request);
        if (!requestInvokedCurrentGeolocation) {
            notifiedClientOfGeolocationChange.set(true);
        }
        if (forceNewRefreshToken.get()) {
            String newRefreshToken = UUID.randomUUID().toString();
            currentRefreshToken.set(newRefreshToken);
        }
        
        String newAccessToken = UUID.randomUUID().toString();
        activeAccessTokens.add(newAccessToken);
        ConcurOAuth2TokenResponseDTO response = createTokenResponse(newAccessToken);
        return ResponseEntity.ok(response);
    }

    private void checkRequestParameters(HttpServletRequest request) {
        String grantType = request.getParameter(FormFieldKeys.GRANT_TYPE);
        String[][] expectedParameters;
        
        if (StringUtils.equals(grantType, ConcurOAuth2.GRANT_TYPE_REFRESH_TOKEN_VALUE)) {
            expectedParameters = new String[][] {
                    {FormFieldKeys.CLIENT_ID, CredentialTestValues.CLIENT_ID},
                    {FormFieldKeys.CLIENT_SECRET, CredentialTestValues.CLIENT_SECRET},
                    {FormFieldKeys.REFRESH_TOKEN, currentRefreshToken.get()}
            };
        } else if (StringUtils.equals(grantType, ConcurOAuth2.GRANT_TYPE_PASSWORD_VALUE)) {
            expectedParameters = new String[][] {
                    {FormFieldKeys.CLIENT_ID, CredentialTestValues.CLIENT_ID},
                    {FormFieldKeys.CLIENT_SECRET, CredentialTestValues.CLIENT_SECRET},
                    {FormFieldKeys.CREDTYPE, ConcurOAuth2.CRED_TYPE_AUTHTOKEN_VALUE},
                    {FormFieldKeys.USER_NAME, CredentialTestValues.USERNAME},
                    {FormFieldKeys.PASSWORD, CredentialTestValues.PASSWORD}
            };
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    FormFieldKeys.GRANT_TYPE + " is missing or invalid");
        }

        for (String[] expectedParameter : expectedParameters) {
            String parameterName = expectedParameter[0];
            String expectedValue = expectedParameter[1];
            String actualValue = request.getParameter(parameterName);
            if (!StringUtils.equals(expectedValue, actualValue)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        parameterName + " has a missing or invalid value");
            }
        }
    }

    private ConcurOAuth2TokenResponseDTO createTokenResponse(String accessToken) {
        ConcurOAuth2TokenResponseDTO response = new ConcurOAuth2TokenResponseDTO();
        response.setExpires_in(CredentialTestValues.EXPIRES_IN);
        response.setScope(CredentialTestValues.SCOPE);
        response.setToken_type(ConcurConstants.BEARER_AUTHENTICATION_SCHEME);
        response.setAccess_token(accessToken);
        response.setRefresh_token(currentRefreshToken.get());
        response.setRefresh_expires_in(CredentialTestValues.REFRESH_EXPIRES_IN);
        response.setGeolocation(currentGeolocation.get());
        response.setId_token(CredentialTestValues.ID_TOKEN);
        return response;
    }

    @ExceptionHandler
    public ResponseEntity<ObjectNode> handleTokenError(ResponseStatusException exception) {
        HttpStatus httpStatus = exception.getStatus();
        
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode jsonNode = nodeFactory.objectNode();
        jsonNode.put(CODE_PROPERTY, ERROR_CODE_1);
        jsonNode.put(ERROR_PROPERTY, ERROR_INVALID_REQUEST);
        jsonNode.put(ERROR_DESCRIPTION_PROPERTY, exception.getReason());
        jsonNode.put(GEOLOCATION_PROPERTY, currentGeolocation.get());
        
        return ResponseEntity.status(httpStatus)
                .body(jsonNode);
    }

    public boolean isAccessTokenActive(String accessToken) {
        return activeAccessTokens.contains(accessToken);
    }

    public String getCurrentRefreshToken() {
        return currentRefreshToken.get();
    }

    public void setCurrentRefreshToken(String currentRefreshToken) {
        this.currentRefreshToken.set(currentRefreshToken);
    }

    public String getCurrentGeolocation() {
        return currentGeolocation.get();
    }

    public void setCurrentGeolocation(String currentGeolocation) {
        this.currentGeolocation.set(currentGeolocation);
    }

    public void setForceNewRefreshToken(boolean forceNewRefreshToken) {
        this.forceNewRefreshToken.set(forceNewRefreshToken);
    }

    public void setForceInternalServerError(boolean forceInternalServerError) {
        this.forceInternalServerError.set(forceInternalServerError);
    }

}
