package edu.cornell.kfs.fp.batch.service.impl;

import org.kuali.kfs.sys.context.SpringContext;

public abstract class CommonProcurementCardIntegTestBase extends CommonPcardCbcpLoadFlatFileTransactionsIntegTestBase {
    
    protected static String PCARD_TRANSACTION_TABLE_SQL_SELECT_COMMAND = "SELECT * FROM FP_PRCRMNT_CARD_TRN_MT";
    protected static String PCARD_TRANSACTION_TABLE_SQL_DELETE_COMMAND = "DELETE FROM FP_PRCRMNT_CARD_TRN_MT";
    protected static String PCARD_TRANSACTION_DETAIL_SQL_DISABLE_VENDOR_FOREIGN_KEY_COMMAND = "ALTER TABLE FP_PRCRMNT_TRN_DTL_T DISABLE CONSTRAINT FP_PRCRMNT_TRN_DTL_TR1";
    protected static String PCARD_TRANSACTION_DETAIL_SQL_DISABLE_PCARD_DOC_FOREIGN_KEY_COMMAND = "ALTER TABLE FP_PRCRMNT_TRN_DTL_T DISABLE CONSTRAINT FP_PRCRMNT_TRN_DTL_TR2";
    protected static String PCARD_ACCT_LINES_SQL_DISABLE_PCARD_TRANSACTION_DETAIL_FOREIGN_KEY_COMMAND = "ALTER TABLE FP_PRCRMNT_ACCT_LINES_T DISABLE CONSTRAINT FP_PRCRMNT_ACCT_LINES_TR8";
    protected static String PCARD_TRANSACTION_DETAIL_TABLE_SQL_DELETE_COMMAND  =  "DELETE FROM FP_PRCRMNT_TRN_DTL_T";
    protected static String PCARD_TRANSACTION_DETAIL_TABLE_SQL_SELECT_COMMAND = "SELECT * FROM FP_PRCRMNT_TRN_DTL_T";

    private static String PCARD_STAGING_FOLDER = "/fp/procurementCard/";
    
    private ProcurementCardLoadFlatTransactionsServiceImpl procurementCardLoadFlatTransactionsService;
    
    @Override
    protected void setUp() throws Exception {
        setDataFileSubDirectory(PCARD_STAGING_FOLDER);
        super.setUp();
        procurementCardLoadFlatTransactionsService = SpringContext.getBean(ProcurementCardLoadFlatTransactionsServiceImpl.class);
    }

    public ProcurementCardLoadFlatTransactionsServiceImpl getProcurementCardLoadFlatTransactionsService() {
        return procurementCardLoadFlatTransactionsService;
    }

    public void setProcurementCardLoadFlatTransactionsService(
            ProcurementCardLoadFlatTransactionsServiceImpl procurementCardLoadFlatTransactionsService) {
        this.procurementCardLoadFlatTransactionsService = procurementCardLoadFlatTransactionsService;
    }
    
}
