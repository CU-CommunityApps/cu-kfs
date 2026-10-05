package edu.cornell.kfs.cemi.pdp.dataaccess.impl;

import java.util.stream.Stream;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiPaymentElectionOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiPaymentElectionOrmDao {

    @Override
    public Stream<PayeeACHAccount> getPayeeAchAccountsForCemiPaymentElectionExtractAsCloseableStream() {
        final String payeeAchAccountIdsCondition;

        // Local environment configuration property setting used by base class method call
        // to reduce processing time for local development during CEMI project work.
        if (shouldUseLessDataDuringCemiDevelopment()) {
            // This conditional was added to reduce processing time for local development during CEMI project work.
            // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
            // well as provide both old and new data that had a variety of attributes for local verification.
            payeeAchAccountIdsCondition = "(A0.ACH_ACCT_GNRTD_ID) IN ("
                    + "SELECT ACH_ACCT_GNRTD_ID FROM CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T"
                    + " WHERE ACH_ACCT_GNRTD_ID <= 10003683 OR ACH_ACCT_GNRTD_ID >= 10205000)";
        } else {
            payeeAchAccountIdsCondition = "(A0.ACH_ACCT_GNRTD_ID) IN ("
                    + "SELECT ACH_ACCT_GNRTD_ID FROM CEMI.CU_CEMI_PYMNT_ELCTN_EXTR_ACH_ACCT_T)";
        }
        final Criteria criteria = new Criteria();
        criteria.addSql(payeeAchAccountIdsCondition);

        final QueryByCriteria query = new QueryByCriteria(PayeeACHAccount.class, criteria);
        query.addOrderByAscending(PdpPropertyConstants.ACH_ACCOUNT_GENERATED_IDENTIFIER);

        return CuOjbUtils.buildCloseableStreamForQueryResults(
                PayeeACHAccount.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }

}
