package edu.cornell.kfs.sys.batch.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;
import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public class TablesPurgeServiceImpl implements TablesPurgeService {
    private static final Logger LOG = LogManager.getLogger(TablesPurgeServiceImpl.class); 
    protected TablePurgeRecordsDao tablePurgeRecordsDao;
    protected ArrayList<TableDetailsForPurge> tablesDetailsForPurge;
    
    public void purgeRecords(LocalDateTime jobRunLocalDateTime) {
        getTablePurgeRecordsDao().purgeRecords(jobRunLocalDateTime, tablesDetailsForPurge);
    }

    public TablePurgeRecordsDao getTablePurgeRecordsDao() {
        return tablePurgeRecordsDao;
    }

    public void setTablePurgeRecordsDao(TablePurgeRecordsDao tablePurgeRecordsDao) {
        this.tablePurgeRecordsDao = tablePurgeRecordsDao;
    }

    public ArrayList<TableDetailsForPurge> getTablesDetailsForPurge() {
        return tablesDetailsForPurge;
    }

    public void setTablesDetailsForPurge(ArrayList<TableDetailsForPurge> tablesDetailsForPurge) {
        this.tablesDetailsForPurge = tablesDetailsForPurge;
    }
    
}
