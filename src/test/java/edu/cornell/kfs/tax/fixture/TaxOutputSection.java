package edu.cornell.kfs.tax.fixture;

public @interface TaxOutputSection {

    String name();

    boolean hasHeaderRow() default false;

    boolean useExactFieldLengths() default false;

    TaxOutputField[] fields();

}
