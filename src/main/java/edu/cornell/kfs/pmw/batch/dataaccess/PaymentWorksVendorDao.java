package edu.cornell.kfs.pmw.batch.dataaccess;

import java.sql.Timestamp;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksVendorDao {
    
    boolean isExistingPaymentWorksVendor(String pmwVendorId);
    
    PaymentWorksVendor savePaymentWorksVendorToStagingTable(PaymentWorksVendor pmwVendorToSave);
    
    void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp);
    
    void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp processingTimeStamp);
}
