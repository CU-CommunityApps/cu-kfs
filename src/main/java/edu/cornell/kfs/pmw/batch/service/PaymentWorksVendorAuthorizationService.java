package edu.cornell.kfs.pmw.batch.service;

import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksVendorAuthorizationService {

    boolean canRestageForPaymentWorksUpload(final PaymentWorksVendor paymentWorksVendor, final Person user);

}
