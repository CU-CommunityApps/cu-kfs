package edu.cornell.kfs.tax.batch.dataaccess;

import org.apache.commons.lang3.function.FailableBiFunction;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;

@FunctionalInterface
public interface TransactionDetailHandler<U>
        extends FailableBiFunction<TaxBatchConfig, TransactionDetailRowMapper<U>, TaxStatistics, Exception> {

}
