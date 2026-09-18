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
                        // Range of proposal numbers for medium sized data set
                        + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
                        // Single value for specific data item troubleshooting
//                          + " WHERE CGPRPSL_NBR IN ('136154'))";
                        // Smaller specific set of proposal numbers for targeted local troubleshooting
//                        + " WHERE CGPRPSL_NBR IN ('15366', '193325', '193412', '193472', '37608', '39769', '40108',"
//                        + " '40914', '42734', '43224', '44647', '45865', '45971', '47057', '47824', '48719', '49294',"
//                        + " '49403', '52390', '53234', '53400', '53731', '54482', '55306', '57220', '60403', '61882',"
//                        + " '62119', '62293'))";
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
