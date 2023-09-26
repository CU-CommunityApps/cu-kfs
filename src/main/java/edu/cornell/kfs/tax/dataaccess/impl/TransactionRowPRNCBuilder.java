package edu.cornell.kfs.tax.dataaccess.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.kew.routeheader.service.CuRouteHeaderService;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.businessobject.SecondPassAttributeUpdateValues;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxSqlUtils.SqlText;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.PRNCSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.RawTransactionDetailRow;

abstract class TransactionRowPRNCBuilder<T extends TransactionDetailSummary> extends TransactionRowBuilder<T> {
	private static final Logger LOG = LogManager.getLogger(TransactionRowPRNCBuilder.class);
	
	private static final int GETTER_BUILDER_SIZE = 2000;
	private static final int DEFAULT_PRE_SPLIT_PARM_COUNT = 1;
	
	protected List<String> finalizedPRNCDocuments;
	
    // Variables pertaining to various statistics that will be collected during transaction row creation.
    private int numForeignDraftsSelected;
    private int numWireTransfersSelected;
    private int numForeignDraftsIgnored;
    private int numWireTransfersIgnored;
	
	public TransactionRowPRNCBuilder() {
		super();
	}

	@Override
	String getTaxSourceName() {
		return CUTaxConstants.TAX_SOURCE_PRNC;
	}
	
