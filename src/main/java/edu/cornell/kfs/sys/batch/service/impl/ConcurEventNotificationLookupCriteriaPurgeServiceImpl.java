package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;

public class ConcurEventNotificationLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl implements TableLookupCriteriaPurgeService {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addEqualTo(ConcurPropertyConstants.ConcurEventNotification.PROCESSED, "Y");
        lookupCriteria.addLessOrEqualThan(ConcurPropertyConstants.ConcurEventNotification.EVENT_DATE_TIME, dateForPurge);
        return lookupCriteria;
    }

}
