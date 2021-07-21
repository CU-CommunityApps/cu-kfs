package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

public class PaymentWorksVendorLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl {

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addLessOrEqualThan("PROC_TS", dateForPurge);
        return lookupCriteria;
    }

}
