package edu.cornell.kfs.coa.batch;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.coa.batch.service.CreateWorkdayOpenAccountsCsvService;

public class CreateWorkdayOpenAccountsCsvStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    protected CreateWorkdayOpenAccountsCsvService createWorkdayOpenAccountsCsvService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        LOG.info("execute, starting create Workday Open Account - Sub Account - Sub Object batch job");
        try {
            createWorkdayOpenAccountsCsvService.createWorkdayOpenAccountsCsv();
        } catch (IOException e) {
            LOG.error("execute, an error occured while trying to generate the open accounts CSV file", e);
            return false;
        }
        return true;
    }

    public void setCreateWorkdayOpenAccountsCsvService(
            CreateWorkdayOpenAccountsCsvService createWorkdayOpenAccountsCsvService) {
        this.createWorkdayOpenAccountsCsvService = createWorkdayOpenAccountsCsvService;
    }

}
