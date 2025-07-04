package edu.cornell.kfs.tax.dataaccess.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;

/**
 * Custom transaction detail row processor that simply prints the detail rows
 * that were created and updated by the tax processing. The output will be
 * sent to a file that is named according to the tax type and report year.
 * See the static nested classes for tax-type-specific implementations.
 * 
 * <p>This implementation will automatically replace tab characters with spaces
 * in the various text-based fields, to support printing the data to a
 * tab-delimited text file.</p>
 * 
 * <p>The following DERIVED-type "pieces" will be populated by this implementation:</p>
 * 
 * <ul>
 *   <li>ssn (unencrypted and unformatted)</li>
 *   <li>dvCheckStubTextWithUpdatedWhitespace (each whitespace character replaced with a single space)</li>
 * </ul>
 * 
 * <p>Using these derived field types instead of their raw value counterparts is recommended,
 * since they can print a more user-friendly representation of the data.</p>
 * 
 * <p>When running in "scrubbed" mode, the following fields will be forcibly masked in the output:</p>
 * 
 * <ul>
 *   <li>ssn (DERIVED field)</li>
 *   <li>vendorGIIN (DETAIL field)</li>
 * </ul>
 */
abstract class TransactionRowPrintProcessor<T extends TransactionDetailSummary> extends TransactionRowProcessor<T> {
	private static final Logger LOG = LogManager.getLogger(TransactionRowPrintProcessor.class);

    private static final int NUM_PRINT_CHAR_BUFFERS = 2;
    private static final int NUM_PRINT_WRITERS = 1;

    private static final int HEADER_BUFFER_INDEX = 0;
    private static final int DETAIL_ROW_BUFFER_INDEX = 1;

    private static final int MED_BUILDER_SIZE = 100;

    private DerivedFieldStringPiece ssnP;
    private DerivedFieldStringPiece dvCheckStubTextP;

    private ResultSet rsTransactionDetail;
    private String tempStringValue;
    private BigDecimal tempBigDecimalValue;
    private java.sql.Date tempDateValue;

    TransactionRowPrintProcessor() {
        super(0, 0, NUM_PRINT_CHAR_BUFFERS, NUM_PRINT_WRITERS);
    }

    abstract TaxTableField getFieldForWhereClause(T summary);



    /**
     * This implementation can only generate pieces for the DETAIL type and select DERIVED types.
     */
    @Override
    RecordPiece getPieceForField(TaxFieldSource fieldSource, TaxTableField field, String name, int len, T summary) {
        RecordPiece piece;
        
        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot create piece for BLANK type");
            
            case STATIC :
                throw new IllegalArgumentException("Cannot create piece for STATIC type");
            
            case DETAIL :
                // Create a piece that pre-loads its value from the transaction detail ResultSet line, or a static masked piece if GIIN and in scrubbed mode.
                piece = (!summary.scrubbedOutput || field.index != summary.transactionDetailRow.vendorGIIN.index)
                        ? getPieceForDetailField(field, name, len) : new StaticStringRecordPiece(name, len, CUTaxConstants.MASKED_VALUE_19_CHARS);
                break;
            
            case PDP :
                throw new IllegalArgumentException("Cannot create piece for PDP type");
            
            case DV :
                throw new IllegalArgumentException("Cannot create piece for DV type");
            
            case VENDOR :
                throw new IllegalArgumentException("Cannot create print-only piece for VENDOR type");
            
            case VENDOR_US_ADDRESS :
                throw new IllegalArgumentException("Cannot create print-only piece for VENDOR_US_ADDRESS type");
            
            case VENDOR_ANY_ADDRESS :
                throw new IllegalArgumentException("Cannot create print-only piece for VENDOR_ANY_ADDRESS type");
            
            case DERIVED :
                // SSN and DV_CHECK_STUB_WITH_UPDATED_WHITESPACE are the only derived-type "pieces" supported by this implementation.
                if (summary.derivedValues.ssn.equals(field)) {
                    piece = new DerivedFieldStringPiece(name, len);
                } else if (summary.derivedValues.dvCheckStubTextWithUpdatedWhitespace.equals(field)) {
                    piece = new DerivedFieldStringPiece(name, len);
                } else {
                    throw new IllegalArgumentException("Cannot create print-only piece for the given derived-field type");
                }
                break;
            
            default :
                throw new IllegalArgumentException("Unrecognized piece type");
        }
        
