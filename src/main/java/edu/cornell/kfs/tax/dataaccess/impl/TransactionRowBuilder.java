package edu.cornell.kfs.tax.dataaccess.impl;

import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.api.KEWPropertyConstants;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResult;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResults;
import org.kuali.kfs.kew.docsearch.service.DocumentSearchService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;

/**
 * Base helper class for objects that construct transaction detail rows from KFS-side tax data.
 * 
 * @param <T> An implementation of the abstract TransactionDetailSummary class.
 */
abstract class TransactionRowBuilder<T extends TransactionDetailSummary> {
    static final int INSERTION_BATCH_SIZE = 500;
    private static final int DOC_ID_CRITERIA_SIZE = 5000;
    private static final int MAX_AUTO_TAXNUM_DIGITS = 8;
    private static final Logger LOG = LogManager.getLogger(TransactionRowBuilder.class);

    // Convenience map for associating payee IDs with auto-generated tax ID, in the event of encountering a vendor with a blank tax ID.
    private Map<String,String> nullTaxNumberReplacementsByPayeeId;
    // A counter indicating how many payees currently have a null tax ID.
    private int numNullTaxNumbersForDistinctPayees;
    // A counter indicating the total number of transaction rows with null tax IDs.
    private int numNullTaxNumbers;
    // An object for formatting auto-generated tax IDs.
    private DecimalFormat autoGenTaxNumFormat;

    // Helper variables for batch-related processing.
    private List<String> documentIds;
    private Iterator<String> documentIdsForBulkQuery;
    private List<DocumentRouteHeaderValue> documentsForBatch;
    private Iterator<DocumentRouteHeaderValue> documentsForProcessing;
    private DocumentRouteHeaderValue currentDocument;

    // Other statistics.
    private int numNullDocumentHeaders;
    private int numNoDocumentHeaders;
    private int numNoEntityName;
    private int numNoAccount;
    private int numNoOrg;

    // The various services used by all builders.
    private DocumentSearchService documentSearchService;
    private IdentityService identityService;
    private EncryptionService encryptionService;
    private AccountService accountService;
    private OrganizationService organizationService;

    private Integer maxSearchResultSize;



    /**
     * Gets the name of the tax source (DV, PDP, etc.) that this builder uses for creating
     * the transaction rows. It is primarily used for logging purposes.
     * 
     * @return The name of the source of the tax data used by this builder.
     */
    abstract String getTaxSourceName();

    /**
     * Gets the SQL for retrieving the tax source data from the appropriate KFS tables.
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A String representing the tax source retrieval SQL.
     */
    abstract String getSqlForSelect(T summary);

    /**
     * Gets the WHERE clause section of tax source data retrieval SQL that is specific
     * to the given type of tax reporting (1099, 1042S, etc.). It is expected that this
     * method will be called by the getSqlForSelect() method during its processing.
     * 
     * <p>If no extra SQL is needed for the given tax reporting, then this method
     * should return an empty String. Otherwise, it is recommended that the SQL
     * snippet should start with "AND"/"OR" and end with a trailing space.</p>
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A String representing the tax-type-specific WHERE clause condition(s).
     */
    abstract String getTaxTypeSpecificConditionForSelect(T summary);

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
     * @return A two-dimensional Object array defining the parameters to set for the tax source data SELECT query.
     */
    abstract Object[][] getParameterValuesForSelect(T summary);



    /**
     * Gets the SQL for retrieving the transaction rows built by the buildTransactionRows() method.
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A String representing the tax source retrieval SQL.
     */
    abstract String getSqlForSelectingCreatedRows(T summary);

    /**
     * Returns the parameter values that should be set on the PreparedStatement that was built
     * from the SQL returned by getSqlForSelecttingCreatedRows(). See the
     * getParameterValuesForSelect() method for details on what data is
     * expected in the returned array.
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A two-dimensional Object array defining the parameters to set for the transaction row SELECT query.
     */
    abstract Object[][] getParameterValuesForSelectingCreatedRows(T summary);



    /**
     * Creates transaction rows from the given source.
     * 
     * @param rs The ResultSet containing the tax source data; can only move forwards and is read-only.
     * @param insertStatement The PreparedStatement to use for inserting new transaction rows.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @throws SQLException
     */
    abstract void buildTransactionRows(ResultSet rs, PreparedStatement insertStatement, T summary) throws SQLException;

    /**
     * Helper method for performing tax-type-specific setup in the transaction row creation process.
     * This method is typically called by the buildTransactionRows() method at some point.
     * 
     * @param rs The ResultSet containing the tax source data; can only move forwards and is read-only.
     * @param insertStatement The PreparedStatement to use for inserting new transaction rows.
     * @param financialObjectCode The object code related to the current tax source row; may be blank.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @throws SQLException
     */
    abstract void doTaxSpecificRowSetup(ResultSet rs, PreparedStatement insertStatement, String financialObjectCode, T summary) throws SQLException;

