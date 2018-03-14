package edu.cornell.kfs.pmw.batch.service;

import org.kuali.rice.kim.api.identity.Person;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;

public interface PaymentWorksBatchUtilityService {
    
    String retrievePaymentWorksParameterValue(String parameterName);
    
    String getFileContents(String fileName);
    
    KfsVendorDataWrapper createNoteRecordingAnyErrors(KfsVendorDataWrapper kfsVendorDataWrapper, String noteText, String noteErrorDescriptor);
    
    Person getSystemUser();
    
    boolean foundExistingPaymentWorksVendorByKfsDocumentNumber(String kfsDocumentNumber);
    
    boolean foundExistingPaymentWorksVendorByPaymentWorksVendorId(String pmwVendorId);
    
    void registerKfsPvenApprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenApprovalForKfsEnteredVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenDisapprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenApprovalForKfsEditedVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
}
