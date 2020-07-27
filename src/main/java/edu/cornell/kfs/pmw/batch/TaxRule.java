package edu.cornell.kfs.pmw.batch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public enum TaxRule {
    INDIVIDUAL_US_SSN(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString(),
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), true, true, false), 
    INDIVIDUAL_US_EIN(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(),
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), true, false, true), 
    NOT_INDIVIDUAL_US(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), StringUtils.EMPTY, true, false, true),
    FOREIGN_INDIVIDUAL(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString(), 
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), false, true, false, 
            true, true, "BN"),
    FOREIGN_ENTITY(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), 
            PaymentWorksConstants.PaymentWorksTaxClassification.C_CORPORATION.getTranslationToKfsOwnershipTypeCode(), false, false, true, true, false,
            "BE"),
    OTHER(StringUtils.EMPTY, StringUtils.EMPTY, false, false, false, false, false, StringUtils.EMPTY);

    public final String taxTypeCode;
    public final String ownershipTypeCode;
    public final boolean populateW9Attributes;
    public final boolean populateFirstLastLegalName;
    public final boolean populateBusinessLegalName;
    public final boolean isForeign;
    public final boolean populateDateOfBirth;
    public final String w8TypeCode;
    
    private TaxRule(String taxTypeCode, String ownershipTypeCode, boolean populateW9Attributes, boolean populateFirstLastLegalName, 
            boolean populateBusinessLegalName) {
        this(taxTypeCode, ownershipTypeCode, populateW9Attributes, populateFirstLastLegalName, populateBusinessLegalName, false, false, StringUtils.EMPTY);
    }
    
    private TaxRule(String taxTypeCode, String ownershipTypeCode, boolean populateW9Attributes, boolean populateFirstLastLegalName, 
            boolean populateBusinessLegalName, boolean isForeign, boolean populateDateOfBirth, String w8TypeCode) {
        this.taxTypeCode = taxTypeCode;
        this.ownershipTypeCode = ownershipTypeCode;
        this.populateW9Attributes = populateW9Attributes;
        this.populateFirstLastLegalName = populateFirstLastLegalName;
        this.populateBusinessLegalName = populateBusinessLegalName;
        this.isForeign = isForeign;
        this.populateDateOfBirth = populateDateOfBirth;
        this.w8TypeCode = w8TypeCode;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
