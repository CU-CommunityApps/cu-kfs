package edu.cornell.kfs.sys.dataaccess.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.HistoricalTablePurgeRecordIdentifiersDao;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalTablePurgeRecordIdentifiersDaoJdbc extends PlatformAwareDaoBaseJdbc implements HistoricalTablePurgeRecordIdentifiersDao {
        private static final Logger LOG = LogManager.getLogger();
        private static final SimpleDateFormat CREATE_DATE_SQL_FORMATTER = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_dd_MMM_yy, Locale.US);
    
    @Override
    public List<String> obtainInitiatedDocumentIdsToPurge(Date dateForPurge, int databaseRowFetchCount) {
        String sqlRestrictedByFetchCount = buildQuery(dateForPurge, databaseRowFetchCount);
        LOG.info("obtainInitiatedDocumentIdsToPurge: SQL generated for execution: {}", sqlRestrictedByFetchCount);
        
        List<String> docIds = getJdbcTemplate().queryForList(sqlRestrictedByFetchCount, String.class);
        LOG.info("obtainInitiatedDocumentIdsToPurge: JDBC query found {} historical docIds to purge.", docIds.size());
        return docIds;
    }
    
    private String buildQuery(Date dateForPurge, int databaseRowFetchCount) {
        StringBuilder sqlRestrictedByFetchCount = new StringBuilder();
        sqlRestrictedByFetchCount.append("SELECT DOC_HDR_ID FROM KFS.KREW_DOC_HDR_T WHERE DOC_HDR_STAT_CD like 'I' ");
        sqlRestrictedByFetchCount.append("AND CRTE_DT < '" + CREATE_DATE_SQL_FORMATTER.format(dateForPurge) + "' ");
        sqlRestrictedByFetchCount.append("ORDER BY CRTE_DT ASC FETCH FIRST " + databaseRowFetchCount + " ROWS ONLY");
        return sqlRestrictedByFetchCount.toString();
    }
    
}
