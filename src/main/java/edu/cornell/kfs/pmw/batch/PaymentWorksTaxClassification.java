package edu.cornell.kfs.pmw.batch;

public enum PaymentWorksTaxClassification {
    INDIVIDUAL_SOLE_PROPRIETOR(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR, "Individual/sole proprietor or single-member LLC", "ID"),
    C_CORPORATION(PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "C Corporation", "CP"),
    S_CORPORATION(PaymentWorksConstants.S_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "S Corporation", "SC"),
    PARTNERSHIP(PaymentWorksConstants.PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR, "Partnership", "PT"),
    TRUST_ESTATE(PaymentWorksConstants.TRUST_ESTATE_TAX_CLASSIFICATION_INDICATOR, "Trust/estate", "ET"), 
    LLC_TAXED_AS_C_CORPORATION(PaymentWorksConstants.LLC_TAXED_AS_C_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as C Corporation", "CP"),
    LLC_TAXED_AS_S_CORPORATION(PaymentWorksConstants.LLC_TAXED_AS_S_CORPORATION_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as S Corporation", "SC"),
    LLC_TAXED_AS_PARTNERSHIP(PaymentWorksConstants.LLC_TAXED_AS_PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR, "LLC taxed as Partnership", "PT"),
    OTHER(PaymentWorksConstants.OTHER_TAX_CLASSIFICATION_INDICATOR, "Other", "OT");
    
    public final int pmwCode;
    public final String pmwDescription;
    public final String translationToKfsOwnershipTypeCode;
    
    private PaymentWorksTaxClassification(int pmwCode, String pmwDescription, String translationToKfsOwnershipTypeCode) {
        this.pmwCode = pmwCode;
        this.pmwDescription = pmwDescription;
        this.translationToKfsOwnershipTypeCode = translationToKfsOwnershipTypeCode;
    }
    
    public String getTranslationToKfsOwnershipTypeCode() {
        return translationToKfsOwnershipTypeCode;
    }
    
    public static PaymentWorksTaxClassification findPaymentWorksTaxClassification(int requestingCompanyTaxClassificationCode) {
        for (PaymentWorksTaxClassification classification : PaymentWorksTaxClassification.values()) {
            if (classification.pmwCode == requestingCompanyTaxClassificationCode) {
                return classification;
            }
        }
        throw new IllegalArgumentException("Unable to find a tax classficaiton for code " + requestingCompanyTaxClassificationCode);
    }
}
