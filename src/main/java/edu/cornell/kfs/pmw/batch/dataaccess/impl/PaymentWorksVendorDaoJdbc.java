package edu.cornell.kfs.pmw.batch.dataaccess.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import org.kuali.kfs.sys.KFSConstants;
import edu.cornell.kfs.pmw.batch.dataaccess.PaymentWorksVendorDao;
import org.kuali.kfs.krad.util.ObjectUtils;

public class PaymentWorksVendorDaoJdbc extends PlatformAwareDaoBaseJdbc implements PaymentWorksVendorDao{
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksVendorDaoJdbc.class);
    
    protected static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    protected static final String UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_SQL = "UPDATE KFS.CU_PMW_VENDOR_T SET PMW_REQ_STAT = ?, KFS_VND_PROC_STAT = ?, PROC_TS = ? WHERE PMW_VND_REQ_ID = ?";
    protected static final String UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_ACH_STATUS_SQL = "UPDATE KFS.CU_PMW_VENDOR_T SET PMW_REQ_STAT = ?, KFS_VND_PROC_STAT = ?, PROC_TS = ?, KFS_ACH_PROC_STAT = ? WHERE PMW_VND_REQ_ID = ?";
    protected static final String UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_KFS_EDOC_SQL = "UPDATE KFS.CU_PMW_VENDOR_T SET PMW_REQ_STAT = ?, KFS_VND_PROC_STAT = ?, PROC_TS = ?, PVEN_FDOC_NBR = ? WHERE PMW_VND_REQ_ID = ?";
    protected static final String UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_ACH_STATUS_KFS_EDOC_SQL = "UPDATE KFS.CU_PMW_VENDOR_T SET PMW_REQ_STAT = ?, KFS_VND_PROC_STAT = ?, PROC_TS = ?, KFS_ACH_PROC_STAT = ?, PVEN_FDOC_NBR = ? WHERE PMW_VND_REQ_ID = ?";
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp) {
        int rowsUpdated = updatePaymentWorksVendorStatusInStagingTable(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, processingTimeStamp);
        LOG.info("updateExistingPaymentWorksVendorInStagingTable: ");
        return;
    }
    
    @Override
    public void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp  processingTimeStamp) {
        int rowsUpdated = updatePaymentWorksVendorStatusInStagingTable(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, processingTimeStamp);
        return;
    }

    protected int updatePaymentWorksVendorStatusInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp  processingTimeStamp) {
        String timestampInSqlFormat = PROCESSING_TIMESTAMP_SQL_FORMATTER.format(processingTimeStamp);
        ArrayList<String> sqlParameters = new ArrayList<String>();
        int rowsUpdated = 0;

        if (isUpdateAllParameters(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, timestampInSqlFormat)) {
            sqlParameters.add(pmwRequestStatus);
            sqlParameters.add(kfsVendorProcessingStatus);
            sqlParameters.add(timestampInSqlFormat);
            sqlParameters.add(kfsAchProcessingStatus);
            sqlParameters.add(kfsVendorDocumentNumber);
            sqlParameters.add(pmwVendorRequestId);
            rowsUpdated = performUpdate(UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_ACH_STATUS_KFS_EDOC_SQL, sqlParameters);
        }
        else if (isUpdatePmwStatusKfsStatusKfsEdocParameters(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, timestampInSqlFormat)) {
            sqlParameters.add(pmwRequestStatus);
            sqlParameters.add(kfsVendorProcessingStatus);
            sqlParameters.add(timestampInSqlFormat);
            sqlParameters.add(kfsVendorDocumentNumber);
            sqlParameters.add(pmwVendorRequestId);
            rowsUpdated = performUpdate(UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_KFS_EDOC_SQL, sqlParameters);
        }
        else if (isUpdatePmwStatusKfsStatusKfsAchStatusParameters(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, timestampInSqlFormat)) {
            sqlParameters.add(pmwRequestStatus);
            sqlParameters.add(kfsVendorProcessingStatus);
            sqlParameters.add(timestampInSqlFormat);
            sqlParameters.add(kfsAchProcessingStatus);
            sqlParameters.add(pmwVendorRequestId);
            rowsUpdated = performUpdate(UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_ACH_STATUS_SQL, sqlParameters);
        }
        else if (isUpdatePmwStatusKfsStatusParameters(pmwVendorRequestId, pmwRequestStatus, kfsVendorProcessingStatus, kfsAchProcessingStatus, kfsVendorDocumentNumber, timestampInSqlFormat)) {
            sqlParameters.add(pmwRequestStatus);
            sqlParameters.add(kfsVendorProcessingStatus);
            sqlParameters.add(timestampInSqlFormat);
            sqlParameters.add(kfsAchProcessingStatus);
            sqlParameters.add(pmwVendorRequestId);
            rowsUpdated = performUpdate(UPDATE_PMW_KFS_VENDOR_STATUS_TIMESTAMP_SQL, sqlParameters);
        }
        return rowsUpdated;
    }
    
    private boolean isUpdateAllParameters(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String timestampInSqlFormat) {
        if (StringUtils.isNotBlank(pmwVendorRequestId) && StringUtils.isNotBlank(pmwRequestStatus) && StringUtils.isNotBlank(kfsVendorProcessingStatus) &&
            StringUtils.isNotBlank(kfsAchProcessingStatus) && StringUtils.isNotBlank(kfsVendorDocumentNumber) && StringUtils.isNotBlank(timestampInSqlFormat)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean isUpdatePmwStatusKfsStatusKfsEdocParameters(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String timestampInSqlFormat) {
        if (StringUtils.isNotBlank(pmwVendorRequestId) && StringUtils.isNotBlank(pmwRequestStatus) && StringUtils.isNotBlank(kfsVendorProcessingStatus) &&
            StringUtils.isBlank(kfsAchProcessingStatus) && StringUtils.isNotBlank(kfsVendorDocumentNumber) && StringUtils.isNotBlank(timestampInSqlFormat)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean isUpdatePmwStatusKfsStatusKfsAchStatusParameters(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String timestampInSqlFormat) {
        if (StringUtils.isNotBlank(pmwVendorRequestId) && StringUtils.isNotBlank(pmwRequestStatus) && StringUtils.isNotBlank(kfsVendorProcessingStatus) &&
            StringUtils.isNotBlank(kfsAchProcessingStatus) && StringUtils.isBlank(kfsVendorDocumentNumber) && StringUtils.isNotBlank(timestampInSqlFormat)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean isUpdatePmwStatusKfsStatusParameters(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String timestampInSqlFormat) {
        if (StringUtils.isNotBlank(pmwVendorRequestId) && StringUtils.isNotBlank(pmwRequestStatus) && StringUtils.isNotBlank(kfsVendorProcessingStatus) &&
            StringUtils.isBlank(kfsAchProcessingStatus) && StringUtils.isBlank(kfsVendorDocumentNumber) && StringUtils.isNotBlank(timestampInSqlFormat)) {
            return true;
        }
        else {
            return false;
        }
    }

    private int performUpdate (String updateSql, List<String> sqlParameters) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int result = 0;
        int parameterIndex = 0;
        try {
            connection = getJdbcTemplate().getDataSource().getConnection();
            preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.clearParameters();
            while (parameterIndex < sqlParameters.size()) {
                preparedStatement.setString(parameterIndex, sqlParameters.get(parameterIndex));
                parameterIndex++;
            }
            result = preparedStatement.executeUpdate();
        } catch (SQLException se) {
            LOG.error("performUpdate: Could not update new vendor processing status due to SQLException: " + se.toString());
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException ps) {
                    LOG.error("performUpdate: Could not close the prepared statement for updateSql due to SQLException: " + ps.toString());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ce) {
                    LOG.error("performUpdate: Could not close the connection for updateSql due to SQLException: " + ce.toString());
                }
            }
        }

        return result;
    }

    private String convertTimestampToSqlDateString(Timestamp timestampToConvert) {
        return PROCESSING_TIMESTAMP_SQL_FORMATTER.format(timestampToConvert);
    }

}
