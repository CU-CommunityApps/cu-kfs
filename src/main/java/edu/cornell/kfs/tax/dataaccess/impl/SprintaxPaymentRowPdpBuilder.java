package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.businessobject.SecondPassAttributeUpdateValues;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxSqlUtils.SqlText;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.PdpSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.RawTransactionDetailRow;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.kew.api.KEWPropertyConstants;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResult;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResults;
import org.kuali.kfs.kew.docsearch.service.DocumentSearchService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import java.math.BigDecimal;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class SprintaxPaymentRowPdpBuilder {
	private static final Logger LOG = LogManager.getLogger(SprintaxPaymentRowPdpBuilder.class);

    private static final int GETTER_BUILDER_SIZE = 2000;
    private DecimalFormat autoGenTaxNumFormat;

    private int numExcludedAssignedIncomeCodes;
    private int numExcludedAssignedIncomeCodeSubTypes;
    private int numDeterminedFederalTaxWithheldIncomeCodes;
    private int numUndeterminedFederalTaxWithheldIncomeCodes;
    private int numDeterminedStateIncomeTaxWithheldIncomeCodes;
    private int numUndeterminedStateIncomeTaxWithheldIncomeCodes;
    private int numNullTaxNumbersForDistinctPayees;
    private int numNullTaxNumbers;
    private int numNullDocumentHeaders;
    private int numNoDocumentHeaders;
    private int numNoEntityName;
    private int numNoAccount;
    private int numNoOrg;
    private Map<String,String> nullTaxNumberReplacementsByPayeeId;

    public SprintaxPaymentSummary summary;
    private DocumentSearchService documentSearchService;
    private PersonService personService;
    private EncryptionService encryptionService;
    private AccountService accountService;
    private OrganizationService organizationService;

    private Integer maxSearchResultSize;

    // Helper variables for batch-related processing.
    private List<String> documentIds;
    private Iterator<String> documentIdsForBulkQuery;
    private List<DocumentRouteHeaderValue> documentsForBatch;
    private Iterator<DocumentRouteHeaderValue> documentsForProcessing;
    private DocumentRouteHeaderValue currentDocument;

    SprintaxPaymentRowPdpBuilder(SprintaxPaymentSummary sprintaxPaymentSummary) {
        this.summary = sprintaxPaymentSummary;
    }

    String getTaxSourceName() {
        return CUTaxConstants.TAX_SOURCE_PDP;
    }

    String getSqlForSelect() {
        StringBuilder fullSql = new StringBuilder(GETTER_BUILDER_SIZE);
        PdpSourceRow pdpRow = summary.pdpRow;
        
        // Build the query.
        TaxSqlUtils.appendQuery(fullSql,
                SqlText.SELECT, pdpRow.orderedFields, SqlText.FROM, pdpRow.tables,
                SqlText.WHERE,
                
                // Limit results to those whose disbursement dates are within the given time period.
                        pdpRow.disbursementDate, SqlText.BETWEEN, SqlText.PARAMETER, SqlText.AND, SqlText.PARAMETER,
                
                // Connect by customer IDs, and do not include results whose customers are in the IT-KUAL-DV chart-unit-subUnit combo.
                SqlText.AND,
                        pdpRow.summaryCustomerId, SqlText.EQUALS, pdpRow.customerId,
                SqlText.AND, SqlText.NOT, SqlText.PAREN_OPEN,
                                pdpRow.customerCampusCode, SqlText.EQUALS, "'IT'",
                        SqlText.AND,
                                pdpRow.unitCode, SqlText.EQUALS, "'KUAL'",
                        SqlText.AND,
                                pdpRow.subUnitCode, SqlText.EQUALS, "'DV'",
                SqlText.PAREN_CLOSE,
                
                // Connect by process IDs, and make sure the payment groups' disbursement numbers are within the ranges of the matching summaries.
                SqlText.AND,
                        pdpRow.summaryProcessId, SqlText.EQUALS, pdpRow.paymentGroupProcessId,
                SqlText.AND,
                        pdpRow.disbursementNbr, SqlText.BETWEEN, pdpRow.beginDisbursementNbr, SqlText.AND, pdpRow.endDisbursementNbr,
                
                // Add other key-matching, vendor-related, and tax-type-specific criteria.
                SqlText.AND,
                        pdpRow.payeeIdTypeCode, SqlText.EQUALS, "'V'",
                SqlText.AND,
                        pdpRow.paymentGroupId, SqlText.EQUALS, pdpRow.paymentDetailPaymentGroupId,
                SqlText.AND,
                        pdpRow.accountDetailPaymentDetailId, SqlText.EQUALS, pdpRow.paymentDetailId,
                SqlText.AND,
                        TaxSqlUtils.getPayeeIdToVendorHeaderIdCriteria(pdpRow.payeeId, pdpRow.vendorHeaderGeneratedId),
                "",
                SqlText.AND,
                        pdpRow.vendorForeignInd, SqlText.EQUALS, SqlText.PARAMETER,
                        
                SqlText.AND,
                		pdpRow.preqDocumentNumber, SqlText.CONDITIONAL_EQUALS_JOIN, pdpRow.custPaymentDocNbr,
                		
                SqlText.AND,
                		pdpRow.dvDocumentNumber, SqlText.CONDITIONAL_EQUALS_JOIN, pdpRow.custPaymentDocNbr,
                
                SqlText.AND,
                		pdpRow.nraDocumentNumber, SqlText.CONDITIONAL_EQUALS_JOIN, pdpRow.dvDocumentNumber,
                
                // Build the ORDER BY clause.
                SqlText.ORDER_BY,
                        new Object[][] {{pdpRow.vendorTaxNumber}, {pdpRow.summaryLastUpdatedTimestamp}, {pdpRow.summaryId}});
        
        
        
        // Log and return the full query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final PDP selection query: " + fullSql.toString());
        }
        return fullSql.toString();
    }

    Object[][] getParameterValuesForSelectingCreatedRows() {
        return new Object[][] {
            {summary.reportYear},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }

    void buildRawTransactionRows(ResultSet rs, PreparedStatement insertStatement) throws SQLException {
        PdpSourceRow pdpRow = summary.pdpRow;
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        int offset = rawDetailRow.insertOffset;
        String documentId;
        String financialObjectCode;
        BigDecimal netPaymentAmount;
        Set<String> docIds = new HashSet<String>();
        int currentBatchSize = 0;
        
        while (rs.next()) {
            // Perform initialization.
            documentId = rs.getString(pdpRow.custPaymentDocNbr.index);
            financialObjectCode = rs.getString(pdpRow.finObjectCode.index);
            netPaymentAmount = rs.getBigDecimal(pdpRow.accountNetAmount.index);
            
            // Add docId to map if non-blank.
            if (StringUtils.isNotBlank(documentId)) {
                docIds.add(documentId);
            }
            
            // If net payment amount is null, then set to zero.
            if (netPaymentAmount == null) {
                netPaymentAmount = summary.zeroAmount;
            }
            
            // Perform extra 1042S-specific setup as needed.
            doTaxSpecificFirstPassRowSetup(insertStatement);
            
            /*
             * Prepare to insert another transaction detail row.
             * 
             * NOTE: It is expected that subclasses use the "doTaxSpecificFirstPassRowSetup"
             * method to populate the following prepared statement arguments:
             * 
             * INCOME_CODE
             * INCOME_CODE_SUB_TYPE
             * FORM_1099_BOX
             * FORM_1099_OVERRIDDEN_BOX
             * FORM_1042S_BOX
             * FORM_1042S_OVERRIDDEN_BOX
             * 
             * In addition, it is expected that updateTransactionRowsFromWorkflowDocuments()
             * will update the following fields after they've been set by this method:
             * 
             * FDOC_NBR (if null, in which case it should be set to "0" or some other constant)
             * DOC_TITLE
             * INITIATOR_NETID
             * VENDOR_TAX_NBR (if null, in which case it should be set to an auto-generated value)
             */
            
            String documentType = rs.getString(pdpRow.financialDocumentTypeCode.index);
            
            insertStatement.setInt(rawDetailRow.reportYear.index - offset, summary.reportYear);
            insertStatement.setString(rawDetailRow.documentNumber.index - offset, (StringUtils.isNotBlank(documentId)) ? documentId : null);
            insertStatement.setString(rawDetailRow.documentType.index - offset, documentType);
            insertStatement.setInt(rawDetailRow.financialDocumentLineNumber.index - offset, rs.getInt(pdpRow.accountDetailId.index));
            insertStatement.setString(rawDetailRow.finObjectCode.index - offset, financialObjectCode);
            insertStatement.setBigDecimal(rawDetailRow.netPaymentAmount.index - offset, netPaymentAmount);
            insertStatement.setString(rawDetailRow.vendorTaxNumber.index - offset, rs.getString(pdpRow.vendorTaxNumber.index));
            insertStatement.setString(rawDetailRow.payeeId.index - offset, rs.getString(pdpRow.payeeId.index));
            insertStatement.setString(rawDetailRow.vendorTypeCode.index - offset, rs.getString(pdpRow.vendorTypeCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCode.index - offset, rs.getString(pdpRow.vendorOwnershipCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCategoryCode.index - offset, rs.getString(pdpRow.vendorOwnershipCategoryCode.index));
            insertStatement.setString(rawDetailRow.vendorForeignIndicator.index - offset, rs.getString(pdpRow.vendorForeignInd.index));
            insertStatement.setString(rawDetailRow.nraPaymentIndicator.index - offset, rs.getString(pdpRow.nraPayment.index));
            insertStatement.setDate(rawDetailRow.paymentDate.index - offset, rs.getDate(pdpRow.disbursementDate.index));
            insertStatement.setString(rawDetailRow.paymentPayeeName.index - offset, rs.getString(pdpRow.payeeName.index));
            insertStatement.setString(rawDetailRow.paymentDescription.index - offset, rs.getString(pdpRow.achPaymentDescription.index));
            insertStatement.setString(rawDetailRow.paymentLine1Address.index - offset, rs.getString(pdpRow.line1Address.index));
            insertStatement.setString(rawDetailRow.paymentCountryName.index - offset, rs.getString(pdpRow.country.index));
            insertStatement.setString(rawDetailRow.chartCode.index - offset, rs.getString(pdpRow.accountDetailFinChartCode.index)); // ?
            insertStatement.setString(rawDetailRow.accountNumber.index - offset, rs.getString(pdpRow.accountNbr.index));
            insertStatement.setString(rawDetailRow.incomeClassCode.index - offset, findincomeClassCode(rs, pdpRow, documentType));
            insertStatement.setString(rawDetailRow.disbursementNbr.index - offset, rs.getString(pdpRow.disbursementNbr.index));
            insertStatement.setString(rawDetailRow.paymentStatusCode.index - offset, rs.getString(pdpRow.paymentStatusCode.index));
            insertStatement.setString(rawDetailRow.disbursementTypeCode.index - offset, rs.getString(pdpRow.disbursementTypeCode.index));
            insertStatement.setString(rawDetailRow.ledgerDocumentTypeCode.index - offset, rs.getString(pdpRow.financialDocumentTypeCode.index));

            insertNullsForTransactionRow(insertStatement, rawDetailRow, offset);

            // Add to batch, and execute batch if needed.
            insertStatement.addBatch();
            currentBatchSize++;
            if (currentBatchSize == CUTaxConstants.INSERT_BATCH_SIZE) {
                insertStatement.executeBatch();
                currentBatchSize = 0;
            }
        }
        
        // Execute any remaining insertions that were not included in a prior batch.
        if (currentBatchSize > 0) {
            insertStatement.executeBatch();
        }
        
        // Prepare collected docIds for next processing iteration.
        prepareForSecondPass(docIds);
    }


    /**
     * Convenience method for preparing to retrieve and reprocess the created transaction rows.
     * This allows for bulk retrieval of data, thus dividing the transaction row
     * creation into a two-pass process.
     *
     * @param uniqueDocumentIds The document IDs from the created transaction rows; cannot be null or contain null/blank values.
     */
    void prepareForSecondPass(Set<String> uniqueDocumentIds) {
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

	protected String findincomeClassCode(ResultSet rs, PdpSourceRow pdpRow, String documentType) throws SQLException {
		String incomeClassCode = null;
		if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.PAYMENT_REQUEST)) {
			incomeClassCode = rs.getString(pdpRow.taxClassificationCode.index);
		} else if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.DISBURSEMENT_VOUCHER)) {
			incomeClassCode = rs.getString(pdpRow.dvIncomeClassCode.index);
		}
		return incomeClassCode;
	}

    void insertNullsForTransactionRow(PreparedStatement insertStatement, RawTransactionDetailRow rawDetailRow, int offset) throws SQLException {
        insertStatement.setString(rawDetailRow.vendorName.index - offset, null);
        insertStatement.setString(rawDetailRow.parentVendorName.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorEmailAddress.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorChapter4StatusCode.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorGIIN.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorLine1Address.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorLine2Address.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorCityName.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorStateCode.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorZipCode.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignLine1Address.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignLine2Address.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignCityName.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignZipCode.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignProvinceName.index - offset, null);
        insertStatement.setString(rawDetailRow.vendorForeignCountryCode.index - offset, null);
        insertStatement.setString(rawDetailRow.initiatorNetId.index - offset, null);
        insertStatement.setString(rawDetailRow.documentTitle.index - offset, null);
        insertStatement.setString(rawDetailRow.dvCheckStubText.index - offset, null);
        insertStatement.setString(rawDetailRow.incomeTaxTreatyExemptIndicator.index - offset, null);
        insertStatement.setString(rawDetailRow.foreignSourceIncomeIndicator.index - offset, null);
        insertStatement.setBigDecimal(rawDetailRow.federalIncomeTaxPercent.index - offset, null);
        insertStatement.setString(rawDetailRow.paymentReasonCode.index - offset, null);
    }

    /**
     * Helper method for copying certain data over to this builder from the previous one, if any.
     * If there was no previous builder, then perform first-in-the-chain initialization instead.
     * Also perform any other initialization as needed.
     *
     * @param builder The previously-executed builder in the chain; may be null.
     * @param processingDao the DAO pertaining to the tax processing, in case any other data needs retrieving for setup.
     */
    void copyValuesFromPreviousBuilder(SprintaxPaymentRowPdpBuilder builder, TaxProcessingDao processingDao) {
        if (builder != null) {
            this.nullTaxNumberReplacementsByPayeeId = builder.nullTaxNumberReplacementsByPayeeId;
            this.autoGenTaxNumFormat = builder.autoGenTaxNumFormat;
            this.documentSearchService = builder.documentSearchService;
            this.personService = builder.personService;
            this.encryptionService = builder.encryptionService;
            this.accountService = builder.accountService;
            this.organizationService = builder.organizationService;
        } else {
            this.nullTaxNumberReplacementsByPayeeId = new HashMap<String,String>();
            this.autoGenTaxNumFormat = new DecimalFormat("~00000000", new DecimalFormatSymbols(Locale.US));
            this.autoGenTaxNumFormat.setMaximumIntegerDigits(8);
            this.documentSearchService = KEWServiceLocator.getDocumentSearchService();
            this.personService = KimApiServiceLocator.getPersonService();
            this.encryptionService = CoreApiServiceLocator.getEncryptionService();
            this.accountService = SpringContext.getBean(AccountService.class);
            this.organizationService = SpringContext.getBean(OrganizationService.class);
        }
    }

    void updateTransactionRowsFromWorkflowDocuments(ResultSet rs, PreparedStatement secondPassTransactionInsertStatement, SprintaxPaymentSummary summary) throws SQLException {
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        String rawTransactionDetailId;
        String documentId;
        String initiatorPrincipalId;
        String initiatorPrincipalName;
        String vendorTaxNumber;
        DocumentRouteHeaderValue document;
        
        
        
        // Perform row updates as needed.
        while (rs.next()) {
            // Only update PDP-related rows.
            if (!DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(rs.getString(rawDetailRow.documentType.index))) {
                // Initialize variables.
                SecondPassAttributeUpdateValues updatedAttributeValues = new SecondPassAttributeUpdateValues();
                rawTransactionDetailId = rs.getString(rawDetailRow.transactionDetailId.index);
                documentId = rs.getString(rawDetailRow.documentNumber.index);
                initiatorPrincipalId = null;
                vendorTaxNumber = rs.getString(rawDetailRow.vendorTaxNumber.index);
                
                // Retrieve document info.
                document = getWorkflowDocumentForTaxRow(documentId);
                if (document != null) {
                    initiatorPrincipalId = document.getInitiatorPrincipalId();
                }
                
                // Check for null objects as needed, and get the initiator's principal name.
                initiatorPrincipalName = checkForEntityAndAccountAndOrgExistence(
                        initiatorPrincipalId,
                        rs.getString(rawDetailRow.chartCode.index),
                        rs.getString(rawDetailRow.accountNumber.index)
                );
                
                // If vendor tax number is blank, then replace with a generated value accordingly.
                if (StringUtils.isBlank(vendorTaxNumber)) {
                    vendorTaxNumber = getReplacementVendorTaxNumber(rs.getString(rawDetailRow.payeeId.index), summary);
                    updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.vendorTaxNumber.index, vendorTaxNumber);
                }
                
                // Do tax-type-specific updates.
                doTaxSpecificSecondPassRowSetup(rs, summary, updatedAttributeValues);
                
                // Update other fields as needed.
                if (StringUtils.isBlank(documentId)) {
                    updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.documentNumber.index, CUTaxConstants.DOC_ID_ZERO);
                }
                updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.documentTitle.index, 
                        (document != null && StringUtils.isNotBlank(document.getTitle())) ? document.getTitle() : CUTaxConstants.DOC_TITLE_IF_NOT_FOUND);
                updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.initiatorNetId.index,
                        StringUtils.isNotBlank(initiatorPrincipalName) ? initiatorPrincipalName : CUTaxConstants.NETID_IF_NOT_FOUND);
                
                // Update the current row.
                LOG.debug("TransactionRowPdpBuilder:: updateTransactionRowsFromWorkflowDocuments: Inserting updated second pass data for "
                        + "rawTransactionDetailId = {}, documentId = {}", rawTransactionDetailId, documentId);
                insertUpdatedTransactionDetail(rs, secondPassTransactionInsertStatement, updatedAttributeValues);
            }
        }
    }

    String getTaxTypeSpecificConditionForSelect(SprintaxPaymentSummary summary) {
        // No extra conditions needed to filter out PDP rows for 1042S processing.
        return KFSConstants.EMPTY_STRING;
    }

    TaxTableField getExtraField(RawTransactionDetailRow rawDetailRow) {
        return rawDetailRow.form1042SBox;
    }

    void doTaxSpecificFirstPassRowSetup(PreparedStatement insertStatement) throws SQLException {
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;

        // Setup prepared statement args accordingly. The 1042S box is set to "?" for bulk retrieval convenience.
        insertStatement.setString(rawDetailRow.incomeCode.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.incomeCodeSubType.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.form1099Type.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.form1099Box.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.form1099OverriddenType.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.form1099OverriddenBox.index - rawDetailRow.insertOffset, null);
        insertStatement.setString(rawDetailRow.form1042SBox.index - rawDetailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
        insertStatement.setString(rawDetailRow.form1042SOverriddenBox.index - rawDetailRow.insertOffset, null);
    }

    void doTaxSpecificSecondPassRowSetup(ResultSet rs, SprintaxPaymentSummary summary, SecondPassAttributeUpdateValues updatedAttributeValues) throws SQLException {
        String financialObjectCode = rs.getString(summary.rawTransactionDetailRow.finObjectCode.index);
        String incomeClassCode = rs.getString(summary.rawTransactionDetailRow.incomeClassCode.index);
        String incomeCode;
        String incomeCodeSubType;

        // Prepare the income and subtype codes.
        incomeCode = summary.incomeClassCodeToIrsIncomeCodeMap.get(incomeClassCode);
        if (StringUtils.isBlank(incomeCode)) {
            // If no income class code was found, then check whether the object code is a fed-tax-withheld one.
            if (summary.federalTaxWithheldObjectCodes.contains(financialObjectCode)) {
                // If the object code represents a fed-tax-withheld one, then use the non-reportable income code.
                incomeCode = summary.nonReportableIncomeCode;
                numDeterminedFederalTaxWithheldIncomeCodes++;
            } else {
                numUndeterminedFederalTaxWithheldIncomeCodes++;
                // If not a fed-tax-withheld object code, check whether it's a state-tax-withheld one.
                if (summary.stateTaxWithheldObjectCodes.contains(financialObjectCode)) {
                    // If the object code represents a state-tax-withheld one, then use the non-reportable income code.
                    incomeCode = summary.nonReportableIncomeCode;
                    numDeterminedStateIncomeTaxWithheldIncomeCodes++;
                } else {
                    numUndeterminedStateIncomeTaxWithheldIncomeCodes++;
                    // If not a state-tax-withheld one either, then use the "excluded" income code.
                    incomeCode = summary.excludedIncomeCode;
                    numExcludedAssignedIncomeCodes++;
                }
            }
        }

        incomeCodeSubType = summary.incomeClassCodeToIrsIncomeCodeSubTypeMap.get(incomeClassCode);
        if (StringUtils.isBlank(incomeCodeSubType)) {
            // If no subtype was found, then set it to the exclusion value.
            incomeCodeSubType = summary.excludedIncomeCodeSubType;
            numExcludedAssignedIncomeCodeSubTypes++;
        }

        // Prepare to update income code and income code subtype fields.
        updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCode.index, incomeCode);
        updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCodeSubType.index, incomeCodeSubType);

    }

    void insertUpdatedTransactionDetail(ResultSet rs, PreparedStatement secondPassInsertStatement, SecondPassAttributeUpdateValues dataForUpdates) throws SQLException {
        initializeTransactionDetailFromRawTransactionDetail(rs, secondPassInsertStatement);
        updateTransactionDetailWithSecondPassData(dataForUpdates, secondPassInsertStatement);
        secondPassInsertStatement.execute();
        secondPassInsertStatement.clearParameters();
    }

    void initializeTransactionDetailFromRawTransactionDetail(ResultSet rs, PreparedStatement secondPassInsertStatement) throws SQLException {
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        int detailRowInsertOffset = detailRow.insertOffset;

        //transactionDetailId is generated by table sequence, not copied over
        secondPassInsertStatement.setInt(detailRow.reportYear.index - detailRowInsertOffset, rs.getInt(rawDetailRow.reportYear.index));
        secondPassInsertStatement.setString(detailRow.documentNumber.index - detailRowInsertOffset, rs.getString(rawDetailRow.documentNumber.index));
        secondPassInsertStatement.setString(detailRow.documentType.index - detailRowInsertOffset, rs.getString(rawDetailRow.documentType.index));
        secondPassInsertStatement.setInt(detailRow.financialDocumentLineNumber.index - detailRowInsertOffset, rs.getInt(rawDetailRow.financialDocumentLineNumber.index));
        secondPassInsertStatement.setString(detailRow.finObjectCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.finObjectCode.index));
        secondPassInsertStatement.setBigDecimal(detailRow.netPaymentAmount.index - detailRowInsertOffset, rs.getBigDecimal(rawDetailRow.netPaymentAmount.index));
        secondPassInsertStatement.setString(detailRow.documentTitle.index - detailRowInsertOffset, rs.getString(rawDetailRow.documentTitle.index));
        secondPassInsertStatement.setString(detailRow.vendorTaxNumber.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorTaxNumber.index));
        secondPassInsertStatement.setString(detailRow.incomeCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.incomeCode.index));
        secondPassInsertStatement.setString(detailRow.incomeCodeSubType.index - detailRowInsertOffset, rs.getString(rawDetailRow.incomeCodeSubType.index));
        secondPassInsertStatement.setString(detailRow.dvCheckStubText.index - detailRowInsertOffset, rs.getString(rawDetailRow.dvCheckStubText.index));
        secondPassInsertStatement.setString(detailRow.payeeId.index - detailRowInsertOffset, rs.getString(rawDetailRow.payeeId.index));
        secondPassInsertStatement.setString(detailRow.vendorName.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorName.index));
        secondPassInsertStatement.setString(detailRow.parentVendorName.index - detailRowInsertOffset, rs.getString(rawDetailRow.parentVendorName.index));
        secondPassInsertStatement.setString(detailRow.vendorTypeCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorTypeCode.index));
        secondPassInsertStatement.setString(detailRow.vendorOwnershipCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorOwnershipCode.index));
        secondPassInsertStatement.setString(detailRow.vendorOwnershipCategoryCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorOwnershipCategoryCode.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignIndicator.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignIndicator.index));
        secondPassInsertStatement.setString(detailRow.vendorEmailAddress.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorEmailAddress.index));
        secondPassInsertStatement.setString(detailRow.vendorChapter4StatusCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorChapter4StatusCode.index));
        secondPassInsertStatement.setString(detailRow.vendorGIIN.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorGIIN.index));
        secondPassInsertStatement.setString(detailRow.vendorLine1Address.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorLine1Address.index));
        secondPassInsertStatement.setString(detailRow.vendorLine2Address.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorLine2Address.index));
        secondPassInsertStatement.setString(detailRow.vendorCityName.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorCityName.index));
        secondPassInsertStatement.setString(detailRow.vendorStateCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorStateCode.index));
        secondPassInsertStatement.setString(detailRow.vendorZipCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorZipCode.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignLine1Address.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignLine1Address.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignLine2Address.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignLine2Address.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignCityName.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignCityName.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignZipCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignZipCode.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignProvinceName.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignProvinceName.index));
        secondPassInsertStatement.setString(detailRow.vendorForeignCountryCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.vendorForeignCountryCode.index));
        secondPassInsertStatement.setString(detailRow.nraPaymentIndicator.index - detailRowInsertOffset, rs.getString(rawDetailRow.nraPaymentIndicator.index));
        secondPassInsertStatement.setDate(detailRow.paymentDate.index - detailRowInsertOffset, rs.getDate(rawDetailRow.paymentDate.index));
        secondPassInsertStatement.setString(detailRow.paymentPayeeName.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentPayeeName.index));
        secondPassInsertStatement.setString(detailRow.incomeClassCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.incomeClassCode.index));
        secondPassInsertStatement.setString(detailRow.incomeTaxTreatyExemptIndicator.index - detailRowInsertOffset, rs.getString(rawDetailRow.incomeTaxTreatyExemptIndicator.index));
        secondPassInsertStatement.setString(detailRow.foreignSourceIncomeIndicator.index - detailRowInsertOffset, rs.getString(rawDetailRow.foreignSourceIncomeIndicator.index));
        secondPassInsertStatement.setBigDecimal(detailRow.federalIncomeTaxPercent.index - detailRowInsertOffset, rs.getBigDecimal(rawDetailRow.federalIncomeTaxPercent.index));
        secondPassInsertStatement.setString(detailRow.paymentDescription.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentDescription.index));
        secondPassInsertStatement.setString(detailRow.paymentLine1Address.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentLine1Address.index));
        secondPassInsertStatement.setString(detailRow.paymentCountryName.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentCountryName.index));
        secondPassInsertStatement.setString(detailRow.chartCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.chartCode.index));
        secondPassInsertStatement.setString(detailRow.accountNumber.index - detailRowInsertOffset, rs.getString(rawDetailRow.accountNumber.index));
        secondPassInsertStatement.setString(detailRow.initiatorNetId.index - detailRowInsertOffset, rs.getString(rawDetailRow.initiatorNetId.index));
        secondPassInsertStatement.setString(detailRow.form1099Type.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1099Type.index));
        secondPassInsertStatement.setString(detailRow.form1099Box.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1099Box.index));
        secondPassInsertStatement.setString(detailRow.form1099OverriddenType.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1099OverriddenType.index));
        secondPassInsertStatement.setString(detailRow.form1099OverriddenBox.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1099OverriddenBox.index));
        secondPassInsertStatement.setString(detailRow.form1042SBox.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1042SBox.index));
        secondPassInsertStatement.setString(detailRow.form1042SOverriddenBox.index - detailRowInsertOffset, rs.getString(rawDetailRow.form1042SOverriddenBox.index));
        secondPassInsertStatement.setString(detailRow.paymentReasonCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentReasonCode.index));
        secondPassInsertStatement.setInt(detailRow.disbursementNbr.index - detailRowInsertOffset, rs.getInt(rawDetailRow.disbursementNbr.index));
        secondPassInsertStatement.setString(detailRow.paymentStatusCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.paymentStatusCode.index));
        secondPassInsertStatement.setString(detailRow.disbursementTypeCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.disbursementTypeCode.index));
        secondPassInsertStatement.setString(detailRow.ledgerDocumentTypeCode.index - detailRowInsertOffset, rs.getString(rawDetailRow.ledgerDocumentTypeCode.index));
    }

    void updateTransactionDetailWithSecondPassData(SecondPassAttributeUpdateValues dataForUpdates, PreparedStatement secondPassInsertStatement) throws RuntimeException {
        TaxTableRow.TransactionDetailRow detailRow = summary.transactionDetailRow;
        int detailRowInsertOffset = detailRow.insertOffset;

        //Update all String(s) in the transaction detail insert statement with data values determined by the second pass processing.
        dataForUpdates.getUpdateStringAttributeValues().forEach((index, value) -> {
            try {
                secondPassInsertStatement.setString(index.intValue() - detailRowInsertOffset, value);
            } catch (SQLException sqle) {
                LOG.error("updateTransactionDetailWithSecondPassData: Updating String attribute for index = {} generated SQLException.", index.intValue(), sqle);
                throw new RuntimeException(sqle);
            }
        });

        //Update all java.sql.Date(s) in the transaction detail insert statement with data values determined by the second pass processing.
        dataForUpdates.getUpdateDateAttributeValues().forEach((index, value) -> {
            try {
                secondPassInsertStatement.setDate(index.intValue() - detailRowInsertOffset, value);
            } catch (SQLException sqle) {
                LOG.error("updateTransactionDetailWithSecondPassData: Updating java.sql.Date attribute for index = {} generated SQLException.", index.intValue(), sqle);
                throw new RuntimeException(sqle);
            }
        });
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
     * @return The workflow document for the current tax row, or null if no such document exists.
     */
    @SuppressWarnings("unchecked")
    DocumentRouteHeaderValue getWorkflowDocumentForTaxRow(String documentId) {
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
                        StringBuilder docIdCriteria = new StringBuilder(5000);
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

    String checkForEntityAndAccountAndOrgExistence(String initiatorPrincipalId, String chartCode, String accountNumber) {
        Account account;
        boolean personHasName = true;
        Person person;

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

        // Return the initiator's principal name, if found and if the initiator has a non-blank name.
        person = (StringUtils.isNotBlank(initiatorPrincipalId)) ? personService.getPerson(initiatorPrincipalId) : null;
        if (person != null && StringUtils.isBlank(person.getName())) {
            personHasName = false;
            numNoEntityName++;
        }
        return (person != null && personHasName) ? person.getPrincipalName() : null;
    }

    String getReplacementVendorTaxNumber(String payeeId, SprintaxPaymentSummary summary) {
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
        EnumMap<TaxStatType,Integer> stats = new EnumMap(TaxStatType.class);
        stats.put(TaxStatType.NUM_NULL_TAX_NUMBERS, Integer.valueOf(numNullTaxNumbers));
        stats.put(TaxStatType.NUM_NULL_TAX_NUMBERS_FOR_DISTINCT_PAYEES, Integer.valueOf(numNullTaxNumbersForDistinctPayees));
        stats.put(TaxStatType.NUM_NULL_DOCUMENT_HEADERS, Integer.valueOf(numNullDocumentHeaders));
        stats.put(TaxStatType.NUM_NO_DOCUMENT_HEADERS, Integer.valueOf(numNoDocumentHeaders));
        stats.put(TaxStatType.NUM_NO_ENTITY_NAME, Integer.valueOf(numNoEntityName));
        stats.put(TaxStatType.NUM_NO_ACCOUNT, Integer.valueOf(numNoAccount));
        stats.put(TaxStatType.NUM_NO_ORG, Integer.valueOf(numNoOrg));

        stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES, Integer.valueOf(numExcludedAssignedIncomeCodes));
        stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_PDP, Integer.valueOf(numExcludedAssignedIncomeCodes));
        stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
        stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_PDP, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
        stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
        stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_PDP, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
        stats.put(TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedFederalTaxWithheldIncomeCodes));
        stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
        stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_PDP, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
        stats.put(TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedStateIncomeTaxWithheldIncomeCodes));

        return stats;
    }

    protected int getMaxSearchSize() {
        if (maxSearchResultSize == null) {
            ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            String searchLimit = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.WORKFLOW,
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

    Object[][] getParameterValuesForSelect() {
        return TaxSqlUtils.getArgsArrayWithVendorOwnershipParameters(Collections.<String,Set<String>>emptyMap(),
                getSelectParametersBeforeSplitPoint(), getSelectParametersAfterSplitPoint());
    }

    Object[][] getSelectParametersBeforeSplitPoint() {
        // Setup parameters. First one is start of DISB_TS range, and second is the end of that range.
        return new Object[][] {
                {summary.getStartDate()},
                {summary.getEndDate()}
        };
    }

    Object[][] getSelectParametersAfterSplitPoint() {
        // Get vendor foreign indicator param.
        return new Object[][] {
                { KRADConstants.YES_INDICATOR_VALUE }
        };
    }

    String getSqlForSelectingCreatedRows() {
        return TaxSqlUtils.getRawTransactionDetailSelectSql(getExtraField(summary.rawTransactionDetailRow), summary.rawTransactionDetailRow, true, false);
    }

}
