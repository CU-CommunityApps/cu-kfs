package edu.cornell.kfs.sys.batch.service.impl;

import org.apache.ojb.broker.query.Criteria;

import edu.cornell.kfs.sys.batch.service.HistoricalTableLookupCriteriaPurgeService;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public abstract class HistoricalTableLookupCriteriaPurgeServiceImpl implements HistoricalTableLookupCriteriaPurgeService {

    public abstract Criteria buildLookupCriteria(String documentIdToPurge);

}
