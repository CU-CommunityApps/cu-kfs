package edu.cornell.kfs.concur.rest.resource;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurTestConstants.MockLegacyAuthConstants;
import edu.cornell.kfs.concur.MockConcurErrorDTO;
import edu.cornell.kfs.concur.rest.xmlObjects.AccessTokenDTO;
import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * JAX-RS resource for imitating Concur's legacy authentication API endpoint.
 * Mocks the needed parts of the API, based on the behavior described on this Concur page:
 * 
 * https://developer.concur.com/api-reference/authentication/authorization-pre-2017.html
 * 
 * NOTE: This resource's error responses do not necessarily match the status codes
 * or error content that the actual Concur server would respond with.
 */
@Path(MockLegacyAuthConstants.BASE_RESOURCE_PATH)
public class MockConcurLegacyAuthenticationServerResource {

    private static final Logger LOG = LogManager.getLogger();

    private static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed";

    private String baseUri;
    private String validClientId;
    private String validClientSecret;
    private String validEncodedCredentials;
    private ConcurrentMap<String, AccessTokenDTO> currentTokens;
    private DateTimeFormatter dateTimeFormatter;

    public MockConcurLegacyAuthenticationServerResource() {
        this.currentTokens = new ConcurrentHashMap<>();
    }

    @GET
    @Path(MockLegacyAuthConstants.REQUEST_TOKEN_PATH)
    @Produces(MediaType.APPLICATION_XML)
    public Response requestAccessToken(@Context HttpHeaders headers) {
        try {
            String clientId = getRequiredHeader(ConcurConstants.CONSUMER_KEY_PROPERTY, headers);
            String authorizationHeader = getRequiredHeader(ConcurConstants.AUTHORIZATION_PROPERTY, headers);
            validateBasicCredentials(authorizationHeader);
            if (!StringUtils.equals(validClientId, clientId)) {
                throw buildUnauthorizedException(AUTHENTICATION_FAILED_MESSAGE);
            }
            
            AccessTokenDTO tokenDTO = buildAndStoreNewTokenDTO();
            currentTokens.put(tokenDTO.getToken(), tokenDTO);
            return Response.ok(tokenDTO).build();
        } catch (Exception e) {
            LOG.error("requestAccessToken: Preparing error response due to exception", e);
            return buildErrorResponseFromException(e);
        }
    }

    @GET
    @Path(MockLegacyAuthConstants.REFRESH_TOKEN_PATH)
    @Produces(MediaType.APPLICATION_XML)
    public Response refreshAccessToken(
            @QueryParam(ConcurConstants.REFRESH_TOKEN_URL_PARAM) String refreshToken,
            @QueryParam(ConcurConstants.CLIENT_ID_URL_PARAM) String clientId,
            @QueryParam(ConcurConstants.CLIENT_SECRET_URL_PARAM) String clientSecret,
            @Context HttpHeaders headers) {
        try {
            checkRequiredParam(ConcurConstants.REFRESH_TOKEN_URL_PARAM, refreshToken);
            checkRequiredParam(ConcurConstants.CLIENT_ID_URL_PARAM, clientId);
            checkRequiredParam(ConcurConstants.CLIENT_SECRET_URL_PARAM, clientSecret);
            
            String authorizationHeader = getRequiredHeader(ConcurConstants.AUTHORIZATION_PROPERTY, headers);
            AccessTokenDTO oldTokenDTO = getAndValidateAccessToken(authorizationHeader);
            if (!StringUtils.equals(validClientId, clientId)
                    || !StringUtils.equals(validClientSecret, clientSecret)
                    || !StringUtils.equals(oldTokenDTO.getRefreshToken(), refreshToken)) {
                throw buildUnauthorizedException(AUTHENTICATION_FAILED_MESSAGE);
            }
            
            AccessTokenDTO newTokenDTO = buildNewTokenDTOWithGivenRefreshToken(oldTokenDTO.getRefreshToken());
            currentTokens.remove(oldTokenDTO.getToken());
            currentTokens.put(newTokenDTO.getToken(), newTokenDTO);
            
            AccessTokenDTO tokenDTOForResponse = buildTokenDTOCopyWithoutRefreshToken(newTokenDTO);
            return Response.ok(tokenDTOForResponse).build();
        } catch (Exception e) {
            LOG.error("refreshAccessToken: Preparing error response due to exception", e);
            return buildErrorResponseFromException(e);
        }
    }

    @POST
    @Path(MockLegacyAuthConstants.REVOKE_TOKEN_PATH)
    public Response revokeAccessToken(
            @QueryParam(ConcurConstants.TOKEN_URL_PARAM) String token,
            @Context HttpHeaders headers) {
        try {
            checkRequiredParam(ConcurConstants.TOKEN_URL_PARAM, token);
            String authorizationHeader = getRequiredHeader(ConcurConstants.AUTHORIZATION_PROPERTY, headers);
            AccessTokenDTO oldTokenDTO = getAndValidateAccessToken(authorizationHeader);
            if (!StringUtils.equals(oldTokenDTO.getToken(), token)) {
                throw buildUnauthorizedException(AUTHENTICATION_FAILED_MESSAGE);
            }
            currentTokens.remove(oldTokenDTO.getToken());
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("revokeAccessToken: Preparing error response due to exception", e);
            return buildErrorResponseFromException(e);
        }
    }

