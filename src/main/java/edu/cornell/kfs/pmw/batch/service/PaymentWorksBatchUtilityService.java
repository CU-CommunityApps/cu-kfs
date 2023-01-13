package edu.cornell.kfs.pmw.batch.service;

import java.util.List;
import java.util.Map;

import org.springframework.util.AutoPopulatingList;

import org.kuali.kfs.kim.impl.identity.Person;

import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksBankAccountType;
import edu.cornell.kfs.pmw.batch.businessobject.KfsAchDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;

public interface PaymentWorksBatchUtilityService {
    
    String retrievePaymentWorksParameterValue(String parameterName);
    
    String getFileContents(String fileName);
    
    KfsVendorDataWrapper createNoteRecordingAnyErrors(KfsVendorDataWrapper kfsVendorDataWrapper, String noteText, String noteErrorDescriptor);
    
    KfsAchDataWrapper createNoteRecordingAnyErrors(KfsAchDataWrapper kfsAchDataWrapper, String noteText, String noteErrorDescriptor);
    
    Person getSystemUser();
    
    boolean foundExistingPaymentWorksVendorByKfsDocumentNumber(String kfsDocumentNumber);
    
    boolean foundExistingPaymentWorksVendorByPaymentWorksVendorId(String pmwVendorId);
    
    void registerKfsPvenApprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenApprovalForKfsEnteredVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenDisapprovalForExistingPaymentWorksVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    void registerKfsPvenApprovalForKfsEditedVendor(String kfsVendorDocumentNumber, VendorDetail vendorDetail);
    
    List<String> convertReportDataValidationErrors(Map<String, AutoPopulatingList<ErrorMessage>> kfsGlobalVariablesMessageMap);
    
    List<PaymentWorksBankAccountType> findAllPmwBankAccountTypesMatching(String pmwBankAccountTypeToVerify);
    
    boolean isPaymentWorksIntegrationProcessingEnabled();
}
