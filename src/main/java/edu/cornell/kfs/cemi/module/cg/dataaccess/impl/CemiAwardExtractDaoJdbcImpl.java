package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
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
        
        final CuSqlQuery accountSubAccountClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_AWD_ACCT_SUBACCT_T");
        executeUpdate(accountSubAccountClearingQuery);
        
        final CuSqlQuery fiscalYearClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_UNIV_FISCAL_YR_T");
        executeUpdate(fiscalYearClearingQuery);
        
        final CuSqlQuery accountSubAccountDirectCostClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_DIR_CST_GL_ENTRY_T");
        executeUpdate(accountSubAccountDirectCostClearingQuery);
        
        final CuSqlQuery accountSubAccountIndirectCostClearingQuery = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_AWD_EXTR_INDIR_CST_GL_ENTRY_T");
        executeUpdate(accountSubAccountIndirectCostClearingQuery);
        
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating tables containing key data from previous runs.");
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
    public void storeFiscalYearDependentQuerySetting(String fiscalYearToUseForDataExtraction) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_UNIV_FISCAL_YR_T ")
                .append("VALUES (").appendAsParameter(Types.INTEGER, Integer.valueOf(fiscalYearToUseForDataExtraction))
                .append(")")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        if (numRowsInserted != 1) {
            LOG.error("storeFiscalYearDependentQuerySetting, Query should have inserted 1 row, "
                    + "but it inserted {} rows instead", numRowsInserted);
            throw new RuntimeException("Failed to insert fiscal year query setting that award extract requires.");
        }
    }
    
    
    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        obtainKeysForAllInScopeAwards();
        obtainAssociatedAccountSubAccountsForAllInScopeAwards();
        
        // These three methods obtain the data from the Gl entry table and sum it at the database level
        // to obtain the value representing the sponsor direct cost amount
        obtainGeneralLedgerEntriesForDirectCostAmount(CemiAwardConstants.DIRECT_COST_NO_SUB_ACCOUNT_GL_ENTRIES_VIEW);
        obtainGeneralLedgerEntriesForDirectCostAmount(CemiAwardConstants.DIRECT_COST_SUB_ACCOUNT_GL_ENTRIES_VIEW);
        obtainCalculatedAwardHeaderDirectCostAmounts();
        
        // These three methods obtain the data from the Gl entry table and sum it at the database level
        // to obtain the value representing the sponsor facilities and administration amount
        obtainGeneralLedgerEntriesForIndirectCostAmount(CemiAwardConstants.INDIRECT_COST_NO_SUB_ACCOUNT_GL_ENTRIES_VIEW);
        obtainGeneralLedgerEntriesForIndirectCostAmount(CemiAwardConstants.INDIRECT_COST_SUB_ACCOUNT_GL_ENTRIES_VIEW);
        obtainCalculatedAwardHeaderIndirectCostAmounts();
