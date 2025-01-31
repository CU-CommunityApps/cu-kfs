package edu.cornell.kfs.tax.batch.service.impl;

import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;

import edu.cornell.kfs.tax.batch.metadata.TaxDtoFieldConverter;

public class TaxDtoFieldConverterOjbImpl implements TaxDtoFieldConverter {

    private final FieldConversion fieldConversion;

    public TaxDtoFieldConverterOjbImpl(final FieldConversion fieldConversion) {
        Validate.notNull(fieldConversion, "fieldConversion cannot be null");
        this.fieldConversion = fieldConversion;
    }

    @Override
    public Object convertToJavaValue(final Object sqlValue) {
        return fieldConversion.sqlToJava(sqlValue);
    }

    @Override
    public Object convertToSqlValue(final Object javaValue) {
        return fieldConversion.javaToSql(javaValue);
    }

}
