package edu.cornell.kfs.tax.batch;

import java.util.Date;

import edu.cornell.kfs.tax.service.SprintaxProcessingService;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.tax.service.TaxProcessingService;

/**
 * Batch step that performs 1042S tax processing for Sprintax in a given year (or date range within a year).
 */
public class SprintaxProcessingStep extends AbstractStep {

    private SprintaxProcessingService sprintaxProcessingService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        sprintaxProcessingService.doTaxProcessing(jobRunDate);
        return true;
    }

    public void setSprintaxProcessingService(SprintaxProcessingService sprintaxProcessingService) {
        this.sprintaxProcessingService = sprintaxProcessingService;
    }
}
