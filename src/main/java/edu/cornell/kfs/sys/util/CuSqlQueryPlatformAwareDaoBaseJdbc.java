package edu.cornell.kfs.sys.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
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
        return runQuery(sqlQuery, logSQLOnError,
                () -> getJdbcTemplate().query(sqlQuery.getQueryString(), rowMapper, sqlQuery.getParametersArray()));
    }

    protected int executeUpdate(final CuSqlQuery sqlQuery) {
        return executeUpdate(sqlQuery, true);
    }
    
    protected int executeUpdate(final CuSqlQuery sqlQuery, boolean logSQLOnError) {
        return runQuery(sqlQuery, logSQLOnError,
                () -> getJdbcTemplate().update(sqlQuery.getQueryString(), sqlQuery.getParametersArray()));
    }
    
    protected <T> int[] executeBatchUpdate(final CuSqlQuery sqlQuery, final List<T> batchItems) {
        return executeBatchUpdate(sqlQuery, batchItems, true);
    }
    
    protected <T> int[] executeBatchUpdate(final CuSqlQuery sqlQuery, final List<T> batchItems,
            final boolean logSQLOnError) {
        return runQuery(sqlQuery, logSQLOnError, () -> {
            final CuSqlQueryBatchPreparedStatementSetter<T> statementSetter
                    = new CuSqlQueryBatchPreparedStatementSetter<>(sqlQuery, batchItems);
            return getJdbcTemplate().batchUpdate(sqlQuery.getQueryString(), statementSetter);
        });
    }

    protected <T> T queryForResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor) {
        return queryForResults(sqlQuery, resultSetExtractor, true);
    }
    
    protected <T> T queryForResults(final CuSqlQuery sqlQuery, ResultSetExtractor<T> resultSetExtractor,
            boolean logSQLOnError) {
        return queryForResults(sqlQuery, resultSetExtractor,
                CuSqlQueryPreparedStatementCreatorAndSetter::forReadOnlyResults, logSQLOnError);
    }
    
    protected <T> T queryForResults(final CuSqlQuery sqlQuery, final ResultSetExtractor<T> resultSetExtractor,
            final Function<CuSqlQuery, CuSqlQueryPreparedStatementCreatorAndSetter> statementHandlerFactory,
            final boolean logSQLOnError) {
        return runQuery(sqlQuery, logSQLOnError, () -> {
            final CuSqlQueryPreparedStatementCreatorAndSetter statementHandler =
                    statementHandlerFactory.apply(sqlQuery);
            return getJdbcTemplate().query(statementHandler, statementHandler, resultSetExtractor);
        });
    }

    protected <T> T execute(final CuSqlQuery sqlQuery, final PreparedStatementCallback<T> action) {
        return execute(sqlQuery, action, true);
    }

    protected <T> T execute(final CuSqlQuery sqlQuery, final PreparedStatementCallback<T> action,
            final boolean logSQLOnError) {
        return runQuery(sqlQuery, logSQLOnError, () -> {
            final CuSqlQueryPreparedStatementCreatorAndSetter statementHandler =
                    CuSqlQueryPreparedStatementCreatorAndSetter.forReadOnlyResults(sqlQuery);
            return getJdbcTemplate().execute(statementHandler, preparedStatement -> {
                if (sqlQuery.getParameters().size() > 0) {
                    statementHandler.setValues(preparedStatement);
                }
                return action.doInPreparedStatement(preparedStatement);
            });
        });
    }

    protected <T> T runQuery(final CuSqlQuery sqlQueryForLog, final boolean logSQLOnError,
            final Supplier<T> queryRunner) {
        try {
            return queryRunner.get();
        } catch (final RuntimeException e) {
            if (logSQLOnError || LOG.isDebugEnabled()) {
                logSQL(sqlQueryForLog);
            }
            LOG.error("runQuery, Unexpected error encountered while running query!", e);
            throw e;
        }
    }

    protected void logSQL(CuSqlQuery sqlQuery) {
        LOG.info("logSQL, queryString: " + sqlQuery.getQueryString());
        LOG.info("logSQL, parameters: " + buildParametersMessage(sqlQuery));
    }

    private String buildParametersMessage(CuSqlQuery sqlQuery) {
        return sqlQuery.getParametersForLogging().stream()
                .map(this::buildMessageForSingleParameter)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
    }

    private String buildMessageForSingleParameter(SqlParameterValue parameter) {
        return MessageFormat.format(PARAMETER_MESSAGE_FORMAT, parameter.getSqlType(), parameter.getValue());
    }

}
