package edu.cornell.kfs.pmw.batch.service;

import java.util.List;

import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksVendorSupplierDiversityService {
    
    List<VendorSupplierDiversity> buildSuppplierDivsersityListFromPaymentWorksVendor(PaymentWorksVendor pmwVendor);
    
}
