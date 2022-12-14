package edu.cornell.kfs.tax.dataaccess.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DocumentNoteRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TaxSourceRowWithVendorData;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorAddressRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.VendorRow;


/**
 * Helper class containing various constants, enums and methods to simplify
 * the process of retrieving and storing tax-related data via SQL.
 * 
 * <p>The SqlText enum provides various constants to simplify the process
 * of building SQL for tax data reading/writing. The constants' toString()
 * methods will also return the desired SQL fragment, as an added convenience.</p>
 * 
 * <p>Some of the main features of this class are the "getQuery..."
 * and "appendQuery..." methods, which allow for easily constructing full
 * or partial SQL based on various given object values. See the Javadocs
 * on these methods for details on how to create query strings.</p>
 * 
 * <p>In addition, there are several convenience methods for constructing
 * very specific SELECT, INSERT, or DELETE queries. Their Javadocs contain
 * more information on how to properly use them.</p>
 */
final class TaxSqlUtils {
    static final String YES_STRING_CONST = "'Y'";

    private static final Logger LOG = LogManager.getLogger(TaxSqlUtils.class);

    private static final int MAX_IN_LIST_SIZE = 1000;
    private static final int MED_BUILDER_SIZE = 100;
    private static final int SB_START_SIZE = 500;

    private TaxSqlUtils() {
        throw new UnsupportedOperationException("do not call");
    }



    /*
     * ============================================================================================
     * Start of helper enums.
     * ============================================================================================
     */

    /**
     * Convenience enum representing various SQL fragments. Those that start a query
     * typically have a trailing space, those that represent sorting order typically
     * have a leading space, and those that appear in the middle of a query
     * usually have both leading and trailing spaces. The exceptions are as follows:
     * 
     * <ul>
     *   <li>COMMA - has no leading space.</li>
     *   <li>PARAMETER - has no spaces.</li>
     *   <li>PAREN_OPEN - has no spaces.</li>
     *   <li>PAREN_CLOSE - has no spaces.</li>
     *   <li>NOT - has no leading space.</li>
     *   <li>IS_NULL - has no trailing space.</li>
     *   <li>IS_NOT_NULL - has no trailing space.</li>
     * </ul>
     * 
     * <p>For convenience, the toString() enum values will return the intended text
     * fragments. This simplifies the process of using these constants as StringBuilder
     * arguments and such.</p>
     */
    static enum SqlText {
        SELECT("SELECT "),
        INSERT_INTO("INSERT INTO "),
        UPDATE("UPDATE "),
        DELETE_FROM("DELETE FROM "),
        FROM(" FROM "),
        WHERE(" WHERE "),
        ORDER_BY(" ORDER BY "),
        VALUES(" VALUES "),
        COMMA(", "),
        PARAMETER("?"),
        PAREN_OPEN("("),
        PAREN_CLOSE(")"),
        AS(" AS "),
        AND(" AND "),
        OR(" OR "),
        NOT("NOT "),
        EQUALS(" = "),
        CONDITIONAL_EQUALS_JOIN(" (+) = "),
        NOT_EQUAL(" <> "),
        IS_NULL(" IS NULL"),
        IS_NOT_NULL(" IS NOT NULL"),
        BETWEEN(" BETWEEN "),
        IN(" IN "),
        ASC(" ASC"),
        DESC(" DESC"),
        ASC_NULLS_FIRST(" ASC NULLS FIRST"),
        DESC_NULLS_LAST(" DESC NULLS LAST");
        
        final String sqlPiece;
        
        private SqlText(String sqlPiece) {
            this.sqlPiece = sqlPiece;
        }
        
        @Override
        public String toString() {
            return sqlPiece;
        }
    }

    /*
     * ============================================================================================
     * End of helper enums.
     * ============================================================================================
     */



    /**
     * Convenience method for building SQL from various objects, with the
     * output containing fully-qualified column names. Behaves as if
     * the appendQuery() method was passed an empty StringBuilder and then
     * returned its contents in String form afterwards.
     * 
     * <p>Please see the appendQuery() method for details on how to use this.</p>
     * 
     * @param queryFragments The various objects making up the full or partial query.
     * @return A String representing a full or partial query.
     */
    static String getQuery(Object... queryFragments) {
        StringBuilder builder = new StringBuilder(SB_START_SIZE);
        appendQueryInternal(builder, true, queryFragments);
        return builder.toString();
    }

