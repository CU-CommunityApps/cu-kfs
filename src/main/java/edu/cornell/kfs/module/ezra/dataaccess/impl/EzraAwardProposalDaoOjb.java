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
	    //KFSPTS-1920 Edits associated with Award Descriptions of MTA(M), NDA(N), RDA(R) should not be picked up for edits to KFS Awards.
        criteria.addNotIn("awardDescriptionCode", getExcludedAwardDescriptions());

		
//		ReportQueryByCriteria query;
//	    query = new ReportQueryByCriteria(EzraProposalAward.class, criteria);
//	    query.setColumns(new String[] { "max(awardProposalId)" });
//		
		
		
        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
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
		//KFSPTS-1920 Edits associated with Award Descriptions of MTA(M), NDA(N), RDA(R) should not be picked up for edits to KFS Awards.
		criteria.addNotIn("awardDescriptionCode", getExcludedAwardDescriptions());
		
		if (date != null) {
			criteria.addGreaterThan("lastUpdated", date);
		}
//		ReportQueryByCriteria query;
//	    query = new ReportQueryByCriteria(EzraProposalAward.class, criteria);
//	    query.setColumns(new String[] { "max(awardProposalId)" });
	
		
        return (List<EzraProposalAward>)getPersistenceBrokerTemplate().getCollectionByQuery(QueryFactory.newQuery(EzraProposalAward.class, criteria));
	}
	
	/**
	 * Returns a list of award descriptions that should be excluded from KFS Award edits.
	 * 
	 * @return a list of award descriptions that should be excluded from KFS Award edits.
	 */
	private List<String> getExcludedAwardDescriptions(){
	       List<String> excludedAwardDescriptions = new ArrayList<String>();
	       
	       excludedAwardDescriptions.add("M");
	       excludedAwardDescriptions.add("N");
	       excludedAwardDescriptions.add("R");
	       
	       return excludedAwardDescriptions;
	}

}
