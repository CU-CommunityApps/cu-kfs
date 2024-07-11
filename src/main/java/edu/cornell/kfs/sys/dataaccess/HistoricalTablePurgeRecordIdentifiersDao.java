package edu.cornell.kfs.sys.dataaccess;

import java.util.Date;
import java.util.List;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public interface HistoricalTablePurgeRecordIdentifiersDao {
    
    List<String> obtainInitiatedDocumentIdsToPurge(Date dateForPurge, int databaseRowFetchCount);
    
}
