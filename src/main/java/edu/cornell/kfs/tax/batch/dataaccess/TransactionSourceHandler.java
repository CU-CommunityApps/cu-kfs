package edu.cornell.kfs.tax.batch.dataaccess;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

@FunctionalInterface
public interface TransactionSourceHandler<T> {

    TaxStatistics generateTransactionDetails(final TaxBatchConfig config,
            final TaxDtoRowMapper<T> rowMapper) throws Exception;

}
