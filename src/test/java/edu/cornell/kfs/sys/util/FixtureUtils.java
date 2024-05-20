package edu.cornell.kfs.sys.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

public final class FixtureUtils {

    public static <A extends Annotation> A getAnnotationBasedFixture(
            final Enum<?> enumConstant, final Class<A> annotationType) {
        try {
            final Class<?> enumClass = enumConstant.getClass();
            final Field enumField = enumClass.getField(enumConstant.name());
            final A annotationBasedFixture = enumField.getAnnotation(annotationType);
            Objects.requireNonNull(annotationBasedFixture,
                    "Expected annotation-based fixture was not present on the enum constant");
            return annotationBasedFixture;
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
