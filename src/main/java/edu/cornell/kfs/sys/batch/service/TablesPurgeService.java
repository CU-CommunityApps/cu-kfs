package edu.cornell.kfs.sys.batch.service;

import java.util.Date;

public interface TablesPurgeService {
    
    void purgeRecords(Date jobRunDate);
    
}
