package edu.cornell.kfs.fp.document.service.impl;

import java.sql.Date;
import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;

public class CuDisbursementVoucherDefaultDueDateServiceImpl implements CuDisbursementVoucherDefaultDueDateService {
    protected static Logger LOG = LogManager.getLogger(CuDisbursementVoucherDefaultDueDateServiceImpl.class);
    
    protected ParameterService parameterService;
    protected DateTimeService dateTimeService;

    @Override
    public Date findDefaultDueDate() {
        LocalDate calculatedDefaultDvDueDate = dateTimeService.getLocalDateNow().plusDays(findNumberOfDaysInTheFuture());
        return dateTimeService.getSqlDate(calculatedDefaultDvDueDate);
    }
    
    protected int findNumberOfDaysInTheFuture() {
        String numberOfDaysString  = parameterService.getParameterValueAsString(DisbursementVoucherDocument.class, 
                CuFPParameterConstants.DisbursementVoucherDocument.NUMBER_OF_DAYS_FOR_DEFAULT_DV_DUE_DATE);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findNumberOfDaysInTheFuture. numberOfDaysString: " + numberOfDaysString);
        }
        try {
            return Integer.parseInt(numberOfDaysString);
        } catch (NumberFormatException nfe) {
            LOG.error("findNumberOfDaysInTheFuture, unable to convert '" + numberOfDaysString + "' into a number.", nfe);
            throw new IllegalStateException(nfe);
        }
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
