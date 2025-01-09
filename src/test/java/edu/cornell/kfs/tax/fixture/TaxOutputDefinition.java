package edu.cornell.kfs.tax.fixture;

public @interface TaxOutputDefinition {

    String fieldSeparator();

    TaxOutputSection[] sections();

}
