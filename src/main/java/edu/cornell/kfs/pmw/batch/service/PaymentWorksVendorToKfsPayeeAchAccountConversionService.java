package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.businessobject.KfsAchDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksVendorToKfsPayeeAchAccountConversionService {
    
    KfsAchDataWrapper createKfsPayeeAchFromPmwVendor(PaymentWorksVendor pmwVendor);

}
