package edu.cornell.kfs.tax.dataaccess.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1099ParameterNames;
import edu.cornell.kfs.tax.batch.TaxDataRow;
import edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.service.TaxProcessingService;
import edu.cornell.kfs.tax.util.TaxUtils;

/**
 * Immutable helper class containing various data to assist with 1099 tax processing.
 */
class Transaction1099Summary extends TransactionDetailSummary {

    private static final int SMALL_KEY_SIZE = 30;

    final Map<String, TaxTableField> boxNumberMappings;
    // Maps object-code-and-DV-payment-reason combos to 1099 tax boxes.
    final Map<String,TaxTableField> objectCodeBucketMappings;
    // Maps date-and-docNumber-and-docLineNumber combos to 1099 tax box overrides.
    final Map<String,TaxTableField> transactionOverrides;
    // Maps vendor ownership codes to zero or more ownership categories.
    final Map<String,Set<String>> vendorOwnershipToCategoryMappings;
    // The payees to exclude from the processing.
    final Set<String> excludedPayeeIds;
    // The PDP doc types to exclude from the processing.
    final Set<String> pdpExcludedDocTypes;
    // The payment reason codes to exclude from the processing.
    final Set<String> excludedPaymentReasonCodes;
    // The payment line 1 address prefixes to exclude from the processing.
    final List<String> otherIncomeExcludedPaymentLine1AddressPrefixes;
    // The doc note text prefixes to exclude from the processing.
    final List<String> excludedDocumentNoteTextPrefixes;
    // Maps object codes to initiator netids for exclusion from the processing.
    final Map<String,Set<String>> excludedInitiatorNetIdsForObjectCodes;
    // Maps object codes to chart-and-account combos for inclusions/exclusion processing.
    final Map<String,Set<String>> royaltiesIncludedObjectCodeChartAccount;
    // Indicates whether each value of royaltiesIncludedObjectCodeChartAccount is a whitelist (true) or a blacklist (false).
    final boolean royaltiesObjCodeChartAccountIsWhitelist;
    // Maps object codes to vendor name fragments for inclusion/exclusion processing.
    final Map<String,List<Pattern>> nonEmployeeCompIncludedObjectCodeVendorName;
    // Indicates whether each value of nonEmployeeCompIncludedObjectCodeVendorName is a whitelist (true) or a blacklist (false).
    final boolean nonEmployeeCompObjCodeVendorNameIsWhitelist;
    // Maps object codes to parent vendor name fragments for inclusion/exclusion processing.
    final Map<String,List<Pattern>> nonEmployeeCompIncludedObjectCodeParentVendorName;
    // Indicates whether each value of nonEmployeeCompIncludedObjectCodeParentVendorName is a whitelist (true) or a blacklist (false).
    final boolean nonEmployeeCompObjCodeParentVendorNameIsWhitelist;
    // Maps object codes to doc title fragments for inclusion/exclusion processing.
    final Map<String,List<Pattern>> nonEmployeeCompIncludedObjectCodeDocTitle;
    // Indicates whether each value of nonEmployeeCompIncludedObjectCodeDocTitle is a whitelist (true) or a blacklist (false).
    final boolean nonEmployeeCompObjCodeDocTitleIsWhitelist;
    // The tab site ID to use for the tab records in the output file.
    final String tabSiteId;
    // The sub-maps associate object codes with DV check stub fragments for inclusion/exclusion, the outer map associates tax boxes with the sub-maps.
    final Map<TaxTableField,Map<String,List<Pattern>>> taxBoxIncludedDvCheckStubTextMaps;
    // Indicates whether the values of the various taxBoxIncludedDvCheckStubTextMaps sub-maps are whitelists (true) or blacklists (false).
    final Map<TaxTableField,Boolean> taxBoxDvCheckStubTextsAreWhitelists;
    //final Map<>

