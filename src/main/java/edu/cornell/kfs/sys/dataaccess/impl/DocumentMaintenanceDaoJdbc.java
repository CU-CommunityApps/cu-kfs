package edu.cornell.kfs.sys.dataaccess.impl;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionitem.ActionItemExtension;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.KEWPropertyConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.springframework.jdbc.core.ConnectionCallback;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

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
                final Collection<String> docTypeIds = findNonRequeueableDocumentTypes();
                final Collection<String> roleIds = findRequeueableRoleIds();

                String selectStatementSql = buildRequeueSqlQuery(docTypeIds.size(), roleIds.size(), true);
                selectStatement = con.prepareStatement(selectStatementSql);

                addDocumentIdAndRoleIdsToSelectStatement(selectStatement, docTypeIds, roleIds);

                queryResultSet = selectStatement.executeQuery();

                while (queryResultSet.next()) {
                    String documentId = queryResultSet.getString(1);
                    documentIdsToRequeue.add(documentId);
                }

                queryResultSet.close();
            } finally {
                processQueryFinally(selectStatement, queryResultSet);
            }
            return documentIdsToRequeue;
        });
	}

	private String buildRequeueSqlQuery(int docTypeIdCount, int roleIdCount, boolean includeOrderByClause) {
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

		sql.append(")))");
		
		if (includeOrderByClause) {
		    sql.append(" order by ").append(workflowDocumentHeaderColumnName).append(" ASC");
		}

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
	
    @Override
    public List<ActionItemNoteDetailDto> getActionNotesToBeRequeued() {
        return getJdbcTemplate().execute((ConnectionCallback<List<ActionItemNoteDetailDto>>) con -> {
            PreparedStatement selectStatement = null;
            ResultSet queryResultSet = null;
            List<ActionItemNoteDetailDto> notes = new ArrayList<ActionItemNoteDetailDto>();

            try {
                final Collection<String> docTypeIds = findNonRequeueableDocumentTypes();
                final Collection<String> roleIds = findRequeueableRoleIds();

                String selectStatementSql = buildActionNoteQuery(docTypeIds.size(), roleIds.size());

                selectStatement = con.prepareStatement(selectStatementSql);

                addDocumentIdAndRoleIdsToSelectStatement(selectStatement, docTypeIds, roleIds);

                queryResultSet = selectStatement.executeQuery();

                while (queryResultSet.next()) {
                    String principalId = queryResultSet.getString(1);
                    String docHeaderId = queryResultSet.getString(2);
                    String actionNote = queryResultSet.getString(3);
                    Timestamp noteTimeStamp = queryResultSet.getTimestamp(4, Calendar.getInstance());
                    String originalActionItemId = queryResultSet.getString(5);
                    notes.add(new ActionItemNoteDetailDto(principalId, docHeaderId, actionNote, originalActionItemId, noteTimeStamp));
                }

                queryResultSet.close();
            } finally {
                processQueryFinally(selectStatement, queryResultSet);
            }
            return notes;
        });
    }

    private Collection<String> findNonRequeueableDocumentTypes() {
        return parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
    }

    private Collection<String> findRequeueableRoleIds() {
        return parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.REQUEUABLE_ROLES);
    }

    private void addDocumentIdAndRoleIdsToSelectStatement(PreparedStatement selectStatement,
            final Collection<String> docTypeIds, final Collection<String> roleIds) throws SQLException {
        int index = 1;
        for (String docTypeId: docTypeIds) {
            selectStatement.setString(index++, docTypeId);
        }

        for (String roleId: roleIds) {
            selectStatement.setString(index++, roleId);
        }
    }

    private void processQueryFinally(PreparedStatement selectStatement, ResultSet queryResultSet) {
        if (queryResultSet != null) {
            try {
                queryResultSet.close();
            } catch (SQLException e) {
                LOG.error("processQueryFinally: Could not close ResultSet", e);
            }
        }
        if (selectStatement != null) {
            try {
                selectStatement.close();
            } catch (SQLException e) {
                LOG.error("processQueryFinally: Could not close selection PreparedStatement", e);
            }
        }
    }
	
    private String buildActionNoteQuery(int docTypeIdCount, int roleIdCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ai.prncpl_id, ai.doc_hdr_id, aie.actn_note, aie.note_ts, ai.actn_itm_id ");
        sb.append("from CYNERGY.").append(retrieveTableNameFromAnnotations(ActionItem.class)).append(" ai, CYNERGY.");
        sb.append(retrieveTableNameFromAnnotations(ActionItemExtension.class)).append(" aie ");
        sb.append("where ai.actn_itm_id = aie.actn_itm_id ");
        sb.append("and ai.doc_hdr_id in (");
        sb.append(buildRequeueSqlQuery(docTypeIdCount, roleIdCount, false));
        sb.append(")");
        return sb.toString();
    }
	
	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
	
}
