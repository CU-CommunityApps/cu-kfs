package edu.cornell.kfs.fp.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;

public class CreateAccountingDocumentsStep extends AbstractStep {
	
	private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentsStep.class);

    private CreateAccountingDocumentService createAccountingDocumentService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        boolean processResults;
        try {
            processResults = createAccountingDocumentService.createAccountingDocumentsFromXml();
        } catch (Exception e) {
            LOG.error("execute, An error has occurred while processing input files.", e);
            processResults = false;
        }
        if (processResults) {
            LOG.info("execute, The job ran successfully");
        } else {
            LOG.warn("execute, there were non business rules errors that caused this job to fail");
        }
        return processResults;
    }

    public void setCreateAccountingDocumentService(CreateAccountingDocumentService createAccountingDocumentService) {
        this.createAccountingDocumentService = createAccountingDocumentService;
    }

}
