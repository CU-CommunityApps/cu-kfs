package edu.cornell.kfs.sys.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;

public class PreparedStatementCreatorForUpdatableResultSets implements PreparedStatementCreator, SqlProvider {

    private final String sql;

    public PreparedStatementCreatorForUpdatableResultSets(final String sql) {
        Validate.notBlank(sql, "sql cannot be blank");
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
        return con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

}
