package edu.cornell.kfs.tax.dataaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.LocationService;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.Tax1099FilerAddressField;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DerivedValuesRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorAddressRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorRow;
import edu.cornell.kfs.tax.service.DocumentType1099BoxService;
import edu.cornell.kfs.tax.service.PaymentReason1099BoxService;

/**
 * Default TransactionRowProcessor implementation for handling 1099 tax processing.
 * 
 * <p>This processor expects the following sections to be defined in the output definition XML:</p>
 * 
 * <ol>
 *   <li>Common header row prefix</li>
 *   <li>1099-MISC header row section</li>
 *   <li>1099-NEC header row section</li>
 *   <li>Common Tab Record prefix</li>
 *   <li>1099-MISC Tab Record section</li>
 *   <li>1099-NEC Tab Record section</li>
 * </ol>
 * 
 * <p>In addition, this processor has the following outputs:</p>
 * 
 * <ol>
 *   <li>1099-MISC Tab records file</li>
 *   <li>1099-NEC Tab records file</li>
 * </ol>
 * 
 * <p>The following DERIVED-type fields will be populated by this implementation:</p>
 * 
 * <ul>
 *   <li>vendorEmailAddress</li>
 *   <li>vendorAnyAddressLine1</li>
 *   <li>vendorZipCodeNumOnly</li>
 *   <li>vendorForeignCountryName</li>
 *   <li>vendorForeignCountryIndicator</li>
 *   <li>ssn</li>
 *   <li>tabSiteId</li>
 *   <li>miscRents</li>
 *   <li>miscRoyalties</li>
 *   <li>miscOtherIncome</li>
 *   <li>miscFedIncomeTaxWithheld</li>
 *   <li>miscFishingBoatProceeds</li>
 *   <li>miscMedicalHealthcarePayments</li>
 *   <li>miscDirectSalesInd</li>
 *   <li>miscSubstitutePayments</li>
 *   <li>miscCropInsuranceProceeds</li>
 *   <li>miscGrossProceedsAttorney</li>
 *   <li>miscSection409ADeferral</li>
 *   <li>miscGoldenParachute</li>
 *   <li>miscNonqualifiedDeferredCompensation</li>
 *   <li>miscStateTaxWithheld</li>
 *   <li>miscPayerStateNumber</li>
 *   <li>miscStateIncome</li>
 *   <li>necNonemployeeCompensation</li>
 *   <li>necFedIncomeTaxWithheld</li>
 *   <li>necStateTaxWithheld</li>
 *   <li>necPayerStateNumber</li>
 *   <li>necStateIncome</li>
 *   <li>taxYear</li>
 *   <li>ein</li>
 *   <li>filerName1</li>
 *   <li>filerName2</li>
 *   <li>filerAddress1</li>
 *   <li>filerAddress2</li>
 *   <li>filerCity</li>
 *   <li>filerState</li>
 *   <li>filerZipCode</li>
 *   <li>filerPhoneNumber</li>
 * </ul>
 * 
 * <p>The vendor handling of this processor will also populate the following DETAIL fields:</p>
 * 
 * <ul>
 *   <li>vendorName</li>
 *   <li>parentVendorName</li>
 * </ul>
 * 
 * <p>When running in "scrubbed" mode, the following fields will be forcibly masked in the output:</p>
 * 
 * <ul>
 *   <li>ssn (DERIVED field)</li>
 *   <li>ein (DERIVED field)</li>
 * </ul>
 */
public class TransactionRow1099Processor extends TransactionRowProcessor<Transaction1099Summary> {
	private static final Logger LOG = LogManager.getLogger(TransactionRow1099Processor.class);

    private static final int BOX_PROPERTY_SUBSTRING_MAX_LENGTH = 16;

    private static final int NUM_1099_CHAR_BUFFERS = 6;
    private static final int NUM_1099_WRITERS = 2;

    private static final int SHARED_HEADER_BUFFER_INDEX = 0;
    private static final int MISC_HEADER_BUFFER_INDEX = 1;
    private static final int NEC_HEADER_BUFFER_INDEX = 2;
    private static final int SHARED_TAB_DATA_BUFFER_INDEX = 3;
    private static final int MISC_TAB_RECORD_BUFFER_INDEX = 4;
    private static final int NEC_TAB_RECORD_BUFFER_INDEX = 5;

    private static final int TAX_1099_MISC_WRITER_INDEX = 0;
    private static final int TAX_1099_NEC_WRITER_INDEX = 1;

    private static final int VENDOR_ANY_ADDRESS_INDEX = 2;
    private static final int TAX_1099_EXTRA_RS_SIZE = 3;

    private static final int SMALL_BUILDER_SIZE = 30;
    private static final int MED_BUILDER_SIZE = 100;

    private static final String TAX_BOX_STAT_CONST_PREFIX = "NUM_";
    private static final String TAX_BOX_STAT_CONST_INCLUDE_SUFFIX = "_DV_CHK_STUB_INCLUSIONS_DETERMINED";
    private static final String TAX_BOX_STAT_CONST_EXCLUDE_SUFFIX = "_DV_CHK_STUB_EXCLUSIONS_DETERMINED";
    private static final String TAX_BOX_STAT_CONST_NEITHER_SUFFIX = "_DV_CHK_STUB_NEITHER_DETERMINED";



    // Variables pertaining to the various query results.
    private ResultSet rsVendor;
    private ResultSet rsVendorAnyAddress;
    private ResultSet rsDocNote;
    private ResultSet rsDummy;
    private String tempRetrievedValue;

    // Variables pertaining to the fields retrieved from each transaction detail row.
    private RecordPiece1099String[] detailStrings;
    private RecordPiece1099Int[] detailInts;
    private RecordPiece1099BigDecimal[] detailBigDecimals;
    private RecordPiece1099Date[] detailDates;

    // Variables pertaining to transaction-row-retrieved fields.
    private RecordPiece1099String rowIdP;
    private RecordPiece1099String docNumberP;
    private RecordPiece1099String payeeIdP;
    private RecordPiece1099String docTypeP;
    private RecordPiece1099String docTitleP;
    private RecordPiece1099String objectCodeP;
    private RecordPiece1099String taxIdP;
    private RecordPiece1099String vendorNameP;
    private RecordPiece1099String parentVendorNameP;
    private RecordPiece1099String paymentReasonCodeP;
    private RecordPiece1099String paymentAddressLine1P;
    private RecordPiece1099String dvCheckStubTextP;
    private RecordPiece1099String chartCodeP;
    private RecordPiece1099String accountNumberP;
    private RecordPiece1099String initiatorNetIdP;
    private RecordPiece1099Int docLineNumberP;
    private RecordPiece1099BigDecimal paymentAmountP;
    private RecordPiece1099Date paymentDateP;

    // Variables pertaining to derived fields not related to a tax box.
    private RecordPiece1099String tabSiteIdP;
    private RecordPiece1099String outputTaxIdP;
    private RecordPiece1099String vendorEmailAddressP;
    private RecordPiece1099String vendorAddressLine1P;
    private RecordPiece1099String vendorNumbersOnlyZipCodeP;
    private RecordPiece1099String vendorForeignCountryNameP;
    private RecordPiece1099String vendorForeignCountryIndicatorP;
    private RecordPiece1099String taxYearP;
    private RecordPiece1099String taxEINValueP;
    private RecordPiece1099String filerName1P;
    private RecordPiece1099String filerName2P;
    private RecordPiece1099String filerAddress1P;
    private RecordPiece1099String filerAddress2P;
    private RecordPiece1099String filerCityP;
    private RecordPiece1099String filerStateP;
    private RecordPiece1099String filerZipCodeP;
    private RecordPiece1099String filerPhoneNumberP;

    // Variables pertaining to derived fields related to a tax box.
    private Map<TaxTableField,RecordPiece1099BigDecimal> boxAmountsMap;
    private RecordPiece1099BigDecimal miscRentsP;
    private RecordPiece1099BigDecimal miscRoyaltiesP;
    private RecordPiece1099BigDecimal miscOtherIncomeP;
    private RecordPiece1099BigDecimal miscFedIncomeTaxWithheldP;
    private RecordPiece1099BigDecimal miscFishingBoatProceedsP;
    private RecordPiece1099BigDecimal miscMedicalHealthcarePaymentsP;
    private RecordPiece1099String miscDirectSalesIndP;
    private RecordPiece1099BigDecimal miscSubstitutePaymentsP;
    private RecordPiece1099BigDecimal miscCropInsuranceProceedsP;
    private RecordPiece1099BigDecimal miscGrossProceedsAttorneyP;
    private RecordPiece1099BigDecimal miscSection409ADeferralP;
    private RecordPiece1099BigDecimal miscGoldenParachuteP;
    private RecordPiece1099BigDecimal miscNonqualifiedDeferredCompensationP;
    private RecordPiece1099BigDecimal miscStateTaxWithheldP;
    private RecordPiece1099String miscPayerStateNumberP;
    private RecordPiece1099BigDecimal miscStateIncomeP;
    private RecordPiece1099BigDecimal necNonemployeeCompensationP;
    private RecordPiece1099BigDecimal necFedIncomeTaxWithheldP;
    private RecordPiece1099BigDecimal necStateTaxWithheldP;
    private RecordPiece1099String necPayerStateNumberP;
    private RecordPiece1099BigDecimal necStateIncomeP;

    // Variables pertaining to row-specific values and flags.
    private String rowKey;
    private String chartAndAccountCombo;
    private TaxTableField taxBox;
    private TaxTableField overrideTaxBox;
    private TaxTableField overriddenTaxBox;
    private RecordPiece1099BigDecimal taxBoxPiece;
    private Boolean dvCheckStubInclusionInd;
    private Boolean royaltiesObjChartAccountInclusionInd;
    private Boolean nonEmployeeCompVendorNameInclusionInd;
    private Boolean nonEmployeeCompParentVendorNameInclusionInd;
    private Boolean nonEmployeeCompDocTitleInclusionInd;
    private boolean isDVRow;
    
