package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksDataTransformationService;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestDTO;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao{
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    
    @Override
    public boolean isExistingPaymentWorksVendor(String pmwVendorId) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put(PaymentWorksConstants.PaymentWorksStagingTableColumnConstants.PMW_VENDOR_REQUEST_ID, pmwVendorId);
        return(getBusinessObjectService().countMatching(PaymentWorksVendor.class, fieldValues) > 0);
    }
    
    @Override
    public PaymentWorksVendor savePaymentWorksVendorToStagingTable(PaymentWorksVendor pmwVendorToSave) {
        pmwVendorToSave.setProcessTimestamp(getDateTimeService().getCurrentTimestamp());
        pmwVendorToSave = getBusinessObjectService().save(pmwVendorToSave);
        return pmwVendorToSave;
    }
    
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
        sql.append("', proc_ts = '" + PaymentWorksDataTransformationService.PROCESSING_TIMESTAMP_SQL_FORMATTER.format(processingTimeStamp));
        if (StringUtils.isNotBlank(kfsAchProcessingStatus)) {
            sql.append("', kfs_ach_proc_stat = '" + kfsAchProcessingStatus);
        }
        if (StringUtils.isNotBlank(kfsAchProcessingStatus)) {
            sql.append("', pven_fdoc_nbr = '" + kfsVendorDocumentNumber);
        }
        sql.append("' where pmw_vnd_req_id = '" + pmwVendorRequestId + "'");
        
        return sql.toString();
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}
