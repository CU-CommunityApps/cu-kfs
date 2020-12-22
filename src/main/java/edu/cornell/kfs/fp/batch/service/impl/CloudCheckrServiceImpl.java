package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.CloudCheckrService;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class CloudCheckrServiceImpl extends DisposableClientServiceImplBase implements CloudCheckrService  {
    
	private static final Logger LOG = LogManager.getLogger(CloudCheckrServiceImpl.class);
    
    protected WebServiceCredentialService webServiceCredentialService;

    @Override
    public CloudCheckrWrapper getCloudCheckrWrapper(String startDate, String endDate, String masterAccountName) throws URISyntaxException, IOException {
        /*
         * If this needs to be done again, cloudcheckr will send an XML file that is our billing data.  Save the file to your file system, and update
         * the fileName String below to point to the file.
         * 
         * If there are XML parsing exceptions about unexpected tags, compare the XML cloudcheckr sent to the test file cloudcheckr_basic_cornell_test.xml.
         * I had to replace the header rows in the cloudcheckr file with what we had in our test file.
         * 
         * The XML cloudcheckr sends will probably only include details for the Cornell+Master+v2.  In hindsight, creating the "create accounting document" 
         * XML locally would be faster if I updated the parameter AWS_MASTER_ACCOUNTS to only include Cornell+Master+v2 account.
         * 
         * The generate "create accounting document" XML start KFS locally, and run the AWS Billing gbatch job.  That will create 
         * AmazonBill-2020-November-Cornell+Master+v2-TIMESTAMP.xml in your staginging_area/fp/accountingXmlDocumentFolder.  Copy that file
         * into a kfs instance, and have prod control run the create accounting document job.
         */
        String fileName = "/Users/jdh34/kuali/infra/work/staging/fp/cloudcheckr_nov_billing_detail.xml";
        CUMarshalServiceImpl service = new CUMarshalServiceImpl();
        File xmlFile = new File(fileName);
        CloudCheckrWrapper wrapper;
        try {
            wrapper = service.unmarshalFile(xmlFile, CloudCheckrWrapper.class);
        } catch (JAXBException e) {
            LOG.error("getCloudCheckrWrapper, had an error creating POJO", e);
            throw new RuntimeException(e);
        }
        return wrapper;
        /*
        String cloudCheckrURL = findCloudCheckrEndPoint();
        String formattedUrl = MessageFormat.format(cloudCheckrURL, startDate, endDate, masterAccountName);
        Response response = null;
        try {
            Invocation request = buildReportDetailsClientRequest(formattedUrl);
            response = request.invoke();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                return response.readEntity(CloudCheckrWrapper.class);
            } else {
                LOG.error("getCloudCheckrWrapper, error calling endpoint.  The HTTP response was " + response.getStatus());
                throw new IOException("Invalid response code: " + response.getStatus());
            }
        } finally {
            CURestClientUtils.closeQuietly(response);
        }
        */
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
                LOG.error("getDefaultKfsAccountForAwsResultWrapper, error calling endpoint.  The HTTP response was " + response.getStatus());
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
    
    @Override
    public String buildAttachmentUrl(String year, String month, String account, String masterAccountNumber) {
        if (StringUtils.length(month) < 2) {
            month = "0" + month;
        }
        
        String attachmentUrl = MessageFormat.format(findAttachmentUrlBase(), year, month, account, masterAccountNumber);
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
