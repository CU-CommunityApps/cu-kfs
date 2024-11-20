package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SprintaxRowPrintProcessor {
    private static final Logger LOG = LogManager.getLogger(SprintaxRowPrintProcessor.class);

    private static final int HEADER_BUFFER_INDEX = 0;
    private static final int VENDOR_US_ADDRESS_INDEX = 2;
    private static final int VENDOR_FOREIGN_ADDRESS_INDEX = 3;

    private SprintaxRowPrintProcessor.DerivedFieldStringPiece ssnP;
    private SprintaxRowPrintProcessor.DerivedFieldStringPiece dvCheckStubTextP;

    private ResultSet rsTransactionDetail;
    private String tempStringValue;
    private BigDecimal tempBigDecimalValue;
    private java.sql.Date tempDateValue;
    private Transaction1042SSummary summary;
    private String reportsDirectory;
    private final ResultSet[] extraResultSets;
    private final PreparedStatement[] extraStatements;
    private final SprintaxRowPrintProcessor.OutputHelper[] outputHelpers;
    private Writer writer;

    SprintaxRowPrintProcessor(Transaction1042SSummary summary, String reportsDirectory) {
        this.summary = summary;
        this.reportsDirectory = reportsDirectory;
        this.extraResultSets = new ResultSet[0];
        this.extraStatements = new PreparedStatement[2];
        this.outputHelpers = new SprintaxRowPrintProcessor.OutputHelper[2];
    }

    SprintaxRowPrintProcessor.RecordPiece getPieceForField(CUTaxBatchConstants.TaxFieldSource fieldSource, TaxTableField field, String name, int len) {
        SprintaxRowPrintProcessor.RecordPiece piece;

        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot create piece for BLANK type");

            case STATIC :
                throw new IllegalArgumentException("Cannot create piece for STATIC type");

            case DETAIL :
                // Create a piece that pre-loads its value from the transaction detail ResultSet line, or a static masked piece if GIIN and in scrubbed mode.
                piece = (!summary.scrubbedOutput || field.index != summary.transactionDetailRow.vendorGIIN.index)
                        ? getPieceForDetailField(field, name, len) : new SprintaxRowPrintProcessor.StaticStringRecordPiece(name, len, CUTaxConstants.MASKED_VALUE_19_CHARS);
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
                    piece = new SprintaxRowPrintProcessor.DerivedFieldStringPiece(name, len);
                } else if (summary.derivedValues.dvCheckStubTextWithUpdatedWhitespace.equals(field)) {
                    piece = new SprintaxRowPrintProcessor.DerivedFieldStringPiece(name, len);
                } else {
                    throw new IllegalArgumentException("Cannot create print-only piece for the given derived-field type");
                }
                break;

            default :
                throw new IllegalArgumentException("Unrecognized piece type");
        }

        return piece;
    }

    private SprintaxRowPrintProcessor.RecordPiece getPieceForDetailField(TaxTableField detailField, String name, int len) {
        SprintaxRowPrintProcessor.RecordPiece recordPiece;

        switch (detailField.jdbcType) {
            case java.sql.Types.DECIMAL :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailBigDecimalPiece(name, len, detailField.index);
                break;

            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailIntPiece(name, len, detailField.index);
                break;

            case java.sql.Types.VARCHAR :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailStringPiece(name, len, detailField.index);
                break;

            case java.sql.Types.DATE :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailDatePiece(name, len, detailField.index);
                break;

            default :
                throw new IllegalStateException("Unrecognized field datatype");
        }

        return recordPiece;
    }

    Set<TaxTableField> getMinimumFields(CUTaxBatchConstants.TaxFieldSource fieldSource) {
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
    void setComplexPieces(Map<String, SprintaxRowPrintProcessor.RecordPiece> complexPieces) {
        ssnP = (SprintaxRowPrintProcessor.DerivedFieldStringPiece) complexPieces.get(summary.derivedValues.ssn.propertyName);
        dvCheckStubTextP = (SprintaxRowPrintProcessor.DerivedFieldStringPiece) complexPieces.get(summary.derivedValues.dvCheckStubTextWithUpdatedWhitespace.propertyName);
    }

    String getSqlForSelect() {
        return TaxSqlUtils.getTransactionDetailSelectSql(getFieldForWhereClause(summary), summary.transactionDetailRow, false, true);
    }

    Object[][] getParameterValuesForSelect() {
        return new Object[][] {
                {summary.reportYear},
                {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }

    void processTaxRows(ResultSet rs) throws SQLException, IOException {
        // Perform initialization as needed.
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
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
            resetBuffer(1);
            appendPieces(1);
            writeBufferToOutput(1, 0);

        }

        LOG.info("Finished raw transaction row printing to file.");
    }

    void clearReferences() {
        rsTransactionDetail = null;
        tempBigDecimalValue = null;
        tempDateValue = null;
        ssnP = null;
        dvCheckStubTextP = null;
    }

    private final class TransactionDetailStringPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailStringPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            tempStringValue = rsTransactionDetail.getString(columnIndex);
            return (tempStringValue != null) ? tempStringValue.replace('\t', ' ') : null;
        }
    }

    private final class TransactionDetailIntPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailIntPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            return Integer.toString(rsTransactionDetail.getInt(columnIndex));
        }
    }

    private final class TransactionDetailBigDecimalPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailBigDecimalPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            tempBigDecimalValue = rsTransactionDetail.getBigDecimal(columnIndex);
            return (tempBigDecimalValue != null) ? tempBigDecimalValue.toPlainString() : null;
        }
    }

    abstract static class IndexedColumnRecordPiece extends SprintaxRowPrintProcessor.RecordPiece {
        // the index of the column to retrieve.
        final int columnIndex;

        IndexedColumnRecordPiece(String name, int len, int columnIndex) {
            super(name, len, false);
            this.columnIndex = columnIndex;
        }
    }

    private final class TransactionDetailDatePiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

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

    private final class DerivedFieldStringPiece extends SprintaxRowPrintProcessor.RecordPiece {
        private String value;

        private DerivedFieldStringPiece(String name, int len) {
            super(name, len, false);
        }

        @Override
        String getValue() throws SQLException {
            return value;
        }
    }

    TaxTableField getFieldForWhereClause(Transaction1042SSummary summary) {
        return summary.transactionDetailRow.form1042SBox;
    }

    String getPrintFilePrefix() {
        return CUTaxConstants.TAX_1042S_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX;
    }

    /**
     * Convenience class representing a single field to be output to a file.
     *
     * <p>The name field is primarily for debugging or logging convenience.</p>
     *
     * <p>The len field should be set to the maximum length expected for the field.
     * During processing, any returned values longer than this length will be
     * truncated to this maximum length. Also, if performing exact-length processing
     * and the returned value is too short, then it will be right-padded with spaces
     * to match this length.</p>
     *
     * <p>The alwaysBlank field is a convenience flag for indicating whether the
     * returned value is always expected to be a blank one. It is meant as a
     * processing convenience to allow for buffer-appending shortcuts.</p>
     */
    abstract static class RecordPiece {
        // The name of the field represented by this piece.
        final String name;
        // The max string length of this piece.
        final int len;
        // Indicates whether this piece is always expected to have a blank value.
        final boolean alwaysBlank;

        /**
         * Constructs a new RecordPiece.
         *
         * @param name        The name of the field represented by this piece.
         * @param len         The max String length of this piece, for right-padding or truncation purposes.
         * @param alwaysBlank Indicates whether the String value of this piece is unconditionally blank.
         * @throws IllegalArgumentException if name is blank or len is non-positive.
         */
        RecordPiece(String name, int len, boolean alwaysBlank) {
            if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("name cannot be blank");
            } else if (len <= 0) {
                throw new IllegalArgumentException("len cannot be non-positive");
            }
            this.name = name;
            this.len = len;
            this.alwaysBlank = alwaysBlank;
        }

        /**
         * Retrieves the value to append, in String form.
         * The enclosing class will automatically replace blank values accordingly.
         * If the value is expected to always be blank, then setting the alwaysBlank flag
         * to true will improve efficiency since it will make the enclosing class skip this method.
         *
         * @return The value to append.
         * @throws SQLException if the piece attempts to retrieve its value from a database but a DB access error occurs.
         */
        abstract String getValue() throws SQLException;

        /**
         * Convenience method that allows subclasses to perform logging
         * or other processing when an appended value gets truncated.
         * The default implementation does nothing.
         */
        void notifyOfTruncatedValue() {
            // Do nothing.
        }
    }


    /**
     * Helper object encapsulating data about a specific section of tax input and output.
     */
    private static final class OutputHelper {
        // The output character buffer to write to.
        private final char[] outputBuffer;
        // The "piece" objects that will generate the String content to append to the buffer.
        private final SprintaxRowPrintProcessor.RecordPiece[] outputPieces;
        // Indicates whether the max lengths of the buffer and each "piece" should be treated as exact lengths.
        private final boolean useExactLengths;
        // Indicates whether a separator character should be added between the output of each "piece".
        private final boolean addSeparatorChar;
        // The separator character to use if addSeparatorChar is set to true.
        private final char separator;

        // The current insertion position for the character buffer.
        private int position;

        private OutputHelper(int bufferLength, List<? extends SprintaxRowPrintProcessor.RecordPiece> pieces, boolean useExactLengths, boolean addSeparatorChar, char separator) {
            this.outputBuffer = new char[bufferLength + (addSeparatorChar ? 1 : 0)];
            this.outputPieces = pieces.toArray(new SprintaxRowPrintProcessor.RecordPiece[pieces.size()]);
            this.useExactLengths = useExactLengths;
            this.addSeparatorChar = addSeparatorChar;
            this.separator = separator;
        }

    }

    static final class StaticStringRecordPiece extends SprintaxRowPrintProcessor.RecordPiece {
        String value;

        StaticStringRecordPiece(String name, int len, String value) {
            super(name, len, false);
            this.value = value;
        }

        @Override
        String getValue() throws SQLException {
            return value;
        }
    }


    String getReportsDirectory() {
        return reportsDirectory;
    }

    final void resetBuffer(int bufferIndex) {
        outputHelpers[bufferIndex].position = 0;
    }

    DateFormat buildDateFormatForFileSuffixes() {
        return new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
    }

    /**
     * Appends the getValue() String values of the matching "pieces" to the indicated buffer,
     * performing truncation, right-padding or separator-character-delimiting as needed.
     * Blank values will be treated as "" for not-exact-length processing, or " " for exact-length processing.
     * Values that are too long will be truncated to the piece's length.
     * If doing exact-length processing, values that are too short will be right-padded to the piece's length.
     * If adding separator characters and at least one piece is added, then a trailing separator character will also be appended.
     *
     * @param bufferIndex
     * @throws SQLException
     */
    final void appendPieces(int bufferIndex) throws SQLException {
        SprintaxRowPrintProcessor.OutputHelper helper = outputHelpers[bufferIndex];
        String tempValue;
        int tempLen;

        final String defaultStringIfBlank = helper.useExactLengths ? KRADConstants.BLANK_SPACE : KFSConstants.EMPTY_STRING;

        // Append the String values of the pieces in order, right-padding or truncating as needed.
        for (SprintaxRowPrintProcessor.RecordPiece piece : helper.outputPieces) {
            if (piece.alwaysBlank) {
                // If the value is unconditionally blank, then just fill with spaces as necessary.
                if (helper.useExactLengths) {
                    Arrays.fill(helper.outputBuffer, helper.position, helper.position + piece.len, ' ');
                    helper.position += piece.len;
                }

            } else {
                // Otherwise, prepare to append the value.
                tempValue = org.apache.commons.lang3.StringUtils.defaultIfBlank(piece.getValue(), defaultStringIfBlank);
                tempLen = tempValue.length();
                if (tempLen <= piece.len) {
                    // If not too long, use the whole value and right-pad with spaces as needed.
                    tempValue.getChars(0, tempLen, helper.outputBuffer, helper.position);
                    if (helper.useExactLengths && tempLen < piece.len) {
                        Arrays.fill(helper.outputBuffer, helper.position + tempLen, helper.position + piece.len, ' ');
                        helper.position += piece.len;
                    } else {
                        helper.position += tempLen;
                    }
                } else {
                    // If the value is too long, then truncate it and notify the piece.
                    tempValue.getChars(0, piece.len, helper.outputBuffer, helper.position);
                    piece.notifyOfTruncatedValue();
                    helper.position += piece.len;
                }
            }

            // If necessary, add a separator character.
            if (helper.addSeparatorChar) {
                helper.outputBuffer[helper.position] = helper.separator;
                helper.position++;
            }
        }

    }

    /**
     * Writes the contents of the specified char buffer to the given writer.
     * A newline character will also be appended to the output.
     * If the buffer has separator characters between each field,
     * then any trailing separator will be excluded.
     *
     * @param bufferIndex The index of the char buffer to read from.
     * @param writerIndex The index of the Writer instance to write to.
     * @throws IOException if an I/O error occurs.
     */
    final void writeBufferToOutput(int bufferIndex, int writerIndex) throws IOException {
        SprintaxRowPrintProcessor.OutputHelper helper = outputHelpers[bufferIndex];

        // Validate buffer length before proceeding.
        if (helper.useExactLengths) {
            // If exact-length output, make sure that the buffer has been completely written over with new content.
            if (helper.position != helper.outputBuffer.length) {
                throw new IllegalStateException("Buffer was not completely full but should have been! Index: " + Integer.toString(bufferIndex)
                        + ", expected buffer length: " + Integer.toString(helper.outputBuffer.length)
                        + ", actual buffer length: " + Integer.toString(helper.position));
            }
        } else if (helper.position == 0) {
            // If variable-length output, do not allow for outputting an empty buffer.
            throw new IllegalStateException("Buffer was empty but should not have been! Index: " + Integer.toString(bufferIndex));
        }

        // Send the buffer contents to the Writer, excluding the trailing separator character if one exists.
        writer.write(helper.outputBuffer, 0, helper.position - (helper.addSeparatorChar ? 1 : 0));
        writer.write('\n');
    }

    String[] getSqlForExtraStatements() {
        String[] extraSql = new String[extraStatements.length];
        if (extraSql.length >= CUTaxConstants.DEFAULT_EXTRA_RS_SIZE) {
            extraSql[CUTaxConstants.VENDOR_DETAIL_INDEX] = TaxSqlUtils.getVendorSelectSql(summary.vendorRow);
            extraSql[CUTaxConstants.DOC_NOTES_INDEX] = TaxSqlUtils.getDocNotesSelectSql(summary.documentNoteRow);
        }

        return extraSql;
    }

    Object[][] getDefaultParameterValuesForExtraStatement(int statementIndex) {
        if (VENDOR_US_ADDRESS_INDEX == statementIndex || VENDOR_FOREIGN_ADDRESS_INDEX == statementIndex) {
            // For vendor address statements, set defaults so that only the first two parameters need to be updated by the processing.
            return new Object[][] {
                    {Integer.valueOf(0)},
                    {Integer.valueOf(0)},
                    {KFSConstants.COUNTRY_CODE_UNITED_STATES}
            };
        } else {
            return null;
        }
    }

    void clearArraysAndReferences() {
        Arrays.fill(extraResultSets, null);
        Arrays.fill(extraStatements, null);
        writer = null;

        for (int i = 0; i < outputHelpers.length; i++) {
            if (outputHelpers[i] != null) {
                Arrays.fill(outputHelpers[i].outputBuffer, '0');
                Arrays.fill(outputHelpers[i].outputPieces, null);
                outputHelpers[i] = null;
            }
        }

        // Clear out subclass-specific data.
        clearReferences();
    }

    /**
     * Returns a map containing various statistics that were collected during the processing.
     * The default implementation creates and returns an empty map; subclasses can override
     * this method to add entries to the superclass's map.
     *
     * @return An EnumMap containing various numeric information on the processed results.
     */
    EnumMap<TaxStatType,Integer> getStatistics() {
        return new EnumMap<TaxStatType,Integer>(TaxStatType.class);
    }

    /**
     * Closes any non-null result sets, prepared statements,
     * and writer that have been stored by this processor,
     * and catches and logs any SQLException or IOException
     * errors instead of leaving them unhandled.
     */
    final void closeForFinallyBlock() {
        // Close any remaining ResultSets.
        for (ResultSet rs : extraResultSets) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOG.warn("Could not close result set");
                }
            }
        }

        // Close any extra PreparedStatements.
        for (PreparedStatement extraStatement : extraStatements) {
            if (extraStatement != null) {
                try {
                    extraStatement.close();
                } catch (SQLException e) {
                    LOG.warn("Could not close prepared statement");
                }
            }
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.warn("Could not close writer");
            }
        }
    }

    final void setWriter(Writer writer) {
        if (this.writer != null) {
            throw new IllegalStateException("A Writer is already defined");
        }
        this.writer = writer;
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
            throw new IllegalStateException("A PreparedStatement is already defined for index " + Integer.toString(statementIndex));
        }
        extraStatements[statementIndex] = extraStatement;
    }

    /**
     * Creates a new character buffer at the given index, and also
     * sets the array of "pieces" that will be used to build it
     * by copying the contents of the provided List.
     *
     * @param bufferIndex The index to associate with the new buffer.
     * @param bufferLength The size of the new buffer; cannot be negative.
     * @param pieces The "piece" objects that will be used to set the buffer's contents.
     * @param useExactLength Indicates whether the max lengths of the buffer and each "piece" should also be treated as exact lengths.
     * @param addSeparatorChar Indicates whether the buffer should separate "piece" output values with a delimiter character.
     * @param separator The character to use as the separator; will be ignored if addSeparatorChar is set to false.
     * @throws IllegalStateException if a character buffer already exists at the given index.
     */
    final void setupOutputBuffer(int bufferIndex, int bufferLength, List<SprintaxRowPrintProcessor.RecordPiece> pieces,
                                 boolean useExactLength, boolean addSeparatorChar, char separator) {
        if (outputHelpers[bufferIndex] != null) {
            throw new IllegalStateException("An output buffer is aleady set for index " + Integer.toString(bufferIndex));
        } else if (bufferLength < 0) {
            throw new IllegalArgumentException("bufferLength cannot be negative");
        }
        outputHelpers[bufferIndex] = new SprintaxRowPrintProcessor.OutputHelper(bufferLength, pieces, useExactLength, addSeparatorChar, separator);
    }

    static final class AlwaysBlankRecordPiece extends SprintaxRowPrintProcessor.RecordPiece {
        AlwaysBlankRecordPiece(String name, int len) {
            super(name, len, true);
        }

        @Override
        String getValue() throws SQLException {
            return null;
        }
    }

}
