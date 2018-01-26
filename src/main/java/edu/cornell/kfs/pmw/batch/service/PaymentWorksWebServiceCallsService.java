package edu.cornell.kfs.pmw.batch.service;

import java.util.List;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorRequestsBatchReportData;

public interface PaymentWorksWebServiceCallsService {
    
    List<String> obtainPmwIdentifiersForPendingNewVendorRequests();
    
    PaymentWorksVendor obtainPmwNewVendorRequestDetailForPmwIdentifier(String pmwNewVendorRequestId, PaymentWorksNewVendorRequestsBatchReportData reportData);
    
    void sendApprovedStatusToPaymentWorksForNewVendor(String approvedVendorId);
    
    void sendRejectedStatusToPaymentWorksForNewVendor(String rejectedVendorId);
    
}
