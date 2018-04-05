package edu.cornell.kfs.fp.batch.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.CloudCheckrService;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class CloudCheckrServiceImpl extends DisposableClientServiceImplBase implements CloudCheckrService  {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CloudCheckrServiceImpl.class);
    
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public CloudCheckrWrapper getCloudCheckrWrapper(String startDate, String endDate) throws URISyntaxException, IOException {
        String cloudCheckrURL = findCloudCheckrEndPoint();
        String formattedUrl = MessageFormat.format(cloudCheckrURL, startDate, endDate);
        Response response = null;
        try {
            Invocation request = buildReportDetailsClientRequest(formattedUrl);
            response = request.invoke();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(CloudCheckrWrapper.class);
            } else {
                LOG.error("getCloudCheckrWrapper, error calling enpoint.  The HTTP response was " + response.getStatus());
                throw new IOException("Invalid response code: " + response.getStatus());
            }
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }
    
    private String findCloudCheckrEndPoint() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuFPConstants.CLOUDCHECKR.CLOUDCHECKR_CREDENTIAL_GROUP_CODE, 
                CuFPConstants.CLOUDCHECKR.CLOUDCHECKR_ENDPOINT_CREDENTIAL_KEY);
    }
    
    protected Invocation buildReportDetailsClientRequest(String url) throws URISyntaxException {
        URI uri = new URI(url);
        Builder builder = getClient().target(uri).request();
        return builder.buildGet();
    }

    @Override
    public DefaultKfsAccountForAwsResultWrapper getDefaultKfsAccountForAwsResultWrapper() throws URISyntaxException, IOException {
        String defaultAccountUrl = findDefaultAccountServiceEndPoint();
        Response response = null;
        try {
            Invocation request = buildClientRequest(defaultAccountUrl, findDefaultAccountServiceXApiKey());
            response = request.invoke();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(DefaultKfsAccountForAwsResultWrapper.class);
            } else {
                LOG.error("getDefaultKfsAccountForAwsResultWrapper, error calling enpoint.  The HTTP response was " + response.getStatus());
                throw new IOException("Invalid response code: " + response.getStatus());
            }
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
    }
    
    private String findDefaultAccountServiceEndPoint() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuFPConstants.CLOUDCHECKR.CLOUDCHECKR_CREDENTIAL_GROUP_CODE, 
                CuFPConstants.CLOUDCHECKR.DEFAULT_KFS_ACCOUNT_ENDPOINT_CREDENTIAL_KEY);
    }
    
    private String findDefaultAccountServiceXApiKey() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_BILL_CREDENTIAL_GROUP_CODE, 
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_BILL_AUTH_HEADER_TOKEN_KEY);
    }
    
    protected Invocation buildClientRequest(String url, String xApiKeyValue) throws URISyntaxException {
        URI uri = new URI(url);
        Builder builder = getClient().target(uri).request();
        builder.header(CuFPConstants.AmazonWebServiceBillingConstants.AWS_BILL_AUTH_HEADER_TOKEN_KEY, xApiKeyValue);
        builder.accept(MediaType.APPLICATION_XML);
        return builder.buildGet();
    }
    
    public String buildAttachmentUrl(String year, String month, String account) {
        if (StringUtils.length(month) < 2) {
            month = "0" + month;
        }
        
        String attachmentUrl = MessageFormat.format(findAttachmentUrlBase(), year, month, account);
        LOG.debug("buildAttachmentUrl, attachment URL: " + attachmentUrl);
        return attachmentUrl;
    }
    
    protected String findAttachmentUrlBase() {
        return webServiceCredentialService.getWebServiceCredentialValue(
                CuFPConstants.CLOUDCHECKR.CLOUDCHECKR_CREDENTIAL_GROUP_CODE, 
                CuFPConstants.CLOUDCHECKR.CLOUDCHECKR_ATTACH_URL);
    }
    
    public void setWebServiceCredentialService(WebServiceCredentialService webServiceCredentialService) {
        this.webServiceCredentialService = webServiceCredentialService;
    }

}
