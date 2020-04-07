/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ld.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferSourceAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferTargetAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.module.ld.document.service.LaborPendingEntryConverterService;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to help generating pending entries for the given labor documents
 */
public final class LaborPendingEntryGenerator {

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private LaborPendingEntryGenerator() {
    }

    /**
     * generate the expense pending entries based on the given document and accounting line
     *
     * @param document       the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense pending entries
     */
    public static List<LaborLedgerPendingEntry> generateExpensePendingEntries(LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        List<LaborLedgerPendingEntry> expensePendingEntries = new ArrayList<>();
        LaborLedgerPendingEntry expensePendingEntry = SpringContext.getBean(LaborPendingEntryConverterService.class)
                .getExpensePendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expensePendingEntry);

        // KFSMI-6863: always create A2 entries regardless of fiscal period
        LaborLedgerPendingEntry expenseA21PendingEntry = SpringContext.getBean(LaborPendingEntryConverterService.class)
                .getExpenseA21PendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expenseA21PendingEntry);

        LaborLedgerPendingEntry expenseA21ReversalPendingEntry =
                SpringContext.getBean(LaborPendingEntryConverterService.class).getExpenseA21ReversalPendingEntry(
                        document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expenseA21ReversalPendingEntry);

        //refresh nonupdateable references for financial object...
        refreshObjectCodeNonUpdateableReferences(expensePendingEntries);

        return expensePendingEntries;
    }

    /**
     * generate the benefit pending entries based on the given document and accounting line
     *
     * @param document       the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of benefit pending entries
     */
    public static List<LaborLedgerPendingEntry> generateBenefitPendingEntries(LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return null;
        }

        String FringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(FringeOrSalaryCode)) {
            return null;
        }

        Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        String objectCode = accountingLine.getFinancialObjectCode();
        Collection<PositionObjectBenefit> positionObjectBenefits =
                SpringContext.getBean(LaborPositionObjectBenefitService.class).getActivePositionObjectBenefits(
                        payrollFiscalyear, chartOfAccountsCode, objectCode);

        List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<>();
        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            positionObjectBenefit.setLaborBenefitRateCategoryCode(accountingLine.getAccount()
                    .getLaborBenefitRateCategoryCode());
            String fringeBenefitObjectCode = positionObjectBenefit.getBenefitsCalculation()
                    .getPositionFringeBenefitObjectCode();

            KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class)
                    .calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(),
                            accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmount.isNonZero() && positionObjectBenefit.getBenefitsCalculation().isActive()) {

                ParameterService parameterService = SpringContext.getBean(ParameterService.class);
                Boolean enableFringeBenefitCalculationByBenefitRate = parameterService.getParameterValueAsBoolean(
                        KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                        LaborConstants.BenefitCalculation.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_PARAMETER);

                //If fringeBenefitObjectCode is empty and its enable to use calculation by benefit rate
                if (StringUtils.isEmpty(fringeBenefitObjectCode) && enableFringeBenefitCalculationByBenefitRate) {

                    String laborBenefitRateCategoryCode = positionObjectBenefit.getLaborBenefitRateCategoryCode();
                    // Use parameter default if labor benefit rate category code is blank
                    if (StringUtils.isBlank(laborBenefitRateCategoryCode)) {
                        laborBenefitRateCategoryCode = parameterService.getParameterValueAsString(Account.class,
                                LaborConstants.BenefitCalculation.DEFAULT_BENEFIT_RATE_CATEGORY_CODE_PARAMETER);
                    }

                    //create a  map for the search criteria to lookup the fringe benefit percentage
                    Map<String, Object> fieldValues = new HashMap<>();
                    fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                            positionObjectBenefit.getUniversityFiscalYear());
                    fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                            positionObjectBenefit.getChartOfAccountsCode());
                    fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE,
                            positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
                    fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE,
                            laborBenefitRateCategoryCode);
                    BenefitsCalculation bc = SpringContext.getBean(BusinessObjectService.class)
                            .findByPrimaryKey(BenefitsCalculation.class, fieldValues);

                    fringeBenefitObjectCode = bc.getPositionFringeBenefitObjectCode();
                }

                List<LaborLedgerPendingEntry> pendingEntries = generateBenefitPendingEntries(document, accountingLine,
                        sequenceHelper, benefitAmount, fringeBenefitObjectCode);
                benefitPendingEntries.addAll(pendingEntries);
            }
        }

        return benefitPendingEntries;
    }

    /**
     * generate the benefit pending entries with the given benefit amount and finge benefit object code based on the
     * given document and accounting line
     *
     * @param document                the given accounting document
     * @param accountingLine          the given accounting line
     * @param sequenceHelper          the given sequence helper
     * @param benefitAmount           the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit pending entries with the given benefit amount and fringe benefit object code
     */
    public static List<LaborLedgerPendingEntry> generateBenefitPendingEntries(LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            KualiDecimal benefitAmount, String fringeBenefitObjectCode) {
        List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<>();
        LaborLedgerPendingEntry benefitPendingEntry = SpringContext.getBean(LaborPendingEntryConverterService.class)
                .getBenefitPendingEntry(document, accountingLine, sequenceHelper, benefitAmount,
                        fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitPendingEntry);

        // KFSMI-6863: always create A2 entries regardless of fiscal period
        LaborLedgerPendingEntry benefitA21PendingEntry = SpringContext.getBean(LaborPendingEntryConverterService.class)
                .getBenefitA21PendingEntry(document, accountingLine, sequenceHelper, benefitAmount,
                        fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitA21PendingEntry);

        LaborLedgerPendingEntry benefitA21ReversalPendingEntry =
                SpringContext.getBean(LaborPendingEntryConverterService.class).getBenefitA21ReversalPendingEntry(
                        document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitA21ReversalPendingEntry);

        //refresh nonupdateable references for financial object...
        refreshObjectCodeNonUpdateableReferences(benefitPendingEntries);

        return benefitPendingEntries;
    }

    /**
     * generate the benefit clearing pending entries with the given benefit amount and fringe benefit object code
     * based on the given document and accounting line
     *
     * @param document            the given accounting document
     * @param sequenceHelper      the given sequence helper
     * @param accountNumber       the given clearing account number
     * @param chartOfAccountsCode the given clearing chart of accounts code
     * @return a set of benefit clearing pending entries
     */
    public static List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(
            LaborLedgerPostingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            String accountNumber, String chartOfAccountsCode) {
        List<LaborLedgerPendingEntry> benefitClearingPendingEntries = new ArrayList<>();

        Map<String, KualiDecimal> sourceLineBenefitAmountSum = new HashMap<>();
        List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (ExpenseTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            updateBenefitAmountSum(sourceLineBenefitAmountSum, accountingLine);
        }

        Map<String, KualiDecimal> targetLineBenefitAmountSum = new HashMap<>();
        List<ExpenseTransferTargetAccountingLine> targetAccountingLines = document.getTargetAccountingLines();
        for (ExpenseTransferTargetAccountingLine accountingLine : targetAccountingLines) {
            updateBenefitAmountSum(targetLineBenefitAmountSum, accountingLine);
        }

        Set<String> benefitTypeCodes = new HashSet<>(targetLineBenefitAmountSum.keySet());
        benefitTypeCodes.addAll(sourceLineBenefitAmountSum.keySet());

        for (String benefitTypeCode : benefitTypeCodes) {
            KualiDecimal targetAmount = KualiDecimal.ZERO;
            if (targetLineBenefitAmountSum.containsKey(benefitTypeCode)) {
                targetAmount = targetLineBenefitAmountSum.get(benefitTypeCode);
            }

            KualiDecimal sourceAmount = KualiDecimal.ZERO;
            if (sourceLineBenefitAmountSum.containsKey(benefitTypeCode)) {
                sourceAmount = sourceLineBenefitAmountSum.get(benefitTypeCode);
            }

            KualiDecimal clearingAmount = sourceAmount.subtract(targetAmount);
            if (clearingAmount.isNonZero() && ObjectUtils.isNotNull(benefitTypeCode)) {
                benefitClearingPendingEntries.add(SpringContext.getBean(LaborPendingEntryConverterService.class)
                        .getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode,
                                benefitTypeCode, clearingAmount));
            }
        }

        //refresh nonupdateable references for financial object...
        refreshObjectCodeNonUpdateableReferences(benefitClearingPendingEntries);

        return benefitClearingPendingEntries;
    }

    /**
     * update the benefit amount summary map based on the given accounting line
     *
     * @param benefitAmountSumByBenefitType the given benefit amount summary map
     * @param accountingLine                the given accounting line
     */
    protected static void updateBenefitAmountSum(Map<String, KualiDecimal> benefitAmountSumByBenefitType,
            ExpenseTransferAccountingLine accountingLine) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return;
        }

        String FringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(FringeOrSalaryCode)) {
            return;
        }

        Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        String objectCode = accountingLine.getFinancialObjectCode();

        Collection<PositionObjectBenefit> positionObjectBenefits =
                SpringContext.getBean(LaborPositionObjectBenefitService.class).getActivePositionObjectBenefits(
                        payrollFiscalyear, chartOfAccountsCode, objectCode);
        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            positionObjectBenefit.setLaborBenefitRateCategoryCode(accountingLine.getAccount()
                    .getLaborBenefitRateCategoryCode());
            String benefitTypeCode = positionObjectBenefit.getBenefitsCalculation().getPositionBenefitTypeCode();

            KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class)
                    .calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(),
                            accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmountSumByBenefitType.containsKey(benefitTypeCode)) {
                benefitAmount = benefitAmount.add(benefitAmountSumByBenefitType.get(benefitTypeCode));
            }
            benefitAmountSumByBenefitType.put(benefitTypeCode, benefitAmount);
        }
    }

    /**
     * determine if the pay fiscal year and period from the accounting line match with its university fiscal year and
     * period.
     *
     * @param document       the given document
     * @param accountingLine the given accounting line of the document
     * @return true if the pay fiscal year and period from the accounting line match with its university fiscal year
     *         and period; otherwise, false
     */
    protected static boolean isAccountingLinePayFYPeriodMatchesUniversityPayFYPeriod(
            LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine) {
        Integer fiscalYear = document.getPostingYear();
        Integer payFiscalYear = accountingLine.getPayrollEndDateFiscalYear();
        if (!fiscalYear.equals(payFiscalYear)) {
            return false;
        }

        String periodCode = document.getPostingPeriodCode();
        String payPeriodCode = accountingLine.getPayrollEndDateFiscalPeriodCode();
        return StringUtils.equals(periodCode, payPeriodCode);
    }

    /**
     * refreshes labor ledger pending entry's object codes.
     *
     * @param llpes
     */
    public static void refreshObjectCodeNonUpdateableReferences(List<LaborLedgerPendingEntry> llpes) {
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);

        //refresh nonupdateable references for financial object...
        Map<String, String> primaryKeys = new HashMap<>();

        for (LaborLedgerPendingEntry llpe : llpes) {
            primaryKeys.put("financialObjectCode", llpe.getFinancialObjectCode());
            ObjectCode objectCode = bos.findByPrimaryKey(ObjectCode.class, primaryKeys);
            llpe.setFinancialObject(objectCode);
        }
    }
}
