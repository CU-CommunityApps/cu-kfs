package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

public interface CemiSupplierOrderFromDao {

    void clearExistingListOfKfsVendorAddressLinks();

    void clearExistingListOfSupplierAddressLinks();

    void clearExistingListOfExtractablePurchaseOrderAddressIds();

    void updateSupplierOrderFromExtractQuerySettings(final String supplierJobRunDate);

    void queryAndStoreListOfKfsVendorAddressLinks(final Supplier<Stream<VendorAddress>> addressQueryRunner);

    void queryAndStoreListOfSupplierAddressLinks(final Supplier<Stream<CemiSupplierAddressBo>> addressQueryRunner);

    void queryAndStoreAddressIdsForSupplierOrderFromExtract();

    boolean determineIfSupplierIsUsedForPunchouts(final String supplierId, final String supplierJobRunDate);

}
