package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;


// Actual example of how the processing should be setup for this business object service class.
// Note: For local development ensure you have the KFS configuration setting in place to
//       restrict the amount of data returned; otherwise you will experience extremely slow execution
//       time. This service's abstract base class uses that configuration value in the function call below.
//          (1) Configuration value : cu.cemi.development.use.smaller.data.set=true
//          (2) Method name : shouldUseLessDataDuringCemiDevelopment
//
//       You will also need to determine the boundaries to include in the "if" portion of the query.
//
//       Abstract class CemiOrmDaoOjbImplBase contains attribute configurationService which REQUIRES
//       a Spring bean definition in every concrete class created.
//

public class CemiImportBudgetExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase
        implements CemiImportBudgetExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiImportBudgetExtractAsCloseableStream() {
        //final String proposalNumberCondition;

        //populate proposal number condition with the list of proposal numbers that are in scope for the extract
        // TODO restore scope filter, local env data restriction, using shouldUseLessDataDuringCemiDevelopment()

        //final Criteria criteria = new Criteria();
        //criteria.addSql(proposalNumberCondition);

        //final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        //query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);

        //return CuOjbUtils.buildCloseableStreamForQueryResults(
        //        Award.class,
        //        () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));

        return Stream.empty();
    }

}
