package edu.cornell.kfs.sys.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Convenience meta-annotation that marks another annotation as only being intended for code documentation purposes.
 * This annotation is not used for processing; it's only for assisting with code documentation.
 */
@Target(ElementType.ANNOTATION_TYPE)
public @interface ForCodeDocumentationOnly {

}
