package edu.cornell.kfs.module.cg.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.module.cg.dataaccess.AwardAccountDao;

public class AwardAccountDaoOjb extends PlatformAwareDaoBaseOjb implements AwardAccountDao {

	/**
	 * @see edu.cornell.kfs.module.cg.dataaccess.AwardAccountDao#isAccountUsedOnAnotherAward(java.lang.String,
	 *      java.lang.String, java.lang.Long)
	 */
	@Override
	public boolean isAccountUsedOnAnotherAward(String chart, String account,Long proposalNumber) {
		Criteria criteria = new Criteria();

		criteria.addNotEqualTo(KFSPropertyConstants.PROPOSAL_NUMBER,proposalNumber);
		criteria.addEqualTo(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart);
		criteria.addEqualTo(KFSPropertyConstants.ACCOUNT_NUMBER, account);
		criteria.addEqualTo(KFSPropertyConstants.ACTIVE, Boolean.TRUE);

		QueryByCriteria query = QueryFactory.newQuery(AwardAccount.class,criteria);

		int numOfRows = getPersistenceBrokerTemplate().getCount(query);

		return numOfRows > 0;
	}

}
