package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.KfsToWorkdayAwardCommonCsvTableColumns;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiAwardExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiAwardExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearingAllExistingBusinessObjectKeysAndSetupDataFromPreviousExecution() {
        LOG.info("clearingAllExistingBusinessObjectKeysAndSetupDataFromPreviousExecution was called.");
        final CuSqlQuery dependentAwardScheduleQuerySettingsClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_SCHD_QUERY_SETTINGS_T");
        executeUpdate(dependentAwardScheduleQuerySettingsClearingQuery);
        
        final CuSqlQuery headerKeysClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_T");
        executeUpdate(headerKeysClearingQuery);
        
        final CuSqlQuery orgCodeLookupClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_ORG_T");
        executeUpdate(orgCodeLookupClearingQuery);
        
//TODO: place truncation of award lines key table here
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating previous run key tables.");
    }
    
    
    @Override
    public void storeAwardScheduleExtractDependentQuerySettings(String awardScheduleJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_AWD_SCHD_QUERY_SETTINGS_T ")
                .append("VALUES (").appendAsParameter(Types.VARCHAR, awardScheduleJobRunDate)
                .append(")")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        if (numRowsInserted != 1) {
            LOG.error("storeAwardScheduleExtractDependentQuerySettings, Query should have inserted 1 row, "
                    + "but it inserted {} rows instead", numRowsInserted);
            throw new RuntimeException("Failed to insert award schedule extract query settings that award schedule extract requires.");
        }
    }
    
    
    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        obtainKeysForAllInScopeAwards();
//TODO: place call to private method obtainKeysForAllInScopeAwardAccounts
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
    
    
    @Override
    public boolean awardScheduleContainsAwardExtractBuiltReferenceId(String awardExtractionBuiltAwardScheduleReferenceId) {
        final CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("SELECT COUNT(AWD_SCHD.JOB_RUN_ROW_INDEX) ")
                .append("FROM CEMI.CU_CEMI_EXTR_AWD_SCHD_TAB_AWD_SCHD_T AWD_SCHD, ")
                .append("CEMI.CU_CEMI_AWD_EXTR_AWD_SCHD_QUERY_SETTINGS_T AWD_SCHD_PARM ")
                .append("WHERE AWD_SCHD.EXTR_FILE_RUNDATE = AWD_SCHD_PARM.AWD_SCHD_EXTR_FILE_RUNDATE ")
                .append("AND AWD_SCHD.WKDY_SPRDSHT_KEY_ID = ").appendAsParameter(awardExtractionBuiltAwardScheduleReferenceId)
                .toQuery();

        List<Integer> results = queryForValues(sqlQuery, SingleColumnRowMapper.newInstance(Integer.class));
        int rowCount = CollectionUtils.isNotEmpty(results) ? (results.get(0)).intValue() : 0;
        
        if (rowCount != 1) {
            LOG.error("awardScheduleContainsAwardExtractBuiltReferenceId, Query should have found 1 previously created "
                    + "Award Schedule extract keyed row {} but found {} instead", awardExtractionBuiltAwardScheduleReferenceId, rowCount);
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
    

}
