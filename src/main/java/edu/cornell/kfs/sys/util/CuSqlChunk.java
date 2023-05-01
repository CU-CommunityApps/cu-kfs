package edu.cornell.kfs.sys.util;

import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.jdbc.core.SqlParameterValue;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class CuSqlChunk implements CharSequence {

    public static final int MAX_IN_LIST_SIZE = 1000;

    private StringBuilder queryString;
    private Stream.Builder<SqlParameterValue> parameters;

    public CuSqlChunk() {
        this.queryString = new StringBuilder(100);
        this.parameters = Stream.builder();
    }

    public CuSqlChunk(SqlParameterValue parameter) {
        Objects.requireNonNull(parameter, "parameter cannot be null");
        this.queryString = new StringBuilder(1);
        this.parameters = Stream.builder();
        append(parameter);
    }

    public CuSqlChunk appendAsParameter(String value) {
        return append(new SqlParameterValue(Types.VARCHAR, value));
    }

    public CuSqlChunk appendAsParameter(int sqlType, Object value) {
        return append(new SqlParameterValue(sqlType, value));
    }

    public CuSqlChunk append(SqlParameterValue parameter) {
        Objects.requireNonNull(parameter, "parameter cannot be null");
        verifyState();
        queryString.append(KFSConstants.QUESTION_MARK);
        parameters.add(parameter);
        return this;
    }

    public CuSqlChunk append(CharSequence sqlChunk) {
        if (sqlChunk instanceof CuSqlChunk) {
            return append((CuSqlChunk) sqlChunk);
        } else {
            verifyState();
            queryString.append(sqlChunk);
            return this;
        }
    }

    public CuSqlChunk append(CuSqlChunk sqlChunk) {
        Objects.requireNonNull(sqlChunk, "sqlChunk cannot be null");
        verifyState();
        sqlChunk.verifyState();
        queryString.append(sqlChunk.queryString);
        Iterator<SqlParameterValue> parametersIterator = sqlChunk.parameters.build().iterator();
        while (parametersIterator.hasNext()) {
            parameters.add(parametersIterator.next());
        }
        sqlChunk.switchToUnmodifiableState();
        return this;
    }
    
    public CuSqlChunk append(CharSequence... subChunks) {
        for (CharSequence subChunk : subChunks) {
            append(subChunk);
        }
        return this;
    }

    public CuSqlQuery toQuery() {
        verifyState();
        CuSqlQuery sqlQuery = new CuSqlQuery(queryString.toString(), parameters.build());
        switchToUnmodifiableState();
        return sqlQuery;
    }

    private void switchToUnmodifiableState() {
        queryString = null;
        parameters = null;
    }

    private void verifyState() {
        if (!isWriteable()) {
            throw new IllegalStateException("Cannot perform operation; SQL chunk has already been converted "
                    + "to a query or has already been appended to another chunk");
        }
    }

    @Override
    public int length() {
        return isWriteable() ? queryString.length() : 0;
    }

    @Override
    public char charAt(int index) {
        verifyState();
        return queryString.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        verifyState();
        return queryString.subSequence(start, end);
    }

    @Override
    public String toString() {
        return isWriteable() ? queryString.toString() : KFSConstants.EMPTY_STRING;
    }

    public boolean isWriteable() {
        return parameters != null;
    }

    public static CuSqlChunk of(CharSequence... subChunks) {
        CuSqlChunk sqlChunk = new CuSqlChunk();
        for (CharSequence subChunk : subChunks) {
            sqlChunk.append(subChunk);
        }
        return sqlChunk;
    }

    public static CuSqlChunk forParameter(String value) {
        return forParameter(Types.VARCHAR, value);
    }

    public static CuSqlChunk forParameter(int sqlType, Object value) {
        SqlParameterValue parameter = new SqlParameterValue(sqlType, value);
        return new CuSqlChunk(parameter);
    }

    public static CuSqlChunk forStringParameters(Collection<?> values) {
        return forParameters(Types.VARCHAR, values);
    }

    public static CuSqlChunk forParameters(int sqlType, Collection<?> values) {
        CuSqlChunk sqlChunk = new CuSqlChunk();
        Iterator<?> valuesIterator = values.iterator();
        if (!valuesIterator.hasNext()) {
            throw new IllegalArgumentException("values collection cannot be empty");
        }
        sqlChunk.appendAsParameter(sqlType, valuesIterator.next());
        while (valuesIterator.hasNext()) {
            sqlChunk.append(CUKFSConstants.COMMA_AND_SPACE)
                    .appendAsParameter(sqlType, valuesIterator.next());
        }
        return sqlChunk;
    }

    public static CuSqlChunk asSqlInCondition(String columnName, Collection<? extends String> values) {
        return asSqlInCondition(columnName, Types.VARCHAR, values, true);
    }

    public static CuSqlChunk asSqlNotInCondition(String columnName, Collection<? extends String> values) {
        return asSqlInCondition(columnName, Types.VARCHAR, values, false);
    }

    public static CuSqlChunk asSqlInCondition(String columnName, int sqlType, Collection<?> values) {
        return asSqlInCondition(columnName, sqlType, values, true);
    }

    public static CuSqlChunk asSqlNotInCondition(String columnName, int sqlType, Collection<?> values) {
        return asSqlInCondition(columnName, sqlType, values, false);
    }

    private static CuSqlChunk asSqlInCondition(
            String columnName, int sqlType, Collection<?> values, boolean positive) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException("values collection cannot be null or empty");
        } else if (StringUtils.isBlank(columnName)) {
            throw new IllegalArgumentException("columnName cannot be blank");
        }

        CuSqlChunk fullCondition = new CuSqlChunk();
        Iterator<?> valuesIterator = values.iterator();
        if (!valuesIterator.hasNext()) {
            throw new IllegalStateException("values collection was non-empty but returned an empty iterator");
        } 

        String inCondition = positive ? " IN " : " NOT IN ";
        String subListCombiner = positive ? " OR " : " AND ";
        int numSubLists = 1;

        do {
            fullCondition.append(columnName, inCondition, CUKFSConstants.LEFT_PARENTHESIS)
                    .appendAsParameter(sqlType, valuesIterator.next());
            int listChunkLength = 1;
            
            while (valuesIterator.hasNext() && listChunkLength < MAX_IN_LIST_SIZE) {
                fullCondition.append(CUKFSConstants.COMMA_AND_SPACE)
                        .appendAsParameter(sqlType, valuesIterator.next());
                listChunkLength++;
            }
            
            fullCondition.append(CUKFSConstants.RIGHT_PARENTHESIS);
            if (listChunkLength == MAX_IN_LIST_SIZE && valuesIterator.hasNext()) {
                fullCondition.append(subListCombiner);
                numSubLists++;
            }
        } while (valuesIterator.hasNext());
        
        if (numSubLists > 1) {
            return CuSqlChunk.of(CUKFSConstants.LEFT_PARENTHESIS, fullCondition, CUKFSConstants.RIGHT_PARENTHESIS);
        } else {
            return fullCondition;
        }
    }

}
