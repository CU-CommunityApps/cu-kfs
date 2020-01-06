package edu.cornell.kfs.coa.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.coa.batch.service.CreateClosedLaborAccountsCsvService;

public class CreateClosedLaborAccountsCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger(CreateClosedLaborAccountsCsvStep.class);
    protected CreateClosedLaborAccountsCsvService createClosedLaborAccountsCsvService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        createClosedLaborAccountsCsvService.createClosedLaborAccountCsvByParameterPastDays();
        return true;
    }

    public CreateClosedLaborAccountsCsvService getCreateClosedLaborAccountsCsvService() {
        return createClosedLaborAccountsCsvService;
    }

    public void setCreateClosedLaborAccountsCsvService(
            CreateClosedLaborAccountsCsvService createClosedLaborAccountsCsvService) {
        this.createClosedLaborAccountsCsvService = createClosedLaborAccountsCsvService;
    }

}
