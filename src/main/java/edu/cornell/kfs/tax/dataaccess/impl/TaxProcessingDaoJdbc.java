package edu.cornell.kfs.tax.dataaccess.impl;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.ConnectionCallback;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.TaxOutputDefinition;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxSqlUtils.SqlText;
import edu.cornell.kfs.tax.service.TaxProcessingService;

/**
 * Default JDBC implementation of TaxProcessingDao.
 */
public class TaxProcessingDaoJdbc extends PlatformAwareDaoBaseJdbc implements TaxProcessingDao {
	private static final Logger LOG = LogManager.getLogger(TaxProcessingDaoJdbc.class);

    private static final int DOCID_MAX_BATCH_SIZE = 5000;

    private String reportsDirectory;
    private ConfigurationService configurationService;

    @Override
    public void doTaxProcessing(String taxType, int reportYear, java.sql.Date startDate, java.sql.Date endDate, boolean vendorForeign,
            java.util.Date processingStartDate) {
        List<EnumMap<TaxStatType,Integer>> stats;
        TaxProcessingService taxProcessingService = SpringContext.getBean(TaxProcessingService.class);
        TaxOutputDefinition tempDefinition;
        
        if (CUTaxConstants.TAX_TYPE_1099.equals(taxType)) {
            // Perform 1099 tax processing.
            Transaction1099Summary summary = new Transaction1099Summary(reportYear, startDate, endDate, vendorForeign,
                    taxProcessingService.getDataDefinition(CUTaxKeyConstants.TAX_TABLE_1099_PREFIX, reportYear).getDataRowsAsMap());
            // Delete any previously-generated rows for the current year and tax type.
            getJdbcTemplate().update(TaxSqlUtils.getRawTransactionDetailDeleteSql(taxType, summary.rawTransactionDetailRow), Integer.valueOf(reportYear));
            getJdbcTemplate().update(TaxSqlUtils.getTransactionDetailDeleteSql(taxType, summary.transactionDetailRow), Integer.valueOf(reportYear));
            // Create new transaction rows, retrieving summary statistics as needed.
            stats = createTransactionRows(summary, Arrays.<Class<? extends TransactionRowBuilder<Transaction1099Summary>>>asList(
                    TransactionRowPdpBuilder.For1099.class, TransactionRowDvBuilder.For1099.class, TransactionRowPRNCBuilder.For1099.class));
            // Create 1099 output data from the transaction rows and send it to the file(s), retrieving summary statistics as needed.
            tempDefinition = taxProcessingService.getOutputDefinition(CUTaxKeyConstants.TAX_FORMAT_1099_PREFIX, reportYear);
            stats.add(processTransactionRows(processingStartDate, summary, TransactionRow1099Processor.class, tempDefinition));
            // Print the transaction row data to another file.
            tempDefinition = taxProcessingService.getOutputDefinition(
                    CUTaxKeyConstants.TAX_FORMAT_1099_PREFIX + CUTaxKeyConstants.TAX_FORMAT_SUMMARY_SUFFIX, reportYear);
            processTransactionRows(processingStartDate, summary, TransactionRowPrintProcessor.For1099.class, tempDefinition);
            
        } else if (CUTaxConstants.TAX_TYPE_1042S.equals(taxType)) {
            // Perform 1042S tax processing.
            Transaction1042SSummary summary = new Transaction1042SSummary(reportYear, startDate, endDate, vendorForeign,
                    taxProcessingService.getDataDefinition(CUTaxKeyConstants.TAX_TABLE_1042S_PREFIX, reportYear).getDataRowsAsMap());
            // Delete any previously-generated rows for the current year and tax type.
            getJdbcTemplate().update(TaxSqlUtils.getRawTransactionDetailDeleteSql(taxType, summary.rawTransactionDetailRow), Integer.valueOf(reportYear));
            getJdbcTemplate().update(TaxSqlUtils.getTransactionDetailDeleteSql(taxType, summary.transactionDetailRow), Integer.valueOf(reportYear));
            // Create new transaction rows, retrieving summary statistics as needed.
            stats = createTransactionRows(summary, Arrays.<Class<? extends TransactionRowBuilder<Transaction1042SSummary>>>asList(
                    TransactionRowPdpBuilder.For1042S.class, TransactionRowDvBuilder.For1042S.class, TransactionRowPRNCBuilder.For1042S.class));
            // Create 1042S output data from the transaction rows and send it to the file(s), retrieving summary statistics as needed.
            tempDefinition = taxProcessingService.getOutputDefinition(CUTaxKeyConstants.TAX_FORMAT_1042S_PREFIX, reportYear);
            stats.add(processTransactionRows(processingStartDate, summary, TransactionRow1042SProcessor.class, tempDefinition));
            // Print the transaction row data to another file.
            tempDefinition = taxProcessingService.getOutputDefinition(
                    CUTaxKeyConstants.TAX_FORMAT_1042S_PREFIX + CUTaxKeyConstants.TAX_FORMAT_SUMMARY_SUFFIX, reportYear);
            processTransactionRows(processingStartDate, summary, TransactionRowPrintProcessor.For1042S.class, tempDefinition);
            
        } else {
            // Invalid tax processing type was given.
            throw new IllegalArgumentException("Unrecognized tax type");
        }
        
        // Print the statistics.
        printStatistics(stats);
    }



