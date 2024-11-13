package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.batch.TaxDataRow;
import edu.cornell.kfs.tax.businessobject.SprintaxReportParameters;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.service.TaxProcessingService;
import edu.cornell.kfs.tax.util.TaxUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Immutable helper class containing various data to assist with 1042S tax processing.
 */
class SprintaxPaymentSummary extends TransactionDetailSummary {
    private static final int SMALL_KEY_SIZE = 30;

    // Maps object codes to income class codes.
    final Map<String,String> objectCodeToIncomeClassCodeMap;
    // Maps income class codes to IRS income codes.
    final Map<String,String> incomeClassCodeToIrsIncomeCodeMap;
    // Maps income class codes to IRS income code subtypes.
    final Map<String,String> incomeClassCodeToIrsIncomeCodeSubTypeMap;
    // Maps date-and-docNumber-and-docLineNumber combos to 1042S box overrides.
    final Map<String,String> transactionOverrides;
    // Maps vendor ownership codes to Chapter 3 status codes.
    final Map<String,String> vendorOwnershipToChapter3StatusMap;
    // Maps Chapter 4 status codes to Chapter 4 exemption codes.
    final Map<String,String> chapter4StatusToChapter4ExemptionMap;
    // Maps object codes to sets of chart-and-account combo strings, which denote royalties-related rows to be included.
    final Map<String,Set<String>> royaltiesIncludedObjectCodeChartAccount;
    // Maps object codes to sets of chart-and-account combo strings, which denote fed-tax-withheld rows to be included.
    final Map<String,Set<String>> fedTaxWithheldObjectCodeChartAccount;
    // Maps object codes to sets of chart-and-account combo strings, which denote state-income-tax-withheld rows to be included.
    final Map<String,Set<String>> stateIncTaxWithheldObjectCodeChartAccount;
    // Maps object codes to DV check stub text fragments.
    final Map<String,List<Pattern>> royaltiesIncludedObjectCodeAndDvCheckStubTextMap;
    // Identifies fed-tax-withheld object codes.
    final Set<String> federalTaxWithheldObjectCodes;
    // Identifies state-income-tax-withheld object codes.
    final Set<String> stateTaxWithheldObjectCodes;
    // The PDP doc types to exclude from 1042S tax processing.
    final Set<String> pdpExcludedDocTypes;
    // The income class codes that denote royalties.
    final Set<String> incomeClassCodesDenotingRoyalties;
    // The payment reason code(s) indicating that the row should be excluded.
    final Set<String> excludedPaymentReasonCodes;
    // The line 1 address prefixes (such as "Petty Cash") indicating that the row should be excluded.
    final List<String> otherIncomeExcludedPaymentLine1AddressPrefixes;
    // The document note text prefixes indicating that the row should be excluded.
    final List<String> excludedDocumentNoteTextPrefixes;
    // The state code/name for state-tax-withheld purposes.
    final String stateCode;
    // The income code to use for non-reportable income.
    final String nonReportableIncomeCode;
    // The income code to use for excluded income.
    final String excludedIncomeCode;
    // The income code sub-type to use for excluded income.
    final String excludedIncomeCodeSubType;
    // The Chapter 3 exemption code representing not-exempt values.
    final String chapter3NotExemptExemptionCode;
    // The Chapter 3 exemption code representing tax treaty exemptions.
    final String chapter3TaxTreatyExemptionCode;
    // The Chapter 3 exemption code representing foreign source exemptions.
    final String chapter3ForeignSourceExemptionCode;
    // The default Chapter 4 exemption code to use if no ch4-status-to-ch4-exemption mapping exists for the transaction row.
    final String chapter4DefaultExemptionCode;
    // Indicates whether each entry in royaltiesIncludedObjectCodeChartAccount is a whitelist (true) or a blacklist (false).
    final boolean royaltiesObjCodeChartAccountIsWhitelist;
    // Indicates whether each entry in royaltiesIncludedObjectCodeAndDvCheckStubTextMap is a whitelist (true) or a blacklist (false).
    final boolean royaltiesObjCodeDvChkStubTextIsWhitelist;
    // Indicates whether each entry in fedTaxWithheldObjectCodeChartAccount is a whitelist (true) or a blacklist (false).
    final boolean ftwObjCodeChartAccountIsWhitelist;
    // Indicates whether each entry in stateIncTaxWithheldObjectCodeChartAccount is a whitelist (true) or a blacklist (false).
    final boolean sitwObjCodeChartAccountIsWhitelist;



