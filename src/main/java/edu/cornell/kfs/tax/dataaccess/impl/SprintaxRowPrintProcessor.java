package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
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
import java.util.Map;

public class SprintaxRowPrintProcessor {
    private static final Logger LOG = LogManager.getLogger(SprintaxRowPrintProcessor.class);

    private DerivedFieldDefinitionString ssnP;

    private ResultSet rsTransactionDetail;
    private Transaction1042SSummary summary;
    private SprintaxFieldDefinition[] fieldDefinitions;
    private Writer writer;

    SprintaxRowPrintProcessor(Transaction1042SSummary summary) {
        this.summary = summary;
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
                piece = new DerivedFieldDefinitionString(name);
            } else {
                throw new IllegalArgumentException("Cannot create print-only piece for the given derived-field type");
            }
        }

        return piece;
    }

    @SuppressWarnings("unchecked")
    void buildSsnFieldDefinition(Map<String, SprintaxFieldDefinition> complexPieces) {
        ssnP = (DerivedFieldDefinitionString) complexPieces.get(summary.derivedValues.ssn.propertyName);
    }

    String getSqlForSelect() {
        TaxTableField fieldForWhereClause = summary.transactionDetailRow.form1042SBox;
        return TaxSqlUtils.getTransactionDetailSelectSql(fieldForWhereClause, summary.transactionDetailRow, false, true);
    }

    void processTaxRows(ResultSet rs) throws SQLException, IOException {
        // Perform initialization as needed.
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        EncryptionService encryptionService = CoreApiServiceLocator.getEncryptionService();
        rsTransactionDetail = rs;
        if (summary.scrubbedOutput) {
            ssnP.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }

        LOG.info("Starting raw transaction row printing to file...");

        writer.write(CUTaxConstants.Sprintax.TRANSACTION_FILE_HEADER_ROW);
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

            writeLine();
        }

        LOG.info("Finished raw transaction row printing to file.");
    }

    void clearReferences() {
        rsTransactionDetail = null;
        ssnP = null;
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

    final void setFieldDefinitions(List<SprintaxFieldDefinition> fieldDefinitions) {
        this.fieldDefinitions = fieldDefinitions.toArray(new SprintaxFieldDefinition[fieldDefinitions.size()]);
    }

}
