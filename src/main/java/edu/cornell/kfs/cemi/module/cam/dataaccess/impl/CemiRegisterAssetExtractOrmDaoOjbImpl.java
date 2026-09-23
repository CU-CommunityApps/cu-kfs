package edu.cornell.kfs.cemi.module.cam.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cam.dataaccess.CemiRegisterAssetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

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
                    + " WHERE TO_NUMBER(CPTLAST_NBR DEFAULT NULL ON CONVERSION ERROR) <= 1000"
                    + " OR TO_NUMBER(CPTLAST_NBR DEFAULT NULL ON CONVERSION ERROR) >= 523000)";
        } else {
            assetNumberCondition = "(A0.CPTLAST_NBR) IN ("
                    + "SELECT CPTLAST_NBR FROM CEMI.CU_CEMI_REGISTER_ASST_EXTR_ASST_T)";
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
