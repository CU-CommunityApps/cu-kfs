package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;

public class ConcurEventNotificationLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl implements TableLookupCriteriaPurgeService {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addEqualTo("processed", "Y");
        lookupCriteria.addLessOrEqualThan("eventDateTime", dateForPurge);
        return lookupCriteria;
    }

}
