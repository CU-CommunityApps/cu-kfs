package edu.cornell.kfs.cemi.module.cam.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractOrmDao;
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

public class CemiRegisterAssetExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiRegisterAssetExtractOrmDao {

    @Override
    public Stream<Asset> getAssetsForCemiRegisterAssetExtractAsCloseableStream() {
        final String assetNumberCondition;
        
      //Local environment configuration property setting used by base class method call 
      //to reduce processing time for local development during CEMI project work.
        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
            // well as provide both old and new awards that had a variety of attributes for local verification. 
            assetNumberCondition = "(A0.CPTLAST_NBR) IN ("
                    + "SELECT CPTLAST_NBR FROM CEMI.CU_CEMI_REGISTER_ASST_EXTR_ASST_T"
                    + " WHERE CPTLAST_NBR <= 3000)";
        } else {
            assetNumberCondition = "(A0.CPTLAST_NBR) IN ("
                    + "SELECT CPTLAST_NBR FROM KFS.CU_CEMI_REGISTER_ASST_EXTR_ASST_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(assetNumberCondition);
        //
        //
        final QueryByCriteria query = new QueryByCriteria(Asset.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.CAPITAL_ASSET_NUMBER);
        //
        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Asset.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
