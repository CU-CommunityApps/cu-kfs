package edu.cornell.kfs.sys.dataaccess;

import java.util.Date;
import java.util.List;

import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;

public interface TablePurgeRecordsDao {
    
    void purgeRecords(Date jobRunDate, List<TableDetailsForPurge> tableDetails);
    
}
