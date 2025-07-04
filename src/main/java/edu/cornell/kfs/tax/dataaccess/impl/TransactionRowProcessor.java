package edu.cornell.kfs.tax.dataaccess.impl;

import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFieldSource;

/**
 * Base class for helper objects that process (and possibly update)
 * generated transaction detail rows for a given tax year.
 * 
 * <p>This superclass contains some helper arrays for building the
 * sections of character data that will be used in the file outputs.
 * These arrays and this class's related append methods allow for
 * convenient construction and re-use of outputs without using
 * numerous String-creating operations (like substring() and trim()).
 * It is assumed that each section either has a pre-defined
 * max length or always has the same length.</p>
 * 
 * <p>The character building is largely achieved through special
 * "piece" objects. These helper objects are meant to represent
 * the various file fields that will be included in the output.
 * They each define a getValue() method whose return value is
 * what will be appended to the related character array. (If needed,
 * the processing will right-pad or truncate the returned value
 * to conform to the expected output format.) Most of the "piece"
 * subclasses are meant to be defined by the row processor subclasses,
 * allowing them to conveniently get and set the behind-the-scenes
 * values for each one.</p>
 * 
 * <p>In addition, this class provides a helper ResultSet array so that
 * subclasses can conveniently re-run PreparedStatement queries and
 * store their results without needing to worry about closing the
 * ResultSet objects explicitly. This class's helper methods will
 * automatically close a ResultSet object at a given index when it is
 * no longer needed, and will also close all the remaining ones
 * once the processing is complete. The PreparedStatement objects
 * are stored in a helper array as well, and will also be closed
 * accordingly after finishing the processing.</p>
 * 
 * <p>This class, its subclasses, its static nested classes, and
 * its inner classes are *NOT* thread-safe. Without external
 * synchronization, only one thread should be handling a
 * processor and its nested/inner classes at a given time.</p>
 */
abstract class TransactionRowProcessor<T extends TransactionDetailSummary> {
	private static final Logger LOG = LogManager.getLogger(TransactionRowProcessor.class);

    // Constants pertaining to constructing the tax ID in NNN-NN-NNNN format.
    private static final int TAXID_SRC_CHUNK1_2_SPLIT = 3;
    private static final int TAXID_SRC_CHUNK2_3_SPLIT = 5;
    private static final int TAXID_SRC_CHUNK3_END = 9;
    private static final int TAXID_DEST_CHUNK2_START = 4;
    private static final int TAXID_DEST_CHUNK3_START = 7;

    // Helper array for storing extra potentially-open result sets created by the tax processing.
    private final ResultSet[] extraResultSets;

    // Helper array for storing extra PreparedStatement instances needed for the tax processing.
    private final PreparedStatement[] extraStatements;

    // Helper objects for generating and storing output.
    private final OutputHelper[] outputHelpers;

    // The Writer instances that the character buffers can be written to.
    private final Writer[] writers;

    // Helper array for formatting tax IDs.
    private final char[] formattedTaxId;

    // The Format instances for formatting numeric and date output.
    private final DecimalFormat amountFormat;
    private final DecimalFormat percentFormat;
    private final DateFormat dateFormat;

    // The default reporting sub-directory to use for file outputs.
    private String reportsDirectory;

