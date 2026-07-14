package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiAwardScheduleExtractOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiAwardScheduleExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiAwardScheduleExtractAsCloseableStream() {
        final String proposalNumberCondition;

        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
            // well as provide both old and new awards that had a variety of attributes for local verification. 
            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                    + "SELECT CGPRPSL_NBR FROM KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T"
                    + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
        } else {
            proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                    + "SELECT CGPRPSL_NBR FROM KFS.CU_CEMI_AWD_SCHDL_EXTR_AWD_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(proposalNumberCondition);

        /*
         * NOTE: The sort order below is crucial to simplify processing the Vendors in a streaming manner.
         * When iterating over the Vendors below, a parent Vendor will be immediately followed
         * by its children BEFORE the next parent Vendor is encountered. That way, when the processing code,
         * iterates over the data but needs to populate child Vendor data based on what's in its parent,
         * only a single parent Vendor needs its reference kept short-term.
         */
        final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Award.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

    // This was added to reduce processing time for local development during CEMI project work.
    private static boolean shouldUseLessDataDuringCemiDevelopment() {
        return getBooleanProperty(CemiBaseConstants.CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY);
    }

    // This was added to reduce processing time for local development during CEMI project work.
    private static boolean getBooleanProperty(String propertyName) {
        return KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsBoolean(propertyName);
    }
    
}
