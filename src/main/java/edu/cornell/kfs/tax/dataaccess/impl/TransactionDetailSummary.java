package edu.cornell.kfs.tax.dataaccess.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;
import edu.cornell.kfs.tax.batch.TaxDataRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DerivedValuesRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DocumentNoteRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DvSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.PRNCSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.PdpSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.RawTransactionDetailRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorAddressRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorRow;

/**
 * Base helper class containing numerous constants and immutable sets/maps
 * for processing convenience.
 * 
 * <p>This class also has a "scrubbedOutput" flag that indicates whether
 * TransactionRowProcessor implementations should mask any confidential or
 * potentially-sensitive fields in their output files. The flag is currently
 * controlled by the "tax.output.scrubbed" config property.</p>
 * 
 * <p>Since the start date and end date fields are technically mutable,
 * they have been declared private, and copies of them can only be accessed
 * through their getter methods.</p>
 */
abstract class TransactionDetailSummary {
    private static final String PERCENT_CHAR = "%";
    private static final String PERCENT_CHAR_REPLACEMENT = ".*";
    private static final String LINE_START_CHAR = "^";
    private static final String LINE_END_CHAR = "$";

    // PDP tax source row metadata.
    final PdpSourceRow pdpRow;
    // DV tax source row metadata.
    final DvSourceRow dvRow;
    // PRNC tax source row metadata.
    final PRNCSourceRow prncRow;
    // Vendor object metadata.
    final VendorRow vendorRow;
    // Vendor address object metadata.
    final VendorAddressRow vendorAddressRow;
    // Document note object metadata.
    final DocumentNoteRow documentNoteRow;
    // RawTransaction detail metadata.
    final RawTransactionDetailRow rawTransactionDetailRow;
    // Transaction detail metadata.
    final TransactionDetailRow transactionDetailRow;
    // Non-DB-backed field metadata.
    final DerivedValuesRow derivedValues;

    // The tax reporting year.
    final int reportYear;
    // The expected vendorForeignIndicator value for the vendors associated with the retrieved PDP and DV data.
    final boolean vendorForeign;
    // The institution's EIN to use in the output of the transaction detail processing.
    final String taxEIN;
    // The vendor Sole Proprietor Owner code for the given tax processing.
    final String soleProprietorOwnerCode;
    // The vendor type codes to use for excluding transaction rows from the tax processing.
    final Set<String> excludedVendorTypeCodes;
    // The ownership type codes to use for excluding transaction rows from the tax processing.
    final Set<String> excludedOwnershipTypeCodes;

    // Convenience constant for holding the Foreign Draft payment method code.
    final String foreignDraftCode;
    // Convenience constant for holding the Wire Transfer payment method code.
    final String wireTransferCode;
    // Convenience constant for holding a BigDecimal zero value with the proper scale.
    final BigDecimal zeroAmount;
    // Convenience constant for the config property indicating whether confidential data needs to be "scrubbed" in the output.
    final boolean scrubbedOutput;
    
    // The payment date to start at (inclusive). It is expected to be in the same year as the reporting year.
    private final java.sql.Date startDate;
    // The payment date to end at (inclusive). It is expected to be in the same year as the reporting year.
    private final java.sql.Date endDate;



