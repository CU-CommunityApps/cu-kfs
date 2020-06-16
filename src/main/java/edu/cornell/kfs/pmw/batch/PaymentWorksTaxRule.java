package edu.cornell.kfs.pmw.batch;

public enum PaymentWorksTaxRule {
    INDIVIDUAL_US_SSN(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_SSN),
    INDIVIDUAL_US_EIN(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_EIN),
    NOT_INDIVIDUAL_US(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_US),
    INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL),
    INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER),
    INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL),
    INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER),
    NOT_INDIVIDUAL_NOT_US_EIN(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_EIN),
    NOT_INDIVIDUAL_NOT_US_FOREIGN(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_FOREIGN),
    COULD_NOT_DETERMINE_TAX_RULE_TO_USE(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.COULD_NOT_DETERMINE_TAX_RULE_TO_USE);
    
    public final int ruleId;
    
    
    private PaymentWorksTaxRule(int ruleId) {
        this.ruleId = ruleId;
    }    
}
