package edu.cornell.kfs.sys.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class CuSqlQuery {
    private static final Logger LOG = LogManager.getLogger();
    private static final String PARAMETER_MESSAGE_FORMAT = "(Type: {0}, Value: {1})";
    private static final String DERIVED_PARAMETER_LOG_PLACEHOLDER = "[ Derived Value ]";

    private final String queryString;
    private final List<SqlParameterValue> parameters;
    private final boolean forBatchExecution;

    public CuSqlQuery(String queryString, Stream<SqlParameterValue> parameters) {
        if (StringUtils.isBlank(queryString)) {
            throw new IllegalArgumentException("queryString cannot be blank");
        }
        Objects.requireNonNull(parameters, "parameters cannot be null");
        this.queryString = queryString;
        this.parameters = parameters.collect(Collectors.toUnmodifiableList());
        this.forBatchExecution = this.parameters.stream()
                .anyMatch(parameter -> parameter.getValue() instanceof Function);
    }

    public boolean isForBatchExecution() {
        return forBatchExecution;
    }

    public String getQueryString() {
        return queryString;
    }

    public List<SqlParameterValue> getParameters() {
        if (forBatchExecution) {
            throw new IllegalStateException(
                    "Query is meant for batch execution; use the one-arg getParameters() variant instead");
        }
        return parameters;
    }

    public <T> List<SqlParameterValue> getParameters(final T currentItemInBatch) {
        if (!forBatchExecution) {
            throw new IllegalStateException(
                    "Query is not meant for batch execution; use the zero-arg getParameters() variant instead");
        }
        return parameters.stream()
                .map(parameter -> convertParameterForBatch(parameter, currentItemInBatch))
                .collect(Collectors.toUnmodifiableList());
    }

    @SuppressWarnings("unchecked")
    private <T> SqlParameterValue convertParameterForBatch(
            final SqlParameterValue parameter, final T currentItemInBatch) {
        if (!(parameter.getValue() instanceof Function)) {
            return parameter;
        }
        final Function<? super T, ?> valueGetter = (Function<? super T, ?>) parameter.getValue();
        final Object value = valueGetter.apply(currentItemInBatch);
        final Integer scale = parameter.getScale();
        return (scale != null)
                ? new SqlParameterValue(parameter.getSqlType(), scale, value)
                : new SqlParameterValue(parameter.getSqlType(), value);
    }

    public <T> List<SqlParameterValue> getParametersForLogging() {
        if (forBatchExecution) {
            return parameters.stream()
                    .map(this::convertParameterForLogging)
                    .collect(Collectors.toUnmodifiableList());
        } else {
            return parameters;
        }
    }

    private SqlParameterValue convertParameterForLogging(final SqlParameterValue parameter) {
        if (!(parameter.getValue() instanceof Function)) {
            return parameter;
        }
        final Integer scale = parameter.getScale();
        return (scale != null)
                ? new SqlParameterValue(parameter.getSqlType(), scale, DERIVED_PARAMETER_LOG_PLACEHOLDER)
                : new SqlParameterValue(parameter.getSqlType(), DERIVED_PARAMETER_LOG_PLACEHOLDER);
    }

    public Object[] getParametersArray() {
        return getParameters().toArray();
    }

    public Object[] getParameterValuesArray() {
        return getParameters().stream()
                .map(SqlParameterValue::getValue)
                .toArray();
    }

    public int[] getParameterTypesArray() {
        return getParameters().stream()
                .mapToInt(SqlParameterValue::getSqlType)
                .toArray();
    }

    public static CuSqlQuery of(CharSequence... sqlChunks) {
        return CuSqlChunk.of(sqlChunks).toQuery();
    }

    public void logSQL() {
        logSQL(true);
    }

    public void logSQL(boolean logParameters) {
        LOG.info("logSQL, queryString: " + getQueryString());
        if (logParameters) {
            LOG.info("logSQL, parameters: " + buildParametersMessage());
        }
    }

    private String buildParametersMessage() {
        return getParametersForLogging().stream()
                .map(this::buildMessageForSingleParameter)
                .collect(Collectors.joining(CUKFSConstants.COMMA_AND_SPACE));
    }

    private String buildMessageForSingleParameter(SqlParameterValue parameter) {
        return MessageFormat.format(PARAMETER_MESSAGE_FORMAT, parameter.getSqlType(), parameter.getValue());
    }

}
