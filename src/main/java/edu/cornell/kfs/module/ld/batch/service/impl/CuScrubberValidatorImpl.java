package edu.cornell.kfs.module.ld.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.gl.batch.service.AccountingCycleCachingService;
import org.kuali.kfs.gl.businessobject.OriginEntryInformation;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborKeyConstants;
import org.kuali.kfs.module.ld.LaborParameterConstants;
import org.kuali.kfs.module.ld.batch.LaborScrubberStep;
import org.kuali.kfs.module.ld.batch.service.LaborAccountingCycleCachingService;
import org.kuali.kfs.module.ld.batch.service.impl.ScrubberValidatorImpl;
import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.businessobject.LaborOriginEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.service.MessageBuilderService;
import org.kuali.kfs.sys.businessobject.UniversityDate;

import edu.cornell.kfs.module.ld.CuLaborKeyConstants;

public class CuScrubberValidatorImpl extends ScrubberValidatorImpl {

    protected boolean removedSubAccountFromFringeEntry;
    protected MessageBuilderService messageBuilderService;

    /**
     * Overridden to also reset a flag for tracking whether the sub-account was removed
     * from a fringe entry, similar to the superclass's flag for tracking whether
     * the entry was updated with a continuation account.
     * 
     * @see org.kuali.kfs.module.ld.batch.service.impl.ScrubberValidatorImpl#validateTransaction(
     * org.kuali.kfs.gl.businessobject.OriginEntryInformation, org.kuali.kfs.gl.businessobject.OriginEntryInformation,
     * org.kuali.kfs.sys.businessobject.UniversityDate, boolean, org.kuali.kfs.gl.batch.service.AccountingCycleCachingService)
     */
    @Override
    public List<Message> validateTransaction(
            final OriginEntryInformation originEntry, final OriginEntryInformation scrubbedEntry, 
            final UniversityDate universityRunDate, final boolean laborIndicator, 
            final AccountingCycleCachingService accountingCycleCachingService) {
        removedSubAccountFromFringeEntry = false;
        return super.validateTransaction(
                originEntry, scrubbedEntry, universityRunDate, laborIndicator, accountingCycleCachingService);
    }

    /**
     * Overridden to potentially skip the superclass's validation,
     * if the account number was modified on the fringe transaction
     * and the sub-account number was cleared as a result.
     * 
     * @see org.kuali.kfs.module.ld.batch.service.impl.ScrubberValidatorImpl#validateSubAccount(org.kuali.kfs.module.ld.businessobject.LaborOriginEntry, org.kuali.kfs.module.ld.businessobject.LaborOriginEntry, org.kuali.kfs.module.ld.batch.service.LaborAccountingCycleCachingService)
     */
    @Override
    protected Message validateSubAccount(
           final LaborOriginEntry originEntry, final LaborOriginEntry workingEntry, 
           final LaborAccountingCycleCachingService laborAccountingCycleCachingService) {
        if (removedSubAccountFromFringeEntry) {
            return null;
        }
        return super.validateSubAccount(originEntry, workingEntry, laborAccountingCycleCachingService);
    }