    /**
     * Helper method for performing tax-type-specific setup in the second pass of the transaction row creation process.
     * This method is typically called by the updateTransactionRowsFromWorkflowDocuments() method at some point.
     * 
     * @param rs The ResultSet containing the generated transaction rows; can only move forwards, but is updatable.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @throws SQLException
     */
    abstract void doTaxSpecificSecondPassRowSetup(ResultSet rs, T summary) throws SQLException;



    /**
     * Performs another iteration over the transaction rows created by the buildTransactionRows() method,
     * to perform updates based on workflow-document-related data.
     * 
     * @param rs The ResultSet containing the transaction row data; can only move forwards, but is updatable.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @throws SQLException
     */
    abstract void updateTransactionRowsFromWorkflowDocuments(ResultSet rs, T summary) throws SQLException;



    /**
     * Helper method for copying certain data over to this builder from the previous one, if any.
     * If there was no previous builder, then perform first-in-the-chain initialization instead.
     * Also perform any other initialization as needed.
     * 
     * @param builder The previously-executed builder in the chain; may be null.
     * @param processingDao the DAO pertaining to the tax processing, in case any other data needs retrieving for setup.
     * @param summary The object encapsulating the tax-type-specific summary info.
     */
    void copyValuesFromPreviousBuilder(TransactionRowBuilder<?> builder, TaxProcessingDao processingDao, T summary) {
        if (builder != null) {
            this.nullTaxNumberReplacementsByPayeeId = builder.nullTaxNumberReplacementsByPayeeId;
            this.autoGenTaxNumFormat = builder.autoGenTaxNumFormat;
            this.documentSearchService = builder.documentSearchService;
            this.identityService = builder.identityService;
            this.encryptionService = builder.encryptionService;
            this.accountService = builder.accountService;
            this.organizationService = builder.organizationService;
        } else {
            this.nullTaxNumberReplacementsByPayeeId = new HashMap<String,String>();
            this.autoGenTaxNumFormat = new DecimalFormat("~00000000", new DecimalFormatSymbols(Locale.US));
            this.autoGenTaxNumFormat.setMaximumIntegerDigits(MAX_AUTO_TAXNUM_DIGITS);
            this.documentSearchService = KEWServiceLocator.getDocumentSearchService();
            this.identityService = KimApiServiceLocator.getIdentityService();
            this.encryptionService = CoreApiServiceLocator.getEncryptionService();
            this.accountService = SpringContext.getBean(AccountService.class);
            this.organizationService = SpringContext.getBean(OrganizationService.class);
        }
    }



    /**
     * Helper method for explicitly inserting nulls into the appropriate placeholders
     * on the transaction detail insertion statement. The default implementation inserts
     * nulls for the following field placeholders:
     * 
     * <ul>
     *   <li>vendorName</li>
     *   <li>parentVendorName</li>
     *   <li>vendorEmailAddress</li>
     *   <li>vendorChapter4StatusCode</li>
     *   <li>vendorGIIN</li>
     *   <li>vendorLine1Address</li>
     *   <li>vendorLine2Address</li>
     *   <li>vendorCityName</li>
     *   <li>vendorStateCode</li>
     *   <li>vendorZipCode</li>
     *   <li>vendorForeignLine1Address</li>
     *   <li>vendorForeignLine2Address</li>
     *   <li>vendorForeignCityName</li>
     *   <li>vendorForeignZipCode</li>
     *   <li>vendorForeignProvinceName</li>
     *   <li>vendorForeignCountryCode</li>
     *   <li>initiatorNetId</li>
     * </ul>
     * 
     * @param insertStatement The prepared statement that will insert the transaction detail rows.
     * @param detailRow The helper object containing metadata about the transaction detail table.
     * @param offset The amount to subtract from the metadata indexes to get the actual insertion statement indexes.
     * @throws SQLException
     */
    void insertNullsForTransactionRow(PreparedStatement insertStatement, TransactionDetailRow detailRow, int offset) throws SQLException {
        insertStatement.setString(detailRow.vendorName.index - offset, null);
        insertStatement.setString(detailRow.parentVendorName.index - offset, null);
        insertStatement.setString(detailRow.vendorEmailAddress.index - offset, null);
        insertStatement.setString(detailRow.vendorChapter4StatusCode.index - offset, null);
        insertStatement.setString(detailRow.vendorGIIN.index - offset, null);
        insertStatement.setString(detailRow.vendorLine1Address.index - offset, null);
        insertStatement.setString(detailRow.vendorLine2Address.index - offset, null);
        insertStatement.setString(detailRow.vendorCityName.index - offset, null);
        insertStatement.setString(detailRow.vendorStateCode.index - offset, null);
        insertStatement.setString(detailRow.vendorZipCode.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignLine1Address.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignLine2Address.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignCityName.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignZipCode.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignProvinceName.index - offset, null);
        insertStatement.setString(detailRow.vendorForeignCountryCode.index - offset, null);
        insertStatement.setString(detailRow.initiatorNetId.index - offset, null);
    }



