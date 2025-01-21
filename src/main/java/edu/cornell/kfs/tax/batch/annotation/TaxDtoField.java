package edu.cornell.kfs.tax.batch.annotation;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

public @interface TaxDtoField {

    Class<? extends BusinessObject> mappedBusinessObject() default BusinessObject.class;

    String actualBOField() default KFSConstants.EMPTY_STRING;

    boolean updatable() default false;

}
