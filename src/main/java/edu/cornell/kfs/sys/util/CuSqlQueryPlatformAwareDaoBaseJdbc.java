package edu.cornell.kfs.sys.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;

public abstract class CuSqlQueryPlatformAwareDaoBaseJdbc extends PlatformAwareDaoBaseJdbc {
    private static final Logger LOG = LogManager.getLogger();
    private static final String PARAMETER_MESSAGE_FORMAT = "(Type: {0}, Value: {1})";
    
    protected <T> List<T> queryForValues(CuSqlQuery sqlQuery, RowMapper<T> rowMapper) {
        return queryForValues(sqlQuery, rowMapper, true);
    }
    
    protected <T> List<T> queryForValues(CuSqlQuery sqlQuery, RowMapper<T> rowMapper, boolean logSQLOnError) {
        try {
            return getJdbcTemplate().query(sqlQuery.getQueryString(), rowMapper, sqlQuery.getParametersArray());
        } catch (RuntimeException e) {
            if (logSQLOnError) {
                logSQL(sqlQuery);
            }
            LOG.error("queryForValues, Unexpected error encountered while running query!", e);
            throw e;
        }
    }
    
    protected void logSQL(CuSqlQuery sqlQuery) {
        LOG.info("logSQL, queryString: " + sqlQuery.getQueryString());
        LOG.info("logSQL, parameters: " + buildParametersMessage(sqlQuery));
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
