package edu.cornell.kfs.fp.document.validation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentTargetAccountingLine;
import org.kuali.kfs.fp.document.validation.impl.BudgetAdjustmentDocumentBalancedValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuBudgetAdjustmentDocumentBalancedValidation extends BudgetAdjustmentDocumentBalancedValidation {

    static final String INCOME_STREAM_CHART_ACCOUNT_DELIMITER = "|";
    
    @Override
    public boolean validate(final AttributedDocumentEvent event) {
        boolean balanced = true;

        // check base amounts are equal
        //KFSMI-3036
        final KualiInteger sourceBaseBudgetTotal = getAccountingDocumentForValidation().getSourceBaseBudgetIncomeTotal()
                .subtract(getAccountingDocumentForValidation().getSourceBaseBudgetExpenseTotal());
        final KualiInteger targetBaseBudgetTotal = getAccountingDocumentForValidation().getTargetBaseBudgetIncomeTotal()
                .subtract(getAccountingDocumentForValidation().getTargetBaseBudgetExpenseTotal());
        if (sourceBaseBudgetTotal.compareTo(targetBaseBudgetTotal) != 0) {
            GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS,
                    FPKeyConstants.ERROR_DOCUMENT_BA_BASE_AMOUNTS_BALANCED);
            balanced = false;
        }
        // check document is balanced within the accounts
        Map accountsMap = buildAccountBalanceMapForDocumentBalance(true);

        for (final KualiDecimal accountAmount : (Collection<KualiDecimal>)accountsMap.values()) {
            if (accountAmount.isNonZero()) {
                GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, CUKFSKeyConstants.ERROR_DOCUMENT_BA_ACCOUNT_BASE_AMOUNTS_BALANCED);
                balanced = false;
                break;
            }
        }

        // check current amounts balance, income stream balance Map should add to 0
        final Map incomeStreamMap = getAccountingDocumentForValidation().buildIncomeStreamBalanceMapForDocumentBalance();
        KualiDecimal totalCurrentAmount = new KualiDecimal(0);
        for (final Object o : incomeStreamMap.values()) {
            KualiDecimal streamAmount = (KualiDecimal) o;
            totalCurrentAmount = totalCurrentAmount.add(streamAmount);
        }

        if (totalCurrentAmount.isNonZero()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, FPKeyConstants.ERROR_DOCUMENT_BA_CURRENT_AMOUNTS_BALANCED);
            balanced = false;
        }
        // check document is balanced within the accounts
        accountsMap = buildAccountBalanceMapForDocumentBalance(false);

        for (final KualiDecimal accountAmount : (Collection<KualiDecimal>)accountsMap.values()) {
            if (accountAmount.isNonZero()) {
                GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, CUKFSKeyConstants.ERROR_DOCUMENT_BA_ACCOUNT_AMOUNTS_BALANCED);
                balanced = false;
                break;
            }
        }

        return balanced;
    }

    /**
     * Builds a map of accounts and their balance.
     * 
     * @return a map of accounts and their balance.
     */
    public Map buildAccountBalanceMapForDocumentBalance(final boolean isBaseAmount) {
        final Map<String, KualiDecimal> accountBalance = new HashMap<String, KualiDecimal>();

        final List<BudgetAdjustmentAccountingLine> accountingLines = new ArrayList<BudgetAdjustmentAccountingLine>();
        accountingLines.addAll(getAccountingDocumentForValidation().getSourceAccountingLines());
        accountingLines.addAll(getAccountingDocumentForValidation().getTargetAccountingLines());
        for (final BudgetAdjustmentAccountingLine budgetAccountingLine : accountingLines) {

            final String accountKey = budgetAccountingLine.getAccount().getChartOfAccountsCode() + INCOME_STREAM_CHART_ACCOUNT_DELIMITER + budgetAccountingLine.getAccount().getAccountNumber();

            // place record in balance map
            accountBalance.put(accountKey, getAccountAmount(budgetAccountingLine, accountBalance.get(accountKey), isBaseAmount));
        }

        return accountBalance;
    }

    /**
     * Computes the total balance within an account.
     * 
     * @param budgetAccountingLine
     * @param accountAmount
     * @return the total balance within an account
     */
    protected KualiDecimal getAccountAmount(final BudgetAdjustmentAccountingLine budgetAccountingLine, KualiDecimal accountAmount, final boolean isBaseAmount) {
        if (accountAmount == null) {
            accountAmount = new KualiDecimal(0);
        }

        // amounts need to be reversed for source expense lines and target income lines
        final DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if ((budgetAccountingLine instanceof BudgetAdjustmentSourceAccountingLine && isDebitUtils.isExpense((AccountingLine) budgetAccountingLine)) || (budgetAccountingLine instanceof BudgetAdjustmentTargetAccountingLine && isDebitUtils.isIncome((AccountingLine) budgetAccountingLine))) {
            if (isBaseAmount) {
                accountAmount = accountAmount.subtract(new KualiDecimal(budgetAccountingLine.getBaseBudgetAdjustmentAmount().intValue()));
            } else {
                accountAmount = accountAmount.subtract(budgetAccountingLine.getCurrentBudgetAdjustmentAmount());
            }
        } else {
            if (isBaseAmount) {
                accountAmount = accountAmount.add(new KualiDecimal(budgetAccountingLine.getBaseBudgetAdjustmentAmount().intValue()));
            } else {
                accountAmount = accountAmount.add(budgetAccountingLine.getCurrentBudgetAdjustmentAmount());
            }
        }

        return accountAmount;
    }
 

}
