package edu.cornell.kfs.pmw.batch.dataaccess;

import java.sql.Timestamp;

public interface PaymentWorksVendorDao {

    void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, Timestamp processingTimeStamp);

    void updateExistingPaymentWorksVendorInStagingTable(String pmwVendorRequestId, String pmwRequestStatus, String kfsVendorProcessingStatus, String kfsAchProcessingStatus, String kfsVendorDocumentNumber, Timestamp processingTimeStamp);

}
