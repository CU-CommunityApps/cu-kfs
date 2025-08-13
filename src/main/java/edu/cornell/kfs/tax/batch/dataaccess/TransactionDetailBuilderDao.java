package edu.cornell.kfs.tax.batch.dataaccess;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;

public interface TransactionDetailBuilderDao {

    void deleteTransactionDetailsForConfiguredTaxTypeAndYear(final TaxBatchConfig config);

    TaxStatistics createDvTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<DvSourceData> dvSourceHandler,
            final TransactionDetailHandler secondPassHandler);

    TaxStatistics createPdpTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PdpSourceData> pdpSourceHandler,
            final TransactionDetailHandler secondPassHandler);

    TaxStatistics createPrncTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PrncSourceData> prncSourceHandler,
            final TransactionDetailHandler secondPassHandler);

}
