package edu.cornell.kfs.tax.fixture;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxOutputFieldType;

public @interface TaxOutputField {

    String name();

    int length();

    TaxOutputFieldType type();

    String key() default KFSConstants.EMPTY_STRING;

    String value() default KFSConstants.EMPTY_STRING;

}
