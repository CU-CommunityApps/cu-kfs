package edu.cornell.kfs.cemi.module.purap.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderIdBo;
import edu.cornell.kfs.cemi.module.purap.dataaccess.CemiPurchaseOrderExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;


public class CemiPurchaseOrderExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiPurchaseOrderExtractOrmDao {

    @Override
    public Stream<CemiPurchaseOrderIdBo> getIdsOfPurchaseOrdersToExtractAsCloseableStream() {
        final Criteria criteria = new Criteria();
        if (shouldUseLessDataDuringCemiDevelopment()) {
            final Criteria lowValuesCondition = new Criteria();
            lowValuesCondition.addLessOrEqualThan(KFSPropertyConstants.DOCUMENT_NUMBER, "20000000");
            criteria.addOrCriteria(lowValuesCondition);

            final Criteria highValuesCondition = new Criteria();
            highValuesCondition.addGreaterOrEqualThan(KFSPropertyConstants.DOCUMENT_NUMBER, "64471000");
            criteria.addOrCriteria(highValuesCondition);
        }

        final QueryByCriteria query = new QueryByCriteria(CemiPurchaseOrderIdBo.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.DOCUMENT_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                CemiPurchaseOrderIdBo.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
