package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.ConfigureContext;

@ConfigureContext(session = ccs1)
public class ProcurementCardLoadFlatTransactionsServiceImplIntegTest extends CommonProcurementCardIntegTestBase {
    private static final Logger LOG = LogManager.getLogger();
    
    public void testGoodAndBadDataLoadFileProcessing() {
        //Good Data Test
        getIntegTestSqlDao().sqlCommand(PCARD_TRANSACTION_TABLE_SQL_DELETE_COMMAND);
        assertTrue(getProcurementCardLoadFlatTransactionsService().loadProcurementCardFile(getStagingBatchDirectory() + GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION));
        List summaryResults =  getIntegTestSqlDao().sqlSelect(PCARD_TRANSACTION_TABLE_SQL_SELECT_COMMAND);
        assertEquals(1, summaryResults.size());
        LOG.info("testGoodAndBadDataLoadFileProcessing, Good PCARD file was successfully loaded.");
        
        //Bad Data Test
        try {
            getIntegTestSqlDao().sqlCommand(PCARD_TRANSACTION_TABLE_SQL_DELETE_COMMAND);
            assertFalse(getProcurementCardLoadFlatTransactionsService().loadProcurementCardFile(getStagingBatchDirectory() + BAD_DATA_FILE_NAME_WITH_DATA_EXTENSION));
        } catch (RuntimeException re) {
            LOG.info("testGoodAndBadDataLoadFileProcessing, Exception caught for PCARD bad data file: " + re.getMessage());
        }
    }
}
