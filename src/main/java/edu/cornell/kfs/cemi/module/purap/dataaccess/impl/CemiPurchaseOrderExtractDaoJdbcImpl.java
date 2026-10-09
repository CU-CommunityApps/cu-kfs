package edu.cornell.kfs.cemi.module.purap.dataaccess.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrder;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrderAccount;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrderItem;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderExtractRow;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;
import edu.cornell.kfs.sys.util.CuSqlQueryPreparedStatementCreatorAndSetter;

public class CemiPurchaseOrderExtractDaoJdbcImpl extends CuSqlQueryPlatformAwareDaoBaseJdbc
        implements CemiPurchaseOrderExtractDao {

    private static final Logger LOG = LogManager.getLogger();

    private static final int EXTRACT_ROWS_FETCH_SIZE = 500;

    private ConfigurationService configurationService;

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
    public Stream<CemiPurchaseOrderExtractRow> getPurchaseOrderExtractRowsAsCloseableStream(
            final String supplierJobRunDateString) {
        final CuSqlChunk sql = new CuSqlChunk()
                .append("SELECT H.FDOC_NBR, H.PO_ID, H.DOC_HDR_STAT_CD, H.APRV_DT, ")
                .append("H.VNDR_HDR_GNRTD_ID, H.VNDR_DTL_ASND_ID, SUP.SUPPLIER_ID, H.VNDR_CONTR_GNRTD_ID, ")
                .append("H.VNDR_PMT_TERM_CD, H.VNDR_PMT_TERM_DESC, ")
                .append("H.RQSTR_PRSN_NM, H.RQSTR_PRSN_EMAIL_ADDR, H.DLVY_TO_NM, H.DLVY_TO_EMAIL_ADDR, ")
                .append("H.DLVY_BLDG_LN1_ADDR, H.DLVY_BLDG_LN2_ADDR, H.DLVY_BLDG_RM_NBR, ")
                .append("H.DLVY_CTY_NM, H.DLVY_ST_CD, H.DLVY_PSTL_CD, H.DLVY_CNTRY_NM, ")
                .append("H.PO_TOT_AMT, H.FRHT_OSTND_ENC_AMT, ")
                .append("I.PO_ITM_ID, I.ITM_LN_NBR, I.ITM_TYP_CD, I.ITM_CATLG_NBR, I.PUR_COMM_CD, I.ITM_DESC, ")
                .append("I.ITM_UOM_CD, I.ITM_UNIT_PRC, I.ITM_OSTND_ENC_QTY, I.ITM_OSTND_ENC_AMT, I.ITM_TOT_AMT, ")
                .append("A.PO_ACCT_ID, A.ITM_ACCT_OSTND_ENCUM_AMT ")
                .append("FROM CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_HDR_V H ")
                .append("LEFT JOIN (")
                        .append("SELECT VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID, MIN(SUPPLIER_ID) AS SUPPLIER_ID ")
                        .append("FROM CEMI.CU_CEMI_EXTR_SUPPLIER_TAB_SUPPLIER_T ")
                        .append("WHERE EXTR_FILE_RUNDATE = ").appendAsParameter(supplierJobRunDateString)
                        .append(" GROUP BY VNDR_HDR_GNRTD_ID, VNDR_DTL_ASND_ID")
                .append(") SUP ON SUP.VNDR_HDR_GNRTD_ID = H.VNDR_HDR_GNRTD_ID ")
                        .append("AND SUP.VNDR_DTL_ASND_ID = H.VNDR_DTL_ASND_ID ")
                .append("LEFT JOIN CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_ITM_V I ON I.FDOC_NBR = H.FDOC_NBR ")
                .append("LEFT JOIN CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_OPEN_ACCT_V A ON A.FDOC_NBR = I.FDOC_NBR ")
                        .append("AND A.PO_ITM_ID = I.PO_ITM_ID ");

        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            sql.append("WHERE H.FDOC_NBR IN (")
                    .append("SELECT FDOC_NBR FROM (")
                            .append("SELECT FDOC_NBR FROM CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_IN_SCOPE_PO_DOCS_T ")
                            .append("WHERE FDOC_NBR <= '20000000' OR FDOC_NBR >= '64471000' ")
                            .append("ORDER BY FDOC_NBR")
                    .append(") WHERE ROWNUM <= 100")
                    .append(") ");
        }

        sql.append("ORDER BY H.FDOC_NBR, I.ITM_LN_NBR, I.PO_ITM_ID, A.PO_ACCT_ID");

        final CuSqlQuery query = sql.toQuery();
        return runQuery(query, true, () -> {
            final CuSqlQueryPreparedStatementCreatorAndSetter statementHandler =
                    CuSqlQueryPreparedStatementCreatorAndSetter.forReadOnlyResults(query);
            return getJdbcTemplate().queryForStream(
                    connection -> {
                        final PreparedStatement preparedStatement =
                                statementHandler.createPreparedStatement(connection);
                        preparedStatement.setFetchSize(EXTRACT_ROWS_FETCH_SIZE);
                        return preparedStatement;
                    },
                    statementHandler,
                    (resultSet, rowNum) -> mapExtractRow(resultSet));
        });
    }

    private CemiPurchaseOrderExtractRow mapExtractRow(final ResultSet resultSet) throws SQLException {
        final CemiLegacyPurchaseOrder purchaseOrder = mapPurchaseOrder(resultSet);
        final Optional<CemiLegacyPurchaseOrderItem> item = mapItemIfPresent(resultSet, purchaseOrder);
        final Optional<CemiLegacyPurchaseOrderAccount> accountingLine = item.isPresent()
                ? mapAccountingLineIfPresent(resultSet) : Optional.empty();
        return new CemiPurchaseOrderExtractRow(purchaseOrder, item, accountingLine);
    }

    private CemiLegacyPurchaseOrder mapPurchaseOrder(final ResultSet resultSet) throws SQLException {
        final CemiLegacyPurchaseOrder purchaseOrder = new CemiLegacyPurchaseOrder();
        purchaseOrder.setDocumentNumber(resultSet.getString("FDOC_NBR"));
        purchaseOrder.setPurapDocumentIdentifier(getInteger(resultSet, "PO_ID"));
        purchaseOrder.setDocumentStatusCode(resultSet.getString("DOC_HDR_STAT_CD"));
        final Timestamp approvedDate = resultSet.getTimestamp("APRV_DT");
        purchaseOrder.setApprovedDate(approvedDate != null ? approvedDate.toLocalDateTime() : null);
        purchaseOrder.setVendorHeaderGeneratedIdentifier(getInteger(resultSet, "VNDR_HDR_GNRTD_ID"));
        purchaseOrder.setVendorDetailAssignedIdentifier(getInteger(resultSet, "VNDR_DTL_ASND_ID"));
        purchaseOrder.setSupplierId(resultSet.getString("SUPPLIER_ID"));
        purchaseOrder.setVendorContractGeneratedIdentifier(getInteger(resultSet, "VNDR_CONTR_GNRTD_ID"));
        purchaseOrder.setPaymentTermsTypeCode(resultSet.getString("VNDR_PMT_TERM_CD"));
        purchaseOrder.setPaymentTermsDescription(resultSet.getString("VNDR_PMT_TERM_DESC"));
        purchaseOrder.setRequestorPersonName(resultSet.getString("RQSTR_PRSN_NM"));
        purchaseOrder.setRequestorPersonEmailAddress(resultSet.getString("RQSTR_PRSN_EMAIL_ADDR"));
        purchaseOrder.setDeliveryToName(resultSet.getString("DLVY_TO_NM"));
        purchaseOrder.setDeliveryToEmailAddress(resultSet.getString("DLVY_TO_EMAIL_ADDR"));
        purchaseOrder.setDeliveryBuildingLine1Address(resultSet.getString("DLVY_BLDG_LN1_ADDR"));
        purchaseOrder.setDeliveryBuildingLine2Address(resultSet.getString("DLVY_BLDG_LN2_ADDR"));
        purchaseOrder.setDeliveryBuildingRoomNumber(resultSet.getString("DLVY_BLDG_RM_NBR"));
        purchaseOrder.setDeliveryCityName(resultSet.getString("DLVY_CTY_NM"));
        purchaseOrder.setDeliveryStateCode(resultSet.getString("DLVY_ST_CD"));
        purchaseOrder.setDeliveryPostalCode(resultSet.getString("DLVY_PSTL_CD"));
        purchaseOrder.setDeliveryCountryName(resultSet.getString("DLVY_CNTRY_NM"));
        purchaseOrder.setTotalDollarAmount(getKualiDecimal(resultSet, "PO_TOT_AMT"));
        purchaseOrder.setFreightOutstandingEncumberedAmount(getKualiDecimal(resultSet, "FRHT_OSTND_ENC_AMT"));
        return purchaseOrder;
    }

    private Optional<CemiLegacyPurchaseOrderItem> mapItemIfPresent(final ResultSet resultSet,
            final CemiLegacyPurchaseOrder purchaseOrder) throws SQLException {
        final Integer itemIdentifier = getInteger(resultSet, "PO_ITM_ID");
        if (itemIdentifier == null) {
            return Optional.empty();
        }
        final CemiLegacyPurchaseOrderItem item = new CemiLegacyPurchaseOrderItem();
        item.setDocumentNumber(purchaseOrder.getDocumentNumber());
        item.setPurchaseOrderId(purchaseOrder.getPurapDocumentIdentifier());
        item.setItemIdentifier(itemIdentifier);
        item.setItemLineNumber(getInteger(resultSet, "ITM_LN_NBR"));
        item.setItemTypeCode(resultSet.getString("ITM_TYP_CD"));
        item.setItemCatalogNumber(resultSet.getString("ITM_CATLG_NBR"));
        item.setPurchasingCommodityCode(resultSet.getString("PUR_COMM_CD"));
        item.setItemDescription(resultSet.getString("ITM_DESC"));
        item.setItemUnitOfMeasureCode(resultSet.getString("ITM_UOM_CD"));
        item.setItemUnitPrice(resultSet.getBigDecimal("ITM_UNIT_PRC"));
        item.setItemOutstandingEncumberedQuantity(getKualiDecimal(resultSet, "ITM_OSTND_ENC_QTY"));
        item.setItemOutstandingEncumberedAmount(getKualiDecimal(resultSet, "ITM_OSTND_ENC_AMT"));
        item.setTotalAmount(getKualiDecimal(resultSet, "ITM_TOT_AMT"));
        return Optional.of(item);
    }

    private Optional<CemiLegacyPurchaseOrderAccount> mapAccountingLineIfPresent(final ResultSet resultSet)
            throws SQLException {
        final Integer accountIdentifier = getInteger(resultSet, "PO_ACCT_ID");
        if (accountIdentifier == null) {
            return Optional.empty();
        }
        final CemiLegacyPurchaseOrderAccount accountingLine = new CemiLegacyPurchaseOrderAccount();
        accountingLine.setAccountIdentifier(accountIdentifier);
        accountingLine.setItemAccountOutstandingEncumbranceAmount(
                getKualiDecimal(resultSet, "ITM_ACCT_OSTND_ENCUM_AMT"));
        return Optional.of(accountingLine);
    }

    private Integer getInteger(final ResultSet resultSet, final String columnName) throws SQLException {
        final int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : Integer.valueOf(value);
    }

    private KualiDecimal getKualiDecimal(final ResultSet resultSet, final String columnName) throws SQLException {
        final BigDecimal value = resultSet.getBigDecimal(columnName);
        return value != null ? new KualiDecimal(value) : null;
    }

    private boolean shouldUseLessDataDuringCemiDevelopment() {
        return configurationService.getPropertyValueAsBoolean(
                CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
