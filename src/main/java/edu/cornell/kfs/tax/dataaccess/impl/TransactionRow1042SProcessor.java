package edu.cornell.kfs.tax.dataaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DerivedValuesRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorAddressRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorRow;

/**
 * Default TransactionRowProcessor implementation for handling 1042S tax processing.
 * 
 * <p>This processor expects the following sections to be defined in the output definition XML:</p>
 * 
 * <ol>
 *   <li>Header section</li>
 *   <li>Biographic section</li>
 *   <li>Detail section</li>
 * </ol>
 * 
 * <p>This implementation will automatically append the header section to the other two, but
 * the output definition must still define the appropriate total length for those sections.</p>
 * 
 * <p>In addition, this processor has the following outputs:</p>
 * 
 * <ol>
 *   <li>Biographic records file</li>
 *   <li>Detail records file</li>
 * </ol>
 * 
 * <p>The following DERIVED-type fields will be handled by this implementation:</p>
 * 
 * <ul>
 *   <li>vendorLastName</li>
 *   <li>vendorFirstName</li>
 *   <li>vendorEmailAddress</li>
 *   <li>vendorUSAddressLine1</li>
 *   <li>vendorForeignAddressLine1</li>
 *   <li>vendorForeignAddressLine2</li>
 *   <li>vendorForeignCityName</li>
 *   <li>vendorForeignZipCode</li>
 *   <li>vendorForeignProvinceName</li>
 *   <li>vendorForeignCountryCode</li>
 *   <li>ssn</li>
 *   <li>itin</li>
 *   <li>chapter3StatusCode</li>
 *   <li>chapter3ExemptionCode</li>
 *   <li>chapter4ExemptionCode</li>
 *   <li>incomeCode</li>
 *   <li>ein</li>
 *   <li>chapter3TaxRate</li>
 *   <li>grossAmount</li>
 *   <li>fedTaxWithheldAmount</li>
 *   <li>stateIncomeTaxWithheldAmount</li>
 *   <li>stateCode</li>
 * </ul>
 * 
 * <p>When running in "scrubbed" mode, the following fields will be forcibly masked in the output:</p>
 * 
 * <ul>
 *   <li>ssn (DERIVED field)</li>
 *   <li>itin (DERIVED field)</li>
 *   <li>ein (DERIVED field)</li>
 *   <li>vendorGIIN (VENDOR field)</li>
 * </ul>
 */
class TransactionRow1042SProcessor extends TransactionRowProcessor<Transaction1042SSummary> {
	private static final Logger LOG = LogManager.getLogger(TransactionRow1042SProcessor.class);

    private static final int HEADER_BUFFER_INDEX = 0;
    private static final int BIO_BUFFER_INDEX = 1;
    private static final int DETAIL_BUFFER_INDEX = 2;

    private static final int BIO_WRITER_INDEX = 0;
    private static final int DETAIL_WRITER_INDEX = 1;

    private static final int VENDOR_US_ADDRESS_INDEX = 2;
    private static final int VENDOR_FOREIGN_ADDRESS_INDEX = 3;
    private static final int TAX_1042S_EXTRA_RS_SIZE = 4;

    private static final int NUM_1042S_CHAR_BUFFERS = 3;
    private static final int NUM_1042S_WRITERS = 2;
    private static final int MED_BUILDER_SIZE = 100;
    private static final int SMALL_BUILDER_SIZE = 30;

    // Constants pertaining to handling the unformatted tax ID.
    private static final int FOURTH_DIGIT_INDEX = 3;

    // Variables pertaining to the various query results.
    private ResultSet rsDummy;
    private ResultSet rsVendor;
    private ResultSet rsVendorUSAddress;
    private ResultSet rsVendorForeignAddress;
    private ResultSet rsDocNote;

    // Variables pertaining to the fields retrieved from each transaction detail row.
    private RecordPiece1042SString[] detailStrings;
    private RecordPiece1042SInt[] detailInts;
    private RecordPiece1042SBigDecimal[] detailBigDecimals;
    private RecordPiece1042SDate[] detailDates;

    // Variables pertaining to detail fields that always need to be retrieved for processing.
    private RecordPiece1042SString rowIdP;
    private RecordPiece1042SString taxIdP;
    private RecordPiece1042SString payeeIdP;
    private RecordPiece1042SString incomeCodeP;
    private RecordPiece1042SString incomeCodeSubTypeP;
    private RecordPiece1042SString taxTreatyExemptIncomeYesNoP;
    private RecordPiece1042SString foreignSourceIncomeYesNoP;
    private RecordPiece1042SString vendorOwnershipCodeP;
    private RecordPiece1042SString paymentAddressLine1P;
    private RecordPiece1042SString objectCodeP;
    private RecordPiece1042SString chartCodeP;
    private RecordPiece1042SString accountNumberP;
    private RecordPiece1042SString docTypeP;
    private RecordPiece1042SString docNumberP;
    private RecordPiece1042SString paymentReasonCodeP;
    private RecordPiece1042SString dvCheckStubTextP;
    private RecordPiece1042SInt docLineNumberP;
    private RecordPiece1042SBigDecimal fedIncomeTaxPctP;
    private RecordPiece1042SBigDecimal paymentAmountP;
    private RecordPiece1042SDate paymentDateP;

    // Variables pertaining to fields that are derived from the processing of other fields.
    private RecordPiece1042SString vendorLastNameP;
    private RecordPiece1042SString vendorFirstNameP;
    private RecordPiece1042SString vendorEmailAddressP;
    private RecordPiece1042SString vendorUSAddressLine1P;
    private RecordPiece1042SString vendorForeignAddressLine1P;
    private RecordPiece1042SString vendorForeignAddressLine2P;
    private RecordPiece1042SString vendorForeignCityNameP;
    private RecordPiece1042SString vendorForeignZipCodeP;
    private RecordPiece1042SString vendorForeignProvinceNameP;
    private RecordPiece1042SString vendorForeignCountryCodeP;
    private RecordPiece1042SString formattedSSNValueP;
    private RecordPiece1042SString formattedITINValueP;
    private RecordPiece1042SString chapter3StatusCodeP;
    private RecordPiece1042SString chapter3ExemptionCodeP;
    private RecordPiece1042SString chapter4ExemptionCodeP;
    private RecordPiece1042SString incomeCodeForOutputP;
    private RecordPiece1042SString taxEINValueP;
    private RecordPiece1042SString stateCodeP;
    private RecordPiece1042SDate endDateP;
    private RecordPiece1042SBigDecimal chapter3TaxRateP;
    private RecordPiece1042SBigDecimal grossAmountP;
    private RecordPiece1042SBigDecimal ftwAmountP;
    private RecordPiece1042SBigDecimal sitwAmountP;

