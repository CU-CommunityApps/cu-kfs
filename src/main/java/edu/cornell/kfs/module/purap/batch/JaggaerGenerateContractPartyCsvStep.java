package edu.cornell.kfs.module.purap.batch;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;
import edu.cornell.kfs.sys.CUKFSConstants;

public class JaggaerGenerateContractPartyCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService;
    protected ParameterService parameterService;
    protected DateTimeService dateTimeService;
    
    @Override
    public boolean execute(String jobName, java.util.Date jobRunDate) {
        JaggaerContractUploadProcessingMode processingMode = findJaggaerContractUploadProcessingMode();
        java.sql.Date processingDate = findProcessingDate(processingMode);
        String processDateForOutput = dateTimeService.toString(processingDate, CUKFSConstants.DATE_FORMAT_dd_MMM_yyyy);
        LOG.info("execute, processing mode: " + processingMode.modeCode + " processing date: " + processDateForOutput);
        
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = jaggaerGenerateContractPartyCsvService.getJaggaerContractsDto(processingMode, processingDate);
        if (CollectionUtils.isNotEmpty(jaggaerUploadDtos)) {
            jaggaerGenerateContractPartyCsvService.generateCsvFile(jaggaerUploadDtos, processingMode);
        } else {
            LOG.info("execute, there were no vendors found to upload to Jaggaer, so we are NOT generating a CSV file");
        }
        if (processingMode == JaggaerContractUploadProcessingMode.VENDOR) {
            updateVendorProcessingDate();
        }
        return true;
    }
    
    protected JaggaerContractUploadProcessingMode findJaggaerContractUploadProcessingMode() {
        String processingMode = getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_PROCESSING_MODE);
        return JaggaerContractUploadProcessingMode.findJaggaerContractUploadProcessingModeByModeCode(processingMode);
    }
    
    protected java.sql.Date findProcessingDate(JaggaerContractUploadProcessingMode processingMode) {
        String dateString;
        if(processingMode == JaggaerContractUploadProcessingMode.PO) {
            dateString = findPODate();
        } else if (processingMode == JaggaerContractUploadProcessingMode.VENDOR) {
            dateString = findVendorDate();
        } else {
            throw new IllegalArgumentException("Unknown processing mode: " + processingMode);
        }
        java.sql.Date processDate;
        try {
            processDate = dateTimeService.convertToSqlDate(dateString);
        } catch (ParseException e) {
            LOG.error("Unable to convert " + dateString + " to Date object.", e);
            throw new RuntimeException(e);
        }
        return processDate;
        
    }
    
    protected String findPODate() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_PO_DATE);
    }
    
    protected String findVendorDate() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_VENDOR_DATE);
    }
    
    protected String getParameterValueString(String parameterName) {
        return parameterService.getParameterValueAsString(this.getClass(), parameterName);
    }
    
    protected void updateVendorProcessingDate() {
        String newDateString = dateTimeService.toString(dateTimeService.getCurrentDate(), CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
        LOG.info("updateVendorProcessingDate, setting JAGGAER_UPLOAD_VENDOR_DATE to " + newDateString);
        Parameter vendorDateParm = parameterService.getParameter(this.getClass(), CUPurapParameterConstants.JAGGAER_UPLOAD_VENDOR_DATE);
        vendorDateParm.setValue(newDateString);
        parameterService.updateParameter(vendorDateParm);
    }

    public void setJaggaerGenerateContractPartyCsvService(
            JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService) {
        this.jaggaerGenerateContractPartyCsvService = jaggaerGenerateContractPartyCsvService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
