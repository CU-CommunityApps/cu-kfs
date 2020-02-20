package edu.cornell.kfs.sys.dataaccess;

import java.util.Date;

public interface TablePurgeRecordsDao {
    
    void purgeRecords(Date jobRunDate, int daysOld);
    
}
