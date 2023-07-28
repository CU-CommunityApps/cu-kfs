package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerUploadSupplierXmlStep;
import edu.cornell.kfs.module.purap.batch.service.JaggaerUploadFileService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.ErrorMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierResponseMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.CUMarshalService;
import jakarta.xml.bind.JAXBException;

public class JaggaerUploadFileServiceImpl implements JaggaerUploadFileService {
    private static final Logger LOG = LogManager.getLogger();

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType jaggaerUploadFileType;
    protected FileStorageService fileStorageService;
    protected CUMarshalService cuMarshalService;
    protected ParameterService parameterService;
    protected ConfigurationService configurationService;
    
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
        return getParameterValueBoolean(CUPurapParameterConstants.JAGGAER_ENABLE_UPLOAD_FILES);
    }
    
    private JaggaerUploadFileResultsDTO uploadJaggaerXmlFile(String jaggaerXmlFileName) {
        JaggaerUploadFileResultsDTO results = new JaggaerUploadFileResultsDTO();
        results.setFileName(jaggaerXmlFileName);
        
        boolean successfulCall = false;
        int numberOfAttempts = 1;
        
        while (!successfulCall && numberOfAttempts < getMaximumNumberOfRetries()) {
            try {
                WebClient webClient = WebClient.create();
                String responseString = webClient.post()
                        .uri(buildSupplierUploadURI())
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(buildPostingStringFromJaggaerFile(jaggaerXmlFileName), String.class)
                        .exchange()
                        .block()
                        .bodyToMono(String.class)
                        .block();
                
                if (StringUtils.isNotBlank(responseString)) {
                    processSuccessfulResponse(results, responseString);
                    successfulCall = true; 
                } else {
                    processUnsuccessfulResponse(results, numberOfAttempts);
                    numberOfAttempts++;
                }
            } catch (Throwable e) {
                LOG.error("JaggaerUploadFileResultsDTO, got an exception calling the Jaggaer Web Service", e);
                processUnsuccessfulResponse(results, numberOfAttempts);
                numberOfAttempts++;
            }
            

        }
        return results;
    }
    
    private int getMaximumNumberOfRetries() {
        String countString = getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_RETRY_COUNT);
        return Integer.valueOf(countString);
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
        LOG.info("processSuccessfulResponse, responseString {}", responseString);
        SupplierResponseMessage responseMessage = buildSupplierResponseMessage(responseString);
        
        results.setResponseCode(responseMessage.getStatus().getStatusCode());
        results.setMessage(responseMessage.getStatus().getStatusText());
        results.setFileProcessedByJaggaer(true);
        
        if (responseMessage.getStatus().getErrorMessages().isEmpty()) {
            results.setErrorMessage(StringUtils.EMPTY);
        } else {
            StringBuilder combinedErrorMessage = new StringBuilder();
            for (ErrorMessage errorMessage : responseMessage.getStatus().getErrorMessages()) {
                if (combinedErrorMessage.length() != 0) {
                    combinedErrorMessage.append(KFSConstants.NEWLINE);
                }
                combinedErrorMessage.append(buildErrorMessageStringForReport(errorMessage));
            }
            results.setErrorMessage(combinedErrorMessage.toString());
        }
    }
    
    private SupplierResponseMessage buildSupplierResponseMessage(String responseString) {
        SupplierSyncMessage syncMessage;
        try {
            syncMessage = cuMarshalService.unmarshalStringIgnoreDtd(responseString, SupplierSyncMessage.class);
        } catch (JAXBException | XMLStreamException | IOException e) {
            LOG.error("buildSupplierResponseMessage, got an error creating SupplierSyncMessage from the response string", e);
            throw new RuntimeException(e);
        }
        SupplierResponseMessage responseMessage = (SupplierResponseMessage) syncMessage.getSupplierSyncMessageItems().get(0);
        return responseMessage;
    }
    
    private String buildErrorMessageStringForReport(ErrorMessage errorMessage) {
        String type = StringUtils.trim(errorMessage.getType());
        String message = StringUtils.trim(errorMessage.getValue());
        return MessageFormat.format(configurationService.getPropertyValueAsString(CUPurapKeyConstants.JAGGAER_UPLOAD_XML_ERROR_MESSAGE), type, message);
    }
    
    private void processUnsuccessfulResponse(JaggaerUploadFileResultsDTO results, int numberOfAttempts) {
        LOG.error("processUnsuccessfulResponse, attempt number {}, had an unsuccessful webservice POST call", numberOfAttempts);
        results.setFileProcessedByJaggaer(false);
        results.setErrorMessage(configurationService.getPropertyValueAsString(CUPurapKeyConstants.JAGGAER_UPLOAD_WEBSERVICE_ERROR));
    }

    private URI buildSupplierUploadURI() {
        String url = getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_ENDPOINT);
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            LOG.error("buildSupplierUploadURI(): URL: " + url, e);
            throw new RuntimeException(e);
        }
    }
    
    private void generateResultsReport(List<JaggaerUploadFileResultsDTO> resultsList) {
        /*
         * A full report will be implemented on Jira KFSPTS-29084
         * Until then, this will produce a nice summary at the bottom of the log file
         */
        for (JaggaerUploadFileResultsDTO result : resultsList) {
            LOG.info("generateResultsReport, results: {}", result.toString());
        }
    }
    
    protected String getParameterValueString(String parameterName) {
        return parameterService.getParameterValueAsString(JaggaerUploadSupplierXmlStep.class, parameterName);
    }

    protected boolean getParameterValueBoolean(String parameterName) {
        return parameterService.getParameterValueAsBoolean(JaggaerUploadSupplierXmlStep.class, parameterName);
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
