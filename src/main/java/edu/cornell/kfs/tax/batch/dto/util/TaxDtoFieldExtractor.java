package edu.cornell.kfs.tax.batch.dto.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableBiFunction;

public final class TaxDtoFieldExtractor<T, R> {

    private final Class<T> dtoClass;
    private final String columnName;
    private final String propertyName;
    private final FailableBiFunction<ResultSet, String, Object, SQLException> columnValueExtractor;
    private final Function<Object, R> propertyValueConverter;
    private final BiConsumer<T, R> businessObjectPropertySetter;

    public TaxDtoFieldExtractor(final Class<T> dtoClass, final String columnName, final String propertyName,
            final FailableBiFunction<ResultSet, String, Object, SQLException> columnValueExtractor,
            final Function<Object, R> propertyValueConverter, final BiConsumer<T, R> businessObjectPropertySetter) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notBlank(columnName, "columnName cannot be blank");
        Validate.notBlank(propertyName, "propertyName cannot be blank");
        Validate.notNull(columnValueExtractor, "columnValueExtractor cannot be null");
        Validate.notNull(propertyValueConverter, "propertyValueConverter cannot be null");
        Validate.notNull(businessObjectPropertySetter, "businessObjectPropertySetter cannot be null");
        
        this.dtoClass = dtoClass;
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.columnValueExtractor = columnValueExtractor;
        this.propertyValueConverter = propertyValueConverter;
        this.businessObjectPropertySetter = businessObjectPropertySetter;
    }

    public void populateFieldOnDto(final T dto, final ResultSet rs) throws SQLException {
        final Object columnValue = columnValueExtractor.apply(rs, columnName);
        final R dtoPropertyValue = propertyValueConverter.apply(columnValue);
        businessObjectPropertySetter.accept(dto, dtoPropertyValue);
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public FailableBiFunction<ResultSet, String, Object, SQLException> getColumnValueExtractor() {
        return columnValueExtractor;
    }

    public Function<Object, R> getPropertyValueConverter() {
        return propertyValueConverter;
    }

    public BiConsumer<T, R> getBusinessObjectPropertySetter() {
        return businessObjectPropertySetter;
    }

}
