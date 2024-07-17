package edu.cornell.kfs.sys.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a method as being used for Spring bean factory purposes. This annotation is currently only used
 * for clarifying the intent of certain unit test methods; it's not actually being used for processing.
 */
@Target(ElementType.METHOD)
public @interface SpringXmlTestBeanFactoryMethod {

}
