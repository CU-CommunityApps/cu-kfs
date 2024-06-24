package edu.cornell.kfs.sys.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

public final class FixtureUtils {

    public static <A extends Annotation> Arguments createNamedAnnotationFixtureArgument(
            final Enum<?> enumConstant, final Class<A> annotationType) {
        final A annotationBasedFixture = getAnnotationBasedFixture(enumConstant, annotationType);
        return Arguments.of(Named.of(enumConstant.name(), annotationBasedFixture));
    }

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
