package edu.cornell.kfs.sys.dataaccess.impl;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.KEWPropertyConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocumentMaintenanceDaoJdbc extends PlatformAwareDaoBaseJdbc implements DocumentMaintenanceDao {
	private static final Logger LOG = LogManager.getLogger(DocumentMaintenanceDaoJdbc.class);

	public static final String WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY = "documentId";

    private ParameterService parameterService;

	/**
	 * @see DocumentMaintenanceDao#getDocumentRequeueValues()#getDocumentRequeueFileValues()
	 */
	@Override
	public Collection<String> getDocumentRequeueValues() {
		return getJdbcTemplate().execute((ConnectionCallback<Collection<String>>) con -> {
            PreparedStatement selectStatement = null;
            ResultSet queryResultSet = null;
            List<String> documentIdsToRequeue = new ArrayList<>();

            try {
                final Collection<String> docTypeIds = parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
                final Collection<String> roleIds = parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.REQUEUABLE_ROLES);

                String selectStatementSql = buildRequeueSqlQuery(docTypeIds.size(), roleIds.size());
                selectStatement = con.prepareStatement(selectStatementSql);

                int index = 1;
                for (String docTypeId: docTypeIds) {
                    selectStatement.setString(index++, docTypeId);
                }

                for (String roleId: roleIds) {
                    selectStatement.setString(index++, roleId);
                }

                queryResultSet = selectStatement.executeQuery();

                while (queryResultSet.next()) {
                    String documentId = queryResultSet.getString(1);
                    documentIdsToRequeue.add(documentId);
                }

                queryResultSet.close();
            } finally {
                if (queryResultSet != null) {
                    try {
                        queryResultSet.close();
                    } catch (SQLException e) {
                        LOG.error("getDocumentRequeueValues: Could not close ResultSet");
                    }
                }
                if (selectStatement != null) {
                    try {
                        selectStatement.close();
                    } catch (SQLException e) {
                        LOG.error("getDocumentRequeueValues: Could not close selection PreparedStatement");
                    }
                }
            }
            return documentIdsToRequeue;
        });
	}

	private String buildRequeueSqlQuery(int docTypeIdCount, int roleIdCount) {
		StringBuilder sql = new StringBuilder();

		sql.append("select DOC_HDR_ID from CYNERGY.KREW_DOC_HDR_T where ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, "docRouteStatus"));
		sql.append("='");
		sql.append(KewApiConstants.ROUTE_HEADER_ENROUTE_CD);
		sql.append("' AND ");
		sql.append(retrieveColumnNameFromAnnotations(DocumentRouteHeaderValue.class, KEWPropertyConstants.DOCUMENT_TYPE_ID));
		sql.append(" NOT IN (?");

		for (int i=1; i<docTypeIdCount; i++) {
			sql.append(",?");
		}

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
		sql.append(" in (?");

		for (int i=1; i<roleIdCount; i++) {
			sql.append(",?");
		}

		sql.append("))) order by ");
		sql.append(workflowDocumentHeaderColumnName);
		sql.append(" ASC");

		return sql.toString();
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