    // Variables pertaining to various flags.
    private boolean writeTabRecord;
    private boolean foundParentVendor;
    private boolean foundAmount;
    private boolean foundExclusion;
    private boolean excludeTransaction;
    private boolean isParm1099Inclusion;
    private boolean isParm1099Exclusion;

    // Variables pertaining to decryption of tax IDs.
    private EncryptionService encryptionService;
    private String unencryptedTaxId;

    // Variables pertaining to values that need to be retrieved from the next detail row before populating the detail "piece" objects.
    private String nextTaxId;
    private String nextPayeeId;

    // Variables pertaining to vendor-specific data.
    private VendorRow vendorRow;
    private VendorAddressRow vendorAddressRow;
    private String vendorNameForOutput;
    private String parentVendorNameForOutput;
    private String vendorLastName;
    private String vendorFirstName;
    private int vendorHeaderId;
    private int vendorDetailId;
    private int currentVendorDetailId;

    // Variables pertaining to document note data.
    private TaxTableField docNoteTextField;

    // Variables pertaining to various statistics.
    private Map<Integer,String> boxNumberMappingsWithStatistics;
    private int numTransactionRows;
    private int numMiscTabRecordsWritten;
    private int numNecTabRecordsWritten;
    private int numVendorNamesParsed;
    private int numVendorNamesNotParsed;
    private int numNoVendor;
    private int numNoParentVendor;
    private int numNoVendorAddress;
    private int numVendorTypeExclusionsDetermined;
    private int numOwnershipTypeExclusionsDetermined;
    private int numPdpDocTypeExclusionsDetermined;
    private int numPaymentReasonExclusionsDetermined;
    private int numDocNoteSetsRetrieved;
    private int numDocNoteSetsNotRetrieved;
    private int numNoBoxDeterminedRows;

    // Variables pertaining to tax-source-specific statistics.
    private OriginSpecificStats dvStats;
    private OriginSpecificStats pdpStats;
    private OriginSpecificStats currentStats;

	private PaymentReason1099BoxService paymentReason1099BoxService;
    private DocumentType1099BoxService documentType1099BoxService;
    private LocationService locationService;

    TransactionRow1099Processor() {
        super(TAX_1099_EXTRA_RS_SIZE, TAX_1099_EXTRA_RS_SIZE, NUM_1099_CHAR_BUFFERS, NUM_1099_WRITERS);
    }



    @Override
    RecordPiece getPieceForField(TaxFieldSource fieldSource, TaxTableField field, String name, int len, Transaction1099Summary summary) {
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
                // Create a piece that derives its value directly from the vendor ResultSet at runtime.
                piece = new RecordPiece1099ResultSetDerivedString(name, len,
                        field.index, CUTaxConstants.VENDOR_DETAIL_INDEX);
                break;
        
            case VENDOR_US_ADDRESS :
                // Create a piece that derives its value directly from the vendor US address ResultSet at runtime.
                throw new IllegalArgumentException("The VENDOR_US_ADDRESS type is not supported for 1099 processing");
            
            case VENDOR_ANY_ADDRESS :
                // Create a piece that derives its value directly from the vendor address ResultSet at runtime.
                piece = new RecordPiece1099ResultSetDerivedString(name, len,
                        field.index, VENDOR_ANY_ADDRESS_INDEX);
                break;
            
            case DERIVED :
                // Create a piece whose value will be set by the 1099 processing.
                piece = getPieceForFieldInternal(field, name, len, summary);
                break;
            
            default :
                throw new IllegalArgumentException("Unrecognized piece type");
        }
        
