package edu.cornell.kfs.tax.dataaccess.impl;

import edu.cornell.kfs.kew.routeheader.service.CuRouteHeaderService;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.businessobject.SecondPassAttributeUpdateValues;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxSqlUtils.SqlText;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.DvSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.RawTransactionDetailRow;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for building transaction detail rows from DV data.
 * See the static nested classes for default implementations.
 */
abstract class TransactionRowDvBuilder<T extends TransactionDetailSummary> extends TransactionRowBuilder<T> {
	private static final Logger LOG = LogManager.getLogger(TransactionRowDvBuilder.class);

    private static final int GETTER_BUILDER_SIZE = 2000;
    private static final int DEFAULT_PRE_SPLIT_PARM_COUNT = 3;

    private List<String> finalizedDvDocuments;

    // Variables pertaining to various statistics that will be collected during transaction row creation.
    private int numDvCheckStubTextsAltered;
    private int numDvCheckStubTextsNotAltered;
    private int numForeignDraftsSelected;
    private int numWireTransfersSelected;
    private int numForeignDraftsIgnored;
    private int numWireTransfersIgnored;

    TransactionRowDvBuilder() {
        super();
    }

    abstract TaxTableField getExtraField(RawTransactionDetailRow rawDetailRow);



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
        
        // Find all DV documents that were finalized between the start and end dates.
        CuRouteHeaderService routeHeaderService = (CuRouteHeaderService) SpringContext.getBean(
                RouteHeaderService.class, KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
        Map<String,java.sql.Timestamp> datesMap = routeHeaderService.getFinalizedDatesForDocumentType(DisbursementVoucherConstants.DOCUMENT_TYPE_CODE,
                new java.sql.Timestamp(summary.getStartDate().getTime()), new java.sql.Timestamp(endDateTime.getTime().getTime()));

        // Filter results to only include Foreign Draft and Wire Transfer DVs.
        finalizedDvDocuments = processingDao.findForeignDraftsAndWireTransfers(new ArrayList<String>(datesMap.keySet()), summary, DisbursementVoucherConstants.DOCUMENT_TYPE_CODE);
        if (finalizedDvDocuments.isEmpty()) {
            // If no matching DV documents were found, then just add a dummy value to prevent query generation problems.
            finalizedDvDocuments.add(CUTaxConstants.DOC_ID_ZERO);
        }
    }



    @Override
    String getTaxSourceName() {
        return CUTaxConstants.TAX_SOURCE_DV;
    }

