package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TaxTableFixture {

    Class<? extends BusinessObject> businessObjectClass();

    String tableName();

    String tableAlias();

}
