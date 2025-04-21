package edu.cornell.kfs.coa.batch;

import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.coa.batch.service.CreateClosedAccountsCsvService;

public class CreateClosedAccountsCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger(CreateClosedAccountsCsvStep.class);
    protected CreateClosedAccountsCsvService createClosedAccountsCsvService;
    
    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        try {
            getCreateClosedAccountsCsvService().createClosedAccountsCsvByParameterPastDays();
        } catch (IOException e) {
            LOG.info("CreateClosedAccountsCsvStep.execute: Caught IOException. Failing batch job.");
            return false;
        }
        return true;
    }

    public CreateClosedAccountsCsvService getCreateClosedAccountsCsvService() {
        return createClosedAccountsCsvService;
    }

    public void setCreateClosedAccountsCsvService(CreateClosedAccountsCsvService createClosedAccountsCsvService) {
        this.createClosedAccountsCsvService = createClosedAccountsCsvService;
    }


}
