package edu.cornell.kfs.sys.dataaccess.impl;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.batch.DocumentRequeueStep;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public class DocumentMaintenanceDaoJdbc extends PlatformAwareDaoBaseJdbc implements DocumentMaintenanceDao {
    private static final Logger LOG = LogManager.getLogger();

    private static final String PARAMETER_MESSAGE_FORMAT = "(Type: '{0}', Value: '{1}')";

    private ParameterService parameterService;

    @Override
    public Collection<String> getDocumentRequeueValues() {
        CuSqlQuery sqlQuery = buildRequeueSqlQuery();
        RowMapper<String> documentIdMapper = (resultSet, rowNum) -> resultSet.getString(1);
        return queryForValues(sqlQuery, documentIdMapper);
    }

    private CuSqlQuery buildRequeueSqlQuery() {
        CuSqlChunk sqlChunk = buildRequeueSqlQueryChunkWithOrderByClause();
        return sqlChunk.toQuery();
    }

    private CuSqlChunk buildRequeueSqlQueryChunkWithOrderByClause() {
        return buildRequeueSqlQueryChunk(true);
    }

    private CuSqlChunk buildRequeueSqlQueryChunkWithoutOrderByClause() {
        return buildRequeueSqlQueryChunk(false);
    }

    private CuSqlChunk buildRequeueSqlQueryChunk(boolean includeOrderByClause) {
        Collection<String> docTypeIds = findNonRequeueableDocumentTypes();
        Collection<String> roleIds = findRequeueableRoleIds();
        CuSqlChunk subQuery = CuSqlChunk.of(
                "SELECT DH.DOC_HDR_ID FROM KFS.KREW_DOC_HDR_T DH ",
                "WHERE DH.DOC_HDR_STAT_CD = ", CuSqlChunk.forParameter(KewApiConstants.ROUTE_HEADER_ENROUTE_CD),
                " AND DH.DOC_TYP_ID NOT IN (", CuSqlChunk.forStringParameters(docTypeIds), ")",
                " AND EXISTS (",
                        "SELECT DISTINCT RQ.DOC_HDR_ID FROM KFS.KREW_ACTN_RQST_T RQ ",
                        "WHERE DH.DOC_HDR_ID = RQ.DOC_HDR_ID ",
                        "AND RQ.RSP_ID IN (",
                                "SELECT RR.RSP_ID FROM KFS.KRIM_ROLE_RSP_T RR ",
                                "WHERE RR.ROLE_ID IN (", CuSqlChunk.forStringParameters(roleIds), ")))");
        if (includeOrderByClause) {
            subQuery.append(" ORDER BY DH.DOC_HDR_ID ASC");
        }
        return subQuery;
    }

    @Override
    public List<ActionItemNoteDetailDto> getActionNotesToBeRequeued() {
        CuSqlQuery sqlQuery = buildActionNoteQuery();
        RowMapper<ActionItemNoteDetailDto> actionItemNoteMapper = (resultSet, rowNum) -> {
            ActionItemNoteDetailDto actionItemNote = new ActionItemNoteDetailDto();
            actionItemNote.setPrincipalId(resultSet.getString(1));
            actionItemNote.setDocHeaderId(resultSet.getString(2));
            actionItemNote.setActionNote(resultSet.getString(3));
            actionItemNote.setNoteTimeStamp(resultSet.getTimestamp(4, Calendar.getInstance()));
            actionItemNote.setOriginalActionItemId(resultSet.getString(5));
            return actionItemNote;
        };
        return queryForValues(sqlQuery, actionItemNoteMapper);
    }

    private CuSqlQuery buildActionNoteQuery() {
        return CuSqlQuery.of(
                "SELECT AI.PRNCPL_ID, AI.DOC_HDR_ID, AIE.ACTN_NOTE, AIE.LAST_UPDT_TS, AI.ACTN_ITM_ID ",
                "FROM KFS.KREW_ACTN_ITM_T AI ",
                "JOIN KFS.KREW_ACTN_ITM_EXT_T AIE ON AI.ACTN_ITM_ID = AIE.ACTN_ITM_ID ",
                "WHERE AI.DOC_HDR_ID IN (", buildRequeueSqlQueryChunkWithoutOrderByClause(), ")");
    }

    private <T> List<T> queryForValues(CuSqlQuery sqlQuery, RowMapper<T> rowMapper) {
        try {
            return getJdbcTemplate().query(sqlQuery.getQueryString(), rowMapper, sqlQuery.getParametersArray());
        } catch (RuntimeException e) {
            LOG.error("queryForValues, Unexpected error encountered while running query! Query String: <["
                    + sqlQuery.getQueryString() + "]>, Query Parameters: <["
                    + buildParametersMessage(sqlQuery) + "]>", e);
            throw e;
        }
    }

    private String buildParametersMessage(CuSqlQuery sqlQuery) {
        return sqlQuery.getParameters().stream()
                .map(this::buildMessageForSingleParameter)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
    }

    private String buildMessageForSingleParameter(SqlParameterValue parameter) {
        return MessageFormat.format(PARAMETER_MESSAGE_FORMAT, parameter.getSqlType(), parameter.getValue());
    }

    private Collection<String> findNonRequeueableDocumentTypes() {
        return parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.NON_REQUEUABLE_DOCUMENT_TYPES);
    }

    private Collection<String> findRequeueableRoleIds() {
        return parameterService.getParameterValuesAsString(DocumentRequeueStep.class, CUKFSParameterKeyConstants.REQUEUABLE_ROLES);
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
