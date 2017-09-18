package edu.cornell.kfs.paymentworks.service;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;

public interface PaymentWorksVendorUpdateConversionService {

    /**
     * Finds duplicate vendor update for a group where fields match what is on a KFS vendor
     *
     * @param vendorDetail
     * @param paymentWorksVendor
     * @return
     */
    boolean duplicateFieldsOnVendor(VendorDetail vendorDetail, PaymentWorksVendor paymentWorksVendor);

    /**
     * Creates a Vendor Detail record from a KFS record, applying PaymentWorks
     * data from the staging table only where we have received data.
     *
     * @param vendorDetail
     * @param paymentWorksVendor
     * @return
     */
    VendorDetail createVendorDetailForEdit(VendorDetail newVendorDetail, VendorDetail oldVendorDetail, PaymentWorksVendor paymentWorksVendor);

    /**
     * Method that takes a vendor update from PaymentWorks and creates a staging table record
     *
     * @param paymentWorksVendorUpdatesDTO
     * @return
     */
    PaymentWorksVendor createPaymentWorksVendorUpdate(PaymentWorksVendorUpdatesDTO paymentWorksVendorUpdatesDTO);

}
