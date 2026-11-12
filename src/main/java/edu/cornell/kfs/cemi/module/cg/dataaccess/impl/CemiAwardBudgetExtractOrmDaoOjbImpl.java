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
        final String proposalNumberCondition;

        if (shouldUseLessDataDuringCemiDevelopment()) {
            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                    + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_BUDG_EXTR_AWD_T"
                    + " WHERE CGPRPSL_NBR IN ('136110', '136135', '138545', '178227', '178160', '179424'))";
        } else {
            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                    + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_BUDG_EXTR_AWD_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(proposalNumberCondition);

        final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Award.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