    /**
     * Creates a new row processor instance with the given max number
     * of extra result sets allowed. The generated result sets can be
     * referenced via numeric indexes that start at zero.
     * 
     * @param maxExtraResultSets The maximum number of extra result sets that can be stored; cannot be negative.
     * @param maxExtraStatements The maximum number of extra prepared statements to have; cannot be negative.
     * @param numOutputBuffers The number of character output buffers to store; cannot be negative.
     * @param numWriters The number of Writer instances to have; cannot be negative.
     * @param reportsDirectory The default reporting sub-directory to use for the file outputs.
     */
    TransactionRowProcessor(int maxExtraResultSets, int maxExtraStatements, int numOutputBuffers, int numWriters) {
        this.extraResultSets = new ResultSet[maxExtraResultSets];
        this.extraStatements = new PreparedStatement[maxExtraStatements];
        this.outputHelpers = new OutputHelper[numOutputBuffers];
        this.writers = new Writer[numWriters];
        this.formattedTaxId = new char[] {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'};
        this.amountFormat = buildAmountFormat();
        this.percentFormat = buildPercentFormat();
        this.dateFormat = buildDateFormat();
    }



    /**
     * Helper method for taking a tax ID and adding hyphens
     * to put it in the NNN-NN-NNNN format.
     * 
     * @param unencryptedTaxId The unencrypted SSN/ITIN to format.
     * @return The formatted tax ID.
     */
    String buildFormattedTaxId(String unencryptedTaxId) {
        unencryptedTaxId.getChars(0, TAXID_SRC_CHUNK1_2_SPLIT, formattedTaxId, 0);
        unencryptedTaxId.getChars(TAXID_SRC_CHUNK1_2_SPLIT, TAXID_SRC_CHUNK2_3_SPLIT, formattedTaxId, TAXID_DEST_CHUNK2_START);
        unencryptedTaxId.getChars(TAXID_SRC_CHUNK2_3_SPLIT, TAXID_SRC_CHUNK3_END, formattedTaxId, TAXID_DEST_CHUNK3_START);
        
        return new String(formattedTaxId);
    }

    /**
     * Builds and returns an object for formatting numeric values representing amounts.
     * This will be called by the constructor to set the default amount formatter.
     * See the CUTaxConstants class for the pattern and max-int-digit configuration
     * used by the default implementation.
     * 
     * @return A new DecimalFormat for formatting amounts.
     */
    DecimalFormat buildAmountFormat() {
        DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_AMOUNT_MAX_INT_DIGITS);
        return newFormat;
    }

    /**
     * Builds and returns an object for formatting numeric values representing percents.
     * This will be called by the constructor to set the default percent formatter.
     * See the CUTaxConstants class for the pattern and max-int-digit configuration
     * used by the default implementation.
     * 
     * @return A new DecimalFormat for formatting percents.
     */
    DecimalFormat buildPercentFormat() {
        DecimalFormat newFormat = new DecimalFormat(CUTaxConstants.DEFAULT_PERCENT_FORMAT, new DecimalFormatSymbols(Locale.US));
        newFormat.setMaximumIntegerDigits(CUTaxConstants.DEFAULT_PERCENT_MAX_INT_DIGITS);
        return newFormat;
    }

    /**
     * Builds and returns an object for formatting date values.
     * This will be called by the constructor to set the default date formatter.
     * The default implementation builds a SimpleDateFormat using the given pattern
     * from the CUTaxConstants class.
     * 
     * @return A new DateFormat for formatting dates.
     */
    DateFormat buildDateFormat() {
        return new SimpleDateFormat(CUTaxConstants.DEFAULT_DATE_FORMAT, Locale.US);
    }

    /**
     * Builds and returns an object for formatting date-time values
     * to be used as the end of a filename.
     * 
     * @return A new DateFormat for formatting date-times as filename suffixes.
     */
    DateFormat buildDateFormatForFileSuffixes() {
        return new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
    }



    String getReportsDirectory() {
        return reportsDirectory;
    }