    @Override
    String getSqlForSelect(T summary) {
        StringBuilder fullSql = new StringBuilder(GETTER_BUILDER_SIZE);
        DvSourceRow dvRow = summary.dvRow;
        
        // Build the query.
        TaxSqlUtils.appendQuery(fullSql,
                SqlText.SELECT, dvRow.orderedFields, SqlText.FROM, dvRow.tables,
                SqlText.WHERE,
                
                SqlText.PAREN_OPEN,
                        SqlText.PAREN_OPEN,
                                // If not a foreign draft or wire transfer, make sure the DV was paid during the given time period.
                                        dvRow.paidDate, SqlText.IS_NOT_NULL,
                                SqlText.AND,
                                        dvRow.paidDate, SqlText.BETWEEN, SqlText.PARAMETER, SqlText.AND, SqlText.PARAMETER,
                                SqlText.AND,
                                        dvRow.universityDate, SqlText.EQUALS, dvRow.paidDate,
                        SqlText.PAREN_CLOSE,
                        SqlText.OR,
                        SqlText.PAREN_OPEN,
                                // If a foreign draft or wire transfer, make sure the DV was finalized during the given time period.
                                        dvRow.documentDisbVchrPaymentMethodCode, SqlText.IN, "('F','W')",
                                SqlText.AND,
                                        dvRow.paidDate, SqlText.IS_NULL,
                                SqlText.AND,
                                        TaxSqlUtils.getInListCriteria(dvRow.dvDocumentNumber, finalizedDvDocuments.size(), true, true),
                                SqlText.AND,
                                        dvRow.universityDate, SqlText.EQUALS, SqlText.PARAMETER,
                        SqlText.PAREN_CLOSE,
                SqlText.PAREN_CLOSE,
                
                // Add doc number criteria.
                SqlText.AND,
                        dvRow.payeeDetailDocumentNumber, SqlText.EQUALS, dvRow.nraDocumentNumber,
                SqlText.AND,
                        dvRow.payeeDetailDocumentNumber, SqlText.EQUALS, dvRow.accountingLineDocumentNumber,
                SqlText.AND,
                        dvRow.payeeDetailDocumentNumber, SqlText.EQUALS, dvRow.dvDocumentNumber,
                SqlText.AND,
                    dvRow.custPaymentDocNbr, SqlText.CONDITIONAL_EQUALS_JOIN, dvRow.payeeDetailDocumentNumber,
                
                // Add various IS NOT NULL criteria.
                SqlText.AND,
                        dvRow.payeeDetailDocumentNumber, SqlText.IS_NOT_NULL,
                SqlText.AND,
                        dvRow.accountingLineDocumentNumber, SqlText.IS_NOT_NULL,
                SqlText.AND,
                        dvRow.accountingLineSequenceNumber, SqlText.IS_NOT_NULL,
                SqlText.AND,
                        dvRow.financialDocumentLineTypeCode, SqlText.IS_NOT_NULL,
                
                // And vendor criteria and tax-type-specific criteria.
                SqlText.AND,
                        dvRow.disbursementVoucherPayeeTypeCode, SqlText.EQUALS, "'V'",
                SqlText.AND,
                        TaxSqlUtils.getPayeeIdToVendorHeaderIdCriteria(dvRow.disbVchrPayeeIdNumber, dvRow.vendorHeaderGeneratedId),
                getTaxTypeSpecificConditionForSelect(summary),
                SqlText.AND,
                        dvRow.vendorForeignInd, SqlText.EQUALS, SqlText.PARAMETER,
                
                // Build the ORDER BY clause.
                SqlText.ORDER_BY,
                        new Object[][] {{dvRow.vendorTaxNumber}, {dvRow.extractDate}, {dvRow.dvDocumentNumber}, {dvRow.accountingLineSequenceNumber}});
        
        
        
        // Log and return the full query.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Final DV selection query: " + fullSql.toString());
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
        // Get parameters. First and last are date range start, second is range end, and the rest are document IDs.
        Object[][] params = new Object[finalizedDvDocuments.size() + DEFAULT_PRE_SPLIT_PARM_COUNT][];
        params[0] = new Object[] {summary.getStartDate()};
        params[1] = new Object[] {summary.getEndDate()};
        params[params.length - 1] = new Object[] {summary.getStartDate()};
        for (int i = finalizedDvDocuments.size() - 1; i >= 0; i--) {
            params[i + 2] = new Object[] {finalizedDvDocuments.get(i)};
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



    @Override
    void buildRawTransactionRows(ResultSet rs, PreparedStatement insertStatement, T summary) throws SQLException {
        DvSourceRow dvRow = summary.dvRow;
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        int offset = rawDetailRow.insertOffset;
        Set<String> docIds = new HashSet<String>();
        String documentId;
        String financialObjectCode;
        
        
        BigDecimal netPaymentAmount;
        int currentBatchSize = 0;
        
        while (rs.next()) {
            // Initialize variables for current row.
            documentId = rs.getString(dvRow.payeeDetailDocumentNumber.index);
            financialObjectCode = rs.getString(dvRow.financialObjectCode.index);
            netPaymentAmount = rs.getBigDecimal(dvRow.amount.index);
            
            // Add doc ID to map if non-blank.
            if (StringUtils.isNotBlank(documentId)) {
                docIds.add(documentId);
            }
            
            
            
            // If net payment amount is null, then set to zero. Otherwise, negate it if it's a debit amount.
            if (netPaymentAmount == null) {
                netPaymentAmount = summary.zeroAmount;
            } else if (KFSConstants.GL_DEBIT_CODE.equals(rs.getString(dvRow.debitCreditCode.index))) {
                netPaymentAmount = netPaymentAmount.negate();
            }
            
            // Perform extra 1099-specific or 1042S-specific setup as needed.
            doTaxSpecificFirstPassRowSetup(rs, insertStatement, financialObjectCode, summary);
            
            /*
             * Prepare to insert another transaction detail row.
             * 
             * NOTE: We temporarily store the payment method code in the doc title slot,
             * to be processed and replaced on the next pass.
             * 
             * NOTE: It is expected that subclasses use the "doTaxSpecificRowSetup"
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
             * PMT_DT (if the row represents a Foreign Draft or Wire Transfer)
             * VENDOR_TAX_NBR (if null, in which case it should be set to an auto-generated value)
             * DV_CHK_STUB_TXT (if it contains non-printable characters, in which case they should be removed)
             */
            insertStatement.setInt(rawDetailRow.reportYear.index - offset, summary.reportYear);
            insertStatement.setString(rawDetailRow.documentNumber.index - offset, StringUtils.isNotBlank(documentId) ? documentId : null);
            insertStatement.setString(rawDetailRow.documentType.index - offset, DisbursementVoucherConstants.DOCUMENT_TYPE_CODE);
            insertStatement.setInt(rawDetailRow.financialDocumentLineNumber.index - offset, rs.getInt(dvRow.accountingLineSequenceNumber.index));
            insertStatement.setString(rawDetailRow.finObjectCode.index - offset, financialObjectCode);
            insertStatement.setBigDecimal(rawDetailRow.netPaymentAmount.index - offset, netPaymentAmount);
            insertStatement.setString(rawDetailRow.documentTitle.index - offset, rs.getString(dvRow.documentDisbVchrPaymentMethodCode.index));
            insertStatement.setString(rawDetailRow.vendorTaxNumber.index - offset, rs.getString(dvRow.vendorTaxNumber.index));
            insertStatement.setString(rawDetailRow.dvCheckStubText.index - offset, rs.getString(dvRow.disbVchrCheckStubText.index));
            insertStatement.setString(rawDetailRow.payeeId.index - offset, rs.getString(dvRow.disbVchrPayeeIdNumber.index));
            insertStatement.setString(rawDetailRow.vendorTypeCode.index - offset, rs.getString(dvRow.vendorTypeCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCode.index - offset, rs.getString(dvRow.vendorOwnershipCode.index));
            insertStatement.setString(rawDetailRow.vendorOwnershipCategoryCode.index - offset, rs.getString(dvRow.vendorOwnershipCategoryCode.index));
            insertStatement.setString(rawDetailRow.vendorForeignIndicator.index - offset, rs.getString(dvRow.vendorForeignInd.index));
            insertStatement.setString(rawDetailRow.nraPaymentIndicator.index - offset, rs.getString(dvRow.disbVchrNonresidentPaymentCode.index));
            insertStatement.setDate(rawDetailRow.paymentDate.index - offset, rs.getDate(dvRow.paidDate.index));
            insertStatement.setString(rawDetailRow.paymentPayeeName.index - offset, rs.getString(dvRow.disbVchrPayeePersonName.index));
            insertStatement.setString(rawDetailRow.incomeClassCode.index - offset, rs.getString(dvRow.incomeClassCode.index));
            insertStatement.setString(rawDetailRow.incomeTaxTreatyExemptIndicator.index - offset, rs.getString(dvRow.incomeTaxTreatyExemptCode.index));
            insertStatement.setString(rawDetailRow.foreignSourceIncomeIndicator.index - offset, rs.getString(dvRow.foreignSourceIncomeCode.index));
            insertStatement.setBigDecimal(rawDetailRow.federalIncomeTaxPercent.index - offset, rs.getBigDecimal(dvRow.federalIncomeTaxPercent.index));
            insertStatement.setString(rawDetailRow.paymentDescription.index - offset, rs.getString(dvRow.financialDocumentLineDescription.index));
            insertStatement.setString(rawDetailRow.paymentLine1Address.index - offset, rs.getString(dvRow.disbVchrPayeeLine1Addr.index));
            insertStatement.setString(rawDetailRow.paymentCountryName.index - offset, rs.getString(dvRow.disbVchrPayeeCountryCode.index));
            insertStatement.setString(rawDetailRow.chartCode.index - offset, rs.getString(dvRow.chartOfAccountsCode.index));
            insertStatement.setString(rawDetailRow.accountNumber.index - offset, rs.getString(dvRow.accountNumber.index));
            insertStatement.setString(rawDetailRow.paymentReasonCode.index - offset, rs.getString(dvRow.disbVchrPaymentReasonCode.index));

            String paymentMethodCode = rs.getString(dvRow.documentDisbVchrPaymentMethodCode.index);
            String disbursementNbr = isPaymentCodeWireOrForeignDraft(paymentMethodCode) ? null: rs.getString(dvRow.disbursementNbr.index);
            String disbursementTypeCode = isPaymentCodeWireOrForeignDraft(paymentMethodCode) ? null : rs.getString(dvRow.disbursementTypeCode.index);
            String paymentStatusCode = isPaymentCodeWireOrForeignDraft(paymentMethodCode) ? null : rs.getString(dvRow.paymentStatusCode.index);
            String ledgerDocumentType = getLedgerDocumentTypeCode(paymentMethodCode);
            insertStatement.setString(rawDetailRow.disbursementNbr.index - offset, disbursementNbr);
            insertStatement.setString(rawDetailRow.disbursementTypeCode.index - offset, disbursementTypeCode);
            insertStatement.setString(rawDetailRow.paymentStatusCode.index - offset, paymentStatusCode);
            insertStatement.setString(rawDetailRow.ledgerDocumentTypeCode.index - offset, ledgerDocumentType);

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


    private String getLedgerDocumentTypeCode(String paymentMethodCode) {
        String ledgerDocumentTypeCode = DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH;
        if (isPaymentCodeWireOrForeignDraft(paymentMethodCode)) {
            ledgerDocumentTypeCode = DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD;
        }
        return ledgerDocumentTypeCode;
    }

    private boolean isPaymentCodeWireOrForeignDraft(String paymentMethodCode) {
        return ObjectUtils.isNotNull(paymentMethodCode) &&
                (paymentMethodCode.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE) || paymentMethodCode.equals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_DRAFT));
    }

    @Override
    void updateTransactionRowsFromWorkflowDocuments(ResultSet rs, PreparedStatement secondPassTransactionInsertStatement, T summary) throws SQLException {
        RawTransactionDetailRow rawDetailRow = summary.rawTransactionDetailRow;
        Pattern nonPrintableCharsPattern = Pattern.compile("[^\\p{Graph}\\p{Space}]");
        String rawTransactionDetailId;
        String documentId;
        String initiatorPrincipalId;
        String initiatorPrincipalName;
        String paymentMethodCode;
        String vendorTaxNumber;
        String checkStubText;
        Matcher checkStubMatcher;
        java.sql.Date dateFinalized;
        DocumentRouteHeaderValue document;
        DocumentStatus documentStatus = null;
        boolean processCurrentRow;
        boolean useDateFinalized;
        java.sql.Date startDate = summary.getStartDate();
        java.sql.Date endDate = summary.getEndDate();
        
        
        
        // Update or remove rows as needed.
        while (rs.next()) {
            // Only update DV-related rows.
            if (DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(rs.getString(rawDetailRow.documentType.index))) {
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
                // Retrieve payment method, which is temporarily stored in the doc title field.
                paymentMethodCode = rs.getString(rawDetailRow.documentTitle.index);
                
                
                // Depending on payment method, verify that the DV has indeed been finalized during the given time period.
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
                        // If not finalized or if in the wrong reporting period, then skip the current DV data row.
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
                    // Finish initialization.
                    SecondPassAttributeUpdateValues updatedAttributeValues = new SecondPassAttributeUpdateValues();
                    vendorTaxNumber = rs.getString(rawDetailRow.vendorTaxNumber.index);
                    checkStubText = rs.getString(rawDetailRow.dvCheckStubText.index);
                    checkStubMatcher = nonPrintableCharsPattern.matcher((checkStubText != null) ? checkStubText : KFSConstants.EMPTY_STRING);
                    
                    // Check for null objects as needed, and get the initiator's principal name.
                    initiatorPrincipalName = checkForEntityAndAccountAndOrgExistence(initiatorPrincipalId,
                            rs.getString(rawDetailRow.chartCode.index), rs.getString(rawDetailRow.accountNumber.index), summary);
                    
                    // If vendor tax number is blank, then replace with a generated value accordingly.
                    if (StringUtils.isBlank(vendorTaxNumber)) {
                        vendorTaxNumber = getReplacementVendorTaxNumber(rs.getString(rawDetailRow.payeeId.index), summary);
                        updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.vendorTaxNumber.index, vendorTaxNumber);
                    }
                    
                    // Remove unprintable characters from the check stub text if necessary.
                    if (checkStubMatcher.find()) {
                        checkStubText = checkStubMatcher.replaceAll(KFSConstants.EMPTY_STRING);
                        updatedAttributeValues.putStringAttributeForUpdating(rawDetailRow.dvCheckStubText.index, checkStubText);
                        numDvCheckStubTextsAltered++;
                    } else {
                        numDvCheckStubTextsNotAltered++;
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
                    
                    // Insert updated first pass raw data into second pass transaction row table.
                    LOG.info("TransactionRowDvBuilder:: updateTransactionRowsFromWorkflowDocuments: Inserting updated second pass data for "
                            + "rawTransactionDetailId = {}, documentId = {}", rawTransactionDetailId, documentId);
                    insertUpdatedTransactionDetail(rs, secondPassTransactionInsertStatement, summary, updatedAttributeValues);
                } else {
                    // If a Foreign Draft or Wire Transfer that wasn't finalized or was in the wrong reporting period,
                    // then log the raw transaction data row was not inserted into the second pass table.
                    LOG.info("TransactionRowDvBuilder:: updateTransactionRowsFromWorkflowDocuments: NO data inserted for "
                            + "rawTransactionDetailId = {}, documentId = {}", rawTransactionDetailId, documentId);
                }
            }
        }
    }



    @Override
    EnumMap<TaxStatType,Integer> getStatistics() {
        EnumMap<TaxStatType,Integer> stats = super.getStatistics();
        
        stats.put(TaxStatType.NUM_DV_CHECK_STUB_TEXTS_ALTERED, Integer.valueOf(numDvCheckStubTextsAltered));
        stats.put(TaxStatType.NUM_DV_CHECK_STUB_TEXTS_NOT_ALTERED, Integer.valueOf(numDvCheckStubTextsNotAltered));
        stats.put(TaxStatType.NUM_DV_FOREIGN_DRAFTS_SELECTED, Integer.valueOf(numForeignDraftsSelected));
        stats.put(TaxStatType.NUM_DV_FOREIGN_DRAFTS_IGNORED, Integer.valueOf(numForeignDraftsIgnored));
        stats.put(TaxStatType.NUM_DV_WIRE_TRANSFERS_SELECTED, Integer.valueOf(numWireTransfersSelected));
        stats.put(TaxStatType.NUM_DV_WIRE_TRANSFERS_IGNORED, Integer.valueOf(numWireTransfersIgnored));
        
        return stats;
    }



    // ==============================================================================================
    // ==============================================================================================
    // ==============================================================================================

    /**
     * Default implementation for building 1099 transaction detail rows from DV data.
     */
    static class For1099 extends TransactionRowDvBuilder<Transaction1099Summary> {
        
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
            return TaxSqlUtils.getVendorOwnershipCriteria(summary.vendorOwnershipToCategoryMappings, summary.dvRow);
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
     * Default implementation for building 1042S transaction detail rows from DV data.
     */
    static class For1042S extends TransactionRowDvBuilder<Transaction1042SSummary> {
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
        void doTaxSpecificSecondPassRowSetup(ResultSet rs, Transaction1042SSummary summary, SecondPassAttributeUpdateValues updateAttributeValues) throws SQLException {
            String financialObjectCode = rs.getString(summary.rawTransactionDetailRow.finObjectCode.index);
            String incomeClassCode;
            String incomeCode;
            String incomeCodeSubType;
            
            // Prepare the income and subtype codes.
            incomeClassCode = rs.getString(summary.rawTransactionDetailRow.incomeClassCode.index);
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
            updateAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCode.index, incomeCode);
            updateAttributeValues.putStringAttributeForUpdating(summary.transactionDetailRow.incomeCodeSubType.index, incomeCodeSubType);
        }
        
        @Override
        EnumMap<TaxStatType,Integer> getStatistics() {
            EnumMap<TaxStatType,Integer> stats = super.getStatistics();
            
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES, Integer.valueOf(numExcludedAssignedIncomeCodes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_DV, Integer.valueOf(numExcludedAssignedIncomeCodes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
            stats.put(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_DV, Integer.valueOf(numExcludedAssignedIncomeCodeSubTypes));
            stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_DV, Integer.valueOf(numDeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedFederalTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_DV, Integer.valueOf(numDeterminedStateIncomeTaxWithheldIncomeCodes));
            stats.put(TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, Integer.valueOf(numUndeterminedStateIncomeTaxWithheldIncomeCodes));
            
            return stats;
        }
        
    }

}
