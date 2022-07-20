package edu.cornell.kfs.sys.xmladapters;

import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class DateTimeUTCOffsetStringToJavaDateAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_ZZ)
            .withZoneUTC()
            .withLocale(Locale.US);

    @Override
    public String marshal(Date value) throws Exception {
        return (value != null) ? DATE_FORMATTER.print(value.getTime()) : null;
    }

    @Override
    public Date unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? parseDateString(value) : null;
    }

    public static Date parseDateString(String value) {
        long millisecondValue = DATE_FORMATTER.parseMillis(value);
        return new Date(millisecondValue);
    }

}
