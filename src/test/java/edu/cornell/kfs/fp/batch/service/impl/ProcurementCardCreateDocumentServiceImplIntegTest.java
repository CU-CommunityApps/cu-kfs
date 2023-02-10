package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext(session = kfs)
public class ProcurementCardCreateDocumentServiceImplIntegTest extends CommonProcurementCardIntegTestBase {
    private static final Logger LOG = LogManager.getLogger();
    
    private ProcurementCardCreateDocumentService procurementCardCreateDocumentService;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardCreateDocumentService = SpringContext.getBean(ProcurementCardCreateDocumentService.class);
    }
    
    public void testCreatePcardDocs() {
        getUnitTestSqlDao().sqlCommand(PCARD_TRANSACTION_TABLE_SQL_DELETE_COMMAND);
        getUnitTestSqlDao().sqlCommand(PCARD_TRANSACTION_DETAIL_SQL_DISABLE_VENDOR_FOREIGN_KEY_COMMAND);
        getUnitTestSqlDao().sqlCommand(PCARD_TRANSACTION_DETAIL_SQL_DISABLE_PCARD_DOC_FOREIGN_KEY_COMMAND);
        getUnitTestSqlDao().sqlCommand(PCARD_ACCT_LINES_SQL_DISABLE_PCARD_TRANSACTION_DETAIL_FOREIGN_KEY_COMMAND);
        getUnitTestSqlDao().sqlCommand(PCARD_TRANSACTION_DETAIL_TABLE_SQL_DELETE_COMMAND);
        assertTrue(getProcurementCardLoadFlatTransactionsService().loadProcurementCardFile(getStagingBatchDirectory() + GOOD_DATA_FILE_NAME_WITH_DATA_EXTENSION));
        assertTrue(procurementCardCreateDocumentService.createProcurementCardDocuments());
        List summaryResults =  getUnitTestSqlDao().sqlSelect(PCARD_TRANSACTION_DETAIL_TABLE_SQL_SELECT_COMMAND);
        assertEquals(1, summaryResults.size());
        LOG.info("testCreatePCardDocs, Good PCard file was successfully loaded with PCard documents successfully created.");
    }
    
}
