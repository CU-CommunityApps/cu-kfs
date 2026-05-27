package edu.cornell.kfs.cemi.vnd.dataaccess.impl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiRemitToSupplierDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc implements CemiRemitToSupplierDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearExistingListOfExtractableRemitAddressIds() {
        LOG.info("clearExistingListOfExtractableRemitAddressIds was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE KFS.CU_CEMI_EXTR_RMT_TO_SUPP_RMT_TO_ADDR_T");
        executeUpdate(query);
        LOG.info("clearExistingListOfExtractableRemitAddressIds finished truncating table.");
    }

    @Override
    public void updateRemitToSupplierExtractQuerySettings(final String supplierJobRunDate) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE KFS.CU_CEMI_RMT_TO_SUPP_QUERY_SETTINGS_T ")
                .append("SET SUPP_EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDate)
                .toQuery();

        final int numRowsUpdated = executeUpdate(query);
        if (numRowsUpdated != 1) {
            LOG.error("updateRemitToSupplierExtractQuerySettings, Query should have updated 1 row, "
                    + "but it updated {} instead", numRowsUpdated);
            throw new RuntimeException("Failed to update Remit To Supplier query settings");
        }
    }

    @Override
    public void queryAndStoreAddressIdsForRemitToSupplierExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO KFS.CU_CEMI_EXTR_RMT_TO_SUPP_RMT_TO_ADDR_T (EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID) ")
                .append("SELECT EXTR_FILE_RUNDATE, SUPP_ADDRESS_ID ")
                .append("FROM KFS.CU_CEMI_RMT_TO_SUPP_EXTR_SUPP_ADDR_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreAddressIdsForRemitToSupplierExtract, Found {} addresses to extract", numRowsInserted);
    }

}
