package edu.cornell.kfs.vnd.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;
import edu.cornell.kfs.vnd.service.CuVendorWorkDayService;

public class CuVendorWorkDayServiceImpl extends DisposableClientServiceImplBase implements CuVendorWorkDayService {
    private static final Logger LOG = LogManager.getLogger();
    private static final String INCLUDE_TERMINATED_WORKERS_URL_PARAM = "Include_Terminated_Workers";
    private static final String SOCIAL_SECURITY_NUMBER_URL_PARAM = "Social_Security_Number";
    private static final String JSON_FORMAT_URL = "format=json";
    
    protected ParameterService parameterService;
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public WorkdayKfsVendorLookupRoot findEmployeeBySocialSecurityNumber(String socialSecurityNumber) throws URISyntaxException {
        LOG.debug("findEmployeeBySocialSecurityNumber, entering");
        Invocation request = buildInvocation(socialSecurityNumber);
        return callWorkDayService(request);
    }

    protected Invocation buildInvocation(String socialSecurityNumber) throws URISyntaxException {
        String webserviceQueryUrl = buildWorkdayServiceCall(socialSecurityNumber);
        String authenticationValue = buildAuthenticationValue();
        URI uri = new URI(webserviceQueryUrl);
        Builder builder = getClient().target(uri).request();
        builder.header(CUKFSConstants.AUTHORIZATION_HEADER_KEY, authenticationValue);
        Invocation request = builder.buildGet();
        return request;
    }
    
    protected String buildWorkdayServiceCall(String socialSecurityNumber) {
        return getWorkdayEndpointBase() + INCLUDE_TERMINATED_WORKERS_URL_PARAM + CUKFSConstants.EQUALS_SIGN
                + getIncludeTerminatedWorkers() + CUKFSConstants.AMPERSAND + SOCIAL_SECURITY_NUMBER_URL_PARAM
                + CUKFSConstants.EQUALS_SIGN + socialSecurityNumber + CUKFSConstants.AMPERSAND + JSON_FORMAT_URL;
    }
    
    protected String buildAuthenticationValue() {
        byte[] byteArrayEncodedCredentials = Base64.encodeBase64(getCredentialValues().getBytes());
        String stringEncodedCredentials = new String(byteArrayEncodedCredentials, StandardCharsets.UTF_8);
        return CUKFSConstants.BASIC_AUTHENTICATION_STARTER + stringEncodedCredentials;
    }
    
    protected WorkdayKfsVendorLookupRoot callWorkDayService(Invocation request) {
        int retryCount = 1;
        int maximumRetries = getMaximumRetries();
        while (retryCount <= maximumRetries) {
            try {
                Response response = request.invoke();
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    String responseString = response.readEntity(String.class);
                    ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
                    WorkdayKfsVendorLookupRoot root = objectMapper.readValue(responseString, WorkdayKfsVendorLookupRoot.class);
                    return root;
                } else {
                    LOG.error("findEmployeeBySocialSecurityNumber, on try {}, got a bad response from workday, the response code was {}", retryCount, response.getStatus());
                }
            } catch (Exception e) {
                LOG.error("findEmployeeBySocialSecurityNumber on try {}, got an error", retryCount, e);
            }
            retryCount++;
        }
        throw new RuntimeException("Unable to call workday endpoint.");
    }
    
    private String getWorkdayEndpointBase() {
        //@todo pull this from a paremter
        return "https://services1.myworkday.com/ccx/service/customreport2/cornell/intsys-HRIS/CRINT127C_KFS_Vendor_Lookup?";
    }
    
    private String getIncludeTerminatedWorkers() {
      //@todo pull this from a paremter
        return "1";
    }
    
    private int getMaximumRetries() {
        //@todo get this from a parameter
        return 5;
    }
    
    private String getCredentialValues() {
        //@todo get this from web service credentials
        return "user:password";
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
