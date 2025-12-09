package edu.cornell.kfs.tax.batch.dataaccess;

import java.util.List;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorContactLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public interface TransactionDetailProcessorDao {

    TaxStatistics processTransactionDetails(final TaxBatchConfig config,
            final TransactionDetailHandler handler);

    void updateVendorInfoAndTaxBoxesOnTransactionDetails(final List<TransactionDetail> transactionDetails,
            final TaxBatchConfig config);

    VendorQueryResults getVendor(final Integer vendorHeaderId, final Integer vendorDetailId);

    List<VendorAddressLite> getPrioritizedUSVendorAddresses(final Integer vendorHeaderId,
            final Integer vendorDetailId);

    List<VendorAddressLite> getPrioritizedForeignVendorAddresses(final Integer vendorHeaderId,
            final Integer vendorDetailId);

    List<VendorContactLite> getPrioritizedVendorContactsWithEmails(final Integer vendorHeaderId,
            final Integer vendorDetailId);

    List<NoteLite> getNotesByDocumentNumber(final String documentNumber);

}
