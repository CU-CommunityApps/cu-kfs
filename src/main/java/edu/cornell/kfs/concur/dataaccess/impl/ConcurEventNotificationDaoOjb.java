package edu.cornell.kfs.concur.dataaccess.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.concur.businessobjects.ConcurEventNotification;
import edu.cornell.kfs.concur.dataaccess.ConcurEventNotificationDao;
import edu.cornell.kfs.sys.dataaccess.impl.TablePurgeRecordsDaoOjb;

public class ConcurEventNotificationDaoOjb extends TablePurgeRecordsDaoOjb implements ConcurEventNotificationDao {
    
    public void purgeRecords(java.util.Date jobRunDate, int daysOld) {
        Date purgeDate = super.getPurgeDate(jobRunDate, daysOld);
        Criteria lookupCriteria = buildLookupCriteria(purgeDate);
        identifyAndRequestRecordsDeletion(ConcurEventNotification.class, lookupCriteria);
    }
    
    protected Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addEqualTo("processed", "Y");
        lookupCriteria.addLessOrEqualThan("eventDateTime", dateForPurge);
        return lookupCriteria;
    }

}
