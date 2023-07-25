package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.module.purap.batch.service.JaggaerUploadFileService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierResponseMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import edu.cornell.kfs.sys.web.CuMultiPartWriter;
import jakarta.xml.bind.JAXBException;
import liquibase.repackaged.org.apache.commons.lang3.StringUtils;

public class JaggaerUploadFileServiceImpl extends DisposableClientServiceImplBase implements JaggaerUploadFileService, Serializable {
    private static final long serialVersionUID = 8561874934736620072L;
    private static final Logger LOG = LogManager.getLogger();

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType jaggaerUploadFileType;
    protected FileStorageService fileStorageService;
    protected CUMarshalService cuMarshalService;
    protected ParameterService parameterService;
    protected ConfigurationService configurationService;
    
    @Override
    protected Client getClient() {
        return super.getClient(CuMultiPartWriter.class);
    }
    
    @Override
    public void uploadSupplierXMLFiles() {
        List<String> jaggaerUploadXmlFileNames = batchInputFileService
                .listInputFileNamesWithDoneFile(jaggaerUploadFileType);
        LOG.info("uploadSupplierXMLFiles, found {} files to upload", jaggaerUploadXmlFileNames.size());
        
        List<JaggaerUploadFileResultsDTO> resultsList = new ArrayList<JaggaerUploadFileResultsDTO>();

        for (String jaggaerXmlFileName : jaggaerUploadXmlFileNames) {
            if (shouldUploadFilesToJaggaer()) {
                LOG.info("uploadSupplierXMLFiles. processing {}", jaggaerXmlFileName);
                JaggaerUploadFileResultsDTO results = uploadJaggaerXmlFile(jaggaerXmlFileName);
                resultsList.add(results);
            } else {
                LOG.info("uploadSupplierXMLFiles. uploading to Jaggaer is turned off, just remove the DONE file for {}",
                        jaggaerXmlFileName);
            }
        }
        
        if (shouldUploadFilesToJaggaer()) {
            generateResultsReport(resultsList);
        }
        
        fileStorageService.removeDoneFiles(jaggaerUploadXmlFileNames);
    }

    protected boolean shouldUploadFilesToJaggaer() {
        /*
         * @todo this should be pulled from a parameter
         */
        return true;
    }
    
    private JaggaerUploadFileResultsDTO uploadJaggaerXmlFile(String jaggaerXmlFileName) {
        JaggaerUploadFileResultsDTO results = new JaggaerUploadFileResultsDTO();
        results.setFileName(jaggaerXmlFileName);
        
        boolean successfulCall = false;
        int numberOfAttempts = 1;
        
        while (!successfulCall && numberOfAttempts < getMaximumNumberOfRetries()) {
            Client client = getClient();
            
            Response response = client
                    .target(buildSupplierUploadURI())
                    .request()
                    .accept(MediaType.APPLICATION_XML)
                    .post(Entity.text(buildPostingStringFromJaggaerFile(jaggaerXmlFileName)));
            
            String responseString = response.readEntity(String.class);
            
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                processSuccessfulResponse(results, responseString);
                successfulCall = true;
            } else {
                processUnsuccessfulResponse(results, numberOfAttempts, response);
                numberOfAttempts++;
                waitBetweenTries();
            }
        }
        return results;
    }
    
    private int getMaximumNumberOfRetries() {
        /*
         * @todo full this parameter.  Use WEBSERVICE_MAX_RETRIES with purap namespace
         */
        return 3;
    }
    
    private String buildPostingStringFromJaggaerFile(String jaggaerXmlFileName) {
        File jaggaerFile = new File(jaggaerXmlFileName);
        String contentToPost;
        try {
            contentToPost = FileUtils.readFileToString(jaggaerFile, Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error("buildPostingStringFromJaggaerFile, unable to read file " + jaggaerXmlFileName, e);
            throw new RuntimeException(e);
        }
        LOG.debug("buildPostingStringFromJaggaerFile, about to post {}", contentToPost);
        return contentToPost;
    }

    private void processSuccessfulResponse(JaggaerUploadFileResultsDTO results, String responseString) {
        LOG.debug("processSuccessfulResponse, responseString {}", responseString);
        SupplierResponseMessage responseMessage = buildSupplierResponseMessage(responseString);
        
        results.setResponseCode(responseMessage.getStatus().getStatusCode());
        results.setMessage(responseMessage.getStatus().getStatusText());
        results.setFileProcessedByJaggaer(true);
        results.setErrorMessage(StringUtils.EMPTY);
    }
    
    private SupplierResponseMessage buildSupplierResponseMessage(String responseString) {
        SupplierSyncMessage synchMessage;
        try {
            synchMessage = cuMarshalService.unmarshalStringIgnoreDtd(responseString, SupplierSyncMessage.class);
        } catch (JAXBException | XMLStreamException | IOException e) {
            LOG.error("buildSupplierResponseMessage, got an error creating SupplierSyncMessage from the response string", e);
            throw new RuntimeException(e);
        }
        SupplierResponseMessage responseMessage = (SupplierResponseMessage) synchMessage.getSupplierSyncMessageItems().get(0);
        return responseMessage;
    }
    
    private void processUnsuccessfulResponse(JaggaerUploadFileResultsDTO results, int numberOfAttempts,
            Response response) {
        LOG.error("processUnsuccessfulResponse, attempt number {}, had an unsuccessful webservice call.  Response status was {}", 
                numberOfAttempts, response.getStatus());
        results.setFileProcessedByJaggaer(false);
        results.setResponseCode(String.valueOf(response.getStatus()));
        results.setErrorMessage(getDefaultErrorMessageWhenThereIsWebServiceError());
    }

    private URI buildSupplierUploadURI() {
        /*
         * @todo pull this URL from a parameter
         */
        String url = "https://usertest-messages.sciquest.com/apps/Router/TSMSupplierXMLImport";
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            LOG.error("buildSupplierUploadURI(): URL: " + url, e);
            throw new RuntimeException(e);
        }
    }
    
    private String getDefaultErrorMessageWhenThereIsWebServiceError() {
        /*
         * @todo pull this from application resources
         */
        return "There was an error calling the Jaggaer upload service";
    }
    
    private void waitBetweenTries() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            LOG.error("waitBetweenTries, had an error waiting", e);
        }
    }
    
    private void generateResultsReport(List<JaggaerUploadFileResultsDTO> resultsList) {
        for (JaggaerUploadFileResultsDTO result : resultsList) {
            LOG.info("generateResultsReport, results: {}", result.toString());
        }
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setJaggaerUploadFileType(BatchInputFileType jaggaerUploadFileType) {
        this.jaggaerUploadFileType = jaggaerUploadFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