    SprintaxPaymentSummary(SprintaxReportParameters sprintaxReportParameters, Map<String,TaxDataRow> dataRows) {
        super(sprintaxReportParameters.getReportYear(), sprintaxReportParameters.getStartDate(), sprintaxReportParameters.getEndDate(), true,
                CUTaxConstants.TAX_TYPE_1042S, CUTaxConstants.TAX_1042S_PARM_DETAIL, dataRows);
        
        ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
        List<TransactionOverride> tempOverrides;
        Map<String,String> tempMap;
        Map<String,Set<String>> tempValuesMap;
        Map<String,List<Pattern>> tempPatternsMap;
        String tempValue;
        
        // Prepare object-code-to-income-class-code map using an income-class-code-to-object-codes parameter.
        this.objectCodeToIncomeClassCodeMap = convertMultiValueListingsToInvertedMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES));
        
        // Prepare income-code-to-IRS-code map using the associated parameter.
        this.incomeClassCodeToIrsIncomeCodeMap = convertValuePairsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE));
        
        // Prepare income-code-to-IRS-code-subtype map using the associated parameter.
        this.incomeClassCodeToIrsIncomeCodeSubTypeMap = convertValuePairsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE_SUB_TYPE));
        
        // Prepare map from date-and-docNumber-and-docLineNumber combos to 1042S tax box overrides using the associated TransactionOverride objects.
        tempMap = new HashMap<String,String>();

        TaxProcessingService taxProcessingService = SpringContext.getBean(TaxProcessingService.class);
        tempOverrides = taxProcessingService.getTransactionOverrides(
                CUTaxConstants.TAX_TYPE_1042S,
                sprintaxReportParameters.getStartDate(),
                sprintaxReportParameters.getEndDate()
        );

        for (TransactionOverride transactionOverride : tempOverrides) {
            tempMap.put(
                    new StringBuilder(SMALL_KEY_SIZE).append(transactionOverride.getUniversityDate()).append(';')
                            .append(transactionOverride.getDocumentNumber()).append(';')
                            .append(transactionOverride.getFinancialDocumentLineNumber()).toString(),
                    getBoxNumberConstant(transactionOverride.getBoxNumber()));
        }
        this.transactionOverrides = Collections.unmodifiableMap(tempMap);
        
        // Prepare vendor-ownership-to-ch3-status map using the associated parameter.
        this.vendorOwnershipToChapter3StatusMap = convertValuePairsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.VENDOR_OWNERSHIP_TO_CHAPTER3_STATUS_CODE));
        
        // Prepare ch4-status-to-ch4-exemption map using the associated parameter.
        this.chapter4StatusToChapter4ExemptionMap = convertValuePairsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES));
        
        // Prepare object-code-to-chart-and-account-combo map (for royalties) using the associated parameter.
        tempValuesMap = new HashMap<String,Set<String>>();
        this.royaltiesObjCodeChartAccountIsWhitelist = populateValuesMapAndDetermineIfWhitelist(tempValuesMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT));
        this.royaltiesIncludedObjectCodeChartAccount = !tempValuesMap.isEmpty()
                ? Collections.unmodifiableMap(tempValuesMap) : Collections.<String,Set<String>>emptyMap();
        
        // Prepare object-code-to-chart-and-account-combo map (for fed-tax-withheld) using the associated parameter.
        tempValuesMap = new HashMap<String,Set<String>>();
        this.ftwObjCodeChartAccountIsWhitelist = populateValuesMapAndDetermineIfWhitelist(tempValuesMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL,
                        Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT));
        this.fedTaxWithheldObjectCodeChartAccount = !tempValuesMap.isEmpty()
                ? Collections.unmodifiableMap(tempValuesMap) : Collections.<String,Set<String>>emptyMap();
        
        // Prepare object-code-to-chart-and-account-combo map (for state-inc-tax-withheld) using the associated parameter.
        tempValuesMap = new HashMap<String,Set<String>>();
        this.sitwObjCodeChartAccountIsWhitelist = populateValuesMapAndDetermineIfWhitelist(tempValuesMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL,
                        Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT));
        this.stateIncTaxWithheldObjectCodeChartAccount = !tempValuesMap.isEmpty()
                ? Collections.unmodifiableMap(tempValuesMap) : Collections.<String,Set<String>>emptyMap();
        
        // Prepare object-code-to-DV-check-stub-fragments map using the associated parameter.
        tempPatternsMap = new HashMap<String,List<Pattern>>();
        this.royaltiesObjCodeDvChkStubTextIsWhitelist = populatePatternsMapAndDetermineIfWhitelist(tempPatternsMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL,
                        Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT));
        this.royaltiesIncludedObjectCodeAndDvCheckStubTextMap = !tempPatternsMap.isEmpty()
                ? Collections.unmodifiableMap(tempPatternsMap) : Collections.<String,List<Pattern>>emptyMap();
        
        // Prepare fed-tax-withheld-object-codes set using the associated parameter.
        this.federalTaxWithheldObjectCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES));
        
        // Prepare fed-tax-withheld-object-codes set using the associated parameter.
        this.stateTaxWithheldObjectCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES));
        
        // Prepare pdp-excluded-doc-types set using the associated parameter.
        this.pdpExcludedDocTypes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES));
        
        // Prepare income-class-codes-denoting-royalties set using the associated parameter.
        this.incomeClassCodesDenotingRoyalties = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.INCOME_CLASS_CODE_DENOTING_ROYALTIES));
        
        // Prepare the excluded Payment Reason code(s) from the associated parameter.
        this.excludedPaymentReasonCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_PAYMENT_REASON_CODE));
        
        // Prepare the excluded Payment Line 1 Address prefixes from the associated parameter.
        this.otherIncomeExcludedPaymentLine1AddressPrefixes = convertPrefixesToList(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR));
        
        // Prepare the excluded document note text prefixes from the associated parameter.
        this.excludedDocumentNoteTextPrefixes = convertPrefixesToList(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_DOC_NOTE_TEXT));
        
        // Prepare the state code from the associated parameter.
        this.stateCode = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.STATE_NAME);
        
        // Prepare the non-reportable income code from the associated parameter.
        this.nonReportableIncomeCode = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.NON_REPORTABLE_INCOME_CODE);
        
        // Prepare the excluded income code from the associated parameter.
        this.excludedIncomeCode = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_INCOME_CODE);
        
        // Prepare the excluded income code sub-type from the associated parameter.
        this.excludedIncomeCodeSubType = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.EXCLUDED_INCOME_CODE_SUB_TYPE);
        
        // Prepare the default Chapter 4 exemption code from the associated paramter.
        this.chapter4DefaultExemptionCode = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.CHAPTER4_DEFAULT_EXEMPTION_CODE);
        
        // Prepare various Chapter 3 exemption code constants from the associated parameter.
        tempMap = convertValuePairsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1042S_PARM_DETAIL, Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES));
        // Not-exempt code.
        tempValue = tempMap.get(CUTaxConstants.CH3_EXEMPTION_NOT_EXEMPT_KEY);
        this.chapter3NotExemptExemptionCode = StringUtils.isNotBlank(tempValue) ? tempValue : KFSConstants.EMPTY_STRING;
        // Tax-treaty code.
        tempValue = tempMap.get(CUTaxConstants.CH3_EXEMPTION_TAX_TREATY_KEY);
        this.chapter3TaxTreatyExemptionCode = StringUtils.isNotBlank(tempValue) ? tempValue : KFSConstants.EMPTY_STRING;
        // Foreign-source code.
        tempValue = tempMap.get(CUTaxConstants.CH3_EXEMPTION_FOREIGN_SOURCE_KEY);
        this.chapter3ForeignSourceExemptionCode = StringUtils.isNotBlank(tempValue) ? tempValue : KFSConstants.EMPTY_STRING;
    }



    private String getBoxNumberConstant(String boxNumber) {
        if (StringUtils.isNotBlank(boxNumber) && (CUTaxConstants.FORM_1042S_GROSS_BOX.equalsIgnoreCase(boxNumber)
                || CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX.equalsIgnoreCase(boxNumber)
                || CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX.equalsIgnoreCase(boxNumber)
                || CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY.equalsIgnoreCase(boxNumber))) {
            return boxNumber.toUpperCase(Locale.US);
        }
        return null;
    }

}
