package edu.cornell.kfs.paymentworks.service;

import java.io.File;
import java.util.Collection;
import java.util.List;

import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummary;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksSupplierUploadDTO;

public interface PaymentWorksUploadSupplierService {

    /**
     * Creates a List of PaymentWorks supplier DTOs to upload to payment works
     * @param newVendors
     * @return
     */
    List<PaymentWorksSupplierUploadDTO> createPaymentWorksSupplierUploadList(Collection<PaymentWorksVendor> newVendors);

    /**
     * Creates an file to uploaded to PaymentWorks and returns the location and
     * file name of the created file.
     * @param paymentWorksSupplierUploadList
     * @param directoryPath
     * @return
     */
    String createSupplierUploadFile(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList, String directoryPath);

    /**
     * Uploads approved suppliers to PaymentWorks
     * @param supplierUploadSummary
     */
    void uploadNewVendorApprovedSupplierFile(SupplierUploadSummary supplierUploadSummary);

    /**
     * Uploads new vendors that have been disapproved to PaymentWorks
     * @param supplierUploadSummary
     */
    void updateNewVendorDisapprovedStatus(SupplierUploadSummary supplierUploadSummary);

    /**
     * Uploads supplier updates to PaymentWorks
     * @param supplierUploadSummary
     */
    void uploadVendorUpdateApprovedSupplierFile(SupplierUploadSummary supplierUploadSummary);

    /**
     * Writes a summary file for the results of uploading suppliers.
     * @param supplierUploadSummary
     * @return File
     */
    File writePaymentWorksSupplierUploadSummaryReport(SupplierUploadSummary supplierUploadSummary);

}
