package edu.cornell.kfs.tax.batch.dataaccess;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

@FunctionalInterface
public interface TransactionDetailHandler {

    TaxStatistics performProcessing(final TaxBatchConfig config,
            final TaxDtoRowMapper<TransactionDetail> rowMapper) throws Exception;

}
