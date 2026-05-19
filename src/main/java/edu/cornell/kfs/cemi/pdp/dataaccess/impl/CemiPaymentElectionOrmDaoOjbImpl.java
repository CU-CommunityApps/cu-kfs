package edu.cornell.kfs.cemi.pdp.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionPropertyConstants;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiPaymentElectionOrmDaoOjbImpl extends PlatformAwareDaoBaseOjb implements CemiPaymentElectionOrmDao {

    @Override
    public Stream<PayeeACHAccount> getPayeeAchAccountIdsForCemiPaymentElectionExtractAsCloseableStream() {
        final String payeeAchAcountIdsCondition;

        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
            // well as provide both old and new data that had a variety of attributes for local verification. 
            payeeAchAcountIdsCondition = "(A0.ACH_ACCT_GNRTD_ID) IN ("
                    + "SELECT ACH_ACCT_GNRTD_ID FROM KFS.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T"
                    + " WHERE ACH_ACCT_GNRTD_ID <= 10003683 OR ACH_ACCT_GNRTD_ID >= 10205000)";
        } else {
            payeeAchAcountIdsCondition = "(A0.ACH_ACCT_GNRTD_ID) IN ("
                    + "SELECT ACH_ACCT_GNRTD_ID FROM KFS.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(payeeAchAcountIdsCondition);

        final QueryByCriteria query = new QueryByCriteria(PayeeACHAccount.class, criteria);
        query.addOrderByAscending(CemiPaymentElectionPropertyConstants.ACH_ACCOUNT_GENERATED_IDENTIFIER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                PayeeACHAccount.class,
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
