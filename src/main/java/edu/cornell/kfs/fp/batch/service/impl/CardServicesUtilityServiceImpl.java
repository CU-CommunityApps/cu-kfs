package edu.cornell.kfs.fp.batch.service.impl;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.service.CardServicesUtilityService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CardServicesUtilityServiceImpl implements CardServicesUtilityService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected DateTimeService dateTimeService;

    public java.sql.Date convertCardDateToSqlDate(String dateAsYYYYMMDD) {
        return this.convertToSqlDate(changeFormatFromYYYYMMDDToSlashedMMDDYYYY(dateAsYYYYMMDD));
    }
    
    public String changeFormatFromYYYYMMDDToSlashedMMDDYYYY(String dateAsYYYYMMDD) {
        String slashedFormattedDateString = dateAsYYYYMMDD.substring(4, 6) 
                + CUKFSConstants.SLASH + dateAsYYYYMMDD.substring(6) 
                + CUKFSConstants.SLASH + dateAsYYYYMMDD.substring(0, 4);
        return slashedFormattedDateString;
    }
    
    public KualiDecimal generateKualiDecimal(String stringToConvert) {
        if (StringUtils.isNotEmpty(stringToConvert)) {
            return new KualiDecimal(stringToConvert);
        } else {
            return KualiDecimal.ZERO;
        }
    }
    
    public void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(
                    dataFileName, CUKFSConstants.DELIMITER) + CUKFSConstants.FileExtensions.DONE);
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }
    
    /**
     * Separate method required to encapsulate/isolate ParseException in one spot.
     */
    private Date convertToSqlDate(String stringToConvert) {
        Date sqlDate = null;
        try {
            sqlDate = dateTimeService.convertToSqlDate(stringToConvert);
        } catch (ParseException e) {
            LOG.error("ParseException generated attempted to convert string " + stringToConvert 
                    + " to java.sql.Date using dateTimeService.convertToSqlDate" + e.getMessage(), e);
            throw new RuntimeException("ParseException generated attempted to convert string " + stringToConvert 
                    + " to java.sql.Date using dateTimeService.convertToSqlDate " + e.getMessage(), e);
        }
        return sqlDate;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