    Transaction1099Summary(int reportYear, java.sql.Date startDate, java.sql.Date endDate, boolean vendorForeign, Map<String,TaxDataRow> dataRows) {
        super(reportYear, startDate, endDate, vendorForeign, CUTaxConstants.TAX_TYPE_1099, CUTaxConstants.TAX_1099_PARM_DETAIL, dataRows);
        
        ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
        TaxProcessingService taxProcessingService = SpringContext.getBean(TaxProcessingService.class);
        Map<String,TaxTableField> tempMap;
        Map<String,Set<String>> tempListingsMap;
        Map<String,List<Pattern>> tempPatternsMap;
        Map<TaxTableField,Map<String,List<Pattern>>> tempCheckStubTextMaps;
        Map<TaxTableField,Boolean> tempWhitelistFlagsMap;
        boolean isPatternMappingWhitelist;
        Map<TaxTableField,String> dvCheckStubTextParamNames = new HashMap<TaxTableField,String>();
        
        // Setup temporary map from tax boxes to DV-check-stub-text-patterns parameter names.
        dvCheckStubTextParamNames.put(derivedValues.miscRents, Tax1099ParameterNames.RENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscRoyalties, Tax1099ParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscOtherIncome, Tax1099ParameterNames.OTHER_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscFedIncomeTaxWithheld, Tax1099ParameterNames.FED_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscFishingBoatProceeds, Tax1099ParameterNames.FISHING_BOAT_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscMedicalHealthcarePayments, Tax1099ParameterNames.MEDICAL_HEALTH_CARE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscSubstitutePayments, Tax1099ParameterNames.SUBSTITUTE_PAYMENTS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscCropInsuranceProceeds, Tax1099ParameterNames.CROP_INSURANCE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscGrossProceedsAttorney, Tax1099ParameterNames.ATTORNEY_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscSection409ADeferral, Tax1099ParameterNames.SECTION_409A_DEFERRALS_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscGoldenParachute, Tax1099ParameterNames.GOLDEN_PARACHUTE_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        // TODO: Do we need a Box 14 parameter mapping?
        dvCheckStubTextParamNames.put(derivedValues.miscStateTaxWithheld, Tax1099ParameterNames.STATE_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.miscStateIncome, Tax1099ParameterNames.STATE_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.necNonEmployeeCompensation, Tax1099ParameterNames.NONEMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        // TODO: Do we need separate parameters for the 1099-NEC tax fields?
        dvCheckStubTextParamNames.put(derivedValues.necFedIncomeTaxWithheld, Tax1099ParameterNames.FED_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.necStateTaxWithheld, Tax1099ParameterNames.STATE_WITHHELD_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        dvCheckStubTextParamNames.put(derivedValues.necStateIncome, Tax1099ParameterNames.STATE_INCOME_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT);
        
        this.boxNumberMappings = parseBoxNumberMappings(parameterService);
        
        // Construct a Map from object-code-and-DV-payment-reason combos to 1099 tax buckets.
        tempMap = new HashMap<String,TaxTableField>();
        List<ObjectCodeBucketMapping> allBucketMappings = taxProcessingService.getBucketMappings(CUTaxConstants.TAX_TYPE_1099);
        for (ObjectCodeBucketMapping bucketMapping : allBucketMappings) {
            String boxNumberMappingKey = TaxUtils.build1099BoxNumberMappingKey(
                    bucketMapping.getFormType(), bucketMapping.getBoxNumber());
            tempMap.put(
                    new StringBuilder().append(bucketMapping.getFinancialObjectCode()).append(';')
                            .append(bucketMapping.getDvPaymentReasonCode()).toString(),
                    boxNumberMappings.get(boxNumberMappingKey));
        }
        this.objectCodeBucketMappings = Collections.unmodifiableMap(tempMap);
        
        // Construct a Map from date-and-docNumber-and-docLineNumber combos to 1099 tax bucket overrides.
        tempMap = new HashMap<String,TaxTableField>();
        List<TransactionOverride> overrides = taxProcessingService.getTransactionOverrides(CUTaxConstants.TAX_TYPE_1099, startDate, endDate);
        for (TransactionOverride transactionOverride : overrides) {
            String boxNumberMappingKey = TaxUtils.build1099BoxNumberMappingKey(
                    transactionOverride.getFormType(), transactionOverride.getBoxNumber());
            tempMap.put(
                    new StringBuilder(SMALL_KEY_SIZE).append(transactionOverride.getUniversityDate()).append(';')
                            .append(transactionOverride.getDocumentNumber()).append(';')
                            .append(transactionOverride.getFinancialDocumentLineNumber()).toString(),
                    boxNumberMappings.get(boxNumberMappingKey));
        }
        this.transactionOverrides = Collections.unmodifiableMap(tempMap);
        
        // Construct a Map from vendor ownership codes to zero or more vendor categories.
        this.vendorOwnershipToCategoryMappings = convertMultiValueListingsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.INCLUDED_VENDOR_OWNERS_AND_CATEGORIES));
        
        // Construct a Set of payees to exclude from the 1099 processing.
        this.excludedPayeeIds = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.EXCLUDED_PAYEE_ID));
        
