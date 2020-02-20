package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Date;

import edu.cornell.kfs.concur.batch.service.ConcurTablesPurgeService;
import edu.cornell.kfs.concur.dataaccess.ConcurEventNotificationDao;
import edu.cornell.kfs.sys.batch.service.impl.TablesPurgeServiceImpl;

public class ConcurTablesPurgeServiceImpl extends TablesPurgeServiceImpl implements ConcurTablesPurgeService{
    
    protected ConcurEventNotificationDao concurEventNotificationDao;
    
    public void purgeRecords(Date jobRunDate) {
        getConcurEventNotificationDao().purgeRecords(jobRunDate, retrieveDefaultDaysBeforePurgeParameterValue());
    }

    public ConcurEventNotificationDao getConcurEventNotificationDao() {
        return concurEventNotificationDao;
    }

    public void setConcurEventNotificationDao(ConcurEventNotificationDao concurEventNotificationDao) {
        this.concurEventNotificationDao = concurEventNotificationDao;
    }

}
