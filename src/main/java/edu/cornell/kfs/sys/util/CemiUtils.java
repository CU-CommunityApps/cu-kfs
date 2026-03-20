package edu.cornell.kfs.sys.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class CemiUtils {

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmss, Locale.US)
                .withZone(ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));
    
    private static final String generateDateTimeInConsistentFormat(final LocalDateTime dateTime) {
        return FILE_DATE_TIME_FORMATTER.format(dateTime);
    }

    public static final String generateFileNameContainingDateTime(
            final LocalDateTime dateTime, final String fileNamePrefix, final String fileExtension) {
        final String dateTimeString = generateDateTimeInConsistentFormat(dateTime);
        return StringUtils.join(fileNamePrefix, dateTimeString, fileExtension);
    }

    public static final String convertToBooleanValueForFileExtract(final boolean value) {
        return Boolean.toString(value)
                .toUpperCase(Locale.US);
    }
    
    public static final String generateBatchJobRunDateAsString(final LocalDateTime jobRunDate) {
        return generateDateTimeInConsistentFormat(jobRunDate);
    }

}
