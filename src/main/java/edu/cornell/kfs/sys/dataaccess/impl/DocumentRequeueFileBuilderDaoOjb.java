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
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kim.bo.role.RoleResponsibility;
import org.kuali.rice.kim.bo.role.impl.RoleResponsibilityImpl;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.dao.BusinessObjectDao;
import org.kuali.rice.kns.dao.impl.DocumentDaoOjb;
import org.kuali.rice.kns.dao.impl.PlatformAwareDaoBaseOjb;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.kuali.rice.kns.util.OjbCollectionAware;

import edu.cornell.kfs.sys.dataaccess.DocumentRequeueFileBuilderDao;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author dwf5
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
		
		// TODO Parameterize values and try to pull column names from OJB
		Criteria criteria = new Criteria();
		
		String sql = buildSqlCriteria();
		
		criteria.addSql(sql);
		ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
		query.setAttributes(new String[] {KEWPropertyConstants.ROUTE_HEADER_ID});
        Iterator<Object[]> results = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);

        return parseReportQueryIteratorToList(results);
	}

	private String buildSqlCriteria() {
		StringBuffer sql = new StringBuffer();
		
		sql.append("DOC_HDR_STAT_CD ='");
		sql.append(KEWConstants.ROUTE_HEADER_ENROUTE_CD);
		sql.append("' and doc_typ_id not in (");

		sql.append("100906, 325237");
//		List<String> parms = SpringContext.getBean(ParameterService.class).getParameterValues(String.class, "");
//		String parmValues = Arrays.toString(parms.toArray());
//		sql.append(parmValues);

		sql.append(") and doc_hdr_id in ("+
				"select distinct(DOC_HDR_ID) from KREW_ACTN_RQST_T where RSP_ID in ("+
				"select RSP_ID from KRIM_ROLE_RSP_T where ROLE_ID='");

		sql.append("41");
//		List<String> parms = SpringContext.getBean(ParameterService.class).getParameterValues(String.class, "");
//		String parmValues = Arrays.toString(parms.toArray());
//		sql.append(parmValues);

		sql.append("')) order by doc_hdr_id ASC");
		
		return sql.toString();
	}
	
	/**
	 * 
	 * @param iter
	 * @return
	 */
	private List<String> parseReportQueryIteratorToList(Iterator<Object[]> iter) {
        // Results only returned as an Iterator of Object[]s so pull values out of iterator and put into array list
        ArrayList<String> ids = new ArrayList<String>();
        while(iter.hasNext()) {
        	Object[] next = (Object[])iter.next();
        	ids.add(((BigDecimal)next[0]).toPlainString());
        }
        
		return ids;
	}
	
}
