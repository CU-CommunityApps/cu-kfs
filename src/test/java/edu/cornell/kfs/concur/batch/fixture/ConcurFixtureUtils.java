package edu.cornell.kfs.concur.batch.fixture;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.concur.ConcurConstants;

/**
 * Helper class containing various utility methods to simplify Concur unit test setup.
 */
public class ConcurFixtureUtils {

    private static final DateTimeFormatter CONCUR_DATE_FORMATTER = DateTimeFormat.forPattern(ConcurConstants.DATE_FORMAT).withLocale(Locale.US);

    /**
     * Returns all the constants of the given enum class that reference the given parent object.
     * 
     * @param fixtureClass The enum class to get the constants from.
     * @param parentFixture The enum constant that is referenced by some of the other enum's constants.
     * @param parentFixtureGetter A Function that takes a child enum constant and returns the reference to its parent object.
     * @return A List of all the enum class's constants that reference the parent object, or an empty List if no such constants exist.
     */
    public static <E extends Enum<E>,P extends Enum<P>> List<E> getFixturesContainingParentFixture(
            Class<E> fixtureClass, P parentFixture, Function<E,P> parentFixtureGetter) {
        return Arrays.stream(fixtureClass.getEnumConstants())
                .filter((fixture) -> parentFixtureGetter.apply(fixture) == parentFixture)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Takes a date string in "MM/dd/yyyy" format, and converts it into a SQL Date.
     */
    public static java.sql.Date toSqlDate(String dateString) {
        DateTime dateTime = DateTime.parse(dateString, CONCUR_DATE_FORMATTER);
        return new java.sql.Date(dateTime.getMillis());
    }

    public static DateTime toDateTimeAtMidnight(String dateString) {
        return CONCUR_DATE_FORMATTER.parseDateTime(dateString);
    }

    /**
     * Builds an immutable Map.Entry instance that can be used with the buildOverrideMap() static method.
     * It is meant to provide a convenient way for enum constants to use another enum constant as a base,
     * and then specify overrides of a few fields via this method.
     */
    public static <E extends Enum<E>,V> Map.Entry<E,V> buildOverride(E key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    /**
     * Builds an EnumMap using the provided Map.Entry instances.
     * It is meant to be used in conjunction with the buildOverride() static method above,
     * as a means of simplifying the setup of enum constants that contain a lot
     * of shared data but only have different values in a couple of fields.
     */
    @SafeVarargs
    public static <E extends Enum<E>,V> EnumMap<E,V> buildOverrideMap(
            Class<E> enumClass, Map.Entry<E,V>... overrides) {
        EnumMap<E,V> overrideMap = new EnumMap<>(enumClass);
        for (Map.Entry<E,V> override : overrides) {
            overrideMap.put(override.getKey(), override.getValue());
        }
        return overrideMap;
    }

}
