package edu.cornell.kfs.pmw.batch.service;

import org.kuali.rice.kim.api.identity.Person;

import org.kuali.kfs.krad.bo.Note;

import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;

public interface PaymentWorksBatchUtilityService {
    
    String retrievePaymentWorksParameterValue(String parameterName);
    
    String getFileContents(String fileName);
    
    KfsVendorDataWrapper createNoteRecordingAnyErrors(KfsVendorDataWrapper kfsVendorDataWrapper, String noteText, String noteErrorDescriptor);
    
    Person getSystemUser();
    
}
