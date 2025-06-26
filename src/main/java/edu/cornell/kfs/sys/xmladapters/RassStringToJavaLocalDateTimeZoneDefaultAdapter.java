package edu.cornell.kfs.sys.xmladapters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;

public class RassStringToJavaLocalDateTimeZoneDefaultAdapter extends XmlAdapter<String, LocalDateTime> {

    protected static final DateTimeFormatter DATE_TIME_ZONE_DEFAULT_FORMATTER_yyyy_MM_dd_T_HH_mm_ss_SSSSSSSSS = 
            DateTimeFormatter.ofPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSSSSSSSS).withZone(ZoneOffset.systemDefault()).withLocale(Locale.US);

    @Override
    public LocalDateTime unmarshal(String dateTimeAsString) {
        return StringUtils.isNotBlank(dateTimeAsString) ? parseToLocalDateTime(dateTimeAsString) : null;
    }

    @Override
    public String marshal(LocalDateTime localDateTimeValue) {
        return (localDateTimeValue != null) ? localDateTimeValue.format(DATE_TIME_ZONE_DEFAULT_FORMATTER_yyyy_MM_dd_T_HH_mm_ss_SSSSSSSSS) : null;
    }

    public static LocalDateTime parseToLocalDateTime(String dateTimeAsString) {
        return LocalDateTime.parse(dateTimeAsString, DATE_TIME_ZONE_DEFAULT_FORMATTER_yyyy_MM_dd_T_HH_mm_ss_SSSSSSSSS);
    }
    
    public static String formatToDateTimeZoneDefault(LocalDateTime localDateTimeValue) {
        return (localDateTimeValue != null) ? localDateTimeValue.format(DATE_TIME_ZONE_DEFAULT_FORMATTER_yyyy_MM_dd_T_HH_mm_ss_SSSSSSSSS) : null;
    }

}
