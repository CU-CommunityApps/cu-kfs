package edu.cornell.kfs.sys.batch.service;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

public interface TableLookupCriteriaPurgeService {

    Criteria buildLookupCriteria(Date dateForPurge);

}
