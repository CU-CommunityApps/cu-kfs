package edu.cornell.kfs.sys.dataaccess;

import java.time.LocalDateTime;
import java.util.List;

import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;

public interface TablePurgeRecordsDao {
    
    void purgeRecords(LocalDateTime jobRunLocalDateTime, List<TableDetailsForPurge> tableDetails);
    
}
