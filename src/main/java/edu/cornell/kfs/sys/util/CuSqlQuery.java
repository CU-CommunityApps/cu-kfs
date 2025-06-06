package edu.cornell.kfs.sys.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.SqlParameterValue;

public final class CuSqlQuery {

    private final String queryString;
    private final List<SqlParameterValue> parameters;

    public CuSqlQuery(String queryString, Stream<SqlParameterValue> parameters) {
        if (StringUtils.isBlank(queryString)) {
            throw new IllegalArgumentException("queryString cannot be blank");
        }
        Objects.requireNonNull(parameters, "parameters cannot be null");
        this.queryString = queryString;
        this.parameters = parameters.collect(Collectors.toUnmodifiableList());
    }

    public String getQueryString() {
        return queryString;
    }

    public List<SqlParameterValue> getParameters() {
        return parameters;
    }

    public Object[] getParametersArray() {
        return parameters.toArray();
    }

    public Object[] getParameterValuesArray() {
        return parameters.stream()
                .map(SqlParameterValue::getValue)
                .toArray();
    }

    public int[] getParameterTypesArray() {
        return parameters.stream()
                .mapToInt(SqlParameterValue::getSqlType)
                .toArray();
    }

    public static CuSqlQuery of(CharSequence... sqlChunks) {
        return CuSqlChunk.of(sqlChunks).toQuery();
    }

}
