package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.JaggaerConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public abstract class SupplierSyncMessageTestBase {
    protected static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    protected File outputFileDirectory;
    protected CUMarshalService marshalService;
    
    @BeforeEach
    protected void setUpBeforeClass() throws Exception {
        Configurator.setLevel(CUMarshalServiceImpl.class, Level.DEBUG);
        marshalService = new CUMarshalServiceImpl();
        outputFileDirectory = new File(buildOutputFilePath());
        outputFileDirectory.mkdir();
    }
    
    protected abstract String buildOutputFilePath();
    
    @AfterEach
    protected void tearDownAfterClass() throws Exception {
        marshalService = null;
        FileUtils.deleteDirectory(outputFileDirectory);
    }
    
    protected void validateFileContainsExpectedHeader(File xmlFile) throws IOException {
        String expeectedHeaderString = CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_VERSION_TAG + KFSConstants.NEWLINE + CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_DTD_TAG;
        String actualXmlContents = FileUtils.readFileToString(xmlFile, Charset.defaultCharset());
        assertTrue(StringUtils.contains(actualXmlContents, expeectedHeaderString));
    }
    
    protected SupplierSyncMessage buildSupplierSyncMessageBase() {
        SupplierSyncMessage supplierSyncMessage = new SupplierSyncMessage();
        supplierSyncMessage.setParameterService(buildMockParameterService());
        supplierSyncMessage.setVersion(JaggaerConstants.SUPPLIER_SYNCH_MESSAGE_XML_VERSION);
        return supplierSyncMessage;
    }
    
    protected ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_VERSION_NUMBER_TAG)).thenReturn(CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_VERSION_TAG);
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG)).thenReturn(CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_DTD_TAG);
        return service;
    }
    
    protected ErrorMessage buildErrorMessage(String message) {
        ErrorMessage em = new ErrorMessage();
        em.setType("Error");
        em.setValue(message);
        return em;
    }
}
