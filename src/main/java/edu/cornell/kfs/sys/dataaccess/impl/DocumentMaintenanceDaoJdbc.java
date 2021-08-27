package edu.cornell.kfs.sys.dataaccess.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.jdbc.core.ConnectionCallback;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;

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

                addQueryParameterCollectionsToSelectStatement(selectStatement,
                        List.of(KewApiConstants.ROUTE_HEADER_ENROUTE_CD), docTypeIds, roleIds);

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
        return buildSqlQuery(
                "SELECT DOC_HDR_ID FROM KFS.KREW_DOC_HDR_T ",
                "WHERE DOC_HDR_STAT_CD = ? ",
                "AND DOC_TYP_ID NOT IN (", buildPlaceholderList(docTypeIdCount), ") ",
                "AND DOC_HDR_ID IN (",
                        "SELECT DISTINCT DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T ",
                        "WHERE RSP_ID IN (",
                                "SELECT RSP_ID FROM KFS.KRIM_ROLE_RSP_T ",
                                "WHERE ROLE_ID IN (", buildPlaceholderList(roleIdCount), ")))",
                buildPotentialOrderByClauseForRequeueSqlQuery(includeOrderByClause));
    }

    private String buildPotentialOrderByClauseForRequeueSqlQuery(boolean includeOrderByClause) {
        return includeOrderByClause ? " ORDER BY DOC_HDR_ID ASC" : KFSConstants.EMPTY_STRING;
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

                addQueryParameterCollectionsToSelectStatement(selectStatement,
                        List.of(KewApiConstants.ROUTE_HEADER_ENROUTE_CD), docTypeIds, roleIds);

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

    @SafeVarargs
    private void addQueryParameterCollectionsToSelectStatement(PreparedStatement selectStatement,
            Collection<String>... parameterCollections) throws SQLException {
        int index = 1;
        for (Collection<String> parameterCollection : parameterCollections) {
            for (String parameter : parameterCollection) {
                selectStatement.setString(index++, parameter);
            }
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
        return buildSqlQuery(
                "SELECT AI.PRNCPL_ID, AI.DOC_HDR_ID, AIE.ACTN_NOTE, AIE.LAST_UPDT_TS, AI.ACTN_ITM_ID ",
                "FROM KFS.KREW_ACTN_ITM_T AI ",
                "JOIN KFS.KREW_ACTN_ITM_EXT_T AIE ON AI.ACTN_ITM_ID = AIE.ACTN_ITM_ID ",
                "WHERE AI.DOC_HDR_ID IN (", buildRequeueSqlQuery(docTypeIdCount, roleIdCount, false), ")");
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    private String buildSqlQuery(String... sqlFragments) {
        return StringUtils.join(sqlFragments);
    }

    private String buildPlaceholderList(int size) {
        return StringUtils.repeat(KFSConstants.QUESTION_MARK, CUKFSConstants.COMMA_AND_SPACE, size);
    }

}
