package edu.cornell.kfs.pmw.batch.service;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.vnd.businessobject.SupplierDiversity;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;

public interface PaymentWorksVendorDataProcessingIntoKfsService {
    
    boolean createValidateAndRouteKFSVendor(PaymentWorksVendor savedStgNewVendorRequestDetailToProcess, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap,
                                            Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, PaymentWorksNewVendorRequestsBatchReportData reportData);
    
}
