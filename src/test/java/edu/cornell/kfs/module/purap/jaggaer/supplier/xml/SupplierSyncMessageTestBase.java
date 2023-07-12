package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public abstract class SupplierSyncMessageTestBase {
    protected static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/jaggaer/xml/";
    
    protected File outputFileDirectory;

    protected CUMarshalService marshalService;
    
    protected ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_VERSION_NUMBER_TAG)).thenReturn(CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_VERSION_TAG);
        Mockito.when(service.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class,
                CUPurapParameterConstants.JAGGAER_UPLOAD_SUPPLIERS_DTD_DOCTYPE_TAG)).thenReturn(CuPurapTestConstants.JAGGAER_UPLOAD_SUPPLIERS_TEST_DTD_TAG);
        return service;
    }
    
    protected void setUpBeforeClass() throws Exception {
        Configurator.setLevel(CUMarshalServiceImpl.class, Level.DEBUG);
        marshalService = new CUMarshalServiceImpl();
        outputFileDirectory = new File(buildOutputFilePath());
        outputFileDirectory.mkdir();
    }
    
    protected abstract String buildOutputFilePath();
    
    protected void tearDownAfterClass() throws Exception {
        marshalService = null;
        FileUtils.deleteDirectory(outputFileDirectory);
    }
}
