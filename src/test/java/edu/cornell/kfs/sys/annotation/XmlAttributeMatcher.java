package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.kuali.kfs.sys.KFSConstants;

/**
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
