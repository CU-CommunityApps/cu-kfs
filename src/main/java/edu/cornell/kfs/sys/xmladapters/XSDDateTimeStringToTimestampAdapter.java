package edu.cornell.kfs.sys.xmladapters;

import java.sql.Timestamp;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

public class XSDDateTimeStringToTimestampAdapter extends XmlAdapter<String, Timestamp> {

    @Override
    public Timestamp unmarshal(String dateTimeString) throws Exception {
        if (StringUtils.isBlank(dateTimeString)) {
            return null;
        }
        Calendar parsedDateTime = DatatypeConverter.parseDateTime(dateTimeString);
        return new Timestamp(parsedDateTime.getTimeInMillis());
    }

    @Override
    public String marshal(Timestamp timestamp) throws Exception {
        if (timestamp == null) {
            return null;
        }
        Calendar calendarDateTime = Calendar.getInstance();
        calendarDateTime.setTimeInMillis(timestamp.getTime());
        return DatatypeConverter.printDateTime(calendarDateTime);
    }

}
