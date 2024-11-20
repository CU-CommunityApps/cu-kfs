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
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.service.SprintaxProcessingService;
import edu.cornell.kfs.tax.service.TaxProcessingService;
import org.apache.commons.lang.StringUtils;
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

        LOG.info("Preparing data for processing");
        Transaction1042SSummary summary = buildSprintaxSummary(taxProcessingService, taxParameters);


        String deleteRawTransactionDetailSql = "DELETE FROM TX_RAW_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteRawTransactionDetailSql, taxParameters.getReportYear());

        String deleteTransactionDetailSql = "DELETE FROM TX_TRANSACTION_DETAIL_T WHERE REPORT_YEAR = ? AND FORM_1042S_BOX IS NOT NULL";
        getJdbcTemplate().update(deleteTransactionDetailSql, taxParameters.getReportYear());

        LOG.info("Creating Transaction Records");
        List<Class<? extends TransactionRowBuilder<Transaction1042SSummary>>> transactionRowBuilders = Arrays.asList(
                TransactionRowPdpBuilder.For1042S.class,
                TransactionRowDvBuilder.For1042S.class,
                TransactionRowPRNCBuilder.For1042S.class
        );
        List<EnumMap<TaxStatType,Integer>> stats = createSprintaxTransactionRows(summary, transactionRowBuilders);

        LOG.info("Processing Transaction Records and generating Biographic csv file");
        EnumMap<TaxStatType, Integer> processingStats = processTransactionRows(summary, taxProcessingService);
        stats.add(processingStats);

        LOG.info("Printing to csv files");
        TaxOutputDefinition paymentsOutputDefinition = taxProcessingService.getSprintaxOutputDefinition("SprintaxTransactionOutputDefinition.xml");
        printTransactionRows(taxParameters.getJobRunDate(), summary, paymentsOutputDefinition);

        printStatistics(stats);
    }

    public Transaction1042SSummary buildSprintaxSummary(SprintaxProcessingService sprintaxProcessingService, SprintaxReportParameters taxParameters) {
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

    protected  <T extends TransactionDetailSummary> List<EnumMap<TaxStatType,Integer>> createSprintaxTransactionRows(T summary, List<Class<? extends TransactionRowBuilder<T>>> builderClasses) {
        final TaxProcessingDao currentDao = this;

        // Use a ConnectionCallback via a JdbcTemplate to simplify the batch processing and transaction management.
        return getJdbcTemplate().execute(new ConnectionCallback<List<EnumMap<TaxStatType,Integer>>>() {
            @Override
            public List<EnumMap<TaxStatType,Integer>> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                PreparedStatement rawTransactionInsertStatement = null;
                PreparedStatement selectRawTransactionStatement = null;
                PreparedStatement secondPassTransactionInsertStatement = null;
                ResultSet rs = null;
                ResultSet rawDataTableResultSet = null;
                List<EnumMap<TaxStatType,Integer>> stats = new ArrayList<EnumMap<TaxStatType,Integer>>();
                try {
                    TransactionRowBuilder<T> previousBuilder = null;

                    // Setup the insertion statements to be used during the first and second passes.
                    rawTransactionInsertStatement = con.prepareStatement(TaxSqlUtils.getRawTransactionDetailInsertSql(summary.rawTransactionDetailRow));
                    secondPassTransactionInsertStatement = con.prepareStatement(TaxSqlUtils.getTransactionDetailInsertSql(summary.transactionDetailRow));

                    // Process each type of retrievable data row (PDP, DV, etc.).
                    for (Class<? extends TransactionRowBuilder<T>> builderClazz : builderClasses) {
                        // Create and configure the builder.
                        TransactionRowBuilder<T> builder = builderClazz.newInstance();
                        builder.copyValuesFromPreviousBuilder(previousBuilder, currentDao, summary);
                        LOG.info("Starting creation of first pass (raw) transaction rows from the following tax source: " + builder.getTaxSourceName());

                        // Setup the retrieval statement.
                        selectStatement = con.prepareStatement(builder.getSqlForSelect(summary));
                        Object[][] parameterValues = builder.getParameterValuesForSelect(summary);
                        setParameters(selectStatement, parameterValues);

                        // Get the results.
                        rs = selectStatement.executeQuery();
                        // Let the builder iterate over the results and insert new transaction detail rows as needed.
                        builder.buildRawTransactionRows(rs, rawTransactionInsertStatement, summary);

                        // Close the result set and SELECT prepared statement to prepare for the second pass.
                        rs.close();
                        selectStatement.close();

                        // Setup retrieval statement for second pass. SELECT needs to obtain data from first pass (raw) transaction details table
                        selectRawTransactionStatement = con.prepareStatement(builder.getSqlForSelectingCreatedRows(summary), ResultSet.TYPE_FORWARD_ONLY);
                        setParameters(selectRawTransactionStatement, builder.getParameterValuesForSelectingCreatedRows(summary));

                        // Get the query results from the first pass table.
                        rawDataTableResultSet = selectRawTransactionStatement.executeQuery();

                        // Let the builder iterate over the raw detail transaction rows from the first pass table
                        // updating specific attributes as needed then inserting those rows into the second pass
                        // table OR logging the keys of the raw data row if it should not be used.
                        builder.updateTransactionRowsFromWorkflowDocuments(rawDataTableResultSet, secondPassTransactionInsertStatement, summary);

                        // Get the statistics collected by the builder.
                        stats.add(builder.getStatistics());

                        // Close the result set and SELECT prepared statement to prepare for any future iterations.
                        rawDataTableResultSet.close();
                        selectRawTransactionStatement.close();

                        LOG.info("Finished creation of transaction rows from the following tax source: " + builder.getTaxSourceName());

                        // Reference the builder for copying data to future builders as needed.
                        previousBuilder = builder;
                    }

                    // Return the collected statistics.
                    return stats;
                } catch (InstantiationException e) {
                    throw new RuntimeException("Could not instantiate builder instance: " + e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not create builder instance: " + e.getMessage());
                } finally {
                    // Close result sets and prepared statements as needed.
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close tax data first pass ResultSet.");
                        }
                    }
                    if (rawDataTableResultSet != null) {
                        try {
                            rawDataTableResultSet.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close tax data second pass updatable ResultSet.");
                        }
                    }
                    if (rawTransactionInsertStatement != null) {
                        try {
                            rawTransactionInsertStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close rawTransactionInsertStatement row insertion statement.");
                        }
                    }
                    if (secondPassTransactionInsertStatement != null) {
                        try {
                            secondPassTransactionInsertStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close secondPassTransactionInsertStatement row insertion statement.");
                        }
                    }
                    if (selectRawTransactionStatement != null) {
                        try {
                            selectRawTransactionStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close tax data second pass selectRawTransactionStatement statement.");
                        }
                    }
                    if (selectStatement != null) {
                        try {
                            selectStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close tax data first pass selection statement.");
                        }
                    }
                }
            }
        });
    }

    private EnumMap<TaxStatType,Integer> processTransactionRows(Transaction1042SSummary summary, SprintaxProcessingService sprintaxProcessingService) {

        TaxOutputDefinition taxOutputDefinition = sprintaxProcessingService.getSprintaxOutputDefinition("SprintaxBioOutputDefinition.xml");
        SprintaxPaymentRowProcessor processor = buildNewProcessor(taxOutputDefinition, summary);

        processor.setReportsDirectory(getReportsDirectory());

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

                    java.util.Date processingStartDate = new java.util.Date();
                    List<String> filePaths = processor.getFilePathsForWriters(processingStartDate);
                    for (int i = 0; i < filePaths.size(); i++) {
                        String filePath = filePaths.get(i);
                        File outputFile = new File(filePath);
                        PrintWriter printWriter = new PrintWriter(outputFile, StandardCharsets.UTF_8);
                        Writer bufferedWriter = new BufferedWriter(printWriter);
                        processor.setWriter(bufferedWriter, i);

                        if (filePath.contains("bio")) {
                            bufferedWriter.write("First_Name,Middle_Name,Last_Name,Email,Unique_ID_Student_Number,TIN,DOB,Foreign_Tax_ID,Country_of_Residence,US_Address_Line_1,US_Address_Line_2,US_City,US_State,US_zip,NOnUS_Address_Line_1,NonUS_Address_Province,NonUS_Address_City,NonUS_address_Country,NonUS-Address_postal_code,Mailing_Address,Canada_Province");
                            bufferedWriter.write("\n");
                            bufferedWriter.flush();
                        }

                    }

                    ResultSet transactionDetailRecords = preparedSelectStatement.executeQuery();
                    processor.processTaxRows(transactionDetailRecords);

                } catch (IOException e) {
                    LOG.error(e.toString());
                }

                return processor.getStatistics();
            }
        });

    }

    private EnumMap<TaxStatType,Integer> printTransactionRows(java.util.Date processingStartDate, Transaction1042SSummary summary, TaxOutputDefinition outputDefinition) {

        SprintaxRowPrintProcessor processor = buildNewPrintProcessor(outputDefinition, summary);

        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet rs = null;
                PreparedStatement tempStatement = null;
                Writer tempWriter = null;

                try {

                    selectStatement = con.prepareStatement(processor.getSqlForSelect(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    setParameters(selectStatement, processor.getParameterValuesForSelect());

                    // Prepare any other statements needed by the tax processing.
                    String[] tempValues = processor.getSqlForExtraStatements();
                    for (int i = 0; i < tempValues.length; i++) {
                        tempStatement = con.prepareStatement(tempValues[i]);
                        Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i);
                        if (defaultArgs != null) {
                            setParameters(tempStatement, defaultArgs);
                        }
                        processor.setExtraStatement(tempStatement, i);
                    }
                    tempStatement = null;

                    String filePathForWriter = getPaymentsCsvFilePath(summary.reportYear, processingStartDate);
                    tempWriter = new BufferedWriter(new PrintWriter(new File(filePathForWriter), StandardCharsets.UTF_8));
                    processor.setWriter(tempWriter);
                    tempWriter = null;

                    // Get the transaction detail rows.
                    rs = selectStatement.executeQuery();

                    // Perform the actual processing.
                    processor.processTaxRows(rs);

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

    /**
     * Helper method for building a new TransactionRowProcessor instance from the parsed XML input.
     * The processor's implementation class *must* have a default constructor.
     * Note that this method will not configure the PreparedStatement and Writer instances;
     * the calling code is responsible for that part of the processor setup.
     *
     * @param outputDefinition The parsed XML definition for this processor's output formatting.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A TransactionRowProcessor implementation of the given type with the given formatting.
     */
    SprintaxPaymentRowProcessor buildNewProcessor(TaxOutputDefinition outputDefinition, Transaction1042SSummary summary) {
        Map<String, SprintaxPaymentRowProcessor.RecordPiece> complexPieces = new HashMap<String, SprintaxPaymentRowProcessor.RecordPiece>();
        Map<String,List<String>> complexPiecesNames = new HashMap<String,List<String>>();
        EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> minimumPieces = new EnumMap<CUTaxBatchConstants.TaxFieldSource,Set<TaxTableField>>(CUTaxBatchConstants.TaxFieldSource.class);
        boolean foundDuplicate = false;
        int i = 0;

        // Check for at least one output section.
        if (outputDefinition.getSections().isEmpty()) {
            throw new IllegalArgumentException("outputDefinition has no sections!");
        }

        SprintaxPaymentRowProcessor rowProcessor = new SprintaxPaymentRowProcessor(summary);

        // Determine the minimum "piece" objects that need to be created for each type (excluding BLANK and STATIC).
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DETAIL, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DETAIL));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DERIVED, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DERIVED));



        // Create the "piece" objects for each section and add them to the processor.
        for (TaxOutputSection section : outputDefinition.getSections()) {
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
                TaxTableField tableField;



                // Create a simple "piece" type or determine what complex "piece" type to create.
                switch (fieldSource) {
                    case BLANK :
                        // Use the AlwaysBlankRecordPiece implementation for blank "pieces".
                        pieces.add(new SprintaxPaymentRowProcessor.AlwaysBlankRecordPiece(field.getName(), field.getLength().intValue()));
                        tableField = null;
                        break;

                    case STATIC :
                        // Use the StaticStringRecordPiece implementation for static-value "pieces".
                        pieces.add(new SprintaxPaymentRowProcessor.StaticStringRecordPiece(field.getName(), field.getLength().intValue(), field.getValue()));
                        tableField = null;
                        break;

                    case DETAIL :
                        tableField = summary.transactionDetailRow.getField(field.getValue());
                        break;

                    case PDP :
                        throw new IllegalStateException("Cannot create piece for PDP type");

                    case DV :
                        throw new IllegalStateException("Cannot create piece for DV type");

                    case VENDOR :
                        tableField = summary.vendorRow.getField(field.getValue());
                        break;

                    case VENDOR_US_ADDRESS :
                    case VENDOR_ANY_ADDRESS :
                        tableField = summary.vendorAddressRow.getField(field.getValue());
                        break;

                    case DOCUMENT_NOTE :
                        throw new IllegalStateException("Cannot create piece for DOCUMENT_NOTE type");

                    case DERIVED :
                        tableField = summary.derivedValues.getField(field.getValue());
                        break;

                    default :
                        throw new IllegalStateException("Unrecognized piece type for field");
                }



                // Create a more complex "piece" type if necessary.
                if (tableField != null) {
                    String pieceKey = tableField.propertyName;

                    // Create a new "piece" or re-use an existing one for duplicates as needed.
                    SprintaxPaymentRowProcessor.RecordPiece currentPiece = complexPieces.get(pieceKey);

                    if (currentPiece == null) {
                        // If not a duplicate, then create a new one.
                        currentPiece = rowProcessor.getPieceForField(fieldSource, tableField, field.getName(), field.getLength().intValue());
                        complexPiecesNames.put(pieceKey, new ArrayList<String>());
                        minimumPieces.get(fieldSource).remove(tableField);
                        // Add piece to cache.
                        complexPieces.put(pieceKey, currentPiece);
                    } else {
                        // If a duplicate, then use the originally-created piece instead, and warn about mismatched lengths.
                        foundDuplicate = true;
                        if (currentPiece.len != field.getLength().intValue()) {
                            LOG.warn("NOTE: Found multiple tax output pieces with key " + pieceKey + " that do not have the same max length!");
                        }
                    }
                    complexPiecesNames.get(pieceKey).add(field.getName());
                    pieces.add(currentPiece);
                }
            }



            // Setup the section's output buffer.
            rowProcessor.setupOutputBuffer(
                    i,
                    section.getLength(),
                    pieces,
                    section.isHasExactLength(),
                    section.isHasSeparators(),
                    section.isHasSeparators() ? section.getSeparatorChar().charValue() : ' '
            );

            i++;
        }



        // If the processor has defined some minimum fields, but they have not been created yet, then create them.
        i = 1;
        for (Map.Entry<CUTaxBatchConstants.TaxFieldSource,Set<TaxTableField>> minTypeSpecificPieces : minimumPieces.entrySet()) {
            if (minTypeSpecificPieces.getValue() != null) {
                for (TaxTableField minPiece : minTypeSpecificPieces.getValue()) {
                    SprintaxPaymentRowProcessor.RecordPiece field = rowProcessor.getPieceForField(minTypeSpecificPieces.getKey(), minPiece, "autoGen" + i, 1);
                    complexPieces.put(minPiece.propertyName, field);
                    i++;
                }
            }
        }

        // Set the processor's complex "pieces" as needed.
        rowProcessor.setComplexPieces(complexPieces);



        // Perform final logging as needed and return the processor.
        if (foundDuplicate && LOG.isDebugEnabled()) {
            LOG.debug("The following tax output fields appeared more than once under different names:");
            for (Map.Entry<String,List<String>> pieceNames : complexPiecesNames.entrySet()) {
                if (pieceNames.getValue().size() > 1) {
                    LOG.debug(pieceNames.getKey() + ": " + pieceNames.getValue().toString());
                }
            }
        }

        return rowProcessor;
    }

    SprintaxRowPrintProcessor buildNewPrintProcessor(TaxOutputDefinition outputDefinition, Transaction1042SSummary summary) {
        Map<String, SprintaxRowPrintProcessor.RecordPiece> complexPieces = new HashMap<>();
        Map<String,List<String>> complexPiecesNames = new HashMap<>();
        EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> minimumPieces = new EnumMap<>(CUTaxBatchConstants.TaxFieldSource.class);
        boolean foundDuplicate = false;
        int i = 0;

        // Check for at least one output section.
        if (outputDefinition.getSections().isEmpty()) {
            throw new IllegalArgumentException("outputDefinition has no sections!");
        }

        SprintaxRowPrintProcessor rowProcessor = new SprintaxRowPrintProcessor(summary, getReportsDirectory());

        // Determine the minimum "piece" objects that need to be created for each type (excluding BLANK and STATIC).
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DETAIL, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DETAIL));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS));
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DERIVED, rowProcessor.getMinimumFields(CUTaxBatchConstants.TaxFieldSource.DERIVED));



        // Create the "piece" objects for each section and add them to the processor.
        for (TaxOutputSection section : outputDefinition.getSections()) {
            if (section.getFields().isEmpty()) {
                throw new RuntimeException("Cannot have empty sections!");
            } else if (section.getLength() == null) {
                throw new RuntimeException("Cannot have section with unspecified max length!");
            } else if (section.isHasSeparators() && section.getSeparator() == null) {
                throw new RuntimeException("Cannot have a null separator for a section with separator-delimited fields!");
            }
            List<SprintaxRowPrintProcessor.RecordPiece> pieces = new ArrayList<>(section.getFields().size());

            for (TaxOutputField field : section.getFields()) {
                if (StringUtils.isBlank(field.getName())) {
                    throw new RuntimeException("Cannot have field with blank name");
                } else if (StringUtils.isBlank(field.getType())) {
                    throw new RuntimeException("Cannot have field with blank type");
                } else if (field.getLength() == null) {
                    throw new RuntimeException("Cannot have field with null length");
                }

                CUTaxBatchConstants.TaxFieldSource fieldSource = CUTaxBatchConstants.TaxFieldSource.valueOf(field.getType());
                TaxTableField tableField;



                // Create a simple "piece" type or determine what complex "piece" type to create.
                switch (fieldSource) {
                    case BLANK :
                        // Use the AlwaysBlankRecordPiece implementation for blank "pieces".
                        pieces.add(new SprintaxRowPrintProcessor.AlwaysBlankRecordPiece(field.getName(), field.getLength().intValue()));
                        tableField = null;
                        break;

                    case STATIC :
                        // Use the StaticStringRecordPiece implementation for static-value "pieces".
                        pieces.add(new SprintaxRowPrintProcessor.StaticStringRecordPiece(field.getName(), field.getLength().intValue(), field.getValue()));
                        tableField = null;
                        break;

                    case DETAIL :
                        tableField = summary.transactionDetailRow.getField(field.getValue());
                        break;

                    case PDP :
                        throw new IllegalStateException("Cannot create piece for PDP type");

                    case DV :
                        throw new IllegalStateException("Cannot create piece for DV type");

                    case VENDOR :
                        tableField = summary.vendorRow.getField(field.getValue());
                        break;

                    case VENDOR_US_ADDRESS :
                    case VENDOR_ANY_ADDRESS :
                        tableField = summary.vendorAddressRow.getField(field.getValue());
                        break;

                    case DOCUMENT_NOTE :
                        throw new IllegalStateException("Cannot create piece for DOCUMENT_NOTE type");

                    case DERIVED :
                        tableField = summary.derivedValues.getField(field.getValue());
                        break;

                    default :
                        throw new IllegalStateException("Unrecognized piece type for field");
                }



                // Create a more complex "piece" type if necessary.
                if (tableField != null) {
                    String pieceKey = tableField.propertyName;

                    // Create a new "piece" or re-use an existing one for duplicates as needed.
                    SprintaxRowPrintProcessor.RecordPiece currentPiece = complexPieces.get(pieceKey);

                    if (currentPiece == null) {
                        // If not a duplicate, then create a new one.
                        currentPiece = rowProcessor.getPieceForField(fieldSource, tableField, field.getName(), field.getLength().intValue());
                        complexPiecesNames.put(pieceKey, new ArrayList<String>());
                        minimumPieces.get(fieldSource).remove(tableField);
                        // Add piece to cache.
                        complexPieces.put(pieceKey, currentPiece);
                    } else {
                        // If a duplicate, then use the originally-created piece instead, and warn about mismatched lengths.
                        foundDuplicate = true;
                        if (currentPiece.len != field.getLength().intValue()) {
                            LOG.warn("NOTE: Found multiple tax output pieces with key " + pieceKey + " that do not have the same max length!");
                        }
                    }
                    complexPiecesNames.get(pieceKey).add(field.getName());
                    pieces.add(currentPiece);
                }
            }



            // Setup the section's output buffer.
            rowProcessor.setupOutputBuffer(i, section.getLength(), pieces, section.isHasExactLength(), section.isHasSeparators(),
                    section.isHasSeparators() ? section.getSeparatorChar().charValue() : ' ');
            i++;
        }



        // If the processor has defined some minimum fields but they have not been created yet, then create them.
        i = 1;
        for (Map.Entry<CUTaxBatchConstants.TaxFieldSource,Set<TaxTableField>> minTypeSpecificPieces : minimumPieces.entrySet()) {
            if (minTypeSpecificPieces.getValue() != null) {
                for (TaxTableField minPiece : minTypeSpecificPieces.getValue()) {
                    SprintaxRowPrintProcessor.RecordPiece field = rowProcessor.getPieceForField(minTypeSpecificPieces.getKey(), minPiece, "autoGen" + i, 1);
                    complexPieces.put(minPiece.propertyName, field);
                    i++;
                }
            }
        }

        // Set the processor's complex "pieces" as needed.
        rowProcessor.setComplexPieces(complexPieces);



        // Perform final logging as needed and return the processor.
        if (foundDuplicate && LOG.isDebugEnabled()) {
            LOG.debug("The following tax output fields appeared more than once under different names:");
            for (Map.Entry<String,List<String>> pieceNames : complexPiecesNames.entrySet()) {
                if (pieceNames.getValue().size() > 1) {
                    LOG.debug(pieceNames.getKey() + ": " + pieceNames.getValue().toString());
                }
            }
        }

        return rowProcessor;
    }


}
