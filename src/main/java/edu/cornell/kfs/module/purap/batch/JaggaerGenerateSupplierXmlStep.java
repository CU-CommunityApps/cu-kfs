package edu.cornell.kfs.module.purap.batch;

import java.text.ParseException;
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
import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerUploadSuppliersProcessingMode;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.CUKFSConstants;

public class JaggaerGenerateSupplierXmlStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerGenerateSupplierXmlService jaggaerGenerateSupplierXmlService;
    protected ParameterService parameterService;
    
    @Override
    public boolean execute(String jobName, java.util.Date jobRunDate) throws InterruptedException {
        JaggaerUploadSuppliersProcessingMode processingMode = findJaggaerContractUploadProcessingMode();
        java.sql.Date processingDate = findProcessingDate(processingMode);
        int maximumNumberOfSuppliersPerListItem = findMaximumNumberOfSuppliersPerListItem();
        LOG.info("execute, processing mode {} and procesing date {}, maximumNumberOfSuppliersPerListItem {}", processingMode.modeCode, processingDate, maximumNumberOfSuppliersPerListItem);
        List<SupplierSyncMessage> messages = jaggaerGenerateSupplierXmlService.getJaggaerContractsDto(processingMode, processingDate, maximumNumberOfSuppliersPerListItem);
        jaggaerGenerateSupplierXmlService.generateXMLForSyncMessages(messages);
        
        if (shouldUpdateProcessingDate(processingMode)) {
            updateVendorProcessingDate();
        }
        
        return true;
    }
    
    protected JaggaerUploadSuppliersProcessingMode findJaggaerContractUploadProcessingMode() {
        String processingMode = getParameterValueString(CUPurapParameterConstants.JAGGAER_UPLOAD_PROCESSING_MODE);
        return JaggaerUploadSuppliersProcessingMode.findJaggaerUploadSuppliersProcessingModeByModeCode(processingMode);
    }
    
    protected java.sql.Date findProcessingDate(JaggaerUploadSuppliersProcessingMode processingMode) {
        String dateString;
        if (processingMode == JaggaerUploadSuppliersProcessingMode.PO) {
            dateString = findPODate();
        } else if (processingMode == JaggaerUploadSuppliersProcessingMode.VENDOR) {
            dateString = findVendorDate();
        } else {
            throw new IllegalArgumentException("Unknown processing mode: " + processingMode);
        }
        java.sql.Date processDate;
        try {
            processDate = dateTimeService.convertToSqlDate(dateString);
        } catch (ParseException e) {
            LOG.error("findProcessingDate, Unable to convert {} to Date object.", dateString, e);
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
    
    protected int findMaximumNumberOfSuppliersPerListItem() {
        String maxString = getParameterValueString(CUPurapParameterConstants.JAGGAER_MAX_NUMBER_OF_VENDORS_PER_XML_FILE);
        return Integer.parseInt(maxString);
    }
    
    protected String getParameterValueString(String parameterName) {
        return parameterService.getParameterValueAsString(this.getClass(), parameterName);
    }
    
    protected boolean shouldUpdateProcessingDate(JaggaerUploadSuppliersProcessingMode processingMode) {
        return processingMode == JaggaerUploadSuppliersProcessingMode.VENDOR;
    }
    
    protected void updateVendorProcessingDate() {
        String newDateString = dateTimeService.toString(dateTimeService.getCurrentDate(), CUKFSConstants.DATE_FORMAT_yyyy_MM_dd);
        LOG.info("updateVendorProcessingDate, setting JAGGAER_UPLOAD_VENDOR_DATE to {}", newDateString);
        Parameter vendorDateParm = parameterService.getParameter(this.getClass(), CUPurapParameterConstants.JAGGAER_UPLOAD_VENDOR_DATE);
        vendorDateParm.setValue(newDateString);
        parameterService.updateParameter(vendorDateParm);
    }
    
    public void setJaggaerGenerateSupplierXmlService(JaggaerGenerateSupplierXmlService jaggaerGenerateSupplierXmlService) {
        this.jaggaerGenerateSupplierXmlService = jaggaerGenerateSupplierXmlService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
