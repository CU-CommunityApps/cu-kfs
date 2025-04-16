package edu.cornell.kfs.sys.batch.service;

import java.time.LocalDateTime;

public interface TablesPurgeService {
    
    void purgeRecords(LocalDateTime jobRunLocalDateTime);
    
}
