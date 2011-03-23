package edu.cornell.kfs.module.ezra.dataaccess.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.cam.util.KualiDecimalUtils;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;

public class EzraAwardProposalDaoOjb extends PlatformAwareDaoBaseOjb implements EzraAwardProposalDao {

	public List<EzraProposalAward> getProposals() {
		Criteria criteria = new Criteria();
		criteria.addLike("awardProposalId", "A%");
		criteria.addGreaterThan("budgetAmt",KualiDecimal.ZERO);
		criteria.addEqualTo("status", "ASAP");

        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
	}
	
	public List<EzraProposalAward> getAwardsUpdatedSince(Date date) {
		Criteria criteria = new Criteria();
		criteria.addLike("awardProposalId", "A%");
		criteria.addGreaterThan("budgetAmt",KualiDecimal.ZERO);
		criteria.addEqualTo("status", "ASAP");
		
		if (date != null) {
			criteria.addLessThan("lastUpdated", date);
		}

        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
	}

}
