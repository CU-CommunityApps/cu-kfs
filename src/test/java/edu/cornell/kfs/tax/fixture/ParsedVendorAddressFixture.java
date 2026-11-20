package edu.cornell.kfs.tax.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ParsedVendorAddressFixture {

    String rawLine1Address();
    String rawLine2Address();
    String expectedLine1();
    String expectedLine2();
    String expectedApartment();

}
