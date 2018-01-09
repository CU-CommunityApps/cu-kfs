package edu.cornell.kfs.fp.document;

import org.kuali.kfs.krad.document.Copyable;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentBase;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.Correctable;
import org.kuali.kfs.sys.document.service.AccountingDocumentRuleHelperService;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.kfs.sys.service.OptionsService;

import static org.kuali.kfs.sys.KFSConstants.BALANCE_TYPE_ACTUAL;

public class AccountFundsUpdateDocument extends AccountingDocumentBase implements Copyable, Correctable, AmountTotaling {

    public AccountFundsUpdateDocument() {
        super();
    }

    public String getSourceAccountingLinesSectionTitle() {
        return KFSConstants.FROM;
    }

    public String getTargetAccountingLinesSectionTitle() {
        return KFSConstants.TO;
    }

/*    @Override
    public boolean customizeOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail accountingLine, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
        offsetEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_ACTUAL);
        return true;
    }
*/

/*
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail generalLedgerPendingEntrySourceDetail, GeneralLedgerPendingEntry explicitEntry) {
        AccountingLine accountingLine = (AccountingLine) generalLedgerPendingEntrySourceDetail;
        SystemOptions options = SpringContext.getBean(OptionsService.class).getCurrentYearOptions();

        explicitEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_ACTUAL);
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (isDebitUtils.isExpense(accountingLine)) {
            explicitEntry.setFinancialObjectTypeCode(options.getFinancialObjectTypeTransferExpenseCd());
        } else {
            if (isDebitUtils.isIncome(accountingLine)) {
                explicitEntry.setFinancialObjectTypeCode(options.getFinancialObjectTypeTransferIncomeCd());
            } else {
                AccountingDocumentRuleHelperService accountingDocumentRuleUtil = SpringContext.getBean(AccountingDocumentRuleHelperService.class);
                explicitEntry.setFinancialObjectTypeCode(accountingDocumentRuleUtil.getObjectCodeTypeCodeWithoutSideEffects(accountingLine));
            }
        }
    }
*/
    /**
     * Adds the following restrictions in addition to those provided by <code>IsDebitUtils.isDebitConsideringNothingPositiveOnly</code>
     * <ol>
     * <li> Only allow income or expense object type codes
     * <li> Target lines have the opposite debit/credit codes as the source lines
     * </ol>
     *
     * @param financialDocument The document used to determine if the accounting line is a debit line.
     * @param accountingLine    The accounting line to be analyzed.
     * @return True if the accounting line provided is a debit line, false otherwise.
     * @see IsDebitUtils#isDebitConsideringNothingPositiveOnly(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)
     * @see org.kuali.rice.krad.rule.AccountingLineRule#isDebit(org.kuali.rice.krad.document.FinancialDocument,
     * org.kuali.rice.krad.bo.AccountingLine)
     */
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLine accountingLine = (AccountingLine) postable;
        // only allow income or expense
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if (!isDebitUtils.isIncome(accountingLine) && !isDebitUtils.isExpense(accountingLine)) {
            throw new IllegalStateException(isDebitUtils.getDebitCalculationIllegalStateExceptionMessage());
        }
        boolean isDebit = false;
        if (accountingLine.isSourceAccountingLine()) {
            isDebit = isDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingLine);
        } else if (accountingLine.isTargetAccountingLine()) {
            isDebit = !isDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingLine);
        } else {
            throw new IllegalStateException(isDebitUtils.getInvalidLineTypeIllegalArgumentExceptionMessage());
        }

        return isDebit;
    }
}
