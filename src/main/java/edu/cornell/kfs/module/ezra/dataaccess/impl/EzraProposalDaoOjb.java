package edu.cornell.kfs.module.ezra.dataaccess.impl;

import java.sql.Date;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposal;
import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.dataaccess.EzraProposalDao;

public class EzraProposalDaoOjb extends PlatformAwareDaoBaseOjb implements EzraProposalDao {

	public List<EzraProposal> getProposalsUpdatedSince(Date date) {
		Criteria criteria = new Criteria();
        criteria.addLessThan("lastUpdated", date);
        Criteria criteria2 = new Criteria();
        criteria2.addIsNull("lastUpdated");
        criteria.addOrCriteria(criteria2);

        return (List<EzraProposal>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposal.class, criteria));
		
	}
	
}
