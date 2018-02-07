package edu.cornell.kfs.pmw.batch.service;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.Note;

public interface PaymentWorksBatchUtilityService {
    
    String retrievePaymentWorksParameterValue(String parameterName);
    
    String getFileContents(String fileName);
    
    Note createNote(String noteText);
    
    Person getSystemUser();
    
}
