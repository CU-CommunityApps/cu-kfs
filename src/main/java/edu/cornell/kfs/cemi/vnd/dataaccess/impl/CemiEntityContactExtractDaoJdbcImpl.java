package edu.cornell.kfs.cemi.vnd.dataaccess.impl;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.vnd.dataaccess.CemiEntityContactExtractDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiEntityContactExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements CemiEntityContactExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeVendorContactKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeVendorContactKeysFromPreviousExecution was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_EXTR_ENT_CNTCT_VNDR_CNTCT_T");
        executeUpdate(query);
        LOG.info("clearAnyExistingInScopeVendorContactKeysFromPreviousExecution finished truncating table.");
    }

    @Override
    public void updateEntityContactExtractQuerySettings(final String supplierJobRunDateString) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE CEMI.CU_CEMI_EXTR_ENT_CNTCT_QUERY_SETTINGS_T ")
                .append("SET SUPP_EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDateString)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateEntityContactExtractQuerySettings, Query should have updated 1 row, "
                    + "but it updated {} instead", numRowsUpdated);
            throw new RuntimeException("Failed to update Business Entity Contact query settings");
        }
    }

    @Override
    public void queryAndStoreInScopeVendorContactKeysForDataExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_EXTR_ENT_CNTCT_VNDR_CNTCT_T (VNDR_CNTCT_GNRTD_ID) ")
                .append("SELECT VNDR_CNTCT_GNRTD_ID ")
                .append("FROM CEMI.CU_CEMI_EXTR_ENT_CNTCT_VNDR_CNTCT_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreInScopeVendorContactKeysForDataExtract, Found {} in scope Vendor Contacts to extract",
                numRowsInserted);
    }

    @Override
    public String findSupplierIdForVendorContact(final Integer vendorContactGeneratedIdentifier,
            final String supplierJobRunDateString) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT SMP.WKDY_SPLR_ID ")
                .append("FROM CEMI.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T SMP ")
                .append("JOIN KFS.PUR_VNDR_CNTCT_T VCT ")
                .append("ON SMP.VNDR_HDR_GNRTD_ID = VCT.VNDR_HDR_GNRTD_ID ")
                .append("AND SMP.VNDR_DTL_ASND_ID = VCT.VNDR_DTL_ASND_ID ")
                .append("AND SMP.EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDateString)
                .append(" WHERE VCT.VNDR_CNTCT_GNRTD_ID = ")
                        .appendAsParameter(Types.INTEGER, vendorContactGeneratedIdentifier)
                .toQuery();

        final String supplierId = queryForResults(query, resultSet -> {
            Validate.validState(resultSet.next(), "No Supplier ID found for Vendor Contact %s and Supplier Extract %s",
                    vendorContactGeneratedIdentifier, supplierJobRunDateString);
            final String idValue = resultSet.getString(1);
            Validate.validState(StringUtils.isNotBlank(idValue),
                    "Found a blank Supplier ID for Vendor Contact %s and Supplier Extract %s",
                    vendorContactGeneratedIdentifier, supplierJobRunDateString);
            return idValue;
        });

        return supplierId;
    }

}
