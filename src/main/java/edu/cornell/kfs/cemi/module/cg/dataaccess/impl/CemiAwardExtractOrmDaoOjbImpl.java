package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiAwardExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiAwardExtractOrmDao {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public Stream<Award> getAwardsForCemiAwardExtractAsCloseableStream() {
        final String proposalNumberCondition;
            
        // Local environment configuration property setting used by base class method call 
        // to reduce processing time for local development during CEMI project work.
            if (shouldUseLessDataDuringCemiDevelopment()) {
                // This conditional was added to reduce processing time for local development during CEMI project work.
                // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
                // well as provide both old and new legacy business objects that had a variety of attributes for local verification. 
                proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                        + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T"
                        + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
            } else {
                proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                        + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T)";
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
