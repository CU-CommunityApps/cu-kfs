package edu.cornell.kfs.tax.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

public final class TaxUtils {

    public static String build1099BoxNumberMappingKey(String formType, String boxNumber) {
        String convertedFormType = StringUtils.defaultIfBlank(formType, CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE);
        String convertedBoxNumber = StringUtils.defaultIfBlank(boxNumber, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
        String boxNumberMappingKey = MessageFormat.format(CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_FORMAT,
                convertedFormType, convertedBoxNumber);
        return StringUtils.upperCase(boxNumberMappingKey, Locale.US);
    }

    public static Pair<String, String> build1099FormTypeAndBoxNumberPair(String boxNumberMappingKey) {
        if (StringUtils.isBlank(boxNumberMappingKey)) {
            return CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY;
        }
        String upperCaseKey = StringUtils.upperCase(boxNumberMappingKey, Locale.US);
        Matcher keyMatcher = CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_PATTERN.matcher(upperCaseKey);
        if (keyMatcher.matches()) {
            String formType = keyMatcher.group(1);
            String boxNumber = keyMatcher.group(2);
            return Pair.of(formType, boxNumber);
        } else {
            throw new IllegalArgumentException("Box number mapping key is malformed: " + boxNumberMappingKey);
        }
    }

    public static boolean is1099BoxNumberMappingKeyFormattedProperly(String boxNumberMappingKey) {
        return StringUtils.isNotBlank(boxNumberMappingKey)
                && CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_PATTERN.matcher(boxNumberMappingKey).matches();
    }

    public static DecimalFormat buildDefaultAmountFormatForFileOutput() {
        final DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_AMOUNT_FORMAT,
                new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_AMOUNT_MAX_INT_DIGITS);
        return newFormat;
    }

    public static DecimalFormat buildDefaultPercentFormatForFileOutput() {
        final DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_PERCENT_FORMAT,
                new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_PERCENT_MAX_INT_DIGITS);
        return newFormat;
    }

    public static SimpleDateFormat buildDefaultDateFormatForFileOutput() {
        return new SimpleDateFormat(CUTaxConstants.DEFAULT_DATE_FORMAT, Locale.US);
    }

    public static Map<String, String> buildTransactionOverridesMap(
            final Collection<TransactionOverride> transactionOverrides) {
        return transactionOverrides.stream()
                .collect(Collectors.toUnmodifiableMap(
                        TaxUtils::buildTransactionOverrideKey, TransactionOverride::getBoxNumber));
    }

    public static String buildTransactionOverrideKey(final TransactionOverride transactionOverride) {
        return buildTransactionOverrideKey(transactionOverride.getUniversityDate(),
                transactionOverride.getDocumentNumber(), transactionOverride.getFinancialDocumentLineNumber());
    }

    public static String buildTransactionOverrideKey(final TransactionDetail transactionDetail) {
        return buildTransactionOverrideKey(transactionDetail.getPaymentDate(), transactionDetail.getDocumentNumber(),
                transactionDetail.getFinancialDocumentLineNumber());
    }

    public static String buildTransactionOverrideKey(final java.sql.Date universityDate, final String documentNumber,
            final Integer financialDocumentLineNumber) {
        return StringUtils.joinWith(CUKFSConstants.SEMICOLON,
                universityDate, documentNumber, financialDocumentLineNumber);
    }

    private TaxUtils() {
        throw new UnsupportedOperationException("do not call");
    }

}
