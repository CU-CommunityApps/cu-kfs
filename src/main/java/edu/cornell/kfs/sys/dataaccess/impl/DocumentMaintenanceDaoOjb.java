/**
 * 
 */
package edu.cornell.kfs.sys.dataaccess.impl;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.KEWPropertyConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.krad.util.OjbCollectionAware;
import org.springframework.util.StringUtils;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;

/**
 * @author dwf5
 *
 */
public class DocumentMaintenanceDaoOjb extends PlatformAwareDaoBaseOjb implements DocumentMaintenanceDao, OjbCollectionAware {
    public static final String WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY = "routeHeaderId";

    
    private ParameterService parameterService;
    
    /**
     * Default constructor
     */
    public DocumentMaintenanceDaoOjb() {
    	super();
    }
    
	/**
	 * @see edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao#getDocumentRequeueFileValues()
	 */
	public List<String> getDocumentRequeueValues() {
		Criteria criteria = new Criteria();
		
		String sql = buildRequeueSqlCriteria();
		
		criteria.addSql(sql);
		ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
		query.setAttributes(new String[] {WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY});
		Iterator<Object[]> results = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);

        return parseReportQueryIteratorToList(results);
	}

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
	 * 
	 * @return
	 */
	private String buildRequeueSqlCriteria() {
		StringBuffer sql = new StringBuffer();
		
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, "docRouteStatus"));
		sql.append("='");
		sql.append(KewApiConstants.ROUTE_HEADER_ENROUTE_CD);
		sql.append("' AND ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, KEWPropertyConstants.DOCUMENT_TYPE_ID));
		sql.append(" NOT IN (");

		List<String> docTypeIds = (List<String>) parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
		sql.append(StringUtils.collectionToCommaDelimitedString(docTypeIds));
		
		sql.append(") AND ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY));
		sql.append(" IN (select distinct(");
		sql.append(retrieveColumnNameFromAnnotations(ActionRequestValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY));
		sql.append(") from ");
		sql.append(retrieveTableNameFromAnnotations(ActionRequestValue.class));
		sql.append(" where ");
		sql.append("RSP_ID");
		sql.append(" in (select ");
		sql.append("RSP_ID");
		sql.append(" from ");
		sql.append("KRIM_ROLE_RSP_T");
		sql.append(" where ");
		sql.append("RSP_ID");
		sql.append("='");

		List<String> roleIds = (List<String>) parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.REQUEUABLE_ROLES);
		sql.append(StringUtils.collectionToCommaDelimitedString(roleIds));

		sql.append("')) order by ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY));
		sql.append(" ASC");

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
	
	/**
	 * 
	 * @return
	 */
	public List<String> getDocumentReindexValues() {
		List<String> reindexValues = new ArrayList<String> ();
		
		Criteria criteria = new Criteria();
		
		String sql = buildReindexSqlCriteria();
		
		criteria.addSql(sql);
		ReportQueryByCriteria query = QueryFactory.newReportQuery(DocumentRouteHeaderValue.class, criteria);
		query.setAttributes(new String[] {WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY});
		Iterator<Object[]> results = this.getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(query);

		
		return reindexValues;
	}
	
	/**
	* 
	* @return
	*/
	private String buildReindexSqlCriteria() {
		StringBuffer sql = new StringBuffer();
		
		
		return sql.toString();
	}

	/**
	 * 
	 * @param className
	 * @param fieldName
	 * @return
	 */
	private String retrieveColumnNameFromAnnotations(Class className, String fieldName) {
		String columnName = "";
		try {
			Annotation[] annots = className.getDeclaredField(fieldName).getAnnotations();
			for(int i=0;i<annots.length;i++) {
				Annotation annot = annots[i];
				if(annot instanceof Column) {
					columnName = ((Column) annot).name();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return columnName;
	}
	
	/**
	 * 
	 * @param className
	 * @return
	 */
	private String retrieveTableNameFromAnnotations(Class className) {
		String tableName = "";
		try {
			Annotation[] annots = className.getDeclaredAnnotations();
			for(int i=0;i<annots.length;i++) {
				Annotation annot = annots[i];
				if(annot instanceof Table) {
					tableName = ((Table) annot).name();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return tableName;
	}
	
	/**
	 * @return the parameterService
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}

	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
	
}