        // Construct a Set of PDP doc types to exclude from the 1099 processing.
        this.pdpExcludedDocTypes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.PDP_EXCLUDED_DOC_TYPES));
        
        // Construct a Set of payment reason codes to exclude from the 1099 processing.
        this.excludedPaymentReasonCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.EXCLUDED_PAYMENT_REASON_CODE));
        
        // Construct a List of payment line1 address prefixes to exclude from the 1099 processing.
        this.otherIncomeExcludedPaymentLine1AddressPrefixes = convertPrefixesToList(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR));
        
        // Construct a List of doc note text prefixes to exclude from the 1099 processing.
        this.excludedDocumentNoteTextPrefixes = convertPrefixesToList(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.EXCLUDED_DOC_NOTE_TEXT));
        
        // Construct a Map of object codes to principal names for exclusion from the 1099 processing.
        this.excludedInitiatorNetIdsForObjectCodes = convertValueListingsToMap(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.EXCLUDED_OBJECT_CODE_AND_INITIATOR_PRNCPL_NM));
        
        // Construct inclusion/exclusion mappings from object codes to chart-and-account combos.
        tempListingsMap = new HashMap<String,Set<String>>();
        this.royaltiesObjCodeChartAccountIsWhitelist = populateValuesMapAndDetermineIfWhitelist(tempListingsMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT));
        this.royaltiesIncludedObjectCodeChartAccount = !tempListingsMap.isEmpty()
                ? Collections.unmodifiableMap(tempListingsMap) : Collections.<String,Set<String>>emptyMap();
        
        // Construct inclusion/exclusion mappings from object codes to vendor names.
        tempPatternsMap = new HashMap<String,List<Pattern>>();
        this.nonEmployeeCompObjCodeVendorNameIsWhitelist = populatePatternsMapAndDetermineIfWhitelist(tempPatternsMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL,
                        Tax1099ParameterNames.NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_VENDOR_NAME));
        this.nonEmployeeCompIncludedObjectCodeVendorName = !tempPatternsMap.isEmpty()
                ? Collections.unmodifiableMap(tempPatternsMap) : Collections.<String,List<Pattern>>emptyMap();
        
        // Construct inclusion/exclusion mappings from object codes to parent vendor names.
        tempPatternsMap = new HashMap<String,List<Pattern>>();
        this.nonEmployeeCompObjCodeParentVendorNameIsWhitelist = populatePatternsMapAndDetermineIfWhitelist(tempPatternsMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL,
                        Tax1099ParameterNames.NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_PARENT_VENDOR_NAME));
        this.nonEmployeeCompIncludedObjectCodeParentVendorName = !tempPatternsMap.isEmpty()
                ? Collections.unmodifiableMap(tempPatternsMap) : Collections.<String,List<Pattern>>emptyMap();
        
        // Construct inclusion/exclusion mappings from object codes to document titles.
        tempPatternsMap = new HashMap<String,List<Pattern>>();
        this.nonEmployeeCompObjCodeDocTitleIsWhitelist = populatePatternsMapAndDetermineIfWhitelist(tempPatternsMap, parameterService.getParameter(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL,
                        Tax1099ParameterNames.NON_EMPLOYEE_COMPENSATION_INCLUDED_OBJECT_CODE_AND_DOC_TITLE));
        this.nonEmployeeCompIncludedObjectCodeDocTitle = !tempPatternsMap.isEmpty()
                ? Collections.unmodifiableMap(tempPatternsMap) : Collections.<String,List<Pattern>>emptyMap();
        
        // Get the 1099 tab site ID.
        this.tabSiteId = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, Tax1099ParameterNames.TAB_SITE_ID);
        
        // Construct inclusion/exclusion mappings based on DV check stub text and tax box.
        tempCheckStubTextMaps = new HashMap<TaxTableField,Map<String,List<Pattern>>>();
        tempWhitelistFlagsMap = new HashMap<TaxTableField,Boolean>();
        
        for (Map.Entry<TaxTableField,String> paramName : dvCheckStubTextParamNames.entrySet()) {
            tempPatternsMap = new HashMap<String,List<Pattern>>();
            isPatternMappingWhitelist = populatePatternsMapAndDetermineIfWhitelist(tempPatternsMap, parameterService.getParameter(
                    CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL, paramName.getValue()));
            tempCheckStubTextMaps.put(paramName.getKey(), !tempPatternsMap.isEmpty()
                    ? Collections.unmodifiableMap(tempPatternsMap) : Collections.<String,List<Pattern>>emptyMap());
            tempWhitelistFlagsMap.put(paramName.getKey(), Boolean.valueOf(isPatternMappingWhitelist));
        }
        
        this.taxBoxIncludedDvCheckStubTextMaps = Collections.unmodifiableMap(tempCheckStubTextMaps);
        this.taxBoxDvCheckStubTextsAreWhitelists = Collections.unmodifiableMap(tempWhitelistFlagsMap);
    }

    private Map<String, TaxTableField> parseBoxNumberMappings(ParameterService parameterService) {
        Collection<String> boxNumberMappingsFromParameter = parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_1099_PARM_DETAIL,
                Tax1099ParameterNames.TAX_BOX_NUMBER_MAPPINGS);
        return boxNumberMappingsFromParameter.stream()
                .collect(Collectors.toUnmodifiableMap(
                        this::getTaxBoxMappingKeyFromParameterEntry,
                        this::getBoxNumberMetadataFromParameterEntry));
    }

    private String getTaxBoxMappingKeyFromParameterEntry(String parameterEntry) {
        String key = StringUtils.substringBefore(parameterEntry, CUKFSConstants.EQUALS_SIGN);
        String convertedKey = StringUtils.upperCase(key, Locale.US);
        if (!TaxUtils.is1099BoxNumberMappingKeyFormattedProperly(convertedKey)) {
            throw new IllegalStateException("Found a malformed 1099 box mapping key: " + key);
        }
        return convertedKey;
    }

    private TaxTableField getBoxNumberMetadataFromParameterEntry(String parameterEntry) {
        String fieldName = StringUtils.substringAfter(parameterEntry, CUKFSConstants.EQUALS_SIGN);
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalStateException("Found a 1099 box mapping with a missing field name: " + parameterEntry);
        }
        TaxTableField taxField = derivedValues.fields.get(fieldName);
        if (taxField == null) {
            throw new IllegalStateException("Could not find 1099 box metadata for field name: " + fieldName);
        }
        return taxField;
    }

    protected TaxTableField getBoxNumberConstant(String boxMappingKey) {
        return StringUtils.isNotBlank(boxMappingKey)
                ? boxNumberMappings.get(boxMappingKey.toUpperCase(Locale.US)) : null;
    }

}
