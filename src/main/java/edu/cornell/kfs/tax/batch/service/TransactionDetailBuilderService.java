package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

public interface TransactionDetailBuilderService {

    TaxStatistics generateTransactionDetails(final TaxBatchConfig config);

}
