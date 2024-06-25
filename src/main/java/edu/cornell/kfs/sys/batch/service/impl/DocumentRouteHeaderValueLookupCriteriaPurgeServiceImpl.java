package edu.cornell.kfs.sys.batch.service.impl;

import java.sql.Date;

import org.apache.ojb.broker.query.Criteria;
import org.kuali.kfs.kew.api.KewApiConstants;

import edu.cornell.kfs.sys.batch.service.TableLookupCriteriaPurgeService;

public class DocumentRouteHeaderValueLookupCriteriaPurgeServiceImpl extends TableLookupCriteriaPurgeServiceImpl
        implements TableLookupCriteriaPurgeService {

    private static final String DOCUMENT_ROUTE_STATUS = "docRouteStatus";
    private static final String CREATE_DATE = "createDate";

    @Override
    public Criteria buildLookupCriteria(Date dateForPurge) {
        Criteria lookupCriteria = new Criteria();
        lookupCriteria.addLike(DOCUMENT_ROUTE_STATUS, KewApiConstants.ROUTE_HEADER_INITIATED_CD);
        lookupCriteria.addLessOrEqualThan(CREATE_DATE, dateForPurge);
        return lookupCriteria;
    }
}
