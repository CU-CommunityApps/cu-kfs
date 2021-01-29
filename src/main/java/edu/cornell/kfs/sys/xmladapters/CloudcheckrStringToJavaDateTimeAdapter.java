package edu.cornell.kfs.sys.xmladapters;

import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class CloudcheckrStringToJavaDateTimeAdapter extends XmlAdapter<String, Date> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am).withLocale(Locale.US);

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
