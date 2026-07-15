package  edu.cornell.kfs.cemi.module.cg.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleExtractService;

public class CreateCemiAwardScheduleExtractStep extends AbstractStep {
    
    private CemiAwardScheduleExtractService cemiAwardScheduleExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiAwardScheduleExtractService.resetState();
        cemiAwardScheduleExtractService.captureInScopeBusinessObjectKeysToProcessingTable();
        //Phase 2: Loop through result set to create all the csv files
        cemiAwardScheduleExtractService.generateIntermediateExtractData(jobRunDate);
        //Phase 3: Create single multi-tabbed file.
        cemiAwardScheduleExtractService.generateAwardScheduleExtractFile(jobRunDate);
        return true;
    }

    public CemiAwardScheduleExtractService getCemiAwardScheduleExtractService() {
        return cemiAwardScheduleExtractService;
    }

    public void setCemiAwardScheduleExtractService(CemiAwardScheduleExtractService cemiAwardScheduleExtractService) {
        this.cemiAwardScheduleExtractService = cemiAwardScheduleExtractService;
    }
}
