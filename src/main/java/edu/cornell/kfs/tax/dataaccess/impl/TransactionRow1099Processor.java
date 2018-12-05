package edu.cornell.kfs.tax.dataaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.CoreApiServiceLocator;
import org.kuali.rice.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
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
 *   <li>Header row section</li>
 *   <li>Tab Record section</li>
 * </ol>
 * 
 * <p>In addition, this processor has the following outputs:</p>
 * 
 * <ol>
 *   <li>Tab records file</li>
 * </ol>
 * 
 * <p>The following DERIVED-type fields will be populated by this implementation:</p>
 * 
 * <ul>
 *   <li>vendorEmailAddress</li>
 *   <li>vendorAnyAddressLine1</li>
 *   <li>vendorZipCodeNumOnly</li>
 *   <li>ssn</li>
 *   <li>tabSiteId</li>
 *   <li>box1</li>
 *   <li>box2</li>
 *   <li>box3</li>
 *   <li>box4</li>
 *   <li>box5</li>
 *   <li>box6</li>
 *   <li>box7</li>
 *   <li>box8</li>
 *   <li>box10</li>
 *   <li>box11</li>
 *   <li>box12</li>
 *   <li>box13</li>
 *   <li>box14</li>
 *   <li>box15a</li>
 *   <li>box15b</li>
 *   <li>box16</li>
 *   <li>box18</li>
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
 * </ul>
 */
public class TransactionRow1099Processor extends TransactionRowProcessor<Transaction1099Summary> {
	private static final Logger LOG = LogManager.getLogger(TransactionRow1099Processor.class);

    private static final String BOX_PREFIX = "box";

    private static final int NUM_1099_CHAR_BUFFERS = 2;
    private static final int NUM_1099_WRITERS = 1;

    private static final int HEADER_BUFFER_INDEX = 0;
    private static final int TAB_RECORD_BUFFER_INDEX = 1;
    private static final int TAX_1099_WRITER_INDEX = 0;

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
    //private RecordPiece1099String vendorLastNameP;
    //private RecordPiece1099String vendorFirstNameP;
    private RecordPiece1099String vendorEmailAddressP;
    private RecordPiece1099String vendorAddressLine1P;
    private RecordPiece1099String vendorNumbersOnlyZipCodeP;

    // Variables pertaining to derived fields related to a tax box.
    private Map<TaxTableField,RecordPiece1099BigDecimal> boxesMap;
    private RecordPiece1099BigDecimal box1P;
    private RecordPiece1099BigDecimal box2P;
    private RecordPiece1099BigDecimal box3P;
    private RecordPiece1099BigDecimal box4P;
    private RecordPiece1099BigDecimal box5P;
    private RecordPiece1099BigDecimal box6P;
    private RecordPiece1099BigDecimal box7P;
    private RecordPiece1099BigDecimal box8P;
    private RecordPiece1099BigDecimal box10P;
    private RecordPiece1099BigDecimal box11P;
    private RecordPiece1099BigDecimal box12P;
    private RecordPiece1099BigDecimal box13P;
    private RecordPiece1099BigDecimal box14P;
    private RecordPiece1099BigDecimal box15aP;
    private RecordPiece1099BigDecimal box15bP;
    private RecordPiece1099BigDecimal box16P;
    private RecordPiece1099BigDecimal box18P;

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
    private int numTabRecordsWritten;
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
                        derivedValues.ssn,
                        derivedValues.tabSiteId,
                        derivedValues.box1,
                        derivedValues.box2,
                        derivedValues.box3,
                        derivedValues.box4,
                        derivedValues.box5,
                        derivedValues.box6,
                        derivedValues.box7,
                        derivedValues.box8,
                        derivedValues.box10,
                        derivedValues.box11,
                        derivedValues.box12,
                        derivedValues.box13,
                        derivedValues.box14,
                        derivedValues.box15a,
                        derivedValues.box15b,
                        derivedValues.box16,
                        derivedValues.box18
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
        rowIdP = (RecordPiece1099String) complexPieces.get(detailRow.transactionDetailId.propertyName);
        docNumberP = (RecordPiece1099String) complexPieces.get(detailRow.documentNumber.propertyName);
        docTypeP = (RecordPiece1099String) complexPieces.get(detailRow.documentType.propertyName);
        docTitleP = (RecordPiece1099String) complexPieces.get(detailRow.documentTitle.propertyName);
        docLineNumberP = (RecordPiece1099Int) complexPieces.get(detailRow.financialDocumentLineNumber.propertyName);
        objectCodeP = (RecordPiece1099String) complexPieces.get(detailRow.finObjectCode.propertyName);
        paymentAmountP = (RecordPiece1099BigDecimal) complexPieces.get(detailRow.netPaymentAmount.propertyName);
        taxIdP = (RecordPiece1099String) complexPieces.get(detailRow.vendorTaxNumber.propertyName);
        dvCheckStubTextP = (RecordPiece1099String) complexPieces.get(detailRow.dvCheckStubText.propertyName);
        payeeIdP = (RecordPiece1099String) complexPieces.get(detailRow.payeeId.propertyName);
        vendorNameP = (RecordPiece1099String) complexPieces.get(detailRow.vendorName.propertyName);
        parentVendorNameP = (RecordPiece1099String) complexPieces.get(detailRow.parentVendorName.propertyName);
        paymentDateP = (RecordPiece1099Date) complexPieces.get(detailRow.paymentDate.propertyName);
        paymentAddressLine1P = (RecordPiece1099String) complexPieces.get(detailRow.paymentLine1Address.propertyName);
        chartCodeP = (RecordPiece1099String) complexPieces.get(detailRow.chartCode.propertyName);
        accountNumberP = (RecordPiece1099String) complexPieces.get(detailRow.accountNumber.propertyName);
        initiatorNetIdP = (RecordPiece1099String) complexPieces.get(detailRow.initiatorNetId.propertyName);
        paymentReasonCodeP = (RecordPiece1099String) complexPieces.get(detailRow.paymentReasonCode.propertyName);
        
