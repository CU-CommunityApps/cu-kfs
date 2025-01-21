package edu.cornell.kfs.tax.batch.annotation;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

public @interface TaxBusinessObjectMapping {

    Class<? extends BusinessObject> businessObjectClass();

    String tableAliasForQuery() default KFSConstants.EMPTY_STRING;

}
