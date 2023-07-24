package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.module.purap.batch.service.JaggaerUploadFileService;
import edu.cornell.kfs.sys.service.impl.DisposableClientServiceImplBase;
import edu.cornell.kfs.sys.web.CuMultiPartWriter;

public class JaggaerUploadFileServiceImpl extends DisposableClientServiceImplBase implements JaggaerUploadFileService, Serializable {
    private static final long serialVersionUID = 8561874934736620072L;
    private static final Logger LOG = LogManager.getLogger();

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType jaggaerUploadFileType;
    protected FileStorageService fileStorageService;
    
    @Override
    protected Client getClient() {
        return super.getClient(CuMultiPartWriter.class);
    }
    
    @Override
    public void uploadSupplierXMLFiles() {
        List<String> jaggaerUploadXmlFileNames = batchInputFileService
                .listInputFileNamesWithDoneFile(jaggaerUploadFileType);
        LOG.info("uploadSupplierXMLFiles, found {} files to upload", jaggaerUploadXmlFileNames.size());

        for (String jaggaerXmlFileName : jaggaerUploadXmlFileNames) {
            if (shouldUploadFilesToJaggaer()) {
                LOG.info("uploadSupplierXMLFiles. processing {}", jaggaerXmlFileName);
                uploadJaggaerXmlFile(jaggaerXmlFileName);
            } else {
                LOG.info("uploadSupplierXMLFiles. uploading to Jaggaer is turned off, just remove the DONE file for {}",
                        jaggaerXmlFileName);
            }
        }
        fileStorageService.removeDoneFiles(jaggaerUploadXmlFileNames);
    }

    protected boolean shouldUploadFilesToJaggaer() {
        /*
         * @todo this should be pulled from a parameter
         */
        return true;
    }
    
    private void uploadJaggaerXmlFile(String jaggaerXmlFileName) {
        Client client = getClient();
        
        File jaggaerFile = new File(jaggaerXmlFileName);
        String contentToPost;
        try {
            contentToPost = FileUtils.readFileToString(jaggaerFile, Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error("uploadJaggaerXmlFile, unable to read file " + jaggaerXmlFileName, e);
            throw new RuntimeException(e);
        }
        LOG.debug("uploadJaggaerXmlFile, about to post {}", contentToPost);
        
        Response response = client
                .target(buildSupplierUploadURI())
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.text(contentToPost));
        
        if (response.getStatus() == 200) {
            String responseString = response.readEntity(String.class);
            LOG.info("uploadJaggaerXmlFile, responseString {}", responseString);
        } else {
            LOG.error("uploadJaggaerXmlFile, response code was not 200 {}", response);
        }
        
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

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setJaggaerUploadFileType(BatchInputFileType jaggaerUploadFileType) {
        this.jaggaerUploadFileType = jaggaerUploadFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

}
