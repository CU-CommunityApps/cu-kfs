package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccountingXmlDocumentFixtureUtils {

    public static <T> List<T> toImmutableList(T[] values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    public static <E extends Enum<E>, T> List<T> convertToPojoList(List<E> fixtures, Function<E, T> fixtureToPojoConverter) {
        return fixtures.stream()
                .map(fixtureToPojoConverter)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
