package edu.cornell.kfs.tax.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.service.TransactionOverrideService;

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

    public static java.util.Date copyDate(final java.util.Date value) {
        return new java.util.Date(value.getTime());
    }

    public static java.sql.Date copyDate(final java.sql.Date value) {
        return new java.sql.Date(value.getTime());
    }

    public static TaxOutputDefinitionV2 parseTaxOutputDefinition(
            final TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType, final String filePath)
                    throws IOException {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(filePath)) {
            final byte[] fileContents = IOUtils.toByteArray(fileStream);
            return taxOutputDefinitionV2FileType.parse(fileContents);
        }
    }

    public static String buildCsvTaxFilePath(final String fileNamePrefix, final String fileDirectory,
            final java.util.Date dateTime) {
        final DateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        return StringUtils.join(fileDirectory, CUKFSConstants.SLASH, fileNamePrefix, dateFormat.format(dateTime),
                FileExtensions.CSV);
    }

    public static Map<String, String> buildTransactionOverridesMap(
            final TransactionOverrideService transactionOverrideService, final TaxBatchConfig config) {
        final Collection<TransactionOverride> transactionOverrides = transactionOverrideService
                .getTransactionOverrides(config.getTaxType(), config.getStartDate(), config.getEndDate());
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
