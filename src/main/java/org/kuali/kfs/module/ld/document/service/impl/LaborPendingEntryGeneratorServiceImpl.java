/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2025 Kuali, Inc.
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
package org.kuali.kfs.module.ld.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
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
import org.kuali.kfs.module.ld.document.service.LaborPendingEntryGeneratorService;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

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
// CU customization: backport FINP-12883
public class LaborPendingEntryGeneratorServiceImpl implements LaborPendingEntryGeneratorService {

    private final LaborPendingEntryConverterService laborPendingEntryConverterService;
    private final ParameterService parameterService;
    private final LaborPositionObjectBenefitService laborPositionObjectBenefitService;
    //access increased for Stevens
    protected final LaborBenefitsCalculationService laborBenefitsCalculationService;
    private final BusinessObjectService businessObjectService;

    public LaborPendingEntryGeneratorServiceImpl(
            final LaborPendingEntryConverterService laborPendingEntryConverterService,
            final ParameterService parameterService,
            final LaborPositionObjectBenefitService laborPositionObjectBenefitService,
            final LaborBenefitsCalculationService laborBenefitsCalculationService,
            final BusinessObjectService businessObjectService
    ) {
        this.laborPendingEntryConverterService = laborPendingEntryConverterService;
        this.parameterService = parameterService;
        this.laborPositionObjectBenefitService = laborPositionObjectBenefitService;
        this.laborBenefitsCalculationService = laborBenefitsCalculationService;
        this.businessObjectService = businessObjectService;
    }

    @Override
    public List<LaborLedgerPendingEntry> generateExpensePendingEntries(
            final LaborLedgerPostingDocument document,
            final ExpenseTransferAccountingLine accountingLine, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        final List<LaborLedgerPendingEntry> expensePendingEntries = new ArrayList<>();
        final LaborLedgerPendingEntry expensePendingEntry = laborPendingEntryConverterService
                .getExpensePendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expensePendingEntry);

        refreshObjectCodeNonUpdateableReferences(expensePendingEntries);

