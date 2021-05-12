package edu.cornell.kfs.concur.batch.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.ConcurConstants;

/**
 * Abstract BO serializer utility class that contains helper methods
 * for serializing SQL dates into the Collector file's date format.
 */
public abstract class CollectorSerializerFieldUtilsWithDate extends BusinessObjectFlatFileSerializerFieldUtils {

    protected DateTimeService dateTimeService;

    /**
     * Converts a SQL date into a formatted "yyyy-MM-dd" date String, or into an empty String if null.
     */
    protected String formatSqlDateForCollectorFile(Object sqlDate) {
        if (sqlDate == null) {
            return StringUtils.EMPTY;
        }
        return dateTimeService.toString((java.sql.Date) sqlDate, ConcurConstants.COLLECTOR_FILE_DATE_FORMAT);
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
