package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardBudgetExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardBudgetExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardBudgetExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearExistingListOfExtractableProposalNumbers() {
        LOG.info("clearExistingListOfExtractableProposalNumbers was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_BUDG_EXTR_AWD_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractableProposalNumbers finished truncating table.");
    }

    @Override
    public void queryAndStoreAwardProposalNumbersForAwardBudgetExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_BUDG_EXTR_AWD_T (CGPRPSL_NBR) ")
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CG_CEMI_AWD_BUDG_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAwardProposalNumbersForAwardBudgetExtract, Found {} awards to extract", numRowsInserted);
    }

}
