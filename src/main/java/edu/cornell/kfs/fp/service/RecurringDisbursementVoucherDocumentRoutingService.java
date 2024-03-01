package edu.cornell.kfs.fp.service;

import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;

public interface RecurringDisbursementVoucherDocumentRoutingService {

    RecurringDisbursementVoucherDocumentRoutingReportItem autoApproveSpawnedDisbursementVoucher(
            String spawnedDvDocumentNumber);

}