    /**
     * Convenience method for appending SQL built from various objects to the given StringBuilder.
     * 
     * <p>The appropriate String values of the various fragments will be appended in order.
     * Both full-query and partial-query construction are supported.</p>
     * 
     * <p>The various fragment types are handled as follows:</p>
     * 
     * <ul>
     *   <li>Null object - Not valid; an exception will be thrown.</li>
     *   <li>SqlText enum constant - Will append the constant's custom toString() value.</li>
     *   <li>String - Will append the String as-is.</li>
     *   <li>TaxTableField object - Will append the field's fully-qualified column name.</li>
     *   <li>Integer object - Will append "?, ?, ... , ?" (a list of comma-separated parameters to match given quantity); integer value must be positive.</li>
     *   <li>List of TaxTableField objects - Will append the fields as comma-separated fully-qualified column names; list cannot be empty.</li>
     *   <li>List of String objects - Will append as comma-separated table names, and auto-include aliases of A0, A1, etc.; list cannot be empty.</li>
     *   <li>Object[][] array - Will append the fields and sort directions for an ORDER BY clause; see below for details.</li>
     *   <li>All other values - Not valid; an exception will be thrown.</li>
     * </ul>
     * 
     * <p>To properly build an ORDER BY clause, add the SqlText.ORDER_BY constant as a fragment,
     * and then have the subsequent fragment be an Object[][] array configured as follows:</p>
     * 
     * <ul>
     *   <li>The first dimension must be non-empty.</li>
     *   <li>The inner Object[] arrays must be arranged in the order that you want the results to be sorted by.</li>
     *   <li>Each inner Object[] array must have a length of 1 or 2.</li>
     *   <li>The first element of each inner Object[] array must be a TaxTableField instance representing the field to sort by.</li>
     *   <li>If an inner Object[] array only has a length of 1, then default ascending sort order is assumed.</li>
     *   <li>If an inner Object[] array has a length of 2, then the second element must be a SqlText constant representing the sort order.</li>
     * </ul>
     * 
     * <p>If only relative column names are desired in the output, please use the
     * appendQueryWithoutColumnPrefixes() method instead.</p>
     * 
     * @param builder The StringBuilder to append the SQL to.
     * @param queryFragments The various objects making up the full or partial query.
     */
    static void appendQuery(StringBuilder builder, Object... queryFragments) {
        appendQueryInternal(builder, true, queryFragments);
    }

    /**
     * Convenience method for building SQL from various objects, with the
     * output containing only relative column names. Behaves as if
     * the appendQueryWithoutColumnPrefixes() method was passed an empty
     * StringBuilder and then returned its contents in String form afterwards.
     * 
     * <p>Please see the appendQuery() method for details on how to use this.</p>
     * 
     * @param queryFragments The various objects making up the full or partial query.
     * @return A String representing a full or partial query.
     */
    static String getQueryWithoutColumnPrefixes(Object... queryFragments) {
        StringBuilder builder = new StringBuilder(SB_START_SIZE);
        appendQueryInternal(builder, false, queryFragments);
        return builder.toString();
    }

    /**
     * Convenience method for appending SQL built from various objects to the given StringBuilder,
     * but without using fully-qualified column names. Behaves just like the appendQuery() method,
     * except that only the relative portions of the column names will be appended.
     * 
     * <p>Please see the appendQuery() method for details on how to use this.</p>
     * 
     * @param builder The StringBuilder to append the SQL to.
     * @param queryFragments The various objects making up the full or partial query.
     */
    static void appendQueryWithoutColumnPrefixes(StringBuilder builder, Object... queryFragments) {
        appendQueryInternal(builder, false, queryFragments);
    }

