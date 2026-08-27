package edu.cornell.kfs.cemi.module.purap.dataaccess.impl;

import java.sql.Types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

public class CemiPurchaseOrderExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements CemiPurchaseOrderExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution() {
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution was called.");
        final CuSqlQuery query = CuSqlQuery.of("TRUNCATE TABLE CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_IN_SCOPE_PO_DOCS_T");
        executeUpdate(query);
        LOG.info("clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution finished truncating table.");
    }

    @Override
    public void queryAndStoreInScopeBusinessObjectKeysForDataExtract() {
        final CuSqlQuery query = new CuSqlChunk()
                .append("INSERT INTO CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_IN_SCOPE_PO_DOCS_T (FDOC_NBR, DOC_TYP_NM) ")
                .append("SELECT FDOC_NBR, DOC_TYP_NM ")
                .append("FROM CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_PO_DOCS_V")
                .toQuery();

        final int numRowsInserted = executeUpdate(query);
        LOG.info("queryAndStoreInScopeBusinessObjectKeysForDataExtract, Found {} in scope business object to extract", numRowsInserted);
    }

    @Override
    public String getSupplierIdForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier, final String supplierJobRunDateString) {
        final CuSqlQuery query = new CuSqlChunk()
                .append("SELECT SUPPLIER_ID FROM CEMI.CU_CEMI_EXTR_SUPPLIER_TAB_SUPPLIER_T ")
                .append("WHERE EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDateString)
                .append(" AND VNDR_HDR_GNRTD_ID = ").appendAsParameter(Types.INTEGER, vendorHeaderGeneratedIdentifier)
                .append(" AND VNDR_DTL_ASND_ID = ").appendAsParameter(Types.INTEGER, vendorDetailAssignedIdentifier)
                .toQuery();
        return queryForResults(query,
                resultSet -> resultSet.next() ? resultSet.getString(1) : CemiBaseConstants.EMPTY_STRING);
    }

}
