package edu.cornell.kfs.tax.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public interface TransactionDetailBuilderDao {

    void deleteTransactionDetailsForConfiguredTaxTypeAndYear(final TaxBatchConfig config);

    void insertTransactionDetails(
            final List<TransactionDetail> transactionDetails, final TaxBatchConfig config);

    List<RouteHeaderLite> getBasicRouteHeaderData(final List<String> documentIds);

    TaxStatistics createDvTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<DvSourceData> dvSourceHandler);

    TaxStatistics createPdpTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PdpSourceData> pdpSourceHandler);

    TaxStatistics createPrncTransactionDetails(final TaxBatchConfig config,
            final TransactionSourceHandler<PrncSourceData> prncSourceHandler);

}
