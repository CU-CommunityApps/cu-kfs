package edu.cornell.kfs.fp.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentService;

public class CreateAccountingDocumentsStep extends AbstractStep {

    private CreateAccountingDocumentService createAccountingDocumentService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        createAccountingDocumentService.createAccountingDocumentsFromXml();
        return true;
    }

    public void setCreateAccountingDocumentService(CreateAccountingDocumentService createAccountingDocumentService) {
        this.createAccountingDocumentService = createAccountingDocumentService;
    }

}
