package edu.cornell.kfs.cemi.patterntemplate.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;

import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiExampleLEGACYOBJECT;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEExtractOrmDao;
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

public class CemiEXTRACTNAMEExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiEXTRACTNAMEExtractOrmDao {

    @Override
    public Stream<CemiExampleLEGACYOBJECT> getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream() {
        //final String proposalNumberCondition;
        
     // Local environment configuration property setting used by base class method call 
     // to reduce processing time for local development during CEMI project work.
        //if (shouldUseLessDataDuringCemiDevelopment()) {
        //    // This conditional was added to reduce processing time for local development during CEMI project work.
        //    // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
        //    // well as provide both old and new awards that had a variety of attributes for local verification. 
        //    proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
        //            + "SELECT CGPRPSL_NBR FROM KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T"
        //            + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
        //} else {
        //    proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
        //            + "SELECT CGPRPSL_NBR FROM KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T)";
        //}
        final Criteria criteria = new Criteria();
        //criteria.addSql(proposalNumberCondition);
        //
        //
        final QueryByCriteria query = new QueryByCriteria(CemiExampleLEGACYOBJECT.class, criteria);
        //query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);
        //
        return CuOjbUtils.buildCloseableStreamForQueryResults(
                CemiExampleLEGACYOBJECT.class,
                () -> super.getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
