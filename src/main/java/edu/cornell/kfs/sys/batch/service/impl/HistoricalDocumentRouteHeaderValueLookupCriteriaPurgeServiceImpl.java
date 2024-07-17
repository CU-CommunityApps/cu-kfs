package edu.cornell.kfs.sys.batch.service.impl;

import org.apache.ojb.broker.query.Criteria;
import org.kuali.kfs.kew.api.KewApiConstants;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalDocumentRouteHeaderValueLookupCriteriaPurgeServiceImpl 
    extends HistoricalTableLookupCriteriaPurgeServiceImpl {

    private static final String DOCUMENT_ROUTE_STATUS = "docRouteStatus";

    @Override
    public Criteria buildLookupCriteria(String documentIdToPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addLike(CUKFSPropertyConstants.DOCUMENT_ID, documentIdToPurge);
        lookupCriteria.addLike(DOCUMENT_ROUTE_STATUS, KewApiConstants.ROUTE_HEADER_INITIATED_CD);
        return lookupCriteria;
    }
}
