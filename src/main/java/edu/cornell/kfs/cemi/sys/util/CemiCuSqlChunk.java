package edu.cornell.kfs.cemi.sys.util;

import edu.cornell.kfs.sys.util.CuSqlChunk;

public final class CemiCuSqlChunk {
    
    public static CuSqlChunk asSqlBetweenCondition(final String columnName, final int sqlType,
            final Object rangeStartValue, final Object rangeEndValue) {
        return new CuSqlChunk()
                .append(columnName)
                .append(" BETWEEN ").appendAsParameter(sqlType, rangeStartValue)
                .append(" AND ").appendAsParameter(sqlType, rangeEndValue);
    }
}
