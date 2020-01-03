package edu.cornell.kfs.coa.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.coa.batch.service.CreateClosedLaborAccountsCsvByPreviousDaysService;

public class CreateClosedLaborAccountsCsvByPreviousDaysStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger(CreateClosedLaborAccountsCsvByPreviousDaysStep.class);
    protected CreateClosedLaborAccountsCsvByPreviousDaysService createClosedLaborAccountsCsvByPreviousDaysService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        createClosedLaborAccountsCsvByPreviousDaysService.createClosedLaborAccountCsvByParameterPastDays();
        return true;
    }

    public CreateClosedLaborAccountsCsvByPreviousDaysService getCreateClosedLaborAccountsCsvByPreviousDaysService() {
        return createClosedLaborAccountsCsvByPreviousDaysService;
    }

    public void setCreateClosedLaborAccountsCsvByPreviousDaysService(
            CreateClosedLaborAccountsCsvByPreviousDaysService createClosedLaborAccountsCsvByPreviousDaysService) {
        this.createClosedLaborAccountsCsvByPreviousDaysService = createClosedLaborAccountsCsvByPreviousDaysService;
    }

}
