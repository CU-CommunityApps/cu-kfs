package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in conjunction with CuXmlFilterer to filter the contents of an XML document.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface XmlElementFilter {

    String name();

    XmlAttributeMatcher[] matchConditions();

}
