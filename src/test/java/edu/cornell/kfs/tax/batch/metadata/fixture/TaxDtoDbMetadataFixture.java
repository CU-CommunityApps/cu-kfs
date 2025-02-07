package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxDtoDbMetadataFixture {

    Class<? extends TaxDtoFieldEnum> fieldEnumClass();

    TaxTableFixture[] mappedTables();

    TaxFieldFixture[] mappedFields();

}