    void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }



    /**
     * Helper method for determining whether an inclusion or exclusion should
     * be performed based on if a value matches any of the given patterns, or
     * whether no inclusions/exclusions should be determined at all.
     * The uppercase version of the value will be used for the checking.
     * 
     * @param value The value to test; may be blank.
     * @param patterns The patterns to test the value against; may be null.
     * @param isWhitelist Indicates whether the patterns list represents a whitelist (true) or a blacklist (false).
     * @return Null if value is blank or patterns is null, true if a value was found in a whitelist or not found in a blacklist, false otherwise.
     */
    Boolean determineClusionWithPatterns(String value, List<Pattern> patterns, boolean isWhitelist) {
        if (StringUtils.isBlank(value) || patterns == null) {
            // Just return null if no inclusion/exclusion check is needed, due to no value to test or no patterns to test against.
            return null;
        }
        
        // Check if the value matches at least one of the given patterns.
        boolean foundMatch = false;
        value = value.toUpperCase(Locale.US);
        for (int i = patterns.size() - 1; !foundMatch && i >= 0; i--) {
            if (patterns.get(i).matcher(value).matches()) {
                foundMatch = true;
            }
        }
        
        // Return a Boolean value based on whether a match was found and whether the patterns represented a whitelist or blacklist.
        return Boolean.valueOf(foundMatch == isWhitelist);
    }



    /**
     * Builds and returns a "piece" configured for handling the specified type of field.
     * Subclasses can store a reference to the created object accordingly if they need to manually
     * handle its behind-the-scenes value.
     * 
     * <p>Note that pieces with a type of BLANK or STATIC will be created by the calling code instead.</p>
     * 
     * @param fieldSource The type of "piece" being created; cannot be null or equal BLANK or STATIC.
     * @param field The field to build a piece for; cannot be null.
     * @param name The field's name; cannot be null.
     * @param len The field's max length or exact length; cannot be non-positive.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A "piece" object configured to handle the value of the given field.
     */
    abstract RecordPiece getPieceForField(TaxFieldSource fieldSource, TaxTableField field, String name, int len, T summary);

    /**
     * Retrieves a Set containing the minimal fields that this builder needs to store references to
     * for the given field type. In the event that any fields from the Set do not get
     * created during the regular piece-building process, the calling code will invoke the
     * getPieceForField() method for each remaining element in the returned Set.
     * 
     * @param pieceType The type of "pieces" to create a Set for; cannot be null or equal BLANK or STATIC.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A Set containing the "pieces" that will always be built by the getPieceForField() method.
     */
    abstract Set<TaxTableField> getMinimumFields(TaxFieldSource fieldSource, T summary);

    /**
     * Sets the "pieces" that may need special management by this processor.
     * The keys are the full property names of the associated TaxTableField objects.
     * 
     * @param complexPieces The transaction detail "pieces" that were created; cannot be null.
     * @param summary The object encapsulating the tax-type-specific summary info.
     */
    abstract void setComplexPieces(Map<String,RecordPiece> complexPieces, T summary);



    /**
     * Returns an array of Strings representing the SQL to use for any extra prepared statements
     * needed by the tax processing. One prepared statement will be created for each String.
     * The indexes of each String will correspond to their generated PreparedStatements
     * that are passed to this superclass via the setExtraStatement() method.
     * 
     * <p>The default implementation creates an array large enough to accommodate the
     * number of extra statements, and sets the first two elements to SQL Strings
     * for retrieving vendors and doc notes, respectively (but only if the array length
     * is 2 or higher). Subclasses must fill in any other elements.</p> 
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return An array of SQL statements to create PreparedStatement instances for.
     */
    String[] getSqlForExtraStatements(T summary) {
        String[] extraSql = new String[extraStatements.length];
        if (extraSql.length >= CUTaxConstants.DEFAULT_EXTRA_RS_SIZE) {
            extraSql[CUTaxConstants.VENDOR_DETAIL_INDEX] = TaxSqlUtils.getVendorSelectSql(summary.vendorRow);
            extraSql[CUTaxConstants.DOC_NOTES_INDEX] = TaxSqlUtils.getDocNotesSelectSql(summary.documentNoteRow);
        }
        
        return extraSql;
    }

    /**
     * Returns the parameter values that should be set on the PreparedStatement that was built
     * from the SQL at the corresponding index of the array returned by getSqlForExtraStatements().
     * See the getParameterValuesForSelect() method for information on the returned array's
     * expected format. Optionally, this method may return null to indicate that no defaults should be set.
     * 
     * <p>The default implementation returns null, to indicate that no defaults should be set.</p>
     * 
     * @param statementIndex The index of the PreparedStatement corresponding to the matching getSqlForExtraStatements() return value.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A two-dimensional Object array defining the parameters to set for the matching extra query, or null to skip default values setup.
     */
    Object[][] getDefaultParameterValuesForExtraStatement(int statementIndex, T summary) {
        return null;
    }

    /**
     * Returns an array of Strings representing the paths of the files to write to.
     * The indexes of each String will correspond to their generated Writers
     * that are passed to this superclass via the setWriter() method.
     * 
     * <p>The default implementation creates an array large enough to accommodate the
     * number of writers. Subclasses must fill in the elements themselves.</p>
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @param processingStartDate The date-time when the current tax processing execution started.
     * @return An array of file paths to create Writer instances for.
     */
    String[] getFilePathsForWriters(T summary, LocalDateTime processingStartDate) {
        return new String[writers.length];
    }

    /**
     * Returns the SQL that should be used for retrieving the transaction detail rows to process.
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A String representing the transaction detail retrieval SQL.
     */
    abstract String getSqlForSelect(T summary);

    /**
     * Returns the parameter values that should be set on the PreparedStatement that was built
     * from the SQL returned by getSqlForSelect().
     * 
     * <p>The result should be a two-dimensional array configured as follows:</p>
     * 
     * <p>The first dimension defines each individual parameter. Index 0 refers to
     * query parameter 1, index 1 refers to query parameter 2, and so on.</p>
     * 
     * <p>The second dimension provides data on the parameter values. Index 0 contains
     * the value to set. Index 1, if defined, contains an Integer denoting the
     * JDBC type of the value. Index 2, if defined, contains an Integer denoting the
     * scale/length of the numeric or stream/reader value (default of zero).</p>
     * 
     * <p>For a given parameter, the second dimension is permitted to have a length
     * of 1 if the value is a String, java.sql.Date, or Integer. For all other
     * parameter types, the second dimension should have a length of 2 or higher.</p>
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A two-dimensional Object array defining the parameters to set for the transaction detail SELECT query.
     */
    abstract Object[][] getParameterValuesForSelect(T summary);

    /**
     * Performs the tax processing.
     * 
     * @param rs The ResultSet containing the transaction detail search results; it can only move forwards, but it is updatable.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @throws SQLException if a database access error occurs while processing.
     * @throws IOException if an I/O error occurs while processing.
     */
    abstract void processTaxRows(ResultSet rs, T summary) throws SQLException, IOException;



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
     * Sets the Writer instance at the given index.
     * 
     * @param writer The Writer to set.
     * @param writerIndex The index to associate with the Writer instance.
     * @throws IllegalStateException if a Writer instance already exists at the given index.
     */
    final void setWriter(Writer writer, int writerIndex) {
        if (writers[writerIndex] != null) {
            throw new IllegalStateException("A Writer is already defined for index " + Integer.toString(writerIndex));
        }
        writers[writerIndex] = writer;
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
    final void setupOutputBuffer(int bufferIndex, int bufferLength, List<? extends RecordPiece> pieces,
            boolean useExactLength, boolean addSeparatorChar, char separator) {
        if (outputHelpers[bufferIndex] != null) {
            throw new IllegalStateException("An output buffer is aleady set for index " + Integer.toString(bufferIndex));
        } else if (bufferLength < 0) {
            throw new IllegalArgumentException("bufferLength cannot be negative");
        }
        outputHelpers[bufferIndex] = new OutputHelper(bufferLength, pieces, useExactLength, addSeparatorChar, separator);
    }



    // ========================================================================================
    // Start of query-configuration-and-execution helper methods.
    // ========================================================================================

    /**
     * Configures the given PreparedStatement with the specified int parameter, then executes it
     * and returns the results.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameter.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, int arg1) throws SQLException {
        extraStatements[statementIndex].setInt(1, arg1);
        return getAndReferenceResults(rsIndex, extraStatements[statementIndex]);
    }

    /**
     * Configures the given PreparedStatement with the specified int parameters, then executes it
     * and returns the results.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @param arg2 The value to set for the second PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameters.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, int arg1, int arg2) throws SQLException {
        PreparedStatement statement = extraStatements[statementIndex];
        statement.setInt(1, arg1);
        statement.setInt(2, arg2);
        return getAndReferenceResults(rsIndex, statement);
    }

    /**
     * Configures the given PreparedStatement with the specified String parameter, then executes it
     * and returns the results.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameter.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, String arg1) throws SQLException {
        extraStatements[statementIndex].setString(1, arg1);
        return getAndReferenceResults(rsIndex, extraStatements[statementIndex]);
    }

    /**
     * Configures the given PreparedStatement with the specified String parameters, then executes it
     * and returns the results.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @param arg2 The value to set for the second PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameters.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, String arg1, String arg2) throws SQLException {
        PreparedStatement statement = extraStatements[statementIndex];
        statement.setString(1, arg1);
        statement.setString(2, arg2);
        return getAndReferenceResults(rsIndex, statement);
    }

    /**
     * Configures the given PreparedStatement with the specified parameter, then executes it
     * and returns the results. It is assumed that the parameter has a scale of zero.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @param arg1Type The JDBC type for the first PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameter.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, Object arg1, int arg1Type) throws SQLException {
        extraStatements[statementIndex].setObject(1, arg1, arg1Type);
        return getAndReferenceResults(rsIndex, extraStatements[statementIndex]);
    }

    /**
     * Configures the given PreparedStatement with the specified parameters, then executes it
     * and returns the results. It is assumed that the parameters have a scale of zero.
     * 
     * <p>See the multi-query-arg configureAndRunQuery() method for more details on this method's effects.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param arg1 The value to set for the first PreparedStatement parameter.
     * @param arg1Type The JDBC type for the first PreparedStatement parameter.
     * @param arg2 The value to set for the second PreparedStatement parameter.
     * @param arg2Type The JDBC type for the second PreparedStatement parameter.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameters.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, Object arg1, int arg1Type, Object arg2, int arg2Type) throws SQLException {
        PreparedStatement statement = extraStatements[statementIndex];
        statement.setObject(1, arg1, arg1Type);
        statement.setObject(2, arg2, arg2Type);
        return getAndReferenceResults(rsIndex, statement);
    }



    /**
     * Configures the given query, executes it, associates its results with the given index, and returns the ResultSet.
     * Any existing ResultSet for the given index will automatically be closed. Also, the returned ResultSet
     * will still be referenced and acted upon by this superclass, so the subclass does not need to worry
     * about explicitly closing it when finished.
     * 
     * <p>The other versions of the configureAndRunQuery() method should preferably be used instead of
     * this one, since they can set up low-arg-count queries much more efficiently.</p>
     * 
     * @param rsIndex The index to associate the returned ResultSet with.
     * @param statementIndex The index of the extra PreparedStatement to configure and run.
     * @param argValues The values to set for the PreparedStatement parameters, in order.
     * @param argTypes The JDBC types for the PreparedStatement parameters, in order.
     * @param argScales The scales of numeric values or the lengths of stream/reader values, in order; ignored for other types or integer-only types.
     * @return The ResultSet that was returned by executing the PreparedStatement after configuring it with the given parameters.
     * @throws SQLException if a database access error occurs.
     */
    final ResultSet configureAndRunQuery(int rsIndex, int statementIndex, Object[] argValues, int[] argTypes, int[] argScales) throws SQLException {
        PreparedStatement statement = extraStatements[statementIndex];
        
        for (int i = 0; i < argValues.length; i++) {
            statement.setObject(i + 1, argValues[i], argTypes[i], argScales[i]);
        }
        
        return getAndReferenceResults(rsIndex, statement);
    }



    /**
     * Closes any existing result set mapped to the given index,
     * runs the given query to get a new result set, associates
     * the new result with the index, and returns the result.
     * The current implementation uses the no-arg executeQuery()
     * method on the statement to get the results.
     * 
     * @param index The index of the result set to replace; must be nonnegative and smaller than the max value that was passed to the constructor.
     * @param pStatement The prepared statement to execute.
     * @return The results obtained from running the query.
     * @throws SQLException if a database access error occurs.
     */
    private ResultSet getAndReferenceResults(int index, PreparedStatement pStatement) throws SQLException {
        // Close current result set for index if one exists.
        ResultSet rs = extraResultSets[index];
        if (rs != null) {
            rs.close();
        }
        // Run the query, and store and return the results.
        rs = pStatement.executeQuery();
        extraResultSets[index] = rs;
        return rs;
    }

    // ========================================================================================
    // End of query-configuration-and-execution helper methods.
    // ========================================================================================



    /**
     * Closes any non-null result sets, prepared statements,
     * and writers that have been stored by this processor,
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
        
        // Close any Writers.
        for (Writer writer :writers) {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOG.warn("Could not close writer");
                }
            }
        }
    }

    /**
     * Clears out the contents of the various internal arrays,
     * resets numbers to zero and sets chars to a static value,
     * and calls the clearReferences() method.
     */
    final void clearArraysAndReferences() {
        Arrays.fill(extraResultSets, null);
        Arrays.fill(extraStatements, null);
        Arrays.fill(writers, null);
        
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
     * Helper method that subclasses can use to clear out
     * any of their references that are no longer needed
     * once all processing and logging is complete.
     */
    abstract void clearReferences();



    /**
     * Resets the insertion index of the given buffer to zero.
     * Does NOT reset the buffer contents, since it is assumed
     * that the relevant portions of the buffer will be
     * overwritten for the next output operation anyway.
     * 
     * @param bufferIndex The index of the buffer to reset.
     */
    final void resetBuffer(int bufferIndex) {
        outputHelpers[bufferIndex].position = 0;
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
        OutputHelper helper = outputHelpers[bufferIndex];
        String tempValue;
        int tempLen;
        
        final String defaultStringIfBlank = helper.useExactLengths ? KRADConstants.BLANK_SPACE : KFSConstants.EMPTY_STRING;
        
        // Append the String values of the pieces in order, right-padding or truncating as needed.
        for (RecordPiece piece : helper.outputPieces) {
            if (piece.alwaysBlank) {
                // If the value is unconditionally blank, then just fill with spaces as necessary.
                if (helper.useExactLengths) {
                    Arrays.fill(helper.outputBuffer, helper.position, helper.position + piece.len, ' ');
                    helper.position += piece.len;
                }
                
            } else {
                // Otherwise, prepare to append the value.
                tempValue = StringUtils.defaultIfBlank(piece.getValue(), defaultStringIfBlank);
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
     * Appends one char buffer with the contents of another char buffer.
     * If the source buffer has separator characters between each field,
     * then a trailing separator will also be put in the destination buffer.
     * 
     * @param sourceBufferIndex The index of the char buffer to copy from.
     * @param destBufferIndex The index of the char buffer to copy to.
     */
    final void appendBuffer(int sourceBufferIndex, int destBufferIndex) {
        OutputHelper sourceHelper = outputHelpers[sourceBufferIndex];
        
        // Validate source buffer length before proceeding.
        if (sourceHelper.useExactLengths) {
            // If exact-length output, make sure that the buffer has been completely written over with new content.
            if (sourceHelper.position != sourceHelper.outputBuffer.length) {
                throw new IllegalStateException("Source buffer was not completely full but should have been! Index: " + Integer.toString(sourceBufferIndex)
                        + ", expected source length: " + Integer.toString(sourceHelper.outputBuffer.length)
                        + ", actual source length: " + Integer.toString(sourceHelper.position));
            }
            
        } else if (sourceHelper.position == 0) {
            // If variable-length output, do not allow for outputting an empty buffer.
            throw new IllegalStateException("Source buffer was empty but should not have been! Index: " + Integer.toString(sourceBufferIndex));
        }
        
        // Copy the contents of the source buffer to the destination one, including a trailing separator character (if any).
        System.arraycopy(
                sourceHelper.outputBuffer, 0, outputHelpers[destBufferIndex].outputBuffer, outputHelpers[destBufferIndex].position, sourceHelper.position);
        
        // Update insertion position of the destination buffer.
        outputHelpers[destBufferIndex].position += sourceHelper.position;
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
        OutputHelper helper = outputHelpers[bufferIndex];
        
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
        writers[writerIndex].write(helper.outputBuffer, 0, helper.position - (helper.addSeparatorChar ? 1 : 0));
        writers[writerIndex].write('\n');
    }



    /*
     * ============================================================================================
     * Below is a helper object for managing a section of tax input and output data.
     * ============================================================================================
     */

    /**
     * Helper object encapsulating data about a specific section of tax input and output.
     */
    private static final class OutputHelper {
        // The output character buffer to write to.
        private final char[] outputBuffer;
        // The "piece" objects that will generate the String content to append to the buffer.
        private final RecordPiece[] outputPieces;
        // Indicates whether the max lengths of the buffer and each "piece" should be treated as exact lengths.
        private final boolean useExactLengths;
        // Indicates whether a separator character should be added between the output of each "piece".
        private final boolean addSeparatorChar;
        // The separator character to use if addSeparatorChar is set to true.
        private final char separator;
        
        // The current insertion position for the character buffer.
        private int position;
        
        private OutputHelper(int bufferLength, List<? extends RecordPiece> pieces, boolean useExactLengths, boolean addSeparatorChar, char separator) {
            this.outputBuffer = new char[bufferLength + (addSeparatorChar ? 1 : 0)];
            this.outputPieces = pieces.toArray(new RecordPiece[pieces.size()]);
            this.useExactLengths = useExactLengths;
            this.addSeparatorChar = addSeparatorChar;
            this.separator = separator;
        }
        
    }



    /*
     * ============================================================================================
     * Below are helper objects for encapsulating values read from or derived from the detail rows,
     * and which will potentially be included in the output files.
     * ============================================================================================
     */

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
         * @param name The name of the field represented by this piece.
         * @param len The max String length of this piece, for right-padding or truncation purposes.
         * @param alwaysBlank Indicates whether the String value of this piece is unconditionally blank.
         * @throws IllegalArgumentException if name is blank or len is non-positive.
         */
        RecordPiece(String name, int len, boolean alwaysBlank) {
            if (StringUtils.isBlank(name)) {
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
     * Convenience RecordPiece subclass that represents
     * an unconditionally-blank field.
     */
    static final class AlwaysBlankRecordPiece extends RecordPiece {
        AlwaysBlankRecordPiece(String name, int len) {
            super(name, len, true);
        }
        
        /**
         * Implemented to always return null.
         * 
         * @see edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.RecordPiece#getValue()
         */
        @Override
        String getValue() throws SQLException {
            return null;
        }
    }



    /**
     * Convenience RecordPiece subclass that represents
     * a field with a constant value.
     */
    static final class StaticStringRecordPiece extends RecordPiece {
        // The constant value.
        final String value;
        
        StaticStringRecordPiece(String name, int len, String value) {
            super(name, len, false);
            this.value = value;
        }
        
        /**
         * Implemented to always return the same value that was passed to the constructor.
         * 
         * @see edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.RecordPiece#getValue()
         */
        @Override
        String getValue() throws SQLException {
            return value;
        }
    }



    /**
     * Convenience RecordPiece subclass that stores
     * the index of the column to be retrieved, in case
     * the value is obtained from a ResultSet.
     */
    abstract static class IndexedColumnRecordPiece extends RecordPiece {
        // the index of the column to retrieve.
        final int columnIndex;
        
        IndexedColumnRecordPiece(String name, int len, int columnIndex) {
            super(name, len, false);
            this.columnIndex = columnIndex;
        }
    }



    /**
     * Convenience RecordPiece subclass (as an *inner* class)
     * that formats numeric values as amounts or percents.
     */
    abstract class FormattedNumberRecordPiece extends IndexedColumnRecordPiece {
        // A flag indicating whether to use amount or percent formatter.
        final boolean useAmountFormat;
        
        FormattedNumberRecordPiece(String name, int len, int columnIndex, boolean useAmountFormat) {
            super(name, len, columnIndex);
            this.useAmountFormat = useAmountFormat;
        }
        
        /**
         * Overridden to get the actual value from the getNumericValue() method and then format it.
         * 
         * @see edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.RecordPiece#getValue()
         */
        @Override
        String getValue() throws SQLException {
            return useAmountFormat ? amountFormat.format(getNumericValue()) : percentFormat.format(getNumericValue());
        }
        
        /**
         * Retrieves the actual numeric value to be formatted. It is up to subclasses
         * to deal with potentially-null values accordingly.
         * 
         * @return The numeric object to be formatted.
         * @throws SQLException if a database access error occurs when attempting to get the numeric value.
         */
        abstract Object getNumericValue() throws SQLException;
    }



    /**
     * Convenience RecordPiece subclass (as an *inner* class)
     * that formats date values.
     */
    abstract class FormattedDateRecordPiece extends IndexedColumnRecordPiece {
        FormattedDateRecordPiece(String name, int len, int columnIndex) {
            super(name, len, columnIndex);
        }
        
        /**
         * Overridden to get the actual value from the getDateValue() method and then format it.
         * 
         * @see edu.cornell.kfs.tax.dataaccess.impl.TransactionRowProcessor.RecordPiece#getValue()
         */
        @Override
        String getValue() throws SQLException {
            return dateFormat.format(getDateValue());
        }
        
        /**
         * Retrieves the actual date value to be formatted. It is up to subclasses
         * to deal with potentially-null values accordingly.
         * 
         * @return The date object to be formatted.
         * @throws SQLException if a database access error occurs when attempting to get the date value.
         */
        abstract Object getDateValue() throws SQLException;
    }

}
