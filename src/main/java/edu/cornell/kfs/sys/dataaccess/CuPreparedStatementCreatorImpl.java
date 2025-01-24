package edu.cornell.kfs.sys.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;

public class CuPreparedStatementCreatorImpl implements PreparedStatementCreator, SqlProvider {

    private final String sql;
    private final int resultSetType;
    private final int resultSetConcurrency;

    public CuPreparedStatementCreatorImpl(final String sql, final int resultSetType, final int resultSetConcurrency) {
        Validate.notBlank(sql, "sql cannot be blank");
        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    public static CuPreparedStatementCreatorImpl forReadOnlyResults(final String sql) {
        return new CuPreparedStatementCreatorImpl(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    public static CuPreparedStatementCreatorImpl forUpdatableResults(final String sql) {
        return new CuPreparedStatementCreatorImpl(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
        return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

}