        // Retrieve the various derived "pieces" that will be needed for the processing.
        vendorEmailAddressP = (RecordPiece1099String) complexPieces.get(derivedValues.vendorEmailAddress.propertyName);
        vendorAddressLine1P = (RecordPiece1099String) complexPieces.get(derivedValues.vendorAnyAddressLine1.propertyName);
        vendorNumbersOnlyZipCodeP = (RecordPiece1099String) complexPieces.get(derivedValues.vendorZipCodeNumOnly.propertyName);
        outputTaxIdP = (RecordPiece1099String) complexPieces.get(derivedValues.ssn.propertyName);
        tabSiteIdP = (RecordPiece1099String) complexPieces.get(derivedValues.tabSiteId.propertyName);
        box1P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box1.propertyName);
        box2P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box2.propertyName);
        box3P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box3.propertyName);
        box4P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box4.propertyName);
        box5P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box5.propertyName);
        box6P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box6.propertyName);
        box7P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box7.propertyName);
        box8P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box8.propertyName);
        box10P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box10.propertyName);
        box11P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box11.propertyName);
        box12P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box12.propertyName);
        box13P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box13.propertyName);
        box14P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box14.propertyName);
        box15aP = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box15a.propertyName);
        box15bP = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box15b.propertyName);
        box16P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box16.propertyName);
        box18P = (RecordPiece1099BigDecimal) complexPieces.get(derivedValues.box18.propertyName);
        
        // Populate tax box field map.
        boxesMap = new HashMap<TaxTableField,RecordPiece1099BigDecimal>();
        boxesMap.put(box1P.tableField, box1P);
        boxesMap.put(box2P.tableField, box2P);
        boxesMap.put(box3P.tableField, box3P);
        boxesMap.put(box4P.tableField, box4P);
        boxesMap.put(box5P.tableField, box5P);
        boxesMap.put(box6P.tableField, box6P);
        boxesMap.put(box7P.tableField, box7P);
        boxesMap.put(box8P.tableField, box8P);
        boxesMap.put(box10P.tableField, box10P);
        boxesMap.put(box11P.tableField, box11P);
        boxesMap.put(box12P.tableField, box12P);
        boxesMap.put(box13P.tableField, box13P);
        boxesMap.put(box14P.tableField, box14P);
        boxesMap.put(box15aP.tableField, box15aP);
        boxesMap.put(box15bP.tableField, box15bP);
        boxesMap.put(box16P.tableField, box16P);
        boxesMap.put(box18P.tableField, box18P);
    }



    @Override
    String[] getFilePathsForWriters(Transaction1099Summary summary, java.util.Date processingStartDate) {
        String[] filePaths = super.getFilePathsForWriters(summary, processingStartDate);
        // Output file for 1099 tab records.
        filePaths[0] = new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/')
                .append(CUTaxConstants.TAX_1099_OUTPUT_FILE_PREFIX).append(summary.reportYear)
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
        rsDummy = new DummyResultSet();
        vendorRow = summary.vendorRow;
        vendorAddressRow = summary.vendorAddressRow;
        docNoteTextField = summary.documentNoteRow.noteText;
        if (summary.scrubbedOutput) {
            outputTaxIdP.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }
        
        // Print header.
        resetBuffer(HEADER_BUFFER_INDEX);
        appendPieces(HEADER_BUFFER_INDEX);
        writeBufferToOutput(HEADER_BUFFER_INDEX, TAX_1099_WRITER_INDEX);
        
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
        box1P.value = summary.zeroAmount;
        box2P.value = summary.zeroAmount;
        box3P.value = summary.zeroAmount;
        box4P.value = summary.zeroAmount;
        box5P.value = summary.zeroAmount;
        box6P.value = summary.zeroAmount;
        box7P.value = summary.zeroAmount;
        box8P.value = summary.zeroAmount;
        box10P.value = summary.zeroAmount;
        box11P.value = summary.zeroAmount;
        box12P.value = summary.zeroAmount;
        box13P.value = summary.zeroAmount;
        box14P.value = summary.zeroAmount;
        box15aP.value = summary.zeroAmount;
        box15bP.value = summary.zeroAmount;
        box16P.value = summary.zeroAmount;
        box18P.value = summary.zeroAmount;
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
            vendorAddressLine1P.value = rsVendorAnyAddress.getString(vendorAddressRow.vendorLine1Address.index);
        } else {
            vendorAddressLine1P.value = CUTaxConstants.NO_ANY_VENDOR_ADDRESS;
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
        if (summary.derivedValues.box2.equals(taxBox)) {
            // If box 2 (royalties), check for inclusions/exclusions based on object code and chart-and-account combo.
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
            
        } else if (summary.derivedValues.box7.equals(taxBox)) {
            // If box 7 (Nonemployee Compensation), check for inclusions/exclusions based on vendor name or document title.
            
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
            rs.updateString(detailRow.form1099Box.index, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
            if (taxBox != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found exclusions for row with key: " + rowKey);
                }
                if (overrideTaxBox != null) {
                    rs.updateString(detailRow.form1099OverriddenBox.index, (overriddenTaxBox != null)
                            ? overriddenTaxBox.propertyName.substring(BOX_PREFIX.length()).toUpperCase() : CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
                }
            } else {
                numNoBoxDeterminedRows++;
            }
            
        } else if (taxBox != null) {
            // If not an exclusion and a tax box was found, then update the box's current amount.
            taxBoxPiece = boxesMap.get(taxBox);
            if (taxBoxPiece != null) {
                taxBoxPiece.value = taxBoxPiece.value.add(paymentAmountP.value);
                
            } else if (!summary.derivedValues.box9.equals(taxBox) && !summary.derivedValues.box17.equals(taxBox)) {
                throw new RuntimeException("Unrecognized 1099 tax box type");
            }
            
            // Update tax box indicator fields.
            foundAmount = true;
            rs.updateString(detailRow.form1099Box.index, taxBox.propertyName.substring(BOX_PREFIX.length()).toUpperCase());
            if (overrideTaxBox != null) {
                rs.updateString(detailRow.form1099OverriddenBox.index, (overriddenTaxBox != null)
                        ? overriddenTaxBox.propertyName.substring(BOX_PREFIX.length()).toUpperCase() : CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
            }
            
        } else {
            // Otherwise, do not update amounts.
            rs.updateString(detailRow.form1099Box.index, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
            numNoBoxDeterminedRows++;
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
        
        // Write the tab record to the file.
        resetBuffer(TAB_RECORD_BUFFER_INDEX);
        appendPieces(TAB_RECORD_BUFFER_INDEX);
        writeBufferToOutput(TAB_RECORD_BUFFER_INDEX, TAX_1099_WRITER_INDEX);
        
        // Update statistics.
        numTabRecordsWritten++;
        
        // Reset values as needed.
        resetTaxBoxes(summary);
        foundAmount = false;
        writeTabRecord = false;
    }



    @Override
    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> statistics = super.getStatistics();
        
        statistics.put(TaxStatType.NUM_TRANSACTION_ROWS, Integer.valueOf(numTransactionRows));
        statistics.put(TaxStatType.NUM_TAB_RECORDS_WRITTEN, Integer.valueOf(numTabRecordsWritten));
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
            String prefix = TAX_BOX_STAT_CONST_PREFIX + taxBoxMapping.getValue().toUpperCase();
            
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



    /*
     * Convenience method for building a Map between derived-field indexes and derived-field property names.
     * The generated Map's iterators will return the results in the order that they should be added to the
     * statistics printing, provided that the Map remains unaltered and does not have its get() method invoked.
     */
    private Map<Integer,String> getTaxBoxNumberMappingsWithStatistics(Transaction1099Summary summary) {
        DerivedValuesRow derivedValues = summary.derivedValues;
        Map<Integer,String> fieldsWithStats = new LinkedHashMap<Integer,String>();
        
        fieldsWithStats.put(Integer.valueOf(derivedValues.box1.index), derivedValues.box1.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box2.index), derivedValues.box2.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box3.index), derivedValues.box3.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box4.index), derivedValues.box4.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box5.index), derivedValues.box5.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box6.index), derivedValues.box6.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box7.index), derivedValues.box7.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box8.index), derivedValues.box8.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box10.index), derivedValues.box10.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box11.index), derivedValues.box11.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box12.index), derivedValues.box12.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box13.index), derivedValues.box13.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box14.index), derivedValues.box14.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box15a.index), derivedValues.box15a.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box15b.index), derivedValues.box15b.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box16.index), derivedValues.box16.propertyName);
        fieldsWithStats.put(Integer.valueOf(derivedValues.box18.index), derivedValues.box18.propertyName);
        
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
        box1P = null;
        box2P = null;
        box3P = null;
        box4P = null;
        box5P = null;
        box6P = null;
        box7P = null;
        box8P = null;
        box10P = null;
        box11P = null;
        box12P = null;
        box13P = null;
        box14P = null;
        box15aP = null;
        box15bP = null;
        box16P = null;
        box18P = null;
        if (boxesMap != null) {
            boxesMap.clear();
            boxesMap = null;
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
}
