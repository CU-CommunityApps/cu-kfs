package edu.cornell.kfs.pmw.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksTaxRuleService;

public class PaymentWorksTaxRuleServiceImpl implements PaymentWorksTaxRuleService {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksTaxRuleServiceImpl.class);

    public int determineTaxRuleToUseForDataPopulation(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        if (isIndividualUsSsn(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_SSN;
        } else if (isIndividualUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_EIN;
        } else if (isNotIndividualUs(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_US;
        } else if (isIndividualNotUsSsnOrItinTaxClassificationIndividual(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL;
        } else if (isIndividualNotUsSsnOrItinTaxClassificationOther(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER;
        } else if (isIndividualNotUsForeignTaxClassificationIndividual(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL;
        } else if (isIndividualNotUsForeignTaxClassificationOther(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER;
        } else if (isNotIndividualNotUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_EIN;
        } else if (isNotIndividualNotUsForeign(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_FOREIGN;
        }
        return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.COULD_NOT_DETERMINE_TAX_RULE_TO_USE;
    }

    private boolean isIndividualUsSsn(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()));
    }

    private boolean isIndividualUsEin(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeEin(pmwVendor.getRequestingCompanyTinType()));
    }

    private boolean isNotIndividualUs(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode));
    }

    private boolean isIndividualNotUsSsnOrItinTaxClassificationIndividual(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && (isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()) | isTinTypeItin(pmwVendor.getRequestingCompanyTinType()))
                && isTaxClassificationIndividualSolePropriatorSingleMemberLlc(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }

    private boolean isIndividualNotUsSsnOrItinTaxClassificationOther(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && (isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()) | isTinTypeItin(pmwVendor.getRequestingCompanyTinType()))
                && isTaxClassificationOther(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }

    private boolean isIndividualNotUsForeignTaxClassificationIndividual(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType())
                && isTaxClassificationIndividualSolePropriatorSingleMemberLlc(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }

    private boolean isIndividualNotUsForeignTaxClassificationOther(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType())
                && isTaxClassificationOther(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }

    private boolean isNotIndividualNotUsEin(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeEin(pmwVendor.getRequestingCompanyTinType()));
    }

    private boolean isNotIndividualNotUsForeign(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode)
                && isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType()));
    }

    /**
     * PMW does not return the value for the yes/no radio button "For tax purposes are you an individual, sole proprietor or single-member LLC?". This radio
     * button yes/no value must be interpreted from the tax classification construct sent back in the requesting company construct on the vendor. Yes ==> Tax
     * Classification will be 0="individual, sole proprietor or single-member LLC", No ==> Tax Classification is any other value.
     */
    private boolean isPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && pmwVendor.getRequestingCompanyTaxClassificationCode()
                .intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR) ? true : false;
    }

    private boolean isUnitedStatesFipsCountryCode(String countryCode) {
        return (StringUtils.isNotBlank(countryCode) && StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
    }

    private boolean isTinTypeSsn(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode)
                && StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString()));
    }

    private boolean isTinTypeEin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode)
                && StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString()));
    }

    private boolean isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && !(pmwVendor.getRequestingCompanyTaxClassificationCode()
                .intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR)) ? true : false;
    }

    private boolean isNotUnitedStatesFipsCountryCode(String countryCode) {
        return (StringUtils.isNotBlank(countryCode) && !StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
    }

    private boolean isTinTypeItin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode)
                && StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.ITIN.getPmwCodeAsString()));
    }

    private boolean isTaxClassificationIndividualSolePropriatorSingleMemberLlc(Integer taxClassificationCode) {
        return (ObjectUtils.isNotNull(taxClassificationCode)
                && taxClassificationCode.intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
    }

    private boolean isTaxClassificationOther(Integer taxClassificationCode) {
        return (ObjectUtils.isNotNull(taxClassificationCode) && taxClassificationCode.intValue() == PaymentWorksConstants.OTHER_TAX_CLASSIFICATION_INDICATOR);
    }

    private boolean isTinTypeForeignTin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode)
                && StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString()));
    }

}
