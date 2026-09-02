package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.KfsToWorkdayAwardCommonCsvTableColumns;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
        final CuSqlQuery headerKeysQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_T");
        executeUpdate(headerKeysQuery);
        
        final CuSqlQuery orgCodeLookupQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_ORG_T");
        executeUpdate(orgCodeLookupQuery);
        
        //TODO: place truncation of award lines key table here
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating previous run key tables.");
    }
    
    
    @Override
    public void updateAwardScheduleExtractDependentQuerySettings(String awardScheduleJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE CEMI.CU_CEMI_AWD_EXTR_AWD_SCHD_QUERY_SETTINGS_T ")
                .append("SET AWD_SCHD_EXTR_FILE_RUNDATE = ").appendAsParameter(awardScheduleJobRunDate)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateAwardScheduleExtractDependentQuerySettings, Query should have updated 1 row, "
                    + "but it updated {} instead", numRowsUpdated);
            throw new RuntimeException("Failed to update award schedule extract dependent query settings");
        }
    }
    

    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        obtainKeysForAllInScopeAwards();
        //TODO: place call to private method obtainKeysForAllInScopeAwardAccounts
    }
    
    
    @Override
    public boolean awardScheduleContainsAwardExtractBuiltReferenceId(String awardExtractionBuiltAwardScheduleReferenceId) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT COUNT(AWD_SCHD.JOB_RUN_ROW_INDEX) ")
                .append("FROM CEMI.CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_T AWD_SCHD, ")
                .append("CEMI.CU_CEMI_AWD_EXTR_AWD_SCHD_QUERY_SETTINGS_T AWD_SCHD_PARM ")
                .append("WHERE AWD_SCHD.EXTR_FILE_RUNDATE = AWD_SCHD_PARM.AWD_SCHD_EXTR_FILE_RUNDATE ")
                .append("AND AWD_SCHD.WKDY_SPRDSHT_KEY_ID = ").appendAsParameter(awardExtractionBuiltAwardScheduleReferenceId)
                .toQuery();

        final int numRowsSelected = executeUpdate(query);
        if (numRowsSelected != 1) {
            LOG.error("awardScheduleContainsAwardExtractBuiltReferenceId, Query should have found 1 previously created "
                    + "Award Schedule extract keyed row {} but found {} instead", awardExtractionBuiltAwardScheduleReferenceId, numRowsSelected);
            return false;
        }
        return true;
    }
    
    
    @Override
    public Map<String, String> buildTranslationForTable(AwardTranslateTables queryToExecute) {

        final CuSqlQuery query = new CuSqlChunk()
                .append(queryToExecute.queryString)
                .toQuery();

        return queryForResults(query, resultSet -> {
            final Stream.Builder<Pair<String, String>> mappingEntries = Stream.builder();
            while (resultSet.next()) {
                final String legacyLookupKey = resultSet.getString(
                        KfsToWorkdayAwardCommonCsvTableColumns.LEGACY_CODE.name());
                final String workdayReturnRefIdValue = resultSet.getString(
                        KfsToWorkdayAwardCommonCsvTableColumns.WORKDAY_REF_ID.name());
                mappingEntries.add(Pair.of(legacyLookupKey, workdayReturnRefIdValue));
            }
            return mappingEntries.build().collect(Collectors.toUnmodifiableMap(Pair::getLeft, Pair::getRight));
        });
    }
    
    //may need to do this as ojb as part of larger data object instead
    @Override
    public String findOrganizationCodeForInScopeAward(String inScopeAwardForPrimaryOrganizationLookup) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT AWDORG2.PMRY_ORG_CD ")
                .append("FROM CEMI.CG_CEMI_AWD_AWDORG_PRIMARY_V AWDORG2 ")
                .append("WHERE AWDORG2.CGPRPSL_NUM = ").appendAsParameter(inScopeAwardForPrimaryOrganizationLookup)
                .toQuery();

        return queryForResults(query, this::getFirstColumnValueFromFirstRowIfPresent);
    }
    private String getFirstColumnValueFromFirstRowIfPresent(final ResultSet resultSet) throws SQLException {
        return resultSet.next() ? resultSet.getString(1) : null;
    }


    
    private void obtainKeysForAllInScopeAwards() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_AWD_T (CGPRPSL_NBR) ")
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CG_CEMI_AWD_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("obtainKeysForAllInScopeAwards, Found {} awards to extract", numRowsInserted);
    }
    

