package edu.cornell.kfs.paymentworks.service;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;

public interface PaymentWorksNewVendorConversionService {

    /**
     * Create a new Vendor Detail record based on a PaymentWorksVendor object.
     * @param paymentWorksVendor
     * @return
     */
    VendorDetail createVendorDetail(PaymentWorksVendor paymentWorksVendor);

    /**
     * Create a new PaymentWorksVendor based on a PaymentWorksNewVendorDetailDTO
     * @param paymentWorksNewVendorDetailDTO
     * @return
     */
    PaymentWorksVendor createPaymentWorksVendor(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO);

    /**
     * Create a new PaymentWorksVendor based on a VendorDetail object and then
     * set the PaymentWorksVendor document number attribute to the document
     * number supplied.
     * @param vendorDetail
     * @param documentNumber
     * @return
     */
    PaymentWorksVendor createPaymentWorksVendor(VendorDetail vendorDetail, String documentNumber);

    /**
     * Creates a new PaymentWorksVendor based on the details of an existing PaymentWorksVendor.
     * @param vendor
     * @return
     */
    PaymentWorksVendor createPaymentWorksVendorFromDetail(PaymentWorksVendor vendor);

}