    /*
     * This is the main method that handles the processing of the other query-building methods of this class.
     */
    private static void appendQueryInternal(StringBuilder builder, boolean includeColumnPrefixes, Object... queryFragments) {
        if (queryFragments.length == 0) {
            throw new IllegalArgumentException("fragments array cannot be empty");
        }
        
        // Convert the fragments into Strings and append them to the builder.
        for (Object fragment : queryFragments) {
            if (fragment == null) {
                // Null fragment, throw an exception.
                throw new IllegalArgumentException("Invalid null fragment found");
                
            } else if (fragment instanceof SqlText) {
                // SqlText enum constant, append toString() value.
                builder.append(fragment);
                
            } else if (fragment instanceof String) {
                // Plain string, just append it.
                builder.append((String) fragment);
                
            } else if (fragment instanceof TaxTableField) {
                // Table field, append fully-qualified column name or relative column name as appropriate.
                builder.append(includeColumnPrefixes ? ((TaxTableField) fragment).columnName : ((TaxTableField) fragment).getRelativeColumnName());
                
            } else if (fragment instanceof Integer) {
                // Integer indicating parameter count, append comma-separated list of parameter placeholders.
                if (((Integer) fragment).intValue() <= 0) {
                    throw new IllegalArgumentException("Parameter list count cannot be non-positive");
                }
                for (int i = ((Integer) fragment).intValue(); i > 0; i--) {
                    builder.append(SqlText.PARAMETER).append(SqlText.COMMA);
                }
                // Remove trailing comma.
                builder.delete(builder.length() - SqlText.COMMA.sqlPiece.length(), builder.length());
                
            } else if (fragment instanceof List) {
                // List, determine element type and append contents accordingly.
                List<?> itemList = (List<?>) fragment;
                if (itemList.isEmpty()) {
                    throw new IllegalArgumentException("item list cannot be empty");
                } else if (itemList.get(0) instanceof TaxTableField) {
                    // List of fields, append in comma-separated fashion.
                    for (Object tableField : itemList) {
                        builder.append(includeColumnPrefixes ? ((TaxTableField) tableField).columnName : ((TaxTableField) tableField).getRelativeColumnName());
                        builder.append(SqlText.COMMA);
                    }
                    // Remove trailing comma.
                    builder.delete(builder.length() - SqlText.COMMA.sqlPiece.length(), builder.length());
                    
                } else if (itemList.get(0) instanceof String) {
                    // List of table names, append in comma-separated fashion with aliases of A0, A1, etc.
                    int i = 0;
                    for (Object tableName : itemList) {
                        if (StringUtils.isBlank((String) tableName)) {
                            throw new IllegalArgumentException("Cannot append blank table names");
                        }
                        builder.append((String) tableName).append(' ').append('A').append(i);
                        builder.append(SqlText.COMMA);
                        i++;
                    }
                    // Remove trailing comma.
                    builder.delete(builder.length() - SqlText.COMMA.sqlPiece.length(), builder.length());
                    
                } else {
                    throw new IllegalArgumentException("Unrecognized list element types.");
                }
                
            } else if (fragment instanceof Object[][]) {
                // Fields and sort directions for ORDER BY clause, append accordingly.
                Object[][] orderByFields = (Object[][]) fragment;
                if (orderByFields.length == 0) {
                    throw new IllegalArgumentException("Cannot append zero-length ORDER BY field configuration");
                }
                for (Object[] orderByField : orderByFields) {
                    if (orderByField.length == 0) {
                        throw new IllegalArgumentException("Cannot have empty array for ORDER BY field fragment");
                    } else if (orderByField.length <= 2) {
                        // Proper array length, continue with appending.
                        SqlText sortDirection;
                        if (orderByField[0] == null || !(orderByField[0] instanceof TaxTableField)) {
                            throw new IllegalArgumentException("Cannot have null or non-field start of ORDER BY field fragment");
                        } else if (orderByField.length == 2) {
                            // Sort direction given, use the provided one.
                            if (orderByField[1] == null || !(orderByField[1] instanceof SqlText)) {
                                throw new IllegalArgumentException("Cannot have null or non-sort end of ORDER BY field fragment");
                            }
                            sortDirection = (SqlText) orderByField[1];
                        } else {
                            // Sort direction not specified, default to ascending.
                            sortDirection = SqlText.ASC;
                        }
                        // Append ORDER BY piece.
                        builder.append(includeColumnPrefixes
                                ? ((TaxTableField) orderByField[0]).columnName : ((TaxTableField) orderByField[0]).getRelativeColumnName());
                        builder.append(sortDirection).append(SqlText.COMMA);
                    } else {
                        throw new IllegalArgumentException("Wrong argument count for ORDER BY field fragment");
                    }
                }
                // Remove trailing comma.
                builder.delete(builder.length() - SqlText.COMMA.sqlPiece.length(), builder.length());
                
            } else {
                // Unrecognized value, throw an exception.
                throw new IllegalArgumentException("Invalid input fragment found");
            }
        }
    }



    /**
     * Convenience method for creating an IN or NOT IN condition containing a potentially large number of parameter values.
     * If the needed item list is too large, then it will be broken up into a union/intersection of
     * multiple IN/NOT IN conditions (such as "IN (...) OR IN (...) ..." or "NOT IN (...) AND NOT IN(...) ...").
     * A block of split-up conditions will be surrounded by parentheses.
     * 
     * @param field The table field whose value will be compared against the list items.
     * @param size The size of the IN/NOT IN list; must be positive.
     * @param positive Indicates whether to create an IN condition (true) or a NOT IN condition (false).
     * @param includeColumnPrefixes Indicates whether to use the field's fully-qualified column name (true) or just the relative part (false).
     * @return A String representing a single IN/NOT IN condition, or a union/intersection of multiple IN/NOT IN conditions.
     */
    static String getInListCriteria(TaxTableField field, int size, boolean positive, boolean includeColumnPrefixes) {
        StringBuilder builder = new StringBuilder(SB_START_SIZE);
        appendInListCriteria(builder, field, size, positive, includeColumnPrefixes);
        return builder.toString();
    }

