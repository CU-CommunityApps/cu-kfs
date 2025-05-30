package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.SpringContext;

/**
 * CBCP utilizes PCard routines for its processing under the covers. 
 * Calling/using PCard routines in this integration test with CBCP items is a valid setup for this test.
 */

@ConfigureContext(session = ccs1)
public class CorporateBilledCorporatePaidLoadFlatTransactionsServiceImplIntegTest extends CommonPcardCbcpLoadFlatFileTransactionsIntegTestBase {
    private static final Logger LOG = LogManager.getLogger(); 

    private static String CBCP_TRANSACTION_TABLE_SQL_DELETE_COMMAND = "DELETE FROM CU_FP_CBCP_TRN_MT";
    private static String CBCP_TRANSACTION_TABLE_SQL_SELECT_COMMAND = "SELECT * FROM CU_FP_CBCP_TRN_MT";
    
    private static String CBCP_STAGING_FOLDER = "/fp/cbcp/";
    
    private CorporateBilledCorporatePaidLoadFlatFileServiceImpl procurementCardLoadFlatTransactionsService;

    @Override
    protected void setUp() throws Exception {
        setDataFileSubDirectory(CBCP_STAGING_FOLDER);
        super.setUp();
        procurementCardLoadFlatTransactionsService = SpringContext.getBean(CorporateBilledCorporatePaidLoadFlatFileServiceImpl.class);
    }
    
    public void testGoodAndBadDataLoadFileProcessing() {
        //Good Data Test
        getIntegTestSqlDao().sqlCommand(CBCP_TRANSACTION_TABLE_SQL_DELETE_COMMAND);
        assertTrue(procurementCardLoadFlatTransactionsService.loadProcurementCardFile(getStagingBatchDirectory() + GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION));
        List summaryResults = getIntegTestSqlDao().sqlSelect(CBCP_TRANSACTION_TABLE_SQL_SELECT_COMMAND);
        assertEquals(1, summaryResults.size());
        LOG.info("testGoodAndBadDataLoadFileProcessing, Good CBCP file was successfully loaded.");
        
        //Bad Data Test
        try {
            getIntegTestSqlDao().sqlCommand(CBCP_TRANSACTION_TABLE_SQL_DELETE_COMMAND);
            assertFalse(procurementCardLoadFlatTransactionsService.loadProcurementCardFile(getStagingBatchDirectory() + BAD_DATA_FILE_NAME_WITH_DATA_EXTENSION));
        } catch (RuntimeException re) {
            LOG.info("testGoodAndBadDataLoadFileProcessing, Exception caught for CBCP bad data file: " + re.getMessage());
        }
    }

}
