package edu.cornell.kfs.fp.document.validation.impl;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.businessobject.NonresidentTaxPercent;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherNonresidentInformationValidation;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kim.api.identity.Person;

public class CuDisbursementVoucherNonresidentInformationValidation extends DisbursementVoucherNonresidentInformationValidation{
    
	private static final Logger LOG = LogManager.getLogger();
	
    private String validationType;

	@Override
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");

        DisbursementVoucherDocument document = (DisbursementVoucherDocument) accountingDocumentForValidation;
        DisbursementVoucherNonresidentTax nonresidentTax = document.getDvNonresidentTax();
        DisbursementVoucherPayeeDetail payeeDetail = document.getDvPayeeDetail();

        Person financialSystemUser = GlobalVariables.getUserSession().getPerson();

        List<String> taxEditMode = this.getTaxEditMode();
        if (!payeeDetail.isDisbVchrNonresidentPaymentCode()
                || !disbursementVoucherValidationService.hasRequiredEditMode(document, financialSystemUser, taxEditMode)) {
            return true;
        }

        MessageMap errors = GlobalVariables.getMessageMap();
        errors.addToErrorPath(KFSPropertyConstants.DOCUMENT);
        errors.addToErrorPath(KFSPropertyConstants.DV_NONRESIDENT_TAX);

        // ICC SECTION

