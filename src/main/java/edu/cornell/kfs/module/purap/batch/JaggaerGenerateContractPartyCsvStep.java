package edu.cornell.kfs.module.purap.batch;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerContractUploadProcessingMode;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService;
    protected ParameterService parameterService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        JaggaerContractUploadProcessingMode processingMode = findJaggaerContractUploadProcessingMode();
        String processingDate = findProcessingDate(processingMode);
        LOG.info("execute, processing mode: " + processingMode + " processing date: " + processingDate);
        
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = jaggaerGenerateContractPartyCsvService.getJaggerContractsDto(processingMode, processingDate);
        LOG.info("execute, completed getting DTOs");
        jaggaerGenerateContractPartyCsvService.generateCsvFile(jaggaerUploadDtos);
        LOG.info("execute, generated CSV file");
        return true;
    }
    
    protected JaggaerContractUploadProcessingMode findJaggaerContractUploadProcessingMode() {
        /*
         * @todo pull this from parameter
         */
        return JaggaerContractUploadProcessingMode.VENDOR;
    }
    
    protected String findProcessingDate(JaggaerContractUploadProcessingMode processingMode) {
        if(processingMode == JaggaerContractUploadProcessingMode.PO) {
            return findPODate();
        } else if (processingMode == JaggaerContractUploadProcessingMode.VENDOR) {
            return findVendorDate();
        } else {
            throw new IllegalArgumentException("Unknown processing mode: " + processingMode);
        }
    }
    
    protected String findPODate() {
        /*
         * @todo pull this from parameter
         */
        return "2022-07-01";
    }
    
    protected String findVendorDate() {
        /*
         * @todo pull this from parameter
         */
        return "2022-08-01";
    }

    public void setJaggaerGenerateContractPartyCsvService(
            JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService) {
        this.jaggaerGenerateContractPartyCsvService = jaggaerGenerateContractPartyCsvService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
