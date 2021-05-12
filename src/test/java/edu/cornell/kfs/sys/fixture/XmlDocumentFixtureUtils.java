package edu.cornell.kfs.sys.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class XmlDocumentFixtureUtils {

    public static String defaultToEmptyStringIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, StringUtils.EMPTY);
    }

    public static <T> List<T> toImmutableList(T[] values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public static <T, U> List<U> convertToPojoList(List<T> testObjects, Function<T, U> testObjectToPojoConverter) {
        return testObjects.stream()
                .map(testObjectToPojoConverter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