    // Variables pertaining to values that need to be retrieved from the next detail row before populating the detail "piece" objects.
    private String nextTaxId;
    private String nextPayeeId;
    private String nextIncomeCode;
    private String nextIncomeCodeSubType;

    // Variables pertaining to decrypting and formatting the tax ID.
    private EncryptionService encryptionService;
    private String unencryptedTaxId;

    // Variables pertaining to vendor-related data.
    private VendorRow vendorRow;
    private VendorAddressRow vendorAddressRow;
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
    private boolean writeWsDetailRecord;
    private boolean writeWsBiographicRecord;
    private boolean foundAmount;
    private boolean isRoyaltyAmount;
    private boolean isParm1042SInclusion;
    private boolean isParm1042SExclusion;

    // Variables pertaining to various statistics.
    private int numTransactionRows;
    private int numBioRecordsWritten;
    private int numDetailRecordsWritten;
    private int numNoVendor;
    private int numNoParentVendor;
    private int numVendorNamesParsed;
    private int numVendorNamesNotParsed;
    private int numNoVendorAddressUS;
    private int numNoVendorAddressForeign;
    private int numPdpDocTypeExclusionsDetermined;
    private int numPaymentReasonExclusionsDetermined;
    private int numVendorTypeExclusionsDetermined;
    private int numOwnershipTypeExclusionsDetermined;
    private int numDocNoteSetsRetrieved;
    private int numDocNoteSetsNotRetrieved;
    private int numRoyaltyObjDvChkStubInclusionsDetermined;
    private int numRoyaltyObjDvChkStubExclusionsDetermined;
    private int numRoyaltyObjDvChkStubNeitherDetermined;
    private int numIncomeCodeExcludedGrossAmounts;
    private int numIncomeCodeSubTypeExcludedGrossAmounts;
    private int numIncomeCodeExcludedFtwAmounts;
    private int numIncomeCodeSubTypeExcludedFtwAmounts;
    private int numIncomeCodeExcludedSitwAmounts;
    private int numIncomeCodeSubTypeExcludedSitwAmounts;
    private int numNoBoxDeterminedRows;
    private int numBioLinesWithTruncatedUSAddress;
    private int numBioLinesWithTruncatedForeignAddress;
    private int numBioLinesWithoutUSAddress;
    private int numBioLinesWithoutForeignAddress;
    private int numBioLinesWithoutAnyAddress;
    private int numDetailLinesWithPositiveFtwAmount;
    private int numDetailLinesWithPositiveSitwAmount;

    // Variables pertaining to tax-source-specific statistics.
    private OriginSpecificStats dvStats;
    private OriginSpecificStats pdpStats;
    private OriginSpecificStats currentStats;



    TransactionRow1042SProcessor() {
        super(TAX_1042S_EXTRA_RS_SIZE, TAX_1042S_EXTRA_RS_SIZE, NUM_1042S_CHAR_BUFFERS, NUM_1042S_WRITERS);
    }



    @Override
    RecordPiece getPieceForField(TaxFieldSource fieldSource, TaxTableField field, String name, int len, Transaction1042SSummary summary) {
        RecordPiece piece;
        
        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot create piece for BLANK type");
            
            case STATIC :
                throw new IllegalArgumentException("Cannot create piece for STATIC type");
            
            case DETAIL :
                // Create a piece that pre-loads its value from the current transaction detail ResultSet line at runtime.
                piece = getPieceForFieldInternal(field, name, len, summary);
                break;
            
            case PDP :
                throw new IllegalArgumentException("Cannot create piece for PDP type");
            
            case DV :
                throw new IllegalArgumentException("Cannot create piece for DV type");
            
            case VENDOR :
                // Create a piece that derives its value directly from the vendor ResultSet at runtime, or a static masked piece if GIIN and in scrubbed mode.
                piece = (!summary.scrubbedOutput || field.index != summary.vendorRow.vendorGIIN.index)
                        ? new RecordPiece1042SResultSetDerivedString(name, len, field.index, CUTaxConstants.VENDOR_DETAIL_INDEX)
                        : new StaticStringRecordPiece(name, len, CUTaxConstants.MASKED_VALUE_19_CHARS);
                break;
            
            case VENDOR_US_ADDRESS :
                // Create a piece that derives its value directly from the vendor US address ResultSet at runtime.
                piece = new RecordPiece1042SResultSetDerivedString(name, len,
                        field.index, VENDOR_US_ADDRESS_INDEX);
                break;
            
            case VENDOR_ANY_ADDRESS :
                // Non-country-specific pieces are not supported by this implementation.
                throw new IllegalArgumentException("The VENDOR_ANY_ADDRESS type is not supported for 1042S processing");
            
            case DOCUMENT_NOTE :
                throw new IllegalArgumentException("Cannot create piece for DOCUMENT_NOTE type");
            
            case DERIVED :
                // Create a piece whose value will be set by the 1042S processing.
                piece = getPieceForFieldInternal(field, name, len, summary);
                break;
            
            default :
                throw new IllegalArgumentException("Unrecognized piece type");
        }
        
