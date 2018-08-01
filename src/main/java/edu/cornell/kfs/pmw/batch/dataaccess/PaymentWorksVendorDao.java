package edu.cornell.kfs.pmw.batch.dataaccess;

import java.sql.Timestamp;
import java.util.List;

public interface PaymentWorksVendorDao {

    void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp);

    void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, String supplierUploadStatus, Timestamp processingTimeStamp);

    void updateExistingPaymentWorksVendorInStagingTable(Integer id, String kfsVendorProcessingStatus, Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, Timestamp processingTimeStamp);
    
    void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp);
    
    void updateExistingPaymentWorksVendorInStagingTable(Integer id, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String supplierUploadStatus, Timestamp processingTimeStamp, String kfsAchDocumentNumber);

    default void updateSupplierUploadStatusesForVendorsInStagingTable(List<Integer> ids, String pmwRequestStatus, String supplierUploadStatus) {
        // TODO: Replace with actual implementation in impl class!
    }

    default void updateSupplierUploadStatusesForVendorsInStagingTable(List<Integer> ids, String supplierUploadStatus) {
        // TODO: Replace with actual implementation in impl class!
    }

}