        return expensePendingEntries;
    }

    @Override
    public List<LaborLedgerPendingEntry> generateBenefitPendingEntries(
            final LaborLedgerPostingDocument document,
            final ExpenseTransferAccountingLine accountingLine, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return null;
        }

        final String FringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(FringeOrSalaryCode)) {
            return null;
        }

        final Integer payrollFiscalYear = accountingLine.getPayrollEndDateFiscalYear();
        final String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        final String objectCode = accountingLine.getFinancialObjectCode();
        String laborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();

        final Collection<PositionObjectBenefit> positionObjectBenefits =
                laborPositionObjectBenefitService.getActivePositionObjectBenefits(
                        payrollFiscalYear, chartOfAccountsCode, objectCode);

        final List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<>();
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {

            final BenefitsCalculation benefitsCalculation =
                    laborBenefitsCalculationService.getBenefitsCalculation(positionObjectBenefit);

            if (ObjectUtils.isNull(benefitsCalculation)) {
                continue;
            }
            String fringeBenefitObjectCode = benefitsCalculation.getPositionFringeBenefitObjectCode();

            final KualiDecimal benefitAmount = laborBenefitsCalculationService
                    .calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(),
                            accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmount.isNonZero() && benefitsCalculation.isActive()) {

                final Boolean enableFringeBenefitCalculationByBenefitRate = parameterService.getParameterValueAsBoolean(
                        KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                        LaborConstants.BenefitCalculation.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_PARAMETER);

                //If fringeBenefitObjectCode is empty and its enable to use calculation by benefit rate
                if (StringUtils.isEmpty(fringeBenefitObjectCode) && enableFringeBenefitCalculationByBenefitRate) {

                    // Use parameter default if labor benefit rate category code is blank
                    if (StringUtils.isBlank(laborBenefitRateCategoryCode)) {
                        laborBenefitRateCategoryCode = parameterService.getParameterValueAsString(Account.class,
                                COAParameterConstants.BENEFIT_RATE);
                    }

                    //create a  map for the search criteria to lookup the fringe benefit percentage
                    final Map<String, Object> fieldValues = new HashMap<>();
                    fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR,
                            positionObjectBenefit.getUniversityFiscalYear());
                    fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
                            positionObjectBenefit.getChartOfAccountsCode());
                    fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE,
                            positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
                    fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE,
                            laborBenefitRateCategoryCode);
                    final BenefitsCalculation bc = businessObjectService
                            .findByPrimaryKey(BenefitsCalculation.class, fieldValues);

                    fringeBenefitObjectCode = bc.getPositionFringeBenefitObjectCode();
                }

                final List<LaborLedgerPendingEntry> pendingEntries = generateBenefitPendingEntries(document, accountingLine,
                        sequenceHelper, benefitAmount, fringeBenefitObjectCode);
                benefitPendingEntries.addAll(pendingEntries);
            }
        }

        return benefitPendingEntries;
    }

    @Override
    public List<LaborLedgerPendingEntry> generateBenefitPendingEntries(
            final LaborLedgerPostingDocument document,
            final ExpenseTransferAccountingLine accountingLine, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            final KualiDecimal benefitAmount, final String fringeBenefitObjectCode) {
        final List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<>();
        final LaborLedgerPendingEntry benefitPendingEntry = laborPendingEntryConverterService
                .getBenefitPendingEntry(document, accountingLine, sequenceHelper, benefitAmount,
                        fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitPendingEntry);

        refreshObjectCodeNonUpdateableReferences(benefitPendingEntries);

        return benefitPendingEntries;
    }

    @Override
    public List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(
            final LaborLedgerPostingDocument document, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            final String accountNumber, final String chartOfAccountsCode) {
        final List<LaborLedgerPendingEntry> benefitClearingPendingEntries = new ArrayList<>();

        final Map<String, KualiDecimal> sourceLineBenefitAmountSum = new HashMap<>();
        final List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (final ExpenseTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            updateBenefitAmountSum(sourceLineBenefitAmountSum, accountingLine);
        }

        final Map<String, KualiDecimal> targetLineBenefitAmountSum = new HashMap<>();
        final List<ExpenseTransferTargetAccountingLine> targetAccountingLines = document.getTargetAccountingLines();
        for (final ExpenseTransferTargetAccountingLine accountingLine : targetAccountingLines) {
            updateBenefitAmountSum(targetLineBenefitAmountSum, accountingLine);
        }

        final Set<String> benefitObjectCodes = new HashSet<>(targetLineBenefitAmountSum.keySet());
        benefitObjectCodes.addAll(sourceLineBenefitAmountSum.keySet());

        for (final String benefitObjectCode : benefitObjectCodes) {
            KualiDecimal targetAmount = KualiDecimal.ZERO;
            if (targetLineBenefitAmountSum.containsKey(benefitObjectCode)) {
                targetAmount = targetLineBenefitAmountSum.get(benefitObjectCode);
            }

            KualiDecimal sourceAmount = KualiDecimal.ZERO;
            if (sourceLineBenefitAmountSum.containsKey(benefitObjectCode)) {
                sourceAmount = sourceLineBenefitAmountSum.get(benefitObjectCode);
            }

            final KualiDecimal clearingAmount = sourceAmount.subtract(targetAmount);
            if (clearingAmount.isNonZero() && ObjectUtils.isNotNull(benefitObjectCode)) {
                benefitClearingPendingEntries.add(laborPendingEntryConverterService
                        .getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode,
                                benefitObjectCode, clearingAmount));
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
    protected void updateBenefitAmountSum(
            final Map<String, KualiDecimal> benefitAmountSumByBenefitType,
            final ExpenseTransferAccountingLine accountingLine) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return;
        }

        final String FringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(FringeOrSalaryCode)) {
            return;
        }

        final Integer payrollFiscalYear = accountingLine.getPayrollEndDateFiscalYear();
        final String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        final String objectCode = accountingLine.getFinancialObjectCode();
        final String laborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();

        final Collection<PositionObjectBenefit> positionObjectBenefits =
                laborPositionObjectBenefitService.getActivePositionObjectBenefits(
                        payrollFiscalYear, chartOfAccountsCode, objectCode);
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {

            final BenefitsCalculation benefitsCalculation =
                    laborBenefitsCalculationService.getBenefitsCalculation(
                            payrollFiscalYear,
                            accountingLine.getChartOfAccountsCode(),
                            positionObjectBenefit.getFinancialObjectBenefitsTypeCode(),
                            laborBenefitRateCategoryCode
                    );
            if (ObjectUtils.isNull(benefitsCalculation)) {
                continue;
            }
            final String benefitObjectCode =
                    benefitsCalculation.getPositionFringeBenefitObjectCode();

            KualiDecimal benefitAmount = laborBenefitsCalculationService
                    .calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(),
                            accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmountSumByBenefitType.containsKey(benefitObjectCode)) {
                benefitAmount = benefitAmount.add(benefitAmountSumByBenefitType.get(benefitObjectCode));
            }
            benefitAmountSumByBenefitType.put(benefitObjectCode, benefitAmount);
        }
    }

    /**
     * refreshes labor ledger pending entry's object codes.
     *
     * @param llpes list of LaborLedgerPendingEntries to refresh object codes for
     */
    protected void refreshObjectCodeNonUpdateableReferences(final List<LaborLedgerPendingEntry> llpes) {
        //refresh nonupdateable references for financial object...
        final Map<String, String> primaryKeys = new HashMap<>();

        for (final LaborLedgerPendingEntry llpe : llpes) {
            primaryKeys.put("financialObjectCode", llpe.getFinancialObjectCode());
            final ObjectCode objectCode = businessObjectService.findByPrimaryKey(ObjectCode.class, primaryKeys);
            llpe.setFinancialObject(objectCode);
        }
    }
}
