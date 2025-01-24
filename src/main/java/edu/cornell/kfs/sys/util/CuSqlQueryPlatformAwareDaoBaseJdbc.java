package edu.cornell.kfs.sys.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.CuPreparedStatementCreatorImpl;

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
            if (logSQLOnError || LOG.isDebugEnabled()) {
                logSQL(sqlQuery);
            }
            LOG.error("queryForValues, Unexpected error encountered while running query!", e);
            throw e;
        }
    }
    
    protected int executeUpdate(final CuSqlQuery sqlQuery) {
        return executeUpdate(sqlQuery, true);
    }
    
    protected int executeUpdate(final CuSqlQuery sqlQuery, boolean logSQLOnError) {
        try {
            return getJdbcTemplate().update(sqlQuery.getQueryString(), sqlQuery.getParametersArray());
        } catch (RuntimeException e) {
            if (logSQLOnError || LOG.isDebugEnabled()) {
                logSQL(sqlQuery);
            }
            LOG.error("update, Unexpected error encountered while running query!", e);
            throw e;
        }
    }
    
    protected <T> T queryForResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor) {
        return queryForResults(sqlQuery, resultSetExtractor, true);
    }
    
    protected <T> T queryForResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor,
            boolean logSQLOnError) {
        return queryForResults(sqlQuery, resultSetExtractor, CuPreparedStatementCreatorImpl::forReadOnlyResults,
                logSQLOnError);
    }
    
    protected <T> T queryForUpdatableResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor) {
        return queryForResults(sqlQuery, resultSetExtractor, true);
    }
    
    protected <T> T queryForUpdatableResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor,
            boolean logSQLOnError) {
        return queryForResults(sqlQuery, resultSetExtractor, CuPreparedStatementCreatorImpl::forUpdatableResults,
                logSQLOnError);
    }
    
    private <T> T queryForResults(final CuSqlQuery sqlQuery, final ResultSetExtractor<T> resultSetExtractor,
            final Function<String, PreparedStatementCreator> statementCreatorFactory, final boolean logSQLOnError) {
        try {
            final PreparedStatementCreator statementCreator = statementCreatorFactory.apply(sqlQuery.getQueryString());
            final PreparedStatementSetter statementSetter = new ArgumentTypePreparedStatementSetter(
                    sqlQuery.getParameterValuesArray(), sqlQuery.getParameterTypesArray());
            return getJdbcTemplate().query(statementCreator, statementSetter, resultSetExtractor);
        } catch (RuntimeException e) {
            if (logSQLOnError || LOG.isDebugEnabled()) {
                logSQL(sqlQuery);
            }
            LOG.error("queryForResults, Unexpected error encountered while running query!", e);
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
