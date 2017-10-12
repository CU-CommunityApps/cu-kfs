package edu.cornell.kfs.sys.xmladapters;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * XML adapter for converting between java.util.Date instances and "MM/dd/yyyy"-formatted date strings.
 * Unlike Rice's StringToDateTimeAdapter, this adapter only supports one pattern,
 * and it does not depend on having Rice's DateTimeService deployed to the bus.
 */
public class StringToJavaDateAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy");

    @Override
    public Date unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? DATE_FORMATTER.parseDateTime(value).toDate() : null;
    }

    @Override
    public String marshal(Date value) throws Exception {
        return (value != null) ? DATE_FORMATTER.print(value.getTime()) : null;
    }

}
