package edu.cornell.kfs.sys.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.CoreConstants;
import org.kuali.kfs.core.impl.datetime.DateTimeServiceImpl;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Micro-testable DateTimeService implementation that will only use
 * the default datetime formats.
 */
public class TestDateTimeServiceImpl extends DateTimeServiceImpl {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.stringToDateFormats = StringUtils.split(CoreConstants.STRING_TO_DATE_FORMATS_DEFAULT, CUKFSConstants.SEMICOLON);
        this.stringToTimeFormats = StringUtils.split(CoreConstants.STRING_TO_TIME_FORMATS_DEFAULT, CUKFSConstants.SEMICOLON);
        this.stringToTimestampFormats = StringUtils.split(CoreConstants.STRING_TO_TIMESTAMP_FORMATS_DEFAULT, CUKFSConstants.SEMICOLON);
        this.dateToStringFormatForUserInterface = CoreConstants.DATE_TO_STRING_FORMAT_FOR_USER_INTERFACE_DEFAULT;
        this.timeToStringFormatForUserInterface = CoreConstants.TIME_TO_STRING_FORMAT_FOR_USER_INTERFACE_DEFAULT;
        this.timestampToStringFormatForUserInterface = CoreConstants.TIMESTAMP_TO_STRING_FORMAT_FOR_USER_INTERFACE_DEFAULT;
        this.dateToStringFormatForFileName = CoreConstants.DATE_TO_STRING_FORMAT_FOR_FILE_NAME_DEFAULT;
        this.timestampToStringFormatForFileName = CoreConstants.TIMESTAMP_TO_STRING_FORMAT_FOR_FILE_NAME_DEFAULT;
    }

}
