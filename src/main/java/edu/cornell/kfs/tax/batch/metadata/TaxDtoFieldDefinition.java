package edu.cornell.kfs.tax.batch.metadata;

import java.sql.JDBCType;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

public final class TaxDtoFieldDefinition<T, U> extends TaxDtoFieldAccessorBase<T, U> {

    private final boolean updatable;
    private final JDBCType jdbcType;
    private final String columnLabel;
    private final Optional<TaxDtoFieldConverter> fieldConverter;

    public TaxDtoFieldDefinition(final Class<T> dtoClass, final Class<U> fieldClass, final String propertyName,
            final Function<T, U> propertyGetter, final BiConsumer<T, U> propertySetter, final boolean updatable,
            final JDBCType jdbcType, final String columnLabel, final Optional<TaxDtoFieldConverter> fieldConverter) {
        super(dtoClass, fieldClass, propertyName, propertyGetter, propertySetter);
        Validate.notNull(jdbcType, "jdbcType cannot be null");
        Validate.notBlank(columnLabel, "columnLabel cannot be blank");
        Validate.notNull(fieldConverter, "fieldConverter wrapper cannot be null");
        this.updatable = updatable;
        this.jdbcType = jdbcType;
        this.columnLabel = columnLabel;
        this.fieldConverter = fieldConverter;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public TaxDtoFieldConverter getFieldConverter() {
        return fieldConverter.get();
    }

    public boolean hasFieldConverter() {
        return fieldConverter.isPresent();
    }

}
