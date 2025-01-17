package edu.cornell.kfs.tax.batch.dto.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableBiFunction;

public final class TaxDtoFieldExtractor<T, U> {

    private final Class<T> dtoClass;
    private final String columnName;
    private final String propertyName;
    private final FailableBiFunction<ResultSet, String, U, SQLException> columnValueExtractor;
    private final BiConsumer<T, U> businessObjectPropertySetter;

    public TaxDtoFieldExtractor(final Class<T> dtoClass, final String columnName, final String propertyName,
            final FailableBiFunction<ResultSet, String, U, SQLException> columnValueExtractor,
            final BiConsumer<T, U> businessObjectPropertySetter) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notBlank(columnName, "columnName cannot be blank");
        Validate.notBlank(propertyName, "propertyName cannot be blank");
        Validate.notNull(columnValueExtractor, "columnValueExtractor cannot be null");
        Validate.notNull(businessObjectPropertySetter, "businessObjectPropertySetter cannot be null");
        
        this.dtoClass = dtoClass;
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.columnValueExtractor = columnValueExtractor;
        this.businessObjectPropertySetter = businessObjectPropertySetter;
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

    public FailableBiFunction<ResultSet, String, U, SQLException> getColumnValueExtractor() {
        return columnValueExtractor;
    }

    public BiConsumer<T, U> getBusinessObjectPropertySetter() {
        return businessObjectPropertySetter;
    }

}
