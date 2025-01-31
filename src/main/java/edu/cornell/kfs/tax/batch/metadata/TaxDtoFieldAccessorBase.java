package edu.cornell.kfs.tax.batch.metadata;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

public abstract class TaxDtoFieldAccessorBase<T, U> {

    private final Class<T> dtoClass;
    private final Class<U> fieldClass;
    private final String propertyName;
    private final Function<T, U> propertyGetter;
    private final BiConsumer<T, U> propertySetter;

    protected TaxDtoFieldAccessorBase(final Class<T> dtoClass, final Class<U> fieldClass, final String propertyName,
            final Function<T, U> propertyGetter, final BiConsumer<T, U> propertySetter) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notNull(fieldClass, "fieldClass cannot be null");
        Validate.notBlank(propertyName, "propertyName cannot be blank");
        Validate.notNull(propertyGetter, "propertyGetter cannot be null");
        Validate.notNull(propertySetter, "propertySetter cannot be null");

        this.dtoClass = dtoClass;
        this.fieldClass = fieldClass;
        this.propertyName = propertyName;
        this.propertyGetter = propertyGetter;
        this.propertySetter = propertySetter;
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }

    public Class<U> getFieldClass() {
        return fieldClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Function<T, U> getPropertyGetter() {
        return propertyGetter;
    }

    public BiConsumer<T, U> getPropertySetter() {
        return propertySetter;
    }

}
