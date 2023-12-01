package edu.cornell.kfs.module.ar.document.validation;

import org.kuali.kfs.module.ar.document.validation.impl.CustomerRule;

public class CuCustomerRule extends CustomerRule {
    
    @Override
    public boolean checkNameIsValidLength(final String customerName) {
        return true;
    }

}