//SELECT COUNT(JOB_RUN_ROW_INDEX) FROM CEMI.CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_T WHERE EXTR_FILE_RUNDATE = '20260521_145448' AND WKDY_SPRDSHT_KEY_ID = 'AS_ITH_163600';

    
//    private void obtainInScopeBaseAwardHeaderFieldsForAllActiveAwards() {
//        final CuSqlQuery query = new CuSqlChunk()
//                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_AWD_HDR_T (CGPRPSL_NBR, CG_GRANT_NUMBER, CGAWD_PROJ_TTL, ")
//                .append("CGAWD_BEG_DT, CG_GRANT_DESC_CD, CGAWD_PURPOSE_CD, CGAWD_STAT_CD, CG_FEDPT_AGNCY_NBR, ")
//                .append("CG_AGENCY_NBR, CG_LTRCR_FNDGRP_CD) ")
//                .append("SELECT CGPRPSL_NBR, CG_GRANT_NUMBER, CGAWD_PROJ_TTL, CGAWD_BEG_DT, CG_GRANT_DESC_CD, ")
//                .append("CGAWD_PURPOSE_CD, CGAWD_STAT_CD, CG_FEDPT_AGNCY_NBR, CG_AGENCY_NBR, CG_LTRCR_FNDGRP_CD ")
//                .append("FROM CEMI.CU_CEMI_INTRM_AWD_HDR_AWD_BASE_FLDS_V")
//                .toQuery();
//
//        final int numRowsInserted = executeUpdate(query);
//        LOG.info("obtainInScopeBaseAwardHeaderFieldsForAllActiveAwards, Found {} in scope base business object to extract.", numRowsInserted);
//    }
//    
//    private void updateInScopeBaseAwardHeaderFieldsWithAwardOrgData() {
//        final CuSqlQuery query = new CuSqlChunk()
//                .append("UPDATE CEMI.CU_CEMI_AWD_EXTR_AWD_HDR_T INTERIM ")
//                .append("SET (AWD_ORG_FIN_COA_CD, AWD_ORG_ORG_CD, AWD_ORG_CGAWD_PRM_ORG_IND, AWD_ORG_ROW_ACTV_IND) = ")
//                .append("(SELECT AWD_ORG.FIN_COA_CD, AWD_ORG.ORG_CD, AWD_ORG.CGAWD_PRM_ORG_IND, AWD_ORG.ROW_ACTV_IND ")
//                .append("FROM KFS.CG_AWD_ORG_T AWD_ORG ")
//                .append("WHERE INTERIM.CGPRPSL_NBR = AWD_ORG.CGPRPSL_NBR ")
//                .append("AND AWD_ORG.CGAWD_PRM_ORG_IND = 'Y' ")
//                .append("AND AWD_ORG.ROW_ACTV_IND = 'Y') ")
//                .append("WHERE EXISTS (SELECT 1 FROM KFS.CG_AWD_ORG_T AWD_ORG2 ")
//                .append("INTERIM.CGPRPSL_NBR = AWD_ORG2.CGPRPSL_NBR)")
//                .toQuery();
//        
//        final int numRowsUpdated = executeUpdate(query);
//        LOG.info("updateInScopeBaseAwardHeaderFieldsWithAwardOrgData, Updated organization data on {} in scope base business object to extract.", numRowsUpdated);
//    }
    
}
