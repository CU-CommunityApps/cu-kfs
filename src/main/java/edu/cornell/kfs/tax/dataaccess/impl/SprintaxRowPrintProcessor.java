package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SprintaxRowPrintProcessor {
    private static final Logger LOG = LogManager.getLogger(SprintaxRowPrintProcessor.class);

    private SprintaxRowPrintProcessor.DerivedFieldStringPiece ssnP;

    private ResultSet rsTransactionDetail;
    private Transaction1042SSummary summary;
    private SprintaxRowPrintProcessor.OutputHelper outputHelper;
    private Writer writer;

    SprintaxRowPrintProcessor(Transaction1042SSummary summary) {
        this.summary = summary;
    }

    SprintaxRowPrintProcessor.RecordPiece getPieceForField(CUTaxBatchConstants.TaxFieldSource fieldSource, TaxTableField field, String name) {
        SprintaxRowPrintProcessor.RecordPiece piece;

        switch (fieldSource) {
            case BLANK :
                throw new IllegalArgumentException("Cannot create piece for BLANK type");

            case STATIC :
                throw new IllegalArgumentException("Cannot create piece for STATIC type");

            case DETAIL :
                // Create a piece that pre-loads its value from the transaction detail ResultSet line, or a static masked piece if GIIN and in scrubbed mode.
                if (summary.scrubbedOutput && field.index == summary.transactionDetailRow.vendorGIIN.index) {
                    piece = new SprintaxRowPrintProcessor.StaticStringRecordPiece(name, CUTaxConstants.MASKED_VALUE_19_CHARS);
                } else {
                    piece = getPieceForDetailField(field, name);
                }
                break;

            case DERIVED :
                if (summary.derivedValues.ssn.equals(field)) {
                    piece = new SprintaxRowPrintProcessor.DerivedFieldStringPiece(name);
                } else {
                    throw new IllegalArgumentException("Cannot create print-only piece for the given derived-field type");
                }
                break;

            default :
                throw new IllegalArgumentException("Unrecognized piece type");
        }

        return piece;
    }

    private SprintaxRowPrintProcessor.RecordPiece getPieceForDetailField(TaxTableField detailField, String name) {
        SprintaxRowPrintProcessor.RecordPiece recordPiece;

        switch (detailField.jdbcType) {
            case java.sql.Types.DECIMAL :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailBigDecimalPiece(name, detailField.index);
                break;

            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailIntPiece(name, detailField.index);
                break;

            case java.sql.Types.VARCHAR :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailStringPiece(name, detailField.index);
                break;

            case java.sql.Types.DATE :
                recordPiece = new SprintaxRowPrintProcessor.TransactionDetailDatePiece(name, detailField.index);
                break;

            default :
                throw new IllegalStateException("Unrecognized field datatype");
        }

        return recordPiece;
    }

    @SuppressWarnings("unchecked")
    void buildSsnFieldDefinition(Map<String, SprintaxRowPrintProcessor.RecordPiece> complexPieces) {
        ssnP = (SprintaxRowPrintProcessor.DerivedFieldStringPiece) complexPieces.get(summary.derivedValues.ssn.propertyName);
    }

    String getSqlForSelect() {
        TaxTableField fieldForWhereClause = summary.transactionDetailRow.form1042SBox;
        return TaxSqlUtils.getTransactionDetailSelectSql(fieldForWhereClause, summary.transactionDetailRow, false, true);
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

        writer.write("E-mail,Student number,UNIQUE FORM IDENTIFIER,Is the current version amendment,The current number of amendment,Income Code,Gross Income,Chapter 3 Indicator,Chapter 3 Exemption Code,Chapter 3 Tax Rate,Chapter 4 Exemption Code,Chapter 4 Tax Rate,Withholding Allowance,Net Income,Federal Tax Withheld,Check if tax not deposited with IRS pursuant to escrow prodecure,Check if withholding occurred in subsequent year with respect to a partnership interest,Tax withheld by other agents,Overwithheld Tax Repaid to Recepient,Total withholding credit,Tax paid by withholding agent (amounts not withheld),Withholding agent's GIIN,Recipient Chapter 3 status code,Recipient Chapter 4 status code,Recipient's GIIN,LOB code,Recipient's account number,Primary Withholding Agent's Name (if applicapable),Primary Withholding Agent's EIN,Pro-rata Reporting,Intermediary flow-through's name,Intermediary's or FTE's Chapter 3 Status Code,Intermediary's or FTE's Chapter 4 Status Code,Intermediary/Flow-Through's Name,Intermediary or FTE GIIN,NQI/FLW-THR/PTP Country Code,Recipient's Foreign Tax I.D. Number,NQI/FLW-THR/PTP Address Line,NQI/FLW-THR/PTP City,Payer's Name,Payer's TIN,Payer's GIIN,Payer's Chapter 3 Status Code,Payer's Chapter 4 Status Code,State income tax withheld,Payer's State Tax Number,Name of State");
        writer.write('\n');

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

            // Do the printing.
            outputHelper.position = 0;
            appendPieces();
            writeBufferToOutput();

        }

        LOG.info("Finished raw transaction row printing to file.");
    }

    void clearReferences() {
        rsTransactionDetail = null;
        ssnP = null;
    }

    private final class TransactionDetailStringPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailStringPiece(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            String stringValue = rsTransactionDetail.getString(columnIndex);
            return StringUtils.replace(stringValue, ",",  " ");
        }
    }

    private final class TransactionDetailIntPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailIntPiece(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            return Integer.toString(rsTransactionDetail.getInt(columnIndex));
        }
    }

    private final class TransactionDetailBigDecimalPiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailBigDecimalPiece(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            BigDecimal val = rsTransactionDetail.getBigDecimal(columnIndex);
            return (val != null) ? val.toPlainString() : null;
        }
    }

    abstract static class IndexedColumnRecordPiece extends SprintaxRowPrintProcessor.RecordPiece {
        final int columnIndex;

        IndexedColumnRecordPiece(String name, int columnIndex) {
            super(name);
            this.columnIndex = columnIndex;
        }
    }

    private final class TransactionDetailDatePiece extends SprintaxRowPrintProcessor.IndexedColumnRecordPiece {

        private TransactionDetailDatePiece(String name, int columnIndex) {
            super(name, columnIndex);
        }

        @Override
        String getValue() throws SQLException {
            java.sql.Date val = rsTransactionDetail.getDate(columnIndex);
            // Return the date in yyyy-mm-dd format.
            return val != null ? val.toString() : null;
        }
    }

    private final class DerivedFieldStringPiece extends SprintaxRowPrintProcessor.RecordPiece {
        private String value;

        private DerivedFieldStringPiece(String name) {
            super(name);
        }

        @Override
        String getValue() throws SQLException {
            return value;
        }
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

        /**
         * Constructs a new RecordPiece.
         *
         * @param name        The name of the field represented by this piece.
         * @throws IllegalArgumentException if name is blank or len is non-positive.
         */
        RecordPiece(String name) {
            this.name = name;
        }

        abstract String getValue() throws SQLException;

        void notifyOfTruncatedValue() {

        }
    }


    /**
     * Helper object encapsulating data about a specific section of tax input and output.
     */
    private static final class OutputHelper {
        private final char[] outputBuffer;
        private final SprintaxRowPrintProcessor.RecordPiece[] outputPieces;
        private char separator = ',';

        // The current insertion position for the character buffer.
        private int position;

        private OutputHelper(int bufferLength, List<SprintaxRowPrintProcessor.RecordPiece> pieces, char separator) {
            this.outputBuffer = new char[bufferLength + 1];
            this.outputPieces = pieces.toArray(new SprintaxRowPrintProcessor.RecordPiece[pieces.size()]);
            this.separator = separator;
        }

    }

    static final class StaticStringRecordPiece extends SprintaxRowPrintProcessor.RecordPiece {
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

    /**
     * Appends the getValue() String values of the matching "pieces" to the indicated buffer,
     * performing truncation, right-padding or separator-character-delimiting as needed.
     * Blank values will be treated as "" for not-exact-length processing, or " " for exact-length processing.
     * Values that are too long will be truncated to the piece's length.
     * If doing exact-length processing, values that are too short will be right-padded to the piece's length.
     * If adding separator characters and at least one piece is added, then a trailing separator character will also be appended.
     *
     * @throws SQLException
     */
    final void appendPieces() throws SQLException {
        String val;
        int len;

        // Append the String values of the pieces in order, right-padding or truncating as needed.
        for (SprintaxRowPrintProcessor.RecordPiece piece : outputHelper.outputPieces) {

            val = StringUtils.defaultIfBlank(piece.getValue(), KFSConstants.EMPTY_STRING);
            len = val.length();
            if (len <= 90) {
                // If not too long, use the whole value and right-pad with spaces as needed.
                val.getChars(0, len, outputHelper.outputBuffer, outputHelper.position);
                outputHelper.position += len;
            } else {
                // If the value is too long, then truncate it and notify the piece.
                val.getChars(0, 90, outputHelper.outputBuffer, outputHelper.position);
                piece.notifyOfTruncatedValue();
                outputHelper.position += 90;
            }

            outputHelper.outputBuffer[outputHelper.position] = outputHelper.separator;
            outputHelper.position++;
        }

    }

    /**
     * Writes the contents of the specified char buffer to the given writer.
     * A newline character will also be appended to the output.
     * Trailing separator will be excluded.
     *
     * @throws IOException if an I/O error occurs.
     */
    final void writeBufferToOutput() throws IOException {

        // Send the buffer contents to the Writer, excluding the trailing separator character if one exists.
        writer.write(outputHelper.outputBuffer, 0, outputHelper.position - 1);
        writer.write('\n');
    }

    void clearArraysAndReferences() {
        writer = null;

        if (outputHelper != null) {
            Arrays.fill(outputHelper.outputBuffer, '0');
            Arrays.fill(outputHelper.outputPieces, null);
            outputHelper = null;
        }

        // Clear out subclass-specific data.
        clearReferences();
    }

    final void closeForFinallyBlock() {

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                LOG.warn("Could not close writer");
            }
        }
    }

    void buildWriter(String filePathForWriter) throws IOException {
        this.writer = new BufferedWriter(new PrintWriter(filePathForWriter, StandardCharsets.UTF_8));
    }

    /**
     * Creates a new character buffer at the given index, and also
     * sets the array of "pieces" that will be used to build it
     * by copying the contents of the provided List.
     *
     * @param bufferLength The size of the new buffer; cannot be negative.
     * @param pieces The "piece" objects that will be used to set the buffer's contents.
     * @param separator The character to use as the separator
     * @throws IllegalStateException if a character buffer already exists at the given index.
     */
    final void buildOutputBuffer(int bufferLength, List<SprintaxRowPrintProcessor.RecordPiece> pieces, char separator) {
        outputHelper = new SprintaxRowPrintProcessor.OutputHelper(bufferLength, pieces, separator);
    }

}
