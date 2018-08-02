package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import org.springframework.jdbc.core.JdbcTemplate;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp  processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, supplierUploadStatus, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String kfsVendorProcessingStatus, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, KFSConstants.EMPTY_STRING, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp, vendorHeaderGeneratedIdentifier, vendorDetailAssignedIdentifier, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, KFSConstants.EMPTY_STRING, supplierUploadStatus, processingTimeStamp, null, null, null);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp, String kfsAchDocumentNumber) {
        updateExistingPaymentWorksVendorInStagingTable(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, KFSConstants.EMPTY_STRING, supplierUploadStatus, processingTimeStamp, null, null, kfsAchDocumentNumber);
    }

    private void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus,
                                                                Timestamp processingTimeStamp, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String kfsAchDocumentNumber) {

        int updatedRowCount = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, supplierUploadStatus, processingTimeStamp, vendorHeaderGeneratedIdentifier, vendorDetailAssignedIdentifier, kfsAchDocumentNumber);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable updated " + updatedRowCount + " record" + (updatedRowCount == 1 ? "" : "s"));
    }

    private int buildUpdateExistingPaymentWorksVendorInStagingTableSql(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp  processingTimeStamp, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String kfsAchDocumentNumber) {
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

        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        return jdbcTemplate.update(sqlFactory.getSql(), sqlFactory.getParameters().toArray());
    }

    private class ParameterizedSqlFactory {
        private StringBuilder sqlStringBuilder;
        private List<Object> parameters;

        public ParameterizedSqlFactory(String initialSql) {
            sqlStringBuilder = new StringBuilder(initialSql);
            parameters = new ArrayList<>();
        }

        public void appendSql(String sql) {
            sqlStringBuilder.append(sql);
        }

        public void appendSql(String sql, Object parameter) {
            appendSql(sql);
            parameters.add(parameter);
        }

        public String toString() {
            String effectiveSql = sqlStringBuilder.toString();
            for (Object parameter : parameters) {
                effectiveSql = effectiveSql.replaceFirst("\\?", parameter instanceof Integer ? ("%d") : "'%s'");
            }
            return String.format(effectiveSql, parameters.toArray());
        }

        public String getSql() {
            return sqlStringBuilder.toString();
        }

        public List<Object> getParameters() {
            return parameters;
        }
    }
}
