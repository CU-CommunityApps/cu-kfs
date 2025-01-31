package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used in conjunction with CuXmlFilterer to filter the contents of an XML document.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface XmlDocumentFilter {

    String rootElementName();

    XmlElementFilter[] directChildElementsToKeep();

}
