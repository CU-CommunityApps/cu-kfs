package edu.cornell.kfs.sys.dataaccess.impl;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.ActionItemNoteDetailDto;
import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public class DocumentMaintenanceDaoJdbc extends PlatformAwareDaoBaseJdbc implements DocumentMaintenanceDao {
    private static final Logger LOG = LogManager.getLogger();

    private static final String PARAMETER_MESSAGE_FORMAT = "(Type: '{0}', Value: '{1}')";

    @Override
    public List<ActionItemNoteDetailDto> getActionNotesToBeRequeuedForDocument(String documentId) {
        CuSqlChunk docIdParameter = CuSqlChunk.forParameter(documentId);
        CuSqlQuery sqlQuery = buildActionNoteQuery(docIdParameter);
        return getActionNotesToBeRequeued(sqlQuery);
    }

    private List<ActionItemNoteDetailDto> getActionNotesToBeRequeued(CuSqlQuery sqlQuery) {
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

    private CuSqlQuery buildActionNoteQuery(CuSqlChunk docIdListOrSubQuery) {
        return CuSqlQuery.of(
                "SELECT AI.PRNCPL_ID, AI.DOC_HDR_ID, AIE.ACTN_NOTE, AIE.LAST_UPDT_TS, AI.ACTN_ITM_ID ",
                "FROM KFS.KREW_ACTN_ITM_T AI ",
                "JOIN KFS.KREW_ACTN_ITM_EXT_T AIE ON AI.ACTN_ITM_ID = AIE.ACTN_ITM_ID ",
                "WHERE AI.DOC_HDR_ID IN (", docIdListOrSubQuery, ")");
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

}
