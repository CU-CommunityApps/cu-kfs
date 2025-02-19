package edu.cornell.kfs.tax.batch.dataaccess;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

@FunctionalInterface
public interface TransactionDetailHandler<U> {

    TaxStatistics performProcessing(final TaxBatchConfig config,
            final TransactionDetailRowMapper<U> rowMapper) throws Exception;

}
