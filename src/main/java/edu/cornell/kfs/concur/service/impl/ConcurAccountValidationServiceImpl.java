package edu.cornell.kfs.concur.service.impl;

import java.util.ArrayList;

import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurAccountValidationServiceImpl implements ConcurAccountValidationService {

    @Override
    public ValidationResult validateConcurAccountInfo(ConcurAccountInfo concurAccountInfo) {
        ValidationResult validationResult = new ValidationResult(false, new ArrayList<String>());

        return validationResult;
    }
    
}
