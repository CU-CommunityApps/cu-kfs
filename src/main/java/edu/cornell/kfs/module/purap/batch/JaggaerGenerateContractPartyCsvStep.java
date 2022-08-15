package edu.cornell.kfs.module.purap.batch;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateContractPartyCsvService;
import edu.cornell.kfs.module.purap.businessobject.lookup.JaggaerContractUploadBaseDto;

public class JaggaerGenerateContractPartyCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    
    protected JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        LOG.info("execute, starting");
        List<JaggaerContractUploadBaseDto> jaggaerUploadDtos = jaggaerGenerateContractPartyCsvService.getJaggerContractsDto();
        LOG.info("execute, completed getting DTOs");
        jaggaerGenerateContractPartyCsvService.generateCsvFile(jaggaerUploadDtos);
        LOG.info("execute, generated CSV file");
        return true;
    }

    public void setJaggaerGenerateContractPartyCsvService(
            JaggaerGenerateContractPartyCsvService jaggaerGenerateContractPartyCsvService) {
        this.jaggaerGenerateContractPartyCsvService = jaggaerGenerateContractPartyCsvService;
    }

}
