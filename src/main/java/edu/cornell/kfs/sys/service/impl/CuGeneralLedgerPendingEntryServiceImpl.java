package edu.cornell.kfs.sys.service.impl;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.kfs.sys.service.impl.GeneralLedgerPendingEntryServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.service.CuGeneralLedgerPendingEntryService;

public class CuGeneralLedgerPendingEntryServiceImpl extends GeneralLedgerPendingEntryServiceImpl
        implements CuGeneralLedgerPendingEntryService {
    private static final Logger LOG = LogManager.getLogger(CuGeneralLedgerPendingEntryServiceImpl.class);

    /*
     * With the 11/12/2020 patch, this function was removed from GeneralLedgerPendingEntryServiceImpl, 
     * this function is a copy of the code that was removed
     */
    @Override
    public GeneralLedgerPendingEntry buildGeneralLedgerPendingEntry(GeneralLedgerPostingDocument document,
            Account account, ObjectCode objectCode, String subAccountNumber, String subObjectCode,
            String organizationReferenceId, String projectCode, String referenceNumber, String referenceTypeCode,
            String referenceOriginCode, String description, boolean isDebit, KualiDecimal amount,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populateExplicitGeneralLedgerPendingEntry(AccountingDocument, AccountingLine,"
                    + " GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry)  start");
        }

        GeneralLedgerPendingEntry explicitEntry = new GeneralLedgerPendingEntry();
        explicitEntry
                .setFinancialDocumentTypeCode(document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
        explicitEntry.setVersionNumber(1L);
        explicitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
        Timestamp transactionTimestamp = new Timestamp(dateTimeService.getCurrentDate().getTime());
        explicitEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        explicitEntry.setTransactionEntryProcessedTs(transactionTimestamp);
        explicitEntry.setAccountNumber(account.getAccountNumber());
        if (account.getAccountSufficientFundsCode() == null) {
            account.setAccountSufficientFundsCode(KFSConstants.SF_TYPE_NO_CHECKING);
        }
        // FIXME! inject the sufficient funds service
        explicitEntry.setAcctSufficientFundsFinObjCd(getSufficientFundsService()
                .getSufficientFundsObjectCode(objectCode, account.getAccountSufficientFundsCode()));
        explicitEntry.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.NOT_PROCESSED);
        explicitEntry.setTransactionEncumbranceUpdateCode(KFSConstants.BLANK_SPACE);
        // this is the default that most documents use
        explicitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        explicitEntry.setChartOfAccountsCode(account.getChartOfAccountsCode());
        explicitEntry.setTransactionDebitCreditCode(isDebit ? KFSConstants.GL_DEBIT_CODE : KFSConstants.GL_CREDIT_CODE);
        // TODO: this value never changes the result could be cached
        explicitEntry.setFinancialSystemOriginationCode(
                homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode());
        explicitEntry.setDocumentNumber(document.getDocumentNumber());
        explicitEntry.setFinancialObjectCode(objectCode.getFinancialObjectCode());
        explicitEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        explicitEntry.setOrganizationDocumentNumber(document.getDocumentHeader().getOrganizationDocumentNumber());
        explicitEntry.setOrganizationReferenceId(organizationReferenceId);
        explicitEntry
                .setProjectCode(getEntryValue(projectCode, GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode()));
        explicitEntry.setReferenceFinancialDocumentNumber(getEntryValue(referenceNumber, KFSConstants.BLANK_SPACE));
        explicitEntry.setReferenceFinancialDocumentTypeCode(getEntryValue(referenceTypeCode, KFSConstants.BLANK_SPACE));
        explicitEntry.setReferenceFinancialSystemOriginationCode(
                getEntryValue(referenceOriginCode, KFSConstants.BLANK_SPACE));
        explicitEntry.setSubAccountNumber(
                getEntryValue(subAccountNumber, GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber()));
        explicitEntry.setFinancialSubObjectCode(
                getEntryValue(subObjectCode, GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode()));
        explicitEntry.setTransactionEntryOffsetIndicator(false);
        explicitEntry.setTransactionLedgerEntryAmount(amount);
        explicitEntry.setTransactionLedgerEntryDescription(
                getEntryValue(description, document.getDocumentHeader().getDocumentDescription()));
        explicitEntry.setUniversityFiscalPeriodCode(
                determineFiscalPeriodCode((GeneralLedgerPendingEntrySource) document));
        explicitEntry.setUniversityFiscalYear(document.getPostingYear());

        if (LOG.isDebugEnabled()) {
            LOG.debug("populateExplicitGeneralLedgerPendingEntry(AccountingDocument, AccountingLine, "
                    + "GeneralLedgerPendingEntrySequenceHelper, GeneralLedgerPendingEntry)  end");
        }

        return explicitEntry;
    }

}
