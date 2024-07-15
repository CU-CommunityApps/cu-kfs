package edu.cornell.kfs.sys.batch.service;

import java.util.Date;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public interface HistoricalTablesPurgeService {
    
    void purgeRecords(Date jobRunDate);
    
}
