package edu.cornell.kfs.module.ld.batch.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.batch.service.impl.ExceptionCaughtStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconBadLoadAbortedStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconOkLoadOkStatus;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.batch.service.EnterpriseFeederService;
import org.kuali.kfs.module.ld.batch.service.FileEnterpriseFeederHelperService;
import org.kuali.kfs.module.ld.report.EnterpriseFeederReportData;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.ReportWriterTextServiceImpl;

@ConfigureContext
public class CuFileEnterpriseFeederHelperServiceImplIntegTest  extends KualiIntegTestBase {
	private static final Logger LOG = LogManager.getLogger();
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/ld/fixture/SMGROS.data";
    private static final String RECON_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/ld/fixture/SMGROS.recon";
    
    private FileEnterpriseFeederHelperService fileEnterpriseFeederHelperService;
    
    private EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors;
    private PrintStream enterpriseFeedPs;
    
    private File dataFileDest;
    private File reconFileDest;
    private File doneFile;
    
    private LedgerSummaryReport ledgerSummaryReport;
    private EnterpriseFeederReportData feederReportData;
    private ReportWriterService errorStatisticsReport;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fileEnterpriseFeederHelperService = SpringContext.getBean(FileEnterpriseFeederHelperService.class);
        
        //make sure we have a batch directory
        String batchDirectory = SpringContext.getBean(EnterpriseFeederService.class).getDirectoryName();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        dataFileDest = new File(batchDirectory + "/SMGROS.data");
        FileUtils.copyFile(dataFileSrc, dataFileDest);
        
        //copy the recon file into place
        File rerconileSrc = new File(RECON_FILE_PATH);
        reconFileDest = new File(batchDirectory + "/SMGROS.recon");
        FileUtils.copyFile(rerconileSrc, reconFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/SMGROS.done";
        doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
        
        statusAndErrors = new EnterpriseFeederStatusAndErrorMessagesWrapper();
        statusAndErrors.setErrorMessages(new ArrayList<Message>());
        statusAndErrors.setFileNames(dataFileDest, reconFileDest, doneFile);
        
        File enterpriseFeedFile = null;
        String enterpriseFeedFileName = LaborConstants.BatchFileSystem.LABOR_ENTERPRISE_FEED + LaborConstants.BatchFileSystem.EXTENSION;
        enterpriseFeedFile = new File(batchDirectory + File.separator + enterpriseFeedFileName);

        enterpriseFeedPs = null;
        try {
            enterpriseFeedPs = new PrintStream(enterpriseFeedFile);
        }
        catch (FileNotFoundException e) {
            LOG.error("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
            throw new RuntimeException("enterpriseFeedFile doesn't exist " + enterpriseFeedFileName);
        }
        
        ledgerSummaryReport = new LedgerSummaryReport();
        feederReportData = new EnterpriseFeederReportData();
        errorStatisticsReport = new ReportWriterTextServiceImpl();
    }
        
    public void testFeedOnFile() {

        fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFileDest, reconFileDest, enterpriseFeedPs, 
                                                        "SomeName", "ld_ldgr_entr_t", statusAndErrors, 
                                                        ledgerSummaryReport, errorStatisticsReport, feederReportData);
        
        assertEquals(new FileReconOkLoadOkStatus().getStatusDescription(), statusAndErrors.getStatus().getStatusDescription());
    }
    
    public void testFeedOnFileBadData() {
        dataFileDest.delete();
        
        try {
            fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFileDest, reconFileDest, enterpriseFeedPs, 
                    "SomeName", "ld_ldgr_entr_t", statusAndErrors, 
                    ledgerSummaryReport, errorStatisticsReport, feederReportData);
            fail("should throw exception");
        } catch (RuntimeException e) {
            assertEquals(new ExceptionCaughtStatus().getStatusDescription(), statusAndErrors.getStatus().getStatusDescription());
        }
    }
    
    public void testFeedOnFileBadRecon() {
        reconFileDest.delete();
        
        try {
            fileEnterpriseFeederHelperService.feedOnFile(doneFile, dataFileDest, reconFileDest, enterpriseFeedPs, 
                    "SomeName", "ld_ldgr_entr_t", statusAndErrors, 
                    ledgerSummaryReport, errorStatisticsReport, feederReportData);
            fail("should throw exception");
        } catch (RuntimeException e) {
            assertEquals(new FileReconBadLoadAbortedStatus().getStatusDescription(), statusAndErrors.getStatus().getStatusDescription());
        }
    }
    


}
