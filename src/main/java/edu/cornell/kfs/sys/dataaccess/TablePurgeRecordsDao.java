package edu.cornell.kfs.sys.dataaccess;

import java.time.LocalDate;
import java.util.List;

import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;

public interface TablePurgeRecordsDao {
    
    void purgeRecords(LocalDate jobRunLocalDate, List<TableDetailsForPurge> tableDetails);
    
}
