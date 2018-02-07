package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;

import org.kuali.kfs.sys.KFSConstants;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao{
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp  processingTimeStamp) {
        String updateSql = buildUpdateExistingPaymentWorksVendorInStagingTableSql(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, processingTimeStamp);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: updateSQL = " + updateSql);
        getJdbcTemplate().batchUpdate(updateSql);
    }
    
    private String buildUpdateExistingPaymentWorksVendorInStagingTableSql(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp  processingTimeStamp) {
        StringBuilder sql = new StringBuilder();
        sql.append("update kfs.cu_pmw_vendor_t ");
        sql.append("set pmw_req_stat = '" + pmwRequestStatus); 
        sql.append("', kfs_vnd_proc_stat = '" + kfsVendorProcessingStatus);
        sql.append("', proc_ts = '" + PROCESSING_TIMESTAMP_SQL_FORMATTER.format(processingTimeStamp));
        if (StringUtils.isNotBlank(kfsAchProcessingStatus)) {
            sql.append("', kfs_ach_proc_stat = '" + kfsAchProcessingStatus);
        }
        if (StringUtils.isNotBlank(kfsVendorDocumentNumber)) {
            sql.append("', pven_fdoc_nbr = '" + kfsVendorDocumentNumber);
        }
        sql.append("' where pmw_vnd_req_id = '" + pmwVendorRequestId + "'");
        
        return sql.toString();
    }

}
