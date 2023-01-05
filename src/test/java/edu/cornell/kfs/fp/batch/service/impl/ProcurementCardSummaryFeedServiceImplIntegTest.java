package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.fp.batch.service.ProcurementCardSummaryFeedService;


@ConfigureContext(session = ccs1)
public class ProcurementCardSummaryFeedServiceImplIntegTest extends KualiIntegTestBase {

    private ProcurementCardSummaryFeedService procurementCardSummaryFeedService;
    private ConfigurationService  kualiConfigurationService;
    
    private static final Logger LOG = LogManager.getLogger();
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/service/fixture/fp_pcard_summary_20140924.data";
    private String batchDirectory;  

    private UnitTestSqlDao unitTestSqlDao;
	
	private static String newTrans1 = "select * from CU_FP_PCARD_SUMMARY_T where CARD_ACCOUNT_NBR = '0001'";
	private static String newTrans2 = "select * from CU_FP_PCARD_SUMMARY_T where CARD_ACCOUNT_NBR = '9898'";
	private static String newTrans3 = "select * from CU_FP_PCARD_SUMMARY_T where CARD_ACCOUNT_NBR = '0002'";

	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardSummaryFeedService = SpringContext.getBean(ProcurementCardSummaryFeedService.class);
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/pcardSummary";
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/fp_pcard_summary_20140924.data");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/fp_pcard_summary_20140924.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
                       
        
    }
    
    public void testCanLoadFiles() {        
       assertTrue(procurementCardSummaryFeedService.loadPCardDataFromBatchFile(batchDirectory + "/fp_pcard_summary_20140924.data"));                                                       
       	List summaryResults1 =  unitTestSqlDao.sqlSelect(newTrans1);
		assertEquals(1, summaryResults1.size());
		List summaryResults2 =  unitTestSqlDao.sqlSelect(newTrans2);
		assertEquals(1, summaryResults2.size());
		List summaryResults3 =  unitTestSqlDao.sqlSelect(newTrans3);
		assertEquals(1, summaryResults3.size());
       
    }
    
}