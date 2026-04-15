package edu.cornell.kfs.vnd.dataaccess.impl;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.vnd.CemiVendorConstants.CemiQuerySettingsIds;
import edu.cornell.kfs.vnd.dataaccess.CemiVendorDao;

public class CemiVendorDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiVendorDao {

    private static final Logger LOG = LogManager.getLogger();

    private DateTimeService dateTimeService;

    @Override
    public void clearExistingListOfBaseVendorData() {
        LOG.info("clearExistingListOfBaseVendorData was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_VNDR_BASE_DATA_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfBaseVendorData finished truncating table.");
    }

    @Override
    public void clearExistingListOfExtractableVendorIds() {
        LOG.info("clearExistingListOfExtractableVendorIds was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_SPLR_EXTR_VNDR_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractableVendorIds finished truncating table.");
    }
    
    @Override
    public void updateSupplierExtractQuerySettings(final LocalDate fromDate, final LocalDate toDate) {
        final ZoneId easternTimeZone = ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN);
        final LocalDate currentDate = dateTimeService.getLocalDateNow();

        final ZonedDateTime fromDateTime = ZonedDateTime.of(
                fromDate, LocalTime.of(0, 0, 0, 0), easternTimeZone);
        final ZonedDateTime toDateTime = ZonedDateTime.of(
                toDate, LocalTime.of(23, 59, 59, 0), easternTimeZone);
        final ZonedDateTime startOfYear = ZonedDateTime.of(
                LocalDate.of(currentDate.getYear(), 1, 1), LocalTime.of(0, 0, 0, 0), easternTimeZone);
        final ZonedDateTime september1stOfPriorYear = ZonedDateTime.of(
                LocalDate.of(currentDate.getYear() - 1, 9, 1), LocalTime.of(0, 0, 0, 0), easternTimeZone);

        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE KFS.CU_CEMI_QUERY_SETTINGS_T ")
                .append("SET DATETIME_RANGE_FROM = ").appendAsParameter(Types.TIMESTAMP, fromDateTime)
                .append(", DATETIME_RANGE_TO = ").appendAsParameter(Types.TIMESTAMP, toDateTime)
                .append(", START_OF_YEAR = ").appendAsParameter(Types.TIMESTAMP, startOfYear)
                .append(", SEPT_1_OF_PRIOR_YEAR = ").appendAsParameter(Types.TIMESTAMP, september1stOfPriorYear)
                .append(" WHERE SETTINGS_ID = ").appendAsParameter(CemiQuerySettingsIds.SUPPLIERS)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateSupplierExtractQuerySettings, Query should have updated 1 row, but it updated {} instead",
                    numRowsUpdated);
            throw new RuntimeException("Failed to update settings for ID: " + CemiQuerySettingsIds.SUPPLIERS);
        }
    }

    @Override
    public void prepareBaseVendorDataNeededForMainVendorIdQuery() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_VNDR_BASE_DATA_T ")
                .append("(VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, PAYEE_ID, VNDR_PARENT_IND, LAST_UPDT_TS) ")
                .append("SELECT ")
                .append("VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, PAYEE_ID, VNDR_PARENT_IND, LAST_UPDT_TS ")
                .append("FROM KFS.CU_CEMI_VNDR_BASE_DATA_SRC_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("prepareBaseVendorDataNeededForMainVendorIdQuery, Found {} vendors for main query", numRowsInserted);
    }

    @Override
    public void queryAndStoreVendorIdsForSupplierExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_SPLR_EXTR_VNDR_T (VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID) ")
                .append("SELECT VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID ")
                .append("FROM KFS.PUR_CEMI_VNDR_EXTRACT_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreVendorIdsForSupplierExtract, Found {} vendors to extract", numRowsInserted);
    }
    
    @Override
    public void storeSupplierIdVendorIdSupplierExtractRunDateMapping(final String supplierId,
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier,
            final LocalDateTime jobRunDate) {
        
        String jobRunDateAsString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);

        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_MAPPING_SPLR_VNDR_EXTR_FILE_T ")
                .append("(WKDY_SPLR_ID, VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, EXTR_FILE_RUNDATE) ")
                .append("VALUES (").appendAsParameter(Types.VARCHAR, supplierId)
                .append(", ").appendAsParameter(Types.INTEGER, vendorHeaderGeneratedIdentifier)
                .append(", ").appendAsParameter(Types.INTEGER, vendorDetailAssignedIdentifier)
                .append(", ").appendAsParameter(Types.VARCHAR, jobRunDateAsString)
                .append(")")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        if (numRowsInserted != 1) {
            LOG.error("storeSupplierIdVendorIdSupplierExtractRunDateMapping, Query should have inserted 1 row,"
                    + " but it inserted {} instead", numRowsInserted);
            throw new RuntimeException(String.format("Failed to insert Supplier-Vendor-JobRunDate row for:"
                    + " supplierId %s, vendor id %s-%s, extraction job run datetime %s.", supplierId,
                    vendorHeaderGeneratedIdentifier.intValue(), vendorDetailAssignedIdentifier.intValue(),
                    jobRunDateAsString));
        }
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
