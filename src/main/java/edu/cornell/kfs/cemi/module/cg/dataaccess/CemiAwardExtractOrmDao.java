package edu.cornell.kfs.cemi.module.cg.dataaccess;

import java.util.List;
import java.util.stream.Stream;

import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;

public interface CemiAwardExtractOrmDao {

    Stream<Award> getAwardsForCemiAwardExtractAsCloseableStream();
    
    List<CemiAwardLegacyNovelutionBo> getAwardNovelutionAtributesForCemiAwardExtractAsCloseableStream(String proposalNumber);
    
//    Stream<CemiAwardRawAwardHeaderFieldsRowBo> getRawAwardForCemiAwardExtractAsCloseableStream();
//    @Override
//    public Stream<CemiAwardRawAwardHeaderFieldsRowBo> getRawAwardForCemiAwardExtractAsCloseableStream() {
//        final String proposalNumberCondition;
//        
//        //Local environment configuration property setting used by base class method call 
//        //to reduce processing time for local development during CEMI project work.
//        if (shouldUseLessDataDuringCemiDevelopment()) {
//            // This conditional was added to reduce processing time for local development during CEMI project work.
//            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
//            // well as provide both old and new awards that had a variety of attributes for local verification. 
//            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
//                    + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_INTRM_AWD_HDR_RAW_FLDS_T"
//                    + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
//        } else {
//            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
//                    + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_INTRM_AWD_HDR_RAW_FLDS_T)";
//        }
//        final Criteria criteria = new Criteria();
//        criteria.addSql(proposalNumberCondition);
//
//        final QueryByCriteria query = new QueryByCriteria(CemiAwardRawAwardHeaderFieldsRowBo.class, criteria);
//        query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);
//
//        return CuOjbUtils.buildCloseableStreamForQueryResults(
//                CemiAwardRawAwardHeaderFieldsRowBo.class,
//                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
//    }

}
