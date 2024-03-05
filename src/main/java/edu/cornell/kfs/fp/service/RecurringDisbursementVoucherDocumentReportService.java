package edu.cornell.kfs.fp.service;

import java.io.File;
import java.util.List;

import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;

public interface RecurringDisbursementVoucherDocumentReportService {

    File buildDvAutoApproveErrorReport(
            List<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItems);

    void sendDvAutoApproveErrorReportEmail(File reportFile);

}
