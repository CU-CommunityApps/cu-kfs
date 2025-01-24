package edu.cornell.kfs.tax.batch.metadata;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

public final class TaxDtoMappingDefinition<T> {

    private final Class<T> dtoClass;
    private final Supplier<T> dtoConstructor;
    private final List<TaxDtoTableDefinition> businessObjectMappings;
    private final List<TaxDtoFieldDefinition<T, ?>> fieldMappings;

    public TaxDtoMappingDefinition(final Class<T> dtoClass, final Supplier<T> dtoConstructor,
            final List<TaxDtoTableDefinition> businessObjectMappings,
            final List<TaxDtoFieldDefinition<T, ?>> fieldMappings) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notNull(dtoConstructor, "dtoConstructor cannot be null");
        Validate.notEmpty(businessObjectMappings, "businessObjectMappings cannot be null or empty");
        Validate.notEmpty(fieldMappings, "fieldMappings cannot be null or empty");

        this.dtoClass = dtoClass;
        this.dtoConstructor = dtoConstructor;
        this.businessObjectMappings = List.copyOf(businessObjectMappings);
        this.fieldMappings = List.copyOf(fieldMappings);
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }

    public Supplier<T> getDtoConstructor() {
        return dtoConstructor;
    }

    public List<TaxDtoTableDefinition> getBusinessObjectMappings() {
        return businessObjectMappings;
    }

    public List<TaxDtoFieldDefinition<T, ?>> getFieldMappings() {
        return fieldMappings;
    }

}
