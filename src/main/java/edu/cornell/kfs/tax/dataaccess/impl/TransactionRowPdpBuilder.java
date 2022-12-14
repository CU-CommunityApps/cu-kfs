package edu.cornell.kfs.tax.dataaccess.impl;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.dataaccess.impl.TaxSqlUtils.SqlText;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.PdpSourceRow;
import edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow;

/**
 * Base class for building transaction detail rows from PDP data.
 * See the static nested classes for default implementations.
 */
abstract class TransactionRowPdpBuilder<T extends TransactionDetailSummary> extends TransactionRowBuilder<T> {
	private static final Logger LOG = LogManager.getLogger(TransactionRowPdpBuilder.class);

    private static final int GETTER_BUILDER_SIZE = 2000;

    TransactionRowPdpBuilder() {
        super();
    }

    abstract TaxTableField getExtraField(TransactionDetailRow detailRow);



    @Override
    String getTaxSourceName() {
        return CUTaxConstants.TAX_SOURCE_PDP;
    }

    @Override
    String getSqlForSelect(T summary) {
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
                getTaxTypeSpecificConditionForSelect(summary),
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

    @Override
    Object[][] getParameterValuesForSelect(T summary) {
        return TaxSqlUtils.getArgsArrayWithVendorOwnershipParameters(Collections.<String,Set<String>>emptyMap(),
                getSelectParametersBeforeSplitPoint(summary), getSelectParametersAfterSplitPoint(summary));
    }

    /*
     * Helper method defining the SELECT parameters that precede any vendor ownership or category ones.
     */
    Object[][] getSelectParametersBeforeSplitPoint(T summary) {
        // Setup parameters. First one is start of DISB_TS range, and second is the end of that range.
        return new Object[][] {
            {summary.getStartDate()},
            {summary.getEndDate()}
        };
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
        return TaxSqlUtils.getTransactionDetailSelectSql(getExtraField(summary.transactionDetailRow), summary.transactionDetailRow, true, false);
    }

    @Override
    Object[][] getParameterValuesForSelectingCreatedRows(T summary) {
        return new Object[][] {
            {summary.reportYear},
            {CUTaxConstants.NEEDS_UPDATING_BOX_KEY}
        };
    }



    @Override
    void buildTransactionRows(ResultSet rs, PreparedStatement insertStatement, T summary) throws SQLException {
        PdpSourceRow pdpRow = summary.pdpRow;
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        int offset = detailRow.insertOffset;
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
            
            // Perform extra 1099-specific or 1042S-specific setup as needed.
            doTaxSpecificRowSetup(rs, insertStatement, financialObjectCode, summary);
            
            /*
             * Prepare to insert another transaction detail row.
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
             * VENDOR_TAX_NBR (if null, in which case it should be set to an auto-generated value)
             */
            
            String documentType = rs.getString(pdpRow.financialDocumentTypeCode.index);
            
            insertStatement.setInt(detailRow.reportYear.index - offset, summary.reportYear);
            insertStatement.setString(detailRow.documentNumber.index - offset, (StringUtils.isNotBlank(documentId)) ? documentId : null);
            insertStatement.setString(detailRow.documentType.index - offset, documentType);
            insertStatement.setInt(detailRow.financialDocumentLineNumber.index - offset, rs.getInt(pdpRow.accountDetailId.index));
            insertStatement.setString(detailRow.finObjectCode.index - offset, financialObjectCode);
            insertStatement.setBigDecimal(detailRow.netPaymentAmount.index - offset, netPaymentAmount);
            insertStatement.setString(detailRow.vendorTaxNumber.index - offset, rs.getString(pdpRow.vendorTaxNumber.index));
            insertStatement.setString(detailRow.payeeId.index - offset, rs.getString(pdpRow.payeeId.index));
            insertStatement.setString(detailRow.vendorTypeCode.index - offset, rs.getString(pdpRow.vendorTypeCode.index));
            insertStatement.setString(detailRow.vendorOwnershipCode.index - offset, rs.getString(pdpRow.vendorOwnershipCode.index));
            insertStatement.setString(detailRow.vendorOwnershipCategoryCode.index - offset, rs.getString(pdpRow.vendorOwnershipCategoryCode.index));
            insertStatement.setString(detailRow.vendorForeignIndicator.index - offset, rs.getString(pdpRow.vendorForeignInd.index));
            insertStatement.setString(detailRow.nraPaymentIndicator.index - offset, rs.getString(pdpRow.nraPayment.index));
            insertStatement.setDate(detailRow.paymentDate.index - offset, rs.getDate(pdpRow.disbursementDate.index));
            insertStatement.setString(detailRow.paymentPayeeName.index - offset, rs.getString(pdpRow.payeeName.index));
            insertStatement.setString(detailRow.paymentDescription.index - offset, rs.getString(pdpRow.achPaymentDescription.index));
            insertStatement.setString(detailRow.paymentLine1Address.index - offset, rs.getString(pdpRow.line1Address.index));
            insertStatement.setString(detailRow.paymentCountryName.index - offset, rs.getString(pdpRow.country.index));
            insertStatement.setString(detailRow.chartCode.index - offset, rs.getString(pdpRow.accountDetailFinChartCode.index)); // ?
            insertStatement.setString(detailRow.accountNumber.index - offset, rs.getString(pdpRow.accountNbr.index));
            insertStatement.setString(detailRow.incomeClassCode.index - offset, findincomeClassCode(rs, pdpRow, documentType));
            insertStatement.setString(detailRow.disbursementNbr.index - offset, rs.getString(pdpRow.disbursementNbr.index));
            insertStatement.setString(detailRow.paymentStatusCode.index - offset, rs.getString(pdpRow.paymentStatusCode.index));
            insertStatement.setString(detailRow.disbursementTypeCode.index - offset, rs.getString(pdpRow.disbursementTypeCode.index));
            insertStatement.setString(detailRow.ledgerDocumentTypeCode.index - offset, rs.getString(pdpRow.financialDocumentTypeCode.index));

            insertNullsForTransactionRow(insertStatement, detailRow, offset);

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

	protected String findincomeClassCode(ResultSet rs, PdpSourceRow pdpRow, String documentType) throws SQLException {
		String incomeClassCode = null;
		if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.PAYMENT_REQUEST)) {
			incomeClassCode = rs.getString(pdpRow.taxClassificationCode.index);
		} else if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.DISBURSEMENT_VOUCHER)) {
			incomeClassCode = rs.getString(pdpRow.dvIncomeClassCode.index);
		}
		return incomeClassCode;
	}

    /**
     * Overridden to also insert nulls for the following field placeholders:
     * 
     * <ul>
     *   <li>documentTitle</li>
     *   <li>dvCheckStubText</li>
     *   <li>incomeTaxTreatyExemptIndicator</li>
     *   <li>foreignSourceIncomeIndicator</li>
     *   <li>federalIncomeTaxPercent</li>
     *   <li>paymentReasonCode</li>
     * </ul>
     * 
     * @see edu.cornell.kfs.tax.dataaccess.impl.TransactionRowBuilder#insertNullsForTransactionRow(java.sql.PreparedStatement,
     * edu.cornell.kfs.tax.dataaccess.impl.TaxTableRow.TransactionDetailRow, int)
     */
    @Override
    void insertNullsForTransactionRow(PreparedStatement insertStatement, TransactionDetailRow detailRow, int offset) throws SQLException {
        super.insertNullsForTransactionRow(insertStatement, detailRow, offset);
        insertStatement.setString(detailRow.documentTitle.index - offset, null);
        insertStatement.setString(detailRow.dvCheckStubText.index - offset, null);
        insertStatement.setString(detailRow.incomeTaxTreatyExemptIndicator.index - offset, null);
        insertStatement.setString(detailRow.foreignSourceIncomeIndicator.index - offset, null);
        insertStatement.setBigDecimal(detailRow.federalIncomeTaxPercent.index - offset, null);
        insertStatement.setString(detailRow.paymentReasonCode.index - offset, null);
    }



    @Override
    void updateTransactionRowsFromWorkflowDocuments(ResultSet rs, T summary) throws SQLException {
        TransactionDetailRow detailRow = summary.transactionDetailRow;
        String documentId;
        String initiatorPrincipalId;
        String initiatorPrincipalName;
        String vendorTaxNumber;
        DocumentRouteHeaderValue document;
        
        
        
        // Perform row updates as needed.
        while (rs.next()) {
            // Only update PDP-related rows.
            if (!DisbursementVoucherConstants.DOCUMENT_TYPE_CODE.equals(rs.getString(detailRow.documentType.index))) {
                // Initialize variables.
                documentId = rs.getString(detailRow.documentNumber.index);
                initiatorPrincipalId = null;
                vendorTaxNumber = rs.getString(detailRow.vendorTaxNumber.index);
                
                // Retrieve document info.
                document = getWorkflowDocumentForTaxRow(documentId, summary);
                if (document != null) {
                    initiatorPrincipalId = document.getInitiatorPrincipalId();
                }
                
                // Check for null objects as needed, and get the initiator's principal name.
                initiatorPrincipalName = checkForEntityAndAccountAndOrgExistence(initiatorPrincipalId,
                        rs.getString(detailRow.chartCode.index), rs.getString(detailRow.accountNumber.index), summary);
                
                // If vendor tax number is blank, then replace with a generated value accordingly.
                if (StringUtils.isBlank(vendorTaxNumber)) {
                    vendorTaxNumber = getReplacementVendorTaxNumber(rs.getString(detailRow.payeeId.index), summary);
                    rs.updateString(detailRow.vendorTaxNumber.index, vendorTaxNumber);
                }
                
                // Do tax-type-specific updates.
                doTaxSpecificSecondPassRowSetup(rs, summary);
                
                // Update other fields as needed.
                if (StringUtils.isBlank(documentId)) {
                    rs.updateString(detailRow.documentNumber.index, CUTaxConstants.DOC_ID_ZERO);
                }
                rs.updateString(detailRow.documentTitle.index, (document != null && StringUtils.isNotBlank(document.getTitle()))
                        ? document.getTitle() : CUTaxConstants.DOC_TITLE_IF_NOT_FOUND);
                rs.updateString(detailRow.initiatorNetId.index,
                        StringUtils.isNotBlank(initiatorPrincipalName) ? initiatorPrincipalName : CUTaxConstants.NETID_IF_NOT_FOUND);
                
                // Update the current row.
                rs.updateRow();
            }
        }
    }



    // ==============================================================================================
    // ==============================================================================================
    // ==============================================================================================

    /**
     * Default implementation for building 1099 transaction detail rows from PDP data.
     */
    static class For1099 extends TransactionRowPdpBuilder<Transaction1099Summary> {
        
        For1099() {
            super();
        }
        
        @Override
        TaxTableField getExtraField(TransactionDetailRow detailRow) {
            return detailRow.form1099Box;
        }
        
        @Override
        String getTaxTypeSpecificConditionForSelect(Transaction1099Summary summary) {
            // Limit results based on vendor ownership type and ownership category.
            return TaxSqlUtils.getVendorOwnershipCriteria(summary.vendorOwnershipToCategoryMappings, summary.pdpRow);
        }
        
        @Override
        Object[][] getParameterValuesForSelect(Transaction1099Summary summary) {
            return TaxSqlUtils.getArgsArrayWithVendorOwnershipParameters(summary.vendorOwnershipToCategoryMappings,
                    getSelectParametersBeforeSplitPoint(summary), getSelectParametersAfterSplitPoint(summary));
        }
        
        @Override
        void doTaxSpecificRowSetup(ResultSet rs, PreparedStatement insertStatement, String financialObjectCode,
                Transaction1099Summary summary) throws SQLException {
            TransactionDetailRow detailRow = summary.transactionDetailRow;
            
            // Setup prepared statement args accordingly. Income-code-related args are null, and 1099 box is set to "?" for bulk retrieval convenience.
            insertStatement.setString(detailRow.incomeCode.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.incomeCodeSubType.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099Type.index - detailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            insertStatement.setString(detailRow.form1099Box.index - detailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            insertStatement.setString(detailRow.form1099OverriddenType.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099OverriddenBox.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1042SBox.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1042SOverriddenBox.index - detailRow.insertOffset, null);
        }
        
        @Override
        void doTaxSpecificSecondPassRowSetup(ResultSet rs, Transaction1099Summary summary) throws SQLException {
            // Do nothing.
        }
        
    }



    // ==============================================================================================
    // ==============================================================================================
    // ==============================================================================================

    /**
     * Default implementation for building 1042S transaction detail rows from PDP data.
     */
    static class For1042S extends TransactionRowPdpBuilder<Transaction1042SSummary> {
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
        TaxTableField getExtraField(TransactionDetailRow detailRow) {
            return detailRow.form1042SBox;
        }
        
        @Override
        String getTaxTypeSpecificConditionForSelect(Transaction1042SSummary summary) {
            // No extra conditions needed to filter out PDP rows for 1042S processing.
            return KFSConstants.EMPTY_STRING;
        }
        
        @Override
        void doTaxSpecificRowSetup(ResultSet rs, PreparedStatement insertStatement, String financialObjectCode,
                Transaction1042SSummary summary) throws SQLException {
            TransactionDetailRow detailRow = summary.transactionDetailRow;
            
            // Setup prepared statement args accordingly. The 1042S box is set to "?" for bulk retrieval convenience.
            insertStatement.setString(detailRow.incomeCode.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.incomeCodeSubType.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099Type.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099Box.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099OverriddenType.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1099OverriddenBox.index - detailRow.insertOffset, null);
            insertStatement.setString(detailRow.form1042SBox.index - detailRow.insertOffset, CUTaxConstants.NEEDS_UPDATING_BOX_KEY);
            insertStatement.setString(detailRow.form1042SOverriddenBox.index - detailRow.insertOffset, null);
        }
        
        @Override
        void doTaxSpecificSecondPassRowSetup(ResultSet rs, Transaction1042SSummary summary) throws SQLException {
            String financialObjectCode = rs.getString(summary.transactionDetailRow.finObjectCode.index);
            String incomeClassCode = rs.getString(summary.transactionDetailRow.incomeClassCode.index);
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
            rs.updateString(summary.transactionDetailRow.incomeCode.index, incomeCode);
            rs.updateString(summary.transactionDetailRow.incomeCodeSubType.index, incomeCodeSubType);
        }
        
        @Override
        EnumMap<TaxStatType,Integer> getStatistics() {
            EnumMap<TaxStatType,Integer> stats = super.getStatistics();
            
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
        
    }

}
