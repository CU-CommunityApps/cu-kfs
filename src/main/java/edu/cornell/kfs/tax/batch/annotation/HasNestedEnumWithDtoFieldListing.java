package edu.cornell.kfs.tax.batch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Convenience code-documentation-only annotation to help clarify that the marked tax DTO has a nested enum
 * that contains entries for each of the DTO's fields. The annotation itself is not actively used for processing.
 */
@Target(ElementType.TYPE)
public @interface HasNestedEnumWithDtoFieldListing {

}
