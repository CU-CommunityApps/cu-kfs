package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.sql.Types;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleDao;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardScheduleDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardScheduleDao {

    private static final Logger LOG = LogManager.getLogger();
    
    protected DateTimeService dateTimeService;

    @Override
    public void clearExistingListOfExtractableProposalNumbers() {
        LOG.info("clearExistingListOfExtractableProposalNumbers was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_SCHDL_EXTR_AWD_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractableProposalNumbers finished truncating table.");
    }

    @Override
    public void queryAndStoreAwardProposalNumbersForAwardScheduleExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_SCHDL_EXTR_AWD_T (CGPRPSL_NBR) ")
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CG_CEMI_AWD_SCHDL_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAwardProposalNumbersForAwardScheduleExtract, Found {} awards to extract", numRowsInserted);
    }
    
    @Override
    public void storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(final String spreadsheetKey,
            final String awardProposalNumber, final LocalDateTime jobRunDate) {

        String jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);

        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_MAPPING_AWD_SCHDL_EXTR_FILE_T ")
                .append("(WKDY_SPRDSHT_KEY_ID, CGPRPSL_NBR, EXTR_FILE_RUNDATE) ")
                .append("VALUES (").appendAsParameter(Types.VARCHAR, spreadsheetKey)
                .append(", ").appendAsParameter(Types.VARCHAR, awardProposalNumber)
                .append(", ").appendAsParameter(Types.VARCHAR, jobRunDateAsString)
                .append(")")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        if (numRowsInserted != 1) {
            LOG.error("storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping, Query should have inserted 1 row,"
                    + " but it inserted {} rows instead", numRowsInserted);
            throw new RuntimeException(String.format("Failed to insert SpreadsheeyKey-ProposaNumber-JobRunDate row for:"
                    + " Spreadsheet Key %s, Proposal Number %s, extraction job run datetime %s.", spreadsheetKey,
                    awardProposalNumber, jobRunDateAsString));
        }
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