    public AccessTokenDTO buildAndStoreNewTokenDTO() {
        String refreshToken = UUID.randomUUID().toString();
        AccessTokenDTO newTokenDTO = buildNewTokenDTOWithGivenRefreshToken(refreshToken);
        currentTokens.put(newTokenDTO.getToken(), newTokenDTO);
        return newTokenDTO;
    }

    private AccessTokenDTO buildNewTokenDTOWithGivenRefreshToken(String refreshToken) {
        String accessToken = UUID.randomUUID().toString();
        
        MutableDateTime expirationDateTime = MutableDateTime.now();
        expirationDateTime.setTime(0, 0, 0, 0);
        expirationDateTime.addMonths(6);
        
        AccessTokenDTO tokenDTO = new AccessTokenDTO();
        tokenDTO.setInstanceURL(baseUri + CUKFSConstants.SLASH);
        tokenDTO.setToken(accessToken);
        tokenDTO.setRefreshToken(refreshToken);
        tokenDTO.setExpirationDate(dateTimeFormatter.print(expirationDateTime));
        return tokenDTO;
    }

    private AccessTokenDTO buildTokenDTOCopyWithoutRefreshToken(AccessTokenDTO oldTokenDTO) {
        AccessTokenDTO newTokenDTO = new AccessTokenDTO();
        newTokenDTO.setInstanceURL(oldTokenDTO.getInstanceURL());
        newTokenDTO.setToken(oldTokenDTO.getToken());
        newTokenDTO.setRefreshToken(null);
        newTokenDTO.setExpirationDate(oldTokenDTO.getExpirationDate());
        return newTokenDTO;
    }

    private void validateBasicCredentials(String authHeaderValue) throws ClientErrorException {
        String encodedCredentials = StringUtils.substringAfter(authHeaderValue,
                ConcurConstants.BASIC_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE);
        if (StringUtils.isBlank(encodedCredentials)) {
            throw buildUnauthorizedException("Authentication credentials are missing or malformed");
        } else if (!StringUtils.equals(validEncodedCredentials, encodedCredentials)) {
            throw buildUnauthorizedException(AUTHENTICATION_FAILED_MESSAGE);
        }
    }

    private AccessTokenDTO getAndValidateAccessToken(String authHeaderValue) throws ClientErrorException {
        String accessTokenString = StringUtils.substringAfter(authHeaderValue,
                ConcurConstants.OAUTH_AUTHENTICATION_SCHEME + KFSConstants.BLANK_SPACE);
        if (StringUtils.isBlank(accessTokenString)) {
            throw buildUnauthorizedException("Access token is missing or malformed");
        }
        AccessTokenDTO accessToken = currentTokens.get(accessTokenString);
        if (accessToken == null) {
            throw buildUnauthorizedException(AUTHENTICATION_FAILED_MESSAGE);
        }
        return accessToken;
    }

    private String getRequiredHeader(String headerName, HttpHeaders headers) throws ClientErrorException {
        String headerValue = headers.getHeaderString(headerName);
        if (StringUtils.isNotBlank(headerValue)) {
            return headerValue;
        } else {
            throw buildBadRequestException("Unexpected blank header: " + headerName);
        }
    }

    private void checkRequiredParam(String paramName, String paramValue) throws ClientErrorException {
        if (StringUtils.isBlank(paramValue)) {
            throw buildBadRequestException("Unexpected blank param: " + paramName);
        }
    }

    private ClientErrorException buildBadRequestException(String message) {
        return buildClientException(Response.Status.BAD_REQUEST, message);
    }

    private ClientErrorException buildUnauthorizedException(String message) {
        return buildClientException(Response.Status.UNAUTHORIZED, message);
    }

    private ClientErrorException buildClientException(Response.Status status, String message) {
        return new ClientErrorException(message, buildErrorResponse(status, message));
    }

    private Response buildErrorResponseFromException(Exception exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        } else {
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    private Response buildErrorResponse(Response.Status status, String message) {
        MockConcurErrorDTO errorDTO = new MockConcurErrorDTO(message);
        return Response.status(status)
                .entity(errorDTO)
                .type(MediaType.APPLICATION_XML)
                .build();
    }

    public Optional<AccessTokenDTO> getTokenDTOByAccessToken(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(currentTokens.get(accessToken));
        }
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getValidClientId() {
        return validClientId;
    }

    public void setValidClientId(String validClientId) {
        this.validClientId = validClientId;
    }

    public String getValidClientSecret() {
        return validClientSecret;
    }

    public void setValidClientSecret(String validClientSecret) {
        this.validClientSecret = validClientSecret;
    }

    public String getValidEncodedCredentials() {
        return validEncodedCredentials;
    }

    public void setValidEncodedCredentials(String validEncodedCredentials) {
        this.validEncodedCredentials = validEncodedCredentials;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

}
