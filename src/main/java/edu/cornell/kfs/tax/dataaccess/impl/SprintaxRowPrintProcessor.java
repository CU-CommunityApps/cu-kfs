package edu.cornell.kfs.tax.dataaccess.impl;

import com.opencsv.CSVWriter;
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

import java.io.FileWriter;
import java.io.IOException;
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

    SprintaxRowPrintProcessor(Transaction1042SSummary summary, TaxOutputDefinition outputDefinition) {
        this.summary = summary;
        buildFieldDefinitions(outputDefinition);
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

    String getSqlForSelect() {
        TaxTableField fieldForWhereClause = summary.transactionDetailRow.form1042SBox;
        return TaxSqlUtils.getTransactionDetailSelectSql(fieldForWhereClause, summary.transactionDetailRow, false, true);
    }

    void processTaxRows(ResultSet rs, String outputFilepath) throws SQLException, IOException {

        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        EncryptionService encryptionService = CoreApiServiceLocator.getEncryptionService();
        rsTransactionDetail = rs;
        if (summary.scrubbedOutput) {
            ssnFieldDefinition.value = CUTaxConstants.MASKED_VALUE_9_CHARS;
        }

        LOG.info("Starting raw transaction row printing to file...");

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputFilepath))) {

            writer.writeNext(CUTaxConstants.Sprintax.TRANSACTION_FILE_HEADER_ROW.split(","));

            while (rsTransactionDetail.next()) {

                if (!summary.scrubbedOutput) {
                    try {
                        ssnFieldDefinition.value = encryptionService.decrypt(rsTransactionDetail.getString(detailRow.vendorTaxNumber.index));
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }

                String[] valuesToWrite = getValuesToWrite();
                writer.writeNext(valuesToWrite);
            }
        } finally {
            rsTransactionDetail = null;
            ssnFieldDefinition = null;
        }

        LOG.info("Finished raw transaction row printing to file.");
    }

    String[] getValuesToWrite() throws SQLException {
        List<String> valuesToWrite = new ArrayList<>();

        for (SprintaxFieldDefinition fieldDefinition : fieldDefinitions) {
            String val = StringUtils.defaultIfBlank(fieldDefinition.getValue(rsTransactionDetail), KFSConstants.EMPTY_STRING);
            val = StringUtils.left(val, CUTaxConstants.Sprintax.MAX_FIELD_LENGTH);
            valuesToWrite.add(val);
        }

        return valuesToWrite.toArray(String[]::new);
    }

}
