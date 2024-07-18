package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.cornell.kfs.sys.dataaccess.HistoricalTablePurgeRecordIdentifiersDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;
import edu.cornell.kfs.sys.util.CuSqlQueryPlatformAwareDaoBaseJdbc;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalTablePurgeRecordIdentifiersDaoJdbc 
    extends CuSqlQueryPlatformAwareDaoBaseJdbc implements HistoricalTablePurgeRecordIdentifiersDao {
    
    private static final Logger LOG = LogManager.getLogger();
    private DateTimeService dateTimeService;

    @Override
    public List<String> obtainInitiatedDocumentIdsToPurge(Date dateForPurge, int databaseRowFetchCount) {
        CuSqlChunk dateForPurgeSqlParameter = CuSqlChunk.forParameter(Types.DATE, dateTimeService.getUtilDateAtStartOfDay(dateForPurge));
        CuSqlChunk databaseRowFetchCountSqlParameter = CuSqlChunk.forParameter(Types.NUMERIC, databaseRowFetchCount);
        CuSqlQuery sqlQuery = buildInitatedDocumentsQuery(dateForPurgeSqlParameter, databaseRowFetchCountSqlParameter);
        logSQL(sqlQuery);
        List<String> docIds = queryForValues(sqlQuery, SingleColumnRowMapper.newInstance(String.class));
        LOG.info("obtainInitiatedDocumentIdsToPurge: JDBC query found {} historical docIds to purge.", docIds.size());
        return docIds;
    }

    private CuSqlQuery buildInitatedDocumentsQuery(CuSqlChunk dateForPurgeSqlParameter, CuSqlChunk databaseRowFetchCountSqlParameter) {
        return CuSqlQuery.of(
                "SELECT DOC_HDR_ID ",
                "FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_STAT_CD like 'I' ",
                "AND CRTE_DT < ", dateForPurgeSqlParameter, " ",
                "ORDER BY CRTE_DT ASC ",
                "FETCH FIRST ", databaseRowFetchCountSqlParameter, " ROWS ONLY");
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
