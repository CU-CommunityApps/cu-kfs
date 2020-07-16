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
            PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode(), false, true, false, true, true),
    FOREIGN_ENTITY(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), StringUtils.EMPTY, false, false, true, true, false),
    OTHER(StringUtils.EMPTY, StringUtils.EMPTY, false, false, false, false, false);

    public final String taxTypeCode;
    public final String ownershipTypeCode;
    public final boolean populateW9Attributes;
    public final boolean populateFirstLastLegalName;
    public final boolean populateBusinessLegalName;
    public final boolean foreign;
    public final boolean dateOfBirth;
    
    private TaxRule(String taxTypeCode, String ownershipTypeCode, boolean populateW9Attributes, boolean populateFirstLastLegalName, 
            boolean populateBusinessLegalName) {
        this(taxTypeCode, ownershipTypeCode, populateW9Attributes, populateFirstLastLegalName, populateBusinessLegalName, false, false);
    }
    
    private TaxRule(String taxTypeCode, String ownershipTypeCode, boolean populateW9Attributes, boolean populateFirstLastLegalName, 
            boolean populateBusinessLegalName, boolean foreign, boolean dateOfBirth) {
        this.taxTypeCode = taxTypeCode;
        this.ownershipTypeCode = ownershipTypeCode;
        this.populateW9Attributes = populateW9Attributes;
        this.populateFirstLastLegalName = populateFirstLastLegalName;
        this.populateBusinessLegalName = populateBusinessLegalName;
        this.foreign = foreign;
        this.dateOfBirth = dateOfBirth;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }
}
