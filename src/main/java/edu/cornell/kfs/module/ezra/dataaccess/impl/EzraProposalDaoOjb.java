package edu.cornell.kfs.module.ezra.dataaccess.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.dataaccess.EzraProposalDao;

public class EzraProposalDaoOjb extends PlatformAwareDaoBaseOjb implements EzraProposalDao {

	public List<EzraProposalAward> getProposalsUpdatedSince(Date date) {
		Criteria criteria = new Criteria();
		criteria.addLike("awardProposalId", "A%");
		criteria.addGreaterThan("budgetAmt", 0);
		List excludeStatus = new ArrayList();
		excludeStatus.add("AAC");
		excludeStatus.add("AC");
		excludeStatus.add("ACOSP");
		
	//	criteria.addNotIn("status", excludeStatus);
		if (date != null) {
		//	criteria.addLessThan("lastUpdated", date);
		}

        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
		
	}
	
//	public List<EzraProposalAward> getProposalsToImport() {
//		Criteria criteria = new Criteria();
//		criteria.addLike("awardProposalId", "%A");
//		criteria.addGreaterThan("budgetAmt", '0');
//		List excludeStatus = new ArrayList();
//		excludeStatus.add("AAC");
//		excludeStatus.add("AC");
//		excludeStatus.add("ACOSP");
//		
//		criteria.addNotIn("status", excludeStatus);
//		
//		
//        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
//
//	}
//	
}
