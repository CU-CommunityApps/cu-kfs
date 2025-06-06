package edu.cornell.kfs.sys.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Convenience class for quickly converting simple strings into date-related objects.
 * Methods that return java.util.Date or java.sql.Date will use the system's default time-zone.
 * 
 * The following date formats are supported (but date-only method calls will discard
 * the time-of-day portion):
 * 
 * yyyy-MM-dd
 * yyyy-MM-ddTHH:mm:ss (with a literal "T")
 * yyyy-MM-dd HH:mm:ss
 */
public final class TestDateUtils {

    private static final int FORMAT_LENGTH_YYYY_MM_DD = 10;
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD =
            DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.US);
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_T_HH_MM_SS =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.US);
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_HH_MM_SS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static java.util.Date toUtilDate(final String value) {
        return java.util.Date.from(
                toLocalDateTime(value)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    public static LocalDateTime toLocalDateTime(final String value) {
        Validate.notBlank(value, "value cannot be blank");
        if (value.length() > FORMAT_LENGTH_YYYY_MM_DD) {
            return parseDateTime(value);
        } else {
            return LocalDateTime.of(parseDate(value), LocalTime.of(0, 0, 0));
        }
    }

    public static java.sql.Date toSqlDate(final String value) {
        return java.sql.Date.valueOf(toLocalDate(value));
    }

    public static LocalDate toLocalDate(final String value) {
        Validate.notBlank(value, "value cannot be blank");
        if (value.length() <= FORMAT_LENGTH_YYYY_MM_DD) {
            return parseDate(value);
        } else {
            return parseDateTime(value).toLocalDate();
        }
    }

    private static LocalDateTime parseDateTime(final String value) {
        return StringUtils.contains(value, 'T')
                ? LocalDateTime.parse(value, FORMATTER_YYYY_MM_DD_T_HH_MM_SS)
                : LocalDateTime.parse(value, FORMATTER_YYYY_MM_DD_HH_MM_SS);
    }

    private static LocalDate parseDate(final String value) {
        return LocalDate.parse(value, FORMATTER_YYYY_MM_DD);
    }

}
