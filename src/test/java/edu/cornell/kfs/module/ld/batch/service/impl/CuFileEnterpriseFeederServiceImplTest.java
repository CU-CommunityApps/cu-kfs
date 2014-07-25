package edu.cornell.kfs.module.ld.batch.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kuali.kfs.gl.batch.service.impl.ExceptionCaughtStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconBadLoadAbortedStatus;
import org.kuali.kfs.module.ld.batch.service.EnterpriseFeederService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.ReportWriterTextServiceImpl;

@ConfigureContext
public class CuFileEnterpriseFeederServiceImplTest extends KualiTestBase  {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuFileEnterpriseFeederServiceImplTest.class);
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/module/ld/fixture/SMGROS.data";
    private static final String RECON_FILE_PATH = "src/test/java/edu/cornell/kfs/module/ld/fixture/SMGROS.recon";
    private static final String REPORTS_FOLDER = "/opt/work/reports/ld";
    
    private EnterpriseFeederService enterpriseFeederService;
      
    private File dataFileDest;
    private File reconFileDest;
    private File doneFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enterpriseFeederService = SpringContext.getBean(EnterpriseFeederService.class);
        
        ReportWriterTextServiceImpl laborEnterpriseFeedReportWriterService  = SpringContext.getBean(ReportWriterTextServiceImpl.class, "laborEnterpriseFeedReportWriterService");
        laborEnterpriseFeedReportWriterService.setAggregationModeOn(true);
        laborEnterpriseFeedReportWriterService.initialize();
        
        ReportWriterTextServiceImpl laborEnterpriseFeedErrorStatisticsWriterService  = SpringContext.getBean(ReportWriterTextServiceImpl.class, "laborEnterpriseFeedErrorStatisticsWriterService");
        laborEnterpriseFeedErrorStatisticsWriterService.setAggregationModeOn(true);
        laborEnterpriseFeedErrorStatisticsWriterService.initialize();
        
        //make sure we have a batch directory
        String batchDirectory = enterpriseFeederService.getDirectoryName();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();
        
        //make sure we have a batch directory for labor origin entry
        String laborOriginEntryDirectory = enterpriseFeederService.getDirectoryName().replace("enterpriseFeed", "originEntry");
        File laborOriginEntrFile = new File(laborOriginEntryDirectory);
        laborOriginEntrFile.mkdir();

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
        
    }
    
   public void testFeed() throws IOException {
        enterpriseFeederService.feed("SomeName", false);
        
        String content = TestUtils.readFileUTF8(REPORTS_FOLDER + "/labor_enterprise_feed_.txt");
        assertTrue(content.contains("19,480.65"));
        assertTrue(content.contains("All files were successfully loaded"));
    }
    
    public void testFeedBadData() throws IOException {
        dataFileDest.delete();
        enterpriseFeederService.feed("SomeName", false);
        
        String content = TestUtils.readFileUTF8(REPORTS_FOLDER + "/labor_enterprise_feed_.txt");
        assertTrue(content.contains("SMGROS.recon"));
        assertTrue(content.contains("No files were successfully loaded"));
    }
    
   public void testFeedBadRecon() throws IOException {
        reconFileDest.delete();
        enterpriseFeederService.feed("SomeName", false);
        
        String content = TestUtils.readFileUTF8(REPORTS_FOLDER + "/labor_enterprise_feed_.txt");

        assertTrue(content.contains("SMGROS.data"));
        assertTrue(content.contains("No files were successfully loaded"));
    }

}
