package edu.cornell.kfs.sys.batch.service;

import java.time.LocalDate;

public interface TablesPurgeService {
    
    void purgeRecords(LocalDate jobRunLocalDate);
    
}