        return piece;
    }



    /*
     * Internal helper method for building "piece" objects whose values are either
     * derived from the transaction detail rows or are set by the 1099 processing.
     */
    private RecordPiece getPieceForFieldInternal(TaxTableField field, String name, int len, Transaction1099Summary summary) {
        RecordPiece piece;
        
        switch (field.jdbcType) {
            case java.sql.Types.DECIMAL :
                piece = new RecordPiece1099BigDecimal(name, len, field.index,
                        !summary.transactionDetailRow.federalIncomeTaxPercent.equals(field) && !summary.derivedValues.chapter3TaxRate.equals(field),
                        field);
                break;
            
            case java.sql.Types.INTEGER :
                piece = new RecordPiece1099Int(name, len, field.index);
                break;
            
            case java.sql.Types.VARCHAR :
                piece = new RecordPiece1099String(name, len, field.index);
                break;
            
            case java.sql.Types.DATE :
                piece = new RecordPiece1099Date(name, len, field.index);
                break;
            
            default :
                throw new IllegalStateException("This processor does not support data fields of the given JDBC type: " + Integer.valueOf(field.jdbcType));
        }
        
        return piece;
    }



    @Override
    Set<TaxTableField> getMinimumFields(TaxFieldSource fieldSource, Transaction1099Summary summary) {
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
                        detailRow.documentTitle,
                        detailRow.financialDocumentLineNumber,
                        detailRow.finObjectCode,
                        detailRow.netPaymentAmount,
                        detailRow.vendorTaxNumber,
                        detailRow.dvCheckStubText,
                        detailRow.payeeId,
                        detailRow.vendorName,
                        detailRow.parentVendorName,
                        detailRow.paymentDate,
                        detailRow.paymentLine1Address,
                        detailRow.chartCode,
                        detailRow.accountNumber,
                        detailRow.initiatorNetId,
                        detailRow.paymentReasonCode
                ));
                break;
            
            case PDP :
                // Leave Set empty.
                break;
            
            case DV :
                // Leave Set empty.
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
                        derivedValues.vendorEmailAddress,
                        derivedValues.vendorAnyAddressLine1,
                        derivedValues.vendorZipCodeNumOnly,
                        derivedValues.vendorForeignCountryName,
                        derivedValues.vendorForeignCountryIndicator,
                        derivedValues.ssn,
                        derivedValues.tabSiteId,
                        derivedValues.miscRents,
                        derivedValues.miscRoyalties,
                        derivedValues.miscOtherIncome,
                        derivedValues.miscFedIncomeTaxWithheld,
                        derivedValues.miscFishingBoatProceeds,
                        derivedValues.miscMedicalHealthcarePayments,
                        derivedValues.miscDirectSalesInd,
                        derivedValues.miscSubstitutePayments,
                        derivedValues.miscCropInsuranceProceeds,
                        derivedValues.miscGrossProceedsAttorney,
                        derivedValues.miscSection409ADeferral,
                        derivedValues.miscGoldenParachute,
                        derivedValues.miscNonqualifiedDeferredCompensation,
                        derivedValues.miscStateTaxWithheld,
                        derivedValues.miscPayerStateNumber,
                        derivedValues.miscStateIncome,
                        derivedValues.necNonemployeeCompensation,
                        derivedValues.necFedIncomeTaxWithheld,
                        derivedValues.necStateTaxWithheld,
                        derivedValues.necPayerStateNumber,
                        derivedValues.necStateIncome,
                        derivedValues.taxYear,
                        derivedValues.ein,
                        derivedValues.filerName1,
                        derivedValues.filerName2,
                        derivedValues.filerAddress1,
                        derivedValues.filerAddress2,
                        derivedValues.filerCity,
                        derivedValues.filerState,
                        derivedValues.filerZipCode,
                        derivedValues.filerPhoneNumber
                ));
                break;
            
            default :
                throw new IllegalArgumentException("Invalid piece type");
        }
        
        return minFields;
    }



    @Override
    void setComplexPieces(Map<String,RecordPiece> complexPieces, Transaction1099Summary summary) {
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        DerivedValuesRow derivedValues = summary.derivedValues;
        List<RecordPiece1099String> stringPieces = new ArrayList<RecordPiece1099String>();
        List<RecordPiece1099Int> intPieces = new ArrayList<RecordPiece1099Int>();
        List<RecordPiece1099BigDecimal> bigDecimalPieces = new ArrayList<RecordPiece1099BigDecimal>();
        List<RecordPiece1099Date> datePieces = new ArrayList<RecordPiece1099Date>();
        
        Function<TaxTableField, RecordPiece1099String> stringPieceFinder =
                taxField -> (RecordPiece1099String) complexPieces.get(taxField.propertyName);
        Function<TaxTableField, RecordPiece1099Int> intPieceFinder =
                taxField -> (RecordPiece1099Int) complexPieces.get(taxField.propertyName);
        Function<TaxTableField, RecordPiece1099BigDecimal> bigDecimalPieceFinder =
                taxField -> (RecordPiece1099BigDecimal) complexPieces.get(taxField.propertyName);
        Function<TaxTableField, RecordPiece1099Date> datePieceFinder =
                taxField -> (RecordPiece1099Date) complexPieces.get(taxField.propertyName);
        
        // retrieve the "piece" objects corresponding to all the given transaction detail fields.
        for (TaxTableField detailField : summary.transactionDetailRow.orderedFields) {
            RecordPiece detailPiece = complexPieces.get(detailField.propertyName);
            if (detailPiece != null) {
                switch (detailField.jdbcType) {
                    case java.sql.Types.DECIMAL :
                        bigDecimalPieces.add((RecordPiece1099BigDecimal) detailPiece);
                        break;
                    
                    case java.sql.Types.INTEGER :
                        intPieces.add((RecordPiece1099Int) detailPiece);
                        break;
                    
                    case java.sql.Types.VARCHAR :
                        stringPieces.add((RecordPiece1099String) detailPiece);
                        break;
                    
                    case java.sql.Types.DATE :
                        datePieces.add((RecordPiece1099Date) detailPiece);
                        break;
                    
                    default :
                        throw new IllegalStateException("Found unsupported detail field JDBC type: " + Integer.toString(detailField.jdbcType));
                }
            }
        }
        
        // Setup the transaction detail "piece" arrays.
        detailStrings = stringPieces.toArray(new RecordPiece1099String[stringPieces.size()]);
        detailInts = intPieces.toArray(new RecordPiece1099Int[intPieces.size()]);
        detailBigDecimals = bigDecimalPieces.toArray(new RecordPiece1099BigDecimal[bigDecimalPieces.size()]);
        detailDates = datePieces.toArray(new RecordPiece1099Date[datePieces.size()]);
        
        // Retrieve the various detail "pieces" that will be needed for the processing.
        rowIdP = stringPieceFinder.apply(detailRow.transactionDetailId);
        docNumberP = stringPieceFinder.apply(detailRow.documentNumber);
        docTypeP = stringPieceFinder.apply(detailRow.documentType);
        docTitleP = stringPieceFinder.apply(detailRow.documentTitle);
        docLineNumberP = intPieceFinder.apply(detailRow.financialDocumentLineNumber);
        objectCodeP = stringPieceFinder.apply(detailRow.finObjectCode);
        paymentAmountP = bigDecimalPieceFinder.apply(detailRow.netPaymentAmount);
        taxIdP = stringPieceFinder.apply(detailRow.vendorTaxNumber);
        dvCheckStubTextP = stringPieceFinder.apply(detailRow.dvCheckStubText);
        payeeIdP = stringPieceFinder.apply(detailRow.payeeId);
        vendorNameP = stringPieceFinder.apply(detailRow.vendorName);
        parentVendorNameP = stringPieceFinder.apply(detailRow.parentVendorName);
        paymentDateP = datePieceFinder.apply(detailRow.paymentDate);
        paymentAddressLine1P = stringPieceFinder.apply(detailRow.paymentLine1Address);
        chartCodeP = stringPieceFinder.apply(detailRow.chartCode);
        accountNumberP = stringPieceFinder.apply(detailRow.accountNumber);
        initiatorNetIdP = stringPieceFinder.apply(detailRow.initiatorNetId);
        paymentReasonCodeP = stringPieceFinder.apply(detailRow.paymentReasonCode);
        
        // Retrieve the various derived "pieces" that will be needed for the processing.
        vendorEmailAddressP = stringPieceFinder.apply(derivedValues.vendorEmailAddress);
        vendorAddressLine1P = stringPieceFinder.apply(derivedValues.vendorAnyAddressLine1);
        vendorNumbersOnlyZipCodeP = stringPieceFinder.apply(derivedValues.vendorZipCodeNumOnly);
        vendorForeignCountryNameP = stringPieceFinder.apply(derivedValues.vendorForeignCountryName);
        vendorForeignCountryIndicatorP = stringPieceFinder.apply(derivedValues.vendorForeignCountryIndicator);
        outputTaxIdP = stringPieceFinder.apply(derivedValues.ssn);
        tabSiteIdP = stringPieceFinder.apply(derivedValues.tabSiteId);
        taxYearP = stringPieceFinder.apply(derivedValues.taxYear);
        taxEINValueP = stringPieceFinder.apply(derivedValues.ein);
        filerName1P = stringPieceFinder.apply(derivedValues.filerName1);
        filerName2P = stringPieceFinder.apply(derivedValues.filerName2);
        filerAddress1P = stringPieceFinder.apply(derivedValues.filerAddress1);
        filerAddress2P = stringPieceFinder.apply(derivedValues.filerAddress2);
        filerCityP = stringPieceFinder.apply(derivedValues.filerCity);
        filerStateP = stringPieceFinder.apply(derivedValues.filerState);
        filerZipCodeP = stringPieceFinder.apply(derivedValues.filerZipCode);
        filerPhoneNumberP = stringPieceFinder.apply(derivedValues.filerPhoneNumber);
        miscRentsP = bigDecimalPieceFinder.apply(derivedValues.miscRents);
        miscRoyaltiesP = bigDecimalPieceFinder.apply(derivedValues.miscRoyalties);
        miscOtherIncomeP = bigDecimalPieceFinder.apply(derivedValues.miscOtherIncome);
        miscFedIncomeTaxWithheldP = bigDecimalPieceFinder.apply(derivedValues.miscFedIncomeTaxWithheld);
        miscFishingBoatProceedsP = bigDecimalPieceFinder.apply(derivedValues.miscFishingBoatProceeds);
        miscMedicalHealthcarePaymentsP = bigDecimalPieceFinder.apply(derivedValues.miscMedicalHealthcarePayments);
        miscDirectSalesIndP = stringPieceFinder.apply(derivedValues.miscDirectSalesInd);
        miscSubstitutePaymentsP = bigDecimalPieceFinder.apply(derivedValues.miscSubstitutePayments);
        miscCropInsuranceProceedsP = bigDecimalPieceFinder.apply(derivedValues.miscCropInsuranceProceeds);
        miscGrossProceedsAttorneyP = bigDecimalPieceFinder.apply(derivedValues.miscGrossProceedsAttorney);
        miscSection409ADeferralP = bigDecimalPieceFinder.apply(derivedValues.miscSection409ADeferral);
        miscGoldenParachuteP = bigDecimalPieceFinder.apply(derivedValues.miscGoldenParachute);
        miscNonqualifiedDeferredCompensationP = bigDecimalPieceFinder.apply(
                derivedValues.miscNonqualifiedDeferredCompensation);
        miscStateTaxWithheldP = bigDecimalPieceFinder.apply(derivedValues.miscStateTaxWithheld);
        miscPayerStateNumberP = stringPieceFinder.apply(derivedValues.miscPayerStateNumber);
        miscStateIncomeP = bigDecimalPieceFinder.apply(derivedValues.miscStateIncome);
        necNonemployeeCompensationP = bigDecimalPieceFinder.apply(derivedValues.necNonemployeeCompensation);
        necFedIncomeTaxWithheldP = bigDecimalPieceFinder.apply(derivedValues.necFedIncomeTaxWithheld);
        necStateTaxWithheldP = bigDecimalPieceFinder.apply(derivedValues.necStateTaxWithheld);
        necPayerStateNumberP = stringPieceFinder.apply(derivedValues.necPayerStateNumber);
        necStateIncomeP = bigDecimalPieceFinder.apply(derivedValues.necStateIncome);
        
        // Populate tax box field map.
        boxAmountsMap = new HashMap<TaxTableField,RecordPiece1099BigDecimal>();
        boxAmountsMap.put(miscRentsP.tableField, miscRentsP);
        boxAmountsMap.put(miscRoyaltiesP.tableField, miscRoyaltiesP);
        boxAmountsMap.put(miscOtherIncomeP.tableField, miscOtherIncomeP);
        boxAmountsMap.put(miscFedIncomeTaxWithheldP.tableField, miscFedIncomeTaxWithheldP);
        boxAmountsMap.put(miscFishingBoatProceedsP.tableField, miscFishingBoatProceedsP);
        boxAmountsMap.put(miscMedicalHealthcarePaymentsP.tableField, miscMedicalHealthcarePaymentsP);
        boxAmountsMap.put(miscSubstitutePaymentsP.tableField, miscSubstitutePaymentsP);
        boxAmountsMap.put(miscCropInsuranceProceedsP.tableField, miscCropInsuranceProceedsP);
        boxAmountsMap.put(miscGrossProceedsAttorneyP.tableField, miscGrossProceedsAttorneyP);
        boxAmountsMap.put(miscSection409ADeferralP.tableField, miscSection409ADeferralP);
        boxAmountsMap.put(miscGoldenParachuteP.tableField, miscGoldenParachuteP);
        boxAmountsMap.put(miscNonqualifiedDeferredCompensationP.tableField, miscNonqualifiedDeferredCompensationP);
        boxAmountsMap.put(miscStateTaxWithheldP.tableField, miscStateTaxWithheldP);
        boxAmountsMap.put(miscStateIncomeP.tableField, miscStateIncomeP);
        boxAmountsMap.put(necNonemployeeCompensationP.tableField, necNonemployeeCompensationP);
        boxAmountsMap.put(necFedIncomeTaxWithheldP.tableField, necFedIncomeTaxWithheldP);
        boxAmountsMap.put(necStateTaxWithheldP.tableField, necStateTaxWithheldP);
        boxAmountsMap.put(necStateIncomeP.tableField, necStateIncomeP);
    }



    @Override
    String[] getFilePathsForWriters(Transaction1099Summary summary, LocalDateTime processingStartDate) {
        String[] filePaths = super.getFilePathsForWriters(summary, processingStartDate);
        // Output file for 1099 tab records.
        filePaths[0] = new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/')
                .append(CUTaxConstants.TAX_1099_MISC_OUTPUT_FILE_PREFIX).append(summary.reportYear)
                .append(buildDateFormatForFileSuffixes().format(processingStartDate)).append(CUTaxConstants.TAX_OUTPUT_FILE_SUFFIX).toString();
        filePaths[1] = new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/')
                .append(CUTaxConstants.TAX_1099_NEC_OUTPUT_FILE_PREFIX).append(summary.reportYear)
                .append(buildDateFormatForFileSuffixes().format(processingStartDate)).append(CUTaxConstants.TAX_OUTPUT_FILE_SUFFIX).toString();
        return filePaths;
    }

    @Override
    String[] getSqlForExtraStatements(Transaction1099Summary summary) {
        String[] extraSql = super.getSqlForExtraStatements(summary);
        
        extraSql[VENDOR_ANY_ADDRESS_INDEX] = TaxSqlUtils.getVendorAddressSelectSql(summary.vendorAddressRow, null);
        
        return extraSql;
    }



    @Override
    String getSqlForSelect(Transaction1099Summary summary) {
        return TaxSqlUtils.getTransactionDetailSelectSql(summary.transactionDetailRow.form1099Box, summary.transactionDetailRow, true, true);
    }

    @Override
    Object[][] getParameterValuesForSelect(Transaction1099Summary summary) {
        return new Object[][] {
            {Integer.valueOf(summary.reportYear)},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }



    @Override
    void processTaxRows(ResultSet rs, Transaction1099Summary summary) throws SQLException, IOException {
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        boolean keepLooping = true;
        boolean taxIdChanged = false;
        
        encryptionService = CoreApiServiceLocator.getEncryptionService();
        dvStats = new OriginSpecificStats(summary.derivedValues.orderedFields.size());
        pdpStats = new OriginSpecificStats(summary.derivedValues.orderedFields.size());
        boxNumberMappingsWithStatistics = getTaxBoxNumberMappingsWithStatistics(summary);
        
        // Setup defaults.
        resetTaxBoxes(summary);
        foundAmount = false;
        writeTabRecord = false;
        
        // Setup values that are not expected to change between each iteration.
        tabSiteIdP.value = summary.tabSiteId;
        taxYearP.value = String.valueOf(summary.reportYear);
        taxEINValueP.value = summary.scrubbedOutput ? CUTaxConstants.MASKED_VALUE_9_CHARS : summary.taxEIN;
        filerName1P.value = summary.filerAddressFields.get(Tax1099FilerAddressField.NAME1);
        filerName2P.value = summary.filerAddressFields.get(Tax1099FilerAddressField.NAME2);
        filerAddress1P.value = summary.filerAddressFields.get(Tax1099FilerAddressField.ADDRESS1);
        filerAddress2P.value = summary.filerAddressFields.get(Tax1099FilerAddressField.ADDRESS2);
        filerCityP.value = summary.filerAddressFields.get(Tax1099FilerAddressField.CITY);
        filerStateP.value = summary.filerAddressFields.get(Tax1099FilerAddressField.STATE);
        filerZipCodeP.value = summary.filerAddressFields.get(Tax1099FilerAddressField.ZIP_CODE);
        filerPhoneNumberP.value = summary.filerAddressFields.get(Tax1099FilerAddressField.PHONE_NUMBER);
        rsDummy = new DummyResultSet();
        vendorRow = summary.vendorRow;
        vendorAddressRow = summary.vendorAddressRow;
        docNoteTextField = summary.documentNoteRow.noteText;
        if (summary.scrubbedOutput) {
            outputTaxIdP.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }
        
        // Print headers.
        resetBuffer(SHARED_HEADER_BUFFER_INDEX);
        resetBuffer(MISC_HEADER_BUFFER_INDEX);
        resetBuffer(NEC_HEADER_BUFFER_INDEX);
        appendPieces(SHARED_HEADER_BUFFER_INDEX);
        appendBuffer(SHARED_HEADER_BUFFER_INDEX, MISC_HEADER_BUFFER_INDEX);
        appendBuffer(SHARED_HEADER_BUFFER_INDEX, NEC_HEADER_BUFFER_INDEX);
        appendPieces(MISC_HEADER_BUFFER_INDEX);
        appendPieces(NEC_HEADER_BUFFER_INDEX);
        writeBufferToOutput(MISC_HEADER_BUFFER_INDEX, TAX_1099_MISC_WRITER_INDEX);
        writeBufferToOutput(NEC_HEADER_BUFFER_INDEX, TAX_1099_NEC_WRITER_INDEX);
        
        // Perform initial processing for first row, if there is one.
        if (rs.next()) {
            // If at least one row exists, then update counters and retrieve field values as needed.
            numTransactionRows++;
            nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
            nextPayeeId = rs.getString(detailRow.payeeId.index);
            vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
            vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
            taxIdChanged = true;
            if (StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for initial row with payee " + nextPayeeId);
            }
            LOG.info("Starting transaction row processing for 1099 tax reporting...");
        } else {
            // Skip processing if no detail rows were found.
            keepLooping = false;
            LOG.info("No transaction rows found for 1099 tax reporting, skipping processing...");
        }
        
        
        
        // Iterate over the transaction detail rows.
        while (keepLooping) {
            // Populate "piece" objects from the current transaction row.
            loadTransactionRowValuesFromResults(rs);
            
            // Set payment amount to zero if null.
            if (paymentAmountP.value == null) {
                paymentAmountP.value = summary.zeroAmount;
            }
            
            // Setup row "key" string for logging purposes.
            rowKey = new StringBuilder(MED_BUILDER_SIZE)
                    .append(rowIdP.value)
                    .append(' ').append(docNumberP.value)
                    .append(' ').append(docLineNumberP.value)
                    .append(' ').append(payeeIdP.value)
                    .append(' ').append(docTypeP.value)
                    .append(' ').append(objectCodeP.value)
                    .append(' ').append(paymentAmountP.value)
                    .append(' ').append(paymentDateP.value)
                    .toString();
            
            // Setup other values as needed.
            foundExclusion = false;
            excludeTransaction = false;
            dvCheckStubInclusionInd = null;
            royaltiesObjChartAccountInclusionInd = null;
            nonEmployeeCompVendorNameInclusionInd = null;
            nonEmployeeCompParentVendorNameInclusionInd = null;
            nonEmployeeCompDocTitleInclusionInd = null;
            isDVRow = DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(docTypeP.value);
            currentStats = isDVRow ? dvStats : pdpStats;
            chartAndAccountCombo = new StringBuilder().append(chartCodeP.value).append('-').append(accountNumberP.value).toString();
            taxBox = null;
            
            // Load data for vendor, if first row or if moving to the next tax ID.
            if (taxIdChanged) {
                resetTaxBoxes(summary);
                foundAmount = false;
                writeTabRecord = false;
                loadVendorData(rs, summary);
            }
            
            // Determine which tax box applies, and check for exclusions.
            if (!summary.excludedPayeeIds.contains(nextPayeeId)) {
                if (!summary.zeroAmount.equals(paymentAmountP.value)) {
                    checkForGeneralExclusionsAndFindTaxBox(rs, summary);
                }
            } else {
                excludeTransaction = true;
            }
            
            // Determine final inclusion/exclusion state, and update tax boxes accordingly.
            processExclusionsAndAmounts(rs, detailRow, summary);
            
            // Update current row's vendor-related data.
            rs.updateString(detailRow.vendorName.index, vendorNameForOutput);
            rs.updateString(detailRow.parentVendorName.index, parentVendorNameForOutput);
            rs.updateString(detailRow.vendorEmailAddress.index, vendorEmailAddressP.value);
            rs.updateString(detailRow.vendorChapter4StatusCode.index, rsVendor.getString(vendorRow.vendorChapter4StatusCode.index));
            rs.updateString(detailRow.vendorGIIN.index, rsVendor.getString(vendorRow.vendorGIIN.index));
            rs.updateString(detailRow.vendorLine1Address.index, vendorAddressLine1P.value);
            rs.updateString(detailRow.vendorLine2Address.index, rsVendorAnyAddress.getString(vendorAddressRow.vendorLine2Address.index));
            rs.updateString(detailRow.vendorCityName.index, rsVendorAnyAddress.getString(vendorAddressRow.vendorCityName.index));
            rs.updateString(detailRow.vendorStateCode.index, rsVendorAnyAddress.getString(vendorAddressRow.vendorStateCode.index));
            rs.updateString(detailRow.vendorZipCode.index, rsVendorAnyAddress.getString(vendorAddressRow.vendorZipCode.index));
            
            // Store any changes made to the current transaction detail row.
            rs.updateRow();
            
            
            
            // Move to next row (if any) and update the looping flag as needed.
            if (rs.next()) {
                // If more rows are available, then update counters and retrieve field values as needed.
                numTransactionRows++;
                nextTaxId = rs.getString(detailRow.vendorTaxNumber.index);
                nextPayeeId = rs.getString(detailRow.payeeId.index);
                vendorHeaderId = Integer.parseInt(nextPayeeId.substring(0, nextPayeeId.indexOf('-')));
                vendorDetailId = Integer.parseInt(nextPayeeId.substring(nextPayeeId.indexOf('-') + 1));
                // Check for changes to the tax ID between rows. The prior tax ID should be non-null at this point.
                taxIdChanged = StringUtils.isBlank(nextTaxId) || !taxIdP.value.equals(nextTaxId);
            } else {
                // If no more rows, then prepare to exit the loop and process any leftover data from the previous iterations.
                keepLooping = false;
                writeTabRecord = true;
            }
            
            // Automatically abort with an error if no tax ID (even an auto-generated one) could be found.
            if (StringUtils.isBlank(nextTaxId)) {
                throw new RuntimeException("Could not find tax ID for row with payee " + nextPayeeId);
            }
            
            
            
            // If necessary, check if a new tab record needs to be written.
            if (!writeTabRecord && taxIdChanged) {
                // Potentially write new tab record in response to a tax ID change.
                writeTabRecord = true;
            }
            
            // Do not write a tab record if no tax box amounts have been identified yet for the current tax ID.
            if (!foundAmount) {
                writeTabRecord = false;
            }
            
            // If needed, write a new tab record to the output file.
            if (writeTabRecord) {
                writeTabLineToFile(summary);
            }
            
            // END OF LOOP
        }
        
        LOG.info("Finished transaction row processing for 1099 tax reporting.");
    }



    private void resetTaxBoxes(Transaction1099Summary summary) {
        miscRentsP.value = summary.zeroAmount;
        miscRoyaltiesP.value = summary.zeroAmount;
        miscOtherIncomeP.value = summary.zeroAmount;
        miscFedIncomeTaxWithheldP.value = summary.zeroAmount;
        miscFishingBoatProceedsP.value = summary.zeroAmount;
        miscMedicalHealthcarePaymentsP.value = summary.zeroAmount;
        miscDirectSalesIndP.value = KFSConstants.EMPTY_STRING;
        miscSubstitutePaymentsP.value = summary.zeroAmount;
        miscCropInsuranceProceedsP.value = summary.zeroAmount;
        miscGrossProceedsAttorneyP.value = summary.zeroAmount;
        miscSection409ADeferralP.value = summary.zeroAmount;
        miscGoldenParachuteP.value = summary.zeroAmount;
        miscNonqualifiedDeferredCompensationP.value = summary.zeroAmount;
        miscStateTaxWithheldP.value = summary.zeroAmount;
        miscPayerStateNumberP.value = KFSConstants.EMPTY_STRING;
        miscStateIncomeP.value = summary.zeroAmount;
        necNonemployeeCompensationP.value = summary.zeroAmount;
        necFedIncomeTaxWithheldP.value = summary.zeroAmount;
        necStateTaxWithheldP.value = summary.zeroAmount;
        necPayerStateNumberP.value = KFSConstants.EMPTY_STRING;
        necStateIncomeP.value = summary.zeroAmount;
    }



    /*
     * Helper method for loading the relevant transaction detail fields into the associated "piece" objects.
     */
    private void loadTransactionRowValuesFromResults(ResultSet rs) throws SQLException {
        // Get String values.
        for (RecordPiece1099String detailString : detailStrings) {
            detailString.value = rs.getString(detailString.columnIndex);
        }
        // Get int values.
        for (RecordPiece1099Int detailInt : detailInts) {
            detailInt.value = rs.getInt(detailInt.columnIndex);
        }
        // Get BigDecimal values.
        for (RecordPiece1099BigDecimal detailBigDecimal : detailBigDecimals) {
            detailBigDecimal.value = rs.getBigDecimal(detailBigDecimal.columnIndex);
        }
        // Get java.sql.Date values.
        for (RecordPiece1099Date detailDate : detailDates) {
            detailDate.value = rs.getDate(detailDate.columnIndex);
        }
    }



    /*
     * Helper method for loading vendor info for the associated transaction detail row.
     */
    private void loadVendorData(ResultSet rs, Transaction1099Summary summary) throws SQLException {
        foundParentVendor = false;
        vendorLastName = null;
        vendorFirstName = null;
        vendorNameForOutput = null;
        parentVendorNameForOutput = null;
        
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
                        vendorLastName = vendorNameForOutput.substring(0, vendorNameForOutput.indexOf(',')).trim();
                        vendorFirstName = vendorNameForOutput.substring(vendorNameForOutput.indexOf(',') + 1).trim();
                        vendorNameForOutput = vendorFirstName + KRADConstants.BLANK_SPACE + vendorLastName;
                    } else {
                        vendorLastName = vendorNameForOutput.trim();
                    }
                    numVendorNamesParsed++;
                } else {
                    vendorNameForOutput = new StringBuilder(SMALL_BUILDER_SIZE)
                            .append("No Vendor Name(").append(vendorHeaderId).append(')').append('!').toString();
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
        
        
        
        // Load vendor address.
        rsVendorAnyAddress = configureAndRunQuery(VENDOR_ANY_ADDRESS_INDEX, VENDOR_ANY_ADDRESS_INDEX,
                vendorHeaderId, currentVendorDetailId);
        
        if (rsVendorAnyAddress.next()) {
            String vendorCountryCode = rsVendorAnyAddress.getString(vendorAddressRow.vendorCountryCode.index);
            vendorAddressLine1P.value = rsVendorAnyAddress.getString(vendorAddressRow.vendorLine1Address.index);
            if (isForeignCountry(vendorCountryCode)) {
                vendorForeignCountryNameP.value = getCountryName(vendorCountryCode);
                vendorForeignCountryIndicatorP.value = CUKFSConstants.CAPITAL_X;
            } else {
                vendorForeignCountryNameP.value = null;
                vendorForeignCountryIndicatorP.value = null;
            }
        } else {
            vendorAddressLine1P.value = CUTaxConstants.NO_ANY_VENDOR_ADDRESS;
            vendorForeignCountryNameP.value = null;
            vendorForeignCountryIndicatorP.value = null;
            numNoVendorAddress++;
            /*
             * Update the ResultSet to the "dummy" one. The superclass will still
             * handle the reference to the "real" ResultSet, so we don't need to
             * explicitly close it here.
             */
            rsVendorAnyAddress = rsDummy;
        }
        
        // Setup email address.
        vendorEmailAddressP.value = rsVendorAnyAddress.getString(vendorAddressRow.vendorAddressEmailAddress.index);
        
        
        // Decrypt tax ID for usage later.
        try {
            unencryptedTaxId = encryptionService.decrypt(taxIdP.value);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isForeignCountry(String countryCode) {
        return StringUtils.isNotBlank(countryCode) && !StringUtils.equals(
                KFSConstants.COUNTRY_CODE_UNITED_STATES, countryCode);
    }

    private String getCountryName(String countryCode) {
        Country country = getLocationService().getCountry(countryCode);
        if (ObjectUtils.isNotNull(country) && StringUtils.isNotBlank(country.getName())) {
            return country.getName();
        } else {
            return CUTaxConstants.UNKNOWN_COUNTRY;
        }
    }

    /*
     * Helper method for determining whether any explicit inclusions/exclusions apply to the current transaction detail row, and for determining box type.
     */
    private void checkForGeneralExclusionsAndFindTaxBox(ResultSet rs, Transaction1099Summary summary) throws SQLException {
        String tempValue;
        Set<String> tempSet;
        boolean foundMatch;
        int idx;
        
        // Find tax box.
        if (getDocumentType1099BoxService().isDocumentTypeMappedTo1099Box(docTypeP.value)) {
            String docType1099Box = getDocumentType1099BoxService().getDocumentType1099Box(docTypeP.value);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found explicit doc-type-based mapping to tax box " + docType1099Box + " for row with key: " + rowKey);
            }
            taxBox = summary.getBoxNumberConstant(docType1099Box);
        } else if (getPaymentReason1099BoxService().isPaymentReasonMappedTo1099Box(paymentReasonCodeP.value)) {
        	String mappedBox = getPaymentReason1099BoxService().getPaymentReason1099Box(paymentReasonCodeP.value);
			LOG.debug("Overriding to tax box " + mappedBox + "  because of payment reason " + paymentReasonCodeP.value + " for vendor " + vendorNameForOutput);
			taxBox = summary.getBoxNumberConstant(mappedBox);
        } else if (getPaymentReason1099BoxService().isPaymentReasonMappedToNo1099Box(paymentReasonCodeP.value)) {
        	LOG.debug("Overriding to NO tax box because of payment reason " + paymentReasonCodeP.value + " for vendor " + vendorNameForOutput);
        	taxBox = null;
        } else {
	        taxBox = summary.objectCodeBucketMappings.get(new StringBuilder().append(objectCodeP.value)
	                .append(';').append((isDVRow && StringUtils.isNotBlank(paymentReasonCodeP.value))
	                        ? paymentReasonCodeP.value : CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON).toString());
	        if (taxBox == null && isDVRow && StringUtils.isNotBlank(paymentReasonCodeP.value)) {
	            taxBox = summary.objectCodeBucketMappings.get(new StringBuilder().append(objectCodeP.value)
	                    .append(';').append(CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON).toString());
	        }
        }
        
        if (summary.derivedValues.boxUnknown1099.equals(taxBox)) {
            excludeTransaction = true;
        }
        
        // Check for vendor type or vendor ownership type exclusions.
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
        
        // Check for PDP doc type exclusions.
        if (taxBox != null && !excludeTransaction) {
            if (summary.pdpExcludedDocTypes.contains(docTypeP.value)) {
                foundExclusion = true;
                numPdpDocTypeExclusionsDetermined++;
            }
        }
        
        // Check for payment reason code exclusions.
        if (!excludeTransaction && StringUtils.isNotBlank(paymentReasonCodeP.value)) {
            if (summary.excludedPaymentReasonCodes.contains(paymentReasonCodeP.value)) {
                foundExclusion = true;
                excludeTransaction = true;
                numPaymentReasonExclusionsDetermined++;
            }
        }
        
        // Check if the row should be excluded from the processing based on the start of the payment line 1 address.
        if (taxBox != null && !excludeTransaction) {
            if (StringUtils.isNotBlank(paymentAddressLine1P.value)) {
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
                numDocNoteSetsRetrieved++;
                foundMatch = false;
                
                do {
                    tempValue = rsDocNote.getString(docNoteTextField.index);
                    if (StringUtils.isNotBlank(tempValue)) {
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
                numDocNoteSetsNotRetrieved++;
            }
        }
        
        // Check if the row should be excluded from the processing based on object code and initiator.
        tempSet = summary.excludedInitiatorNetIdsForObjectCodes.get(objectCodeP.value);
        if (tempSet != null && tempSet.contains(initiatorNetIdP.value)) {
            foundExclusion = true;
            currentStats.numInitiatorNetIdExclusionsDetermined++;
        }
        
        // If a DV row, then look for DV-check-stub-based inclusions/exclusions for the given box number.
        if (isDVRow) {
            Map<String,List<Pattern>> checkStubPatternsMap = summary.taxBoxIncludedDvCheckStubTextMaps.get(taxBox);
            if (checkStubPatternsMap != null) {
                dvCheckStubInclusionInd = determineClusionWithPatterns(dvCheckStubTextP.value, checkStubPatternsMap.get(objectCodeP.value),
                        summary.taxBoxDvCheckStubTextsAreWhitelists.get(taxBox).booleanValue());
                if (dvCheckStubInclusionInd == null) {
                    // No DV-check-stub restrictions exist for the given object code and tax box.
                    currentStats.numTaxBoxDvCheckStubTextNeitherDetermined[taxBox.index - 1]++;
                } else if (dvCheckStubInclusionInd.booleanValue()) {
                    // Found value in whitelist, or did not find value in blacklist.
                    currentStats.numTaxBoxDvCheckStubTextInclusionsDetermined[taxBox.index - 1]++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    currentStats.numTaxBoxDvCheckStubTextExclusionsDetermined[taxBox.index - 1]++;
                }
            }
        }
        
        
        
        /*
         * Look for box-specific inclusions/exclusions.
         */
        if (summary.derivedValues.miscRoyalties.equals(taxBox)) {
            // If box royalties, check for inclusions/exclusions based on object code and chart-and-account combo.
            tempSet = summary.royaltiesIncludedObjectCodeChartAccount.get(objectCodeP.value);
            if (tempSet != null) {
                // Potential chart-and-account inclusions/exclusions exist for the given object code.
                if (summary.royaltiesObjCodeChartAccountIsWhitelist == tempSet.contains(chartAndAccountCombo)) {
                    // Found value in whitelist, or did not find value in blacklist.
                    royaltiesObjChartAccountInclusionInd = Boolean.TRUE;
                    currentStats.numRoyaltyObjChartAccountInclusionsDetermined++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    royaltiesObjChartAccountInclusionInd = Boolean.FALSE;
                    currentStats.numRoyaltyObjChartAccountExclusionsDetermined++;
                }
            } else {
                // No chart-and-account inclusions/exclusions exist for the given object code.
                royaltiesObjChartAccountInclusionInd = null;
                currentStats.numRoyaltyObjChartAccountNeitherDetermined++;
            }
            
        } else if (summary.derivedValues.necNonemployeeCompensation.equals(taxBox)) {
            // If Nonemployee Compensation, check for inclusions/exclusions based on vendor name or document title.
            
            // Check for vendor name inclusions/exclusions.
            nonEmployeeCompVendorNameInclusionInd = determineClusionWithPatterns(vendorNameForOutput,
                    summary.nonEmployeeCompIncludedObjectCodeVendorName.get(objectCodeP.value), summary.nonEmployeeCompObjCodeVendorNameIsWhitelist);
            if (nonEmployeeCompVendorNameInclusionInd == null) {
                // No vendor name inclusions/exclusions exist for the given object code.
                currentStats.numNonEmplCompObjVendorNameNeitherDetermined++;
            } else if (nonEmployeeCompVendorNameInclusionInd.booleanValue()) {
                // Found value in whitelist, or did not find value in blacklist.
                currentStats.numNonEmplCompObjVendorNameInclusionsDetermined++;
            } else {
                // Found value in blacklist, or did not find value in whitelist.
                currentStats.numNonEmplCompObjVendorNameExclusionsDetermined++;
            }
            
            // Check for parent vendor name inclusions/exclusions.
            if (StringUtils.isNotBlank(parentVendorNameForOutput)) {
                nonEmployeeCompParentVendorNameInclusionInd = determineClusionWithPatterns(parentVendorNameForOutput,
                        summary.nonEmployeeCompIncludedObjectCodeParentVendorName.get(objectCodeP.value),
                        summary.nonEmployeeCompObjCodeParentVendorNameIsWhitelist);
                if (nonEmployeeCompParentVendorNameInclusionInd == null) {
                    // No parent vendor name inclusions/exclusions exist for the given object code.
                    currentStats.numNonEmplCompObjParentVendorNameNeitherDetermined++;
                } else if (nonEmployeeCompParentVendorNameInclusionInd.booleanValue()) {
                    // Found value in whitelist, or did not find value in blacklist.
                    currentStats.numNonEmplCompObjParentVendorNameInclusionsDetermined++;
                } else {
                    // Found value in blacklist, or did not find value in whitelist.
                    currentStats.numNonEmplCompObjParentVendorNameExclusionsDetermined++;
                }
            }
            
            // Check for document title inclusions/exclusions.
            nonEmployeeCompDocTitleInclusionInd = determineClusionWithPatterns(docTitleP.value,
                    summary.nonEmployeeCompIncludedObjectCodeDocTitle.get(objectCodeP.value), summary.nonEmployeeCompObjCodeDocTitleIsWhitelist);
            if (nonEmployeeCompDocTitleInclusionInd == null) {
                // No document title inclusions/exclusions exist for the given object code.
                currentStats.numNonEmplCompObjDocTitleNeitherDetermined++;
            } else if (nonEmployeeCompDocTitleInclusionInd.booleanValue()) {
                // Found value in whitelist, or did not find value in blacklist.
                currentStats.numNonEmplCompObjDocTitleInclusionsDetermined++;
            } else {
                // Found value in blacklist, or did not find value in whitelist.
                currentStats.numNonEmplCompObjDocTitleExclusionsDetermined++;
            }
        }
    }



    /*
     * Helper method for determining final inclusion/exclusion state, and for updating box amounts and box type indicators as needed.
     */
    private void processExclusionsAndAmounts(ResultSet rs, TransactionDetailRow detailRow, Transaction1099Summary summary) throws SQLException {
        // Determine explicit inclusion state based on other inclusions.
        isParm1099Inclusion = Boolean.TRUE.equals(dvCheckStubInclusionInd)
                || Boolean.TRUE.equals(royaltiesObjChartAccountInclusionInd)
                || Boolean.TRUE.equals(nonEmployeeCompVendorNameInclusionInd)
                || Boolean.TRUE.equals(nonEmployeeCompParentVendorNameInclusionInd)
                || Boolean.TRUE.equals(nonEmployeeCompDocTitleInclusionInd);
        
        // Determine explicit exclusion state based on other exclusions.
        isParm1099Exclusion = foundExclusion
                || Boolean.FALSE.equals(dvCheckStubInclusionInd)
                || Boolean.FALSE.equals(royaltiesObjChartAccountInclusionInd)
                || Boolean.FALSE.equals(nonEmployeeCompVendorNameInclusionInd)
                || Boolean.FALSE.equals(nonEmployeeCompParentVendorNameInclusionInd)
                || Boolean.FALSE.equals(nonEmployeeCompDocTitleInclusionInd);
        
        
        
        // Check for tax box overrides.
        overrideTaxBox = summary.transactionOverrides.get(new StringBuilder(SMALL_BUILDER_SIZE)
                        .append(paymentDateP.value).append(';')
                        .append(docNumberP.value).append(';')
                        .append(docLineNumberP.value).toString());
        if (overrideTaxBox != null) {
            overriddenTaxBox = taxBox;
            taxBox = overrideTaxBox;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found override for tax box, exclusions will be ignored. Key: " + rowKey);
            }
        } else {
            overriddenTaxBox = null;
        }
        
        
        
        // If explicit inclusion, log stats accordingly.
        if (isParm1099Inclusion) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found explicit inclusions for row with key: " + rowKey);
            }
        }
        
        // Perform logging and updates depending on box type and exclusions.
        if ((isParm1099Exclusion && overrideTaxBox == null) || summary.derivedValues.boxUnknown1099.equals(overrideTaxBox)) {
            // If exclusion without an override (or an override to a non-reportable box type), then do not update amounts.
            rs.updateString(detailRow.form1099Type.index, CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE);
            rs.updateString(detailRow.form1099Box.index, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
            if (taxBox != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found exclusions for row with key: " + rowKey);
                }
                if (overrideTaxBox != null) {
                    Pair<String, String> overriddenTypeAndBox = findFormTypeAndBoxNumberForPotentiallyNullField(
                            overriddenTaxBox, summary);
                    rs.updateString(detailRow.form1099OverriddenType.index, overriddenTypeAndBox.getLeft());
                    rs.updateString(detailRow.form1099OverriddenBox.index, overriddenTypeAndBox.getRight());
                }
            } else {
                numNoBoxDeterminedRows++;
            }
            
        } else if (taxBox != null && !summary.derivedValues.boxUnknown1099.equals(taxBox)) {
            // If not an exclusion and a tax box was found, then update the box's current amount.
            taxBoxPiece = boxAmountsMap.get(taxBox);
            if (taxBoxPiece != null) {
                taxBoxPiece.value = taxBoxPiece.value.add(paymentAmountP.value);
                
            } else if (!summary.derivedValues.miscDirectSalesInd.equals(taxBox)
                    && !summary.derivedValues.miscPayerStateNumber.equals(taxBox)
                    && !summary.derivedValues.necPayerStateNumber.equals(taxBox)) {
                throw new RuntimeException("Unrecognized 1099 tax box type");
            }
            
            // Update tax box indicator fields.
            foundAmount = true;
            Pair<String, String> typeAndBox = findKnownFormTypeAndBoxNumberForField(taxBox, summary);
            rs.updateString(detailRow.form1099Type.index, typeAndBox.getLeft());
            rs.updateString(detailRow.form1099Box.index, typeAndBox.getRight());
            if (overrideTaxBox != null) {
                Pair<String, String> overriddenTypeAndBox = findFormTypeAndBoxNumberForPotentiallyNullField(
                        overriddenTaxBox, summary);
                rs.updateString(detailRow.form1099OverriddenType.index, overriddenTypeAndBox.getLeft());
                rs.updateString(detailRow.form1099OverriddenBox.index, overriddenTypeAndBox.getRight());
            }
            
        } else {
            // Otherwise, do not update amounts.
            rs.updateString(detailRow.form1099Type.index, CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE);
            rs.updateString(detailRow.form1099Box.index, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
            numNoBoxDeterminedRows++;
        }
    }

    private Pair<String, String> findKnownFormTypeAndBoxNumberForField(
            TaxTableField taxField, Transaction1099Summary summary) {
        Objects.requireNonNull(taxField, "taxField cannot be null; this should never happen!");
        return Objects.requireNonNull(summary.boxNumberReverseMappings.get(taxField),
                "Mapping for taxField " + taxField.propertyName + " cannot be null; this should never happen!");
    }

    private Pair<String, String> findFormTypeAndBoxNumberForPotentiallyNullField(
            TaxTableField taxField, Transaction1099Summary summary) {
        if (taxField == null) {
            return CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY;
        } else {
            return summary.boxNumberReverseMappings.getOrDefault(
                    taxField, CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY);
        }
    }

    /*
     * Helper method for writing 1099 tab records to the output file.
     */
    private void writeTabLineToFile(Transaction1099Summary summary) throws SQLException, IOException {
        // Setup unencrypted tax ID piece.
        if (!summary.scrubbedOutput) {
            outputTaxIdP.value = unencryptedTaxId;
        }
        
        // Setup tax names. (Even though these are detail fields, they have been updated in the transaction table as part of the 1099 processing.)
        vendorNameP.value = vendorNameForOutput;
        parentVendorNameP.value = parentVendorNameForOutput;
        
        // Setup zip code without hyphens.
        vendorNumbersOnlyZipCodeP.value = StringUtils.remove(rsVendorAnyAddress.getString(vendorAddressRow.vendorZipCode.index), '-');
        
        // Write the tab record(s) to the file(s).
        resetBuffer(SHARED_TAB_DATA_BUFFER_INDEX);
        appendPieces(SHARED_TAB_DATA_BUFFER_INDEX);
        
        if (shouldWrite1099MiscTabLine(summary)) {
            resetBuffer(MISC_TAB_RECORD_BUFFER_INDEX);
            appendBuffer(SHARED_TAB_DATA_BUFFER_INDEX, MISC_TAB_RECORD_BUFFER_INDEX);
            appendPieces(MISC_TAB_RECORD_BUFFER_INDEX);
            writeBufferToOutput(MISC_TAB_RECORD_BUFFER_INDEX, TAX_1099_MISC_WRITER_INDEX);
            numMiscTabRecordsWritten++;
        }
        
        if (shouldWrite1099NecTabLine(summary)) {
            resetBuffer(NEC_TAB_RECORD_BUFFER_INDEX);
            appendBuffer(SHARED_TAB_DATA_BUFFER_INDEX, NEC_TAB_RECORD_BUFFER_INDEX);
            appendPieces(NEC_TAB_RECORD_BUFFER_INDEX);
            writeBufferToOutput(NEC_TAB_RECORD_BUFFER_INDEX, TAX_1099_NEC_WRITER_INDEX);
            numNecTabRecordsWritten++;
        }
        
        // Reset values as needed.
        resetTaxBoxes(summary);
        foundAmount = false;
        writeTabRecord = false;
    }

    private boolean shouldWrite1099MiscTabLine(Transaction1099Summary summary) {
        return Stream
                .of(miscRentsP, miscRoyaltiesP, miscOtherIncomeP, miscFedIncomeTaxWithheldP, miscFishingBoatProceedsP,
                        miscMedicalHealthcarePaymentsP, miscSubstitutePaymentsP, miscCropInsuranceProceedsP,
                        miscGrossProceedsAttorneyP, miscSection409ADeferralP, miscGoldenParachuteP,
                        miscNonqualifiedDeferredCompensationP, miscStateTaxWithheldP)
                .anyMatch(taxBoxPiece -> taxBoxAmountIsLargeEnoughToBeReported(taxBoxPiece, summary));
    }

    private boolean shouldWrite1099NecTabLine(Transaction1099Summary summary) {
        return Stream.of(necNonemployeeCompensationP, necFedIncomeTaxWithheldP, necStateTaxWithheldP)
                .anyMatch(taxBoxPiece -> taxBoxAmountIsLargeEnoughToBeReported(taxBoxPiece, summary));
    }

    private boolean taxBoxAmountIsLargeEnoughToBeReported(
            RecordPiece1099BigDecimal taxBoxPiece, Transaction1099Summary summary) {
        BigDecimal minimumReportingAmount = summary.taxBoxMinimumReportingAmounts
                .getOrDefault(taxBoxPiece.tableField, summary.defaultTaxBoxMinimumReportingAmount);
        return taxBoxPiece.value.compareTo(minimumReportingAmount) >= 0;
    }

    @Override
    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> statistics = super.getStatistics();
        
        statistics.put(TaxStatType.NUM_TRANSACTION_ROWS, Integer.valueOf(numTransactionRows));
        statistics.put(TaxStatType.NUM_MISC_TAB_RECORDS_WRITTEN, Integer.valueOf(numMiscTabRecordsWritten));
        statistics.put(TaxStatType.NUM_NEC_TAB_RECORDS_WRITTEN, Integer.valueOf(numNecTabRecordsWritten));
        statistics.put(TaxStatType.NUM_VENDOR_NAMES_PARSED, Integer.valueOf(numVendorNamesParsed));
        statistics.put(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED, Integer.valueOf(numVendorNamesNotParsed));
        statistics.put(TaxStatType.NUM_NO_VENDOR, Integer.valueOf(numNoVendor));
        statistics.put(TaxStatType.NUM_NO_PARENT_VENDOR, Integer.valueOf(numNoParentVendor));
        statistics.put(TaxStatType.NUM_NO_VENDOR_ADDRESS, Integer.valueOf(numNoVendorAddress));
        statistics.put(TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numVendorTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numOwnershipTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED, Integer.valueOf(numPdpDocTypeExclusionsDetermined));
        statistics.put(TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED, Integer.valueOf(numPaymentReasonExclusionsDetermined));
        statistics.put(TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED, Integer.valueOf(numDocNoteSetsRetrieved));
        statistics.put(TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED, Integer.valueOf(numDocNoteSetsNotRetrieved));
        statistics.put(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS, Integer.valueOf(numNoBoxDeterminedRows));
        
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED,
                dvStats.numPaymentAddressExclusionsDetermined, pdpStats.numPaymentAddressExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED,
                dvStats.numDocNotesExclusionsDetermined, pdpStats.numDocNotesExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_INITIATOR_NETID_EXCLUSIONS_DETERMINED,
                dvStats.numInitiatorNetIdExclusionsDetermined, pdpStats.numInitiatorNetIdExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                dvStats.numRoyaltyObjChartAccountInclusionsDetermined, pdpStats.numRoyaltyObjChartAccountInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED,
                dvStats.numRoyaltyObjChartAccountExclusionsDetermined, pdpStats.numRoyaltyObjChartAccountExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED,
                dvStats.numRoyaltyObjChartAccountNeitherDetermined, pdpStats.numRoyaltyObjChartAccountNeitherDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_VENDOR_NAME_INCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjVendorNameInclusionsDetermined, pdpStats.numNonEmplCompObjVendorNameInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_VENDOR_NAME_EXCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjVendorNameExclusionsDetermined, pdpStats.numNonEmplCompObjVendorNameExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_VENDOR_NAME_NEITHER_DETERMINED,
                dvStats.numNonEmplCompObjVendorNameNeitherDetermined, pdpStats.numNonEmplCompObjVendorNameNeitherDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_PARENT_VENDOR_NAME_INCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjParentVendorNameInclusionsDetermined, pdpStats.numNonEmplCompObjParentVendorNameInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_PARENT_VENDOR_NAME_EXCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjParentVendorNameExclusionsDetermined, pdpStats.numNonEmplCompObjParentVendorNameExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_PARENT_VENDOR_NAME_NEITHER_DETERMINED,
                dvStats.numNonEmplCompObjParentVendorNameNeitherDetermined, pdpStats.numNonEmplCompObjParentVendorNameNeitherDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_DOC_TITLE_INCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjDocTitleInclusionsDetermined, pdpStats.numNonEmplCompObjDocTitleInclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_DOC_TITLE_EXCLUSIONS_DETERMINED,
                dvStats.numNonEmplCompObjDocTitleExclusionsDetermined, pdpStats.numNonEmplCompObjDocTitleExclusionsDetermined);
        TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.NUM_NON_EMPL_COMP_DOC_TITLE_NEITHER_DETERMINED,
                dvStats.numNonEmplCompObjDocTitleNeitherDetermined, pdpStats.numNonEmplCompObjDocTitleNeitherDetermined);
        
        for (Map.Entry<Integer,String> taxBoxMapping : boxNumberMappingsWithStatistics.entrySet()) {
            int statIndex = taxBoxMapping.getKey().intValue();
            String prefix = getTaxBoxStatConstantPrefix(taxBoxMapping.getValue());
            
            TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.valueOf(prefix + TAX_BOX_STAT_CONST_INCLUDE_SUFFIX),
                    dvStats.numTaxBoxDvCheckStubTextInclusionsDetermined[statIndex],
                    pdpStats.numTaxBoxDvCheckStubTextInclusionsDetermined[statIndex]);
            TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.valueOf(prefix + TAX_BOX_STAT_CONST_EXCLUDE_SUFFIX),
                    dvStats.numTaxBoxDvCheckStubTextExclusionsDetermined[statIndex],
                    pdpStats.numTaxBoxDvCheckStubTextExclusionsDetermined[statIndex]);
            TaxSqlUtils.addDvPdpTotalStats(statistics, TaxStatType.valueOf(prefix + TAX_BOX_STAT_CONST_NEITHER_SUFFIX),
                    dvStats.numTaxBoxDvCheckStubTextNeitherDetermined[statIndex],
                    pdpStats.numTaxBoxDvCheckStubTextNeitherDetermined[statIndex]);
        }
        
        return statistics;
    }

    private String getTaxBoxStatConstantPrefix(String propertyName) {
        String propertyNameSubstring = StringUtils.left(
                StringUtils.defaultString(propertyName), BOX_PROPERTY_SUBSTRING_MAX_LENGTH);
        return TAX_BOX_STAT_CONST_PREFIX + propertyNameSubstring.toUpperCase(Locale.US);
    }

    /*
     * Convenience method for building a Map between derived-field indexes and derived-field property names.
     * The generated Map's iterators will return the results in the order that they should be added to the
     * statistics printing, provided that the Map remains unaltered.
     */
    private Map<Integer, String> getTaxBoxNumberMappingsWithStatistics(Transaction1099Summary summary) {
        DerivedValuesRow derivedValues = summary.derivedValues;
        Map<Integer, String> fieldsWithStats = Stream.of(
                derivedValues.miscRents,
                derivedValues.miscRoyalties,
                derivedValues.miscOtherIncome,
                derivedValues.miscFedIncomeTaxWithheld,
                derivedValues.miscFishingBoatProceeds,
                derivedValues.miscMedicalHealthcarePayments,
                derivedValues.miscSubstitutePayments,
                derivedValues.miscCropInsuranceProceeds,
                derivedValues.miscGrossProceedsAttorney,
                derivedValues.miscSection409ADeferral,
                derivedValues.miscGoldenParachute,
                derivedValues.miscNonqualifiedDeferredCompensation,
                derivedValues.miscStateTaxWithheld,
                derivedValues.miscStateIncome,
                derivedValues.necNonemployeeCompensation,
                derivedValues.necFedIncomeTaxWithheld,
                derivedValues.necStateTaxWithheld,
                derivedValues.necStateIncome
                ).collect(Collectors.toMap(
                        taxField -> taxField.index, taxField -> taxField.propertyName,
                        (value1, value2) -> value2, LinkedHashMap::new));
        return fieldsWithStats;
    }



    @Override
    void clearReferences() {
        // Clear out ResultSet references.
        rsDummy = null;
        rsVendor = null;
        rsVendorAnyAddress = null;
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
        docNumberP = null;
        payeeIdP = null;
        docTypeP = null;
        docTitleP = null;
        objectCodeP = null;
        taxIdP = null;
        vendorNameP = null;
        parentVendorNameP = null;
        paymentReasonCodeP = null;
        paymentAddressLine1P = null;
        dvCheckStubTextP = null;
        chartCodeP = null;
        accountNumberP = null;
        initiatorNetIdP = null;
        docLineNumberP = null;
        paymentAmountP = null;
        paymentDateP = null;
        tabSiteIdP = null;
        outputTaxIdP = null;
        vendorEmailAddressP = null;
        vendorAddressLine1P = null;
        vendorNumbersOnlyZipCodeP = null;
        vendorForeignCountryNameP = null;
        vendorForeignCountryIndicatorP = null;
        taxYearP = null;
        taxEINValueP = null;
        filerName1P = null;
        filerName2P = null;
        filerAddress1P = null;
        filerAddress2P = null;
        filerCityP = null;
        filerStateP = null;
        filerZipCodeP = null;
        filerPhoneNumberP = null;
        miscRentsP = null;
        miscRoyaltiesP = null;
        miscOtherIncomeP = null;
        miscFedIncomeTaxWithheldP = null;
        miscFishingBoatProceedsP = null;
        miscMedicalHealthcarePaymentsP = null;
        miscDirectSalesIndP = null;
        miscSubstitutePaymentsP = null;
        miscCropInsuranceProceedsP = null;
        miscGrossProceedsAttorneyP = null;
        miscSection409ADeferralP = null;
        miscGoldenParachuteP = null;
        miscNonqualifiedDeferredCompensationP = null;
        miscStateTaxWithheldP = null;
        miscPayerStateNumberP = null;
        miscStateIncomeP = null;
        necNonemployeeCompensationP = null;
        necFedIncomeTaxWithheldP = null;
        necStateTaxWithheldP = null;
        necPayerStateNumberP = null;
        necStateIncomeP = null;
        if (boxAmountsMap != null) {
            boxAmountsMap.clear();
            boxAmountsMap = null;
        }
        
        // Perform other cleanup.
        tempRetrievedValue = null;
        taxBox = null;
        overrideTaxBox = null;
        overriddenTaxBox = null;
        taxBoxPiece = null;
        encryptionService = null;
        vendorRow = null;
        vendorAddressRow = null;
        docNoteTextField = null;
        if (boxNumberMappingsWithStatistics != null) {
            boxNumberMappingsWithStatistics.clear();
            boxNumberMappingsWithStatistics = null;
        }
        pdpStats = null;
        dvStats = null;
        currentStats = null;
    }



    /**
     * Helper class containing various statistics that are specific
     * to a particular detail row origin (DV, PDP, etc.).
     */
    private final class OriginSpecificStats {
        private int numPaymentAddressExclusionsDetermined;
        private int numDocNotesExclusionsDetermined;
        private int numInitiatorNetIdExclusionsDetermined;
        private int numRoyaltyObjChartAccountInclusionsDetermined;
        private int numRoyaltyObjChartAccountExclusionsDetermined;
        private int numRoyaltyObjChartAccountNeitherDetermined;
        private int numNonEmplCompObjVendorNameInclusionsDetermined;
        private int numNonEmplCompObjVendorNameExclusionsDetermined;
        private int numNonEmplCompObjVendorNameNeitherDetermined;
        private int numNonEmplCompObjParentVendorNameInclusionsDetermined;
        private int numNonEmplCompObjParentVendorNameExclusionsDetermined;
        private int numNonEmplCompObjParentVendorNameNeitherDetermined;
        private int numNonEmplCompObjDocTitleInclusionsDetermined;
        private int numNonEmplCompObjDocTitleExclusionsDetermined;
        private int numNonEmplCompObjDocTitleNeitherDetermined;
        private int[] numTaxBoxDvCheckStubTextInclusionsDetermined;
        private int[] numTaxBoxDvCheckStubTextExclusionsDetermined;
        private int[] numTaxBoxDvCheckStubTextNeitherDetermined;
        
        
        private OriginSpecificStats(int derivedFieldCount) {
            this.numTaxBoxDvCheckStubTextInclusionsDetermined = new int[derivedFieldCount];
            this.numTaxBoxDvCheckStubTextExclusionsDetermined = new int[derivedFieldCount];
            this.numTaxBoxDvCheckStubTextNeitherDetermined = new int[derivedFieldCount];
        }
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
    private final class RecordPiece1099String extends IndexedColumnRecordPiece {
        private String value;
        
        private RecordPiece1099String(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            return value;
        }
        
        @Override
        void notifyOfTruncatedValue() {
            // Warn of certain truncated vendor name or vendor address values.
            if (this == vendorNameP) {
                LOG.warn("Found tax row whose vendor name had to be truncated! Key: " + rowKey);
            } else if (this == parentVendorNameP) {
                LOG.warn("Found tax row whose parent vendor name had to be truncated! Key: " + rowKey);
            } else if (this == vendorAddressLine1P) {
                LOG.warn("Found tax row whose vendor address had to be truncated! Key: " + rowKey);
            } else if (this == vendorForeignCountryNameP) {
                LOG.warn("Found tax row whose foreign country name had to be truncated! Key: " + rowKey);
            }
        }
    }

    /**
     * Represents an int value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1099Int extends IndexedColumnRecordPiece {
        private int value;
        private boolean negateStringValue;
        
        private RecordPiece1099Int(String name, int len, int columnIndex) {
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
    private final class RecordPiece1099BigDecimal extends FormattedNumberRecordPiece {
        final TaxTableField tableField;
        private BigDecimal value;
        private boolean negateStringValue;
        
        private RecordPiece1099BigDecimal(String name, int len, int columnIndex, boolean useAmountFormat, TaxTableField tableField) {
            super(name, len, columnIndex, useAmountFormat);
            this.tableField = tableField;
        }
        
        @Override
        Object getNumericValue() throws SQLException {
            return negateStringValue ? value.negate() : value;
        }
    }

    /**
     * Represents a java.sql.Date value that will potentially be written to one of the output files.
     */
    private final class RecordPiece1099Date extends FormattedDateRecordPiece {
        private java.sql.Date value;
        
        private RecordPiece1099Date(String name, int len, int columnIndex) {
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
    private final class RecordPiece1099ResultSetDerivedString extends IndexedColumnRecordPiece {
        private final int rsIndex;
        
        private RecordPiece1099ResultSetDerivedString(String name, int len, int columnIndex, int rsIndex) {
            super(name, len, columnIndex);
            this.rsIndex = rsIndex;
        }
        
        @Override
        String getValue() throws SQLException {
            switch (rsIndex) {
                case CUTaxConstants.VENDOR_DETAIL_INDEX :
                    tempRetrievedValue = rsVendor.getString(columnIndex);
                    break;
                
                case VENDOR_ANY_ADDRESS_INDEX :
                    tempRetrievedValue = rsVendorAnyAddress.getString(columnIndex);
                    break;
                
                default :
                    throw new IllegalStateException("Bad result set index configured!");
            }
            
            return tempRetrievedValue;
        }
        
        @Override
        void notifyOfTruncatedValue() {
            if (rsIndex == VENDOR_ANY_ADDRESS_INDEX && columnIndex == vendorAddressRow.vendorLine2Address.index) {
                LOG.warn("Found tax row whose vendor address (line 2) had to be truncated! Key: " + rowKey);
            }
        }
    }
    
    public PaymentReason1099BoxService getPaymentReason1099BoxService() {
		if (paymentReason1099BoxService == null) {
			paymentReason1099BoxService = SpringContext.getBean(PaymentReason1099BoxService.class);
		}
		return paymentReason1099BoxService;
	}

	public void setPaymentReason1099BoxService(PaymentReason1099BoxService paymentReason1099BoxService) {
		this.paymentReason1099BoxService = paymentReason1099BoxService;
	}

    public DocumentType1099BoxService getDocumentType1099BoxService() {
        if (documentType1099BoxService == null) {
            documentType1099BoxService = SpringContext.getBean(DocumentType1099BoxService.class);
        }
        return documentType1099BoxService;
    }

    public void setDocumentType1099BoxService(DocumentType1099BoxService documentType1099BoxService) {
        this.documentType1099BoxService = documentType1099BoxService;
    }

    public LocationService getLocationService() {
        if (locationService == null) {
            locationService = SpringContext.getBean(LocationService.class, CUKFSConstants.LOCATION_SERVICE_BEAN_NAME);
        }
        return locationService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

}
