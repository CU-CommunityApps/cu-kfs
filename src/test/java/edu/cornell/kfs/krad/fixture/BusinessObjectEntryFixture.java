package edu.cornell.kfs.krad.fixture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.krad.bo.BusinessObject;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BusinessObjectEntryFixture {

    Class<? extends BusinessObject> businessObjectClass();
    AttributeDefinitionFixture[] attributes();

}
