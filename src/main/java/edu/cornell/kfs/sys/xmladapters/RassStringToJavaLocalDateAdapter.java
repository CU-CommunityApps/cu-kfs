package edu.cornell.kfs.sys.xmladapters;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.sys.CUKFSConstants;

public class RassStringToJavaLocalDateAdapter extends XmlAdapter<String, LocalDate> {

    protected static final DateTimeFormatter DATE_LOCAL_DATE_FORMATTER_yyyy_MM_dd = 
            DateTimeFormatter.ofPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd).withLocale(Locale.US);

    @Override
    public LocalDate unmarshal(String dateStringToConvert) throws Exception {
        return StringUtils.isNotBlank(dateStringToConvert) ? parseToLocalDate(dateStringToConvert) : null;
    }

    @Override
    public String marshal(LocalDate localDate) throws Exception {
        return (localDate != null) ? localDate.format(DATE_LOCAL_DATE_FORMATTER_yyyy_MM_dd) : null;
    }

    public static LocalDate parseToLocalDate(String dateAsString) {
        return LocalDate.parse(dateAsString, DATE_LOCAL_DATE_FORMATTER_yyyy_MM_dd);
    }

}
