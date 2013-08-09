/*
 * Copyright 2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.document.validation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentTargetAccountingLine;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

/**
 * A validation which checks if a Budget Adjustment document is balanced before heading to routing
 */
public class BudgetAdjustmentDocumentBalancedValidation extends GenericValidation {
    public BudgetAdjustmentDocument accountingDocumentForValidation;

    /**
     * Validates that the budget adjustment document is balanced, based on whether the source base amount equals the target base amount
     * and that the income stream balance map has no non-zero values.
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        MessageMap errors = GlobalVariables.getMessageMap();

        boolean balanced = true;

        // check base amounts are equal
        //KFSMI-3036
        KualiInteger sourceBaseBudgetTotal = getAccountingDocumentForValidation().getSourceBaseBudgetIncomeTotal().subtract( getAccountingDocumentForValidation().getSourceBaseBudgetExpenseTotal());
        KualiInteger targetBaseBudgetTotal = getAccountingDocumentForValidation().getTargetBaseBudgetIncomeTotal().subtract( getAccountingDocumentForValidation().getTargetBaseBudgetExpenseTotal());
        if (sourceBaseBudgetTotal.compareTo(targetBaseBudgetTotal) != 0) {
            GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, KFSKeyConstants.ERROR_DOCUMENT_BA_BASE_AMOUNTS_BALANCED);
            balanced = false;
        }

        // check current amounts balance, income stream balance Map should add to 0
        Map incomeStreamMap = getAccountingDocumentForValidation().buildIncomeStreamBalanceMapForDocumentBalance();
        KualiDecimal totalCurrentAmount = new KualiDecimal(0);
        for (Iterator iter = incomeStreamMap.values().iterator(); iter.hasNext();) {
            KualiDecimal streamAmount = (KualiDecimal) iter.next();
            totalCurrentAmount = totalCurrentAmount.add(streamAmount);
        }

        if (totalCurrentAmount.isNonZero()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, KFSKeyConstants.ERROR_DOCUMENT_BA_CURRENT_AMOUNTS_BALANCED);
            balanced = false;
        }
        
        // check document is balanced within the accounts
        Map accountsMap = buildAccountBalanceMapForDocumentBalance();

        for (Iterator iter = accountsMap.values().iterator(); iter.hasNext();) {
            KualiDecimal accountAmount = (KualiDecimal) iter.next();
            if (accountAmount.isNonZero()) {
                GlobalVariables.getMessageMap().putError(KFSConstants.ACCOUNTING_LINE_ERRORS, KFSKeyConstants.ERROR_DOCUMENT_BA_ACCOUNT_AMOUNTS_BALANCED);
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
    public Map buildAccountBalanceMapForDocumentBalance() {
        Map<String, KualiDecimal> accountBalance = new HashMap<String, KualiDecimal>();

        List<BudgetAdjustmentAccountingLine> accountingLines = new ArrayList<BudgetAdjustmentAccountingLine>();
        accountingLines.addAll(getAccountingDocumentForValidation().getSourceAccountingLines());
        accountingLines.addAll(getAccountingDocumentForValidation().getTargetAccountingLines());
        for (BudgetAdjustmentAccountingLine budgetAccountingLine : accountingLines) {

            String accountKey = budgetAccountingLine.getAccount().getChartOfAccountsCode() + BudgetAdjustmentDocumentRuleConstants.INCOME_STREAM_CHART_ACCOUNT_DELIMITER + budgetAccountingLine.getAccount().getAccountNumber();

            // place record in balance map
            accountBalance.put(accountKey, getAccountAmount(budgetAccountingLine, accountBalance.get(accountKey)));
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
    protected KualiDecimal getAccountAmount(BudgetAdjustmentAccountingLine budgetAccountingLine, KualiDecimal accountAmount) {
        if (accountAmount == null) {
            accountAmount = new KualiDecimal(0);
        }

        // amounts need to be reversed for source expense lines and target income lines
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        if ((budgetAccountingLine instanceof BudgetAdjustmentSourceAccountingLine && isDebitUtils.isExpense((AccountingLine) budgetAccountingLine)) || (budgetAccountingLine instanceof BudgetAdjustmentTargetAccountingLine && isDebitUtils.isIncome((AccountingLine) budgetAccountingLine))) {
            accountAmount = accountAmount.subtract(budgetAccountingLine.getCurrentBudgetAdjustmentAmount());
        } else {
            accountAmount = accountAmount.add(budgetAccountingLine.getCurrentBudgetAdjustmentAmount());
        }

        return accountAmount;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public BudgetAdjustmentDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(BudgetAdjustmentDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}
