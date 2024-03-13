package edu.cornell.kfs.fp.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;

public class ApproveDvsSpawnedByRecurringDvStep extends AbstractStep {

    protected RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        getRecurringDisbursementVoucherDocumentService().autoApproveDisbursementVouchersSpawnedByRecurringDvs();
        return true;
    }

    public RecurringDisbursementVoucherDocumentService getRecurringDisbursementVoucherDocumentService() {
        return recurringDisbursementVoucherDocumentService;
    }

    public void setRecurringDisbursementVoucherDocumentService(
            RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService) {
        this.recurringDisbursementVoucherDocumentService = recurringDisbursementVoucherDocumentService;
    }
}
