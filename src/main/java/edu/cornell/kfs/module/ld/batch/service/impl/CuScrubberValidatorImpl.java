package edu.cornell.kfs.module.ld.batch.service.impl;

import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborKeyConstants;
import org.kuali.kfs.module.ld.LaborParameterConstants;
import org.kuali.kfs.module.ld.batch.LaborScrubberStep;
import org.kuali.kfs.module.ld.batch.service.impl.ScrubberValidatorImpl;
import org.kuali.kfs.module.ld.batch.service.LaborAccountingCycleCachingService;
import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.businessobject.LaborOriginEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.service.MessageBuilderService;
import org.kuali.kfs.sys.businessobject.UniversityDate;

import edu.cornell.kfs.module.ld.CuLaborKeyConstants;

public class CuScrubberValidatorImpl extends ScrubberValidatorImpl {
    
    protected MessageBuilderService messageBuilderService;

    /**
     * Cornell Customization: 
     * KualiCo 2023-04-19 version of the method with Cornell customization to append the removed sub-account to the warning message.
     * 
     * For fringe transaction types checks if the account accepts fringe benefits. If not, retrieves the alternative
     * account, then calls expiration checking on either the alternative account or the account passed in.
     */
    @Override
    protected Message checkAccountFringeIndicator(
            final LaborOriginEntry laborOriginEntry,
            final LaborOriginEntry laborWorkingEntry, final Account account, final UniversityDate universityRunDate,
            final LaborAccountingCycleCachingService laborAccountingCycleCachingService) {
        // check for fringe transaction type
        final LaborObject laborObject = laborAccountingCycleCachingService.getLaborObject(
                laborOriginEntry.getUniversityFiscalYear(), laborOriginEntry.getChartOfAccountsCode(),
                laborOriginEntry.getFinancialObjectCode());
        final boolean isFringeTransaction = laborObject != null
                                            && StringUtils.equals(LaborConstants.BenefitExpenseTransfer.LABOR_LEDGER_BENEFIT_CODE,
                    laborObject.getFinancialObjectFringeOrSalaryCode());

        // alternative account handling for non fringe accounts
        if (isFringeTransaction && !account.isAccountsFringesBnftIndicator()) {
            final Account altAccount = accountService.getByPrimaryId(
                    laborOriginEntry.getAccount().getReportsToChartOfAccountsCode(),
                    laborOriginEntry.getAccount().getReportsToAccountNumber());
            if (ObjectUtils.isNotNull(altAccount)) {
                laborWorkingEntry.setAccount(altAccount);
                laborWorkingEntry.setAccountNumber(altAccount.getAccountNumber());
                laborWorkingEntry.setSubAccount(null);
                laborWorkingEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
                laborWorkingEntry.setChartOfAccountsCode(altAccount.getChartOfAccountsCode());
                Message err = handleExpiredClosedAccount(altAccount, laborOriginEntry, laborWorkingEntry,
                        universityRunDate);
                if (err == null) {
                    err = messageBuilderService.buildMessageWithPlaceHolder(
                            LaborKeyConstants.MESSAGE_FRINGES_MOVED_TO,
                            Message.TYPE_WARNING,
                            laborWorkingEntry.getChartOfAccountsCode(),
                            laborWorkingEntry.getAccountNumber(),
                            laborWorkingEntry.getSubAccountNumber()
                    );
                }
                /* Cornell customization - start */
                clearSubAccountOnModifiedFringeTransaction(
                        laborOriginEntry, laborWorkingEntry, laborAccountingCycleCachingService, err);
                /* Cornell customization - end */
                return err;
            }

            // no alt acct, use suspense acct if active
            final boolean suspenseAccountLogicInd = parameterService.getParameterValueAsBoolean(LaborScrubberStep.class,
                    LaborParameterConstants.SUSPENSE_ACCOUNT_IND
            );
            if (suspenseAccountLogicInd) {
                return useSuspenseAccount(laborWorkingEntry);
            }

            return messageBuilderService.buildMessage(
                    LaborKeyConstants.ERROR_NON_FRINGE_ACCOUNT_ALTERNATIVE_NOT_FOUND,
                    Message.TYPE_FATAL
            );
        }

        return handleExpiredClosedAccount(account, laborOriginEntry, laborWorkingEntry, universityRunDate);
    }
    
    /**
     * Cornell Customization to support checkAccountFringeIndicator base code customization.
     */
    protected void clearSubAccountOnModifiedFringeTransaction(
            final LaborOriginEntry laborOriginEntry, final LaborOriginEntry laborWorkingEntry,
            final LaborAccountingCycleCachingService laborAccountingCycleCachingService, final Message existingMessage) {
        if (hasSubAccountNumber(laborOriginEntry)) {
            if (existingMessage.getType() == Message.TYPE_WARNING) {
                appendSubAccountWarningToMessage(existingMessage, laborOriginEntry.getSubAccountNumber());
            }
            laborWorkingEntry.setSubAccount(null);
            laborWorkingEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        }
    }

    /**
     * Cornell Customization to support checkAccountFringeIndicator base code customization.
     */
    protected boolean hasSubAccountNumber(final LaborOriginEntry laborEntry) {
        return StringUtils.isNotBlank(laborEntry.getSubAccountNumber())
                && !StringUtils.equals(KFSConstants.getDashSubAccountNumber(), laborEntry.getSubAccountNumber());
    }
    
    /**
     * Cornell Customization to support checkAccountFringeIndicator base code customization.
     */
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
