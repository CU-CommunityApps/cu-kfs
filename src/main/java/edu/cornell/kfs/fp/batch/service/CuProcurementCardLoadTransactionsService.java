package edu.cornell.kfs.fp.batch.service;

import org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService;

public interface CuProcurementCardLoadTransactionsService extends ProcurementCardLoadTransactionsService {
    boolean loadProcurementCardFile(String fileName);
}
