/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.fp.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.fp.document.service.BudgetAdjustmentLaborBenefitsService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This service contains methods related to the generation of labor benefit accounting lines for the budget adjustment
 * document.
 */
// CU customization: backport FINP-12883
public class BudgetAdjustmentLaborBenefitsServiceImpl implements BudgetAdjustmentLaborBenefitsService {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    // loosened for UConn
    protected LaborBenefitsCalculationService laborBenefitsCalculationService;
    private ParameterService parameterService;

    /**
     * This method generated labor benefit accounting lines to be added to the BudgetDocument provided.
     *
     * @param budgetDocument The BudgetDocument to have the new labor benefit accounting lines added to.
     */
    @Override
    public void generateLaborBenefitsAccountingLines(final BudgetAdjustmentDocument budgetDocument) {
        final Integer fiscalYear = budgetDocument.getPostingYear();

        final List accountingLines = new ArrayList();
        accountingLines.addAll(budgetDocument.getSourceAccountingLines());
        accountingLines.addAll(budgetDocument.getTargetAccountingLines());

        /*
         * find lines that have labor object codes, then retrieve the benefit calculation records for the object code.
         * Finally, for each benefit record, create an accounting line with properties set from the original line,
         * but substituted with the benefit object code and calculated current and base amount.
         */
        for (final Object accountingLine : accountingLines) {
            final BudgetAdjustmentAccountingLine line = (BudgetAdjustmentAccountingLine) accountingLine;

            // check if the line was previously generated benefit line, if so delete and skip
            if (line.isFringeBenefitIndicator()) {
                if (line.isSourceAccountingLine()) {
                    budgetDocument.getSourceAccountingLines().remove(line);
                } else {
                    budgetDocument.getTargetAccountingLines().remove(line);
                }
                continue;
            }

            final List<BudgetAdjustmentAccountingLine> benefitLines = generateBenefitLines(fiscalYear, line, budgetDocument);

            for (final BudgetAdjustmentAccountingLine benefitLine : benefitLines) {
                if (benefitLine.isSourceAccountingLine()) {
                    budgetDocument.addSourceAccountingLine((SourceAccountingLine) benefitLine);
                } else {
                    budgetDocument.addTargetAccountingLine((TargetAccountingLine) benefitLine);
                }
            }
        }
    }