        /* income class code required */
        if (StringUtils.isBlank(nonresidentTax.getIncomeClassCode())) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS, KFSKeyConstants.ERROR_REQUIRED,
                    "Income class code");
            return false;
        }

        /* country code required, unless income type is nonreportable */
        if (StringUtils.isBlank(nonresidentTax.getPostalCountryCode())
                && !DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_NON_REPORTABLE.equals(nonresidentTax.getIncomeClassCode())) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS, KFSKeyConstants.ERROR_REQUIRED,
                    "Country code");
            return false;
        }

        // income class is FELLOWSHIP
        if(nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_FELLOWSHIP) ){
            // Place holder for logic related to the ICC
        }
        // income class is INDEPENDENT CONTRACTOR
        if(nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_INDEPENDENT_CONTRACTOR)){
            // Place holder for logic related to the ICC
        }
        // income class is ROYALTIES
        if(nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_ROYALTIES)){
            // Place holder for logic related to the ICC
        }
        // income class is NON_REPORTABLE
        if (nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_NON_REPORTABLE)) {
            if ((nonresidentTax.isForeignSourceIncomeCode())
                    || (nonresidentTax.isIncomeTaxTreatyExemptCode())
                    || (nonresidentTax.isTaxOtherExemptIndicator())
                    || (nonresidentTax.isIncomeTaxGrossUpCode())
                    || (nonresidentTax.isTaxUSAIDPerDiemIndicator())
                    || (nonresidentTax.getTaxSpecialW4Amount() != null)
                    || (nonresidentTax.getReferenceFinancialDocumentNumber() != null)
                    || (nonresidentTax.getTaxNQIId() != null)
                    || (nonresidentTax.getPostalCountryCode() != null)) {
                String boxCode = "";
                if(nonresidentTax.isForeignSourceIncomeCode())
                {
                    boxCode = "Foreign Source";
                }
                if(nonresidentTax.isIncomeTaxTreatyExemptCode())
                {
                    boxCode = "Treaty Exempt";
                }
                if(nonresidentTax.isTaxOtherExemptIndicator())
                {
                    boxCode = "Exempt Under Other Code";
                }
                if(nonresidentTax.isIncomeTaxGrossUpCode())
                {
                    boxCode = "Gross Up Payment";
                }
                if(nonresidentTax.isTaxUSAIDPerDiemIndicator())
                {
                    boxCode = "USAID Per Diem";
                }
                if(nonresidentTax.getTaxSpecialW4Amount() != null)
                {
                    boxCode = "Special W-4 Amount";
                }
                if(nonresidentTax.getReferenceFinancialDocumentNumber() != null)
                {
                    boxCode = "Reference Doc";
                }
                if(nonresidentTax.getTaxNQIId() != null)
                {
                    boxCode = "NQI Id";
                }
                if(nonresidentTax.getPostalCountryCode() != null)
                {
                    boxCode = "Country Code";
                }
                errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                        FPKeyConstants.ERROR_DV_NON_REPORTABLE_ONLY, boxCode);
                return false;
            }
        }

        // TAX RATES SECTION

        /* check tax rates */
        if (((nonresidentTax.getFederalIncomeTaxPercent() == null)
                || (nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) == 0))
                && (nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_NON_REPORTABLE))) {
            nonresidentTax.setFederalIncomeTaxPercent(BigDecimal.ZERO);
        }
        else {
            if (nonresidentTax.getFederalIncomeTaxPercent() == null) {
                errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS, KFSKeyConstants.ERROR_REQUIRED,
                        "Federal tax percent");
                return false;
            }
            else {
                // check Federal tax percent is in non-resident alien tax percent table for income class code
                NonresidentTaxPercent taxPercent = new NonresidentTaxPercent();
                taxPercent.setIncomeClassCode(nonresidentTax.getIncomeClassCode());
                taxPercent.setIncomeTaxTypeCode(DisbursementVoucherConstants.FEDERAL_TAX_TYPE_CODE);
                taxPercent.setIncomeTaxPercent(nonresidentTax.getFederalIncomeTaxPercent());

                NonresidentTaxPercent retrievedPercent = (NonresidentTaxPercent) businessObjectService
                        .retrieve(taxPercent);
                if (retrievedPercent == null) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_INVALID_FED_TAX_PERCENT,
                            nonresidentTax.getFederalIncomeTaxPercent().toString(),
                            nonresidentTax.getIncomeClassCode());
                    return false;
                }
            }
        }
        if (((nonresidentTax.getStateIncomeTaxPercent() == null) || (nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) == 0)) && (nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_NON_REPORTABLE)) ) {
            nonresidentTax.setStateIncomeTaxPercent(BigDecimal.ZERO);
        }
        else {
            if (nonresidentTax.getStateIncomeTaxPercent() == null) {
                errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS, KFSKeyConstants.ERROR_REQUIRED,
                        "State tax percent");
                return false;
            }
            else {
                // check State tax percent is in non-resident alien tax percent table for income class code
                NonresidentTaxPercent taxPercent = new NonresidentTaxPercent();
                taxPercent.setIncomeClassCode(nonresidentTax.getIncomeClassCode());
                taxPercent.setIncomeTaxTypeCode(DisbursementVoucherConstants.STATE_TAX_TYPE_CODE);
                taxPercent.setIncomeTaxPercent(nonresidentTax.getStateIncomeTaxPercent());

                PersistableBusinessObject retrievedPercent = businessObjectService.retrieve(taxPercent);
                if (retrievedPercent == null) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_INVALID_STATE_TAX_PERCENT,
                            nonresidentTax.getStateIncomeTaxPercent().toString(),
                            nonresidentTax.getIncomeClassCode());
                    return false;
                }
                else {
                    if ((!document.getDvNonresidentTax().getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_ROYALTIES)) && (!document.getDvNonresidentTax().getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_INDEPENDENT_CONTRACTOR))) {
                        
                        // If fed tax rate is zero, the state tax rate should be zero.
                        if ((document.getDvNonresidentTax().getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) == 0) && (document.getDvNonresidentTax().getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)) {
                                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS, FPKeyConstants.ERROR_DV_STATE_TAX_SHOULD_BE_ZERO );
                                    return false;
                        }
                    }
                }
            }
        }

        // CHECK BOX SECTION

        /*examine check boxes*/

        // the 4 check boxes (Foreign Source, Treaty Exempt, Gross Up Payment, Exempt Under Other Code) shall be mutual
        // exclusive
        if (OneOrLessBoxesChecked(document)) {

            // if Foreign Source is checked
            if (nonresidentTax.isForeignSourceIncomeCode()) {
                // Conditions to be met for "Foreign Source" error to be generated
                // Federal and State tax rate should be zero.
                if ((nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) !=0 )
                        || (nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_FEDERAL_AND_STATE_TAXES_SHOULD_BE_ZERO,
                            "Foreign Source");
                    return false;
                }
                // No other items (mutual exclusiveness checking on USAID Per Diem and Special W-4 Amount are optional
                // here since these are also ensured by their validation later)
                if ((nonresidentTax.isTaxUSAIDPerDiemIndicator())
                        || (nonresidentTax.getTaxSpecialW4Amount() != null)
                        || (nonresidentTax.getReferenceFinancialDocumentNumber() != null)
                        || (nonresidentTax.getTaxNQIId() != null)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_CANNOT_HAVE_VALUE,
                            "Foreign Source", "NQI Id, Reference Doc, USAID Per Diem, or Special W-4 Amount");
                    return false;
                }
            }

            // if Treaty Exempt is checked
            if (nonresidentTax.isIncomeTaxTreatyExemptCode()) {
                // Conditions to be met for "Treaty Exempt" error to be generated
                // No other items (mutual exclusiveness checking on USAID Per Diem and Special W-4 Amount are optional
                // here since these are also ensured by their validation later)
                if ((nonresidentTax.isTaxUSAIDPerDiemIndicator())
                        || (nonresidentTax.getTaxSpecialW4Amount() != null)
                        || (nonresidentTax.getReferenceFinancialDocumentNumber() != null)
                        || (nonresidentTax.getTaxNQIId() != null)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_CANNOT_HAVE_VALUE,
                            "Treaty Exempt", "NQI Id, Reference Doc, USAID Per Diem, or Special W-4 Amount");
                    return false;
                }
            }

            // if Gross Up Payment is checked
            if (nonresidentTax.isIncomeTaxGrossUpCode()) {
                // Conditions to be met for "Gross Up Payment" error to be generated
                // Federal tax rate cannot be zero
                // NOTE: Also, state tax not allowed to be zero for income classes "R" and "I", however, this rule is
                // already checked in the tax rate section, so no need to re-check
                if (nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) == 0) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_FEDERAL_TAX_CANNOT_BE_ZERO,
                            "Gross Up Payment");
                    return false;
                }
                // No other items (mutual exclusiveness checking on USAID Per Diem and Special W-4 Amount are optional
                // here since these are also ensured by their validation later)
                if ((nonresidentTax.isTaxUSAIDPerDiemIndicator())
                        || (nonresidentTax.getTaxSpecialW4Amount() != null)
                        || (nonresidentTax.getReferenceFinancialDocumentNumber() != null)
                        || (nonresidentTax.getTaxNQIId() != null)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_CANNOT_HAVE_VALUE,
                            "Gross Up Payment", "NQI Id, Reference Doc, USAID Per Diem, or Special W-4 Amount");
                    return false;
                }
            }

            // if Exempt Under Other Code is checked
            if (nonresidentTax.isTaxOtherExemptIndicator()) {
                // also exists in PurapPropertyConstants.java as PurapPropertyConstants.TAX_OTHER_EXEMPT_INDICATOR
                // Conditions to be met for "Exempt Under Other Code" error to be generated
                // Federal and State tax rate should be zero.
                if ((nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)
                        || (nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_FEDERAL_AND_STATE_TAXES_SHOULD_BE_ZERO,
                            "Exempt Under Other Code");
                    return false;
                }
            }

            // if USAID Per Diem is checked
            if (nonresidentTax.isTaxUSAIDPerDiemIndicator()) {
                // Conditions to be met for "USAID Per Diem" error to be generated
                // income class code should be fellowship
                if (!nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_FELLOWSHIP)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_SHOULD_BE_SELECTED,
                            "USAID Per Diem", "Income Class Code : Fellowship");
                    return false;
                }
                // Federal and State tax rate should be zero.
                if ((nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)
                        || (nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_FEDERAL_AND_STATE_TAXES_SHOULD_BE_ZERO,
                            "USAID Per Diem");
                    return false;
                }
                // Exempt Under Other Code should be checked; this will ensure the other 3 check boxes not checked due
                // to mutual exclusiveness
                if (!nonresidentTax.isTaxOtherExemptIndicator()) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_SHOULD_BE_SELECTED,
                            "USAID Per Diem", "Exempt Under Other Code");
                    return false;
                }
                // Special W-4 Amount shall have no value
                if (nonresidentTax.getTaxSpecialW4Amount() != null) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_CANNOT_HAVE_VALUE,
                            "USAID Per Diem", "Special W-4 Amount");
                    return false;
                }
            }

            // if Special W-4 Amount is entered
            if (nonresidentTax.getTaxSpecialW4Amount() != null) {
                // Conditions to be met for "Special W-4 Amount" error to be generated
                // income class code should be fellowship
                if (!nonresidentTax.getIncomeClassCode().equals(DisbursementVoucherConstants.NONRESIDENT_TAX_INCOME_CLASS_FELLOWSHIP)) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_SHOULD_BE_SELECTED,
                            "Special W-4 Amount", "Income Class Code : Fellowship");
                    return false;
                }
                // Federal and State tax rate should be zero.
                if (nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0
                        || nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_FEDERAL_AND_STATE_TAXES_SHOULD_BE_ZERO,
                            "Special W-4 Amount");
                    return false;
                }
                // Exempt Under Other Code should be checked; this will ensure the other 3 check boxes not checked due
                // to mutual exclusiveness
                if (!nonresidentTax.isTaxOtherExemptIndicator()) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_SHOULD_BE_SELECTED,
                            "Special W-4 Amount", "Exempt Under Other Code");
                    return false;
                }
                // USAID Per Diem should not be checked (mutual exclusive checking on USAID Per Diem here is optional
                // since this is also ensured by validation on Special W-4 Amount above
                if (nonresidentTax.isTaxUSAIDPerDiemIndicator()) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_TAX_WHEN_CHECKED_CANNOT_BE_SELECTED,
                            "Special W-4 Amount", "USAID Per Diem");
                    return false;
                }
            }
        }

        // RUN FOR SUBMISSION

        if (!"GENERATE".equals(validationType)) {
            // verify tax lines have been generated
            if (nonresidentTax.getFederalIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0
                    || nonresidentTax.getStateIncomeTaxPercent().compareTo(BigDecimal.ZERO) != 0) {
                if (StringUtils.isBlank(nonresidentTax.getFinancialDocumentAccountingLineText())) {
                    errors.putErrorWithoutFullErrorPath(KFSConstants.GENERAL_NONRESIDENTTAX_TAB_ERRORS,
                            FPKeyConstants.ERROR_DV_NONRESIDENT_NO_TAX_LINES_GENERATED);
                    return false;
                }
            }
        }

        errors.removeFromErrorPath(KFSPropertyConstants.DV_NONRESIDENT_TAX);
        errors.removeFromErrorPath(KFSPropertyConstants.DOCUMENT);

        return true;
    }
    
    public void setValidationType(String validationType) {
		this.validationType = validationType;
	}
    

}
