package edu.cornell.kfs.tax.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;

public interface TransactionDetailProcessorDao {

    TaxStatistics processTransactionDetails(final TaxBatchConfig config,
            final TransactionDetailHandler handler);

    VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId);

    VendorAddressLite getHighestPriorityUSVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId);

    VendorAddressLite getHighestPriorityForeignVendorAddress(final Integer vendorHeaderId,
            final Integer vendorDetailId);

    List<NoteLite> getNotesByDocumentNumber(final String documentNumber);

}