    /**
     * Given a budget adjustment accounting line, generates appropriate fringe benefit lines for the line
     *
     * @param line a line to generate fringe benefit lines for
     * @return a List of BudgetAdjustmentAccountingLines to add to the document as fringe benefit lines
     */
    protected List<BudgetAdjustmentAccountingLine> generateBenefitLines(
            final Integer fiscalYear,
            final BudgetAdjustmentAccountingLine line, final BudgetAdjustmentDocument document) {
        final List<BudgetAdjustmentAccountingLine> fringeLines = new ArrayList<>();
        try {
            final Collection<PositionObjectBenefit> objectBenefits =
                    retrieveActiveLaborPositionObjectBenefits(fiscalYear,
                            line.getChartOfAccountsCode(), line.getFinancialObjectCode());
            if (objectBenefits != null) {
                for (final PositionObjectBenefit fringeBenefitInformation : objectBenefits) {
                    // now create and set properties for the benefit line
                    final BudgetAdjustmentAccountingLine benefitLine;
                    if (line.isSourceAccountingLine()) {
                        benefitLine = (BudgetAdjustmentAccountingLine) document.getSourceAccountingLineClass().newInstance();
                    } else {
                        benefitLine = (BudgetAdjustmentAccountingLine) document.getTargetAccountingLineClass().newInstance();
                    }

                    // create a map to use in the lookup of the account
                    final Map<String, Object> fieldValues = new HashMap<>();
                    fieldValues.put("chartOfAccountsCode", line.getChartOfAccountsCode());
                    fieldValues.put("accountNumber", line.getAccountNumber());
                    // use the budget adjustment accounting line to get the account number that will then be used to
                    // lookup the labor benefit rate category code
                    final Account lookupAccount = businessObjectService.findByPrimaryKey(Account.class, fieldValues);
                    final BenefitsCalculation benefitsCalculation;
                    String laborBenefitsRateCategoryCode = "";
                    // make sure the parameter exists
                    if (parameterService.parameterExists(Account.class, COAParameterConstants.BENEFIT_RATE)) {
                        laborBenefitsRateCategoryCode = parameterService.getParameterValueAsString(Account.class,
                                COAParameterConstants.BENEFIT_RATE);
                    }
                    // make sure the system parameter exists
                    if (parameterService.parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                            KFSParameterKeyConstants.LdParameterConstants
                                    .ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND)) {
                        // check the system param to see if the labor benefit rate category should be filled in
                        final String sysParam = parameterService.getParameterValueAsString(
                                KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                                KFSParameterKeyConstants.LdParameterConstants
                                        .ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND);
                        LOG.debug("sysParam: {}", sysParam);
                        // if sysParam == Y then Labor Benefit Rate Category should be used in the search
                        if ("Y".equalsIgnoreCase(sysParam)) {
                            if (StringUtils.isBlank(line.getSubAccount().getSubAccountNumber())) {
                                laborBenefitsRateCategoryCode = lookupAccount.getLaborBenefitRateCategoryCode();
                            } else {
                                laborBenefitsRateCategoryCode =
                                        laborBenefitsCalculationService.getBenefitRateCategoryCode(
                                                line.getChartOfAccountsCode(),
                                                line.getAccountNumber(),
                                                line.getSubAccount().getSubAccountNumber()
                                        );
                            }

                            // make sure laborBenefitsRateCategoryCode isn't null
                            if (ObjectUtils.isNull(laborBenefitsRateCategoryCode)) {
                                // make sure the parameter exists
                                if (parameterService.parameterExists(Account.class, COAParameterConstants.BENEFIT_RATE)) {
                                    laborBenefitsRateCategoryCode = parameterService
                                            .getParameterValueAsString(Account.class, COAParameterConstants.BENEFIT_RATE);
                                } else {
                                    laborBenefitsRateCategoryCode = "";
                                }
                            }
                        }
                    }

                    final String beneCalc = "{" + fringeBenefitInformation
                            .getUniversityFiscalYear() + "," + fringeBenefitInformation
                            .getChartOfAccountsCode() + "," + fringeBenefitInformation
                            .getFinancialObjectBenefitsTypeCode() + "," + laborBenefitsRateCategoryCode + "}";
                    LOG.info("Looking for a benefits calculation for {}", beneCalc);
                    // get the benefits calculation taking the laborBenefitRateCategoryCode into account
                    // CU customization: backport FINP-12883
                    benefitsCalculation =
                            laborBenefitsCalculationService.getBenefitsCalculation(
                                    fringeBenefitInformation.getUniversityFiscalYear(),
                                    fringeBenefitInformation.getChartOfAccountsCode(),
                                    fringeBenefitInformation.getFinancialObjectBenefitsTypeCode(),
                                    laborBenefitsRateCategoryCode
                            );

                    if (benefitsCalculation != null) {
                        LOG.info("Found benefits calculation for {}", beneCalc);
                    } else {
                        LOG.info("Couldn't locate a benefits calculation for {}", beneCalc);
                    }
                    if (benefitsCalculation != null && benefitsCalculation.isActive()) {
                        benefitLine.copyFrom(line);
                        benefitLine.setFinancialObjectCode(benefitsCalculation.getPositionFringeBenefitObjectCode());
                        benefitLine.refreshNonUpdateableReferences();
                        if (ObjectUtils.isNotNull(
                                laborBenefitsCalculationService.getCostSharingSourceAccountNumber())
                        ) {
                            benefitLine.setAccountNumber(
                                    laborBenefitsCalculationService.getCostSharingSourceAccountNumber()
                            );
                            benefitLine.setSubAccountNumber(
                                    laborBenefitsCalculationService.getCostSharingSourceSubAccountNumber()
                            );
                            benefitLine.setChartOfAccountsCode(
                                    laborBenefitsCalculationService.getCostSharingSourceAccountChartOfAccountsCode()
                            );
                        }

                        benefitLine.refreshNonUpdateableReferences();

                        // convert whole percentage to decimal value (5% to .0500, 18.66% to 0.1866)
                        final BigDecimal fringeBenefitPercent =
                                formatPercentageForMultiplication(benefitsCalculation.getPositionFringeBenefitPercent());
                        // compute the benefit current amount with all decimals and then round it to the closest integer
                        // by setting the scale to 0 and using the round half up rounding mode:
                        // exp. 1200*0.1866 = 223.92 -> rounded to 224
                        BigDecimal benefitCurrentAmount = line.getCurrentBudgetAdjustmentAmount().bigDecimalValue()
                                .multiply(fringeBenefitPercent);
                        benefitCurrentAmount = benefitCurrentAmount.setScale(2, RoundingMode.HALF_UP);
                        benefitLine.setCurrentBudgetAdjustmentAmount(new KualiDecimal(benefitCurrentAmount));

                        final KualiInteger benefitBaseAmount = line.getBaseBudgetAdjustmentAmount()
                                .multiply(fringeBenefitPercent);
                        benefitLine.setBaseBudgetAdjustmentAmount(benefitBaseAmount);

                        // clear monthly lines per KULEDOCS-1606
                        benefitLine.clearFinancialDocumentMonthLineAmounts();

                        // set flag on line so we know it was a generated benefit line and can clear it out later if
                        // needed
                        benefitLine.setFringeBenefitIndicator(true);

                        fringeLines.add(benefitLine);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException ie) {
            // it's doubtful this catch block or the catch block below are ever accessible, as accounting lines should
            // already have been generated for the document. But we can still make it somebody else's problem
            throw new RuntimeException(ie);
        }

        return fringeLines;
    }

    // Used in UConn and Stevens Overlays
    protected List<PositionObjectBenefit> retrieveActiveLaborPositionObjectBenefits(
            final Integer fiscalYear,
            final String chartOfAccountsCode, final String objectCode) {
        final Map<String, Object> searchCriteria = new HashMap<>();

        searchCriteria.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        searchCriteria.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        searchCriteria.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, objectCode);
        searchCriteria.put(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR);

        return (List<PositionObjectBenefit>) businessObjectService.findMatching(
                PositionObjectBenefit.class,
                searchCriteria
        );
    }

    @Override
    public boolean hasLaborObjectCodes(final BudgetAdjustmentDocument budgetDocument) {
        boolean hasLaborObjectCodes = false;

        final List<AccountingLine> accountingLines = new ArrayList<>();
        accountingLines.addAll(budgetDocument.getSourceAccountingLines());
        accountingLines.addAll(budgetDocument.getTargetAccountingLines());

        final Integer fiscalYear = budgetDocument.getPostingYear();

        for (final AccountingLine line : accountingLines) {
            if (hasFringeBenefitProducingObjectCodes(fiscalYear, line.getChartOfAccountsCode(),
                    line.getFinancialObjectCode())) {
                hasLaborObjectCodes = true;
                break;
            }
        }

        return hasLaborObjectCodes;
    }

    private boolean hasFringeBenefitProducingObjectCodes(
            final Integer fiscalYear,
            final String chartOfAccountsCode,
            final String financialObjectCode
    ) {
        final List<PositionObjectBenefit> objectBenefits = retrieveActiveLaborPositionObjectBenefits(
                fiscalYear, chartOfAccountsCode, financialObjectCode);
        return objectBenefits != null && !objectBenefits.isEmpty();
    }

    /**
     * Formats the stored percentage to be used in multiplication. For example if the percentage is 18.66 it will return
     * 0.1866. The returned number will always have 4 digits.
     *
     * @param percent the stored percent
     * @return percentage formatted for multiplication
     */
    protected BigDecimal formatPercentageForMultiplication(final BigDecimal percent) {
        BigDecimal result = BigDecimal.ZERO;

        if (percent != null) {
            result = percent.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        }

        return result;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setLaborBenefitsCalculationService(
            final LaborBenefitsCalculationService laborBenefitsCalculationService
    ) {
        this.laborBenefitsCalculationService = laborBenefitsCalculationService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
