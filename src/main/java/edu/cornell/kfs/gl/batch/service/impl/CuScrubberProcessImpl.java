package edu.cornell.kfs.gl.batch.service.impl;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.gl.batch.ScrubberStep;
import org.kuali.kfs.gl.batch.service.impl.ScrubberProcessImpl;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.gl.businessobject.ScrubberProcessTransactionError;
import org.kuali.kfs.gl.service.ScrubberReportData;
import org.kuali.kfs.gl.service.impl.StringHelper;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.exception.InvalidFlexibleOffsetException;
import org.kuali.kfs.core.api.parameter.ParameterEvaluator;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

/**
 * Cornell's implementation of ScrubberProcess that extends from the KFS-delivered ScrubberProcessImpl.
 */
public class CuScrubberProcessImpl extends ScrubberProcessImpl {
    private static final Logger LOG = LogManager.getLogger(CuScrubberProcessImpl.class);

    private static final int CONTINUATION_ACCOUNT_DEPTH_LIMIT = 10;
    private static final String SCRUBBER_JOB_PLANT_INDEBTEDNESS_COMPONENT = "ScrubberJobPlantFundIndebtedness";

    /**
     * Overridden to update cost share source account entries so that they will post to the
     * continuation account if the cost share account is closed.
     * 
     * @see org.kuali.kfs.gl.batch.service.impl.ScrubberProcessImpl#generateCostShareEntries(
     *         org.kuali.kfs.gl.businessobject.OriginEntryInformation, org.kuali.kfs.gl.service.ScrubberReportData)
     */
    @Override
    protected ScrubberProcessTransactionError generateCostShareEntries(
            final OriginEntryInformation scrubbedEntry,
            final ScrubberReportData scrubberReport) {
        // 3000-COST-SHARE to 3100-READ-OFSD in the cobol Generate Cost Share Entries
        LOG.debug("generateCostShareEntries() started");
        try {
            final OriginEntryFull costShareEntry = OriginEntryFull.copyFromOriginEntryable(scrubbedEntry);

            final SystemOptions scrubbedEntryOption = accountingCycleCachingService.getSystemOptions(
                    scrubbedEntry.getUniversityFiscalYear());
            final A21SubAccount scrubbedEntryA21SubAccount = accountingCycleCachingService.getA21SubAccount(
                    scrubbedEntry.getChartOfAccountsCode(), scrubbedEntry.getAccountNumber(),
                    scrubbedEntry.getSubAccountNumber());

            costShareEntry.setFinancialObjectCode(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.GL,
                    SCRUBBER_JOB_COST_SHARE_COMPONENT,
                    TRANSFER_IN_OBJECT_CODE));
            costShareEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            costShareEntry.setFinancialObjectTypeCode(scrubbedEntryOption.getFinancialObjectTypeTransferExpenseCd());
            costShareEntry.setTransactionLedgerEntrySequenceNumber(new Integer(0));

            StringBuffer description = new StringBuffer();
            description.append(costShareDescription);
            description.append(" ").append(scrubbedEntry.getAccountNumber());
            description.append(offsetString);
            costShareEntry.setTransactionLedgerEntryDescription(description.toString());

            costShareEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount);
            if (scrubCostShareAmount.isPositive()) {
                costShareEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            }
            else {
                costShareEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
                costShareEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount.negated());
            }

            costShareEntry.setTransactionDate(runDate);
            costShareEntry.setOrganizationDocumentNumber(null);
            costShareEntry.setProjectCode(KFSConstants.getDashProjectCode());
            costShareEntry.setOrganizationReferenceId(null);
            costShareEntry.setReferenceFinancialDocumentTypeCode(null);
            costShareEntry.setReferenceFinancialSystemOriginationCode(null);
            costShareEntry.setReferenceFinancialDocumentNumber(null);
            costShareEntry.setFinancialDocumentReversalDate(null);
            costShareEntry.setTransactionEncumbranceUpdateCode(null);

            createOutputEntry(costShareEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            OriginEntryFull costShareOffsetEntry = new OriginEntryFull(costShareEntry);
            costShareOffsetEntry.setTransactionLedgerEntryDescription(getOffsetMessage());
            OffsetDefinition offsetDefinition = accountingCycleCachingService.getOffsetDefinition(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    KFSConstants.TRANSFER_FUNDS, scrubbedEntry.getFinancialBalanceTypeCode());
            if (offsetDefinition != null) {
                if (offsetDefinition.getFinancialObject() == null) {
                    final String objectCodeKey = offsetDefinition.getUniversityFiscalYear() + "-" +
                            offsetDefinition.getChartOfAccountsCode() + "-" +
                            offsetDefinition.getFinancialObjectCode();
                    final Message m = new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND) + " (" + objectCodeKey +
                            ")", Message.TYPE_FATAL);
                    LOG.debug("generateCostShareEntries() Error 1 object not found");
                    return new ScrubberProcessTransactionError(costShareEntry, m);
                }

                costShareOffsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());
                costShareOffsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
                costShareOffsetEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            }
            else {
                final String offsetKey = "cost share transfer " + scrubbedEntry.getUniversityFiscalYear() + "-" +
                        scrubbedEntry.getChartOfAccountsCode() + "-TF-" +
                        scrubbedEntry.getFinancialBalanceTypeCode();
                final Message m = new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND) + " (" + offsetKey + ")",
                        Message.TYPE_FATAL);

                LOG.debug("generateCostShareEntries() Error 2 offset not found");
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            costShareOffsetEntry.setFinancialObjectTypeCode(offsetDefinition.getFinancialObject()
                    .getFinancialObjectTypeCode());

            if (costShareEntry.isCredit()) {
                costShareOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            }
            else {
                costShareOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }

            try {
                flexibleOffsetAccountService.updateOffset(costShareOffsetEntry);
            }
            catch (final InvalidFlexibleOffsetException e) {
                final Message m = new Message(e.getMessage(), Message.TYPE_FATAL);
                LOG.debug("generateCostShareEntries() Cost Share Transfer Flexible Offset Error: {}", e::getMessage);
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            createOutputEntry(costShareOffsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            final OriginEntryFull costShareSourceAccountEntry = new OriginEntryFull(costShareEntry);

            description = new StringBuffer();
            description.append(costShareDescription);
            description.append(" ").append(scrubbedEntry.getAccountNumber());
            description.append(offsetString);
            costShareSourceAccountEntry.setTransactionLedgerEntryDescription(description.toString());

            // CU Customization: If Cost Share account is closed, use its continuation account instead.
            final ScrubberProcessTransactionError continuationError = setupEntryWithPotentialContinuation(costShareSourceAccountEntry, scrubbedEntryA21SubAccount);
            if (continuationError != null) {
                return continuationError;
            }

            setCostShareObjectCode(costShareSourceAccountEntry, scrubbedEntry);

            if (StringHelper.isNullOrEmpty(costShareSourceAccountEntry.getSubAccountNumber())) {
                costShareSourceAccountEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            }

            costShareSourceAccountEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            costShareSourceAccountEntry.setFinancialObjectTypeCode(
                    scrubbedEntryOption.getFinancialObjectTypeTransferExpenseCd());
            costShareSourceAccountEntry.setTransactionLedgerEntrySequenceNumber(new Integer(0));

            costShareSourceAccountEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount);
            if (scrubCostShareAmount.isPositive()) {
                costShareSourceAccountEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }
            else {
                costShareSourceAccountEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
                costShareSourceAccountEntry.setTransactionLedgerEntryAmount(scrubCostShareAmount.negated());
            }

            costShareSourceAccountEntry.setTransactionDate(runDate);
            costShareSourceAccountEntry.setOrganizationDocumentNumber(null);
            costShareSourceAccountEntry.setProjectCode(KFSConstants.getDashProjectCode());
            costShareSourceAccountEntry.setOrganizationReferenceId(null);
            costShareSourceAccountEntry.setReferenceFinancialDocumentTypeCode(null);
            costShareSourceAccountEntry.setReferenceFinancialSystemOriginationCode(null);
            costShareSourceAccountEntry.setReferenceFinancialDocumentNumber(null);
            costShareSourceAccountEntry.setFinancialDocumentReversalDate(null);
            costShareSourceAccountEntry.setTransactionEncumbranceUpdateCode(null);

            createOutputEntry(costShareSourceAccountEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            final OriginEntryFull costShareSourceAccountOffsetEntry = new OriginEntryFull(costShareSourceAccountEntry);
            costShareSourceAccountOffsetEntry.setTransactionLedgerEntryDescription(getOffsetMessage());

            // Lookup the new offset definition.
            offsetDefinition = accountingCycleCachingService.getOffsetDefinition(
                    scrubbedEntry.getUniversityFiscalYear(), scrubbedEntry.getChartOfAccountsCode(),
                    KFSConstants.TRANSFER_FUNDS, scrubbedEntry.getFinancialBalanceTypeCode());
            if (offsetDefinition != null) {
                if (offsetDefinition.getFinancialObject() == null) {
                    final String objectCodeKey = costShareEntry.getUniversityFiscalYear() +
                            "-" + scrubbedEntry.getChartOfAccountsCode() +
                            "-" + scrubbedEntry.getFinancialObjectCode();
                    final Message m = new Message(configurationService.getPropertyValueAsString(
                            KFSKeyConstants.ERROR_OFFSET_DEFINITION_OBJECT_CODE_NOT_FOUND) + " (" + objectCodeKey +
                            ")", Message.TYPE_FATAL);

                    LOG.debug("generateCostShareEntries() Error 3 object not found");
                    return new ScrubberProcessTransactionError(costShareSourceAccountEntry, m);
                }

                costShareSourceAccountOffsetEntry.setFinancialObjectCode(offsetDefinition.getFinancialObjectCode());
                costShareSourceAccountOffsetEntry.setFinancialObject(offsetDefinition.getFinancialObject());
                costShareSourceAccountOffsetEntry.setFinancialSubObjectCode(
                        KFSConstants.getDashFinancialSubObjectCode());
            }
            else {
                final String offsetKey = "cost share transfer source " + scrubbedEntry.getUniversityFiscalYear() + "-" +
                        scrubbedEntry.getChartOfAccountsCode() + "-TF-" + scrubbedEntry.getFinancialBalanceTypeCode();
                final Message m = new Message(configurationService.getPropertyValueAsString(
                        KFSKeyConstants.ERROR_OFFSET_DEFINITION_NOT_FOUND) + " (" + offsetKey + ")",
                        Message.TYPE_FATAL);

                LOG.debug("generateCostShareEntries() Error 4 offset not found");
                return new ScrubberProcessTransactionError(costShareSourceAccountEntry, m);
            }

            costShareSourceAccountOffsetEntry.setFinancialObjectTypeCode(offsetDefinition.getFinancialObject()
                    .getFinancialObjectTypeCode());

            if (scrubbedEntry.isCredit()) {
                costShareSourceAccountOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            }
            else {
                costShareSourceAccountOffsetEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            }

            try {
                flexibleOffsetAccountService.updateOffset(costShareSourceAccountOffsetEntry);
            }
            catch (final InvalidFlexibleOffsetException e) {
                final Message m = new Message(e.getMessage(), Message.TYPE_FATAL);
                LOG.debug(
                        "generateCostShareEntries() Cost Share Transfer Account Flexible Offset Error: {}",
                        e::getMessage
                );
                return new ScrubberProcessTransactionError(costShareEntry, m);
            }

            createOutputEntry(costShareSourceAccountOffsetEntry, OUTPUT_GLE_FILE_ps);
            scrubberReport.incrementCostShareEntryGenerated();

            scrubCostShareAmount = KualiDecimal.ZERO;
        } catch (final IOException ioe) {
            LOG.error("generateCostShareEntries() Stopped: {}", ioe::getMessage);
            throw new RuntimeException("generateCostShareEntries() Stopped: " + ioe.getMessage(), ioe);
        }
        LOG.debug("generateCostShareEntries() successful");
        return null;
    }

    /**
     * Helper method for configuring the chart, account and sub-account on the cost-share-source-account entry,
     * using the cost share account's continuation account or descendant (up to a depth of 10) in the
     * event of a closed cost share account.
     * 
     * @param costShareSourceAccountEntry The origin entry to configure.
     * @param scrubbedEntryA21SubAccount The A21 sub-account from the original scrubbed origin entry.
     * @return A ScrubberProcessTransactionError if a valid cost share or continuation account could not be found, null otherwise.
     */
    protected ScrubberProcessTransactionError setupEntryWithPotentialContinuation(OriginEntryFull costShareSourceAccountEntry, A21SubAccount scrubbedEntryA21SubAccount) {
        Account costShareAccount = accountingCycleCachingService.getAccount(
                scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode(), scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber());
        if (ObjectUtils.isNotNull(costShareAccount) && costShareAccount.isClosed()) {
            // Cost share source account is closed; check for a valid continuation account.
            Account continuationAccount = costShareAccount;
            for (int i = 0; i < CONTINUATION_ACCOUNT_DEPTH_LIMIT && ObjectUtils.isNotNull(continuationAccount) && continuationAccount.isClosed(); i++) {
                continuationAccount = accountingCycleCachingService.getAccount(
                        continuationAccount.getContinuationFinChrtOfAcctCd(), continuationAccount.getContinuationAccountNumber());
            }
            if (ObjectUtils.isNull(continuationAccount) || costShareAccount == continuationAccount || continuationAccount.isClosed()) {
                // Could not find a valid Cost Share continuation account; return an error.
                return new ScrubberProcessTransactionError(costShareSourceAccountEntry, new Message(MessageFormat.format(
                        configurationService.getPropertyValueAsString(CUKFSKeyConstants.ERROR_CSACCOUNT_CONTINUATION_ACCOUNT_CLOSED),
                                scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode(), scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber()),
                        Message.TYPE_FATAL));
            } else {
                // Found a valid Cost Share continuation account, so use it.
                LOG.warn(MessageFormat.format(configurationService.getPropertyValueAsString(CUKFSKeyConstants.WARNING_CSACCOUNT_CONTINUATION_ACCOUNT_USED),
                        scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode(), scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber(),
                        continuationAccount.getChartOfAccountsCode(), continuationAccount.getAccountNumber()));
                costShareSourceAccountEntry.setChartOfAccountsCode(continuationAccount.getChartOfAccountsCode());
                costShareSourceAccountEntry.setAccountNumber(continuationAccount.getAccountNumber());
                costShareSourceAccountEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            }
        } else {
            // Cost Share source account is still open, so use it.
            costShareSourceAccountEntry.setChartOfAccountsCode(scrubbedEntryA21SubAccount.getCostShareChartOfAccountCode());
            costShareSourceAccountEntry.setAccountNumber(scrubbedEntryA21SubAccount.getCostShareSourceAccountNumber());
            costShareSourceAccountEntry.setSubAccountNumber(scrubbedEntryA21SubAccount.getCostShareSourceSubAccountNumber());
        }
        
        return null;
    }

    /**
     * Overridden to also restrict plant indebtedness based on document type,
     * by means of a custom DOCUMENT_TYPES parameter.
     * 
     * @see org.kuali.kfs.gl.batch.service.impl.ScrubberProcessImpl#processPlantIndebtedness(
     * org.kuali.kfs.gl.businessobject.OriginEntryInformation, org.kuali.kfs.gl.service.ScrubberReportData)
     */
    @Override
    protected String processPlantIndebtedness(
            final OriginEntryInformation scrubbedEntry, 
            final ScrubberReportData scrubberReport) {
        // Make sure plant indebtedness processing is enabled.
        if (parameterService.getParameterValueAsBoolean(KFSConstants.CoreModuleNamespaces.GL, SCRUBBER_JOB_PLANT_INDEBTEDNESS_COMPONENT, PLANT_FUND_LIABILITY_IND).booleanValue()) {
            // Make sure the entry was from a document that supports plant indebtedness, similar to the logic from the processCapitalization() method.
            final ParameterEvaluator plantIndebtednessDocTypes = getParameterEvaluatorService().getParameterEvaluator(
                    KFSConstants.CoreModuleNamespaces.GL, SCRUBBER_JOB_PLANT_INDEBTEDNESS_COMPONENT,
                    CuGeneralLedgerConstants.CuGlScrubberGroupRules.DOCUMENT_TYPES,
                    scrubbedEntry.getFinancialDocumentTypeCode());
            if (plantIndebtednessDocTypes.evaluationSucceeds()) {
                return super.processPlantIndebtedness(scrubbedEntry, scrubberReport);
            }
        }
        return null;
    }

}
