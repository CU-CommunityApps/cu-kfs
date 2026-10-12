package edu.cornell.kfs.cemi.module.purap.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderDocumentLite;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;


public class CemiPurchaseOrderExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiPurchaseOrderExtractOrmDao {

    @Override
    public Stream<CemiPurchaseOrderDocumentLite> getPurchaseOrdersToExtractAsCloseableStream() {
        final Criteria criteria = new Criteria();

        String optionalDocumentNumberRangeCondition = CemiBaseConstants.EMPTY_STRING;
        if (shouldUseLessDataDuringCemiDevelopment()) {
            optionalDocumentNumberRangeCondition = " WHERE FDOC_NBR  <= '20000000' OR FDOC_NBR >= '64471000'"
                    + " FETCH FIRST 400 ROWS ONLY";
        }

        final String documentNumberCondition = "A0.FDOC_NBR IN ("
                + "SELECT FDOC_NBR FROM CEMI.CU_CEMI_EXTR_PURCHASE_ORDER_IN_SCOPE_PO_DOCS_T"
                + optionalDocumentNumberRangeCondition
                + ")";
        criteria.addSql(documentNumberCondition);

        final QueryByCriteria query = new QueryByCriteria(CemiPurchaseOrderDocumentLite.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.DOCUMENT_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                CemiPurchaseOrderDocumentLite.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
