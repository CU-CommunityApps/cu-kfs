package edu.cornell.kfs.cemi.module.cam.dataaccess.impl;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.cam.CemiRegisterAssetConstants.RegisterAssetTranslateTables;
import edu.cornell.kfs.cemi.module.cam.batch.translatetable.KfsToWorkdayRegisterAssetCommonCsvTableColumns;
import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiRegisterAssetExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiRegisterAssetExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_REGISTER_ASST_EXTR_ASST_T");
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
    
    
    @Override
    public Map<String, String> buildTranslationForTable(RegisterAssetTranslateTables queryToExecute) {
        final CuSqlQuery query = new CuSqlChunk()
                .append(queryToExecute.queryString)
                .toQuery();

        return queryForResults(query, resultSet -> {
            final Stream.Builder<Pair<String, String>> mappingEntries = Stream.builder();
            while (resultSet.next()) {
                final String legacyLookupKey = resultSet.getString(
                        KfsToWorkdayRegisterAssetCommonCsvTableColumns.LEGACY_CODE.name());
                final String workdayReturnRefIdValue = resultSet.getString(
                        KfsToWorkdayRegisterAssetCommonCsvTableColumns.WORKDAY_REF_ID.name());
                mappingEntries.add(Pair.of(legacyLookupKey, workdayReturnRefIdValue));
            }
            return mappingEntries.build().collect(Collectors.toUnmodifiableMap(Pair::getLeft, Pair::getRight));
        });
    }

}