//        obtainGeneralLedgerEntriesForAuthorizedAmount();
    }
    
    private void obtainKeysForAllInScopeAwards() {
        LOG.info("obtainKeysForAllInScopeAwards was called.");
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_AWD_T (CGPRPSL_NBR) ")
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CG_CEMI_AWD_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("obtainKeysForAllInScopeAwards, Found {} awards to extract", numRowsInserted);
    }
    
    
    private void obtainAssociatedAccountSubAccountsForAllInScopeAwards() {
        LOG.info("obtainAssociatedAccountSubAccountsForAllInScopeAwards was called.");
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_AWD_ACCT_SUBACCT_T ")
                .append("(OBJ_ID, CGPRPSL_NBR, FIN_COA_CD, ACCOUNT_NBR, PERSON_UNVL_ID, AWD_ACCT_ROW_ACTV_IND, ")
                .append("FNL_BILLED_IND, CURR_LST_BILLED_DT, PREV_LST_BILLED_DT, ACCT_SUB_FUND_GRP_CD, ")
                .append("ACCT_CG_ACCT_RESP_ID, ACCT_ACCT_TYP_CD, ACCT_CG_CFDA_NBR, ACCT_ACCT_CLOSED_IND, ")
                .append("PO_NBR, SUB_ACCT_NBR, SUB_ACCT_NM, SUB_ACCT_ACTV_CD) ")
                .append("SELECT SYS_GUID(), CGPRPSL_NBR, FIN_COA_CD, ACCOUNT_NBR, PERSON_UNVL_ID, AWD_ACCT_ROW_ACTV_IND, ")
                .append("FNL_BILLED_IND, CURR_LST_BILLED_DT, PREV_LST_BILLED_DT, ACCT_SUB_FUND_GRP_CD, ")
                .append("ACCT_CG_ACCT_RESP_ID, ACCT_ACCT_TYP_CD, ACCT_CG_CFDA_NBR, ACCT_ACCT_CLOSED_IND, ")
                .append("PO_NBR, SUB_ACCT_NBR, SUB_ACCT_NM, SUB_ACCT_ACTV_CD ")
                .append("FROM CEMI.CG_CEMI_AWD_ACCT_SUBACCT_V ")
                .append("ORDER BY CGPRPSL_NBR, FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR ASC")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("obtainAssociatedAccountSubAccountsForAllInScopeAwards, Found {} accounts with associated sub-accounts to configure for export", numRowsInserted);
    }
    
    
    private void obtainGeneralLedgerEntriesForDirectCostAmount(String directCostViewToQuery) {
        LOG.info("obtainGeneralLedgerEntriesForDirectCostAmount was called for {}", directCostViewToQuery);
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_DIR_CST_GL_ENTRY_T ")
                .append("(OBJ_ID, CGPRPSL_NBR, UNIV_FISCAL_YR, FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, FIN_OBJECT_CD, ")
                .append("FIN_SUB_OBJ_CD, FIN_BALANCE_TYP_CD, FIN_OBJ_TYP_CD, TRN_LDGR_ENTR_AMT) ")
                .append("SELECT SYS_GUID(), CGPRPSL_NBR, UNIV_FISCAL_YR, FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, ")
                .append("FIN_OBJECT_CD, FIN_SUB_OBJ_CD, FIN_BALANCE_TYP_CD, FIN_OBJ_TYP_CD, TRN_LDGR_ENTR_AMT ")
                .append("FROM ").append(directCostViewToQuery).append(" ")
                .append("ORDER BY FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR ASC")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("obtainGeneralLedgerEntriesForDirectCostAmount, Found {} {} rows.", numRowsInserted, directCostViewToQuery);
    }
    
    
    private void obtainCalculatedAwardHeaderDirectCostAmounts() {
        LOG.info("obtainCalculatedAwardHeaderDirectCostAmounts was called.");
        // Obtain list of awards to calculate award header sponsor direct cost
        final CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T ")
                .append("ORDER BY CGPRPSL_NBR")
                .toQuery();
        List<String> cgprpslNbrList = queryForValues(sqlQuery, SingleColumnRowMapper.newInstance(String.class));
        int rowsShouldBeUpdated = 0;
        int rowsActuallyUpdated = 0;
        if (CollectionUtils.isNotEmpty(cgprpslNbrList)) {
            // For every proposal number in the list, calculate the award header sponsor direct cost amount
            rowsShouldBeUpdated = cgprpslNbrList.size(); 
            for (String cgprpslNbr : cgprpslNbrList) {
                final CuSqlQuery updateQuery = new CuSqlChunk()
                        .append("UPDATE CEMI.CU_CEMI_AWD_EXTR_AWD_T AWD ")
                        .append("SET AWD.SPNSR_DIR_CST_AMT = (SELECT V.DIR_CST_AMT FROM CEMI.CU_CEMI_AWD_EXTR_DIR_CST_V V WHERE V.GLE_CGPRPSL_NBR = ").appendAsParameter(cgprpslNbr).append(")")
                        .append("WHERE AWD.CGPRPSL_NBR = ").appendAsParameter(cgprpslNbr)
                        .toQuery();
                final int numRowsUpdated = executeUpdate(updateQuery);
                if (numRowsUpdated == 0) {
                    LOG.warn("obtainCalculatedAwardHeaderDirectCostAmounts, No Direct Cost data rows updated for CG Proposal Number {}", cgprpslNbr);
                } else {
                    rowsActuallyUpdated++;
                }
            }
        } else {
            LOG.warn("obtainCalculatedAwardHeaderDirectCostAmounts, No data rows returned for table "
                    + "CEMI.CU_CEMI_AWD_EXTR_AWD_T, this is NOT EXPECTED processing.");
        }
        LOG.info("obtainCalculatedAwardHeaderDirectCostAmounts, {} rows should have been updated. "
                + "{} rows were updated.", rowsShouldBeUpdated, rowsActuallyUpdated);
    }
    
    
    private void obtainGeneralLedgerEntriesForIndirectCostAmount(String indirectCostViewToQuery) {
        LOG.info("obtainGeneralLedgerEntriesForIndirectCostAmount was called for {}", indirectCostViewToQuery);
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_AWD_EXTR_INDIR_CST_GL_ENTRY_T ")
                .append("(OBJ_ID, CGPRPSL_NBR, UNIV_FISCAL_YR, FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, FIN_OBJECT_CD, ")
                .append("FIN_SUB_OBJ_CD, FIN_BALANCE_TYP_CD, FIN_OBJ_TYP_CD, TRN_LDGR_ENTR_AMT) ")
                .append("SELECT SYS_GUID(), CGPRPSL_NBR, UNIV_FISCAL_YR, FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR, ")
                .append("FIN_OBJECT_CD, FIN_SUB_OBJ_CD, FIN_BALANCE_TYP_CD, FIN_OBJ_TYP_CD, TRN_LDGR_ENTR_AMT ")
                .append("FROM ").append(indirectCostViewToQuery).append(" ")
                .append("ORDER BY FIN_COA_CD, ACCOUNT_NBR, SUB_ACCT_NBR ASC")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("obtainGeneralLedgerEntriesForIndirectCostAmount, Found {} {} rows.", numRowsInserted, indirectCostViewToQuery);
    }
    
    
    private void obtainCalculatedAwardHeaderIndirectCostAmounts() {
        LOG.info("obtainCalculatedAwardHeaderIndirectCostAmounts was called.");
        // Obtain list of awards to calculate award header sponsor facilities and administration amount
        final CuSqlQuery sqlQuery = new CuSqlChunk()
                .append("SELECT CGPRPSL_NBR ")
                .append("FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T ")
                .append("ORDER BY CGPRPSL_NBR")
                .toQuery();
        List<String> cgprpslNbrList = queryForValues(sqlQuery, SingleColumnRowMapper.newInstance(String.class));
        int rowsShouldBeUpdated = 0;
        int rowsActuallyUpdated = 0;
        if (CollectionUtils.isNotEmpty(cgprpslNbrList)) {
            // For every proposal number in the list, calculate the award header sponsor direct cost amount
            rowsShouldBeUpdated = cgprpslNbrList.size(); 
            for (String cgprpslNbr : cgprpslNbrList) {
                final CuSqlQuery updateQuery = new CuSqlChunk()
                        .append("UPDATE CEMI.CU_CEMI_AWD_EXTR_AWD_T AWD ")
                        .append("SET AWD.SPNSR_INDIR_CST_AMT = (SELECT V.INDIR_CST_AMT FROM CEMI.CU_CEMI_AWD_EXTR_INDIR_CST_V V WHERE V.GLE_CGPRPSL_NBR = ").appendAsParameter(cgprpslNbr).append(")")
                        .append("WHERE AWD.CGPRPSL_NBR = ").appendAsParameter(cgprpslNbr)
                        .toQuery();
                final int numRowsUpdated = executeUpdate(updateQuery);
                if (numRowsUpdated == 0) {
                    LOG.warn("obtainCalculatedAwardHeaderIndirectCostAmounts, No Indirect Cost data rows updated for CG Proposal Number {}", cgprpslNbr);
                } else {
                    rowsActuallyUpdated++;
                }
            }
        } else {
            LOG.warn("obtainCalculatedAwardHeaderIndirectCostAmounts, No data rows returned for table "
                    + "CEMI.CU_CEMI_AWD_EXTR_AWD_T, this is NOT EXPECTED processing.");
        }
        LOG.info("obtainCalculatedAwardHeaderIndirectCostAmounts, {} rows should have been updated. "
                + "{} rows were updated.", rowsShouldBeUpdated, rowsActuallyUpdated);
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
