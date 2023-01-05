package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

@ConfigureContext(session = kfs)
public class ProcurementCardCreateDocumentServiceImplIntegTest extends KualiIntegTestBase {

    private ProcurementCardLoadFlatTransactionsServiceImpl procurementCardLoadFlatTransactionsService;
    private ProcurementCardCreateDocumentService procurementCardCreateDocumentService;
    private ConfigurationService  kualiConfigurationService;
    
    private static final Logger LOG = LogManager.getLogger();
    
    private UnitTestSqlDao unitTestSqlDao;
	
    private static String delTable1 = "DELETE FROM FP_PRCRMNT_CARD_TRN_MT";
    private static String alter1 = "alter table FP_PRCRMNT_TRN_DTL_T disable constraint FP_PRCRMNT_TRN_DTL_TR1";
    private static String alter2 = "alter table FP_PRCRMNT_TRN_DTL_T disable constraint FP_PRCRMNT_TRN_DTL_TR2";
    private static String alter3 = "alter table FP_PRCRMNT_ACCT_LINES_T disable constraint FP_PRCRMNT_ACCT_LINES_TR8";
    private static String delTable2 =  "delete from FP_PRCRMNT_TRN_DTL_T";
    private static String transAmt = "SELECT * FROM FP_PRCRMNT_TRN_DTL_T";

    
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/service/fixture/fp_pcdo_usbank_2014267.data";
    private String batchDirectory;  

    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardLoadFlatTransactionsService = SpringContext.getBean(ProcurementCardLoadFlatTransactionsServiceImpl.class);
        procurementCardCreateDocumentService = SpringContext.getBean(ProcurementCardCreateDocumentService.class);
        
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/procurementCard";
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/fp_pcdo_usbank_2014267.data");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/fp_pcdo_usbank_2014267.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
                       
        
    }
    
    public void testCreateDocs() {        
       
    	unitTestSqlDao.sqlCommand(delTable1);
    	unitTestSqlDao.sqlCommand(alter1);
    	unitTestSqlDao.sqlCommand(alter2);
    	unitTestSqlDao.sqlCommand(alter3);
    	unitTestSqlDao.sqlCommand(delTable2);
    	assertTrue(procurementCardLoadFlatTransactionsService.loadProcurementCardFile(batchDirectory + "/fp_pcdo_usbank_2014267.data"));                                                       
    	assertTrue(procurementCardCreateDocumentService.createProcurementCardDocuments());
    	List summaryResults =  unitTestSqlDao.sqlSelect(transAmt);
    	
    	assertEquals(1, summaryResults.size());
    }

	
    
}