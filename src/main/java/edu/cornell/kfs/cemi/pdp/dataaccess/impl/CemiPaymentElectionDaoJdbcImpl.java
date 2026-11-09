package edu.cornell.kfs.cemi.pdp.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiPaymentElectionDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiPaymentElectionDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearExistingListOfExtractablePayeeAchAccountGeneratedIds() {
        LOG.info("clearExistingListOfExtractablePayeeAchAccountGeneratedIds was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractablePayeeAchAccountGeneratedIds finished truncating table.");
    }

    @Override
    public void queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T (ACH_ACCT_GNRTD_ID) ")
                .append("SELECT ACH_ACCT_GNRTD_ID ")
                .append("FROM CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract, Found {} payee ach accounts to extract", numRowsInserted);
    }

}
