package edu.cornell.kfs.tax.batch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UpdatableTaxDto {

    Class<? extends BusinessObject> relatedBusinessObject();

}
