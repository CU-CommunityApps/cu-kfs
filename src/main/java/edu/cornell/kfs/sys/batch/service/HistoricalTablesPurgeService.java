package edu.cornell.kfs.sys.batch.service;

import java.time.LocalDateTime;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public interface HistoricalTablesPurgeService {
    
    void purgeRecords(LocalDateTime jobRunDate);
    
}
