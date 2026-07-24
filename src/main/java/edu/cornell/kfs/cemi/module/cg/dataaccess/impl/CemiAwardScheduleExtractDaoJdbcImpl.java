package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardScheduleExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardScheduleExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearExistingListOfExtractableProposalNumbers() {
        LOG.info("clearExistingListOfExtractableProposalNumbers was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractableProposalNumbers finished truncating table.");
    }

    @Override
    public void queryAndStoreAwardProposalNumbersForAwardScheduleExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T (CGPRPSL_NBR) ")
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM KFS.CG_CEMI_AWD_SCHDL_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAwardProposalNumbersForAwardScheduleExtract, Found {} awards to extract", numRowsInserted);
    }

}