    TransactionDetailSummary(int reportYear, java.sql.Date startDate, java.sql.Date endDate, boolean vendorForeign, String taxType, String parmDetailType,
            Map<String,TaxDataRow> dataRows) {
        ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
        
        // Setup constants pertaining to the given arguments.
        this.reportYear = reportYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.vendorForeign = vendorForeign;
        
        // Setup constants and immutable collections/maps from the associated tax parameters.
        this.soleProprietorOwnerCode = parameterService.getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, parmDetailType, taxType + TaxCommonParameterNames.SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX);
        this.excludedVendorTypeCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, parmDetailType, taxType + TaxCommonParameterNames.EXCLUDE_BY_VENDOR_TYPE_PARAMETER_SUFFIX));
        this.excludedOwnershipTypeCodes = convertValuesToSet(parameterService.getParameterValuesAsString(
                CUTaxConstants.TAX_NAMESPACE, parmDetailType, taxType + TaxCommonParameterNames.EXCLUDE_BY_OWNERSHIP_TYPE_PARAMETER_SUFFIX));
        
        // Setup constants obtained from config params.
        this.taxEIN = ConfigContext.getCurrentContextConfig().getProperty(CUTaxKeyConstants.TAX_OUTPUT_EIN);
        this.scrubbedOutput = ConfigContext.getCurrentContextConfig().getBooleanProperty(CUTaxKeyConstants.TAX_OUTPUT_SCRUBBED, false);
        
        // Setup constants for Foreign Draft and Wire Transfer codes so that we don't need to keep calling the enum values.
        this.foreignDraftCode = KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT;
        this.wireTransferCode = KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE;
        
        // Setup convenience constant for a zero BigDecimal value of proper precision.
        this.zeroAmount = KualiDecimal.ZERO.bigDecimalValue();
        
        // Setup table row metadata.
        TaxTableMetadataService tableMetadataService = SpringContext.getBean(TaxTableMetadataService.class);
        this.pdpRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.PDP.name()), PdpSourceRow.class);
        this.dvRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.DV.name()), DvSourceRow.class);
        this.prncRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.PRNC.name()), PRNCSourceRow.class);
        this.vendorRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.VENDOR.name()), VendorRow.class);
        this.vendorAddressRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.VENDOR_ANY_ADDRESS.name()), VendorAddressRow.class);
        this.documentNoteRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.DOCUMENT_NOTE.name()), DocumentNoteRow.class);
        this.transactionDetailRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.DETAIL.name()), TransactionDetailRow.class);
        this.derivedValues = tableMetadataService.getTransientRowFromData(dataRows.get(TaxFieldSource.DERIVED.name()), DerivedValuesRow.class);
        this.rawTransactionDetailRow = tableMetadataService.getRowFromData(dataRows.get(TaxFieldSource.RAW_DETAIL.name()), RawTransactionDetailRow.class);
    }

    /**
     * Returns the start of the payment date range. Will only return a new copy of the date object.
     * 
     * @return A java.sql.Date instance representing the start of the payment date range.
     */
    java.sql.Date getStartDate() {
        // Return a new instance, to prevent calling code from potentially tampering with the existing start date instance.
        return new java.sql.Date(startDate.getTime());
    }

    /**
     * Returns the end of the payment date range. Will only return a new copy of the date object.
     * 
     * @return A java.sql.Date instance representing the end of the payment date range.
     */
    java.sql.Date getEndDate() {
        // Return a new instance, to prevent calling code from potentially tampering with the existing end date instance.
        return new java.sql.Date(endDate.getTime());
    }



    /*
     * Convenience method that takes a collection of value pair strings
     * in the form { "key1=value1", "key2=value2", ... } and converts
     * it into a Map.
     */
    Map<String,String> convertValuePairsToMap(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String,String> valuePairsMap = new HashMap<String,String>();
        for (String parameterValue : parameterValues) {
            valuePairsMap.put(parameterValue.substring(0, parameterValue.indexOf('=')),
                    parameterValue.substring(parameterValue.indexOf('=') + 1));
        }
        
        return Collections.unmodifiableMap(valuePairsMap);
    }

    /*
     * Convenience method that takes a collection of listings in
     * the form { "key1=val1a,val1b,...", "key2=val2a,val2b,...", ... }
     * and converts it into a Map of values to keys.
     */
    Map<String,String> convertMultiValueListingsToInvertedMap(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String,String> listingsMap = new HashMap<String,String>();
        for (String parameterValue : parameterValues) {
            // Each string should contain a single key, followed by an equals sign and zero or more comma-delimited values.
            int equalsSignIndex = parameterValue.indexOf('=');
            if (equalsSignIndex != -1 && equalsSignIndex < parameterValue.length() - 1) {
                String listingKey = parameterValue.substring(0, equalsSignIndex);
                String[] listingValues = StringUtils.split(parameterValue.substring(equalsSignIndex + 1), ',');
                for (String listingValue : listingValues) {
                    listingsMap.put(listingValue, listingKey);
                }
            }
        }
        
        return Collections.unmodifiableMap(listingsMap);
    }

    /*
     * Convenience method that takes a collection of listings in
     * the form { "key1=val1a,val1b,...", "key2=val2a,val2b,...", ... }
     * and converts it into a Map from keys to value sets.
     */
    Map<String,Set<String>> convertMultiValueListingsToMap(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String,Set<String>> multiListingsMap = new HashMap<String,Set<String>>();
        for (String parameterValue : parameterValues) {
            // Each string should contain a single key, followed by an equals sign and zero or more comma-delimited values.
            int equalsSignIndex = parameterValue.indexOf('=');
            if (equalsSignIndex != -1) {
                if (equalsSignIndex < parameterValue.length() - 1) {
                    Set<String> listingValues = new HashSet<String>(Arrays.asList(
                            StringUtils.split(parameterValue.substring(equalsSignIndex + 1), ',')));
                    multiListingsMap.put(parameterValue.substring(0, equalsSignIndex), Collections.unmodifiableSet(listingValues));
                } else {
                    multiListingsMap.put(parameterValue.substring(0, equalsSignIndex), Collections.<String>emptySet());
                }
            }
        }
        
        return Collections.unmodifiableMap(multiListingsMap);
    }

    /*
     * Convenience method that takes a collection of parameter values
     * and returns it as a Set.
     */
    Set<String> convertValuesToSet(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(new HashSet<String>(parameterValues));
    }

    /*
     * Convenience method that takes a collection of prefixes, uppercases them,
     * and returns them as a list.
     */
    List<String> convertPrefixesToList(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> prefixes = new ArrayList<String>(parameterValues.size());
        for (String parameterValue : parameterValues) {
            prefixes.add(parameterValue.toUpperCase(Locale.US));
        }
        
        return Collections.unmodifiableList(prefixes);
    }

    /*
     * Convenience method that takes a collection of listings in
     * the form { "key1=value1a", "key1=value1b", ... , "key2=value2a", ... }
     * and converts them into a Map of keys to value Sets.
     */
    Map<String,Set<String>> convertValueListingsToMap(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String,Set<String>> valuesMap = new HashMap<String,Set<String>>();
        for (String parameterValue : parameterValues) {
            int equalsSignIndex = parameterValue.indexOf('=');
            if (equalsSignIndex != -1 && equalsSignIndex < parameterValue.length() - 1) {
                // Get the key and the value.
                String key = parameterValue.substring(0, equalsSignIndex);
                String value = parameterValue.substring(equalsSignIndex + 1);
                
                // Create a new Set for the given key if one does not exist.
                Set<String> values = valuesMap.get(key);
                if (values == null) {
                    values = new HashSet<String>();
                    valuesMap.put(key, values);
                }
                
                // Add the value to the Set.
                values.add(value);
            }
        }
        
        // Make the sets immutable before returning the Map.
        for (Map.Entry<String,Set<String>> valuesEntry : valuesMap.entrySet()) {
            valuesEntry.setValue(Collections.unmodifiableSet(valuesEntry.getValue()));
        }
        
        return Collections.unmodifiableMap(valuesMap);
    }

    /*
     * Convenience method that takes a collection of listings in
     * the form { "key1=pattern1a", "key1=pattern1b", ... , "key2=pattern2a", ... }
     * and converts them into a Map of keys to uppercased-Pattern lists.
     * Any "%" and "_" characters in the patterns will be converted to ".*" and ".",
     * respectively, when generating the Pattern objects.
     * Also, "^" and "$" will be added appropriately if the pattern does not start or
     * end with those characters or with "%".
     */
    Map<String,List<Pattern>> convertPatternListingsToMap(Collection<String> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<String,List<Pattern>> patternMap = new HashMap<String,List<Pattern>>();
        for (String parameterValue : parameterValues) {
            int equalsSignIndex = parameterValue.indexOf('=');
            if (equalsSignIndex != -1 && equalsSignIndex < parameterValue.length() - 1) {
                // Get the key and the value, and uppercase the value.
                String key = parameterValue.substring(0, equalsSignIndex);
                String value = parameterValue.substring(equalsSignIndex + 1).toUpperCase(Locale.US);
                
                // Create a new Pattern list for the given key if one does not exist.
                List<Pattern> patterns = patternMap.get(key);
                if (patterns == null) {
                    patterns = new ArrayList<Pattern>();
                    patternMap.put(key, patterns);
                }
                
                // Create the Pattern.
                switch (value.charAt(0)) {
                    case '%' :
                        // Leave as-is if the pattern begins with '%'; such chars will be auto-converted to ".*" below.
                        break;
                        
                    case '^' :
                        // Leave as-is if the pattern begins with '^'.
                        break;
                        
                    default :
                        // Otherwise, add a leading '^'.
                        value = LINE_START_CHAR + value;
                        break;
                }
                switch(value.charAt(value.length() - 1)) {
                    case '$' :
                        // Leave as-is if the pattern ends with '$'.
                        break;
                        
                    case '%' :
                        // Leave as-is if the pattern ends with '%'; such chars will be auto-converted to ".*" below.
                        break;
                        
                    default :
                        // Otherwise, add a trailing '$'.
                        value = value + LINE_END_CHAR;
                        break;
                }
                value = value.replace(PERCENT_CHAR, PERCENT_CHAR_REPLACEMENT).replace('_', '.');
                patterns.add(Pattern.compile(value));
            }
        }
        
        // Make the lists immutable before returning the Map.
        for (Map.Entry<String,List<Pattern>> patternEntry : patternMap.entrySet()) {
            patternEntry.setValue(Collections.unmodifiableList(patternEntry.getValue()));
        }
        
        return Collections.unmodifiableMap(patternMap);
    }



    /*
     * Convenience method for populating the given values map based on the values of the passed-in parameter,
     * and for returning whether the parameter's constraint code is "A" (Allow).
     */
    boolean populateValuesMapAndDetermineIfWhitelist(Map<String,Set<String>> destMap, Parameter parameter) {
        if (parameter != null) {
            String[] values = StringUtils.split(parameter.getValue(), ';');
            if (values != null) {
                destMap.putAll(convertValueListingsToMap(Arrays.asList(values)));
            }
            return EvaluationOperator.ALLOW.equals(parameter.getEvaluationOperator());
        } else {
            return false;
        }
    }

    /*
     * Convenience method for populating the given patterns map based on the values of the passed-in parameter,
     * and for returning whether the parameter's constraint code is "A" (Allow).
     */
    boolean populatePatternsMapAndDetermineIfWhitelist(Map<String,List<Pattern>> destMap, Parameter parameter) {
        if (parameter != null) {
            String[] values = StringUtils.split(parameter.getValue(), ';');
            if (values != null) {
                destMap.putAll(convertPatternListingsToMap(Arrays.asList(values)));
            }
            return EvaluationOperator.ALLOW.equals(parameter.getEvaluationOperator());
        } else {
            return false;
        }
    }
}