    @Override
    void copyValuesFromPreviousBuilder(TransactionRowBuilder<?> builder, TaxProcessingDao processingDao, T summary) {
        super.copyValuesFromPreviousBuilder(builder, processingDao, summary);
        
        // Calculate end-of-day end date-time value.
        final int TWENTY_THREE = 23;
        final int FIFTY_NINE = 59;
        Calendar endDateTime = CoreApiServiceLocator.getDateTimeService().getCurrentCalendar();
        endDateTime.setTime(summary.getEndDate());
        endDateTime.set(Calendar.HOUR_OF_DAY, TWENTY_THREE);
        endDateTime.set(Calendar.MINUTE, FIFTY_NINE);
        endDateTime.set(Calendar.SECOND, FIFTY_NINE);
        
        // Find all PRNC documents that were finalized between the start and end dates.
        CuRouteHeaderService routeHeaderService = (CuRouteHeaderService) SpringContext.getBean(
                RouteHeaderService.class, KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
        Map<String,java.sql.Timestamp> datesMap = routeHeaderService.getFinalizedDatesForDocumentType(PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT,
                new java.sql.Timestamp(summary.getStartDate().getTime()), new java.sql.Timestamp(endDateTime.getTime().getTime()));
        // Filter results to only include Foreign Draft and Wire Transfer PRNCs.
        finalizedPRNCDocuments = processingDao.findForeignDraftsAndWireTransfers(new ArrayList<String>(datesMap.keySet()), summary, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
        if (finalizedPRNCDocuments.isEmpty()) {
            // If no matching PRNC documents were found, then just add a dummy value to prevent query generation problems.
        	finalizedPRNCDocuments.add(CUTaxConstants.DOC_ID_ZERO);
        }
    }

	@Override
	String getSqlForSelect(T summary) {
		  StringBuilder fullSql = new StringBuilder(GETTER_BUILDER_SIZE);
	        PRNCSourceRow prncRow = summary.prncRow;
	        
	        // Build the query.
	        TaxSqlUtils.appendQuery(fullSql,
	                SqlText.SELECT, prncRow.orderedFields, SqlText.FROM, prncRow.tables,
	                SqlText.WHERE,        
	                prncRow.paymentMethodCode, SqlText.IN, "('F','W')",
	                SqlText.AND,                          
                    TaxSqlUtils.getInListCriteria(prncRow.preqDocumentNumber, finalizedPRNCDocuments.size(), true, true),
                    SqlText.AND,
	                prncRow.universityDate, SqlText.EQUALS, SqlText.PARAMETER,
	                
	                // Add doc number criteria.
	                SqlText.AND,
	                prncRow.preqPurapDocumentIdentifier, SqlText.EQUALS, prncRow.purapDocumentIdentifier,
	                SqlText.AND,
	                prncRow.itemIdentifier, SqlText.EQUALS, prncRow.accountItemIdentifier,
	                
	                // Add various IS NOT NULL criteria.
	                SqlText.AND,
	                prncRow.preqPurapDocumentIdentifier, SqlText.IS_NOT_NULL,
	                SqlText.AND,
	                prncRow.itemIdentifier, SqlText.IS_NOT_NULL,
	                SqlText.AND,
	                prncRow.accountIdentifier, SqlText.IS_NOT_NULL,
	                
	                // And vendor criteria and tax-type-specific criteria.
	                SqlText.AND,
	                       prncRow.preqVendorHeaderGeneratedIdentifier,SqlText.EQUALS, prncRow.vendorHeaderGeneratedId,
	                getTaxTypeSpecificConditionForSelect(summary),
	                SqlText.AND,
	                prncRow.vendorForeignInd, SqlText.EQUALS, SqlText.PARAMETER,
	                
	                // Build the ORDER BY clause.
	                SqlText.ORDER_BY,
	                        new Object[][] {{prncRow.vendorTaxNumber}, {prncRow.preqDocumentNumber}, {prncRow.accountItemIdentifier}, {prncRow.accountIdentifier}}
	        );
	        	        
	        
	        // Log and return the full query.
	        if (LOG.isDebugEnabled()) {
	            LOG.debug("Final PRNC selection query: " + fullSql.toString());
	        }
	        return fullSql.toString();
	}

    @Override
    Object[][] getParameterValuesForSelect(T summary) {
        return TaxSqlUtils.getArgsArrayWithVendorOwnershipParameters(Collections.<String,Set<String>>emptyMap(),
                getSelectParametersBeforeSplitPoint(summary), getSelectParametersAfterSplitPoint(summary));
    }

    /*
     * Helper method defining the SELECT parameters that precede any vendor ownership or category ones.
     */
    Object[][] getSelectParametersBeforeSplitPoint(T summary) {
        // Get parameters. First is date range start and the rest are document IDs.
        Object[][] params = new Object[finalizedPRNCDocuments.size() + DEFAULT_PRE_SPLIT_PARM_COUNT][];
        params[params.length - 1] = new Object[] {summary.getStartDate()};
        for (int i = finalizedPRNCDocuments.size() - 1; i >= 0; i--) {
            params[i] = new Object[] {finalizedPRNCDocuments.get(i)};
        }
        return params;
    }

    /*
     * Helper method defining the SELECT parameters that come after any vendor ownership or category ones.
     */
    Object[][] getSelectParametersAfterSplitPoint(T summary) {
        // Get vendor foreign indicator param.
        return new Object[][] {
            {summary.vendorForeign ? KRADConstants.YES_INDICATOR_VALUE : KRADConstants.NO_INDICATOR_VALUE}
        };
    }


    @Override
    void buildRawTransactionRows(ResultSet rs, PreparedStatement insertStatement, T summary) throws SQLException {
        PRNCSourceRow prncRow = summary.prncRow;
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        int offset = rawDetailRow.insertOffset;
        Set<String> docIds = new HashSet<String>();
        String documentId;
        String financialObjectCode;
        
        
        BigDecimal netPaymentAmount;
        int currentBatchSize = 0;
        
        while (rs.next()) {
            // Initialize variables for current row.
            documentId = rs.getString(prncRow.preqDocumentNumber.index);
            financialObjectCode = rs.getString(prncRow.financialObjectCode.index);
            netPaymentAmount = rs.getBigDecimal(prncRow.amount.index);
            
            // Add doc ID to map if non-blank.
            if (StringUtils.isNotBlank(documentId)) {
                docIds.add(documentId);
            }
                        
            
            // If net payment amount is null, then set to zero
            if (netPaymentAmount == null) {
                netPaymentAmount = summary.zeroAmount;
            } 

            // Perform extra 1099-specific or 1042S-specific setup as needed.
            doTaxSpecificFirstPassRowSetup(rs, insertStatement, financialObjectCode, summary);
            
            insertStatement.setInt(rawDetailRow.reportYear.index - offset, summary.reportYear);
            insertStatement.setString(rawDetailRow.documentNumber.index - offset, StringUtils.isNotBlank(documentId) ? documentId : null);
            insertStatement.setString(rawDetailRow.documentType.index - offset, PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
            insertStatement.setInt(rawDetailRow.financialDocumentLineNumber.index - offset, rs.getInt(prncRow.accountIdentifier.index));
            insertStatement.setString(rawDetailRow.finObjectCode.index - offset, financialObjectCode);
            insertStatement.setBigDecimal(rawDetailRow.netPaymentAmount.index - offset, netPaymentAmount);
            insertStatement.setString(rawDetailRow.documentTitle.index - offset, rs.getString(prncRow.paymentMethodCode.index));
            insertStatement.setString(rawDetailRow.incomeClassCode.index - offset, rs.getString(prncRow.taxClassificationCode.index));
            insertStatement.setString(rawDetailRow.vendorTaxNumber.index - offset, rs.getString(prncRow.vendorTaxNumber.index));
            insertStatement.setString(rawDetailRow.payeeId.index - offset, rs.getString(prncRow.preqVendorHeaderGeneratedIdentifier.index) +  "-" + rs.getString(prncRow.preqVendorDetailAssignedIdentifier.index));
            insertStatement.setString(rawDetailRow.vendorTypeCode.index - offset, rs.getString(prncRow.vendorTypeCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCode.index - offset, rs.getString(prncRow.vendorOwnershipCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCategoryCode.index - offset, rs.getString(prncRow.vendorOwnershipCategoryCode.index));
            insertStatement.setString(rawDetailRow.vendorForeignIndicator.index - offset, rs.getString(prncRow.vendorForeignInd.index));
            insertStatement.setString(rawDetailRow.nraPaymentIndicator.index - offset, rs.getString(prncRow.vendorForeignInd.index));
            insertStatement.setString(rawDetailRow.chartCode.index - offset, rs.getString(prncRow.chartOfAccountsCode.index));
            insertStatement.setString(rawDetailRow.accountNumber.index - offset, rs.getString(prncRow.accountNumber.index));
            insertStatement.setString(rawDetailRow.paymentPayeeName.index - offset, rs.getString(prncRow.preqVendorName.index));
            insertStatement.setString(rawDetailRow.paymentLine1Address.index - offset, rs.getString(prncRow.vendorLine1Address.index));
            insertStatement.setString(rawDetailRow.paymentCountryName.index - offset, rs.getString(prncRow.vendorCountryCode.index));

            insertStatement.setString(rawDetailRow.ledgerDocumentTypeCode.index - offset, CUTaxConstants.TAX_SOURCE_PRNC);

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
        prepareForSecondPass(summary, docIds);
    }

    @Override
    void updateTransactionRowsFromWorkflowDocuments(ResultSet rs, PreparedStatement secondPassTransactionInsertStatement, T summary) throws SQLException {
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        String rawTransactionDetailId;
        String documentId;
        String initiatorPrincipalId;
        String initiatorPrincipalName;
        String paymentMethodCode;
        String vendorTaxNumber;
        java.sql.Date dateFinalized;
        DocumentRouteHeaderValue document;
        DocumentStatus documentStatus = null;
        boolean processCurrentRow;
        boolean useDateFinalized;
        java.sql.Date startDate = summary.getStartDate();
        java.sql.Date endDate = summary.getEndDate();
               
        // Update or remove rows as needed.
        while (rs.next()) {
            String documentType = rs.getString(rawDetailRow.documentType.index);
            
            // Retrieve payment method, which is temporarily stored in the doc title field.
            paymentMethodCode = rs.getString(rawDetailRow.documentTitle.index);
            
            if (isPaymentRequestDocument(documentType) && isForeignOrWireTransferPaymentMethod(summary, paymentMethodCode)) {
                // Initialized minimal variables for current row.
                processCurrentRow = true;
                rawTransactionDetailId = rs.getString(rawDetailRow.transactionDetailId.index);
                documentId = rs.getString(rawDetailRow.documentNumber.index);
                initiatorPrincipalId = null;
                documentStatus = null;
                dateFinalized = null;
                useDateFinalized = false;
                
                // Retrieve document info.
                document = getWorkflowDocumentForTaxRow(documentId, summary);
                if (document != null) {
                    initiatorPrincipalId = document.getInitiatorPrincipalId();
                    documentStatus = document.getStatus();
                    if (document.getDateFinalized() != null) {
                        dateFinalized = new java.sql.Date(document.getDateFinalized().getMillis());
                    }
                }
                
                // Depending on payment method, verify that the PRNC has indeed been finalized during the given time period.
                if (summary.foreignDraftCode.equals(paymentMethodCode) || summary.wireTransferCode.equals(paymentMethodCode)) {
                    // If a Foreign Draft or Wire Transfer, check the doc finalization date and status.
                    if (DocumentStatus.FINAL.equals(documentStatus) && dateFinalized != null
                            && dateFinalized.compareTo(startDate) >= 0 && dateFinalized.compareTo(endDate) <= 0) {
                        // If finalized during the current reporting period, then increment counters accordingly and use finalize date as payment date.
                        useDateFinalized = true;
                        if (summary.foreignDraftCode.equals(paymentMethodCode)) {
                            numForeignDraftsSelected++;
                        } else if (summary.wireTransferCode.equals(paymentMethodCode)) {
                            numWireTransfersSelected++;
                        }
                    } else {
                        // If not finalized or if in the wrong reporting period, then skip the current PRNC data row.
                        if (summary.foreignDraftCode.equals(paymentMethodCode)) {
                            numForeignDraftsIgnored++;
                        } else if (summary.wireTransferCode.equals(paymentMethodCode)) {
                            numWireTransfersIgnored++;
                        }
                        
                        // Skip any further processing for the current row.
                        processCurrentRow = false;
                    }
                }
                                
                
                if (processCurrentRow) {
                    // Finish initialization
                    SecondPassAttributeUpdateValues updatedAttributeValues = new SecondPassAttributeUpdateValues();
                    vendorTaxNumber = rs.getString(rawDetailRow.vendorTaxNumber.index);
          
                    // Check for null objects as needed, and get the initiator's principal name.
                    initiatorPrincipalName = checkForEntityAndAccountAndOrgExistence(initiatorPrincipalId,
                            rs.getString(rawDetailRow.chartCode.index), rs.getString(rawDetailRow.accountNumber.index), summary);
                    
                    // If vendor tax number is blank, then replace with a generated value accordingly.
                    if (StringUtils.isBlank(vendorTaxNumber)) {
                        vendorTaxNumber = getReplacementVendorTaxNumber(rs.getString(rawDetailRow.payeeId.index), summary);
                        updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.vendorTaxNumber.index, vendorTaxNumber);
                    }
                    
                    // Do tax-type-specific updates.
                    doTaxSpecificSecondPassRowSetup(rs, summary, updatedAttributeValues);
                    
                    // Update other fields as needed.
                    if (StringUtils.isBlank(documentId)) {
                        updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.documentNumber.index, CUTaxConstants.DOC_ID_ZERO);
                    }
                    updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.documentTitle.index,
                            (document != null && StringUtils.isNotBlank(document.getTitle())) ? document.getTitle() : CUTaxConstants.DOC_TITLE_IF_NOT_FOUND);
                    updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.initiatorNetId.index,
                            StringUtils.isNotBlank(initiatorPrincipalName) ? initiatorPrincipalName : CUTaxConstants.NETID_IF_NOT_FOUND);
                    if (useDateFinalized) {
                        updatedAttributeValues.putSqlDateAttributeForUpdating(rawDetailRow.paymentDate.index, dateFinalized);
                    }
                    
                    // Update the transaction row.
                    LOG.info("TransactionRowPRNCBuilder:: updateTransactionRowsFromWorkflowDocuments: Inserting updated second pass data for "
                            + "rawTransactionDetailId = {}, documentId = {}", rawTransactionDetailId, documentId);
                    insertUpdatedTransactionDetail(rs, secondPassTransactionInsertStatement, summary, updatedAttributeValues);
                } else {
                    // If a Foreign Draft or Wire Transfer that wasn't finalized or was in the wrong reporting period, then delete the row.
                    LOG.info("TransactionRowPRNCBuilder:: updateTransactionRowsFromWorkflowDocuments: NO data inserted for "
                            + "rawTransactionDetailId = {}, documentId = {}", rawTransactionDetailId, documentId);
                }
            }
        }
    }

	private boolean isForeignOrWireTransferPaymentMethod(T summary, String paymentMethodCode) {
		return StringUtils.equalsIgnoreCase(paymentMethodCode, summary.foreignDraftCode) || 
				StringUtils.equalsIgnoreCase(paymentMethodCode, summary.wireTransferCode);
	}

	private boolean isPaymentRequestDocument(String documentType) {
		return StringUtils.equalsIgnoreCase(PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT, documentType);
	}
    
    @Override
    void insertNullsForTransactionRow(PreparedStatement firstPassInsertStatement,
            RawTransactionDetailRow rawDetailRow, int offset) throws SQLException {
        super.insertNullsForTransactionRow(firstPassInsertStatement, rawDetailRow, offset);
        firstPassInsertStatement.setString(rawDetailRow.dvCheckStubText.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.incomeTaxTreatyExemptIndicator.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.foreignSourceIncomeIndicator.index - offset, null);
        firstPassInsertStatement.setBigDecimal(rawDetailRow.federalIncomeTaxPercent.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.paymentReasonCode.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.paymentDescription.index - offset, null);

        firstPassInsertStatement.setString(rawDetailRow.disbursementNbr.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.disbursementTypeCode.index - offset, null);
        firstPassInsertStatement.setString(rawDetailRow.paymentStatusCode.index - offset, null);
    }

    @Override
    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> stats = super.getStatistics();

        stats.put(TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_SELECTED, Integer.valueOf(numForeignDraftsSelected));
        stats.put(TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_IGNORED, Integer.valueOf(numForeignDraftsIgnored));
        stats.put(TaxStatType.NUM_PRNC_WIRE_TRANSFERS_SELECTED, Integer.valueOf(numWireTransfersSelected));
        stats.put(TaxStatType.NUM_PRNC_WIRE_TRANSFERS_IGNORED, Integer.valueOf(numWireTransfersIgnored));
        
        return stats;
    }
    
    abstract TaxTableField getExtraField(RawTransactionDetailRow rawDetailRow);

    @Override
    String getSqlForSelectingCreatedRows(T summary) {
        return TaxSqlUtils.getRawTransactionDetailSelectSql(getExtraField(summary.rawTransactionDetailRow), summary.rawTransactionDetailRow, true, false);
    }

    @Override
    Object[][] getParameterValuesForSelectingCreatedRows(T summary) {
        return new Object[][] {
            {summary.reportYear},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }



    // ==============================================================================================
    // ==============================================================================================
    // ==============================================================================================

    /**
     * Default implementation for building 1099 transaction detail rows from PRNC data.
     */
    static class For1099 extends TransactionRowPRNCBuilder<Transaction1099Summary> {
        
        For1099() {
            super();
        }
        
        @Override
        TaxTableField getExtraField(RawTransactionDetailRow rawDetailRow) {
            return rawDetailRow.form1099Box;
        }
        
        @Override
        String getTaxTypeSpecificConditionForSelect(Transaction1099Summary summary) {
            // Limit results based on vendor ownership type and ownership category.
            return TaxSqlUtils.getVendorOwnershipCriteria(summary.vendorOwnershipToCategoryMappings, summary.prncRow);
        }
        
        @Override
        Object[][] getParameterValuesForSelect(Transaction1099Summary summary) {
            return TaxSqlUtils.getArgsArrayWithVendorOwnershipParameters(summary.vendorOwnershipToCategoryMappings,
                    getSelectParametersBeforeSplitPoint(summary), getSelectParametersAfterSplitPoint(summary));
        }
        
        @Override
        void doTaxSpecificFirstPassRowSetup(ResultSet rs, PreparedStatement insertStatement, String financialObjectCode,
                Transaction1099Summary summary) throws SQLException {
            RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
            
            // Setup prepared statement args accordingly. Income-code-related args are null, and 1099 box is set to "?" for bulk retrieval convenience.
            insertStatement.setString(rawDetailRow.incomeCode.index - rawDetailRow.insertOffset, null);
            insertStatement.setString(rawDetailRow.incomeCodeSubType.index - rawDetailRow.insertOffset, null);
            insertStatement.setString(rawDetailRow.form1099Type.index - rawDetailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            insertStatement.setString(rawDetailRow.form1099Box.index - rawDetailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            insertStatement.setString(rawDetailRow.form1099OverriddenType.index - rawDetailRow.insertOffset, null);
            insertStatement.setString(rawDetailRow.form1099OverriddenBox.index - rawDetailRow.insertOffset, null);
            insertStatement.setString(rawDetailRow.form1042SBox.index - rawDetailRow.insertOffset, null);
            insertStatement.setString(rawDetailRow.form1042SOverriddenBox.index - rawDetailRow.insertOffset, null);
        }
        
        @Override
        void doTaxSpecificSecondPassRowSetup(ResultSet rs, Transaction1099Summary summary, SecondPassAttributeUpdateValues updateAttributeValues) throws SQLException {
            // Do nothing.
        }
    }



    // ==============================================================================================
    // ==============================================================================================
    // ==============================================================================================

    /**
     * Default implementation for building 1042S transaction detail rows from PRNC data.
     */
    static class For1042S extends TransactionRowPRNCBuilder<Transaction1042SSummary> {
        private int numExcludedAssignedIncomeCodes;
        private int numExcludedAssignedIncomeCodeSubTypes;
        private int numDeterminedFederalTaxWithheldIncomeCodes;
        private int numUndeterminedFederalTaxWithheldIncomeCodes;
        private int numDeterminedStateIncomeTaxWithheldIncomeCodes;
        private int numUndeterminedStateIncomeTaxWithheldIncomeCodes;
        
        For1042S() {
            super();
        }
        
        @Override
        TaxTableField getExtraField(RawTransactionDetailRow rawDetailRow) {
            return rawDetailRow.form1042SBox;
        }
        
        @Override
        String getTaxTypeSpecificConditionForSelect(Transaction1042SSummary summary) {
            // No extra conditions needed to filter out DV rows for 1042S processing.
            return KFSConstants.EMPTY_STRING;
        }
        
        @Override
        void doTaxSpecificFirstPassRowSetup(ResultSet rs, PreparedStatement insertStatement, String financialObjectCode,
                Transaction1042SSummary summary) throws SQLException {
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
        
        @Override
        void doTaxSpecificSecondPassRowSetup(ResultSet rs, Transaction1042SSummary summary, SecondPassAttributeUpdateValues updatedAttributeValues) throws SQLException {
            String financialObjectCode = rs.getString(summary.rawTransactionDetailRow.finObjectCode.index);
            String incomeClassCode = rs.getString(summary.rawTransactionDetailRow.incomeClassCode.index);;
            String incomeCode;
            String incomeCodeSubType;
            
            // Prepare the income and subtype codes.
            if (StringUtils.isNotBlank(incomeClassCode)) {
                // If income class code exists for the given object code, then get the income code and subtype.
                incomeCode = summary.incomeClassCodeToIrsIncomeCodeMap.get(incomeClassCode);
                incomeCodeSubType = summary.incomeClassCodeToIrsIncomeCodeSubTypeMap.get(incomeClassCode);
                if (StringUtils.isBlank(incomeCodeSubType)) {
                    // If no subtype was found, then set it to the exclusion value.
                    incomeCodeSubType = summary.excludedIncomeCodeSubType;
                    numExcludedAssignedIncomeCodeSubTypes++;
                }
            } else {
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
                // Set income code subtype to excluded value for now.
                incomeCodeSubType = summary.excludedIncomeCodeSubType;
            }
            
            // Prepare to update income code and income code subtype fields.
            updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCode.index, incomeCode);
            updatedAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCodeSubType.index, incomeCodeSubType);
        }
        
        @Override
        EnumMap<TaxStatType,Integer> getStatistics() {
            EnumMap<TaxStatType,Integer> stats = super.getStatistics();
            
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES, Integer.valueOf(numExcludedAssignedIncomeCodes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_PRNC, Integer.valueOf(numExcludedAssignedIncomeCodes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_PRNC, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
            stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_PRNC, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_PRNC, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedStateIncomeTaxWithheldIncomeCodes));
            
            return stats;
        }
        
    }

}
