package edu.cornell.kfs.cemi.pdp.dataaccess.impl;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiPaymentElectionDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiPaymentElectionDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T");
        executeUpdate(query);
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating table.");
    }

    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T (ACH_ACCT_GNRTD_ID) ")
                .append("SELECT ACH_ACCT_GNRTD_ID ")
                .append("FROM CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreInScopeBusinessObjectKeysForDataExtract, Found {} payee ach accounts to extract", numRowsInserted);
    }

    @Override
    public void storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(final String employeeId,
            final KualiInteger achAccountGeneratedIdentifier, final String jobRunDateString) {

        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_MAPPING_PYMNT_ELCTN_EXTR_FILE_T ")
                .append("(EMPLOYEE_ID, ACH_ACCT_GNRTD_ID, EXTR_FILE_RUNDATE) ")
                .append("VALUES (").appendAsParameter(Types.VARCHAR, employeeId)
                .append(", ").appendAsParameter(Types.INTEGER, achAccountGeneratedIdentifier)
                .append(", ").appendAsParameter(Types.VARCHAR, jobRunDateString)
                .append(")")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        if (numRowsInserted != 1) {
            LOG.error("storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping, Query should have inserted 1 row,"
                    + " but it inserted {} rows instead", numRowsInserted);
            throw new RuntimeException(String.format("Failed to insert EmployeeId-AchAccountGeneratedIdentifier-JobRunDate"
                    + " row for: Employee_ID %s, ACH Account Generated Identifier %s, extraction job run datetime %s.",
                    employeeId, achAccountGeneratedIdentifier, jobRunDateString));
        }
    }

}
