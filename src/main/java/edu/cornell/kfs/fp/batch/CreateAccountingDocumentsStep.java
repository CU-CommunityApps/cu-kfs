package edu.cornell.kfs.fp.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;

public class CreateAccountingDocumentsStep extends AbstractStep {
	
	private static final Logger LOG = LogManager.getLogger(CreateAccountingDocumentsStep.class);

    private CreateAccountingDocumentService createAccountingDocumentService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		try {
			return createAccountingDocumentService.createAccountingDocumentsFromXml();
		} catch (Exception e) {
			LOG.error("execute, An error has ocurred while processing input files: " + e.getMessage(), e);
			return false;
		}
    }

    public void setCreateAccountingDocumentService(CreateAccountingDocumentService createAccountingDocumentService) {
        this.createAccountingDocumentService = createAccountingDocumentService;
    }

}
