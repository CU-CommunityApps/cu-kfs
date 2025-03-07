package edu.cornell.kfs.sys.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class FixtureUtils {

    public static <A extends Annotation> Arguments createNamedAnnotationFixtureArgument(
            final Enum<?> enumConstant, final Class<A> annotationType) {
        return Arguments.of(createNamedAnnotationFixturePayload(enumConstant, annotationType));
    }

    public static <A extends Annotation> Named<A> createNamedAnnotationFixturePayload(
            final Enum<?> enumConstant, final Class<A> annotationType) {
        final A annotationBasedFixture = getAnnotationBasedFixture(enumConstant, annotationType);
        return Named.of(enumConstant.name(), annotationBasedFixture);
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

    @SafeVarargs
    public static <A extends Annotation, T> List<T> convertFixtures(
            final Function<A, T> converter, final A... annotationBasedFixtures) {
        return Stream.of(annotationBasedFixtures)
                .map(converter)
                .collect(Collectors.toUnmodifiableList());
    }

    public static String convertToNullIfEqualToTheWordNull(final String value) {
        return StringUtils.equalsIgnoreCase(value, CUKFSConstants.NULL) ? null : value;
    }

}
