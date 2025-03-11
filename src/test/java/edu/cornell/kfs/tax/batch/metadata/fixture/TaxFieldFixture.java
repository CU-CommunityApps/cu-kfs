package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TaxFieldFixture {

    String key();

    String column();

    String alias();

}
