package edu.cornell.kfs.module.ezra.dataaccess.impl;

import java.sql.Date;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;

import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.dataaccess.SponsorDao;

public class SponsorDaoOjb extends PlatformAwareDaoBaseOjb implements SponsorDao {

	public List<Sponsor> getSponsorsUpdatedSince(Date date) {
		Criteria criteria = new Criteria();
        criteria.addLessThan("lastUpdated", date);
        Criteria criteria2 = new Criteria();
        criteria2.addIsNull("lastUpdated");
        criteria.addOrCriteria(criteria2);

        return (List<Sponsor>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(Sponsor.class, criteria));
		
	}
	
}
