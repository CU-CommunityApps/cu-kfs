package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants;
import edu.cornell.kfs.tax.batch.TaxDataDefinition;
import edu.cornell.kfs.tax.batch.TaxDataRow;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.batch.TaxOutputField;
import edu.cornell.kfs.tax.batch.TaxOutputSection;
import edu.cornell.kfs.tax.businessobject.SprintaxReportParameters;
import edu.cornell.kfs.tax.dataaccess.SprintaxProcessingDao;
import edu.cornell.kfs.tax.service.SprintaxProcessingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.jdbc.core.ConnectionCallback;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SprintaxProcessingDaoJdbc extends TaxProcessingDaoJdbc implements SprintaxProcessingDao {
	private static final Logger LOG = LogManager.getLogger(SprintaxProcessingDaoJdbc.class);

    @Override
    public void doSprintaxProcessing(SprintaxReportParameters taxParameters) {
        SprintaxProcessingService taxProcessingService = SpringContext.getBean(SprintaxProcessingService.class);

        Transaction1042SSummary summary = buildTransaction1042SSummary(taxProcessingService, taxParameters);

        clearTaxTransactionRecords(taxParameters.getReportYear());

        List<EnumMap<TaxStatType,Integer>> stats = generateTransactionRecords(summary);

        EnumMap<TaxStatType, Integer> processingStats = processTransactionRows(summary, taxProcessingService);
        stats.add(processingStats);

        printTransactionRows(taxParameters.getJobRunDate(), summary, taxProcessingService);

        printStatistics(stats);
    }

    List<EnumMap<TaxStatType,Integer>> generateTransactionRecords(Transaction1042SSummary summary) {
        LOG.info("Creating Transaction Records using parent TaxProcessingDaoJdbc");

        List<Class<? extends TransactionRowBuilder<Transaction1042SSummary>>> transactionRowBuilders = Arrays.asList(
                TransactionRowPdpBuilder.For1042S.class
//                TransactionRowDvBuilder.For1042S.class,
//                TransactionRowPRNCBuilder.For1042S.class
        );
        List<EnumMap<TaxStatType,Integer>> stats = super.createTransactionRows(summary, transactionRowBuilders);
        return stats;
    }

    void clearTaxTransactionRecords(int reportYear) {
        LOG.info("Deleting Tax Transaction Records for Year " + reportYear);

        String deleteRawTransactionDetailSql = "DELETE FROM TX_RAW_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteRawTransactionDetailSql, reportYear);

        String deleteTransactionDetailSql = "DELETE FROM TX_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteTransactionDetailSql, reportYear);
    }

    Transaction1042SSummary buildTransaction1042SSummary(SprintaxProcessingService sprintaxProcessingService, SprintaxReportParameters taxParameters) {
        LOG.info("Preparing data for processing");

        TaxDataDefinition taxDataDefinition = sprintaxProcessingService.getDataDefinition(CUTaxKeyConstants.TAX_TABLE_1042S_PREFIX, taxParameters.getReportYear());
        Map<String, TaxDataRow> taxDataDefinitionMap = taxDataDefinition.getDataRowsAsMap();

        Transaction1042SSummary summary = new Transaction1042SSummary(
                taxParameters.getReportYear(),
                taxParameters.getStartDate(),
                taxParameters.getEndDate(),
                true,
                taxDataDefinitionMap
        );
        return  summary;
    }

    private EnumMap<TaxStatType,Integer> processTransactionRows(Transaction1042SSummary summary, SprintaxProcessingService sprintaxProcessingService) {
        LOG.info("Processing Transaction Records and Generating Biographic CSV File");

        TaxOutputDefinition taxOutputDefinition = sprintaxProcessingService.getSprintaxOutputDefinition("SprintaxBioOutputDefinition.xml");
        SprintaxPaymentRowProcessor processor = buildNewProcessor(taxOutputDefinition, summary);

        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {

                String selectSql = processor.getSqlForSelect();
                PreparedStatement preparedSelectStatement = con.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Object[][] selectParameters = processor.getParameterValuesForSelect();
                setParameters(preparedSelectStatement, selectParameters);

                String[] sqlForExtraStatements = processor.getSqlForExtraStatements();
                for (int i = 0; i < sqlForExtraStatements.length; i++) {
                    PreparedStatement tempStatement = con.prepareStatement(sqlForExtraStatements[i]);
                    Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i);
                    if (defaultArgs != null) {
                        setParameters(tempStatement, defaultArgs);
                    }
                    processor.setExtraStatement(tempStatement, i);
                }

                try {
                    Writer writer = buildBufferedWriterForBioFile();

                    ResultSet transactionDetailRecords = preparedSelectStatement.executeQuery();
                    processor.processTaxRows(writer, transactionDetailRecords);

                } catch (IOException e) {
                    LOG.error(e.toString());
                }

                return processor.getStatistics();
            }
        });

    }

    Writer buildBufferedWriterForBioFile() throws IOException {
        java.util.Date processingStartDate = new java.util.Date();
        String filePath = getFilePathForBioWriter(processingStartDate);
        File outputFile = new File(filePath);
        PrintWriter printWriter = new PrintWriter(outputFile, StandardCharsets.UTF_8);
        Writer bufferedWriter = new BufferedWriter(printWriter);

        bufferedWriter.write(CUTaxConstants.Sprintax.BIO_HEADER_ROW);
        bufferedWriter.write("\n");
        bufferedWriter.flush();

        return bufferedWriter;
    }

    String getFilePathForBioWriter(java.util.Date processingStartDate) {
        DateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        String bioFilePath = getReportsDirectory() + "/" + CUTaxConstants.Sprintax.BIO_OUTPUT_FILE_PREFIX + dateFormat.format(processingStartDate) + ".csv";
        return bioFilePath;
    }

    private void printTransactionRows(java.util.Date processingStartDate, Transaction1042SSummary summary, SprintaxProcessingService sprintaxService) {
        LOG.info("Printing to csv files");

        TaxOutputDefinition outputDefinition = sprintaxService.getSprintaxOutputDefinition("SprintaxTransactionOutputDefinition.xml");
        SprintaxRowPrintProcessor processor = new SprintaxRowPrintProcessor(summary, outputDefinition);
        String outputFilePath = getPaymentsCsvFilePath(summary.reportYear, processingStartDate);

        getJdbcTemplate().execute(new ConnectionCallback<String>() {
            @Override
            public String doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet transactionDetailRecords = null;

                try {

                    String selectSql = processor.getSqlForSelect();
                    selectStatement = con.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

                    selectStatement.setInt(1, summary.reportYear);
                    selectStatement.setString(2, "?");

                    transactionDetailRecords = selectStatement.executeQuery();

                    processor.processTaxRows(transactionDetailRecords, outputFilePath);

                    return "success";
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Close resources, handling exceptions as needed.
                    processor.closeForFinallyBlock();

                    if (transactionDetailRecords != null) {
                        try {
                            transactionDetailRecords.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close transaction row ResultSet");
                        }
                    }

                    if (selectStatement != null) {
                        try {
                            selectStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close transaction row selection statement");
                        }
                    }

                    processor.clearArraysAndReferences();
                }
            }
        });
    }

    String getPaymentsCsvFilePath(int reportYear, java.util.Date processingStartDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);

        String filePathForPaymentsCsv = getReportsDirectory()
                + "/"
                + CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX
                + reportYear
                + dateFormat.format(processingStartDate)
                + CUTaxConstants.Sprintax.TAX_CSV_FILE_SUFFIX;

        return filePathForPaymentsCsv;
    }

    SprintaxPaymentRowProcessor buildNewProcessor(TaxOutputDefinition outputDefinition, Transaction1042SSummary summary) {
        Map<String, SprintaxPaymentRowProcessor.RecordPiece> complexPieces = new HashMap<>();
        EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> minimumPieces = new EnumMap<>(CUTaxBatchConstants.TaxFieldSource.class);

        // Check for at least one output section.
        if (outputDefinition.getSections().size() != 1) {
            throw new IllegalArgumentException("outputDefinition should have 1 section");
        }

        SprintaxPaymentRowProcessor rowProcessor = new SprintaxPaymentRowProcessor(summary);

        // Determine the minimum "piece" objects that need to be created for each type (excluding BLANK and STATIC).
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DETAIL, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DETAIL));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DERIVED, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DERIVED));



        // Create the "piece" objects for the section and add them to the processor.
        TaxOutputSection section = outputDefinition.getSections().get(0);
        if (section.getFields().isEmpty()) {
            throw new RuntimeException("Cannot have empty sections!");
        } else if (section.getLength() == null) {
            throw new RuntimeException("Cannot have section with unspecified max length!");
        } else if (section.isHasSeparators() && section.getSeparator() == null) {
            throw new RuntimeException("Cannot have a null separator for a section with separator-delimited fields!");
        }
        List<SprintaxPaymentRowProcessor.RecordPiece> pieces = new ArrayList<>();

        for (TaxOutputField field : section.getFields()) {
            if (StringUtils.isBlank(field.getName())) {
                throw new RuntimeException("Cannot have field with blank name");
            } else if (StringUtils.isBlank(field.getType())) {
                throw new RuntimeException("Cannot have field with blank type");
            } else if (field.getLength() == null) {
                throw new RuntimeException("Cannot have field with null length");
            }

            CUTaxBatchConstants.TaxFieldSource fieldSource = CUTaxBatchConstants.TaxFieldSource.valueOf(field.getType());


            if (fieldSource == CUTaxBatchConstants.TaxFieldSource.STATIC) {
//                StaticStringFieldDefinition fieldDefinition = new StaticStringFieldDefinition(field.getName(), field.getValue());
//                pieces.add(fieldDefinition);
                SprintaxPaymentRowProcessor.StaticStringRecordPiece fieldDef = new SprintaxPaymentRowProcessor.StaticStringRecordPiece(field.getName(), field.getValue());
                continue;
            }

            TaxTableField tableField = null;

            if (fieldSource == CUTaxBatchConstants.TaxFieldSource.DETAIL) {
                tableField = summary.transactionDetailRow.getField(field.getValue());
            } else if (fieldSource == CUTaxBatchConstants.TaxFieldSource.DERIVED) {
                tableField = summary.derivedValues.getField(field.getValue());
            } else if (fieldSource == CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS) {
                tableField = summary.vendorAddressRow.getField(field.getValue());
            } else {
                throw new IllegalStateException("Unrecognized piece type for field");
            }

            String pieceKey = tableField.propertyName;

            // Create a new "piece" or re-use an existing one for duplicates as needed.
            SprintaxPaymentRowProcessor.RecordPiece currentPiece = complexPieces.get(pieceKey);

            if (currentPiece == null) {
                currentPiece = rowProcessor.getPieceForField(fieldSource, tableField, field.getName());
                minimumPieces.get(fieldSource).remove(tableField);
                complexPieces.put(pieceKey, currentPiece);
            }

            pieces.add(currentPiece);

        }

        rowProcessor.setupOutputBuffer(pieces);

        // If the processor has defined some minimum fields, but they have not been created yet, then create them.
        for (Map.Entry<CUTaxBatchConstants.TaxFieldSource,Set<TaxTableField>> minTypeSpecificPieces : minimumPieces.entrySet()) {
            if (minTypeSpecificPieces.getValue() != null) {
                for (TaxTableField minPiece : minTypeSpecificPieces.getValue()) {
                    SprintaxPaymentRowProcessor.RecordPiece field = rowProcessor.getPieceForField(minTypeSpecificPieces.getKey(), minPiece, "autoGen" + minPiece.columnName);
                    complexPieces.put(minPiece.propertyName, field);
                }
            }
        }

        rowProcessor.setComplexPieces(complexPieces);

        return rowProcessor;
    }

}
