package edu.cornell.kfs.sys.xmladapters;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class ZonedStringToJavaDateXmlAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            CUKFSConstants.DATE_FORMAT_yyyy_MM_dd_T_HH_mm_ss_SSS_XXX, Locale.US);

    @Override
    public Date unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? parseDate(value) : null;
    }

    private Date parseDate(String value) {
        long milliseconds = ZonedDateTime.parse(value, DATE_FORMATTER)
                .toInstant()
                .toEpochMilli();
        return new Date(milliseconds);
    }

    @Override
    public String marshal(Date value) throws Exception {
        return value != null ? formatDate(value) : null;
    }

    private String formatDate(Date value) {
        Instant milliInstant = Instant.ofEpochMilli(value.getTime());
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(milliInstant, ZoneOffset.UTC);
        return DATE_FORMATTER.format(dateTime);
    }

}
