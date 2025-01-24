package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.function.FailableBiFunction;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;

public interface TransactionDetailProcessorDao {

    <U> TaxStatistics processTransactionDetails(final TaxBatchConfig config, final Class<U> updaterDtoType,
            final FailableBiFunction<TaxBatchConfig, TransactionDetailExtractor<U>, TaxStatistics, Exception> handler)
                    throws SQLException;

    VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId) throws SQLException;

    VendorAddressLite getHighestPriorityUSVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException;

    VendorAddressLite getHighestPriorityForeignVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException;

    List<NoteLite> getNotesByDocumentNumber(final String documentNumber) throws SQLException;

}
