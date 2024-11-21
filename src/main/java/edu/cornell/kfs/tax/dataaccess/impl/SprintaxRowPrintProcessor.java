package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.TaxOutputField;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.sys.KFSConstants;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SprintaxRowPrintProcessor {
    private static final Logger LOG = LogManager.getLogger(SprintaxRowPrintProcessor.class);

    private DerivedFieldDefinitionString ssnFieldDefinition;

    private ResultSet rsTransactionDetail;
    private Transaction1042SSummary summary;
    private List<SprintaxFieldDefinition> fieldDefinitions = new ArrayList<>();
    private Writer writer;

    SprintaxRowPrintProcessor(Transaction1042SSummary summary, TaxOutputDefinition outputDefinition) {
        this.summary = summary;
        buildFieldDefinitions(outputDefinition);
    }

    SprintaxFieldDefinition buildFieldDefinition(CUTaxBatchConstants.TaxFieldSource fieldSource, TaxTableField field, String name) {
        SprintaxFieldDefinition piece = null;

        if (fieldSource.equals(CUTaxBatchConstants.TaxFieldSource.DETAIL)) {
            if (summary.scrubbedOutput && field.index == summary.transactionDetailRow.vendorGIIN.index) {
                piece = new StaticStringFieldDefinition(name, CUTaxConstants.MASKED_VALUE_19_CHARS);
            } else {
                piece = SprintaxFieldDefinition.buildTransactionDetailFieldDefinition(field, name);
            }
        } else if (fieldSource.equals(CUTaxBatchConstants.TaxFieldSource.DERIVED)) {
            if (summary.derivedValues.ssn.equals(field)) {
                piece = ssnFieldDefinition = new DerivedFieldDefinitionString(name);
            } else {
                throw new IllegalArgumentException("Cannot create print-only piece for the given derived-field type");
            }
        }

        return piece;
    }

    void buildFieldDefinitions(TaxOutputDefinition outputDefinition) {

        for (TaxOutputField field : outputDefinition.getSections().get(0).getFields()) {
            CUTaxBatchConstants.TaxFieldSource fieldSource = CUTaxBatchConstants.TaxFieldSource.valueOf(field.getType());
            TaxTableField tableField = null;

            if (fieldSource.equals(CUTaxBatchConstants.TaxFieldSource.STATIC)) {
                fieldDefinitions.add(new StaticStringFieldDefinition(field.getName(), field.getValue()));
            } else if (fieldSource.equals(CUTaxBatchConstants.TaxFieldSource.DETAIL)) {
                tableField = summary.transactionDetailRow.getField(field.getValue());
            } else if (fieldSource.equals(CUTaxBatchConstants.TaxFieldSource.DERIVED)) {
                tableField = summary.derivedValues.getField(field.getValue());
            }

            if (tableField != null) {
                SprintaxFieldDefinition currentPiece = buildFieldDefinition(fieldSource, tableField, field.getValue());
                fieldDefinitions.add(currentPiece);
            }
        }
    }

    String getSqlForSelect() {
        TaxTableField fieldForWhereClause = summary.transactionDetailRow.form1042SBox;
        return TaxSqlUtils.getTransactionDetailSelectSql(fieldForWhereClause, summary.transactionDetailRow, false, true);
    }

    void processTaxRows(ResultSet rs, String outputFilepath) throws SQLException, IOException {
        buildWriter(outputFilepath);

        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        EncryptionService encryptionService = CoreApiServiceLocator.getEncryptionService();
        rsTransactionDetail = rs;
        if (summary.scrubbedOutput) {
            ssnFieldDefinition.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }

        LOG.info("Starting raw transaction row printing to file...");

        writer.write(CUTaxConstants.Sprintax.TRANSACTION_FILE_HEADER_ROW);
        writer.write('\n');

        while (rsTransactionDetail.next()) {

            if (!summary.scrubbedOutput) {
                try {
                    ssnFieldDefinition.value = encryptionService.decrypt(rsTransactionDetail.getString(detailRow.vendorTaxNumber.index));
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            writeLine();
        }

        LOG.info("Finished raw transaction row printing to file.");
    }

    void clearReferences() {
        rsTransactionDetail = null;
        ssnFieldDefinition = null;
    }

    void writeLine() throws SQLException, IOException {
        List<String> valuesToWrite = new ArrayList<>();

        for (SprintaxFieldDefinition fieldDefinition : fieldDefinitions) {
            String val = StringUtils.defaultIfBlank(fieldDefinition.getValue(rsTransactionDetail), KFSConstants.EMPTY_STRING);
            val = StringUtils.left(val, CUTaxConstants.Sprintax.MAX_FIELD_LENGTH);
            valuesToWrite.add(val);
        }

        String line = String.join(",", valuesToWrite);
        writer.write(line);
        writer.write("\n");
    }

    void clearArraysAndReferences() {
        writer = null;
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

}
