package edu.cornell.kfs.cemi.module.cam.dataaccess.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiRegisterAssetExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiRegisterAssetExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_EXTR_REGISTER_ASST_TAB_REGISTER_ASST_T");
        executeUpdate(query);
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating table.");
    }

    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {

        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_REGISTER_ASST_EXTR_ASST_T (CPTLAST_NBR) ")
                .append("SELECT CPTLAST_NBR ")
                .append("FROM CEMI.CG_CEMI_REGISTER_ASSET_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreInScopeBusinessObjectKeysForDataExtract, Found {} in scope business object to extract", numRowsInserted);
    }
 
//    EXAMPLE: Actual example that would work for a data extraction
//    @Override
//    public void storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(final String spreadsheetKey,
//            final String awardProposalNumber, final String jobRunDateString) {
//
//        final CuSqlQuery query = new CuSqlChunk()
//                .append("INSERT INTO KFS.CU_CEMI_MAPPING_AWD_SCHDL_EXTR_FILE_T ")
//                .append("(WKDY_SPRDSHT_KEY_ID, CGPRPSL_NBR, EXTR_FILE_RUNDATE) ")
//                .append("VALUES (").appendAsParameter(Types.VARCHAR, spreadsheetKey)
//                .append(", ").appendAsParameter(Types.VARCHAR, awardProposalNumber)
//                .append(", ").appendAsParameter(Types.VARCHAR, jobRunDateString)
//                .append(")")
//                .toQuery();
//
//        final int numRowsInserted = executeUpdate(query);
//        if (numRowsInserted != 1) {
//            LOG.error("storeSpreadsheetKeyProposalNumberEXTRACTNAMEExtractRunDateMapping, Query should have inserted 1 row,"
//                    + " but it inserted {} rows instead", numRowsInserted);
//            throw new RuntimeException(String.format("Failed to insert SpreadsheeyKey-ProposaNumber-JobRunDate row for:"
//                    + " Spreadsheet Key %s, Proposal Number %s, extraction job run datetime %s.", spreadsheetKey,
//                    awardProposalNumber, jobRunDateString));
//        }
//    }

}
