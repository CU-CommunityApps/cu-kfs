/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.module.ld.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferSourceAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferTargetAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;

/**
 * This class is used to help generating pending entries for the given labor documents
 */
public class LaborPendingEntryGenerator {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborPendingEntryGenerator.class);

    /**
     * generate the expense pending entries based on the given document and accounting line
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense pending entries
     */
    public static List<LaborLedgerPendingEntry> generateExpensePendingEntries(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        List<LaborLedgerPendingEntry> expensePendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        LaborLedgerPendingEntry expensePendingEntry = LaborPendingEntryConverter.getExpensePendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expensePendingEntry);

        // KFSMI-6863: always create A2 entries regardless of fiscal period
        LaborLedgerPendingEntry expenseA21PendingEntry = LaborPendingEntryConverter.getExpenseA21PendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expenseA21PendingEntry);

        LaborLedgerPendingEntry expenseA21ReversalPendingEntry = LaborPendingEntryConverter.getExpenseA21ReversalPendingEntry(document, accountingLine, sequenceHelper);
        expensePendingEntries.add(expenseA21ReversalPendingEntry);

        return expensePendingEntries;
    }

    /**
     * generate the benefit pending entries based on the given document and accounting line
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of benefit pending entries
     */
    public static List<LaborLedgerPendingEntry> generateBenefitPendingEntries(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return null;
        }

        String fringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(fringeOrSalaryCode)) {
            return null;
        }

        Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        String objectCode = accountingLine.getFinancialObjectCode();
        Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(payrollFiscalyear, chartOfAccountsCode, objectCode);

        List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            String fringeBenefitObjectCode = retrieveFringeBenefitObjectCode(accountingLine, chartOfAccountsCode);

            KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class).calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(), accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmount.isNonZero()) {
                
                //make sure the fringebenefitobjectcode is not null
                if(ObjectUtils.isNull(fringeBenefitObjectCode)){
                    String laborBenefitRateCategoryCode = "";
                    
                    //make sure the system parameter exists
                    if (SpringContext.getBean(ParameterService.class).parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY")) {
                        //parameter exists, get the benefit rate based off of the university fiscal year, chart of account code, labor benefit type code and labor benefit rate category code 
                        String sysParam = SpringContext.getBean(ParameterService.class).getParameterValue(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY");
                        LOG.debug("sysParam: " + sysParam);
                        //if sysParam == Y then use the Labor Benefit Rate Category Code to help determine the fringe benefit rate
                        if (sysParam.equalsIgnoreCase("Y")) {
                            laborBenefitRateCategoryCode = positionObjectBenefit.getLaborBenefitRateCategoryCode();
                        }else{
                         // make sure the parameter exists
                            if (SpringContext.getBean(ParameterService.class).parameterExists(Account.class, "DEFAULT_BENEFIT_RATE_CATEGORY_CODE")) {
                                laborBenefitRateCategoryCode = SpringContext.getBean(ParameterService.class).getParameterValue(Account.class, "DEFAULT_BENEFIT_RATE_CATEGORY_CODE");
                            }
                            else {
                                laborBenefitRateCategoryCode = "";
                            }
                        }
                    }
                    
                    if(StringUtils.isBlank(laborBenefitRateCategoryCode)){
                        laborBenefitRateCategoryCode = SpringContext.getBean(ParameterService.class).getParameterValue(Account.class, "DEFAULT_BENEFIT_RATE_CATEGORY_CODE");
                    }
                    
                    //create a  map for the search criteria to lookup the fringe benefit percentage 
                    Map<String, Object> fieldValues = new HashMap<String, Object>();
                    fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, positionObjectBenefit.getUniversityFiscalYear());
                    fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, positionObjectBenefit.getChartOfAccountsCode());
                    fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
                    fieldValues.put("laborBenefitRateCategoryCode",laborBenefitRateCategoryCode);
                    BenefitsCalculation bc = (BenefitsCalculation) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(BenefitsCalculation.class, fieldValues);
                    
                    fringeBenefitObjectCode = bc.getPositionFringeBenefitObjectCode();
                }
                
                List<LaborLedgerPendingEntry> pendingEntries = generateBenefitPendingEntries(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
                benefitPendingEntries.addAll(pendingEntries);
            }
        }

        return benefitPendingEntries;
    }

    /**
     * generate the benefit pending entries with the given benefit amount and fringe benefit object code based on the given document
     * and accounting line
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @param benefitAmount the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit pending entries with the given benefit amount and fringe benefit object code
     */
    public static List<LaborLedgerPendingEntry> generateBenefitPendingEntries(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {
        List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        LaborLedgerPendingEntry benefitPendingEntry = LaborPendingEntryConverter.getBenefitPendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitPendingEntry);

        // KFSMI-6863: always create A2 entries regardless of fiscal period
        LaborLedgerPendingEntry benefitA21PendingEntry = LaborPendingEntryConverter.getBenefitA21PendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitA21PendingEntry);

        LaborLedgerPendingEntry benefitA21ReversalPendingEntry = LaborPendingEntryConverter.getBenefitA21ReversalPendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
        benefitPendingEntries.add(benefitA21ReversalPendingEntry);

        return benefitPendingEntries;
    }

    /**
     * generate the benefit clearing pending entries with the given benefit amount and fringe benefit object code based on the given
     * document and accounting line
     * 
     * @param document the given accounting document
     * @param sequenceHelper the given sequence helper
     * @param accountNumber the given clearing account number
     * @param chartOfAccountsCode the given clearing chart of accounts code
     * @return a set of benefit clearing pending entries
     */
    public static List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(LaborLedgerPostingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, String accountNumber, String chartOfAccountsCode) {
        List<LaborLedgerPendingEntry> benefitClearingPendingEntries = new ArrayList<LaborLedgerPendingEntry>();

        Map<String, Map<String, KualiDecimal>> sourceLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (ExpenseTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            updateBenefitAmountSum(sourceLineBenefitAmountSumByObjectCode, accountingLine);
        }
        
        Map<String, Map<String, KualiDecimal>> targetLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        List<ExpenseTransferTargetAccountingLine> targetAccountingLines = document.getTargetAccountingLines();
        for (ExpenseTransferTargetAccountingLine accountingLine : targetAccountingLines) {
            updateBenefitAmountSum(targetLineBenefitAmountSumByObjectCode, accountingLine);
        }

        Set<String> benefitTypeCodes = new HashSet<String>();
        for (String key : targetLineBenefitAmountSumByObjectCode.keySet()) {
            benefitTypeCodes.add(key);
        }

        for (String key : sourceLineBenefitAmountSumByObjectCode.keySet()) {
            benefitTypeCodes.add(key);
        }

        for (String benefitTypeCode : benefitTypeCodes) {
            KualiDecimal targetAmount = KualiDecimal.ZERO;
            Map<String, KualiDecimal> targetBenefitSumsByObjectCode = new HashMap<String, KualiDecimal>();
            if (targetLineBenefitAmountSumByObjectCode.containsKey(benefitTypeCode)) {
                targetBenefitSumsByObjectCode = targetLineBenefitAmountSumByObjectCode.get(benefitTypeCode);

                for(String objCode : targetBenefitSumsByObjectCode.keySet()) {
                	if(targetBenefitSumsByObjectCode.containsKey(objCode)) {
                		targetAmount = targetAmount.add(targetBenefitSumsByObjectCode.get(objCode));
                	}
                }
            }

            KualiDecimal sourceAmount = KualiDecimal.ZERO;
            Map<String, KualiDecimal> sourceBenefitSumsByObjectCode = new HashMap<String, KualiDecimal>();
            if (sourceLineBenefitAmountSumByObjectCode.containsKey(benefitTypeCode)) {
                sourceBenefitSumsByObjectCode = sourceLineBenefitAmountSumByObjectCode.get(benefitTypeCode);
                
                for(String objCode : sourceBenefitSumsByObjectCode.keySet()) {
                	if(sourceBenefitSumsByObjectCode.containsKey(objCode)) {
                		sourceAmount = sourceAmount.add(sourceBenefitSumsByObjectCode.get(objCode));
                	}
                }
            }

            KualiDecimal clearingAmount = sourceAmount.subtract(targetAmount);
            KualiDecimal amountForObjectCode = KualiDecimal.ZERO;
            if (clearingAmount.isNonZero() && ObjectUtils.isNotNull(benefitTypeCode)) {
            	for(String objCode : sourceBenefitSumsByObjectCode.keySet()) {
            		amountForObjectCode = sourceBenefitSumsByObjectCode.get(objCode);
            		benefitClearingPendingEntries.add(LaborPendingEntryConverter.getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, objCode, benefitTypeCode, amountForObjectCode));
            	}
            	for(String objCode : targetBenefitSumsByObjectCode.keySet()) {
            		amountForObjectCode = targetBenefitSumsByObjectCode.get(objCode);
            		benefitClearingPendingEntries.add(LaborPendingEntryConverter.getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, objCode, benefitTypeCode, amountForObjectCode.negated()));
            	}
            }
        }

        return benefitClearingPendingEntries;
    }

    /**
     * update the benefit amount summary map based on the given accounting line
     * 
     * @param benefitAmountSumByBenefitType the given benefit amount summary map, which stores the amounts in a sub-map based on object code
     * @param accountingLine the given accounting line
     */
    protected static void updateBenefitAmountSum(Map<String, Map<String, KualiDecimal>> benefitAmountSumByBenefitType, ExpenseTransferAccountingLine accountingLine) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return;
        }

        String fringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(fringeOrSalaryCode)) {
            return;
        }

        Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        String objectCode = accountingLine.getFinancialObjectCode();

        String fringeBenefitObjectCode = retrieveFringeBenefitObjectCode(accountingLine, chartOfAccountsCode);
        
        Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(payrollFiscalyear, chartOfAccountsCode, objectCode);
        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
        	String tmpLaborBenefitRateCategoryCode = ((AccountExtendedAttribute)accountingLine.getAccount().getExtension()).getLaborBenefitRateCategoryCode();

            positionObjectBenefit.setLaborBenefitRateCategoryCode(tmpLaborBenefitRateCategoryCode);
            String benefitTypeCode = positionObjectBenefit.getBenefitsCalculation().getPositionBenefitTypeCode();

            Map<String, KualiDecimal> benefitSumsByObjectCode = new HashMap<String, KualiDecimal> ();
            KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class).calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(), accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmountSumByBenefitType.containsKey(benefitTypeCode)) {
            	benefitSumsByObjectCode = benefitAmountSumByBenefitType.get(benefitTypeCode);
            	if(benefitSumsByObjectCode.containsKey(fringeBenefitObjectCode)) {
                    benefitAmount = benefitAmount.add(benefitSumsByObjectCode.get(fringeBenefitObjectCode));
            	}
            	benefitSumsByObjectCode.put(fringeBenefitObjectCode, benefitAmount);
            } else {
            	benefitSumsByObjectCode.put(fringeBenefitObjectCode, benefitAmount);
            	benefitAmountSumByBenefitType.put(benefitTypeCode, benefitSumsByObjectCode);
            }
            benefitAmountSumByBenefitType.put(benefitTypeCode, benefitSumsByObjectCode);
        }
    }

    /**
     * determine if the pay fiscal year and period from the accounting line match with its university fiscal year and period.
     * 
     * @param document the given document
     * @param accountingLine the given accounting line of the document
     * @return true if the pay fiscal year and period from the accounting line match with its university fiscal year and period;
     *         otherwise, false
     */
    protected static boolean isAccountingLinePayFYPeriodMatchesUniversityPayFYPeriod(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine) {
        Integer fiscalYear = document.getPostingYear();
        Integer payFiscalYear = accountingLine.getPayrollEndDateFiscalYear();
        if (!fiscalYear.equals(payFiscalYear)) {
            return false;
        }

        String periodCode = document.getPostingPeriodCode();
        String payPeriodCode = accountingLine.getPayrollEndDateFiscalPeriodCode();
        if (!StringUtils.equals(periodCode, payPeriodCode)) {
            return false;
        }

        return true;
    }
    
    /**
     * 
     * @param accountingLine
     * @param chartOfAccountsCode
     * @return
     */
    private static String retrieveFringeBenefitObjectCode(ExpenseTransferAccountingLine accountingLine, String chartOfAccountsCode) {
        String fringeBenefitObjectCode = "";
        Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(accountingLine.getPayrollEndDateFiscalYear(), chartOfAccountsCode, accountingLine.getFinancialObjectCode());

        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
        	String tmpLaborBenefitRateCategoryCode = ((AccountExtendedAttribute)accountingLine.getAccount().getExtension()).getLaborBenefitRateCategoryCode();
        	BenefitsCalculation benefitsCalculation = positionObjectBenefit.getBenefitsCalculation(tmpLaborBenefitRateCategoryCode);
        	positionObjectBenefit.setLaborLedgerBenefitsCalculation(benefitsCalculation);
            positionObjectBenefit.setLaborBenefitRateCategoryCode(tmpLaborBenefitRateCategoryCode);
            fringeBenefitObjectCode = positionObjectBenefit.getBenefitsCalculation().getPositionFringeBenefitObjectCode();
        }
        return fringeBenefitObjectCode;
    }
    
        public static List<LaborLedgerPendingEntry> generateOffsetPendingEntries(List<LaborLedgerPendingEntry> expenseEntries, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
    	        List<LaborLedgerPendingEntry> offsetPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
    	        for (LaborLedgerPendingEntry expenseEntry : expenseEntries) {
    	            offsetPendingEntries.addAll(LaborPendingEntryConverter.getOffsetPendingEntries(expenseEntry, sequenceHelper));
    	        }
    	        
    	        return offsetPendingEntries;
    	    }
}
