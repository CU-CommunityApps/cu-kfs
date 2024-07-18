package edu.cornell.kfs.sys.batch.service;

import org.apache.ojb.broker.query.Criteria;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public interface HistoricalTableLookupCriteriaPurgeService {

    Criteria buildLookupCriteria(String documentIdToPurge);

}