    /**
     * Overridden to clear out the sub-account number on fringe transactions
     * if the transaction had its account number modified.
     * 
     * @see org.kuali.kfs.module.ld.batch.service.impl.ScrubberValidatorImpl#checkAccountFringeIndicator(
     * org.kuali.kfs.module.ld.businessobject.LaborOriginEntry, org.kuali.kfs.module.ld.businessobject.LaborOriginEntry,
     * org.kuali.kfs.coa.businessobject.Account, org.kuali.kfs.sys.businessobject.UniversityDate,
     * org.kuali.kfs.module.ld.batch.service.LaborAccountingCycleCachingService)
     */
    @Override
    protected Message checkAccountFringeIndicator(
            final LaborOriginEntry laborOriginEntry, 
            final LaborOriginEntry laborWorkingEntry, final Account account, final UniversityDate universityRunDate, 
            final LaborAccountingCycleCachingService laborAccountingCycleCachingService) {
        final LaborObject laborObject = laborAccountingCycleCachingService.getLaborObject(
                laborOriginEntry.getUniversityFiscalYear(), laborOriginEntry.getChartOfAccountsCode(), laborOriginEntry.getFinancialObjectCode());
        final boolean isFringeTransaction = laborObject != null && StringUtils.equals(
                LaborConstants.BenefitExpenseTransfer.LABOR_LEDGER_BENEFIT_CODE, laborObject.getFinancialObjectFringeOrSalaryCode());

        if (isFringeTransaction && !account.isAccountsFringesBnftIndicator()) {
            final Account altAccount = accountService.getByPrimaryId(
                    laborOriginEntry.getAccount().getReportsToChartOfAccountsCode(), laborOriginEntry.getAccount().getReportsToAccountNumber());
            if (ObjectUtils.isNotNull(altAccount)) {
                laborWorkingEntry.setAccount(altAccount);
                laborWorkingEntry.setAccountNumber(altAccount.getAccountNumber());
                laborWorkingEntry.setChartOfAccountsCode(altAccount.getChartOfAccountsCode());
                Message err = handleExpiredClosedAccount(altAccount, laborOriginEntry, laborWorkingEntry, universityRunDate);
                if (err == null) {
                    err = messageBuilderService.buildMessageWithPlaceHolder(
                            LaborKeyConstants.MESSAGE_FRINGES_MOVED_TO, 
                            Message.TYPE_WARNING, 
                            new Object[]{altAccount.getAccountNumber()});
                }
                clearSubAccountOnModifiedFringeTransaction(
                        laborOriginEntry, laborWorkingEntry, laborAccountingCycleCachingService, err);
                return err;
            }

            final boolean suspenseAccountLogicInd = parameterService.getParameterValueAsBoolean(LaborScrubberStep.class, 
                    LaborParameterConstants.SUSPENSE_ACCOUNT_IND
            );
            if (suspenseAccountLogicInd) {
                return useSuspenseAccount(laborWorkingEntry);
            }

            return messageBuilderService.buildMessage(
                    LaborKeyConstants.ERROR_NON_FRINGE_ACCOUNT_ALTERNATIVE_NOT_FOUND, 
                    Message.TYPE_FATAL);
        }

        return handleExpiredClosedAccount(account, laborOriginEntry, laborWorkingEntry, universityRunDate);
    }

    protected void clearSubAccountOnModifiedFringeTransaction(
            final LaborOriginEntry laborOriginEntry, final LaborOriginEntry laborWorkingEntry,
            final LaborAccountingCycleCachingService laborAccountingCycleCachingService, final Message existingMessage) {
        if (hasSubAccountNumber(laborOriginEntry)) {
            if (existingMessage.getType() == Message.TYPE_WARNING) {
                appendSubAccountWarningToMessage(existingMessage, laborOriginEntry.getSubAccountNumber());
            }
            laborWorkingEntry.setSubAccount(null);
            laborWorkingEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            removedSubAccountFromFringeEntry = true;
        }
    }

    protected boolean hasSubAccountNumber(final LaborOriginEntry laborEntry) {
        return StringUtils.isNotBlank(laborEntry.getSubAccountNumber())
                && !StringUtils.equals(KFSConstants.getDashSubAccountNumber(), laborEntry.getSubAccountNumber());
    }

    protected void appendSubAccountWarningToMessage(
            final Message existingMessage, final String subAccountNumber) {
        String unresolvedMessage = kualiConfigurationService.getPropertyValueAsString(CuLaborKeyConstants.MESSAGE_FRINGE_SUB_ACCOUNT_CLEARED);
        String subAccountMessage = MessageFormat.format(unresolvedMessage, subAccountNumber);
        String combinedMessage = existingMessage.getMessage() + KFSConstants.BLANK_SPACE + subAccountMessage;
        existingMessage.setMessage(combinedMessage);
    }
    
    public void setMessageBuilderService(final MessageBuilderService messageBuilderService) {
        super.setMessageBuilderService(messageBuilderService);
        this.messageBuilderService = messageBuilderService;
    }

}
