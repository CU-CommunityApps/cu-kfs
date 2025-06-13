package edu.cornell.kfs.sys.batch.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;

import edu.cornell.kfs.sys.batch.service.HistoricalTablesPurgeService;
import edu.cornell.kfs.sys.businessobject.HistoricalTableDetailsForPurge;
import edu.cornell.kfs.sys.dataaccess.HistoricalTablePurgeRecordsDao;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalTablesPurgeServiceImpl implements HistoricalTablesPurgeService {
    protected HistoricalTablePurgeRecordsDao historicalTablePurgeRecordsDao;
    protected ArrayList<HistoricalTableDetailsForPurge> historicalTablesDetailsForPurge;
    
    public void purgeRecords(LocalDateTime jobRunDate) {
        getHistoricalTablePurgeRecordsDao().purgeRecords(jobRunDate, historicalTablesDetailsForPurge);
    }

    public HistoricalTablePurgeRecordsDao getHistoricalTablePurgeRecordsDao() {
        return historicalTablePurgeRecordsDao;
    }

    public void setHistoricalTablePurgeRecordsDao(HistoricalTablePurgeRecordsDao historicalTablePurgeRecordsDao) {
        this.historicalTablePurgeRecordsDao = historicalTablePurgeRecordsDao;
    }

    public ArrayList<HistoricalTableDetailsForPurge> getHistoricalTablesDetailsForPurge() {
        return historicalTablesDetailsForPurge;
    }

    public void setHistoricalTablesDetailsForPurge(ArrayList<HistoricalTableDetailsForPurge> historicalTablesDetailsForPurge) {
        this.historicalTablesDetailsForPurge = historicalTablesDetailsForPurge;
    }
    
}
