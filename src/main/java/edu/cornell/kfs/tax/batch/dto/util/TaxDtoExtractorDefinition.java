package edu.cornell.kfs.tax.batch.dto.util;

import java.util.List;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.tax.batch.annotation.ExtractionSource;

public final class TaxDtoExtractorDefinition<T> {

    private final Class<T> dtoClass;
    private final List<ExtractionSource> sourceBusinessObjects;
    private final List<TaxDtoFieldExtractor<T, ?>> fieldExtractors;

    public TaxDtoExtractorDefinition(final Class<T> dtoClass, final ExtractionSource[] sourceBusinessObjects,
            final List<TaxDtoFieldExtractor<T, ?>> fieldExtractors) {
        Validate.notNull(dtoClass, "dtoClass cannot be null");
        Validate.notNull(sourceBusinessObjects, "sourceBusinessObjects cannot be null");
        Validate.notNull(fieldExtractors, "fieldExtractors cannot be null");

        this.dtoClass = dtoClass;
        this.sourceBusinessObjects = List.of(sourceBusinessObjects);
        this.fieldExtractors = List.copyOf(fieldExtractors);
    }

    public Class<T> getDtoClass() {
        return dtoClass;
    }

    public List<ExtractionSource> getSourceBusinessObjects() {
        return sourceBusinessObjects;
    }

    public List<TaxDtoFieldExtractor<T, ?>> getFieldExtractors() {
        return fieldExtractors;
    }

}