    /**
     * Convenience method for appending an IN or NOT IN condition containing a potentially large number of parameter values.
     * If the needed item list is too large, then it will be broken up into a union/intersection of
     * multiple IN/NOT IN conditions (such as "IN (...) OR IN (...) ..." or "NOT IN (...) AND NOT IN(...) ...").
     * A block of split-up conditions will be surrounded by parentheses.
     * 
     * @param builder The StringBuilder to append the SQL to.
     * @param field The table field whose value will be compared against the list items.
     * @param size The size of the IN/NOT IN list; must be positive.
     * @param positive Indicates whether to create an IN condition (true) or a NOT IN condition (false).
     * @param includeColumnPrefixes Indicates whether to use the field's fully-qualified column name (true) or just the relative part (false).
     */
    static void appendInListCriteria(StringBuilder builder, TaxTableField field, int size, boolean positive, boolean includeColumnPrefixes) {
        // For sizes equal to or below the maximum, just use one IN or NOT IN section.
        if (size <= MAX_IN_LIST_SIZE) {
            appendQueryInternal(builder, includeColumnPrefixes,
                    field,
                    positive ? KFSConstants.EMPTY_STRING : SqlText.NOT,
                    SqlText.IN, SqlText.PAREN_OPEN, Integer.valueOf(size), SqlText.PAREN_CLOSE);
            return;
        }
        
        // Otherwise, concatenate multiple ANDed or ORed IN/NOT IN sections.
        boolean beyondFirst = false;
        appendQueryInternal(builder, includeColumnPrefixes, SqlText.PAREN_OPEN);
        // Append blocks of max-size number of parameter placeholders.
        for (int i = size / MAX_IN_LIST_SIZE; i > 0; i--) {
            appendQueryInternal(builder, includeColumnPrefixes,
                    beyondFirst ? (positive ? SqlText.OR : SqlText.AND) : KFSConstants.EMPTY_STRING,
                    field,
                    positive ? KFSConstants.EMPTY_STRING : SqlText.NOT,
                    SqlText.IN, SqlText.PAREN_OPEN, Integer.valueOf(MAX_IN_LIST_SIZE), SqlText.PAREN_CLOSE);
            beyondFirst = true;
        }
        // Append any remaining needed parameter placeholders.
        if (size % MAX_IN_LIST_SIZE > 0) {
            appendQueryInternal(builder, includeColumnPrefixes,
                    positive ? SqlText.OR : SqlText.AND,
                    field,
                    positive ? KFSConstants.EMPTY_STRING : SqlText.NOT,
                    SqlText.IN, SqlText.PAREN_OPEN, Integer.valueOf(size % MAX_IN_LIST_SIZE), SqlText.PAREN_CLOSE);
        }
        // Close the block.
        appendQueryInternal(builder, includeColumnPrefixes, SqlText.PAREN_CLOSE);
    }



