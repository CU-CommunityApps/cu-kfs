package edu.cornell.kfs.sys.batch.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;

import edu.cornell.kfs.sys.batch.service.TablesPurgeService;
import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public class TablesPurgeServiceImpl implements TablesPurgeService {
    protected TablePurgeRecordsDao tablePurgeRecordsDao;
    protected ArrayList<TableDetailsForPurge> tablesDetailsForPurge;
    
    public void purgeRecords(LocalDate jobRunLocalDate) {
        getTablePurgeRecordsDao().purgeRecords(jobRunLocalDate, tablesDetailsForPurge);
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
