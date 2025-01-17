package edu.cornell.kfs.tax.batch.dto.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.sys.util.CuFailableTriConsumer;

public class TaxDtoFieldUpdater<T, U> {

    private final Class<T> dtoClass;
    private final String columnName;
    private final String propertyName;
    private final CuFailableTriConsumer<ResultSet, String, U, SQLException> columnValueUpdater;
    private final Function<T, U> businessObjectPropertyGetter;

    public TaxDtoFieldUpdater(final Class<T> dtoClass, final String columnName, final String propertyName,
            final CuFailableTriConsumer<ResultSet, String, U, SQLException> columnValueUpdater,
            final Function<T, U> businessObjectPropertyGetter) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notBlank(columnName, "columnName cannot be blank");
        Validate.notBlank(propertyName, "propertyName cannot be blank");
        Validate.notNull(columnValueUpdater, "columnValueUpdater cannot be null");
        Validate.notNull(businessObjectPropertyGetter, "businessObjectPropertyGetter cannot be null");

        this.dtoClass = dtoClass;
        this.columnName = columnName;
        this.propertyName = propertyName;
        this.columnValueUpdater = columnValueUpdater;
        this.businessObjectPropertyGetter = businessObjectPropertyGetter;
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

    public CuFailableTriConsumer<ResultSet, String, U, SQLException> getColumnValueUpdater() {
        return columnValueUpdater;
    }

    public Function<T, U> getBusinessObjectPropertyGetter() {
        return businessObjectPropertyGetter;
    }

}
