package edu.cornell.kfs.sys.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Helper annotation that specifies the Spring file to load
 * when running a unit test extending from SpringEnabledMicroTestBase.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LoadSpringFile {
    String value();
}
