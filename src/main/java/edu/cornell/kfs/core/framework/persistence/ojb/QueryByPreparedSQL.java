package edu.cornell.kfs.core.framework.persistence.ojb;

import org.apache.ojb.broker.core.ValueContainer;
import org.apache.ojb.broker.metadata.JdbcType;
import org.apache.ojb.broker.metadata.JdbcTypesHelper;
import org.apache.ojb.broker.query.QueryBySQL;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.util.CuSqlQuery;

/**
 * Custom extension of QueryBySQL that supports parameterized plain-SQL queries.
 * Uses Cornell's custom CuSqlQuery class to define the SQL and its parameters.
 */
public class QueryByPreparedSQL extends QueryBySQL {

    private static final long serialVersionUID = 1L;

    private CuSqlQuery sqlQuery;

    public QueryByPreparedSQL(Class<?> targetClass, CuSqlQuery sqlQuery) {
        super(targetClass, sqlQuery.getQueryString());
        this.sqlQuery = sqlQuery;
    }

    public ValueContainer[] getParameters() {
        return sqlQuery.getParameters().stream()
                .map(this::createValueContainerFromParameter)
                .toArray(ValueContainer[]::new);
    }

    private ValueContainer createValueContainerFromParameter(SqlParameterValue parameter) {
        JdbcType jdbcType = JdbcTypesHelper.getJdbcTypeByTypesIndex(parameter.getSqlType());
        if (jdbcType == null) {
            throw new IllegalStateException("Could not find OJB variant of JDBC Type: " + parameter.getSqlType()
                    + " -- " + parameter.getTypeName());
        }
        return new ValueContainer(parameter.getValue(), jdbcType);
    }

}
