package edu.cornell.kfs.paymentworks.service;

public interface PaymentWorksUploadSuppliersService {
	
	boolean uploadNewVendorApprovedSupplierFile();
	
	boolean updateNewVendorDisapprovedStatus();
	
	boolean uploadVendorUpdateApprovedSupplierFile();
}