    /**
     * Convenience method for preparing to retrieve and reprocess the created transaction rows.
     * This allows for bulk retrieval of data, thus dividing the transaction row
     * creation into a two-pass process.
     * 
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @param uniqueDocumentIds The document IDs from the created transaction rows; cannot be null or contain null/blank values.
     */
    void prepareForSecondPass(T summary, Set<String> uniqueDocumentIds) {
        DocumentRouteHeaderValue dummyDocument = new DocumentRouteHeaderValue();
        dummyDocument.setDocumentId(CUTaxConstants.DOC_ID_ZERO);
        // Create and sort the document IDs list.
        documentIds = new ArrayList<String>(uniqueDocumentIds);
        Collections.sort(documentIds);
        // Prepare the iterators and other variables.
        documentIdsForBulkQuery = documentIds.iterator();
        currentDocument = dummyDocument;
        documentsForBatch = null;
        documentsForProcessing = Collections.emptyIterator();
    }



    /**
     * Helper method for retrieving the workflow document of the current tax source row,
     * and for updating statistics accordingly if the document could not be found.
     * 
     * <p>NOTE: In order for this method to work as intended, the list of doc IDs for
     * retrieval *MUST* be sorted in ascending order, and the invocations of this method
     * for a given builder *MUST* pass in the individual document IDs in ascending order
     * (preferably with null/blank values coming first).</p>
     * 
     * @param documentId The document's ID; may be blank.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return The workflow document for the current tax row, or null if no such document exists.
     */
    @SuppressWarnings("unchecked")
    DocumentRouteHeaderValue getWorkflowDocumentForTaxRow(String documentId, T summary) {
        if (StringUtils.isNotBlank(documentId)) {
            int idCompareResult = documentId.compareTo(currentDocument.getDocumentId());
            if (idCompareResult > 0) {
                // If no match and doc ID is greater than cached one, then get next document from bulk-retrieved ones.
                do {
                    while (documentsForProcessing.hasNext() && idCompareResult > 0) {
                        currentDocument = documentsForProcessing.next();
                        idCompareResult = documentId.compareTo(currentDocument.getDocumentId());
                    }
                    
                    // If still greater than cached ID and more unfetched docs exist, perform next bulk retrieval.
                    if (idCompareResult > 0 && documentIdsForBulkQuery.hasNext()) {
                        StringBuilder docIdCriteria = new StringBuilder(DOC_ID_CRITERIA_SIZE);
                        for (int i = 0; documentIdsForBulkQuery.hasNext() && i < getMaxSearchSize(); i++) {
                            // Build a docId criteria string with "|" (Kuali lookup OR) as the separator.
                            docIdCriteria.append(documentIdsForBulkQuery.next()).append('|');
                        }
                        // Remove last unneeded "|" separator.
                        docIdCriteria.deleteCharAt(docIdCriteria.length() - 1);
                        // Get and sort the documents.
                        DocumentSearchCriteria.Builder criteria = DocumentSearchCriteria.Builder.create();
                        criteria.setDocumentId(docIdCriteria.toString());
                        DocumentSearchResults results = documentSearchService.lookupDocuments(null, criteria.build(), false);
                        documentsForBatch = new ArrayList<DocumentRouteHeaderValue>(results.getSearchResults().size());
                        for (DocumentSearchResult result : results.getSearchResults()) {
                            documentsForBatch.add(result.getDocument());
                        }
                        Collections.sort(documentsForBatch, new BeanPropertyComparator(Collections.singletonList(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_ID)));
                        documentsForProcessing = documentsForBatch.iterator();
                        // Select and compare first document from new batch, if non-empty.
                        if (documentsForProcessing.hasNext()) {
                            currentDocument = documentsForProcessing.next();
                            idCompareResult = documentId.compareTo(currentDocument.getDocumentId());
                        }
                    }
                    
                    // Keep looping until doc ID is less than or equal to cached one, or until cached values are exhausted.
                } while (idCompareResult > 0 && (documentsForProcessing.hasNext() || documentIdsForBulkQuery.hasNext()));
            }
            if (idCompareResult == 0) {
                // Return document if found.
                return currentDocument;
            }
            // No document was found, so update statistics and return null.
            numNoDocumentHeaders++;
        } else {
            numNullDocumentHeaders++;
        }
        return null;
    }