    /**
     * This implementation expects the helperObject to be an implementation of TransactionDetailSummary.
     * 
     * @see edu.cornell.kfs.tax.dataaccess.TaxProcessingDao#findForeignDraftsAndWireTransfers(java.util.List, java.lang.Object)
     */
    @Override
    public List<String> findForeignDraftsAndWireTransfers(final List<String> documentIds, Object helperObject, String docType) {
        final TransactionDetailSummary summary = (TransactionDetailSummary) helperObject;
        final String documentType = docType;
        if (documentIds.isEmpty()) {
            return new ArrayList<String>();
        }
        
        return getJdbcTemplate().execute(new ConnectionCallback<List<String>>() {
            @Override
            public List<String> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet rs = null;
                List<String> finalResults = new ArrayList<String>();
                int largeBatchCounter = 0;
                
                try {
                    // Do larger-batch retrievals first.
                    if (documentIds.size() >= DOCID_MAX_BATCH_SIZE) {
                    	String selectStatementSql = getForeignDraftAndWireTransferSelectSql(DOCID_MAX_BATCH_SIZE, summary, documentType);
                    	LOG.debug(selectStatementSql);
                        selectStatement = con.prepareStatement(selectStatementSql);
                        int largeBatchLimit = documentIds.size() - (documentIds.size() % DOCID_MAX_BATCH_SIZE);
                        
                        for (largeBatchCounter = 0; largeBatchCounter < largeBatchLimit; largeBatchCounter += DOCID_MAX_BATCH_SIZE) {
                            for (int i = 0; i < DOCID_MAX_BATCH_SIZE; i++) {
                                selectStatement.setString(i + 1, documentIds.get(largeBatchCounter + i));
                            }
                            
                            rs = selectStatement.executeQuery();
                            while (rs.next()) {
                                finalResults.add(rs.getString(1));
                            }
                            rs.close();
                        }
                        
                        selectStatement.close();
                        largeBatchCounter = largeBatchLimit;
                    }
                    
                    // Do a smaller-batch retrieval last if necessary.
                    if (documentIds.size() % DOCID_MAX_BATCH_SIZE > 0) {
                    	String selectStatementSql = getForeignDraftAndWireTransferSelectSql(documentIds.size() % DOCID_MAX_BATCH_SIZE, summary, documentType);
                    	LOG.debug(selectStatementSql);
                        selectStatement = con.prepareStatement(selectStatementSql);
                        
                        for (int i = (documentIds.size() % DOCID_MAX_BATCH_SIZE) - 1; i >= 0; i--) {
                            selectStatement.setString(i + 1, documentIds.get(largeBatchCounter + i));
                        }
                        
                        rs = selectStatement.executeQuery();
                        while (rs.next()) {
                            finalResults.add(rs.getString(1));
                        }
                    }
                    
                } finally {
                    // Close result sets and statements accordingly.
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close ResultSet");
                        }
                    }
                    if (selectStatement != null) {
                        try {
                            selectStatement.close();
                        } catch (SQLException e) {
                            LOG.error("Could not close selection PreparedStatement");
                        }
                    }
                }
                
                return finalResults;
            }
        });
    }

    private String getForeignDraftAndWireTransferSelectSql(int paramSize, TransactionDetailSummary summary, String docType) {
    	 if(DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equalsIgnoreCase(docType)){
    		return TaxSqlUtils.getQueryWithoutColumnPrefixes(SqlText.SELECT, summary.dvRow.dvDocumentNumber,
                    SqlText.FROM, summary.dvRow.tables.get(TaxSqlUtils.getTableIndexForField(summary.dvRow.dvDocumentNumber)),
                    SqlText.WHERE,
                            TaxSqlUtils.getInListCriteria(summary.dvRow.dvDocumentNumber, paramSize, true, false),
                    SqlText.AND,
                            summary.dvRow.documentDisbVchrPaymentMethodCode, SqlText.IN, "('F','W')");
    	}
    	else if(PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equalsIgnoreCase(docType)){
    		return TaxSqlUtils.getQueryWithoutColumnPrefixes(SqlText.SELECT, summary.prncRow.preqDocumentNumber,
    				SqlText.FROM, summary.prncRow.tables.get(TaxSqlUtils.getTableIndexForField(summary.prncRow.preqDocumentNumber)),
    				SqlText.WHERE,
    				TaxSqlUtils.getInListCriteria(summary.prncRow.preqDocumentNumber, paramSize, true, false),
    				SqlText.AND,
    				summary.prncRow.paymentMethodCode, SqlText.IN, "('F','W')");
    	}
    	else {
    		return StringUtils.EMPTY;
    	}
    }



    /*
     * Helper method for retrieving the tax data from the various sources (DV, PDP, etc.)
     * and placing it in new raw transaction detail table rows. Returns a list of EnumMaps
     * containing numeric statistics pertaining to the various tax data sources.
     * NOTE: Each builder class must have a default constructor!
     */
    protected <T extends TransactionDetailSummary> List<EnumMap<TaxStatType,Integer>> createTransactionRows(
            final T summary, final List<Class<? extends TransactionRowBuilder<T>>> builderClasses) {
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



    /*
     * Helper method that retrieves the generated transaction detail rows from the second pass table
     * TX_TRANSACTION_DETAIL_T and uses them to print tax rows to the output file(s) accordingly. 
     * Returns an EnumMap containing numeric statistics pertaining to the transaction row processing.
     */
    protected <T extends TransactionDetailSummary> EnumMap<TaxStatType,Integer> processTransactionRows(final java.util.Date processingStartDate,
            final T summary, final Class<? extends TransactionRowProcessor<T>> processorClazz, final TaxOutputDefinition outputDefinition) {
        // Create the object that will handle the processing of the transaction row data.
        final TransactionRowProcessor<T> processor = TransactionRowProcessorBuilder.createBuilder().buildNewProcessor(
                processorClazz, outputDefinition, summary);
        processor.setReportsDirectory(reportsDirectory);
        
        // Use a ConnectionCallback via a JdbcTemplate to simplify the batch processing and transaction management.
        return getJdbcTemplate().execute(new ConnectionCallback<EnumMap<TaxStatType,Integer>>() {
            @Override
            public EnumMap<TaxStatType,Integer> doInConnection(Connection con) throws SQLException {
                PreparedStatement selectStatement = null;
                ResultSet rs = null;
                // Once tempStatement or tempWriter get passed to the processor successfully, we don't need to hold onto their references here.
                PreparedStatement tempStatement = null;
                Writer tempWriter = null;
                
                try {
                    String[] tempValues;
                    
                    // Prepare the second pass table selection SQL. Connection needs to be updatable due to the 
                    // TransactionRowXXXXProcessor classes performing updates to some of the rows based upon 
                    // processing logic encountered in those classes.
                    selectStatement = con.prepareStatement(processor.getSqlForSelect(summary), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                    setParameters(selectStatement, processor.getParameterValuesForSelect(summary));
                    
                    // Prepare any other statements needed by the tax processing.
                    tempValues = processor.getSqlForExtraStatements(summary);
                    for (int i = 0; i < tempValues.length; i++) {
                        tempStatement = con.prepareStatement(tempValues[i]);
                        Object[][] defaultArgs = processor.getDefaultParameterValuesForExtraStatement(i, summary);
                        if (defaultArgs != null) {
                            setParameters(tempStatement, defaultArgs);
                        }
                        processor.setExtraStatement(tempStatement, i);
                    }
                    tempStatement = null;
                    
                    // Prepare any Writer instances needed by the tax processing.
                    tempValues = processor.getFilePathsForWriters(summary, processingStartDate);
                    for (int i = 0; i < tempValues.length; i++) {
                        tempWriter = new BufferedWriter(
                                new PrintWriter(new File(tempValues[i]), StandardCharsets.UTF_8));
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



    /*
     * Helper method for configuring PreparedStatement parameters from a two-dimensional Object array.
     * The first dimension defines the parameters to set (index 0 for parm 1, index 1 for parm 2, etc.).
     * The second dimension should have a length of 1 or higher (though elements beyond the third one
     * are ignored), and should contain the following elements:
     * 
     * Index 0: The value to set (required, may be null)
     * Index 1: The value's JDBC type (optional for String, int/Integer, or java.sql.Date values)
     * Index 2: The scale or stream length of the value (optional)
     */
    protected void setParameters(PreparedStatement pStatement, Object[][] args) throws SQLException {
        int i = 1;
        for (Object[] arg : args) {
            if (arg.length == 0) {
                // Length cannot be zero.
                throw new RuntimeException("Second dimension of args array cannot have length of zero");
                
            } else if (arg.length == 1) {
                // Shortcut to set String, Integer, or java.sql.Date args.
                if (arg[0] instanceof String) {
                    pStatement.setString(i, (String) arg[0]);
                } else if (arg[0] instanceof Integer) {
                    pStatement.setInt(i, ((Integer) arg[0]).intValue());
                } else if (arg[0] instanceof java.sql.Date) {
                    pStatement.setDate(i, (java.sql.Date) arg[0]);
                } else {
                    throw new RuntimeException("Invalid arg type for single-value second dimension of args array");
                }
                
            } else if (arg.length == 2) {
                // Set arg with default number scale or stream length of zero.
                pStatement.setObject(i, arg[0], ((Integer) arg[1]).intValue());
                
            } else {
                // Set arg with given number scale or stream length.
                pStatement.setObject(i, arg[0], ((Integer) arg[1]).intValue(), ((Integer) arg[2]).intValue());
            }
            
            i++;
        }
    }



    /*
     * Helper method for printing the numeric statistics collected from the tax processing.
     */
    protected void printStatistics(List<EnumMap<TaxStatType,Integer>> stats) {
        for (TaxStatType statType : TaxStatType.values()) {
            int total = 0;
            boolean statDefined = false;
            
            // Get the sum of all the numeric results for the given type of statistic.
            for (EnumMap<TaxStatType,Integer> statMap : stats) {
                Integer amount = statMap.get(statType);
                if (amount != null) {
                    total += amount.intValue();
                    statDefined = true;
                }
            }
            
            // Print the label for the given type of statistic, followed by ": " and the sum.
            if (statDefined) {
                String labelKey = statType.toString();
                String label = configurationService.getPropertyValueAsString(labelKey);
                LOG.info("printStatistics: " + label + ": " + Integer.toString(total));
            }
        }
    }

    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public String getReportsDirectory() {
        return reportsDirectory;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

}
