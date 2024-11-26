package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SprintaxPaymentRowProcessor {
    private static final Logger LOG = LogManager.getLogger(SprintaxPaymentRowProcessor.class);

    // Constants pertaining to constructing the tax ID in NNN-NN-NNNN format.
    private static final int TAXID_SRC_CHUNK1_2_SPLIT = 3;
    private static final int TAXID_SRC_CHUNK2_3_SPLIT = 5;
    private static final int TAXID_SRC_CHUNK3_END = 9;
    private static final int TAXID_DEST_CHUNK2_START = 4;
    private static final int TAXID_DEST_CHUNK3_START = 7;

    private static final int VENDOR_US_ADDRESS_INDEX = 2;
    private static final int VENDOR_FOREIGN_ADDRESS_INDEX = 3;

    // Helper array for storing extra potentially-open result sets created by the tax processing.
    private final ResultSet[] extraResultSets;

    // Helper array for storing extra PreparedStatement instances needed for the tax processing.
    private final PreparedStatement[] extraStatements;

    private SprintaxPaymentRowProcessor.RecordPiece[] outputFieldDefinitions;

    // Helper array for formatting tax IDs.
    private final char[] formattedTaxId;

    // The Format instances for formatting numeric and date output.
    private final DecimalFormat amountFormat;
    private final DecimalFormat percentFormat;
    private final DateFormat dateFormat;
    private Transaction1042SSummary summary;

    // Variables pertaining to values that need to be retrieved from the next detail row before populating the detail "piece" objects.
    private String nextTaxId;
    private String nextPayeeId;

    // Variables pertaining to decrypting and formatting the tax ID.
    private EncryptionService encryptionService;
    private String unencryptedTaxId;

    // Variables pertaining to vendor-related data.
    private TaxTableRow.VendorRow vendorRow;
    private TaxTableRow.VendorAddressRow vendorAddressRow;
    private boolean foundParentVendor;
    private int vendorHeaderId;
    private int vendorDetailId;
    private int currentVendorDetailId;
    private String vendorNameForOutput;
    private String parentVendorNameForOutput;
    private String tempRetrievedValue;

    // Variables pertaining to document note data.
    private TaxTableField docNoteTextField;

    // Variables pertaining to row-specific data.
    private TaxTableField taxBox;
    private TaxTableField grossAmountField;
    private TaxTableField ftwAmountField;
    private TaxTableField sitwAmountField;
    private String rowKey;
    private String chartAndAccountCombo;
    private String incomeClassCodeFromMap;
    private String overrideTaxBox;
    private String overriddenTaxBox;
    private Boolean taxTreatyExemptIncomeInd;
    private Boolean foreignSourceIncomeInd;
    private Boolean royaltiesInclusionInd;
    private Boolean royaltiesObjInclusionInd;
    private Boolean fedTaxWithheldInclusionInd;
    private Boolean stateIncTaxWithheldInclusionInd;
    private boolean isDVRow;

    // Variables pertaining to various flags.
    private boolean foundExclusion;
    private boolean excludeTransaction;
    private boolean writeWsBiographicRecord;
    private boolean isRoyaltyAmount;
    private boolean isParm1042SInclusion;
    private boolean isParm1042SExclusion;

    private EnumMap<TaxStatType, Integer> statistics;



    // Variables pertaining to tax-source-specific statistics.
    private SprintaxPaymentRowProcessor.OriginSpecificStats dvStats;
    private SprintaxPaymentRowProcessor.OriginSpecificStats pdpStats;
    private SprintaxPaymentRowProcessor.OriginSpecificStats currentStats;

    // Variables pertaining to the various query results.
    private ResultSet rsDummy;
    private ResultSet rsVendor;
    private ResultSet rsVendorUSAddress;
    private ResultSet rsVendorForeignAddress;
    private ResultSet rsDocNote;

    // Variables pertaining to the fields retrieved from each transaction detail row.
    private SprintaxPaymentRowProcessor.RecordPiece1042SString[] detailStrings;
    private SprintaxPaymentRowProcessor.RecordPiece1042SInt[] detailInts;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal[] detailBigDecimals;
    private SprintaxPaymentRowProcessor.RecordPiece1042SDate[] detailDates;

    // Variables pertaining to detail fields that always need to be retrieved for processing.
    private SprintaxPaymentRowProcessor.RecordPiece1042SString rowIdP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString taxIdP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString payeeIdP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString incomeCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString incomeCodeSubTypeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString taxTreatyExemptIncomeYesNoP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString foreignSourceIncomeYesNoP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorOwnershipCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString paymentAddressLine1P;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString objectCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString chartCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString accountNumberP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString docTypeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString docNumberP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString paymentReasonCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString dvCheckStubTextP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SInt docLineNumberP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal fedIncomeTaxPctP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal paymentAmountP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SDate paymentDateP;

    // Variables pertaining to fields that are derived from the processing of other fields.
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorLastNameP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorFirstNameP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorEmailAddressP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorUSAddressLine1P;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignAddressLine1P;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignAddressLine2P;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignCityNameP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignZipCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignProvinceNameP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString vendorForeignCountryCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString formattedSSNValueP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString formattedITINValueP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString chapter3StatusCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString chapter3ExemptionCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString chapter4ExemptionCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString incomeCodeForOutputP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString taxEINValueP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SString stateCodeP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SDate endDateP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal chapter3TaxRateP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal grossAmountP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal ftwAmountP;
    private SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal sitwAmountP;

    SprintaxPaymentRowProcessor(Transaction1042SSummary summary) {
        this.summary = summary;
        this.extraResultSets = new ResultSet[4];
        this.extraStatements = new PreparedStatement[4];
        this.formattedTaxId = new char[]{'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'};
        this.amountFormat = buildAmountFormat();
        this.percentFormat = buildPercentFormat();
        this.dateFormat = new SimpleDateFormat(CUTaxConstants.DEFAULT_DATE_FORMAT, Locale.US);

        this.statistics = new EnumMap<>(TaxStatType.class);
        initializeStatistics();
    }

    void initializeStatistics() {
        ArrayList<TaxStatType> stats = new ArrayList<>(Arrays.asList(TaxStatType.NUM_TRANSACTION_ROWS, TaxStatType.NUM_BIO_RECORDS_WRITTEN,
                TaxStatType.NUM_NO_VENDOR, TaxStatType.NUM_NO_PARENT_VENDOR, TaxStatType.NUM_VENDOR_NAMES_PARSED, TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED,
                TaxStatType.NUM_NO_VENDOR_ADDRESS_US, TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN, TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED, TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED,
                TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED, TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED,
                TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED, TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_EXCLUSIONS_DETERMINED, TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_NEITHER_DETERMINED,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_GROSS_AMOUNTS, TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_GROSS_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_FTW_AMOUNTS, TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_FTW_AMOUNTS,
                TaxStatType.NUM_INCOME_CODE_EXCLUDED_SITW_AMOUNTS, TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_SITW_AMOUNTS,
                TaxStatType.NUM_NO_BOX_DETERMINED_ROWS, TaxStatType.NUM_BIO_LINES_WITHOUT_US_ADDRESS, TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS,
                TaxStatType.NUM_BIO_LINES_WITHOUT_ANY_ADDRESS));

        for(TaxStatType stat : stats) {
            statistics.put(stat, 0);
        }
    }


    /**
     * Helper method for taking a tax ID and adding hyphens
     * to put it in the NNN-NN-NNNN format.
     *
     * @param unencryptedTaxId The unencrypted SSN/ITIN to format.
     * @return The formatted tax ID.
     */
    String buildFormattedTaxId(String unencryptedTaxId) {
        unencryptedTaxId.getChars(0, TAXID_SRC_CHUNK1_2_SPLIT, formattedTaxId, 0);
        unencryptedTaxId.getChars(TAXID_SRC_CHUNK1_2_SPLIT, TAXID_SRC_CHUNK2_3_SPLIT, formattedTaxId, TAXID_DEST_CHUNK2_START);
        unencryptedTaxId.getChars(TAXID_SRC_CHUNK2_3_SPLIT, TAXID_SRC_CHUNK3_END, formattedTaxId, TAXID_DEST_CHUNK3_START);

        return new String(formattedTaxId);
    }

    /**
     * Builds and returns an object for formatting numeric values representing amounts.
     * This will be called by the constructor to set the default amount formatter.
     * See the CUTaxConstants class for the pattern and max-int-digit configuration
     * used by the default implementation.
     *
     * @return A new DecimalFormat for formatting amounts.
     */
    DecimalFormat buildAmountFormat() {
        DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_AMOUNT_MAX_INT_DIGITS);
        return newFormat;
    }

    /**
     * Builds and returns an object for formatting numeric values representing percents.
     * This will be called by the constructor to set the default percent formatter.
     * See the CUTaxConstants class for the pattern and max-int-digit configuration
     * used by the default implementation.
     *
     * @return A new DecimalFormat for formatting percents.
     */
    DecimalFormat buildPercentFormat() {
        DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_PERCENT_FORMAT, new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_PERCENT_MAX_INT_DIGITS);
        return newFormat;
    }

    /**
     * Helper method for determining whether an inclusion or exclusion should
     * be performed based on if a value matches any of the given patterns, or
     * whether no inclusions/exclusions should be determined at all.
     * The uppercase version of the value will be used for the checking.
     *
     * @param value       The value to test; may be blank.
     * @param patterns    The patterns to test the value against; may be null.
     * @param isWhitelist Indicates whether the patterns list represents a whitelist (true) or a blacklist (false).
     * @return Null if value is blank or patterns is null, true if a value was found in a whitelist or not found in a blacklist, false otherwise.
     */
    Boolean determineClusionWithPatterns(String value, List<Pattern> patterns, boolean isWhitelist) {
        if (StringUtils.isBlank(value) || patterns == null) {
            // Just return null if no inclusion/exclusion check is needed, due to no value to test or no patterns to test against.
            return null;
        }

        // Check if the value matches at least one of the given patterns.
        boolean foundMatch = false;
        value = value.toUpperCase(Locale.US);
        for (int i = patterns.size() - 1; !foundMatch && i >= 0; i--) {
            if (patterns.get(i).matcher(value).matches()) {
                foundMatch = true;
            }
        }

        // Return a Boolean value based on whether a match was found and whether the patterns represented a whitelist or blacklist.
        return Boolean.valueOf(foundMatch == isWhitelist);
    }


    /**
     * Builds and returns a "piece" configured for handling the specified type of field.
     * Subclasses can store a reference to the created object accordingly if they need to manually
     * handle its behind-the-scenes value.
     *
     * <p>Note that pieces with a type of BLANK or STATIC will be created by the calling code instead.</p>
     *
     * @param fieldSource The type of "piece" being created; cannot be null or equal BLANK or STATIC.
     * @param field       The field to build a piece for; cannot be null.
     * @param name        The field's name; cannot be null.
     * @return A "piece" object configured to handle the value of the given field.
     */
    SprintaxPaymentRowProcessor.RecordPiece getPieceForField(CUTaxBatchConstants.TaxFieldSource fieldSource, TaxTableField field, String name) {
        SprintaxPaymentRowProcessor.RecordPiece piece;

        switch (fieldSource) {
            case BLANK:
                throw new IllegalArgumentException("Cannot create piece for BLANK type");

            case STATIC:
                throw new IllegalArgumentException("Cannot create piece for STATIC type");

            case DETAIL:
                // Create a piece that pre-loads its value from the current transaction detail ResultSet line at runtime.
                piece = getPieceForFieldInternal(field, name);
                break;

            case PDP:
                throw new IllegalArgumentException("Cannot create piece for PDP type");

            case DV:
                throw new IllegalArgumentException("Cannot create piece for DV type");

            case VENDOR:
                // Create a piece that derives its value directly from the vendor ResultSet at runtime, or a static masked piece if GIIN and in scrubbed mode.
                piece = (!summary.scrubbedOutput || field.index != summary.vendorRow.vendorGIIN.index)
                        ? new SprintaxPaymentRowProcessor.RecordPiece1042SResultSetDerivedString(name, field.index, CUTaxConstants.VENDOR_DETAIL_INDEX)
                        : new SprintaxPaymentRowProcessor.StaticStringRecordPiece(name, CUTaxConstants.MASKED_VALUE_19_CHARS);
                break;

            case VENDOR_US_ADDRESS:
                // Create a piece that derives its value directly from the vendor US address ResultSet at runtime.
                piece = new SprintaxPaymentRowProcessor.RecordPiece1042SResultSetDerivedString(name, field.index, VENDOR_US_ADDRESS_INDEX);
                break;

            case VENDOR_ANY_ADDRESS:
                // Non-country-specific pieces are not supported by this implementation.
                throw new IllegalArgumentException("The VENDOR_ANY_ADDRESS type is not supported for 1042S processing");

            case DOCUMENT_NOTE:
                throw new IllegalArgumentException("Cannot create piece for DOCUMENT_NOTE type");

            case DERIVED:
                // Create a piece whose value will be set by the 1042S processing.
                piece = getPieceForFieldInternal(field, name);
                break;

            default:
                throw new IllegalArgumentException("Unrecognized piece type");
        }

        return piece;
    }

    /*
     * Internal helper method for creating DETAIL or DERIVED RecordPiece instances.
     */
    public SprintaxPaymentRowProcessor.RecordPiece getPieceForFieldInternal(TaxTableField field, String name) {
        SprintaxPaymentRowProcessor.RecordPiece piece;

        switch (field.jdbcType) {
            case java.sql.Types.DECIMAL:
                piece = new SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal(name, field.index,
                        !summary.transactionDetailRow.federalIncomeTaxPercent.equals(field) && !summary.derivedValues.chapter3TaxRate.equals(field));
                break;

            case java.sql.Types.INTEGER:
                piece = new SprintaxPaymentRowProcessor.RecordPiece1042SInt(name, field.index);
                break;

            case java.sql.Types.VARCHAR:
                piece = new SprintaxPaymentRowProcessor.RecordPiece1042SString(name, field.index);
                break;

            case java.sql.Types.DATE:
                piece = new SprintaxPaymentRowProcessor.RecordPiece1042SDate(name, field.index);
                break;

            default:
                throw new IllegalStateException("This processor does not support data fields of the given JDBC type: " + Integer.valueOf(field.jdbcType));
        }

        return piece;
    }


    Set<TaxTableField> getMinimumFields(CUTaxBatchConstants.TaxFieldSource fieldSource) {
        Set<TaxTableField> minFields = new HashSet<TaxTableField>();
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        TaxTableRow.DerivedValuesRow derivedValues = summary.derivedValues;

        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot find minimum fields for BLANK type");

            case STATIC :
                throw new IllegalArgumentException("Cannot find minimum fields for STATIC type");

            case DETAIL :
                minFields.addAll(Arrays.asList(
                        detailRow.transactionDetailId,
                        detailRow.documentNumber,
                        detailRow.documentType,
                        detailRow.financialDocumentLineNumber,
                        detailRow.finObjectCode,
                        detailRow.netPaymentAmount,
                        detailRow.vendorTaxNumber,
                        detailRow.incomeCode,
                        detailRow.incomeCodeSubType,
                        detailRow.dvCheckStubText,
                        detailRow.payeeId,
                        detailRow.vendorOwnershipCode,
                        detailRow.paymentDate,
                        detailRow.incomeTaxTreatyExemptIndicator,
                        detailRow.foreignSourceIncomeIndicator,
                        detailRow.federalIncomeTaxPercent,
                        detailRow.paymentLine1Address,
                        detailRow.chartCode,
                        detailRow.accountNumber,
                        detailRow.paymentReasonCode
                ));
                break;

            case VENDOR :
                // Leave Set empty.
                break;

            case VENDOR_US_ADDRESS :
                // Leave Set empty.
                break;

            case VENDOR_ANY_ADDRESS :
                // Leave Set empty.
                break;

            case DERIVED :
                minFields.addAll(Arrays.asList(
                        derivedValues.vendorLastName,
                        derivedValues.vendorFirstName,
                        derivedValues.vendorEmailAddress,
                        derivedValues.vendorUSAddressLine1,
                        derivedValues.vendorForeignAddressLine1,
                        derivedValues.vendorForeignAddressLine2,
                        derivedValues.vendorForeignCityName,
                        derivedValues.vendorForeignZipCode,
                        derivedValues.vendorForeignProvinceName,
                        derivedValues.vendorForeignCountryCode,
                        derivedValues.ssn,
                        derivedValues.itin,
                        derivedValues.chapter3StatusCode,
                        derivedValues.chapter3ExemptionCode,
                        derivedValues.chapter4ExemptionCode,
                        derivedValues.incomeCode,
                        derivedValues.ein,
                        derivedValues.chapter3TaxRate,
                        derivedValues.grossAmount,
                        derivedValues.fedTaxWithheldAmount,
                        derivedValues.stateIncomeTaxWithheldAmount,
                        derivedValues.stateCode,
                        derivedValues.endDate
                ));
                break;

            default :
                throw new IllegalArgumentException("Invalid piece type");
        }

        return minFields;
    }



    void setComplexPieces(Map<String, SprintaxPaymentRowProcessor.RecordPiece> complexPieces) {
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        TaxTableRow.DerivedValuesRow derivedValues = summary.derivedValues;
        List<SprintaxPaymentRowProcessor.RecordPiece1042SString> stringPieces = new ArrayList<SprintaxPaymentRowProcessor.RecordPiece1042SString>();
        List<SprintaxPaymentRowProcessor.RecordPiece1042SInt> intPieces = new ArrayList<SprintaxPaymentRowProcessor.RecordPiece1042SInt>();
        List<SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal> bigDecimalPieces = new ArrayList<SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal>();
        List<SprintaxPaymentRowProcessor.RecordPiece1042SDate> datePieces = new ArrayList<SprintaxPaymentRowProcessor.RecordPiece1042SDate>();

        // retrieve the "piece" objects corresponding to all the given transaction detail fields.
        for (TaxTableField detailField : summary.transactionDetailRow.orderedFields) {
            SprintaxPaymentRowProcessor.RecordPiece detailPiece = complexPieces.get(detailField.propertyName);
            if (detailPiece != null) {
                switch (detailField.jdbcType) {
                    case java.sql.Types.DECIMAL :
                        bigDecimalPieces.add((SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) detailPiece);
                        break;

                    case java.sql.Types.INTEGER :
                        intPieces.add((SprintaxPaymentRowProcessor.RecordPiece1042SInt) detailPiece);
                        break;

                    case java.sql.Types.VARCHAR :
                        stringPieces.add((SprintaxPaymentRowProcessor.RecordPiece1042SString) detailPiece);
                        break;

                    case java.sql.Types.DATE :
                        datePieces.add((SprintaxPaymentRowProcessor.RecordPiece1042SDate) detailPiece);
                        break;

                    default :
                        throw new IllegalStateException("Found unsupported detail field JDBC type: " + Integer.toString(detailField.jdbcType));
                }
            }
        }

        // Setup the transaction detail "piece" arrays.
        detailStrings = stringPieces.toArray(new SprintaxPaymentRowProcessor.RecordPiece1042SString[stringPieces.size()]);
        detailInts = intPieces.toArray(new SprintaxPaymentRowProcessor.RecordPiece1042SInt[intPieces.size()]);
        detailBigDecimals = bigDecimalPieces.toArray(new SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal[bigDecimalPieces.size()]);
        detailDates = datePieces.toArray(new SprintaxPaymentRowProcessor.RecordPiece1042SDate[datePieces.size()]);

        // Retrieve the various detail "pieces" that will be needed for the processing.
        rowIdP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.transactionDetailId.propertyName);
        docNumberP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.documentNumber.propertyName);
        docTypeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.documentType.propertyName);
        docLineNumberP = (SprintaxPaymentRowProcessor.RecordPiece1042SInt) complexPieces.get(detailRow.financialDocumentLineNumber.propertyName);
        objectCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.finObjectCode.propertyName);
        paymentAmountP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(detailRow.netPaymentAmount.propertyName);
        taxIdP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.vendorTaxNumber.propertyName);
        incomeCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.incomeCode.propertyName);
        incomeCodeSubTypeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.incomeCodeSubType.propertyName);
        dvCheckStubTextP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.dvCheckStubText.propertyName);
        payeeIdP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.payeeId.propertyName);
        vendorOwnershipCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.vendorOwnershipCode.propertyName);
        paymentDateP = (SprintaxPaymentRowProcessor.RecordPiece1042SDate) complexPieces.get(detailRow.paymentDate.propertyName);
        taxTreatyExemptIncomeYesNoP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.incomeTaxTreatyExemptIndicator.propertyName);
        foreignSourceIncomeYesNoP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.foreignSourceIncomeIndicator.propertyName);
        fedIncomeTaxPctP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(detailRow.federalIncomeTaxPercent.propertyName);
        paymentAddressLine1P = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.paymentLine1Address.propertyName);
        chartCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.chartCode.propertyName);
        accountNumberP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.accountNumber.propertyName);
        paymentReasonCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(detailRow.paymentReasonCode.propertyName);

        // Retrieve the various derived "pieces" that will be needed for the processing.
        vendorLastNameP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorLastName.propertyName);
        vendorFirstNameP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorFirstName.propertyName);
        vendorEmailAddressP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorEmailAddress.propertyName);
        vendorUSAddressLine1P = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorUSAddressLine1.propertyName);
        vendorForeignAddressLine1P = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignAddressLine1.propertyName);
        vendorForeignAddressLine2P = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignAddressLine2.propertyName);
        vendorForeignCityNameP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignCityName.propertyName);
        vendorForeignZipCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignZipCode.propertyName);
        vendorForeignProvinceNameP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignProvinceName.propertyName);
        vendorForeignCountryCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignCountryCode.propertyName);
        formattedSSNValueP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.ssn.propertyName);
        formattedITINValueP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.itin.propertyName);
        chapter3StatusCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.chapter3StatusCode.propertyName);
        chapter3ExemptionCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.chapter3ExemptionCode.propertyName);
        chapter4ExemptionCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.chapter4ExemptionCode.propertyName);
        incomeCodeForOutputP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.incomeCode.propertyName);
        taxEINValueP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.ein.propertyName);
        chapter3TaxRateP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.chapter3TaxRate.propertyName);
        grossAmountP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.grossAmount.propertyName);
        ftwAmountP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.fedTaxWithheldAmount.propertyName);
        ftwAmountP.negateStringValue = true;
        sitwAmountP = (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.stateIncomeTaxWithheldAmount.propertyName);
        sitwAmountP.negateStringValue = true;
        stateCodeP = (SprintaxPaymentRowProcessor.RecordPiece1042SString) complexPieces.get(derivedValues.stateCode.propertyName);
        endDateP = (SprintaxPaymentRowProcessor.RecordPiece1042SDate) complexPieces.get(derivedValues.endDate.propertyName);
    }

    String[] getSqlForExtraStatements() {
        String[] extraSql = new String[extraStatements.length];
        if (extraSql.length >= CUTaxConstants.DEFAULT_EXTRA_RS_SIZE) {
            extraSql[CUTaxConstants.VENDOR_DETAIL_INDEX] = TaxSqlUtils.getVendorSelectSql(summary.vendorRow);
            extraSql[CUTaxConstants.DOC_NOTES_INDEX] = TaxSqlUtils.getDocNotesSelectSql(summary.documentNoteRow);
        }

        extraSql[VENDOR_US_ADDRESS_INDEX] = TaxSqlUtils.getVendorAddressSelectSql(summary.vendorAddressRow, Boolean.TRUE);
        extraSql[VENDOR_FOREIGN_ADDRESS_INDEX] = TaxSqlUtils.getVendorAddressSelectSql(summary.vendorAddressRow, Boolean.FALSE);

        return extraSql;
    }


    /**
     * Returns the parameter values that should be set on the PreparedStatement that was built
     * from the SQL at the corresponding index of the array returned by getSqlForExtraStatements().
     * See the getParameterValuesForSelect() method for information on the returned array's
     * expected format. Optionally, this method may return null to indicate that no defaults should be set.
     *
     * <p>The default implementation returns null, to indicate that no defaults should be set.</p>
     *
     * @param statementIndex The index of the PreparedStatement corresponding to the matching getSqlForExtraStatements() return value.
     * @return A two-dimensional Object array defining the parameters to set for the matching extra query, or null to skip default values setup.
     */
    Object[][] getDefaultParameterValuesForExtraStatement(int statementIndex) {
        if (VENDOR_US_ADDRESS_INDEX == statementIndex || VENDOR_FOREIGN_ADDRESS_INDEX == statementIndex) {
            // For vendor address statements, set defaults so that only the first two parameters need to be updated by the processing.
            return new Object[][] {
                    {Integer.valueOf(0)},
                    {Integer.valueOf(0)},
                    {KFSConstants.COUNTRY_CODE_UNITED_STATES}
            };
        } else {
            // Don't set up defaults for all other parameters.
            return null;
        }
    }

    /**
     * Returns the SQL that should be used for retrieving the transaction detail rows to process.
     *
     * @return A String representing the transaction detail retrieval SQL.
     */
    String getSqlForSelect() {
        return TaxSqlUtils.getTransactionDetailSelectSql(summary.transactionDetailRow.form1042SBox, summary.transactionDetailRow, true, true);
    }

    /**
     * Returns the parameter values that should be set on the PreparedStatement that was built
     * from the SQL returned by getSqlForSelect().
     *
     * <p>The result should be a two-dimensional array configured as follows:</p>
     *
     * <p>The first dimension defines each individual parameter. Index 0 refers to
     * query parameter 1, index 1 refers to query parameter 2, and so on.</p>
     *
     * <p>The second dimension provides data on the parameter values. Index 0 contains
     * the value to set. Index 1, if defined, contains an Integer denoting the
     * JDBC type of the value. Index 2, if defined, contains an Integer denoting the
     * scale/length of the numeric or stream/reader value (default of zero).</p>
     *
     * <p>For a given parameter, the second dimension is permitted to have a length
     * of 1 if the value is a String, java.sql.Date, or Integer. For all other
     * parameter types, the second dimension should have a length of 2 or higher.</p>
     *
     * @return A two-dimensional Object array defining the parameters to set for the transaction detail SELECT query.
     */
    Object[][] getParameterValuesForSelect() {
        return new Object[][] {
                {Integer.valueOf(summary.reportYear)},
                {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }

    /**
     * Performs the tax processing.
     *
     * @param rs The ResultSet containing the transaction detail search results; it can only move forwards, but it is updatable.
     * @throws SQLException if a database access error occurs while processing.
     * @throws IOException  if an I/O error occurs while processing.
     */
    void processTaxRows(Writer writer, ResultSet rs) throws SQLException, IOException {

        boolean keepLooping = true;
        boolean taxIdChanged = false;
        TaxTableRow.TransactionDetailRow detailRow;

        encryptionService = CoreApiServiceLocator.getEncryptionService();
        dvStats = new SprintaxPaymentRowProcessor.OriginSpecificStats();
        pdpStats = new SprintaxPaymentRowProcessor.OriginSpecificStats();

        // Set default values as needed.
        grossAmountP.value = summary.zeroAmount;
        ftwAmountP.value = summary.zeroAmount;
        sitwAmountP.value = summary.zeroAmount;

        // Setup values that are not expected to change between each iteration.
        taxEINValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_9_CHARS : summary.taxEIN;
        stateCodeP.value = summary.stateCode;
        endDateP.value = summary.getEndDate();
        rsDummy = new DummyResultSet();
        detailRow = summary.transactionDetailRow;
        vendorRow = summary.vendorRow;
        vendorAddressRow = summary.vendorAddressRow;
        docNoteTextField = summary.documentNoteRow.noteText;
        grossAmountField = summary.derivedValues.grossAmount;
        ftwAmountField = summary.derivedValues.fedTaxWithheldAmount;
        sitwAmountField = summary.derivedValues.stateIncomeTaxWithheldAmount;


        // Perform initial processing for first row, if there is one.
        if (rs.next()) {
            // If at least one row exists, then update counters and retrieve field values as needed.
            incrementStatistic(TaxStatType.NUM_TRANSACTION_ROWS);
            nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
            nextPayeeId = rs.getString(detailRow.payeeId.index);
            vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
            vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
            taxIdChanged = true;
            if (org.apache.commons.lang.StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for initial row with payee " + nextPayeeId);
            }
        } else {
            // Skip processing if no detail rows were found.
            keepLooping = false;
            LOG.info("No transaction rows found for 1042S tax reporting, skipping processing...");
        }


        // Iterate over the transaction detail rows.
        while (keepLooping) {
            // Initialize transaction detail variables from the current detail line.
            loadTransactionRowValuesFromResults(rs);

            // Setup defaults as needed.
            if (paymentAmountP.value == null) {
                paymentAmountP.value = summary.zeroAmount;
            }

            // Derive helper "key" for logging purposes.
            rowKey = new StringBuilder(100)
                    .append(rowIdP.value)
                    .append(' ').append(docNumberP.value)
                    .append(' ').append(docLineNumberP.value)
                    .append(' ').append(payeeIdP.value)
                    .append(' ').append(docTypeP.value)
                    .append(' ').append(objectCodeP.value)
                    .append(' ').append(incomeCodeP.value)
                    .append(' ').append(incomeCodeSubTypeP.value)
                    .append(' ').append(paymentAmountP.value)
                    .append(' ').append(paymentDateP.value)
                    .toString();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing transaction row with key: " + rowKey);
            }

            // Derive Boolean values.
            taxTreatyExemptIncomeInd = org.apache.commons.lang.StringUtils.isNotBlank(taxTreatyExemptIncomeYesNoP.value)
                    ? Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(taxTreatyExemptIncomeYesNoP.value)) : null;
            foreignSourceIncomeInd = org.apache.commons.lang.StringUtils.isNotBlank(foreignSourceIncomeYesNoP.value)
                    ? Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(foreignSourceIncomeYesNoP.value)) : null;

            // Derive or reset remaining values.
            chartAndAccountCombo = new StringBuilder().append(chartCodeP.value).append('-').append(accountNumberP.value).toString();
            isDVRow = DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(docTypeP.value);
            currentStats = isDVRow ? dvStats : pdpStats;
            excludeTransaction = false;
            foundExclusion = false;
            royaltiesObjInclusionInd = null;
            royaltiesInclusionInd = null;
            fedTaxWithheldInclusionInd = null;
            stateIncTaxWithheldInclusionInd = null;
            taxBox = null;

            // Load new vendor if necessary.
            if (taxIdChanged) {
                loadNewVendor();
            }

            // If necessary, check for exclusions and identify the type of amount.
            if (!summary.zeroAmount.equals(paymentAmountP.value)) {
                checkForExclusionsAndAmounts();
            }

            // Perform final inclusion/exclusion processing, and update amounts accordingly.
            processExclusionsAndAmounts(rs, detailRow);

            // Update current row's vendor-related data.
            rs.updateString(detailRow.vendorName.index, vendorNameForOutput);
            rs.updateString(detailRow.parentVendorName.index, parentVendorNameForOutput);
            rs.updateString(detailRow.vendorEmailAddress.index, vendorEmailAddressP.value);
            rs.updateString(detailRow.vendorChapter4StatusCode.index, rsVendor.getString(vendorRow.vendorChapter4StatusCode.index));
            rs.updateString(detailRow.vendorGIIN.index, rsVendor.getString(vendorRow.vendorGIIN.index));
            rs.updateString(detailRow.vendorLine1Address.index, vendorUSAddressLine1P.value);
            rs.updateString(detailRow.vendorLine2Address.index, rsVendorUSAddress.getString(vendorAddressRow.vendorLine2Address.index));
            rs.updateString(detailRow.vendorCityName.index, rsVendorUSAddress.getString(vendorAddressRow.vendorCityName.index));
            rs.updateString(detailRow.vendorStateCode.index, rsVendorUSAddress.getString(vendorAddressRow.vendorStateCode.index));
            rs.updateString(detailRow.vendorZipCode.index, rsVendorUSAddress.getString(vendorAddressRow.vendorZipCode.index));
            rs.updateString(detailRow.vendorForeignLine1Address.index, vendorForeignAddressLine1P.value);
            rs.updateString(detailRow.vendorForeignLine2Address.index, vendorForeignAddressLine2P.value);
            rs.updateString(detailRow.vendorForeignCityName.index, vendorForeignCityNameP.value);
            rs.updateString(detailRow.vendorForeignZipCode.index, vendorForeignZipCodeP.value);
            rs.updateString(detailRow.vendorForeignProvinceName.index, vendorForeignProvinceNameP.value);
            rs.updateString(detailRow.vendorForeignCountryCode.index, vendorForeignCountryCodeP.value);

            // Store any changes made to the current transaction detail row.
            rs.updateRow();

            if(writeWsBiographicRecord) {
                writeBioLineToFile(writer);
            }


            // Move to next row (if any) and update the looping flag as needed.
            if (rs.next()) {
                // If more rows are available, then update counters and retrieve field values as needed.
                incrementStatistic(TaxStatType.NUM_TRANSACTION_ROWS);
                nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
                nextPayeeId = rs.getString(detailRow.payeeId.index);
                vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
                vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
                // Check for changes to the tax ID between rows. The prior tax ID should be non-null at this point.
                taxIdChanged = org.apache.commons.lang.StringUtils.isBlank(nextTaxId) || !taxIdP.value.equals(nextTaxId);
            } else {
                // If no more rows, then prepare to exit the loop and process any leftover data from the previous iterations.
                keepLooping = false;
            }

            // Automatically abort with an error if the current row has no tax ID.
            if (org.apache.commons.lang.StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for row with payee " + nextPayeeId);
            }

            // END OF LOOP
        }

        LOG.info("Finished transaction row processing for 1042S tax reporting.");
    }

    /*
     * Helper method for determining final inclusion/exclusion state, and for updating box amounts and box type indicators as needed.
     */
    private void processExclusionsAndAmounts(ResultSet rs, TaxTableRow.TransactionDetailRow detailRow) throws SQLException {
        // Determine explicit inclusion state based on other inclusions.
        isParm1042SInclusion = Boolean.TRUE.equals(royaltiesObjInclusionInd)
                || Boolean.TRUE.equals(royaltiesInclusionInd)
                || Boolean.TRUE.equals(fedTaxWithheldInclusionInd)
                || Boolean.TRUE.equals(stateIncTaxWithheldInclusionInd);

        // If no explicit inclusions, determine final exclusion state based on other exclusions.
        isParm1042SExclusion = !isParm1042SInclusion && (
                foundExclusion
                        || Boolean.FALSE.equals(royaltiesObjInclusionInd)
                        || Boolean.FALSE.equals(royaltiesInclusionInd)
                        || Boolean.FALSE.equals(fedTaxWithheldInclusionInd)
                        || Boolean.FALSE.equals(stateIncTaxWithheldInclusionInd)
        );



        // Update final exclusion state based on income code and sub-type if necessary.
        if (!isParm1042SExclusion) {
            if (summary.excludedIncomeCode.equals(incomeCodeP.value)) {
                // If needed, exclude based on income code.
                if (taxBox == grossAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_EXCLUDED_GROSS_AMOUNTS);
                } else if (taxBox == ftwAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_EXCLUDED_FTW_AMOUNTS);
                } else if (taxBox == sitwAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_EXCLUDED_SITW_AMOUNTS);
                }
            } else if (summary.excludedIncomeCodeSubType.equals(incomeCodeSubTypeP.value)) {
                // If needed, exclude based on income code sub-type.
                if (taxBox == grossAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_GROSS_AMOUNTS);
                } else if (taxBox == ftwAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_FTW_AMOUNTS);
                } else if (taxBox == sitwAmountField) {
                    isParm1042SExclusion = true;
                    incrementStatistic(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_SITW_AMOUNTS);
                }
            }
        }



        // Check for tax box overrides.
        overrideTaxBox = summary.transactionOverrides.get(new StringBuilder(30)
                .append(paymentDateP.value).append(';')
                .append(docNumberP.value).append(';')
                .append(docLineNumberP.value).toString());
        if (org.apache.commons.lang.StringUtils.isNotBlank(overrideTaxBox)) {
            // Found override, so setup override and overridden values accordingly.
            if (taxBox == grossAmountField) {
                overriddenTaxBox = CUTaxConstants.FORM_1042S_GROSS_BOX;
            } else if (taxBox == ftwAmountField) {
                overriddenTaxBox = CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX;
            } else if (taxBox == sitwAmountField) {
                overriddenTaxBox = CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX;
            } else {
                overriddenTaxBox = CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY;
            }

            if (CUTaxConstants.FORM_1042S_GROSS_BOX.equals(overrideTaxBox)) {
                taxBox = grossAmountField;
            } else if (CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX.equals(overrideTaxBox)) {
                taxBox = ftwAmountField;
            } else if (CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX.equals(overrideTaxBox)) {
                taxBox = sitwAmountField;
            } else {
                taxBox = summary.derivedValues.boxUnknown1042s;
            }
        } else {
            overriddenTaxBox = null;
        }


        // If explicit inclusion, log stats accordingly.
        if (isParm1042SInclusion) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found explicit inclusions for row with key: " + rowKey);
            }
        }

        // Perform logging and updates dependingImp on amount type and exclusions.
        if ((isParm1042SExclusion && org.apache.commons.lang.StringUtils.isBlank(overrideTaxBox)) || CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY.equals(overrideTaxBox)) {
            // If exclusion and no overrides (or an override to a non-reportable box type), then do not update amounts.
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
            if (taxBox != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found exclusions for row with key: " + rowKey);
                }
            } else {
                incrementStatistic(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS);
            }

        } else if (taxBox == grossAmountField) {
            // If detail row has gross amount, then update flags, amounts, and the current row as needed.
            grossAmountP.value = grossAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_GROSS_BOX);

        } else if (taxBox == ftwAmountField) {
            // If detail row has FTW amount, then update flags, amounts, and the current row as needed.
            ftwAmountP.value = ftwAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX);

        } else if (taxBox == sitwAmountField) {
            // If detail row has SITW amount, then update flags, amounts, and the current row as needed.
            sitwAmountP.value = sitwAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX);

        } else {
            // If no exclusions but box is still undetermined, then do not update amounts.
            incrementStatistic(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
        }

        // If tax box was overridden, then set previous value on transaction row.
        if (org.apache.commons.lang.StringUtils.isNotBlank(overriddenTaxBox)) {
            rs.updateString(detailRow.form1042SOverriddenBox.index, overriddenTaxBox);
        }
    }

    /*
     * Helper method for loading vendor info for the associated transaction detail row, and for resetting variables as needed.
     */
    private void loadNewVendor() throws SQLException {
        // Reset flags and other variables as needed.
        grossAmountP.value = summary.zeroAmount;
        ftwAmountP.value = summary.zeroAmount;
        sitwAmountP.value = summary.zeroAmount;
        vendorLastNameP.value = null;
        vendorFirstNameP.value = null;
        foundParentVendor = false;

        // Assume that we'll need to write a new Biographic record to the file.
        writeWsBiographicRecord = true;



        // Load vendor detail plus relevant header info.
        rsVendor = configureAndRunQuery(CUTaxConstants.VENDOR_DETAIL_INDEX, CUTaxConstants.VENDOR_DETAIL_INDEX,
                vendorHeaderId, vendorDetailId);

        if (rsVendor.next() && vendorDetailId == rsVendor.getInt(vendorRow.vendorDetailAssignedIdentifier.index)) {
            // Matching vendor detail was found, so set parent flag to true unless parent cannot be found.
            foundParentVendor = true;

            // Do extra processing if not the parent vendor detail.
            if (!KRADConstants.YES_INDICATOR_VALUE.equals(rsVendor.getString(vendorRow.vendorParentIndicator.index))) {
                // Update parent name variable accordingly if the vendor owner is a sole proprietor owner.
                if (org.apache.commons.lang.StringUtils.equals(summary.soleProprietorOwnerCode, rsVendor.getString(vendorRow.vendorOwnershipCode.index))) {
                    parentVendorNameForOutput = rsVendor.getString(vendorRow.vendorName.index);
                } else {
                    parentVendorNameForOutput = null;
                }

                // Check for parent, which should be the next row in the result set (if there is one).
                if (!rsVendor.next() || !KRADConstants.YES_INDICATOR_VALUE.equals(rsVendor.getString(vendorRow.vendorParentIndicator.index))) {
                    foundParentVendor = false;
                }
            } else {
                parentVendorNameForOutput = null;
            }

            // Handle the results of the vendor parent search accordingly.
            if (foundParentVendor) {
                currentVendorDetailId = rsVendor.getInt(vendorRow.vendorDetailAssignedIdentifier.index);
                vendorNameForOutput = rsVendor.getString(vendorRow.vendorName.index);
                if (org.apache.commons.lang.StringUtils.isNotBlank(vendorNameForOutput)) {
                    // Parse vendor last name and first name.
                    if (KRADConstants.YES_INDICATOR_VALUE.equals(rsVendor.getString(vendorRow.vendorFirstLastNameIndicator.index))
                            && vendorNameForOutput.indexOf(',') != -1) {
                        vendorLastNameP.value = vendorNameForOutput.substring(0, vendorNameForOutput.indexOf(',')).trim();
                        vendorFirstNameP.value = vendorNameForOutput.substring(vendorNameForOutput.indexOf(',') + 1).trim();
                    } else {
                        vendorLastNameP.value = vendorNameForOutput.trim();
                    }
                    incrementStatistic(TaxStatType.NUM_VENDOR_NAMES_PARSED);
                } else {
                    incrementStatistic(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED);
                }
            } else {
                currentVendorDetailId = vendorDetailId;
                vendorNameForOutput = new StringBuilder(30)
                        .append("No Parent Vendor (").append(vendorHeaderId).append(')').append('!').toString();

                incrementStatistic(TaxStatType.NUM_NO_PARENT_VENDOR);
                /*
                 * Update the ResultSet to the "dummy" one. The superclass will still
                 * handle the reference to the "real" ResultSet, so we don't need to
                 * explicitly close it here.
                 */
                rsVendor = rsDummy;
            }

        } else {
            // No matching vendor detail was found.
            currentVendorDetailId = vendorDetailId;
            vendorNameForOutput = new StringBuilder(30)
                    .append("No Vendor (").append(vendorHeaderId).append('-').append(vendorDetailId).append(')').append('!').toString();
            parentVendorNameForOutput = null;

            incrementStatistic(TaxStatType.NUM_NO_VENDOR);

            /*
             * Update the ResultSet to the "dummy" one. The superclass will still
             * handle the reference to the "real" ResultSet, so we don't need to
             * explicitly close it here.
             */
            rsVendor = rsDummy;
        }



        // Load vendor US address.
        rsVendorUSAddress = configureAndRunQuery(VENDOR_US_ADDRESS_INDEX, VENDOR_US_ADDRESS_INDEX,
                vendorHeaderId, currentVendorDetailId);

        if (rsVendorUSAddress.next()) {
            vendorUSAddressLine1P.value = rsVendorUSAddress.getString(vendorAddressRow.vendorLine1Address.index);
        } else {
            vendorUSAddressLine1P.value = CUTaxConstants.NO_US_VENDOR_ADDRESS;
            incrementStatistic(TaxStatType.NUM_NO_VENDOR_ADDRESS_US);
            /*
             * Update the ResultSet to the "dummy" one. The superclass will still
             * handle the reference to the "real" ResultSet, so we don't need to
             * explicitly close it here.
             */
            rsVendorUSAddress = rsDummy;
        }



        // Load vendor foreign address.
        rsVendorForeignAddress = configureAndRunQuery(VENDOR_FOREIGN_ADDRESS_INDEX, VENDOR_FOREIGN_ADDRESS_INDEX,
                vendorHeaderId, currentVendorDetailId);

        if (rsVendorForeignAddress.next()) {
            vendorForeignAddressLine1P.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorLine1Address.index);
        } else {
            vendorForeignAddressLine1P.value = CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS;
            incrementStatistic(TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN);

            /*
             * Update the ResultSet to the "dummy" one. The superclass will still
             * handle the reference to the "real" ResultSet, so we don't need to
             * explicitly close it here.
             */
            rsVendorForeignAddress = rsDummy;
        }
        vendorForeignAddressLine2P.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorLine2Address.index);
        vendorForeignCityNameP.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorCityName.index);
        vendorForeignZipCodeP.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorZipCode.index);
        vendorForeignProvinceNameP.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorAddressInternationalProvinceName.index);
        vendorForeignCountryCodeP.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorCountryCode.index);



        // Set the Chapter 4 Exemption Code based on the Chapter 4 Status Code. Set to an appropriate default if a Ch4 status mapping does not exist.
        chapter4ExemptionCodeP.value = summary.chapter4StatusToChapter4ExemptionMap.get(rsVendor.getString(vendorRow.vendorChapter4StatusCode.index));
        if (org.apache.commons.lang.StringUtils.isBlank(chapter4ExemptionCodeP.value)) {
            chapter4ExemptionCodeP.value = summary.chapter4DefaultExemptionCode;
        }

        // Configure which email address to use in the output file. Foreign email address takes precedence.
        if (rsVendorForeignAddress instanceof DummyResultSet) {
            vendorEmailAddressP.value = rsVendorUSAddress.getString(vendorAddressRow.vendorAddressEmailAddress.index);
        } else {
            vendorEmailAddressP.value = rsVendorForeignAddress.getString(vendorAddressRow.vendorAddressEmailAddress.index);
        }

        // Decrypt tax ID for usage later.
        try {
            unencryptedTaxId = encryptionService.decrypt(taxIdP.value);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }



    /*
     * Helper method for determining whether any explicit inclusions/exclusions apply to the current transaction detail row, and for determining box type.
     */
    private void checkForExclusionsAndAmounts() throws SQLException {
        int idx;
        boolean foundMatch;
        String tempValue;
        Set<String> distinctValues;

        incomeClassCodeFromMap = summary.objectCodeToIncomeClassCodeMap.get(objectCodeP.value);

        /*
         * Determine if object code is one of the types that allows for this tax processing.
         * If so, then indicate whether the amount represents a gross, FTW, or SITW one.
         */
        if (org.apache.commons.lang.StringUtils.isNotBlank(incomeClassCodeFromMap)) {
            // Found a matching income class.
            taxBox = grossAmountField;
            currentStats.numGrossAmountsDetermined++;
        } else if (summary.federalTaxWithheldObjectCodes.contains(objectCodeP.value)) {
            // No matching income class, but object code is a fed-tax-withheld one.
            taxBox = ftwAmountField;
            currentStats.numFtwAmountsDetermined++;
        } else if (summary.stateTaxWithheldObjectCodes.contains(objectCodeP.value)) {
            // No matching income class or FTW code, but object code is a state-tax-withheld one.
            taxBox = sitwAmountField;
            currentStats.numSitwAmountsDetermined++;
        }



        /*
         * If the row is eligible for processing, then check for exclusions.
         */

        // Check if the vendor type or vendor ownership type is one that should be excluded from the processing.
        if (taxBox != null && !excludeTransaction) {
            if (summary.excludedVendorTypeCodes.contains(rsVendor.getString(vendorRow.vendorTypeCode.index))) {
                foundExclusion = true;
                incrementStatistic(TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED);
            }

            if (summary.excludedOwnershipTypeCodes.contains(rsVendor.getString(vendorRow.vendorOwnershipCode.index))) {
                foundExclusion = true;
                incrementStatistic(TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED);
            }
        }

        // Check if the doc type is a particular PDP one that should be excluded from the processing.
        if (taxBox != null && !excludeTransaction) {
            if (summary.pdpExcludedDocTypes.contains(docTypeP.value)) {
                foundExclusion = true;
                incrementStatistic(TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED);
            }
        }

        // Check if the payment reason code is one that should be excluded from the processing.
        if (!excludeTransaction && org.apache.commons.lang.StringUtils.isNotBlank(paymentReasonCodeP.value)) {
            if (summary.excludedPaymentReasonCodes.contains(paymentReasonCodeP.value)) {
                excludeTransaction = true;
                foundExclusion = true;
                incrementStatistic(TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED);
            }
        }

        // Check if the row should be excluded from the processing based on the start of the payment line 1 address.
        if (taxBox != null && !excludeTransaction) {
            if (org.apache.commons.lang.StringUtils.isNotBlank(paymentAddressLine1P.value)) {
                tempValue = paymentAddressLine1P.value.toUpperCase(Locale.US);
                foundMatch = false;

                for (idx = summary.otherIncomeExcludedPaymentLine1AddressPrefixes.size() - 1; !foundMatch && idx >= 0; idx--) {
                    if (tempValue.startsWith(summary.otherIncomeExcludedPaymentLine1AddressPrefixes.get(idx))) {
                        foundMatch = true;
                    }
                }

                if (foundMatch) {
                    foundExclusion = true;
                    currentStats.numPaymentAddressExclusionsDetermined++;
                }
            }
        }

        // Check if the row should be excluded from the processing based on the associated document's notes.
        if (taxBox != null && !excludeTransaction) {
            rsDocNote = configureAndRunQuery(CUTaxConstants.DOC_NOTES_INDEX, CUTaxConstants.DOC_NOTES_INDEX, docNumberP.value);

            if (rsDocNote.next()) {
                incrementStatistic(TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED);
                foundMatch = false;

                do {
                    tempValue = rsDocNote.getString(docNoteTextField.index);
                    if (org.apache.commons.lang.StringUtils.isNotBlank(tempValue)) {
                        tempValue = tempValue.toUpperCase(Locale.US);

                        for (idx = summary.excludedDocumentNoteTextPrefixes.size() - 1; !foundMatch && idx >= 0; idx--) {
                            if (tempValue.startsWith(summary.excludedDocumentNoteTextPrefixes.get(idx))) {
                                foundMatch = true;
                            }
                        }
                    }
                } while (!foundMatch && rsDocNote.next());

                if (foundMatch) {
                    foundExclusion = true;
                    currentStats.numDocNotesExclusionsDetermined++;
                }
            } else {
                incrementStatistic(TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED);
            }
        }

        // Check for royalty limitations based on chart-and-account combos.
        if (taxBox != null && !excludeTransaction) {
            distinctValues = summary.royaltiesIncludedObjectCodeChartAccount.get(objectCodeP.value);
            if (distinctValues == null) {
                // No royalties-chart-and-account restrictions exist for the given object code.
                royaltiesObjInclusionInd = null;
                currentStats.numRoyaltyObjChartAccountNeitherDetermined++;
            } else if (summary.royaltiesObjCodeChartAccountIsWhitelist == distinctValues.contains(chartAndAccountCombo)) {
                // Found value in whitelist, or did not find value in blacklist.
                royaltiesObjInclusionInd = Boolean.TRUE;
                currentStats.numRoyaltyObjChartAccountInclusionsDetermined++;
            } else {
                // Found value in blacklist, or did not find value in whitelist.
                royaltiesObjInclusionInd = Boolean.FALSE;
                currentStats.numRoyaltyObjChartAccountExclusionsDetermined++;
            }
        }

        // Check for royalties, fed tax withheld, and state tax withheld.
        if (taxBox != null && !excludeTransaction) {
            isRoyaltyAmount = taxBox != ftwAmountField && taxBox != sitwAmountField
                    && org.apache.commons.lang.StringUtils.isNotBlank(incomeClassCodeFromMap) && summary.incomeClassCodesDenotingRoyalties.contains(incomeClassCodeFromMap);

            if (isRoyaltyAmount && isDVRow) {
                // Check for royalty limitations based on DV check stub text.
                royaltiesInclusionInd = determineClusionWithPatterns(dvCheckStubTextP.value,
                        summary.royaltiesIncludedObjectCodeAndDvCheckStubTextMap.get(objectCodeP.value), summary.royaltiesObjCodeDvChkStubTextIsWhitelist);
                if (royaltiesInclusionInd == null) {
                    // No DV-check-stub restrictions exist for the given object code.
                    incrementStatistic(TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_NEITHER_DETERMINED);
                } else if (royaltiesInclusionInd.booleanValue()) {
                    // Found value in whitelist, or did not find value in blacklist.
                    incrementStatistic(TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED);
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    incrementStatistic(TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_EXCLUSIONS_DETERMINED);
                }

            }

            if (taxBox == ftwAmountField) {
                // Check for fed-tax-withheld limitations based on chart-and-account combos.
                distinctValues = summary.fedTaxWithheldObjectCodeChartAccount.get(objectCodeP.value);
                if (distinctValues == null) {
                    // No FTW-chart-and-account restrictions exist for the given object code.
                    fedTaxWithheldInclusionInd = null;
                    currentStats.numFtwObjChartAccountNeitherDetermined++;
                } else if (summary.ftwObjCodeChartAccountIsWhitelist == distinctValues.contains(chartAndAccountCombo)) {
                    // Found value in whitelist, or did not find value in blacklist.
                    fedTaxWithheldInclusionInd = Boolean.TRUE;
                    currentStats.numFtwObjChartAccountInclusionsDetermined++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    fedTaxWithheldInclusionInd = Boolean.FALSE;
                    currentStats.numFtwObjChartAccountExclusionsDetermined++;
                }
            }

            if (taxBox == sitwAmountField) {
                // Check for state-income-tax-withheld limitations based on chart-and-account combos.
                distinctValues = summary.stateIncTaxWithheldObjectCodeChartAccount.get(objectCodeP.value);
                if (distinctValues == null) {
                    // No SITW-chart-and-account restrictions exist for the given object code.
                    stateIncTaxWithheldInclusionInd = null;
                    currentStats.numSitwObjChartAccountNeitherDetermined++;
                } else if (summary.sitwObjCodeChartAccountIsWhitelist == distinctValues.contains(chartAndAccountCombo)) {
                    // Found value in whitelist, or did not find value in blacklist.
                    stateIncTaxWithheldInclusionInd = Boolean.TRUE;
                    currentStats.numSitwObjChartAccountInclusionsDetermined++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    stateIncTaxWithheldInclusionInd = Boolean.FALSE;
                    currentStats.numSitwObjChartAccountExclusionsDetermined++;
                }
            }
        }

    }

    /*
     * Helper method for loading the relevant transaction detail fields into the associated "piece" objects.
     */
    private void loadTransactionRowValuesFromResults(ResultSet rs) throws SQLException {
        // Get String values.
        for (SprintaxPaymentRowProcessor.RecordPiece1042SString detailString : detailStrings) {
            detailString.value = rs.getString(detailString.columnIndex);
        }
        // Get int values.
        for (SprintaxPaymentRowProcessor.RecordPiece1042SInt detailInt : detailInts) {
            detailInt.value = rs.getInt(detailInt.columnIndex);
        }
        // Get BigDecimal values.
        for (SprintaxPaymentRowProcessor.RecordPiece1042SBigDecimal detailBigDecimal : detailBigDecimals) {
            detailBigDecimal.value = rs.getBigDecimal(detailBigDecimal.columnIndex);
        }
        // Get java.sql.Date values.
        for (SprintaxPaymentRowProcessor.RecordPiece1042SDate detailDate : detailDates) {
            detailDate.value = rs.getDate(detailDate.columnIndex);
        }
    }

    /*
     * Helper method for writing 1042S biographic and detail records to their respective files.
     */
    private void writeBioLineToFile(Writer writer) throws SQLException, IOException {
        /*
         * SSN-vs-ITIN logic per Loree Kanellis:
         * If the taxID starts with a '9' and the fourth digit is a '7' or '8'
         * then format as NNN-NN-NNNN for data chunk ITIN (WsA8) and blank out SSN (WsA5)
         * else format as NNN-NN-NNNN for data chunk SSN (WsA5) and blank out ITIN (WsA8)
         */
        if (unencryptedTaxId.charAt(0) == '9'
                && (unencryptedTaxId.charAt(3) == '7' || unencryptedTaxId.charAt(3) == '8')) {
            formattedSSNValueP.value = null;
            formattedITINValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_11_CHARS : buildFormattedTaxId(unencryptedTaxId);
        } else {
            formattedSSNValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_11_CHARS : buildFormattedTaxId(unencryptedTaxId);
            formattedITINValueP.value = null;
        }

        incrementAddressStatistics();

        // Derive Chapter 3 Status Code. (Set to blank if no mapping is found.)
        chapter3StatusCodeP.value = summary.vendorOwnershipToChapter3StatusMap.get(vendorOwnershipCodeP.value);

        // Prepare and write the biographic record.
        List<String> values = new ArrayList<>();
        for (SprintaxPaymentRowProcessor.RecordPiece piece : outputFieldDefinitions) {

            String val = StringUtils.defaultIfBlank(piece.getValue(), KFSConstants.EMPTY_STRING);

            if (val.length() > 4096) {
                val = StringUtils.left(val, 4096);
            }

            values.add(val);
        }

        String line = String.join(",", values);
        writer.write(line);
        writer.write("\n");
        writer.flush();

        writeWsBiographicRecord = false;
        incrementStatistic(TaxStatType.NUM_BIO_RECORDS_WRITTEN);

        // Setup output income code accordingly. It will be overridden as non-reportable if no gross amount is given.
        incomeCodeForOutputP.value = summary.zeroAmount.equals(grossAmountP.value) ? summary.nonReportableIncomeCode : incomeCodeP.value;

        /*
         * Set the Chapter 3 Exemption Code based on the tax-treaty-exempt-income and foreign-source-income flags. Values at the time of this writing:
         *
         * 03 if KFS nonresident tax attribute is Foreign Source Income
         * 04 if KFS nonresident tax attribute is Exempt under Treaty
         * 00 (not exempt) otherwise
         */
        if (taxTreatyExemptIncomeInd == null) {
            // Missing tax-treaty-exempt flag, set to not-exempt.
            chapter3ExemptionCodeP.value = summary.chapter3NotExemptExemptionCode;
        } else if (Boolean.TRUE.equals(taxTreatyExemptIncomeInd)) {
            // Tax-treaty-exempt flag is set to true, set to tax-treaty-exempt.
            chapter3ExemptionCodeP.value = summary.chapter3TaxTreatyExemptionCode;
        } else {
            // Tax-treaty-exempt flag is set to false, set to foreign-source or not-exempt based on the foreign-source-income flag.
            chapter3ExemptionCodeP.value = Boolean.TRUE.equals(foreignSourceIncomeInd)
                    ? summary.chapter3ForeignSourceExemptionCode : summary.chapter3NotExemptExemptionCode;
        }

        /*
         * Set the Chapter 3 Tax Rate depending on exemptions and the transaction detail input.
         *
         * FED_INC_TAX_PCT column value if non-null and no tax-treaty or foreign-source exemptions apply
         * 0 otherwise
         */
        chapter3TaxRateP.value = (fedIncomeTaxPctP.value != null && summary.chapter3NotExemptExemptionCode.equals(chapter3ExemptionCodeP.value))
                ? fedIncomeTaxPctP.value : summary.zeroAmount;
    }

    void incrementAddressStatistics() {
        if (CUTaxConstants.NO_US_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorUSAddressLine1P.value))) {
            incrementStatistic(TaxStatType.NUM_BIO_LINES_WITHOUT_US_ADDRESS);

            if (CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorForeignAddressLine1P.value))) {
                incrementStatistic(TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS);
                incrementStatistic(TaxStatType.NUM_BIO_LINES_WITHOUT_ANY_ADDRESS);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found an output row with no US or Foreign address on file! Key: " + rowKey);
                }
            }

        } else if (CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorForeignAddressLine1P.value))) {
            incrementStatistic(TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS);
        }
    }

    EnumMap<TaxStatType, Integer> getStatistics() {

        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED,
                dvStats.numGrossAmountsDetermined, pdpStats.numGrossAmountsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_FTW_AMOUNTS_DETERMINED,
                dvStats.numFtwAmountsDetermined, pdpStats.numFtwAmountsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_SITW_AMOUNTS_DETERMINED,
                dvStats.numSitwAmountsDetermined, pdpStats.numSitwAmountsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED,
                dvStats.numPaymentAddressExclusionsDetermined, pdpStats.numPaymentAddressExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED,
                dvStats.numDocNotesExclusionsDetermined, pdpStats.numDocNotesExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                dvStats.numRoyaltyObjChartAccountInclusionsDetermined, pdpStats.numRoyaltyObjChartAccountInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                dvStats.numRoyaltyObjChartAccountExclusionsDetermined, pdpStats.numRoyaltyObjChartAccountExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED,
                dvStats.numRoyaltyObjChartAccountNeitherDetermined, pdpStats.numRoyaltyObjChartAccountNeitherDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_FTW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                dvStats.numFtwObjChartAccountInclusionsDetermined, pdpStats.numFtwObjChartAccountInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_FTW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                dvStats.numFtwObjChartAccountExclusionsDetermined, pdpStats.numFtwObjChartAccountExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_FTW_CHART_ACCOUNT_NEITHER_DETERMINED,
                dvStats.numFtwObjChartAccountNeitherDetermined, pdpStats.numFtwObjChartAccountNeitherDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_SITW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                dvStats.numSitwObjChartAccountInclusionsDetermined, pdpStats.numSitwObjChartAccountInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_SITW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                dvStats.numSitwObjChartAccountExclusionsDetermined, pdpStats.numSitwObjChartAccountExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_SITW_CHART_ACCOUNT_NEITHER_DETERMINED,
                dvStats.numSitwObjChartAccountNeitherDetermined, pdpStats.numSitwObjChartAccountNeitherDetermined);

        return statistics;

    }


    /**
     * Sets the PreparedStatement instance at the given index.
     *
     * @param extraStatement The PreparedStatement to set.
     * @param statementIndex The index to associate with the PreparedStatement instance.
     * @throws IllegalStateException if a PreparedStatement instance already exists at the given index.
     */
    final void setExtraStatement(PreparedStatement extraStatement, int statementIndex) {
        if (extraStatements[statementIndex] != null) {
            throw new IllegalStateException("A PreparedStatement is already defined for index " + statementIndex);
        }
        extraStatements[statementIndex] = extraStatement;
    }
    public void setupOutputBuffer(List<SprintaxPaymentRowProcessor.RecordPiece> pieces) {
        outputFieldDefinitions = pieces.toArray(new SprintaxPaymentRowProcessor.RecordPiece[pieces.size()]);
    }


    // ========================================================================================
    // Start of query-configuration-and-execution helper methods.
    // ========================================================================================

    /**
     * Configures the given PreparedStatement with the specified int parameters, then executes it
     * and returns the results.
     *
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     *
     * @param rsIndex        The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1           The value to set for the first PreparedStatement parameter.
     * @param arg2           The value to set for the second PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameters.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, int arg1, int arg2) throws SQLException {
        PreparedStatement statement = extraStatements[statementIndex];
        statement.setInt(1, arg1);
        statement.setInt(2, arg2);
        return getAndReferenceResults(rsIndex, statement);
    }

    /**
     * Configures the given PreparedStatement with the specified String parameter, then executes it
     * and returns the results.
     *
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     *
     * @param rsIndex        The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1           The value to set for the first PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameter.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, String arg1) throws SQLException {
        extraStatements[statementIndex].setString(1, arg1);
        return getAndReferenceResults(rsIndex, extraStatements[statementIndex]);
    }

    /**
     * Closes any existing result set mapped to the given index,
     * runs the given query to get a new result set, associates
     * the new result with the index, and returns the result.
     * The current implementation uses the no-arg executeQuery()
     * method on the statement to get the results.
     *
     * @param index      The index of the result set to replace; must be nonnegative and smaller than the max value that was passed to the constructor.
     * @param pStatement The prepared statement to execute.
     * @return The results obtained from running the query.
     * @throws SQLException if a database access error occurs.
     */
    private ResultSet getAndReferenceResults(int index, PreparedStatement pStatement) throws SQLException {
        // Close current result set for index if one exists.
        ResultSet rs = extraResultSets[index];
        if (rs != null) {
            rs.close();
        }
        // Run the query, and store and return the results.
        rs = pStatement.executeQuery();
        extraResultSets[index] = rs;
        return rs;
    }

    // ========================================================================================
    // End of query-configuration-and-execution helper methods.
    // ========================================================================================


    /**
     * Helper method that subclasses can use to clear out
     * any of their references that are no longer needed
     * once all processing and logging is complete.
     */
    void clearReferences() {
        // Clear out ResultSet references.
        rsDummy = null;
        rsVendor = null;
        rsVendorUSAddress = null;
        rsVendorForeignAddress = null;
        rsDocNote = null;

        // Clear out "piece" references.
        if (detailStrings != null) {
            Arrays.fill(detailStrings, null);
            detailStrings = null;
        }
        if (detailInts != null) {
            Arrays.fill(detailInts, null);
            detailInts = null;
        }
        if (detailBigDecimals != null) {
            Arrays.fill(detailBigDecimals, null);
            detailBigDecimals = null;
        }
        if (detailDates != null) {
            Arrays.fill(detailDates, null);
            detailDates = null;
        }
        rowIdP = null;
        taxIdP = null;
        payeeIdP = null;
        incomeCodeP = null;
        incomeCodeSubTypeP = null;
        taxTreatyExemptIncomeYesNoP = null;
        foreignSourceIncomeYesNoP = null;
        vendorOwnershipCodeP = null;
        paymentAddressLine1P = null;
        objectCodeP = null;
        chartCodeP = null;
        accountNumberP = null;
        docTypeP = null;
        docNumberP = null;
        paymentReasonCodeP = null;
        dvCheckStubTextP = null;
        docLineNumberP = null;
        fedIncomeTaxPctP = null;
        paymentAmountP = null;
        paymentDateP = null;
        vendorLastNameP = null;
        vendorFirstNameP = null;
        vendorEmailAddressP = null;
        vendorUSAddressLine1P = null;
        vendorForeignAddressLine1P = null;
        vendorForeignAddressLine2P = null;
        vendorForeignCityNameP = null;
        vendorForeignZipCodeP = null;
        vendorForeignProvinceNameP = null;
        vendorForeignCountryCodeP = null;
        formattedSSNValueP = null;
        formattedITINValueP = null;
        chapter3StatusCodeP = null;
        chapter3ExemptionCodeP = null;
        chapter4ExemptionCodeP = null;
        incomeCodeForOutputP = null;
        taxEINValueP = null;
        stateCodeP = null;
        endDateP = null;
        chapter3TaxRateP = null;
        grossAmountP = null;
        ftwAmountP = null;
        sitwAmountP = null;

        // Perform other cleanup.
        tempRetrievedValue = null;
        encryptionService = null;
        dvStats = null;
        pdpStats = null;
        currentStats = null;
        vendorRow = null;
        vendorAddressRow = null;
        docNoteTextField = null;
        grossAmountField = null;
        ftwAmountField = null;
        sitwAmountField = null;
        taxBox = null;
    }

    private void incrementStatistic(TaxStatType statName) {
        Integer statValue = statistics.get(statName);
        statValue += 1;
        statistics.put(statName, statValue);
    }

    abstract static class RecordPiece {
        final String name;

        RecordPiece(String name) {
            this.name = name;
        }

        abstract String getValue() throws SQLException;

        void notifyOfTruncatedValue() {
            // Do nothing.
        }
    }

    static final class StaticStringRecordPiece extends SprintaxPaymentRowProcessor.RecordPiece {
        String value;

        StaticStringRecordPiece(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        String getValue() throws SQLException {
            return value;
        }
    }

    abstract static class IndexedColumnRecordPiece extends SprintaxPaymentRowProcessor.RecordPiece {
        // the index of the column to retrieve.
        final int columnIndex;

        IndexedColumnRecordPiece(String name, int columnIndex) {
            super(name);
            this.columnIndex = columnIndex;
        }
    }

    abstract class FormattedNumberRecordPiece extends SprintaxPaymentRowProcessor.IndexedColumnRecordPiece {
        // A flag indicating whether to use amount or percent formatter.
        final boolean useAmountFormat;

        FormattedNumberRecordPiece(String name, int columnIndex, boolean useAmountFormat) {
            super(name, columnIndex);
            this.useAmountFormat = useAmountFormat;
        }

        @Override
        String getValue() throws SQLException {
            return useAmountFormat ? amountFormat.format(getNumericValue()) : percentFormat.format(getNumericValue());
        }

        abstract Object getNumericValue() throws SQLException;
    }


    /**
     * Convenience RecordPiece subclass (as an *inner* class)
     * that formats date values.
     */
    abstract class FormattedDateRecordPiece extends SprintaxPaymentRowProcessor.IndexedColumnRecordPiece {
        FormattedDateRecordPiece(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            return dateFormat.format(getDateValue());
        }

        abstract Object getDateValue() throws SQLException;
    }

    private final class RecordPiece1042SBigDecimal extends SprintaxPaymentRowProcessor.FormattedNumberRecordPiece {
        private BigDecimal value;
        private boolean negateStringValue;

        private RecordPiece1042SBigDecimal(String name, int columnIndex, boolean useAmountFormat) {
            super(name, columnIndex, useAmountFormat);
        }

        @Override
        Object getNumericValue() throws SQLException {
            return negateStringValue ? value.negate() : value;
        }
    }


    private final class RecordPiece1042SDate extends SprintaxPaymentRowProcessor.FormattedDateRecordPiece {
        private java.sql.Date value;

        private RecordPiece1042SDate(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        Object getDateValue() throws SQLException {
            return value;
        }
    }


    private final class RecordPiece1042SResultSetDerivedString extends IndexedColumnRecordPiece {
        private final int rsIndex;

        private RecordPiece1042SResultSetDerivedString(String name, int columnIndex, int rsIndex) {
            super(name, columnIndex);
            this.rsIndex = rsIndex;
        }

        @Override
        String getValue() throws SQLException {
            switch (rsIndex) {
                case CUTaxConstants.VENDOR_DETAIL_INDEX :
                    tempRetrievedValue = rsVendor.getString(columnIndex);
                    break;

                case VENDOR_US_ADDRESS_INDEX :
                    tempRetrievedValue = rsVendorUSAddress.getString(columnIndex);
                    break;

                case VENDOR_FOREIGN_ADDRESS_INDEX :
                    tempRetrievedValue = rsVendorForeignAddress.getString(columnIndex);
                    break;

                default :
                    throw new IllegalStateException("Bad result set index configured!");
            }

            return tempRetrievedValue;
        }

        @Override
        void notifyOfTruncatedValue() {
            if (rsIndex == VENDOR_US_ADDRESS_INDEX) {
                if (columnIndex == vendorAddressRow.vendorLine2Address.index) {
                    LOG.warn("Found tax row whose vendor US address (line 2) had to be truncated! Key: " + rowKey);
                }
            } else if (rsIndex == VENDOR_FOREIGN_ADDRESS_INDEX) {
                if (columnIndex == vendorAddressRow.vendorLine2Address.index) {
                    LOG.warn("Found tax row whose vendor foreign address (line 2) had to be truncated! Key: " + rowKey);
                }
            }
        }
    }

    /**
     * Helper class containing various statistics that are specific
     * to a particular detail row origin (DV, PDP, etc.).
     */
    private final class OriginSpecificStats {
        private int numGrossAmountsDetermined;
        private int numFtwAmountsDetermined;
        private int numSitwAmountsDetermined;
        private int numPaymentAddressExclusionsDetermined;
        private int numDocNotesExclusionsDetermined;
        private int numRoyaltyObjChartAccountInclusionsDetermined;
        private int numRoyaltyObjChartAccountExclusionsDetermined;
        private int numRoyaltyObjChartAccountNeitherDetermined;
        private int numFtwObjChartAccountInclusionsDetermined;
        private int numFtwObjChartAccountExclusionsDetermined;
        private int numFtwObjChartAccountNeitherDetermined;
        private int numSitwObjChartAccountInclusionsDetermined;
        private int numSitwObjChartAccountExclusionsDetermined;
        private int numSitwObjChartAccountNeitherDetermined;
    }


    /*
     * ============================================================================================
     * Below are helper objects for encapsulating values read from or derived from the detail rows,
     * and which will potentially be included in the output files.
     * ============================================================================================
     */

    /**
     * Represents a String value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1042SString extends SprintaxPaymentRowProcessor.IndexedColumnRecordPiece {
        private String value;

        private RecordPiece1042SString(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            return value;
        }

        @Override
        void notifyOfTruncatedValue() {
            // Warn of certain truncated vendor name or vendor address values.
            if (this == vendorLastNameP) {
                LOG.warn("Found tax row whose vendor last name had to be truncated! Key: " + rowKey);
            } else if (this == vendorFirstNameP) {
                LOG.warn("Found tax row whose vendor first name had to be truncated! Key: " + rowKey);
            } else if (this == vendorUSAddressLine1P) {
                LOG.warn("Found tax row whose vendor US address (line 1) had to be truncated! Key: " + rowKey);
            } else if (this == vendorForeignAddressLine1P) {
                LOG.warn("Found tax row whose vendor foreign address (line 1) had to be truncated! Key: " + rowKey);
            }
        }
    }

    /**
     * Represents an int value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1042SInt extends SprintaxPaymentRowProcessor.IndexedColumnRecordPiece {
        private int value;
        private boolean negateStringValue;

        private RecordPiece1042SInt(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            return Integer.toString(negateStringValue ? -value : value);
        }
    }


}