    /**
     * Helper method for checking if entity, account, and org objects exist
     * for the current tax source row. Certain statistics will be updated
     * accordingly if one or more of these objects cannot not be found.
     * The initiator's principal name will also be returned, if found.
     * 
     * @param initiatorPrincipalId The document initiator's principal ID; may be blank.
     * @param chartCode The chart code; may be blank.
     * @param accountNumber The account number; may be blank.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return The initiator's principal name, or null if a principal name could not be found.
     */
    String checkForEntityAndAccountAndOrgExistence(String initiatorPrincipalId, String chartCode, String accountNumber, T summary) {
        Account account;
        EntityName entityName;
        Principal principal;
        
        // Check for null entity name info.
        entityName = (StringUtils.isNotBlank(initiatorPrincipalId)) ? identityService.getDefaultNamesForPrincipalId(initiatorPrincipalId) : null;
        if (entityName == null) {
            numNoEntityName++;
        }
        
        // Check for null account or null org.
        account = (StringUtils.isNotBlank(chartCode) && StringUtils.isNotBlank(accountNumber))
                ? accountService.getByPrimaryIdWithCaching(chartCode, accountNumber) : null;
        if (account != null) {
            if (organizationService.getByPrimaryIdWithCaching(chartCode, account.getOrganizationCode()) == null) {
                numNoOrg++;
            }
        } else {
            numNoAccount++;
        }
        
        // Return the initiator's principal name, if found.
        principal = (StringUtils.isNotBlank(initiatorPrincipalId)) ? identityService.getPrincipal(initiatorPrincipalId) : null;
        return (principal != null && entityName != null) ? principal.getPrincipalName() : null;
    }

    /**
     * Helper method for auto-generating a tax ID, in the event that the current
     * tax source row has none. The same auto-generated key will be used for all
     * source rows that have the same payee ID.
     * 
     * @param payeeId The ID of the payee lacking a tax ID.
     * @param summary The object encapsulating the tax-type-specific summary info.
     * @return A new auto-generated tax ID, or an existing auto-generated tax ID if one exists for the given payee.
     */
    String getReplacementVendorTaxNumber(String payeeId, T summary) {
        numNullTaxNumbers++;
        String newTaxNumber = nullTaxNumberReplacementsByPayeeId.get(payeeId);
        if (StringUtils.isBlank(newTaxNumber)) {
            numNullTaxNumbersForDistinctPayees++;
            try {
                newTaxNumber = encryptionService.encrypt(
                        autoGenTaxNumFormat.format(nullTaxNumberReplacementsByPayeeId.size() + 1));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
            nullTaxNumberReplacementsByPayeeId.put(payeeId, newTaxNumber);
        }
        
        return newTaxNumber;
    }

    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> stats = new EnumMap<TaxStatType,Integer>(TaxStatType.class);
        stats.put(TaxStatType.NUM_NULL_TAX_NUMBERS, Integer.valueOf(numNullTaxNumbers));
        stats.put(TaxStatType.NUM_NULL_TAX_NUMBERS_FOR_DISTINCT_PAYEES, Integer.valueOf(numNullTaxNumbersForDistinctPayees));
        stats.put(TaxStatType.NUM_NULL_DOCUMENT_HEADERS, Integer.valueOf(numNullDocumentHeaders));
        stats.put(TaxStatType.NUM_NO_DOCUMENT_HEADERS, Integer.valueOf(numNoDocumentHeaders));
        stats.put(TaxStatType.NUM_NO_ENTITY_NAME, Integer.valueOf(numNoEntityName));
        stats.put(TaxStatType.NUM_NO_ACCOUNT, Integer.valueOf(numNoAccount));
        stats.put(TaxStatType.NUM_NO_ORG, Integer.valueOf(numNoOrg));
        
        return stats;
    }

    protected int getMaxSearchSize() {
    	if (maxSearchResultSize == null) {
	    	String searchLimit = getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.WORKFLOW,
	        		KRADConstants.DetailTypes.DOCUMENT_SEARCH_DETAIL_TYPE, KewApiConstants.DOC_SEARCH_RESULT_CAP);
	    	try {
	    		LOG.debug("Found a value for KewApiConstants.DOC_SEARCH_RESULT_CAP, and it is '" + searchLimit + "'");
	    		maxSearchResultSize = new Integer(searchLimit);
	    	} catch (Exception e) {
	    		LOG.error("Unable to convert '" + searchLimit +
	    				"' to an integer.  Returning value of KewApiConstants.DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP which is " +
	    				KewApiConstants.DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP);
	    		maxSearchResultSize = new Integer(KewApiConstants.DOCUMENT_LOOKUP_DEFAULT_RESULT_CAP);
	    	}
    	}
    	return maxSearchResultSize.intValue();
    }

    protected ParameterService getParameterService() {
    	return SpringContext.getBean(ParameterService.class);
    }

}
