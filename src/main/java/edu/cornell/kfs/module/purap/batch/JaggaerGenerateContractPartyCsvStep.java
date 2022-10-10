package edu.cornell.kfs.module.purap.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    
    @Override
    public boolean execute(String jobName, java.util.Date jobRunDate) {
        JaggaerContractUploadProcessingMode processingMode = findJaggaerContractUploadProcessingMode();
        java.sql.Date processingDate = findProcessingDate(processingMode);
        String processDateForOutput = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_dd_MMM_yyyy).format(processingDate);
        LOG.info("execute, processing mode: " + processingMode.modeCode + " processing date: " + processDateForOutput);
        
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = jaggaerGenerateContractPartyCsvService.getJaggerContractsDto(processingMode, processingDate);
        jaggaerGenerateContractPartyCsvService.generateCsvFile(jaggaerUploadDtos, processingMode);
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
            processDate = new java.sql.Date(findSimpleDateFormat().parse(dateString).getTime());
        } catch (ParseException e) {
            LOG.error("Unable able to covert " + dateString + " to Date object.", e);
            throw new RuntimeException(e);
        }
        return processDate;
        
    }
    
    protected SimpleDateFormat findSimpleDateFormat() {
        return new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd, Locale.US);
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
    
    protected void  updateVendorProcessingDate() {
        String newDateString = findSimpleDateFormat().format(Calendar.getInstance(Locale.US).getTime());
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

}
