package edu.cornell.kfs.paymentworks.service;

import java.util.Collection;
import java.util.List;

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
	 * Creates an file to uploaded to PaymentWorks and returns the location and file name of the created file.
	 * @param paymentWorksSupplierUploadList
	 * @param directoryPath
	 * @return
	 */
	String createSupplierUploadFile(List<PaymentWorksSupplierUploadDTO> paymentWorksSupplierUploadList,String directoryPath);
	
	/**
	 * Uploads approved suppkiers to PaymentWorks
	 */
	void uploadNewVendorApprovedSupplierFile();
	
	/**
	 * Uploads new vendors that have been disapproved to PaymentWorks
	 */
	void updateNewVendorDisapprovedStatus();
	
	/**
	 * Uploads supplier updates to PaymentWorks
	 */
	void uploadVendorUpdateApprovedSupplierFile();

}
