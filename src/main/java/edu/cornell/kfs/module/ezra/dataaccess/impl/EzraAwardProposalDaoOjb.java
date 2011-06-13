package edu.cornell.kfs.module.ezra.dataaccess.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.KualiDecimal;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;

public class EzraAwardProposalDaoOjb extends PlatformAwareDaoBaseOjb implements EzraAwardProposalDao {

	public List<EzraProposalAward> getProposals() {
		Criteria criteria = new Criteria();
		criteria.addLike("awardProposalId", "A%");
		criteria.addGreaterThan("budgetAmt",KualiDecimal.ZERO);
		criteria.addEqualTo("status", "ASAP");

		
		ReportQueryByCriteria query;
	    query = new ReportQueryByCriteria(EzraProposalAward.class, criteria);
	    query.setColumns(new String[] { "max(awardProposalId)" });
		
		
		
        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(query);
	}
	
	public List<EzraProposalAward> getAwardsUpdatedSince(Date date) {
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		Collection<Award> awards = bos.findAll(Award.class);
		List awardNumbers = new ArrayList();
		for (Award award : awards) {
			awardNumbers.add(award.getProposalNumber());
		}
		
		
		Criteria criteria = new Criteria();
		criteria.addIn("projectId", awardNumbers);
		criteria.addLike("awardProposalId", "A%");
		
		Criteria orCriteria = new Criteria();
		
		if (date != null) {
			orCriteria.addGreaterThan("lastUpdated", date);
		}
		criteria.addOrCriteria(orCriteria);

		ReportQueryByCriteria query;
	    query = new ReportQueryByCriteria(EzraProposalAward.class, criteria);
	    query.setColumns(new String[] { "max(awardProposalId)" });
	
		
        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(query);
	}

}
