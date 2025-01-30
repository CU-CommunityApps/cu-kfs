package edu.cornell.kfs.tax.batch.metadata.fixture;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface TaxDtoTableDefinitionFixture {

    Class<? extends BusinessObject> businessObjectClass();

    String tableName();

    String tableAliasForQuery() default KFSConstants.EMPTY_STRING;

}
