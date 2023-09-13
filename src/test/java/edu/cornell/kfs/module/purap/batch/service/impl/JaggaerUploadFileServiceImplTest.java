package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerUploadSupplierXmlStep;
import edu.cornell.kfs.module.purap.batch.service.impl.MockJaggaerUploadSuppliersEndpoint.JaggaerMockServerCongiration;
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

@Execution(ExecutionMode.SAME_THREAD)
public class JaggaerUploadFileServiceImplTest extends CuLocalServerTestBase {
    private static final String TEMP_SUPPLIER_UPLOAD_DIRECTORY = "test/jaggaer/xml/";
    private static final String JAGGAER_TEST_XML_FILE_NAME = TEMP_SUPPLIER_UPLOAD_DIRECTORY + "jaggaerTestFile.xml";
    private static final String JAGGAER_TEST_DONE_FILE_NAME = TEMP_SUPPLIER_UPLOAD_DIRECTORY + "jaggaerTestFile.done";
    private static final String XML = "xml";
    private static final String FILE_TYPE_IDENTIFIER = "jaggaerUploadFileType";
    private static final String JAGGAER_WEBSERVICE_ERROR_MESSAGE = "There was an error calling the Jaggaer upload service";

    private JaggaerUploadFileServiceImpl jaggaerUploadFileServiceImpl;
    private CUMarshalService cuMarshalService;
    private MockJaggaerUploadSuppliersEndpoint uploadSuppliersEndpoint;
    private String baseServerUrl;

    private LogTestAppender appender;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        appender = new LogTestAppender();
        Logger.getRootLogger().addAppender(appender);

        jaggaerUploadFileServiceImpl = new JaggaerUploadFileServiceImpl();
        jaggaerUploadFileServiceImpl.setJaggaerUploadFileType(buildJAXBXmlBatchInputFileTypeBase());

        cuMarshalService = new CUMarshalServiceImpl();
        jaggaerUploadFileServiceImpl.setCuMarshalService(cuMarshalService);

        jaggaerUploadFileServiceImpl.setBatchInputFileService(new BatchInputFileServiceImpl());
        jaggaerUploadFileServiceImpl.setFileStorageService(new FileSystemFileStorageServiceImpl());
        jaggaerUploadFileServiceImpl.setConfigurationService(buildMockConfigurationService());

        prepareTempDirectory();

        uploadSuppliersEndpoint = new MockJaggaerUploadSuppliersEndpoint(cuMarshalService);
        String pattern = uploadSuppliersEndpoint.getRelativeUrlPatternForHandlerRegistration();
        HttpRequestHandler handler = uploadSuppliersEndpoint;
        server.registerHandler(pattern, handler);
        HttpHost httpHost = start();

        baseServerUrl = httpHost.toURI();
    }

    private JAXBXmlBatchInputFileTypeBase buildJAXBXmlBatchInputFileTypeBase() {
        JAXBXmlBatchInputFileTypeBase fileType = new JAXBXmlBatchInputFileTypeBase();
        fileType.setDirectoryPath(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        fileType.setFileExtension(XML);
        fileType.setFileTypeIdentifier(FILE_TYPE_IDENTIFIER);
        return fileType;
    }
    
    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(service.getPropertyValueAsString(CUPurapKeyConstants.JAGGAER_UPLOAD_WEBSERVICE_ERROR)).thenReturn(JAGGAER_WEBSERVICE_ERROR_MESSAGE);
        return service;
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
        deleteTemporaryFileDirectory();
        uploadSuppliersEndpoint = null;
    }

    private void deleteTemporaryFileDirectory() throws Exception {
        File uploadedSuppliersDirectory = new File(TEMP_SUPPLIER_UPLOAD_DIRECTORY);
        if (uploadedSuppliersDirectory.exists() && uploadedSuppliersDirectory.isDirectory()) {
            FileUtils.forceDelete(uploadedSuppliersDirectory.getAbsoluteFile());
        }
    }
    
    @ParameterizedTest
    @MethodSource("testUploadSupplierXMLFilesParameters")
    public void testUploadSupplierXMLFiles(boolean shouldUploadFiles, JaggaerMockServerCongiration configuration, String... searchStrings) {
        uploadSuppliersEndpoint.setJaggaerMockServerCongiration(configuration);
        jaggaerUploadFileServiceImpl.setParameterService(buildMockParameterService(shouldUploadFiles));
        jaggaerUploadFileServiceImpl.uploadSupplierXMLFiles();
        for (String searchString : searchStrings) {
            assertTrue(LogTestingUtils.doesLogEntryExist(appender.getLog(), searchString));
        }

    }
    
    static Stream<Arguments> testUploadSupplierXMLFilesParameters() {
        return Stream.of(
                Arguments.of(false, JaggaerMockServerCongiration.OK, 
                        new String[]{"uploadSupplierXMLFiles. uploading to Jaggaer is turned off, just remove the DONE file for test/jaggaer/xml/jaggaerTestFile.xml"}),
                Arguments.of(true, JaggaerMockServerCongiration.OK, new String[]{"<StatusCode>200</StatusCode>", "fileProcessedByJaggaer=true"}),
                Arguments.of(true, JaggaerMockServerCongiration.ACCEPTED, new String[]{"<StatusCode>202</StatusCode>", "fileProcessedByJaggaer=true"}),
                Arguments.of(true, JaggaerMockServerCongiration.SERVER_ERROR, new String[]{"<StatusCode>500</StatusCode>", "fileProcessedByJaggaer=false", 
                        "processUnsuccessfulResponse, attempt number 1, had an unsuccessful webservice call",
                        "processUnsuccessfulResponse, attempt number 2, had an unsuccessful webservice call"}),
                Arguments.of(true, JaggaerMockServerCongiration.BAD_REQUEST, new String[]{"<StatusCode>400</StatusCode>", "fileProcessedByJaggaer=false", 
                        "processUnsuccessfulResponse, attempt number 1, had an unsuccessful webservice call",
                        "processUnsuccessfulResponse, attempt number 2, had an unsuccessful webservice call"})
        );
    }

    private ParameterService buildMockParameterService(boolean shouldUploadFiles) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsBoolean(JaggaerUploadSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_ENABLE_UPLOAD_FILES)).thenReturn(shouldUploadFiles);
        Mockito.when(service.getParameterValueAsString(JaggaerUploadSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_RETRY_COUNT)).thenReturn("2");
        Mockito.when(service.getParameterValueAsString(JaggaerUploadSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_ENDPOINT))
                .thenReturn(baseServerUrl + uploadSuppliersEndpoint.getRelativeUrlPatternForHandlerRegistration());
        return service;
    }

}
