package edu.cornell.kfs.gl.businessobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.sys.businessobject.BusinessObjectFlatFileSerializerFieldUtils;

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
        return dateTimeService.toString((java.sql.Date) sqlDate, CuGeneralLedgerConstants.COLLECTOR_FILE_DATE_FORMAT);
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
