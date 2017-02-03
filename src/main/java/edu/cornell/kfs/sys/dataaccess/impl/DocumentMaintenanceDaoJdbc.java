package edu.cornell.kfs.sys.dataaccess.impl;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.KEWPropertyConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DocumentMaintenanceDaoJdbc extends PlatformAwareDaoBaseJdbc implements DocumentMaintenanceDao {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentMaintenanceDaoJdbc.class);

	public static final String WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY = "documentId";

    private ParameterService parameterService;

	/**
	 * @see DocumentMaintenanceDao#getDocumentRequeueValues()#getDocumentRequeueFileValues()
	 */
	@Override
	public Collection<String> getDocumentRequeueValues() {
		String sql = buildRequeueSqlCriteria();

		List<Map<String, Object>> results = getSimpleJdbcTemplate().queryForList(sql);

		return convertResultsToList(results);
	}

	private String buildRequeueSqlCriteria() {
		StringBuilder sql = new StringBuilder();

		sql.append("select DOC_HDR_ID from CYNERGY.KREW_DOC_HDR_T where ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, "docRouteStatus"));
		sql.append("='");
		sql.append(KewApiConstants.ROUTE_HEADER_ENROUTE_CD);
		sql.append("' AND ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, KEWPropertyConstants.DOCUMENT_TYPE_ID));
		sql.append(" NOT IN (");

		final Collection<String> docTypeIds = parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
		sql.append(StringUtils.collectionToCommaDelimitedString(docTypeIds));
		sql.append(") AND ");

		final String workflowDocumentHeaderColumnName = retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY);
		sql.append(workflowDocumentHeaderColumnName);
		sql.append(" IN (select distinct(");
		sql.append(retrieveColumnNameFromAnnotations(ActionRequestValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY));
		sql.append(") from CYNERGY.");
		sql.append(retrieveTableNameFromAnnotations(ActionRequestValue.class));
		sql.append(" where ");
		sql.append("RSP_ID");
		sql.append(" in (select ");
		sql.append("RSP_ID");
		sql.append(" from ");
		sql.append("CYNERGY.KRIM_ROLE_RSP_T");
		sql.append(" where ");
		sql.append("ROLE_ID");
		sql.append("='");

		final Collection<String> roleIds = parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.REQUEUABLE_ROLES);
		sql.append(StringUtils.collectionToCommaDelimitedString(roleIds));
		sql.append("')) order by ");
		sql.append(workflowDocumentHeaderColumnName);
		sql.append(" ASC");

		return sql.toString();
	}
	
	private List<String> convertResultsToList(List<Map<String, Object>> results) {
		ArrayList<String> documentIds = new ArrayList<String>();

		for (Map<String, Object> resultMap : results) {
			documentIds.add((String)resultMap.get(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY)));
		}

		return documentIds;
	}
	
	private String retrieveColumnNameFromAnnotations(Class className, String fieldName) {
		String columnName = KFSConstants.EMPTY_STRING;

		try {
			Annotation[] annotations = className.getDeclaredField(fieldName).getAnnotations();
			for(Annotation annotation: annotations) {
				if(annotation instanceof Column) {
					columnName = ((Column) annotation).name();
					break;
				}
			}
		} catch (NoSuchFieldException e) {
			LOG.warn("retrieveColumnNameFromAnnotations - caught Exception when trying to determine columnName", e);
		}
		
		return columnName;
	}
	
	private String retrieveTableNameFromAnnotations(Class className) {
		String tableName = KFSConstants.EMPTY_STRING;

		Annotation[] annotations = className.getDeclaredAnnotations();
		for (Annotation annotation: annotations) {
			if (annotation instanceof Table) {
				tableName = ((Table) annotation).name();
				break;
			}
		}

		return tableName;
	}
	
	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
	
}
