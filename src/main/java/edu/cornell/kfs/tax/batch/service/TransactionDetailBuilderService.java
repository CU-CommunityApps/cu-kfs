package edu.cornell.kfs.tax.batch.service;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

public interface TransactionDetailBuilderService {

    void deleteTransactionDetailsForConfiguredTaxTypeAndYear(final TaxBatchConfig config);

    TaxStatistics generateTransactionDetails(final TaxBatchConfig config);

}
