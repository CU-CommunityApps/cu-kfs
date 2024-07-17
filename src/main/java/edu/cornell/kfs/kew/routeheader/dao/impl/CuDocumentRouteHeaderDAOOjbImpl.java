package edu.cornell.kfs.kew.routeheader.dao.impl;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.dao.impl.DocumentRouteHeaderDAOOjbImpl;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.kew.routeheader.dao.CuDocumentRouteHeaderDAO;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CuDocumentRouteHeaderDAOOjbImpl extends DocumentRouteHeaderDAOOjbImpl
        implements CuDocumentRouteHeaderDAO {

    @Override
    public Map<String, Timestamp> getFinalizedDatesForDocumentType(
            final String documentTypeName, 
            final Timestamp startDate,
            final Timestamp endDate) {
        final Criteria subCriteria = new Criteria();
        subCriteria.addEqualTo(KRADPropertyConstants.NAME, documentTypeName);
        final ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(DocumentType.class, subCriteria);
        subQuery.setAttributes(new String[] {KFSPropertyConstants.DOCUMENT_TYPE_ID});
        subQuery.setJdbcTypes(new int[] {Types.VARCHAR});
        
        final Criteria mainCriteria = new Criteria();
        mainCriteria.addIn(KFSPropertyConstants.DOCUMENT_TYPE_ID, subQuery);
        mainCriteria.addBetween(CUKFSPropertyConstants.FINALIZED_DATE, startDate, endDate);
        final ReportQueryByCriteria reportQuery = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, mainCriteria);
        reportQuery.setAttributes(
                new String[] {CUKFSPropertyConstants.DOCUMENT_ID, CUKFSPropertyConstants.FINALIZED_DATE});
        reportQuery.setJdbcTypes(new int[] {Types.VARCHAR, Types.TIMESTAMP});

        try (
            final Stream<Object[]> resultsStream = CuOjbUtils.buildCloseableStreamForReportQueryResults(
                    () -> getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(reportQuery))
        ) {
            return resultsStream.collect(
                    Collectors.toUnmodifiableMap(fieldArray -> (String) fieldArray[0], fieldArray -> (Timestamp) fieldArray[1]));
        }
    }

}
