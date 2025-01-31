package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxDtoMappingDefinitionFixture {

    Class<?> dtoClass();

    TaxDtoTableDefinitionFixture[] businessObjectMappings();

    TaxDtoFieldDefinitionFixture[] fieldMappings();

}
