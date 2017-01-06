package edu.cornell.kfs.concur.service;

import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public interface ConcurAccountValidationService {
    
    ValidationResult validateConcurAccountInfo(ConcurAccountInfo concurAccountInfo);

}
