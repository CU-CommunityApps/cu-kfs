package edu.cornell.kfs.tax.batch.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TaxBusinessObjectMapping {

    Class<? extends BusinessObject> businessObjectClass();

    String tableAliasForQuery() default KFSConstants.EMPTY_STRING;

}
