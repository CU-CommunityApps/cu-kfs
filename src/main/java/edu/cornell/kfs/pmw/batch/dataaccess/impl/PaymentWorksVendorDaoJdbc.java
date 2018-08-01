package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, null, null, null, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp  processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, supplierUploadStatus, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String kfsVendorProcessingStatus, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, null, kfsVendorProcessingStatus, null, null, null, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, null, supplierUploadStatus, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp, String kfsAchDocumentNumber) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, null, supplierUploadStatus, processingTimeStamp, null, null, kfsAchDocumentNumber);
    }

    private void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus,
                                                                Timestamp processingTimeStamp, Integer vendorHeaderAssignedIdentifier, Integer vendorDetailAssignedIdentifier, String kfsAchDocumentNumber) {
        try {
            PreparedStatement preparedSqlStatement = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, supplierUploadStatus, processingTimeStamp, vendorHeaderAssignedIdentifier, vendorDetailAssignedIdentifier, kfsAchDocumentNumber);
            preparedSqlStatement.executeUpdate();
            int updatedRowCount = preparedSqlStatement.getUpdateCount();
            LOG.info("updateExistingPaymentWorksVendorInStagingTable updated " + updatedRowCount + " record" + (updatedRowCount == 1 ? "" : "s"));
        }
        catch (SQLException ex) {
            LOG.error("updateExistingPaymentWorksVendorInStagingTable", ex);
        }
    }

    private String buildUpdateExistingPaymentWorksVendorInStagingTableSql_OLD(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus,
                                                                          Timestamp  processingTimeStamp, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String kfsAchDocumentNumber) {
        StringBuilder sql = new StringBuilder();
        sql.append("update kfs.cu_pmw_vendor_t set");
        if (StringUtils.isNotBlank(pmwRequestStatus)) {
            sql.append(" pmw_req_stat = '" + pmwRequestStatus + "',");
        }
        sql.append(" kfs_vnd_proc_stat = '" + kfsVendorProcessingStatus + "',");
        sql.append(" proc_ts = '" + PROCESSING_TIMESTAMP_SQL_FORMATTER.format(processingTimeStamp) + "'");
        if (StringUtils.isNotBlank(kfsAchProcessingStatus)) {
            sql.append(", kfs_ach_proc_stat = '" + kfsAchProcessingStatus + "'");
        }
        if (StringUtils.isNotBlank(supplierUploadStatus)) {
            sql.append(", supp_upld_stat = '" + supplierUploadStatus + "'");
        }
        if (StringUtils.isNotBlank(kfsVendorDocumentNumber)) {
            sql.append(", pven_fdoc_nbr = '" + kfsVendorDocumentNumber + "'");
        }
        if (ObjectUtils.isNotNull(vendorHeaderGeneratedIdentifier)) {
            sql.append(", vndr_hdr_gnrtd_id = '" + vendorHeaderGeneratedIdentifier.intValue() + "'");
        }
        if (ObjectUtils.isNotNull(vendorDetailAssignedIdentifier)) {
            sql.append(", vndr_dtl_asnd_id = '" + vendorDetailAssignedIdentifier.intValue() + "'");
        }
        if (ObjectUtils.isNotNull(kfsAchDocumentNumber)) {
            sql.append(", paat_fdoc_nbr = '" + kfsAchDocumentNumber + "'");
        }
        sql.append(" where id = '" + id + "'");
        
        return sql.toString();
    }

    private PreparedStatement buildUpdateExistingPaymentWorksVendorInStagingTableSql(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp  processingTimeStamp, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String kfsAchDocumentNumber) throws SQLException {
        ParameterizedSqlFactory sqlFactory = new ParameterizedSqlFactory("update kfs.cu_pmw_vendor_t set");

        if (StringUtils.isNotBlank(pmwRequestStatus)) {
            sqlFactory.appendSql(" pmw_req_stat = ?,", pmwRequestStatus);
        }

        sqlFactory.appendSql(" kfs_vnd_proc_stat = ?,", kfsVendorProcessingStatus);
        sqlFactory.appendSql(" proc_ts = ?", PROCESSING_TIMESTAMP_SQL_FORMATTER.format(processingTimeStamp));

        if (StringUtils.isNotBlank(kfsAchProcessingStatus)) {
            sqlFactory.appendSql(", kfs_ach_proc_stat = ?", kfsAchProcessingStatus);
        }
        if (StringUtils.isNotBlank(supplierUploadStatus)) {
            sqlFactory.appendSql(", supp_upld_stat = ?", supplierUploadStatus);
        }
        if (StringUtils.isNotBlank(kfsVendorDocumentNumber)) {
            sqlFactory.appendSql(", pven_fdoc_nbr = ?", kfsVendorDocumentNumber);
        }
        if (ObjectUtils.isNotNull(vendorHeaderGeneratedIdentifier)) {
            sqlFactory.appendSql(", vndr_hdr_gnrtd_id = ?", vendorHeaderGeneratedIdentifier);
        }
        if (ObjectUtils.isNotNull(vendorDetailAssignedIdentifier)) {
            sqlFactory.appendSql(", vndr_dtl_asnd_id = ?", vendorDetailAssignedIdentifier);
        }
        if (ObjectUtils.isNotNull(kfsAchDocumentNumber)) {
            sqlFactory.appendSql(", paat_fdoc_nbr = ?", kfsAchDocumentNumber);
        }
        sqlFactory.appendSql(" where id = ?", id);

        PreparedStatement preparedSqlStatement = sqlFactory.getPreparedSqlStatement();
        return preparedSqlStatement;
    }

    private class ParameterizedSqlFactory {
        private StringBuilder sqlStringBuilder;
        private ArrayList parameters;

        public ParameterizedSqlFactory(String initialSql) {
            sqlStringBuilder = new StringBuilder(initialSql);
            parameters = new ArrayList();
        }

        public void appendSql(String sql) {
            sqlStringBuilder.append(sql);
        }

        public void appendSql(String sql, Object parameter) {
            appendSql(sql);
            parameters.add(parameter);
        }

        public PreparedStatement getPreparedSqlStatement() throws SQLException {
            PreparedStatement preparedSqlStatement = getConnection().prepareStatement(sqlStringBuilder.toString());
            for (int parameterIndex=1; parameterIndex<=parameters.size(); ++parameterIndex) {
                addParameterToPreparedStatement(preparedSqlStatement, parameterIndex, parameters.get(parameterIndex - 1));
            }
            return preparedSqlStatement;
        }

        private void addParameterToPreparedStatement(PreparedStatement preparedSqlStatement, int parameterIndex, Object parameterValue) throws SQLException {
            if (parameterValue instanceof Integer) {
                preparedSqlStatement.setInt(parameterIndex, (Integer) parameterValue);
            }
            else {
                preparedSqlStatement.setString(parameterIndex, parameterValue.toString());
            }
        }

        public String toString() {
            String effectiveSql = sqlStringBuilder.toString();
            int parameterIndex = parameters.size();

            while (parameterIndex > 0) {
                int lastQIndex = effectiveSql.lastIndexOf("?");
                Object parameter = parameters.get(parameterIndex - 1);
                String parameterSql = parameter instanceof String ? "'" + parameter.toString() + "'" : parameter.toString();
                effectiveSql = effectiveSql.substring(0, lastQIndex) + parameterSql + effectiveSql.substring(lastQIndex + 1);

                --parameterIndex;
            }

            return effectiveSql;
        }
    }
}
