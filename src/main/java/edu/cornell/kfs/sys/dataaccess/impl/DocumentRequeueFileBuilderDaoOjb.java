/**
 * 
 */
package edu.cornell.kfs.sys.dataaccess.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryBySQL;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kim.bo.role.RoleResponsibility;
import org.kuali.rice.kim.bo.role.impl.RoleResponsibilityImpl;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.dao.BusinessObjectDao;
import org.kuali.rice.kns.dao.impl.DocumentDaoOjb;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.OjbCollectionAware;

import edu.cornell.kfs.sys.dataaccess.DocumentRequeueFileBuilderDao;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author Admin-dwf5
 *
 */
public class DocumentRequeueFileBuilderDaoOjb extends PlatformAwareDaoBaseOjb implements DocumentRequeueFileBuilderDao, OjbCollectionAware {
    private static final Logger LOG = Logger.getLogger(DocumentRequeueFileBuilderDaoOjb.class);

    /**
     * Default constructor
     */
    public DocumentRequeueFileBuilderDaoOjb() {
    	super();
    }
    
	/**
	 * @see edu.cornell.kfs.sys.dataaccess.DocumentRequeueFileBuilderDao#getDocumentRequeueFileValues()
	 */
	public List<String> getDocumentRequeueFileValues() {
		
		/**
			select DOC_HDR_ID 
			from cynergy.KREW_DOC_HDR_T 
			where DOC_HDR_STAT_CD ='R' 
				and doc_typ_id not in (100906, 325237) 
				and doc_hdr_id in (
					select distinct(DOC_HDR_ID) 
					from cynergy.KREW_ACTN_RQST_T 
					where RSP_ID in (
						select RSP_ID 
						from cynergy.KRIM_ROLE_RSP_T 
						where ROLE_ID=41)) 
			order by doc_hdr_id ASC;
    	 */
		
/*		
		  // *********** Takes 5+ minutes to complete ************
	      Criteria criteria = new Criteria();
	      
	      List<String> idList = new ArrayList<String>();
	      idList.add("100906"); // Account delegate
	      idList.add("325237"); // Account delegate global
	      criteria.addNotIn("documentTypeId", idList);
	
	      Criteria criteria1 = new Criteria();
	      criteria1.addEqualTo("docRouteStatus", "R");
	      
	      criteria.addAndCriteria(criteria1);
	      
	      Criteria criteria4 = new Criteria();
	      criteria4.addEqualTo("roleId", "41"); // Role 41 = Fiscal Officer
	      
	      ReportQueryByCriteria subQuery2 = QueryFactory.newReportQuery(RoleResponsibilityImpl.class, criteria4);
	      ArrayList<RoleResponsibilityImpl> subQuery2Results = new ArrayList<RoleResponsibilityImpl>(this.getPersistenceBrokerTemplate().getCollectionByQuery(subQuery2));
	      
	      ArrayList subQuery2IdsOnly = new ArrayList();
	      for(RoleResponsibilityImpl rrImpl : subQuery2Results) {
	      	subQuery2IdsOnly.add(rrImpl.getResponsibilityId());
	      }
	      
	      Criteria criteria3 = new Criteria();
	      criteria3.addIn("responsibilityId", subQuery2IdsOnly);
	      
	      ReportQueryByCriteria subQuery = QueryFactory.newReportQuery(ActionRequestValue.class, criteria3);
	      subQuery.setAttributes(new String[]{"routeHeaderId"});
	      subQuery.setDistinct(true);
	      Iterator subQueryResults = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(subQuery);
	      
	      ArrayList subQueryIdsOnly = new ArrayList();
	      while(subQueryResults.hasNext()) {
	      	Object[] item = (Object[])subQueryResults.next();
	      	subQueryIdsOnly.add(item[0]);
	      }
	      
	      Criteria criteria2 = new Criteria();
	      criteria2.addIn("routeHeaderId", subQueryIdsOnly);
	
	      criteria.addAndCriteria(criteria2);
	      
	      ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
	      query.setAttributes(new String[]{"routeHeaderId"});
	      Iterator queryResults = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
*/
		
		// *********** Runs in <1 minute ************
		Criteria criteria = new Criteria();
		criteria.addSql("DOC_HDR_STAT_CD ='R' and doc_typ_id not in (100906, 325237) and doc_hdr_id in ("+
				"select distinct(DOC_HDR_ID) from KREW_ACTN_RQST_T where RSP_ID in ("+
				"select RSP_ID from KRIM_ROLE_RSP_T where ROLE_ID=41)) order by doc_hdr_id ASC");
		ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
		query.setAttributes(new String[] {"routeHeaderId"});
        Iterator<Object[]> results = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);
		
        // Results only returned as an Iterator of Object[]s so pull values out of iterator and put into array list
        ArrayList<String> ids = new ArrayList<String>();
        while(results.hasNext()) {
        	Object[] next = (Object[])results.next();
        	ids.add(((BigDecimal)next[0]).toPlainString());
        }
        
		return ids;
	}

}
