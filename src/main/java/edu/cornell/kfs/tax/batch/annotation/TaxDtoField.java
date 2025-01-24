package edu.cornell.kfs.tax.batch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TaxDtoField {

    Class<? extends BusinessObject> mappedBusinessObject() default BusinessObject.class;

    String actualBOField() default KFSConstants.EMPTY_STRING;

}
