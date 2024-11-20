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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
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
        Transaction1042SSummary summary = buildTransaction1042SSummary(taxProcessingService, taxParameters);


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
        List<EnumMap<TaxStatType,Integer>> stats = createTransactionRows(summary, transactionRowBuilders);

        LOG.info("Processing Transaction Records and generating Biographic csv file");
        EnumMap<TaxStatType, Integer> processingStats = processTransactionRows(summary, taxProcessingService);
        stats.add(processingStats);

        LOG.info("Printing to csv files");
        TaxOutputDefinition paymentsOutputDefinition = taxProcessingService.getSprintaxOutputDefinition("SprintaxTransactionOutputDefinition.xml");
        printTransactionRows(taxParameters.getJobRunDate(), summary, paymentsOutputDefinition);

        printStatistics(stats);
    }

    public Transaction1042SSummary buildTransaction1042SSummary(SprintaxProcessingService sprintaxProcessingService, SprintaxReportParameters taxParameters) {
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
                    Writer writer = buildBufferedWriterForBioFile();
                    processor.setWriter(writer);

                    ResultSet transactionDetailRecords = preparedSelectStatement.executeQuery();
                    processor.processTaxRows(transactionDetailRecords);

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

        bufferedWriter.write("First_Name,Middle_Name,Last_Name,Email,Unique_ID_Student_Number,TIN,DOB,Foreign_Tax_ID,Country_of_Residence,US_Address_Line_1,US_Address_Line_2,US_City,US_State,US_zip,NOnUS_Address_Line_1,NonUS_Address_Province,NonUS_Address_City,NonUS_address_Country,NonUS-Address_postal_code,Mailing_Address,Canada_Province");
        bufferedWriter.write("\n");
        bufferedWriter.flush();

        return bufferedWriter;
    }

    String getFilePathForBioWriter(java.util.Date processingStartDate) {
        DateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        String bioFilePath = getReportsDirectory() + "/irs_1042s_sprintax_bio" + dateFormat.format(processingStartDate) + ".csv";

        return bioFilePath;
    }

    private void printTransactionRows(java.util.Date processingStartDate, Transaction1042SSummary summary, TaxOutputDefinition outputDefinition) {

        SprintaxRowPrintProcessor processor = buildNewPrintProcessor(outputDefinition, summary);

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

                    String filePathForWriter = getPaymentsCsvFilePath(summary.reportYear, processingStartDate);
                    processor.buildWriter(filePathForWriter);

                    processor.processTaxRows(transactionDetailRecords);

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

    //todo remove duplication
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
//            TaxOutputSection section = outputDefinition.getSections().get(0);
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
            rowProcessor.setupOutputBuffer(section.getLength(), pieces);

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
        boolean foundDuplicate = false;
        int i = 0;

        SprintaxRowPrintProcessor rowProcessor = new SprintaxRowPrintProcessor(summary);
        EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> minimumPieces = buildMinimumPiecesForPrintProcessor(summary);

        // Create the "piece" objects for each section and add them to the processor.
        for (TaxOutputSection section : outputDefinition.getSections()) {
            List<SprintaxRowPrintProcessor.RecordPiece> pieces = new ArrayList<>();

            for (TaxOutputField field : section.getFields()) {
                CUTaxBatchConstants.TaxFieldSource fieldSource = CUTaxBatchConstants.TaxFieldSource.valueOf(field.getType());
                TaxTableField tableField;

                // Create a simple "piece" type or determine what complex "piece" type to create.
                switch (fieldSource) {

                    case STATIC :
                        // Use the StaticStringRecordPiece implementation for static-value "pieces".
                        pieces.add(new SprintaxRowPrintProcessor.StaticStringRecordPiece(field.getName(), field.getValue()));
                        tableField = null;
                        break;

                    case DETAIL :
                        tableField = summary.transactionDetailRow.getField(field.getValue());
                        break;

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
                        currentPiece = rowProcessor.getPieceForField(fieldSource, tableField, field.getName());
                        complexPiecesNames.put(pieceKey, new ArrayList<>());
                        minimumPieces.get(fieldSource).remove(tableField);
                        // Add piece to cache.
                        complexPieces.put(pieceKey, currentPiece);
                    } else {
                        // If a duplicate, then use the originally-created piece instead, and warn about mismatched lengths.
                        foundDuplicate = true;
                    }
                    complexPiecesNames.get(pieceKey).add(field.getName());
                    pieces.add(currentPiece);
                }
            }

            // Setup the section's output buffer.
            rowProcessor.buildOutputBuffer(section.getLength(), pieces, section.getSeparatorChar().charValue());
            i++;
        }



        // If the processor has defined some minimum fields but they have not been created yet, then create them.
        i = 1;
        for (Map.Entry<CUTaxBatchConstants.TaxFieldSource,Set<TaxTableField>> minTypeSpecificPieces : minimumPieces.entrySet()) {
            if (minTypeSpecificPieces.getValue() != null) {
                for (TaxTableField minPiece : minTypeSpecificPieces.getValue()) {
                    SprintaxRowPrintProcessor.RecordPiece field = rowProcessor.getPieceForField(minTypeSpecificPieces.getKey(), minPiece, "autoGen" + i);
                    complexPieces.put(minPiece.propertyName, field);
                    i++;
                }
            }
        }

        // Set the processor's complex "pieces" as needed.
        rowProcessor.buildSsnFieldDefinition(complexPieces);



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


    public EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> buildMinimumPiecesForPrintProcessor(Transaction1042SSummary summary) {
        EnumMap<CUTaxBatchConstants.TaxFieldSource, Set<TaxTableField>> minimumPieces = new EnumMap<>(CUTaxBatchConstants.TaxFieldSource.class);

        HashSet<TaxTableField> detailMinPieces = new HashSet<>();
        detailMinPieces.addAll(summary.transactionDetailRow.orderedFields);
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DETAIL, detailMinPieces);

        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR, new HashSet<>());
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_US_ADDRESS, new HashSet<>());
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.VENDOR_ANY_ADDRESS, new HashSet<>());


        HashSet<TaxTableField> derivedMinPieces = new HashSet<>();
        derivedMinPieces.add(summary.derivedValues.ssn);
        minimumPieces.put(CUTaxBatchConstants.TaxFieldSource.DERIVED, derivedMinPieces);

        return minimumPieces;
    }

}
