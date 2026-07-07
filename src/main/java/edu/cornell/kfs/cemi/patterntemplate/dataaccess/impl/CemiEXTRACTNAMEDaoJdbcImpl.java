package edu.cornell.kfs.cemi.patterntemplate.dataaccess.impl;

import java.sql.Types;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiEXTRACTNAMEDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiEXTRACTNAMEDao {

    private static final Logger LOG = LogManager.getLogger();
    
    protected DateTimeService dateTimeService;

    // This method should configure and execute a JDBC database table truncate command against the table
    // populated by the previous execution. The table name in that SQL call should match the OJB definition
    // as well as the nonprod-sql script used for the table's initial creation.
    // NOTE: Schema names need to be specified in all JDBC calls for tables.
    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution, was called.");
        // EXAMPLE of working truncate command to model. 
        //
        // final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T");
        executeUpdate(query);
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution, finished truncating table.");
    }

    // Under most circumstances, this method will utilize a single SQL statement to query a pre-built database 
    // view that obtains in scope business object keys which are immediately inserted into a prebuilt table 
    // to be utilized by down stream processing.
    // NOTE: Schema names need to be specified in all JDBC calls for both tables and views.
    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract(String inScopeBusinessObjectName) {
        //Example of working query to model.
        //
        //final CuSqlQuery query = new CuSqlChunk()
        //        .append("INSERT INTO KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T (CGPRPSL_NBR) ")
        //        .append("SELECT CGPRPSL_NBR ")
        //        .append("FROM KFS.CG_CEMI_AWD_SCHDL_EXTR_V")
        //        .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreInScopeBusinessObjectKeysForDataExtract, Found {} {} to extract.", numRowsInserted, inScopeBusinessObjectName);
    }
    
    @Override
    public void storeSpreadsheetKeyProposalNumberAwardScheduleExtractRunDateMapping(final String spreadsheetKey,
            final String awardProposalNumber, final LocalDateTime jobRunDate) {

        String jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);

        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_MAPPING_AWD_SCHDL_EXTR_FILE_T ")
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
