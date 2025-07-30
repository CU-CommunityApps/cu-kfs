package edu.cornell.kfs.tax.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
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
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
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
            final TaxBatchConfig config) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        final String dateSuffix = config.getProcessingStartDate().format(dateFormatter);
        return StringUtils.join(fileDirectory, CUKFSConstants.SLASH, fileNamePrefix, config.getReportYear(),
                dateSuffix, FileExtensions.CSV);
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

    public static TaxStatistics generateBaseStatisticsFor1042S() {
        return new TaxStatistics(
                TaxStatType.NUM_TRANSACTION_ROWS,
                TaxStatType.NUM_BIO_RECORDS_WRITTEN,
                TaxStatType.NUM_DETAIL_RECORDS_WRITTEN,
                TaxStatType.NUM_NO_VENDOR,
                TaxStatType.NUM_NO_PARENT_VENDOR,
                TaxStatType.NUM_VENDOR_NAMES_PARSED,
                TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED,
                TaxStatType.NUM_NO_VENDOR_ADDRESS_US,
                TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN,
                TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED,
                TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_NEITHER_DETERMINED,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_GROSS_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_GROSS_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_FTW_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_FTW_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_SITW_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_SITW_AMOUNTS,
                TaxStatType.NUM_NO_BOX_DETERMINED_ROWS,
                TaxStatType.NUM_BIO_LINES_WITH_TRUNCATED_US_ADDRESS,
                TaxStatType.NUM_BIO_LINES_WITH_TRUNCATED_FOREIGN_ADDRESS,
                TaxStatType.NUM_BIO_LINES_WITHOUT_US_ADDRESS,
                TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS,
                TaxStatType.NUM_BIO_LINES_WITHOUT_ANY_ADDRESS,
                TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_FTW_AMOUNT,
                TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_SITW_AMOUNT,
                TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED,
                TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED_DV,
                TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED_PDP,
                TaxStatType.NUM_FTW_AMOUNTS_DETERMINED,
                TaxStatType.NUM_FTW_AMOUNTS_DETERMINED_DV,
                TaxStatType.NUM_FTW_AMOUNTS_DETERMINED_PDP,
                TaxStatType.NUM_SITW_AMOUNTS_DETERMINED,
                TaxStatType.NUM_SITW_AMOUNTS_DETERMINED_DV,
                TaxStatType.NUM_SITW_AMOUNTS_DETERMINED_PDP,
                TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED_DV,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED_PDP,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_INCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_INCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_NEITHER_DETERMINED_DV,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_NEITHER_DETERMINED_PDP,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_INCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_INCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_DV,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED_PDP,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_NEITHER_DETERMINED_DV,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_NEITHER_DETERMINED_PDP);
    }

    private TaxUtils() {
        throw new UnsupportedOperationException("do not call");
    }

}
