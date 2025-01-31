package edu.cornell.kfs.tax.batch.metadata;

public interface TaxDtoFieldConverter {

    Object convertToJavaValue(final Object sqlValue);

    Object convertToSqlValue(final Object javaValue);

}
