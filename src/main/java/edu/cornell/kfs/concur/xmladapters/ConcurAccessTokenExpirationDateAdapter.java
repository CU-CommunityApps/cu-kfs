package edu.cornell.kfs.concur.xmladapters;

import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.sys.CUKFSConstants;

public class ConcurAccessTokenExpirationDateAdapter extends XmlAdapter<String, Date> {

    private static final DateTimeFormatter EXPIRATION_DATE_FORMATTER = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am)
            .withLocale(Locale.US);

    @Override
    public String marshal(Date value) throws Exception {
        return value != null ? EXPIRATION_DATE_FORMATTER.print(value.getTime()) : null;
    }

    @Override
    public Date unmarshal(String value) throws Exception {
        return StringUtils.isNotBlank(value) ? new Date(EXPIRATION_DATE_FORMATTER.parseMillis(value)) : null;
    }

}
