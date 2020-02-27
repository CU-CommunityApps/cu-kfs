package edu.cornell.kfs.sys.batch.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.sys.batch.service.TablesPurgeService;
import edu.cornell.kfs.sys.businessobject.TableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.TablePurgeRecordsDao;

public class TablesPurgeServiceImpl implements TablesPurgeService {
    private static final Logger LOG = LogManager.getLogger(TablesPurgeServiceImpl.class); 
    protected TablePurgeRecordsDao tablePurgeRecordsDao;
    protected List<TableDetailsForPurge> tablesDetailsForPurge;
    
    public void purgeRecords(Date jobRunDate) {
        initializaDetails();
        getTablePurgeRecordsDao().purgeRecords(jobRunDate, tablesDetailsForPurge);
    }
    
    private void initializaDetails() {
        tablesDetailsForPurge = new ArrayList<TableDetailsForPurge>();
        
        TableDetailsForPurge tableDetails = new TableDetailsForPurge();
        tableDetails.setBusinessObjectForRecordsTablePurge(ConcurEventNotification.class);
        tableDetails.setUseDefaultDaysBeforePurgeParameter(true);
        tableDetails.setServiceImplForPurgeTableLookupCriteria("edu.cornell.kfs.sys.batch.service.impl.ConcurEventNotificationLookupCriteriaPurgeServiceImpl");
        
        tablesDetailsForPurge.add(tableDetails);
    }

    public TablePurgeRecordsDao getTablePurgeRecordsDao() {
        return tablePurgeRecordsDao;
    }

    public void setTablePurgeRecordsDao(TablePurgeRecordsDao tablePurgeRecordsDao) {
        this.tablePurgeRecordsDao = tablePurgeRecordsDao;
    }

    public List<TableDetailsForPurge> getTablesDetailsForPurge() {
        return tablesDetailsForPurge;
    }

    public void setTablesDetailsForPurge(List<TableDetailsForPurge> tablesDetailsForPurge) {
        this.tablesDetailsForPurge = tablesDetailsForPurge;
    }
    
}
