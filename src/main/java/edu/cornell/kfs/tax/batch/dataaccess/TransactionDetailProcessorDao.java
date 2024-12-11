package edu.cornell.kfs.tax.batch.dataaccess;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.function.FailableBiFunction;

import edu.cornell.kfs.tax.batch.TaxOutputConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.businessobject.NoteLite;
import edu.cornell.kfs.tax.businessobject.VendorAddressLite;

public interface TransactionDetailProcessorDao {

    TaxStatistics processTransactionDetails(final TaxOutputConfig config,
            final FailableBiFunction<TaxOutputConfig, TransactionDetailExtractor, TaxStatistics, Exception> handler)
                    throws SQLException;

    VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId) throws SQLException;

    VendorAddressLite getHighestPriorityUSVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException;

    VendorAddressLite getHighestPriorityForeignVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId) throws SQLException;

    List<NoteLite> getNotesByDocumentNumber(final String documentNumber) throws SQLException;

}