        return piece;
    }

    private RecordPiece getPieceForDetailField(TaxTableField detailField, String name, int len) {
        RecordPiece recordPiece;
        
        switch (detailField.jdbcType) {
            case java.sql.Types.DECIMAL :
                recordPiece = new TransactionDetailBigDecimalPiece(name, len, detailField.index);
                break;
            
            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
                recordPiece = new TransactionDetailIntPiece(name, len, detailField.index);
                break;
            
            case java.sql.Types.VARCHAR :
                recordPiece = new TransactionDetailStringPiece(name, len, detailField.index);
                break;
            
            case java.sql.Types.DATE :
                recordPiece = new TransactionDetailDatePiece(name, len, detailField.index);
                break;
            
            default :
                throw new IllegalStateException("Unrecognized field datatype");
        }
        
        return recordPiece;
    }



    @Override
    Set<TaxTableField> getMinimumFields(TaxFieldSource fieldSource, T summary) {
        Set<TaxTableField> minFields = new HashSet<TaxTableField>();
        
        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot find minimum fields for BLANK type");
            
            case STATIC :
                throw new IllegalArgumentException("Cannot find minimum fields for STATIC type");
            
            case DETAIL :
                minFields.addAll(summary.transactionDetailRow.orderedFields);
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
                minFields.add(summary.derivedValues.ssn);
                minFields.add(summary.derivedValues.dvCheckStubTextWithUpdatedWhitespace);
                break;
            
            default :
                throw new IllegalArgumentException("Invalid piece type");
        }
        
        return minFields;
    }

    @SuppressWarnings("unchecked")
    @Override
    void setComplexPieces(Map<String,RecordPiece> complexPieces, T summary) {
        ssnP = (DerivedFieldStringPiece) complexPieces.get(summary.derivedValues.ssn.propertyName);
        dvCheckStubTextP = (DerivedFieldStringPiece) complexPieces.get(summary.derivedValues.dvCheckStubTextWithUpdatedWhitespace.propertyName);
    }



    @Override
    String[] getFilePathsForWriters(T summary, LocalDateTime processingStartDate) {
        String[] filePaths = super.getFilePathsForWriters(summary, processingStartDate);
        filePaths[0] = new StringBuilder(MED_BUILDER_SIZE).append(getReportsDirectory()).append('/')
                .append(getPrintFilePrefix(summary)).append(summary.reportYear)
                .append(buildDateFormatForFileSuffixes().format(processingStartDate)).append(CUTaxConstants.TAX_OUTPUT_FILE_SUFFIX).toString();
        return filePaths;
    }

    /**
     * Returns the prefix of the name of the file to print the transaction row data to.
     * The TransactionRowPrintProcessor.getFilePathsForWriters() method will construct
     * the output file path as follows:
     * 
     * </p>[reporting directory]/[getPrintFilePrefix() value][report year][formatted batch job start time].txt</p>
     * 
     * @param summary The object containing the tax-type-specific summary information.
     * @return The prefix to use for the output file.
     */
    abstract String getPrintFilePrefix(T summary);



    @Override
    String getSqlForSelect(T summary) {
        return TaxSqlUtils.getTransactionDetailSelectSql(getFieldForWhereClause(summary), summary.transactionDetailRow, false, true);
    }

    @Override
    Object[][] getParameterValuesForSelect(T summary) {
        return new Object[][] {
            {summary.reportYear},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }



    @Override
    void processTaxRows(ResultSet rs, T summary) throws SQLException, IOException {
        // Perform initialization as needed.
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        Pattern whitespacePattern = Pattern.compile("\\p{Space}");
        EncryptionService encryptionService = CoreApiServiceLocator.getEncryptionService();
        rsTransactionDetail = rs;
        if (summary.scrubbedOutput) {
            ssnP.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }
        
        LOG.info("Starting raw transaction row printing to file...");
        
        // Print the header.
        resetBuffer(HEADER_BUFFER_INDEX);
        appendPieces(HEADER_BUFFER_INDEX);
        writeBufferToOutput(HEADER_BUFFER_INDEX, 0);
        
        // Print the data for each row to the file.
        while (rsTransactionDetail.next()) {
            // Prepare the tax number.
            if (!summary.scrubbedOutput) {
                try {
                    ssnP.value = encryptionService.decrypt(rsTransactionDetail.getString(detailRow.vendorTaxNumber.index));
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
            // Prepare the whitespace-replaced-with-spaces DV check stub text.
            dvCheckStubTextP.value = rsTransactionDetail.getString(detailRow.dvCheckStubText.index);
            if (StringUtils.isNotBlank(dvCheckStubTextP.value)) {
                dvCheckStubTextP.value = whitespacePattern.matcher(dvCheckStubTextP.value).replaceAll(KRADConstants.BLANK_SPACE);
            } 
            
            // Do the printing.
            resetBuffer(DETAIL_ROW_BUFFER_INDEX);
            appendPieces(DETAIL_ROW_BUFFER_INDEX);
            writeBufferToOutput(DETAIL_ROW_BUFFER_INDEX, 0);
        }
        
        LOG.info("Finished raw transaction row printing to file.");
    }

    @Override
    void clearReferences() {
        rsTransactionDetail = null;
        tempBigDecimalValue = null;
        tempDateValue = null;
        ssnP = null;
        dvCheckStubTextP = null;
    }



    /*
     * ============================================================================================
     * Below are helper objects for encapsulating values read from or derived from the detail rows,
     * and which will potentially be included in the output files.
     * ============================================================================================
     */

    /**
     * A RecordPiece representing a String column on a transaction detail row.
     * Replaces tab characters with spaces, due to the output file depending on
     * tab characters as delimiters.
     */
    private final class TransactionDetailStringPiece extends IndexedColumnRecordPiece {
        
        private TransactionDetailStringPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            tempStringValue = rsTransactionDetail.getString(columnIndex);
            return (tempStringValue != null) ? tempStringValue.replace('\t', ' ') : null;
        }
    }



    /**
     * A RecordPiece representing an int column on a transaction detail row.
     */
    private final class TransactionDetailIntPiece extends IndexedColumnRecordPiece {
        
        private TransactionDetailIntPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            return Integer.toString(rsTransactionDetail.getInt(columnIndex));
        }
    }



    /**
     * A RecordPiece representing a BigDecimal column on a transaction detail row.
     */
    private final class TransactionDetailBigDecimalPiece extends IndexedColumnRecordPiece {
        
        private TransactionDetailBigDecimalPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            tempBigDecimalValue = rsTransactionDetail.getBigDecimal(columnIndex);
            return (tempBigDecimalValue != null) ? tempBigDecimalValue.toPlainString() : null;
        }
    }



    /**
     * A RecordPiece representing a java.sql.Date column on a transaction detail row.
     */
    private final class TransactionDetailDatePiece extends IndexedColumnRecordPiece {
        
        private TransactionDetailDatePiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        @Override
        String getValue() throws SQLException {
            tempDateValue = rsTransactionDetail.getDate(columnIndex);
            // Return the date in yyyy-mm-dd format.
            return (tempDateValue != null) ? tempDateValue.toString() : null;
        }
    }

    /**
     * A RecordPiece representing a DerivedField "piece" with a String value.
     */
    private final class DerivedFieldStringPiece extends RecordPiece {
        private String value;
        
        private DerivedFieldStringPiece(String name, int len) {
            super(name, len, false);
        }
        
        @Override
        String getValue() throws SQLException {
            return value;
        }
    }

    /*
     * ============================================================================================
     * End of helper objects.
     * ============================================================================================
     */



    /*
     * ============================================================================================
     * Below are the tax-type-specific implementations of this class.
     * ============================================================================================
     */

    /**
     * Row-printing processor for 1099 tax reporting.
     */
    static class For1099 extends TransactionRowPrintProcessor<Transaction1099Summary> {
        For1099() {
            super();
        }
        
        @Override
        TaxTableField getFieldForWhereClause(Transaction1099Summary summary) {
            return summary.transactionDetailRow.form1099Box;
        }
        
        @Override
        String getPrintFilePrefix(Transaction1099Summary summary) {
            return CUTaxConstants.TAX_1099_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX;
        }
    }



    /**
     * Row-printing processor for 1042S tax reporting.
     */
    static class For1042S extends TransactionRowPrintProcessor<Transaction1042SSummary> {
        For1042S() {
            super();
        }
        
        @Override
        TaxTableField getFieldForWhereClause(Transaction1042SSummary summary) {
            return summary.transactionDetailRow.form1042SBox;
        }
        
        @Override
        String getPrintFilePrefix(Transaction1042SSummary summary) {
            return CUTaxConstants.TAX_1042S_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX;
        }
    }

}
