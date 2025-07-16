package edu.cornell.kfs.sys.dataaccess;

import java.time.LocalDateTime;
import java.util.List;

import edu.cornell.kfs.sys.businessobject.HistoricalTableDetailsForPurge;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public interface HistoricalTablePurgeRecordsDao {
    
    void purgeRecords(LocalDateTime jobRunDate, List<HistoricalTableDetailsForPurge> tableDetails);
    
}
