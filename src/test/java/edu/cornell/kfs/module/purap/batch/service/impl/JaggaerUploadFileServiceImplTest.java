package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerUploadSupplierXmlStep;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Header;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierRequestMessage;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.batch.JAXBXmlBatchInputFileTypeBase;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.LogTestAppender;
import edu.cornell.kfs.sys.util.LogTestingUtils;
import edu.cornell.kfs.sys.web.mock.CuLocalServerTestBase;
import jakarta.xml.bind.JAXBException;


public class JaggaerUploadFileServiceImplTest extends CuLocalServerTestBase {
    private static final String TEMP_SUPPLIER_UPLOAD_DIRECTORY = "test/jaggaer/xml/";
    private static final String JAGGAER_TEST_XML_FILE_NAME = TEMP_SUPPLIER_UPLOAD_DIRECTORY + "jaggaerTestFile.xml";
    private static final String JAGGAER_TEST_DONE_FILE_NAME = TEMP_SUPPLIER_UPLOAD_DIRECTORY + "jaggaerTestFile.done";
    private static final String XML = "xml";
    private static final String FILE_TYPE_IDENTIFIER = "jaggaerUploadFileType";
    
    private JaggaerUploadFileServiceImpl jaggaerUploadFileServiceImpl;
    private CUMarshalService cuMarshalService;
    
    private LogTestAppender appender;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Configurator.setLevel(JaggaerUploadFileServiceImpl.class, Level.DEBUG);

        appender = new LogTestAppender();
        Logger.getRootLogger().addAppender(appender);
        
        jaggaerUploadFileServiceImpl = new JaggaerUploadFileServiceImpl();
        jaggaerUploadFileServiceImpl.setJaggaerUploadFileType(buildJAXBXmlBatchInputFileTypeBase());
        
        cuMarshalService = new CUMarshalServiceImpl(); 
        jaggaerUploadFileServiceImpl.setCuMarshalService(cuMarshalService);
        
        jaggaerUploadFileServiceImpl.setBatchInputFileService(new BatchInputFileServiceImpl());
        jaggaerUploadFileServiceImpl.setFileStorageService(new FileSystemFileStorageServiceImpl());
        
        prepareTempDirectory();
    }
    
    private JAXBXmlBatchInputFileTypeBase buildJAXBXmlBatchInputFileTypeBase() {
        JAXBXmlBatchInputFileTypeBase fileType = new JAXBXmlBatchInputFileTypeBase();
        fileType.setDirectoryPath(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        fileType.setFileExtension(XML);
        fileType.setFileTypeIdentifier(FILE_TYPE_IDENTIFIER);
        return fileType;
    }

    
    private void prepareTempDirectory() throws IOException, JAXBException {
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        FileUtils.forceMkdir(uploadedSuppliersDirectory);
        SupplierSyncMessage testMessage = buildTestingSupplierSyncMessage();
        cuMarshalService.marshalObjectToXML(testMessage, JAGGAER_TEST_XML_FILE_NAME);
        File doneFile = new File(JAGGAER_TEST_DONE_FILE_NAME);
        doneFile.createNewFile();
    }
    
    private SupplierSyncMessage buildTestingSupplierSyncMessage() {
        SupplierSyncMessage message = new SupplierSyncMessage();
        message.setVersion(JaggaerConstants.XML_VERSION);
        message.setHeader(buildHeader());
        SupplierRequestMessage supplierRequest = new SupplierRequestMessage();
        message.getSupplierSyncMessageItems().add(supplierRequest);
        return message;
    }
    
    private Header buildHeader() {
        Header header = new Header();
        header.setAuthentication(null);
        header.setMessageId(UUID.randomUUID().toString());
        header.setRelatedMessageId(UUID.randomUUID().toString());
        return header;
    }

    @Override
    @AfterEach
    public void shutDown() throws Exception {
        super.shutDown();
        jaggaerUploadFileServiceImpl = null;
        cuMarshalService = null;
        //logCaptor = null;
        deleteTemporaryFileDirectory();
    }
    
    private void deleteTemporaryFileDirectory() throws Exception {
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        if (uploadedSuppliersDirectory.exists() && uploadedSuppliersDirectory.isDirectory()) {
            FileUtils.forceDelete(uploadedSuppliersDirectory.getAbsoluteFile());
        }
    }

    @Test
    public void testUploadSupplierXMLFiles() {
        boolean shouldUploadFiles = false;
        jaggaerUploadFileServiceImpl.setParameterService(buildMockParameterService(shouldUploadFiles));
        jaggaerUploadFileServiceImpl.uploadSupplierXMLFiles();
        String expectedInfoMessage = "uploadSupplierXMLFiles. uploading to Jaggaer is turned off, just remove the DONE file for test/jaggaer/xml/jaggaerTestFile.xml";
        assertTrue(LogTestingUtils.doesLogEntryExist(appender.getLog(), expectedInfoMessage));
        
    }
    
    private ParameterService buildMockParameterService(boolean shouldUploadFiles) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsBoolean(JaggaerUploadSupplierXmlStep.class, CUPurapParameterConstants.JAGGAER_ENABLE_UPLOAD_FILES)).thenReturn(shouldUploadFiles);
        return service;
    }

}