        return piece;
    }



    /*
     * Internal helper method for creating DETAIL or DERIVED RecordPiece instances.
     */
    private RecordPiece getPieceForFieldInternal(TaxTableField field, String name, int len, Transaction1042SSummary summary) {
        RecordPiece piece;
        
        switch (field.jdbcType) {
            case java.sql.Types.DECIMAL :
                piece = new RecordPiece1042SBigDecimal(name, len, field.index,
                        !summary.transactionDetailRow.federalIncomeTaxPercent.equals(field) && !summary.derivedValues.chapter3TaxRate.equals(field));
                break;
            
            case java.sql.Types.INTEGER :
                piece = new RecordPiece1042SInt(name, len, field.index);
                break;
            
            case java.sql.Types.VARCHAR :
                piece = new RecordPiece1042SString(name, len, field.index);
                break;
            
            case java.sql.Types.DATE :
                piece = new RecordPiece1042SDate(name, len, field.index);
                break;
            
            default :
                throw new IllegalStateException("This processor does not support data fields of the given JDBC type: " + Integer.valueOf(field.jdbcType));
        }
        
        return piece;
    }



    @Override
    Set<TaxTableField> getMinimumFields(TaxFieldSource fieldSource, Transaction1042SSummary summary) {
        Set<TaxTableField> minFields = new HashSet<TaxTableField>();
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        DerivedValuesRow derivedValues = summary.derivedValues;
        
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



    @Override
    void setComplexPieces(Map<String,RecordPiece> complexPieces, Transaction1042SSummary summary) {
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        DerivedValuesRow derivedValues = summary.derivedValues;
        List<RecordPiece1042SString> stringPieces = new ArrayList<RecordPiece1042SString>();
        List<RecordPiece1042SInt> intPieces = new ArrayList<RecordPiece1042SInt>();
        List<RecordPiece1042SBigDecimal> bigDecimalPieces = new ArrayList<RecordPiece1042SBigDecimal>();
        List<RecordPiece1042SDate> datePieces = new ArrayList<RecordPiece1042SDate>();
        
        // retrieve the "piece" objects corresponding to all the given transaction detail fields.
        for (TaxTableField detailField : summary.transactionDetailRow.orderedFields) {
            RecordPiece detailPiece = complexPieces.get(detailField.propertyName);
            if (detailPiece != null) {
                switch (detailField.jdbcType) {
                    case java.sql.Types.DECIMAL :
                        bigDecimalPieces.add((RecordPiece1042SBigDecimal) detailPiece);
                        break;
                    
                    case java.sql.Types.INTEGER :
                        intPieces.add((RecordPiece1042SInt) detailPiece);
                        break;
                    
                    case java.sql.Types.VARCHAR :
                        stringPieces.add((RecordPiece1042SString) detailPiece);
                        break;
                    
                    case java.sql.Types.DATE :
                        datePieces.add((RecordPiece1042SDate) detailPiece);
                        break;
                    
                    default :
                        throw new IllegalStateException("Found unsupported detail field JDBC type: " + Integer.toString(detailField.jdbcType));
                }
            }
        }
        
        // Setup the transaction detail "piece" arrays.
        detailStrings = stringPieces.toArray(new RecordPiece1042SString[stringPieces.size()]);
        detailInts = intPieces.toArray(new RecordPiece1042SInt[intPieces.size()]);
        detailBigDecimals = bigDecimalPieces.toArray(new RecordPiece1042SBigDecimal[bigDecimalPieces.size()]);
        detailDates = datePieces.toArray(new RecordPiece1042SDate[datePieces.size()]);
        
        // Retrieve the various detail "pieces" that will be needed for the processing.
        rowIdP = (RecordPiece1042SString) complexPieces.get(detailRow.transactionDetailId.propertyName);
        docNumberP = (RecordPiece1042SString) complexPieces.get(detailRow.documentNumber.propertyName);
        docTypeP = (RecordPiece1042SString) complexPieces.get(detailRow.documentType.propertyName);
        docLineNumberP = (RecordPiece1042SInt) complexPieces.get(detailRow.financialDocumentLineNumber.propertyName);
        objectCodeP = (RecordPiece1042SString) complexPieces.get(detailRow.finObjectCode.propertyName);
        paymentAmountP = (RecordPiece1042SBigDecimal) complexPieces.get(detailRow.netPaymentAmount.propertyName);
        taxIdP = (RecordPiece1042SString) complexPieces.get(detailRow.vendorTaxNumber.propertyName);
        incomeCodeP = (RecordPiece1042SString) complexPieces.get(detailRow.incomeCode.propertyName);
        incomeCodeSubTypeP = (RecordPiece1042SString) complexPieces.get(detailRow.incomeCodeSubType.propertyName);
        dvCheckStubTextP = (RecordPiece1042SString) complexPieces.get(detailRow.dvCheckStubText.propertyName);
        payeeIdP = (RecordPiece1042SString) complexPieces.get(detailRow.payeeId.propertyName);
        vendorOwnershipCodeP = (RecordPiece1042SString) complexPieces.get(detailRow.vendorOwnershipCode.propertyName);
        paymentDateP = (RecordPiece1042SDate) complexPieces.get(detailRow.paymentDate.propertyName);
        taxTreatyExemptIncomeYesNoP = (RecordPiece1042SString) complexPieces.get(detailRow.incomeTaxTreatyExemptIndicator.propertyName);
        foreignSourceIncomeYesNoP = (RecordPiece1042SString) complexPieces.get(detailRow.foreignSourceIncomeIndicator.propertyName);
        fedIncomeTaxPctP = (RecordPiece1042SBigDecimal) complexPieces.get(detailRow.federalIncomeTaxPercent.propertyName);
        paymentAddressLine1P = (RecordPiece1042SString) complexPieces.get(detailRow.paymentLine1Address.propertyName);
        chartCodeP = (RecordPiece1042SString) complexPieces.get(detailRow.chartCode.propertyName);
        accountNumberP = (RecordPiece1042SString) complexPieces.get(detailRow.accountNumber.propertyName);
        paymentReasonCodeP = (RecordPiece1042SString) complexPieces.get(detailRow.paymentReasonCode.propertyName);
        
        // Retrieve the various derived "pieces" that will be needed for the processing.
        vendorLastNameP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorLastName.propertyName);
        vendorFirstNameP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorFirstName.propertyName);
        vendorEmailAddressP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorEmailAddress.propertyName);
        vendorUSAddressLine1P = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorUSAddressLine1.propertyName);
        vendorForeignAddressLine1P = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignAddressLine1.propertyName);
        vendorForeignAddressLine2P = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignAddressLine2.propertyName);
        vendorForeignCityNameP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignCityName.propertyName);
        vendorForeignZipCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignZipCode.propertyName);
        vendorForeignProvinceNameP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignProvinceName.propertyName);
        vendorForeignCountryCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.vendorForeignCountryCode.propertyName);
        formattedSSNValueP = (RecordPiece1042SString) complexPieces.get(derivedValues.ssn.propertyName);
        formattedITINValueP = (RecordPiece1042SString) complexPieces.get(derivedValues.itin.propertyName);
        chapter3StatusCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.chapter3StatusCode.propertyName);
        chapter3ExemptionCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.chapter3ExemptionCode.propertyName);
        chapter4ExemptionCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.chapter4ExemptionCode.propertyName);
        incomeCodeForOutputP = (RecordPiece1042SString) complexPieces.get(derivedValues.incomeCode.propertyName);
        taxEINValueP = (RecordPiece1042SString) complexPieces.get(derivedValues.ein.propertyName);
        chapter3TaxRateP = (RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.chapter3TaxRate.propertyName);
        grossAmountP = (RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.grossAmount.propertyName);
        ftwAmountP = (RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.fedTaxWithheldAmount.propertyName);
        ftwAmountP.negateStringValue = true;
        sitwAmountP = (RecordPiece1042SBigDecimal) complexPieces.get(derivedValues.stateIncomeTaxWithheldAmount.propertyName);
        sitwAmountP.negateStringValue = true;
        stateCodeP = (RecordPiece1042SString) complexPieces.get(derivedValues.stateCode.propertyName);
        endDateP = (RecordPiece1042SDate) complexPieces.get(derivedValues.endDate.propertyName);
    }



    @Override
    String[] getFilePathsForWriters(Transaction1042SSummary summary, java.util.Date processingStartDate) {
        String[] filePaths = super.getFilePathsForWriters(summary, processingStartDate);
        DateFormat tempFormat = buildDateFormatForFileSuffixes();
        // Output file for 1042S biographic records.
        filePaths[BIO_WRITER_INDEX] =
                new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/').append(CUTaxConstants.TAX_1042S_BIO_OUTPUT_FILE_PREFIX)
                        .append(summary.reportYear).append(tempFormat.format(processingStartDate)).append(CUTaxConstants.TAX_OUTPUT_FILE_SUFFIX).toString();
        // Output file for 1042S detail records.
        filePaths[DETAIL_WRITER_INDEX] =
                new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/').append(CUTaxConstants.TAX_1042S_DETAIL_OUTPUT_FILE_PREFIX)
                        .append(summary.reportYear).append(tempFormat.format(processingStartDate)).append(CUTaxConstants.TAX_OUTPUT_FILE_SUFFIX).toString();
        return filePaths;
    }

    @Override
    String[] getSqlForExtraStatements(Transaction1042SSummary summary) {
        String[] extraSql = super.getSqlForExtraStatements(summary);
        
        extraSql[VENDOR_US_ADDRESS_INDEX] = TaxSqlUtils.getVendorAddressSelectSql(summary.vendorAddressRow, Boolean.TRUE);
        extraSql[VENDOR_FOREIGN_ADDRESS_INDEX] = TaxSqlUtils.getVendorAddressSelectSql(summary.vendorAddressRow, Boolean.FALSE);
        
        return extraSql;
    }

    @Override
    Object[][] getDefaultParameterValuesForExtraStatement(int statementIndex, Transaction1042SSummary summary) {
        if (VENDOR_US_ADDRESS_INDEX == statementIndex || VENDOR_FOREIGN_ADDRESS_INDEX == statementIndex) {
            // For vendor address statements, set defaults so that only the first two parameters need to be updated by the processing.
            return new Object[][] {
                {Integer.valueOf(0)},
                {Integer.valueOf(0)},
                {KFSConstants.COUNTRY_CODE_UNITED_STATES}
            };
        } else {
            // Don't set up defaults for all other parameters.
            return super.getDefaultParameterValuesForExtraStatement(statementIndex, summary);
        }
    }

    @Override
    String getSqlForSelect(Transaction1042SSummary summary) {
        return TaxSqlUtils.getTransactionDetailSelectSql(summary.transactionDetailRow.form1042SBox, summary.transactionDetailRow, true, true);
    }

    @Override
    Object[][] getParameterValuesForSelect(Transaction1042SSummary summary) {
        return new Object[][] {
            {Integer.valueOf(summary.reportYear)},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }



    @Override
    void processTaxRows(ResultSet rs, Transaction1042SSummary summary) throws SQLException, IOException {
        boolean keepLooping = true;
        boolean taxIdChanged = false;
        TransactionDetailRow detailRow;
        
        encryptionService = CoreApiServiceLocator.getEncryptionService();
        dvStats = new OriginSpecificStats();
        pdpStats = new OriginSpecificStats();
        
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
            numTransactionRows++;
            nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
            nextPayeeId = rs.getString(detailRow.payeeId.index);
            nextIncomeCode = rs.getString(detailRow.incomeCode.index);
            nextIncomeCodeSubType = rs.getString(detailRow.incomeCodeSubType.index);
            vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
            vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
            taxIdChanged = true;
            if (StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for initial row with payee " + nextPayeeId);
            }
            LOG.info("Starting transaction row processing for 1042S tax reporting...");
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
            rowKey = new StringBuilder(MED_BUILDER_SIZE)
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
            taxTreatyExemptIncomeInd = StringUtils.isNotBlank(taxTreatyExemptIncomeYesNoP.value)
                    ? Boolean.valueOf(KRADConstants.YES_INDICATOR_VALUE.equals(taxTreatyExemptIncomeYesNoP.value)) : null;
            foreignSourceIncomeInd = StringUtils.isNotBlank(foreignSourceIncomeYesNoP.value)
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
                loadNewVendor(summary);
            }
            
            // If necessary, check for exclusions and identify the type of amount.
            if (!summary.zeroAmount.equals(paymentAmountP.value)) {
                checkForExclusionsAndAmounts(summary);
            }
            
            // Perform final inclusion/exclusion processing, and update amounts accordingly.
            processExclusionsAndAmounts(rs, detailRow, summary);
            
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
            
            
            
            // Move to next row (if any) and update the looping flag as needed.
            if (rs.next()) {
                // If more rows are available, then update counters and retrieve field values as needed.
                numTransactionRows++;
                nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
                nextPayeeId = rs.getString(detailRow.payeeId.index);
                nextIncomeCode = rs.getString(detailRow.incomeCode.index);
                nextIncomeCodeSubType = rs.getString(detailRow.incomeCodeSubType.index);
                vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
                vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
                // Check for changes to the tax ID between rows. The prior tax ID should be non-null at this point.
                taxIdChanged = StringUtils.isBlank(nextTaxId) || !taxIdP.value.equals(nextTaxId);
            } else {
                // If no more rows, then prepare to exit the loop and process any leftover data from the previous iterations.
                keepLooping = false;
                writeWsDetailRecord = true;
            }
            
            // Automatically abort with an error if the current row has no tax ID.
            if (StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for row with payee " + nextPayeeId);
            }
            
            
            
            // If necessary, determine whether changes to tax ID, income code or subtype warrant writing a detail line.
            if (!writeWsDetailRecord) {
                if ((StringUtils.isNotBlank(incomeCodeP.value) && (StringUtils.isBlank(nextIncomeCode) || !incomeCodeP.value.equals(nextIncomeCode)))
                        || (taxIdChanged && foundAmount)) {
                    // Write record due to change from non-blank code to new or blank code, or due to tax ID change if gross/ftw/sitw amounts were found.
                    writeWsDetailRecord = true;
                } else if (StringUtils.isNotBlank(incomeCodeSubTypeP.value)
                        && (StringUtils.isBlank(nextIncomeCodeSubType) || !incomeCodeSubTypeP.value.equals(nextIncomeCodeSubType))) {
                    // Write record due to change from non-blank subtype to new or blank subtype.
                    writeWsDetailRecord = true;
                }
            }
            
            /*
             * Do not write a new line to the file if no valid gross/ftw/sitw amount was found yet OR (as per Lori Kanellis) the following criteria are met:
             * 
             * The line does NOT have an Exemption Code of 3 (Foreign Source) or 4 (Treaty Exempt)
             * AND it does NOT have a Federal tax withholding RATE
             * AND it does NOT have a Federal Tax Withholding amount
             */
            if (!foundAmount || (
                    !Boolean.TRUE.equals(taxTreatyExemptIncomeInd)
                    && !Boolean.TRUE.equals(foreignSourceIncomeInd)
                    && (fedIncomeTaxPctP.value == null || summary.zeroAmount.equals(fedIncomeTaxPctP.value))
                    && summary.zeroAmount.equals(ftwAmountP.value)
                    )) {
                writeWsDetailRecord = false;
            }
            
            
            
            /*
             * If needed, write new detail and/or biographic records to the appropriate output files.
             */
            if (writeWsDetailRecord) {
                writeLinesToFiles(summary);
            }
            
            // END OF LOOP
        }
        
        LOG.info("Finished transaction row processing for 1042S tax reporting.");
    }



    /*
     * Helper method for loading the relevant transaction detail fields into the associated "piece" objects.
     */
    private void loadTransactionRowValuesFromResults(ResultSet rs) throws SQLException {
        // Get String values.
        for (RecordPiece1042SString detailString : detailStrings) {
            detailString.value = rs.getString(detailString.columnIndex);
        }
        // Get int values.
        for (RecordPiece1042SInt detailInt : detailInts) {
            detailInt.value = rs.getInt(detailInt.columnIndex);
        }
        // Get BigDecimal values.
        for (RecordPiece1042SBigDecimal detailBigDecimal : detailBigDecimals) {
            detailBigDecimal.value = rs.getBigDecimal(detailBigDecimal.columnIndex);
        }
        // Get java.sql.Date values.
        for (RecordPiece1042SDate detailDate : detailDates) {
            detailDate.value = rs.getDate(detailDate.columnIndex);
        }
    }



    /*
     * Helper method for loading vendor info for the associated transaction detail row, and for resetting variables as needed.
     */
    private void loadNewVendor(Transaction1042SSummary summary) throws SQLException {
        // Reset flags and other variables as needed.
        grossAmountP.value = summary.zeroAmount;
        ftwAmountP.value = summary.zeroAmount;
        sitwAmountP.value = summary.zeroAmount;
        vendorLastNameP.value = null;
        vendorFirstNameP.value = null;
        foundAmount = false;
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
                if (StringUtils.equals(summary.soleProprietorOwnerCode, rsVendor.getString(vendorRow.vendorOwnershipCode.index))) {
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
                if (StringUtils.isNotBlank(vendorNameForOutput)) {
                    // Parse vendor last name and first name.
                    if (KRADConstants.YES_INDICATOR_VALUE.equals(rsVendor.getString(vendorRow.vendorFirstLastNameIndicator.index))
                            && vendorNameForOutput.indexOf(',') != -1) {
                        vendorLastNameP.value = vendorNameForOutput.substring(0, vendorNameForOutput.indexOf(',')).trim();
                        vendorFirstNameP.value = vendorNameForOutput.substring(vendorNameForOutput.indexOf(',') + 1).trim();
                    } else {
                        vendorLastNameP.value = vendorNameForOutput.trim();
                    }
                    numVendorNamesParsed++;
                } else {
                    numVendorNamesNotParsed++;
                }
            } else {
                currentVendorDetailId = vendorDetailId;
                vendorNameForOutput = new StringBuilder(SMALL_BUILDER_SIZE)
                        .append("No Parent Vendor (").append(vendorHeaderId).append(')').append('!').toString();
                numNoParentVendor++;
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
            vendorNameForOutput = new StringBuilder(SMALL_BUILDER_SIZE)
                    .append("No Vendor (").append(vendorHeaderId).append('-').append(vendorDetailId).append(')').append('!').toString();
            parentVendorNameForOutput = null;
            numNoVendor++;
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
            numNoVendorAddressUS++;
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
            numNoVendorAddressForeign++;
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
        if (StringUtils.isBlank(chapter4ExemptionCodeP.value)) {
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
    private void checkForExclusionsAndAmounts(Transaction1042SSummary summary) throws SQLException {
        int idx;
        boolean foundMatch;
        String tempValue;
        Set<String> distinctValues;
        
        incomeClassCodeFromMap = summary.objectCodeToIncomeClassCodeMap.get(objectCodeP.value);
        
        /*
         * Determine if object code is one of the types that allows for this tax processing.
         * If so, then indicate whether the amount represents a gross, FTW, or SITW one.
         */
        if (StringUtils.isNotBlank(incomeClassCodeFromMap)) {
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
                numVendorTypeExclusionsDetermined++;
            }
            
            if (summary.excludedOwnershipTypeCodes.contains(rsVendor.getString(vendorRow.vendorOwnershipCode.index))) {
                foundExclusion = true;
                numOwnershipTypeExclusionsDetermined++;
            }
        }
        
        // Check if the doc type is a particular PDP one that should be excluded from the processing.
        if (taxBox != null && !excludeTransaction) {
            if (summary.pdpExcludedDocTypes.contains(docTypeP.value)) {
                foundExclusion = true;
                numPdpDocTypeExclusionsDetermined++;
            }
        }
        
        // Check if the payment reason code is one that should be excluded from the processing.
        if (!excludeTransaction && StringUtils.isNotBlank(paymentReasonCodeP.value)) {
            if (summary.excludedPaymentReasonCodes.contains(paymentReasonCodeP.value)) {
                excludeTransaction = true;
                foundExclusion = true;
                numPaymentReasonExclusionsDetermined++;
            }
        }
        
        // Check if the row should be excluded from the processing based on the start of the payment line 1 address.
        if (taxBox != null && !excludeTransaction) {
            if (StringUtils.isNotBlank(paymentAddressLine1P.value)) {
                tempValue = paymentAddressLine1P.value.toUpperCase();
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
                numDocNoteSetsRetrieved++;
                foundMatch = false;
                
                do {
                    tempValue = rsDocNote.getString(docNoteTextField.index);
                    if (StringUtils.isNotBlank(tempValue)) {
                        tempValue = tempValue.toUpperCase();
                        
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
                numDocNoteSetsNotRetrieved++;
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
                    && StringUtils.isNotBlank(incomeClassCodeFromMap) && summary.incomeClassCodesDenotingRoyalties.contains(incomeClassCodeFromMap);
            
            if (isRoyaltyAmount && isDVRow) {
                // Check for royalty limitations based on DV check stub text.
                royaltiesInclusionInd = determineClusionWithPatterns(dvCheckStubTextP.value,
                        summary.royaltiesIncludedObjectCodeAndDvCheckStubTextMap.get(objectCodeP.value), summary.royaltiesObjCodeDvChkStubTextIsWhitelist);
                if (royaltiesInclusionInd == null) {
                    // No DV-check-stub restrictions exist for the given object code.
                    numRoyaltyObjDvChkStubNeitherDetermined++;
                } else if (royaltiesInclusionInd.booleanValue()) {
                    // Found value in whitelist, or did not find value in blacklist.
                    numRoyaltyObjDvChkStubInclusionsDetermined++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    numRoyaltyObjDvChkStubExclusionsDetermined++;
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
     * Helper method for determining final inclusion/exclusion state, and for updating box amounts and box type indicators as needed.
     */
    private void processExclusionsAndAmounts(ResultSet rs, TransactionDetailRow detailRow, Transaction1042SSummary summary) throws SQLException {
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
                    numIncomeCodeExcludedGrossAmounts++;
                } else if (taxBox == ftwAmountField) {
                    isParm1042SExclusion = true;
                    numIncomeCodeExcludedFtwAmounts++;
                } else if (taxBox == sitwAmountField) {
                    isParm1042SExclusion = true;
                    numIncomeCodeExcludedSitwAmounts++;
                }
            } else if (summary.excludedIncomeCodeSubType.equals(incomeCodeSubTypeP.value)) {
                // If needed, exclude based on income code sub-type.
                if (taxBox == grossAmountField) {
                    isParm1042SExclusion = true;
                    numIncomeCodeSubTypeExcludedGrossAmounts++;
                } else if (taxBox == ftwAmountField) {
                    isParm1042SExclusion = true;
                    numIncomeCodeSubTypeExcludedFtwAmounts++;
                } else if (taxBox == sitwAmountField) {
                    isParm1042SExclusion = true;
                    numIncomeCodeSubTypeExcludedSitwAmounts++;
                }
            }
        }
        
        
        
        // Check for tax box overrides.
        overrideTaxBox = summary.transactionOverrides.get(new StringBuilder(SMALL_BUILDER_SIZE)
                .append(paymentDateP.value).append(';')
                .append(docNumberP.value).append(';')
                .append(docLineNumberP.value).toString());
        if (StringUtils.isNotBlank(overrideTaxBox)) {
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
        
        // Perform logging and updates depending on amount type and exclusions.
        if ((isParm1042SExclusion && StringUtils.isBlank(overrideTaxBox)) || CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY.equals(overrideTaxBox)) {
            // If exclusion and no overrides (or an override to a non-reportable box type), then do not update amounts.
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
            if (taxBox != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found exclusions for row with key: " + rowKey);
                }
            } else {
                numNoBoxDeterminedRows++;
            }
            
        } else if (taxBox == grossAmountField) {
            // If detail row has gross amount, then update flags, amounts, and the current row as needed.
            grossAmountP.value = grossAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_GROSS_BOX);
            foundAmount = true;
            
        } else if (taxBox == ftwAmountField) {
            // If detail row has FTW amount, then update flags, amounts, and the current row as needed.
            ftwAmountP.value = ftwAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX);
            foundAmount = true;
            
        } else if (taxBox == sitwAmountField) {
            // If detail row has SITW amount, then update flags, amounts, and the current row as needed.
            sitwAmountP.value = sitwAmountP.value.add(paymentAmountP.value);
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX);
            foundAmount = true;
            
        } else {
            // If no exclusions but box is still undetermined, then do not update amounts.
            numNoBoxDeterminedRows++;
            rs.updateString(detailRow.form1042SBox.index, CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY);
        }
        
        // If tax box was overridden, then set previous value on transaction row.
        if (StringUtils.isNotBlank(overriddenTaxBox)) {
            rs.updateString(detailRow.form1042SOverriddenBox.index, overriddenTaxBox);
        }
    }



    /*
     * Helper method for writing 1042S biographic and detail records to their respective files.
     */
    private void writeLinesToFiles(Transaction1042SSummary summary) throws SQLException, IOException {
        /*
         * If needed, replace the header record's contents and write a new biographic record.
         */
        if (writeWsBiographicRecord) {
            /*
             * SSN-vs-ITIN logic per Loree Kanellis:
             * If the taxID starts with a '9' and the fourth digit is a '7' or '8'
             * then format as NNN-NN-NNNN for data chunk ITIN (WsA8) and blank out SSN (WsA5)
             * else format as NNN-NN-NNNN for data chunk SSN (WsA5) and blank out ITIN (WsA8)
             */
            if (unencryptedTaxId.charAt(0) == '9'
                    && (unencryptedTaxId.charAt(FOURTH_DIGIT_INDEX) == '7' || unencryptedTaxId.charAt(FOURTH_DIGIT_INDEX) == '8')) {
                formattedSSNValueP.value = null;
                formattedITINValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_11_CHARS : buildFormattedTaxId(unencryptedTaxId);
            } else {
                formattedSSNValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_11_CHARS : buildFormattedTaxId(unencryptedTaxId);
                formattedITINValueP.value = null;
            }
            
            // Perform logging and processing as needed if no US or foreign vendor addresses could be found.
            if (CUTaxConstants.NO_US_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorUSAddressLine1P.value))) {
                numBioLinesWithoutUSAddress++;
                if (CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorForeignAddressLine1P.value))) {
                    numBioLinesWithoutForeignAddress++;
                    numBioLinesWithoutAnyAddress++;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found an output row with no US or Foreign address on file! Key: " + rowKey);
                    }
                }
            } else if (CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS.equalsIgnoreCase(StringUtils.trim(vendorForeignAddressLine1P.value))) {
                numBioLinesWithoutForeignAddress++;
            }
            
            // Derive Chapter 3 Status Code. (Set to blank if no mapping is found.)
            chapter3StatusCodeP.value = summary.vendorOwnershipToChapter3StatusMap.get(vendorOwnershipCodeP.value);
            
            
            
            // Prepare the header.
            resetBuffer(HEADER_BUFFER_INDEX);
            appendPieces(HEADER_BUFFER_INDEX);
            
            // Prepare and write the biographic record.
            resetBuffer(BIO_BUFFER_INDEX);
            appendBuffer(HEADER_BUFFER_INDEX, BIO_BUFFER_INDEX);
            appendPieces(BIO_BUFFER_INDEX);
            writeBufferToOutput(BIO_BUFFER_INDEX, BIO_WRITER_INDEX);
            
            writeWsBiographicRecord = false;
            numBioRecordsWritten++;
        }
        
        
        
        // Setup output income code accordingly. It will be overridden as non-reportable if no gross amount is given.
        incomeCodeForOutputP.value = summary.zeroAmount.equals(grossAmountP.value)
                ? summary.nonReportableIncomeCode : incomeCodeP.value;
        
        /*
         * Warn about positive FTW amount. Per Loree Kanellis:
         * 
         * All Federal Tax Withholding transactions are negative numbers,
         * so, if there are multiple federal withholding transactions (for the same payee),
         * the net of all Federal Tax Withholding transactions would be a negative number
         * (e.g. -50.00 + -4.52 = Net -54.52)
         * Since we need to report to the IRS only positive numbers, this net negative amount
         * would be changed to a positive value.
         * 
         * The issue is that if the net Federal Tax withholding is a positive number
         * (e.g. -4.52 + 50.00 = Net 45.48), the net of all Federal Tax withholding
         * transactions would need to be reported as a negative amount.
         * Even though we cannot report negative amounts to the IRS, we need to know that
         * this result would be reflected as a negative number.  It would be a rare occurrence
         * that could happen AND we would need to perform an Override.
         */
        if (ftwAmountP.value.compareTo(summary.zeroAmount) > 0) {
            numDetailLinesWithPositiveFtwAmount++;
            LOG.warn("Found a detail row with FTW amount greater than zero! May need override. Key: " + rowKey);
        }
        
        /*
         * Warn about positive SITW amount. Per Loree Kanellis:
         * 
         * All State Income Tax Withholding transactions are negative numbers,
         * so, if there are multiple state income withholding transactions (for the same payee),
         * the net of all State Income Tax Withholding transactions would be a negative number
         * (e.g. -50.00 + -4.52 = Net -54.52)
         * Since we need to report to the IRS only positive numbers, this net negative amount
         * would be changed to a positive value.
         * 
         * The issue is that if the net State Income Tax withholding is a positive number
         * (e.g. -4.52 + 50.00 = Net 45.48), the net of all State Income Tax withholding
         * transactions would need to be reported as a negative amount.
         * Even though we cannot report negative amounts to the IRS, we need to know that
         * this result would be reflected as a negative number.  It would be a rare occurrence
         * that could happen AND we would need to perform an Override.
         */
        if (sitwAmountP.value.compareTo(summary.zeroAmount) > 0) {
            numDetailLinesWithPositiveSitwAmount++;
            LOG.warn("Found a detail row with SITW amount greater than zero! May need override. Key: " + rowKey);
        }
        
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
        
        
        
        // Prepare and write the detail record.
        resetBuffer(DETAIL_BUFFER_INDEX);
        appendBuffer(HEADER_BUFFER_INDEX, DETAIL_BUFFER_INDEX);
        appendPieces(DETAIL_BUFFER_INDEX);
        writeBufferToOutput(DETAIL_BUFFER_INDEX, DETAIL_WRITER_INDEX);
        numDetailRecordsWritten++;
        
        // Reset flags and amounts as needed.
        grossAmountP.value = summary.zeroAmount;
        ftwAmountP.value = summary.zeroAmount;
        sitwAmountP.value = summary.zeroAmount;
        foundAmount = false;
        writeWsDetailRecord = false;
    }



    @Override
    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> statistics = super.getStatistics();
        
        statistics.put(TaxStatType.NUM_TRANSACTION_ROWS, Integer.valueOf(numTransactionRows));
        statistics.put(TaxStatType.NUM_BIO_RECORDS_WRITTEN, Integer.valueOf(numBioRecordsWritten));
        statistics.put(TaxStatType.NUM_DETAIL_RECORDS_WRITTEN, Integer.valueOf(numDetailRecordsWritten));
        statistics.put(TaxStatType.NUM_NO_VENDOR, Integer.valueOf(numNoVendor));
        statistics.put(TaxStatType.NUM_NO_PARENT_VENDOR, Integer.valueOf(numNoParentVendor));
        statistics.put(TaxStatType.NUM_VENDOR_NAMES_PARSED, Integer.valueOf(numVendorNamesParsed));
        statistics.put(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED, Integer.valueOf(numVendorNamesNotParsed));
        statistics.put(TaxStatType.NUM_NO_VENDOR_ADDRESS_US, Integer.valueOf(numNoVendorAddressUS));
        statistics.put(TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN, Integer.valueOf(numNoVendorAddressForeign));
        statistics.put(TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numPdpDocTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED, Integer.valueOf(numPaymentReasonExclusionsDetermined));
        statistics.put(TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numVendorTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numOwnershipTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED, Integer.valueOf(numDocNoteSetsRetrieved));
        statistics.put(TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED, Integer.valueOf(numDocNoteSetsNotRetrieved));
        statistics.put(TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_INCLUSIONS_DETERMINED, Integer.valueOf(numRoyaltyObjDvChkStubInclusionsDetermined));
        statistics.put(TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_EXCLUSIONS_DETERMINED, Integer.valueOf(numRoyaltyObjDvChkStubExclusionsDetermined));
        statistics.put(TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_NEITHER_DETERMINED, Integer.valueOf(numRoyaltyObjDvChkStubNeitherDetermined));
        statistics.put(TaxStatType.NUM_INCOME_CODE_EXCLUDED_GROSS_AMOUNTS, Integer.valueOf(numIncomeCodeExcludedGrossAmounts));
        statistics.put(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_GROSS_AMOUNTS, Integer.valueOf(numIncomeCodeSubTypeExcludedGrossAmounts));
        statistics.put(TaxStatType.NUM_INCOME_CODE_EXCLUDED_FTW_AMOUNTS, Integer.valueOf(numIncomeCodeExcludedFtwAmounts));
        statistics.put(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_FTW_AMOUNTS, Integer.valueOf(numIncomeCodeSubTypeExcludedFtwAmounts));
        statistics.put(TaxStatType.NUM_INCOME_CODE_EXCLUDED_SITW_AMOUNTS, Integer.valueOf(numIncomeCodeExcludedSitwAmounts));
        statistics.put(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_SITW_AMOUNTS, Integer.valueOf(numIncomeCodeSubTypeExcludedSitwAmounts));
        statistics.put(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS, Integer.valueOf(numNoBoxDeterminedRows));
        statistics.put(TaxStatType.NUM_BIO_LINES_WITH_TRUNCATED_US_ADDRESS, Integer.valueOf(numBioLinesWithTruncatedUSAddress));
        statistics.put(TaxStatType.NUM_BIO_LINES_WITH_TRUNCATED_FOREIGN_ADDRESS, Integer.valueOf(numBioLinesWithTruncatedForeignAddress));
        statistics.put(TaxStatType.NUM_BIO_LINES_WITHOUT_US_ADDRESS, Integer.valueOf(numBioLinesWithoutUSAddress));
        statistics.put(TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS, Integer.valueOf(numBioLinesWithoutForeignAddress));
        statistics.put(TaxStatType.NUM_BIO_LINES_WITHOUT_ANY_ADDRESS, Integer.valueOf(numBioLinesWithoutAnyAddress));
        statistics.put(TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_FTW_AMOUNT, Integer.valueOf(numDetailLinesWithPositiveFtwAmount));
        statistics.put(TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_SITW_AMOUNT, Integer.valueOf(numDetailLinesWithPositiveSitwAmount));
        
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

    @Override
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
    private final class RecordPiece1042SString extends IndexedColumnRecordPiece {
        private String value;
        
        private RecordPiece1042SString(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
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
    private final class RecordPiece1042SInt extends IndexedColumnRecordPiece {
        private int value;
        private boolean negateStringValue;
        
        private RecordPiece1042SInt(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            return Integer.toString(negateStringValue ? -value : value);
        }
    }

    /**
     * Represents a BigDecimal value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1042SBigDecimal extends FormattedNumberRecordPiece {
        private BigDecimal value;
        private boolean negateStringValue;
        
        private RecordPiece1042SBigDecimal(String name, int len, int columnIndex, boolean useAmountFormat) {
            super(name, len, columnIndex, useAmountFormat);
        }
        
        @Override
        Object getNumericValue() throws SQLException {
            return negateStringValue ? value.negate() : value;
        }
    }

    /**
     * Represents a java.sql.Date value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1042SDate extends FormattedDateRecordPiece {
        private java.sql.Date value;
        
        private RecordPiece1042SDate(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        Object getDateValue() throws SQLException {
            return value;
        }
    }

    /**
     * Represents a ResultSet-retrieved String value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1042SResultSetDerivedString extends IndexedColumnRecordPiece {
        private final int rsIndex;
        
        private RecordPiece1042SResultSetDerivedString(String name, int len, int columnIndex, int rsIndex) {
            super(name, len, columnIndex);
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
}
