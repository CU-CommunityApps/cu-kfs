package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiAwardBudgetExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiAwardBudgetExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiAwardBudgetExtractAsCloseableStream() {
//TODO FIXME scope restriction to in-scope Awards will be added in the data scope iteration of this extract.
        final Criteria criteria = new Criteria();

        final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Award.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
