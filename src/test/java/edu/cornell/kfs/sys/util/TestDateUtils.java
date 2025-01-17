package edu.cornell.kfs.sys.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.Validate;

/**
 * Convenience class for quickly converting simple strings into date-related objects.
 * Methods that return java.util.Date or java.sql.Date will use the system's default time-zone.
 * 
 * The following date formats are supported (but formats containing both date and time
 * are not permitted in the date-only method calls):
 * 
 * yyyy-MM-dd
 * yyyy-MM-ddTHH:mm:ss (with a literal "T")
 */
public final class TestDateUtils {

    private static final int FORMAT_LENGTH_YYYY_MM_DD = 10;
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD =
            DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.US);
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD_T_HH_MM_SS =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.US);

    public static java.util.Date toUtilDate(final String value) {
        return java.util.Date.from(
                toLocalDateTime(value)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    public static LocalDateTime toLocalDateTime(final String value) {
        Validate.notBlank(value, "value cannot be blank");
        if (value.length() > FORMAT_LENGTH_YYYY_MM_DD) {
            return LocalDateTime.parse(value, FORMATTER_YYYY_MM_DD_T_HH_MM_SS);
        } else {
            return LocalDateTime.parse(value, FORMATTER_YYYY_MM_DD);
        }
    }

    public static java.sql.Date toSqlDate(final String value) {
        return java.sql.Date.valueOf(toLocalDate(value));
    }

    public static LocalDate toLocalDate(final String value) {
        Validate.notBlank(value, "value cannot be blank");
        return LocalDate.parse(value, FORMATTER_YYYY_MM_DD);
    }

}
