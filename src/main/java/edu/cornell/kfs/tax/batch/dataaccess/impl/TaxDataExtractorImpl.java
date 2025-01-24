package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDataExtractor;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldConverter;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldDefinition;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoMappingDefinition;

public class TaxDataExtractorImpl<T> implements TaxDataExtractor<T> {

    /*
     * This Map contains a subset of the JDBC type mappings listed in Oracle's Java documentation:
     * 
     * https://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
     */
    private static final Map<JDBCType, Class<?>> DEFAULT_DATA_TYPE_MAPPINGS = Map.ofEntries(
            Map.entry(JDBCType.CHAR, String.class),
            Map.entry(JDBCType.VARCHAR, String.class),
            Map.entry(JDBCType.LONGVARCHAR, String.class),
            Map.entry(JDBCType.NUMERIC, BigDecimal.class),
            Map.entry(JDBCType.DECIMAL, BigDecimal.class),
            Map.entry(JDBCType.BIT, Boolean.class),
            Map.entry(JDBCType.TINYINT, Byte.class),
            Map.entry(JDBCType.SMALLINT, Short.class),
            Map.entry(JDBCType.INTEGER, Integer.class),
            Map.entry(JDBCType.BIGINT, Long.class),
            Map.entry(JDBCType.REAL, Float.class),
            Map.entry(JDBCType.FLOAT, Double.class),
            Map.entry(JDBCType.DOUBLE, Double.class),
            Map.entry(JDBCType.BINARY, byte[].class),
            Map.entry(JDBCType.VARBINARY, byte[].class),
            Map.entry(JDBCType.LONGVARBINARY, byte[].class),
            Map.entry(JDBCType.DATE, java.sql.Date.class),
            Map.entry(JDBCType.TIME, java.sql.Time.class),
            Map.entry(JDBCType.TIMESTAMP, java.sql.Timestamp.class),
            Map.entry(JDBCType.CLOB, java.sql.Clob.class),
            Map.entry(JDBCType.BLOB, java.sql.Blob.class)
    );

    private final TaxDtoMappingDefinition<T> dtoDefinition;
    private final ResultSet resultSet;

    public TaxDataExtractorImpl(final TaxDtoMappingDefinition<T> dtoDefinition, final ResultSet resultSet) {
        Validate.notNull(dtoDefinition, "dtoDefinition cannot be null");
        Validate.notNull(resultSet, "resultSet cannot be null");
        this.dtoDefinition = dtoDefinition;
        this.resultSet = resultSet;
    }

    protected ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public boolean moveToNextRow() throws SQLException {
        return resultSet.next();
    }

    @Override
    public T getCurrentRow() throws SQLException {
        final T currentDto = dtoDefinition.getDtoConstructor().get();
        for (final TaxDtoFieldDefinition<T, ?> fieldMapping : dtoDefinition.getFieldMappings()) {
            populateFieldOnDto(currentDto, fieldMapping);
        }
        return currentDto;
    }

    private <U> void populateFieldOnDto(final T currentDto, final TaxDtoFieldDefinition<T, U> fieldMapping)
            throws SQLException {
        final U fieldValue;

        if (fieldMapping.hasFieldConverter()) {
            final TaxDtoFieldConverter fieldConverter = fieldMapping.getFieldConverter();
            final Class<?> defaultFieldType = DEFAULT_DATA_TYPE_MAPPINGS.getOrDefault(
                    fieldMapping.getJdbcType(), String.class);
            final Object retrievedValue = resultSet.getObject(fieldMapping.getColumnLabel(), defaultFieldType);
            final Object convertedValue = fieldConverter.convertToJavaValue(retrievedValue);
            fieldValue = fieldMapping.getFieldClass().cast(convertedValue);
        } else {
            fieldValue = resultSet.getObject(fieldMapping.getColumnLabel(), fieldMapping.getFieldClass());
        }

        fieldMapping.getPropertySetter().accept(currentDto, fieldValue);
    }

}
