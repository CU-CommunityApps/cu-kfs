package edu.cornell.kfs.pmw.batch.service;

import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksTaxRuleService {
    
    int determineTaxRuleToUseForDataPopulation(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode);

}
