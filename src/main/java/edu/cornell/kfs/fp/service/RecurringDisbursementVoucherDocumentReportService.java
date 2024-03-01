package edu.cornell.kfs.fp.service;

import java.util.List;

import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;

public interface RecurringDisbursementVoucherDocumentReportService {

    void buildDvAutoApproveErrorReportIfNecessary(
            List<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItems);

}
