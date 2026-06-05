package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

public interface CemiSupplierOrderFromDao {

    void clearExistingListOfKfsVendorAddressLinks();

    void clearExistingListOfSupplierAddressLinks();

    void clearExistingListOfExtractablePurchaseOrderAddressIds();

    void updateSupplierOrderFromExtractQuerySettings(final String supplierJobRunDate);

    void storeAsListOfKfsVendorAddressLinks(final Iterator<VendorAddress> addressIterator);

    void storeAsListOfSupplierAddressLinks(final Iterator<CemiSupplierAddressBo> addressIterator);

    void queryAndStoreAddressIdsForSupplierOrderFromExtract();

    boolean determineIfSupplierIsUsedForPunchouts(final String supplierId, final String supplierJobRunDate);

}
