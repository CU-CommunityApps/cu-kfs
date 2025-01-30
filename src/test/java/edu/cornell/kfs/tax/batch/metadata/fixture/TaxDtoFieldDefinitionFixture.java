package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TaxDtoFieldDefinitionFixture {

    String propertyName();

    Class<?> fieldClass();

    JDBCType jdbcType();

    String columnLabel();

    boolean hasFieldConverter() default false;

}
