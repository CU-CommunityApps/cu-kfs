package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiImportBudgetExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase
        implements CemiImportBudgetExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiImportBudgetExtractAsCloseableStream() {
        //final String proposalNumberCondition;

        //populate proposal number condition with the list of proposal numbers that are in scope for the extract

        final Criteria criteria = new Criteria();
        //criteria.addSql(proposalNumberCondition);

        final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        //query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Award.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
