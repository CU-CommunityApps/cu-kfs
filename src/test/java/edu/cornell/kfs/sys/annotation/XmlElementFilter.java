package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface XmlElementFilter {

    String name();

    XmlAttributeMatcher[] matchConditions();

}
