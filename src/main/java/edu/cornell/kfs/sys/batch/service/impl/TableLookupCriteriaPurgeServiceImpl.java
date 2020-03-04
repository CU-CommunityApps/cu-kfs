package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;

public abstract class TableLookupCriteriaPurgeServiceImpl implements TableLookupCriteriaPurgeService {

    public abstract Criteria buildLookupCriteria(Date dateForPurge);

}
