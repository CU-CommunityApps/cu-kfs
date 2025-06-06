package edu.cornell.kfs.sys.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;

public class CuSqlQueryBatchPreparedStatementSetter<T> implements BatchPreparedStatementSetter {

    private final CuSqlQuery query;
    private final List<T> batchItems;

    public CuSqlQueryBatchPreparedStatementSetter(final CuSqlQuery query, final List<T> batchItems) {
        Validate.notNull(query, "query cannot be null");
        Validate.isTrue(query.isForBatchExecution(), "query does not contain any batch-item-derived parameters");
        Validate.isTrue(CollectionUtils.isNotEmpty(batchItems), "batchItems cannot be null or empty");
        this.query = query;
        this.batchItems = List.copyOf(batchItems);
    }

    @Override
    public int getBatchSize() {
        return batchItems.size();
    }

    @Override
    public void setValues(final PreparedStatement ps, final int i) throws SQLException {
        final T batchItem = batchItems.get(i);
        final List<SqlParameterValue> parameters = query.getParameters(batchItem);
        int parameterIndex = 1;
        for (final SqlParameterValue parameter : parameters) {
            final Integer scale = parameter.getScale();
            if (scale != null) {
                ps.setObject(parameterIndex, parameter.getValue(), parameter.getSqlType(), scale);
            } else {
                ps.setObject(parameterIndex, parameter.getValue(), parameter.getSqlType());
            }
            parameterIndex++;
        }
    }

}
