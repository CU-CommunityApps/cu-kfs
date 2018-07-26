package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;

import org.kuali.kfs.sys.KFSConstants;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao{
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp, null, null, null);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp  processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, supplierUploadStatus, processingTimeStamp, null, null, null);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String kfsVendorProcessingStatus, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, Timestamp processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, KFSConstants.EMPTY_STRING, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp, vendorHeaderGeneratedIdentifier, vendorDetailAssignedIdentifier, null);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, KFSConstants.EMPTY_STRING, supplierUploadStatus, processingTimeStamp, null, null, null);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp, String kfsAchDocumentNumber) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(id, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, KFSConstants.EMPTY_STRING, supplierUploadStatus, processingTimeStamp, null, null, kfsAchDocumentNumber);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }

    private String buildUpdateExistingPaymentWorksVendorInStagingTableSql(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus,
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

}
