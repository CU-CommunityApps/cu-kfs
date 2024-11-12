package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.TaxDataDefinition;
import edu.cornell.kfs.tax.batch.TaxDataRow;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.businessobject.SprintaxReportParameters;
import edu.cornell.kfs.tax.dataaccess.SprintaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.service.SprintaxProcessingService;
import edu.cornell.kfs.tax.service.TaxProcessingService;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SprintaxProcessingDaoJdbc extends TaxProcessingDaoJdbc implements SprintaxProcessingDao {
	private static final Logger LOG = LogManager.getLogger(SprintaxProcessingDaoJdbc.class);

    @Override
    public void doSprintaxProcessing(SprintaxReportParameters taxParameters) {
        SprintaxProcessingService sprintaxProcessingService = SpringContext.getBean(SprintaxProcessingService.class);

        TaxDataDefinition taxDataDefinition = sprintaxProcessingService.getDataDefinition(CUTaxKeyConstants.TAX_TABLE_1042S_PREFIX, taxParameters.getReportYear());
        Map<String, TaxDataRow> taxDataRowMap = taxDataDefinition.getDataRowsAsMap();
//        SprintaxPaymentSummary summary = new SprintaxPaymentSummary(taxParameters, taxDataRowMap);
        Transaction1042SSummary summary = new Transaction1042SSummary(taxParameters.getReportYear(), taxParameters.getStartDate(), taxParameters.getEndDate(), true, taxDataRowMap);

        String deleteRawTransactionDetailSql = "DELETE FROM TX_RAW_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteRawTransactionDetailSql, taxParameters.getReportYear());

        String deleteTransactionDetailSql = "DELETE FROM TX_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteTransactionDetailSql, taxParameters.getReportYear());

        List<Class<? extends TransactionRowBuilder<Transaction1042SSummary>>> transactionRowBuilders = Arrays.<Class<? extends TransactionRowBuilder<Transaction1042SSummary>>>asList(
                TransactionRowPdpBuilder.For1042S.class
//                TransactionRowDvBuilder.For1042S.class,
//                TransactionRowPRNCBuilder.For1042S.class
        );
        List<EnumMap<TaxStatType,Integer>> stats = createTransactionRows(summary, transactionRowBuilders);

        EnumMap<TaxStatType, Integer> processingStats = processPayments(summary, taxParameters.getReportYear(), sprintaxProcessingService);
        stats.add(processingStats);


        TaxOutputDefinition paymentsOutputDefinition = sprintaxProcessingService.get1042PaymentsOutputDefinition();
        printTransactionRows(taxParameters.getJobRunDate(), summary, PaymentRowPrintProcessor.For1042S.class, paymentsOutputDefinition);

        TaxOutputDefinition bioOutputDefinition = sprintaxProcessingService.get1042BioOutputDefinition();
        printBiographicRows(taxParameters.getJobRunDate(), summary, PaymentRowPrintProcessor.For1042S.class, bioOutputDefinition);

        printStatistics(stats);
    }

    private <T extends TransactionDetailSummary> EnumMap<TaxStatType,Integer> processPayments(Transaction1042SSummary summary, int reportYear,
                                                                                              SprintaxProcessingService sprintaxProcessingService) {
        TaxOutputDefinition taxOutputDefinition = sprintaxProcessingService.getOutputDefinition("tax.format.1042s.", reportYear);
        TransactionRowProcessorBuilder transactionRowProcessorBuilder = TransactionRowProcessorBuilder.createBuilder();
        TransactionRow1042SProcessor processor = transactionRowProcessorBuilder.buildNewProcessor(TransactionRow1042SProcessor.class, taxOutputDefinition, summary);

        processor.setReportsDirectory(getReportsDirectory());

        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {

                String selectSql = processor.getSqlForSelect(summary);
                PreparedStatement preparedSelectStatement = con.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                Object[][] selectParameters = processor.getParameterValuesForSelect(summary);
                setParameters(preparedSelectStatement, selectParameters);

                String[] sqlForExtraStatements = processor.getSqlForExtraStatements(summary);
                for (int i = 0; i < sqlForExtraStatements.length; i++) {
                    PreparedStatement tempStatement = con.prepareStatement(sqlForExtraStatements[i]);
                    Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i, summary);
                    if (defaultArgs != null) {
                        setParameters(tempStatement, defaultArgs);
                    }
                    processor.setExtraStatement(tempStatement, i);
                }

                try {

                    java.util.Date processingStartDate = new java.util.Date();
                    String[] filePaths = processor.getFilePathsForWriters(summary, processingStartDate);
                    for (int i = 0; i < filePaths.length; i++) {
                        String filePath = filePaths[i];
                        File outputFile = new File(filePath);
                        PrintWriter printWriter = new PrintWriter(outputFile, StandardCharsets.UTF_8);
                        Writer bufferedWriter = new BufferedWriter(printWriter);
                        processor.setWriter(bufferedWriter, i);
                    }

                    ResultSet transactionDetailRecords = preparedSelectStatement.executeQuery();
                    processor.processTaxRows(transactionDetailRecords, summary);

                } catch (IOException e) {
                    LOG.error(e.toString());
                }

                return processor.getStatistics();
            }
        });

    }

    private <T extends TransactionDetailSummary> EnumMap<TaxStatType,Integer> printTransactionRows(java.util.Date processingStartDate, T summary, Class<? extends TransactionRowProcessor<T>> processorClazz, TaxOutputDefinition outputDefinition) {
        // Create the object that will handle the processing of the transaction row data.
        final TransactionRowProcessor<T> processor = TransactionRowProcessorBuilder.createBuilder().buildNewProcessor(
                processorClazz, outputDefinition, summary);
        processor.setReportsDirectory(getReportsDirectory());

        // Use a ConnectionCallback via a JdbcTemplate to simplify the batch processing and transaction management.
        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet rs = null;
                PreparedStatement tempStatement = null;
                Writer tempWriter = null;

                try {

                    selectStatement = con.prepareStatement(processor.getSqlForSelect(summary), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    setParameters(selectStatement, processor.getParameterValuesForSelect(summary));

                    // Prepare any other statements needed by the tax processing.
                    String[] tempValues = processor.getSqlForExtraStatements(summary);
                    for (int i = 0; i < tempValues.length; i++) {
                        tempStatement = con.prepareStatement(tempValues[i]);
                        Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i, summary);
                        if (defaultArgs != null) {
                            setParameters(tempStatement, defaultArgs);
                        }
                        processor.setExtraStatement(tempStatement, i);
                    }
                    tempStatement = null;

                    List<String> filePathsForWriters = getPaymentsCsvFilePath(summary.reportYear, processingStartDate);
                    for (int i = 0; i < filePathsForWriters.size(); i++) {
                        tempWriter = new BufferedWriter(new PrintWriter(new File(filePathsForWriters.get(i)), StandardCharsets.UTF_8));
                        processor.setWriter(tempWriter, i);
                    }
                    tempWriter = null;

                    // Get the transaction detail rows.
                    rs = selectStatement.executeQuery();

                    // Perform the actual processing.
                    processor.processTaxRows(rs, summary);

                    // Return the collected statistics.
                    return processor.getStatistics();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Close resources, handling exceptions as needed.
                    processor.closeForFinallyBlock();

                    if (tempWriter != null) {
                        // If an error occurred passing a Writer to the processor, then close it here.
                        try {
                            tempWriter.close();
                        } catch (IOException e) {
                            LOG.error("Could not close file writer");
                        }
                    }

                    if (tempStatement != null) {
                        // If an error occurred passing a PreparedStatement to the processor, then close it here.
                        try {
                            tempStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close extra tax processing statement");
                        }
                    }

                    if (rs != null) {
                        try {
                            rs.close();
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
    private <T extends TransactionDetailSummary> EnumMap<TaxStatType,Integer> printBiographicRows(java.util.Date processingStartDate, T summary, Class<? extends TransactionRowProcessor<T>> processorClazz, TaxOutputDefinition outputDefinition) {
        // Create the object that will handle the processing of the transaction row data.
        final TransactionRowProcessor<T> processor = TransactionRowProcessorBuilder.createBuilder().buildNewProcessor(processorClazz, outputDefinition, summary);
        processor.setReportsDirectory(getReportsDirectory());

        // Use a ConnectionCallback via a JdbcTemplate to simplify the batch processing and transaction management.
        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet rs = null;
                PreparedStatement tempStatement = null;
                Writer tempWriter = null;

                try {

                    selectStatement = con.prepareStatement(processor.getSqlForSelect(summary), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    setParameters(selectStatement, processor.getParameterValuesForSelect(summary));

                    // Prepare any other statements needed by the tax processing.
                    String[] tempValues = processor.getSqlForExtraStatements(summary);
                    for (int i = 0; i < tempValues.length; i++) {
                        tempStatement = con.prepareStatement(tempValues[i]);
                        Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i, summary);
                        if (defaultArgs != null) {
                            setParameters(tempStatement, defaultArgs);
                        }
                        processor.setExtraStatement(tempStatement, i);
                    }
                    tempStatement = null;

                    List<String> filePathsForWriters = getBiographicCsvFilePath(summary.reportYear, processingStartDate);
                    for (int i = 0; i < filePathsForWriters.size(); i++) {
                        tempWriter = new BufferedWriter(new PrintWriter(new File(filePathsForWriters.get(i)), StandardCharsets.UTF_8));
                        processor.setWriter(tempWriter, i);
                    }
                    tempWriter = null;

                    // Get the transaction detail rows.
                    rs = selectStatement.executeQuery();

                    // Perform the actual processing.
                    processor.processTaxRows(rs, summary);

                    // Return the collected statistics.
                    return processor.getStatistics();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    // Close resources, handling exceptions as needed.
                    processor.closeForFinallyBlock();

                    if (tempWriter != null) {
                        // If an error occurred passing a Writer to the processor, then close it here.
                        try {
                            tempWriter.close();
                        } catch (IOException e) {
                            LOG.error("Could not close file writer");
                        }
                    }

                    if (tempStatement != null) {
                        // If an error occurred passing a PreparedStatement to the processor, then close it here.
                        try {
                            tempStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close extra tax processing statement");
                        }
                    }

                    if (rs != null) {
                        try {
                            rs.close();
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

    List<String> getPaymentsCsvFilePath(int reportYear, java.util.Date processingStartDate) {
        List<String> ret = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);

        String filePathForPaymentsCsv = getReportsDirectory()
                + "/"
                + CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX
                + reportYear
                + dateFormat.format(processingStartDate)
                + CUTaxConstants.Sprintax.TAX_CSV_FILE_SUFFIX;

        ret.add(filePathForPaymentsCsv);
        return ret;
    }

    List<String> getBiographicCsvFilePath(int reportYear, java.util.Date processingStartDate) {
        List<String> ret = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);

        String filePathForPaymentsCsv = getReportsDirectory()
                + "/"
                + CUTaxConstants.Sprintax.BIO_OUTPUT_FILE_PREFIX
                + reportYear
                + dateFormat.format(processingStartDate)
                + CUTaxConstants.Sprintax.TAX_CSV_FILE_SUFFIX;

        ret.add(filePathForPaymentsCsv);
        return ret;
    }

}
