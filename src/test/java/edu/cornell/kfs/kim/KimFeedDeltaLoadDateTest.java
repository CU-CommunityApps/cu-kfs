package edu.cornell.kfs.kim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class KimFeedDeltaLoadDateTest {

    private static final DateTimeFormatter DATE_ZONE_DEFAULT_FORMATTER_MM_dd_yyyy = DateTimeFormatter.ofPattern(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
    private String testingDeltaLoadDate;
    private static LocalDateTime localDateTimeNow = LocalDateTime.now();
    private static LocalDate localDateNow = localDateTimeNow.toLocalDate();
    private static LocalTime localTimeStartOfDay = LocalTime.MIDNIGHT;
    private static LocalTime localTimeEndOfDay = LocalTime.MIDNIGHT.minusSeconds(1).plusNanos(999999999);
    private static String localDayNowAsString = localDateTimeNow.format(DATE_ZONE_DEFAULT_FORMATTER_MM_dd_yyyy);

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @ParameterizedTest
    @MethodSource("provideForTestVerifyTimstampFromDateAsStringList")
    public void testStringConvertsToTimestamp(String dateAsString, LocalDate expectedLocalDate, 
            Timestamp expectedStartOfDayTimestamp, Timestamp expectedEndOfDayTimestamp) throws Exception {
        testingDeltaLoadDate = dateAsString;
        LocalDate generatedLocalDate = getParsedDeltaLoadDate();
        assertEquals(generatedLocalDate, expectedLocalDate);
        Timestamp generatedStartOfDayTimestamp = getTimestampForStartOfDay(generatedLocalDate);
        assertEquals(generatedStartOfDayTimestamp, expectedStartOfDayTimestamp);
        Timestamp generatedEndOfDayTimestamp = getTimestampForEndOfDay(generatedLocalDate);
        assertEquals(generatedEndOfDayTimestamp, expectedEndOfDayTimestamp);
    }

    private static Stream<Arguments> provideForTestVerifyTimstampFromDateAsStringList() {
        return Stream.of(
            Arguments.of("02/28/2020", 
                LocalDate.of(2020, Month.FEBRUARY, 28), 
                Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2020, Month.FEBRUARY, 28), LocalTime.of(0, 0, 0, 0))),
                Timestamp.valueOf(LocalDateTime.of(LocalDate.of(2020, Month.FEBRUARY, 28), LocalTime.of(23, 59, 59, 999999999)))),
            Arguments.of(localDayNowAsString, 
                    localDateNow, 
                    Timestamp.valueOf(LocalDateTime.of(localDateNow, localTimeStartOfDay)),
                    Timestamp.valueOf(LocalDateTime.of(localDateNow,localTimeEndOfDay)))
        );
    }

    private String getDeltaLoadDate() {
        return testingDeltaLoadDate;
    }

    /*************************************************************************** 
     * Private methods from edu.cornell.kfs.kim.KimFeed.java that should be 
     * unit tested to ensure java date time libraries are functioning correctly.
     ***************************************************************************/

    private LocalDate getParsedDeltaLoadDate() {
        String deltaLoadDate = getDeltaLoadDate();
        if (ObjectUtils.isNull(deltaLoadDate) || StringUtils.isBlank(deltaLoadDate)) {
            return null;
        }
        LocalDate parsedDeltaLoadDateAsLocalDate = LocalDate.parse(deltaLoadDate, DATE_ZONE_DEFAULT_FORMATTER_MM_dd_yyyy);
        return parsedDeltaLoadDateAsLocalDate;
    }

    private Timestamp getTimestampForStartOfDay(LocalDate localDateToUse) {
        if (ObjectUtils.isNull(localDateToUse)) {
            return null;
        }
        Timestamp startOfDayAsTimestamp = Timestamp.valueOf(LocalDateTime.of(localDateToUse, LocalTime.of(0, 0, 0, 0)));
        return startOfDayAsTimestamp;
    }

    private Timestamp getTimestampForEndOfDay(LocalDate localDateToUse) {
        if (ObjectUtils.isNull(localDateToUse)) {
            return null;
        }
        Timestamp endOfDayTimestamp = Timestamp.valueOf(LocalDateTime.of(localDateToUse, LocalTime.of(23, 59, 59, 999999999)));
        return endOfDayTimestamp;
    }

}
