package edu.cornell.kfs.module.purap.batch;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlService;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;

public class JaggaerGenerateSupplierXmlStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerGenerateSupplierXmlService jaggaerGenerateSupplierXmlService;
    protected ParameterService parameterService;
    
    @Override
    public boolean execute(String jobName, java.util.Date jobRunDate) throws InterruptedException {
        JaggaerContractUploadProcessingMode processingMode = findProcessingMode();
        java.sql.Date processingDate = findProcessingDate();
        int maximumNumberOfSuppliersPerListItem = findMaximumNumberOfSuppliersPerListItem();
        LOG.info("execute, processing mode {} and procesing date {}, maximumNumberOfSuppliersPerListItem {}", processingMode.modeCode, processingDate, maximumNumberOfSuppliersPerListItem);
        List<SupplierSyncMessage> messages = jaggaerGenerateSupplierXmlService.getJaggaerContractsDto(processingMode, processingDate, maximumNumberOfSuppliersPerListItem);
        jaggaerGenerateSupplierXmlService.generateXMLForSyncMessages(messages);
        return true;
    }
    
    private JaggaerContractUploadProcessingMode findProcessingMode() {
        return JaggaerContractUploadProcessingMode.PO;
    }
    
    private java.sql.Date findProcessingDate() {
        java.sql.Date processingDate = new java.sql.Date(Calendar.getInstance(Locale.US).getTimeInMillis());
        return processingDate;
    }
    
    private int findMaximumNumberOfSuppliersPerListItem() {
        return 2;
    }

    public void setJaggaerGenerateSupplierXmlService(JaggaerGenerateSupplierXmlService jaggaerGenerateSupplierXmlService) {
        this.jaggaerGenerateSupplierXmlService = jaggaerGenerateSupplierXmlService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
}
