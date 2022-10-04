package edu.cornell.kfs.sys.xmladapters;

import java.util.Date;
import java.util.Locale;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.sys.KFSConstants;

/**
 * XML adapter for converting between java.util.Date instances and "MM/dd/yyyy"-formatted date strings.
 * This adapter only supports one pattern.
 */
public class StringToJavaDateAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT).withLocale(Locale.US);

    @Override
    public Date unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? parseToDateTime(value).toDate() : null;
    }

    @Override
    public String marshal(Date value) throws Exception {
        return (value != null) ? DATE_FORMATTER.print(value.getTime()) : null;
    }

    public static DateTime parseToDateTime(String value) {
        return DateTime.parse(value, DATE_FORMATTER);
    }

}