    /**
     * Convenience method for building the SQL that will be used for inserting
     * new transaction detail rows into KFS, based on the values in the
     * TransactionDetailRow object. The returned query will populate the primary key
     * using the associated sequence, meaning a "?" parameter will NOT be
     * included for that column in the SQL.
     * 
     * @param detailRow The TransactionDetailRow instance containing metadata about transaction detail read/write operations.
     * @return The transaction detail insertion SQL.
     */
    static String getTransactionDetailInsertSql(TransactionDetailRow detailRow) {
        StringBuilder insertSql = new StringBuilder(SB_START_SIZE);
        
        // Build the query.
        appendQueryWithoutColumnPrefixes(insertSql,
                SqlText.INSERT_INTO, detailRow.tables.get(0),
                SqlText.PAREN_OPEN, detailRow.orderedFields, SqlText.PAREN_CLOSE,
                SqlText.VALUES, SqlText.PAREN_OPEN, "TX_TRANSACTION_DETAIL_S.NEXTVAL", SqlText.COMMA,
                        Integer.valueOf(detailRow.orderedFields.size() - detailRow.insertOffset), SqlText.PAREN_CLOSE);
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final transaction detail insertion query: " + insertSql.toString());
        }
        return insertSql.toString();
    }

    /**
     * Convenience method for building the SQL that will be used for deleting
     * old transaction detail rows for the given report year and tax type.
     * 
     * @param taxType The type of tax processing (1099, 1042S, etc.) being performed.
     * @param detailRow The TransactionDetailRow instance containing metadata about transaction detail read/write operations.
     * @return The transaction detail deletion SQL.
     */
    static String getTransactionDetailDeleteSql(String taxType, TransactionDetailRow detailRow) {
        TaxTableField extraField;
        if (CUTaxConstants.TAX_TYPE_1099.equals(taxType)) {
            extraField = detailRow.form1099Box;
        } else if (CUTaxConstants.TAX_TYPE_1042S.equals(taxType)) {
            extraField = detailRow.form1042SBox;
        } else {
            throw new IllegalArgumentException("Unrecognized tax type");
        }
        
        StringBuilder deleteSql = new StringBuilder(SB_START_SIZE);
        
        // Build the deletion SQL.
        appendQueryWithoutColumnPrefixes(deleteSql,
                SqlText.DELETE_FROM, detailRow.tables.get(0),
                SqlText.WHERE,
                        detailRow.reportYear, SqlText.EQUALS, SqlText.PARAMETER,
                SqlText.AND,
                        extraField, SqlText.IS_NOT_NULL);
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final transaction detail deletion query: " + deleteSql.toString());
        }
        return deleteSql.toString();
    }

    /**
     * Convenience method for building the SQL that will be used for selecting
     * transaction detail rows from KFS, based on the values in the TransactionDetailRow object.
     * The returned query's WHERE clause will require the rows to have a given report year,
     * and will also require them to have another field be equal or not equal to a given value
     * (depending on the args given). In addition, its ORDER BY clause will sort the results
     * in ascending order, first by (encrypted) tax ID or doc ID (depending on flag), then by
     * income code, and finally by income code sub-type.
     * 
     * @param extraField The extra column to add to the WHERE clause.
     * @param detailRow The TransactionDetailRow instance containing metadata about transaction detail read/write operations.
     * @param equals Whether the extra column should have "=" or "<>" as the condition; true if the former, false if the latter.
     * @param forProcessingPhase Indicates whether to order primarily by (encrypted) tax ID (true) or document number with nulls first (false).
     * @return The transaction detail selection SQL, which expects parameters for the report year and the extra field.
     * @throws IllegalArgumentException if extraField is null.
     */
    static String getTransactionDetailSelectSql(TaxTableField extraField, TransactionDetailRow detailRow, boolean equals, boolean forProcessingPhase) {
        if (extraField == null) {
            throw new IllegalArgumentException("extraField cannot be null");
        }
        StringBuilder selectSql = new StringBuilder(SB_START_SIZE);
        
        // Build the query.
        appendQuery(selectSql,
                SqlText.SELECT, detailRow.orderedFields, SqlText.FROM, detailRow.tables,
                SqlText.WHERE,
                        detailRow.reportYear, SqlText.EQUALS, SqlText.PARAMETER,
                SqlText.AND,
                        extraField, equals ? SqlText.EQUALS : SqlText.NOT_EQUAL, SqlText.PARAMETER,
                SqlText.ORDER_BY, new Object[][]
                {
                    {forProcessingPhase ? detailRow.vendorTaxNumber : detailRow.documentNumber, forProcessingPhase ? SqlText.ASC : SqlText.ASC_NULLS_FIRST},
                    {detailRow.incomeCode},
                    {detailRow.incomeCodeSubType}
                });
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final transaction detail selection query: " + selectSql.toString());
        }
        return selectSql.toString();
    }

    /**
     * Convenience method for building the SQL that will be used to select document
     * notes for KFS-side docs. The returned query's WHERE clause expects a single
     * document ID parameter, which is used in a subquery for convenience in retrieving
     * all the notes of a given document.
     * 
     * @param docNoteRow The DocumentNoteRow instance containing metadata about document note read operations.
     * @return The document note selection SQL, which expects a parameter for the document ID.
     */
    static String getDocNotesSelectSql(DocumentNoteRow docNoteRow) {
        StringBuilder selectSql = new StringBuilder(SB_START_SIZE);
        
        // Build the query.
        appendQuery(selectSql,
                SqlText.SELECT, docNoteRow.orderedFields, SqlText.FROM, docNoteRow.tables,
                SqlText.WHERE,
                        docNoteRow.remoteObjectIdentifier, SqlText.EQUALS, "(SELECT OBJ_ID FROM FS_DOC_HEADER_T WHERE FDOC_NBR = ?)");
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final note selection query: " + selectSql.toString());
        }
        return selectSql.toString();
    }

    /**
     * Convenience method for building the SQL that will be used to select vendor details
     * and their accompanying header data. The returned query's WHERE clause expects two parameters:
     * One for the vendor header ID, and one for the vendor detail ID. (The matching vendor details
     * must either have the given detail ID or be a parent vendor.) In addition, the results will be
     * placed in ascending order of the vendor parent N/Y indicator (nulls-first).
     * 
     * @param vendorRow The VendorRow instance containing metadata about vendor read operations.
     * @return The vendor detail/header selection SQL, which expects parameters for the header ID and the detail ID.
     */
    static String getVendorSelectSql(VendorRow vendorRow) {
        StringBuilder selectSql = new StringBuilder(SB_START_SIZE);
        
        // Build the query.
        appendQuery(selectSql,
                SqlText.SELECT, vendorRow.orderedFields, SqlText.FROM, vendorRow.tables,
                SqlText.WHERE,
                        vendorRow.vendorDetailVendorHeaderGeneratedIdentifier, SqlText.EQUALS, SqlText.PARAMETER,
                SqlText.AND,
                        SqlText.PAREN_OPEN,
                                        vendorRow.vendorDetailAssignedIdentifier, SqlText.EQUALS, SqlText.PARAMETER,
                                SqlText.OR,
                                        vendorRow.vendorParentIndicator, SqlText.EQUALS, YES_STRING_CONST,
                        SqlText.PAREN_CLOSE,
                SqlText.AND,
                        vendorRow.vendorDetailVendorHeaderGeneratedIdentifier, SqlText.EQUALS, vendorRow.vendorHeaderGeneratedIdentifier,
                SqlText.ORDER_BY,
                        new Object[][] { {vendorRow.vendorParentIndicator, SqlText.ASC_NULLS_FIRST} });
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final multi-table vendor selection query: " + selectSql.toString());
        }
        return selectSql.toString();
    }

    /**
     * Convenience method for building the SQL that will be used to select vendor addresses.
     * The returned query's WHERE clause expects at least two parameters: Vendor header ID
     * and vendor detail ID. If the given flag is non-null, then a third parameter is
     * expected: The country code representing the United States. Also,
     * the given flag will adjust the WHERE clause accordingly so that it will either
     * retrieve only US addresses or only foreign addresses. (No adjustment is made for a null flag.)
     * In addition, the results will be placed in descending order of address type code,
     * and only addresses of type 'TX' (Tax), 'RM' (Remit) or 'PO' (Purchase Order) will be retrieved.
     * (They will also be placed in descending vendor address ID order, but the
     * type code ordering takes precedence.)
     * 
     * @param vendorAddressRow The VendorAddressRow instance containing metadata about vendor address read operations.
     * @param forUSAddress Indicates whether to have the query only retrieve US addresses (true) or only non-US addresses (false) or all addresses (null).
     * @return The vendor address selection SQL, which expects header ID and detail ID parms and may expect a US country code parm.
     */
    static String getVendorAddressSelectSql(VendorAddressRow vendorAddressRow, Boolean forUSAddress) {
        StringBuilder selectSql = new StringBuilder(SB_START_SIZE);
        
        // Build the query.
        appendQuery(selectSql,
                SqlText.SELECT, vendorAddressRow.orderedFields, SqlText.FROM, vendorAddressRow.tables,
                SqlText.WHERE,
                        vendorAddressRow.vendorHeaderGeneratedIdentifier, SqlText.EQUALS, SqlText.PARAMETER,
                SqlText.AND,
                        vendorAddressRow.vendorDetailAssignedIdentifier, SqlText.EQUALS, SqlText.PARAMETER);
        
        // If needed, add criteria to limit the results to US-only or foreign-only addresses.
        if (forUSAddress != null) {
            if (forUSAddress.booleanValue()) {
                // Limit to US-only addresses.
                appendQuery(selectSql,
                        SqlText.AND,
                        SqlText.PAREN_OPEN,
                                        vendorAddressRow.vendorCountryCode, SqlText.IS_NULL,
                                SqlText.OR,
                                        vendorAddressRow.vendorCountryCode, SqlText.EQUALS, SqlText.PARAMETER,
                        SqlText.PAREN_CLOSE);
            } else {
                // Limit to foreign-only addresses.
                appendQuery(selectSql,
                        SqlText.AND,
                                vendorAddressRow.vendorCountryCode, SqlText.IS_NOT_NULL,
                        SqlText.AND,
                                vendorAddressRow.vendorCountryCode, SqlText.NOT_EQUAL, SqlText.PARAMETER);
            }
        }
        
        // Finish building the query.
        appendQuery(selectSql,
                SqlText.AND,
                        vendorAddressRow.vendorAddressTypeCode, SqlText.IN, "('TX', 'RM', 'PO')",
                SqlText.AND,
                        vendorAddressRow.active, SqlText.EQUALS, YES_STRING_CONST,
                SqlText.ORDER_BY, new Object[][]
                {
                    {vendorAddressRow.vendorAddressTypeCode, SqlText.DESC},
                    {vendorAddressRow.vendorAddressGeneratedIdentifier, SqlText.DESC}
                });
        
        // Log and return the query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final vendor address selection query: " + selectSql.toString());
        }
        return selectSql.toString();
    }



    /**
     * Helper method for constructing query criteria involving vendor ownerships and vendor ownership categories.
     * Takes a map from ownership types to ownership categories and builds a criteria section accordingly.
     * 
     * <p>Any mappings with a non-empty ownership categories set will create a condition like the following:</p>
     * 
     * <p>(OWNER_CODE = ? AND OWNER_CATEGORY_CODE IS NOT NULL AND OWNER_CATEGORY_CODE IN (?, ?,..., ?))</p> 
     * 
     * <p>For mappings with an empty ownership categories set, another condition will be added at the end
     * of the block as follows:</p>
     * 
     * <p>OWNER_CODE IN (?, ?,..., ?)</p>
     * 
     * <p>The condition will be preceded with " AND " and will use the table aliases provided by the metadata object.</p>
     * 
     * @param ownerMappings The mappings from vendor ownership types to vendor ownership categories; cannot be null or have null values; may be empty.
     * @param vendorDataRow The TaxSourceWithVendorData instance containing metadata about vendor-header-related read operations.
     * @return A query condition string as described above.
     */
    static String getVendorOwnershipCriteria(Map<String,Set<String>> ownerMappings, TaxSourceRowWithVendorData vendorDataRow) {
        if (ownerMappings.isEmpty()) {
            // Just return an empty string if there are no ownership mappings.
            return KFSConstants.EMPTY_STRING;
        }
        StringBuilder criteria = new StringBuilder(SB_START_SIZE);
        int numOwnerMappingsWithoutCategories = 0;
        boolean needOrSeparator = false;
        
        // Create block start.
        appendQuery(criteria, SqlText.AND, SqlText.PAREN_OPEN);
        
        // Loop over ownership mappings and build the criteria.
        for (Map.Entry<String,Set<String>> ownerMapping : ownerMappings.entrySet()) {
            if (ownerMapping.getValue().isEmpty()) {
                numOwnerMappingsWithoutCategories++;
            } else {
                // Add a block that also expects a certain vendor category if the vendor has the given ownership type.
                if (needOrSeparator) {
                    appendQuery(criteria, SqlText.OR);
                }
                appendQuery(criteria,
                        SqlText.PAREN_OPEN,
                                        vendorDataRow.vendorOwnershipCode, SqlText.EQUALS, SqlText.PARAMETER,
                                SqlText.AND,
                                        vendorDataRow.vendorOwnershipCategoryCode, SqlText.IS_NOT_NULL,
                                SqlText.AND,
                                        vendorDataRow.vendorOwnershipCategoryCode, SqlText.IN,
                                                SqlText.PAREN_OPEN, Integer.valueOf(ownerMapping.getValue().size()), SqlText.PAREN_CLOSE,
                        SqlText.PAREN_CLOSE);
                
                needOrSeparator = true;
            }
        }
        
        // If necessary, add a block that just checks for ownership types, regardless of category.
        if (numOwnerMappingsWithoutCategories > 0) {
            if (needOrSeparator) {
                appendQuery(criteria, SqlText.OR);
            }
            appendQuery(criteria,
                    vendorDataRow.vendorOwnershipCode, SqlText.IN,
                            SqlText.PAREN_OPEN, Integer.valueOf(numOwnerMappingsWithoutCategories), SqlText.PAREN_CLOSE);
        }
        
        // End the block.
        appendQuery(criteria, SqlText.PAREN_CLOSE);
        
        // Return the result.
        return criteria.toString();
    }

    /**
     * Convenience method for constructing a query condition that checks if a payee ID is equal to a vendor header ID.
     * Because the payee ID likely has "-" and the detail ID as the suffix, it needs to be excluded from the value
     * being compared. Thus, the generated condition will end up in the following format:
     * 
     * <p>SUBSTR(PAYEE_ID_COL,1,(NVL(INSTR(PAYEE_ID_COL,'-',1,1),(LENGTH(PAYEE_ID_COL)+1))-1)) = VENDOR_HEADER_ID_COL</p>
     * 
     * @param payeeIdField The field representing the payee ID.
     * @param vendorHeaderIdField The field representing the vendor header ID.
     * @return A query condition to check if the payee ID (without the hyphen-plus-detailId suffix) is equal to the vendor header ID.
     */
    static String getPayeeIdToVendorHeaderIdCriteria(TaxTableField payeeIdField, TaxTableField vendorHeaderIdField) {
        return getQuery("SUBSTR(", payeeIdField, ",1,(NVL(INSTR(", payeeIdField, ",'-',1,1),(LENGTH(", payeeIdField, ")+1))-1))",
                SqlText.EQUALS, vendorHeaderIdField);
    }



    /**
     * Convenience method that analyzes the qualified table field name and returns
     * the index in the corresponding tables list that contains the name of the
     * table the field belongs to.
     * 
     * @param field The TaxTableField instance containing a fully-qualified column name.
     * @return The index where the field's table can be found in the row's table list.
     */
    static int getTableIndexForField(TaxTableField field) {
        return Integer.parseInt(field.columnName.substring(field.columnName.indexOf('A') + 1, field.columnName.indexOf('.')));
    }



    /**
     * Helper method for building a parameters array containing the vendor ownership args.
     * The constructed array will follow the format defined by the
     * TransactionRowBuilder.getParameterValuesForSelect() method. The array will contain
     * the given paramsBefore values, followed by the vendor owner and category params,
     * followed by the paramsAfter values.
     * 
     * <p>NOTE: For proper functionality, it is expected that this method uses the same
     * unaltered ownership map that was passed to the getVendorOwnershipCriteria() method.</p>
     * 
     * @param ownerMappings The mappings from vendor ownership types to vendor ownership categories; cannot be null or have null values; may be empty.
     * @param paramsBefore The parameters that should precede the vendor ownership ones in the final array; cannot be null.
     * @param paramsAfter The parameters that should come after the vendor ownership ones in the final array; cannot be null.
     * @return A two-dimensional Object array containing parameters as defined above.
     */
    static Object[][] getArgsArrayWithVendorOwnershipParameters(Map<String,Set<String>> ownerMappings, Object[][] paramsBefore, Object[][] paramsAfter) {
        List<String> ownerMappingsWithoutCategories = new ArrayList<String>();
        Object[][] finalParams;
        int finalSize = 0;
        int insertIdx = paramsBefore.length;
        
        // Compute final size.
        finalSize += paramsBefore.length;
        finalSize += paramsAfter.length;
        finalSize += ownerMappings.size();
        for (Set<String> categories : ownerMappings.values()) {
            finalSize += categories.size();
        }
        
        // Create final array.
        finalParams = new Object[finalSize][];
        
        // Copy the contents of the given arrays over to the final array.
        if (paramsBefore.length > 0) {
            System.arraycopy(paramsBefore, 0, finalParams, 0, paramsBefore.length);
        }
        if (paramsAfter.length > 0) {
            System.arraycopy(paramsAfter, 0, finalParams, finalParams.length - paramsAfter.length, paramsAfter.length);
        }
        
        // Add the ownership type and ownership category args to the final array.
        for (Map.Entry<String,Set<String>> ownerMapping : ownerMappings.entrySet()) {
            if (ownerMapping.getValue().isEmpty()) {
                // If no ownership categories for type, then add the ownership type later.
                ownerMappingsWithoutCategories.add(ownerMapping.getKey());
            } else {
                // Add the owner type and category types to the array.
                finalParams[insertIdx++] = new Object[] {ownerMapping.getKey()};
                for (String ownerCategory : ownerMapping.getValue()) {
                    finalParams[insertIdx++] = new Object[] {ownerCategory};
                }
            }
        }
        
        // Add the remaining ownership-type-only args to the final array.
        for (String ownershipCode : ownerMappingsWithoutCategories) {
            finalParams[insertIdx++] = new Object[] {ownershipCode};
        }
        
        return finalParams;
    }



    /**
     * Helper method for adding tax-source-specific stats to an EnumMap.
     * Three stats will be added: One for the given enum constant, one
     * for the associated constant with the "_DV" suffix, and one for
     * the associated constant with the "_PDP" suffix. The values of the
     * added stats are: The sum of the DV and PDP totals, the DV total,
     * and the PDP total, respectively.
     * 
     * @param statistics The EnumMap to add the new stats to.
     * @param totalStat The TaxStatType constant representing the grand total, and which has associated constants with "_DV" and "_PDP" suffixes.
     * @param dvTotal The value of the DV-specific statistic.
     * @param pdpTotal The value of the PDP-specific statistic.
     */
    static void addDvPdpTotalStats(EnumMap<TaxStatType,Integer> statistics, TaxStatType totalStat, int dvTotal, int pdpTotal) {
        TaxStatType tempType;
        
        // Add the statistic that sums the DV and PDP totals.
        statistics.put(totalStat, Integer.valueOf(dvTotal + pdpTotal));
        
        // Add the statistic for the DV total only.
        tempType = TaxStatType.valueOf(
                new StringBuilder(MED_BUILDER_SIZE).append(totalStat.name()).append('_').append(CUTaxConstants.TAX_SOURCE_DV).toString());
        statistics.put(tempType, Integer.valueOf(dvTotal));
        
        // Add the statistic for the PDP total only.
        tempType = TaxStatType.valueOf(
                new StringBuilder(MED_BUILDER_SIZE).append(totalStat.name()).append('_').append(CUTaxConstants.TAX_SOURCE_PDP).toString());
        statistics.put(tempType, Integer.valueOf(pdpTotal));
    }
}
