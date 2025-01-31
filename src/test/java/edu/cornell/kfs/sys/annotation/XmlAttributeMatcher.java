package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.sys.KFSConstants;

/**
 * This annotation is used in conjunction with CuXmlFilterer to filter the contents of an XML document.
 * 
 * NOTE: When specifying this annotation, fill in either the values() property or the regex() property, but not both.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface XmlAttributeMatcher {

    String name();

    String namespaceURI() default KFSConstants.EMPTY_STRING;

    String[] values() default {};

    String regex() default KFSConstants.EMPTY_STRING;

}
