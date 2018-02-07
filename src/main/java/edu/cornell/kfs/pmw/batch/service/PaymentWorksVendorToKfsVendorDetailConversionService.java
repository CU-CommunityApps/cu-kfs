package edu.cornell.kfs.pmw.batch.service;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.vnd.businessobject.SupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksVendorToKfsVendorDetailConversionService {
    
    KfsVendorDataWrapper createKfsVendorDetailFromPmwVendor(PaymentWorksVendor pmwVendor, 
                                                            Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap, 
                                                            Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap);

}
