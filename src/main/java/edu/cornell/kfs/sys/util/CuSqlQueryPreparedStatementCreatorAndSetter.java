package edu.cornell.kfs.sys.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;

/**
 * Convenience CuSqlQuery-to-JDBC-query class that can be used as both a PreparedStatementCreator
 * and a PreparedStatementSetter by a JdbcTemplate. Furthermore, it also allows specifying the desired
 * type and concurrency for the generated ResultSet.
 */
public class CuSqlQueryPreparedStatementCreatorAndSetter extends ArgumentTypePreparedStatementSetter
        implements PreparedStatementCreator, SqlProvider {

    private final CuSqlQuery query;
    private final int resultSetType;
    private final int resultSetConcurrency;

    public CuSqlQueryPreparedStatementCreatorAndSetter(final CuSqlQuery query, final int resultSetType,
            final int resultSetConcurrency) {
        super(query.getParameterValuesArray(), query.getParameterTypesArray());
        this.query = query;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public static CuSqlQueryPreparedStatementCreatorAndSetter forReadOnlyResults(final CuSqlQuery query) {
        return new CuSqlQueryPreparedStatementCreatorAndSetter(
                query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public String getSql() {
        return query.getQueryString();
    }

    @Override
    public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
        return con.prepareStatement(getSql(), resultSetType, resultSetConcurrency);
    }

}
