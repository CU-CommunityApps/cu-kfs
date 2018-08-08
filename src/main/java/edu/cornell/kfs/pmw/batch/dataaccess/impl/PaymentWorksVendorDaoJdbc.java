package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.sys.CUKFSConstants;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    
    private static final int MAX_LIST_CHUNK_SIZE = 1000;

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

    @Override
    public void updateSupplierUploadStatusesForVendorsInStagingTable(List<Integer> ids, String supplierUploadStatus) {
        updateSupplierUploadStatusesForVendorsInStagingTable(ids, KFSConstants.EMPTY_STRING, supplierUploadStatus);
    }

    @Override
    public void updateSupplierUploadStatusesForVendorsInStagingTable(List<Integer> ids, String pmwRequestStatus, String supplierUploadStatus) {
        ParameterizedSqlFactory sqlFactory = new ParameterizedSqlFactory("update kfs.cu_pmw_vendor_t set ");
        
        if (StringUtils.isNotBlank(pmwRequestStatus)) {
            sqlFactory.appendSql("pmw_req_stat = ?, ", pmwRequestStatus);
        }
        sqlFactory.appendSql("supp_upld_stat = ? ", supplierUploadStatus);
        
        sqlFactory.appendSql("where ");
        appendInCondition(sqlFactory, "id", ids);
        
        int updateRowCount = getJdbcTemplate().update(sqlFactory.getSql(), sqlFactory.getParameters().toArray());
        LOG.info("updateSupplierUploadStatusForVendorsInStagingTable, updated the following number of records: " + updateRowCount);
    }

    private void appendInCondition(ParameterizedSqlFactory sqlFactory, String columnName, List<?> values) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("Cannot create an IN condition from an empty values list");
        }
        
        int[] chunkSizes = computeInConditionChunkSizes(values);
        int chunkStartIndex = 0;
        
        sqlFactory.appendSql(CUKFSConstants.LEFT_PARENTHESIS);
        for (int chunkSize : chunkSizes) {
            if (chunkStartIndex != 0) {
                sqlFactory.appendSql(" or ");
            }
            List<?> valuesChunk = values.subList(chunkStartIndex, chunkStartIndex + chunkSize);
            appendInConditionChunk(sqlFactory, columnName, valuesChunk);
            chunkStartIndex += chunkSize;
        }
        sqlFactory.appendSql(CUKFSConstants.RIGHT_PARENTHESIS);
    }

    private int[] computeInConditionChunkSizes(List<?> values) {
        int listSizeRemainder = values.size() % MAX_LIST_CHUNK_SIZE;
        int chunkCount = (int) Math.ceil(values.size() / (double) MAX_LIST_CHUNK_SIZE);
        
        int[] chunkSizes = new int[chunkCount];
        Arrays.fill(chunkSizes, MAX_LIST_CHUNK_SIZE);
        if (listSizeRemainder > 0) {
            chunkSizes[chunkSizes.length - 1] = listSizeRemainder;
        }
        return chunkSizes;
    }

    private void appendInConditionChunk(ParameterizedSqlFactory sqlFactory, String columnName, List<?> valuesChunk) {
        sqlFactory.appendSql(columnName);
        sqlFactory.appendSql(" in ");
        sqlFactory.appendSql(CUKFSConstants.LEFT_PARENTHESIS);
        
        sqlFactory.appendSql("?", valuesChunk.get(0));
        valuesChunk.stream()
                .skip(1L)
                .forEach((value) -> sqlFactory.appendSql(", ?", value));
        
        sqlFactory.appendSql(CUKFSConstants.RIGHT_PARENTHESIS);
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
