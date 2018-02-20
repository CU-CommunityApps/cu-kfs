package edu.cornell.kfs.sys.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class AwsStringToJavaDateTimeAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

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
