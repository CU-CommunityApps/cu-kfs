package edu.cornell.kfs.module.ld.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.COAParameterConstants;
import org.kuali.kfs.coa.businessobject.Account;
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
import org.kuali.kfs.module.ld.util.LaborPendingEntryGenerator;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.ld.document.service.impl.CuLaborPendingEntryConverterServiceImpl;

public class CuLaborPendingEntryGenerator {
	
    public static List<LaborLedgerPendingEntry> generateOffsetPendingEntries(List<LaborLedgerPendingEntry> expenseEntries, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        List<LaborLedgerPendingEntry> offsetPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        for (LaborLedgerPendingEntry expenseEntry : expenseEntries) {
            offsetPendingEntries.addAll(((CuLaborPendingEntryConverterServiceImpl) SpringContext.getBean(LaborPendingEntryConverterService.class))
                    .getOffsetPendingEntries(expenseEntry, sequenceHelper));
        }
          
        return offsetPendingEntries;
    }
    
    public static List<LaborLedgerPendingEntry> generateBenefitPendingEntries(
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

        final Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        final String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        final String objectCode = accountingLine.getFinancialObjectCode();
        final Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getActivePositionObjectBenefits(payrollFiscalyear, chartOfAccountsCode, objectCode);

        final List<LaborLedgerPendingEntry> benefitPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            positionObjectBenefit.setLaborBenefitRateCategoryCode(accountingLine.getAccount().getLaborBenefitRateCategoryCode());
            String fringeBenefitObjectCode = retrieveFringeBenefitObjectCode(accountingLine, chartOfAccountsCode);

            final KualiDecimal benefitAmount = SpringContext.getBean(LaborBenefitsCalculationService.class).calculateFringeBenefit(positionObjectBenefit, accountingLine.getAmount(), accountingLine.getAccountNumber(), accountingLine.getSubAccountNumber());
            if (benefitAmount.isNonZero() && positionObjectBenefit.getBenefitsCalculation().isActive()) {

                final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
                final Boolean enableFringeBenefitCalculationByBenefitRate = parameterService.getParameterValueAsBoolean(KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class, LaborConstants.BenefitCalculation.ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_PARAMETER);
                
                //If fringeBenefitObjectCode is empty and its enable to use calculation by benefit rate
                if(StringUtils.isEmpty(fringeBenefitObjectCode) && enableFringeBenefitCalculationByBenefitRate){
                    
                    String laborBenefitRateCategoryCode = positionObjectBenefit.getLaborBenefitRateCategoryCode();
                     // Use parameter default if labor benefit rate category code is blank
                    if(StringUtils.isBlank(laborBenefitRateCategoryCode)){
                        laborBenefitRateCategoryCode = parameterService.getParameterValueAsString(Account.class, COAParameterConstants.BENEFIT_RATE);
                    }
                
                    //create a  map for the search criteria to lookup the fringe benefit percentage 
                    final Map<String, Object> fieldValues = new HashMap<String, Object>();
                    fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, positionObjectBenefit.getUniversityFiscalYear());
                    fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, positionObjectBenefit.getChartOfAccountsCode());
                    fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
                    fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE,laborBenefitRateCategoryCode);
                    final BenefitsCalculation bc = (BenefitsCalculation) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(BenefitsCalculation.class, fieldValues);
                    
                    fringeBenefitObjectCode = bc.getPositionFringeBenefitObjectCode();
                }
                
                final List<LaborLedgerPendingEntry> pendingEntries = LaborPendingEntryGenerator.generateBenefitPendingEntries(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
                benefitPendingEntries.addAll(pendingEntries);
            }
        }

        return benefitPendingEntries;
    }
    
    public static List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(
            final LaborLedgerPostingDocument document, 
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper, final String accountNumber, final String chartOfAccountsCode) {
        final List<LaborLedgerPendingEntry> benefitClearingPendingEntries = new ArrayList<LaborLedgerPendingEntry>();

        final Map<String, Map<String, KualiDecimal>> sourceLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        final List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (final ExpenseTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            updateBenefitAmountSumObject(sourceLineBenefitAmountSumByObjectCode, accountingLine);
        }
        
        final Map<String, Map<String, KualiDecimal>> targetLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        final List<ExpenseTransferTargetAccountingLine> targetAccountingLines = document.getTargetAccountingLines();
        for (final ExpenseTransferTargetAccountingLine accountingLine : targetAccountingLines) {
        	updateBenefitAmountSumObject(targetLineBenefitAmountSumByObjectCode, accountingLine);
        }

        final Set<String> benefitTypeCodes = new HashSet<String>();
        for (final String key : targetLineBenefitAmountSumByObjectCode.keySet()) {
            benefitTypeCodes.add(key);
        }

        for (final String key : sourceLineBenefitAmountSumByObjectCode.keySet()) {
            benefitTypeCodes.add(key);
        }

        for (final String benefitTypeCode : benefitTypeCodes) {
            KualiDecimal targetAmount = KualiDecimal.ZERO;
            Map<String, KualiDecimal> targetBenefitSumsByObjectCode = new HashMap<String, KualiDecimal>();
            if (targetLineBenefitAmountSumByObjectCode.containsKey(benefitTypeCode)) {
                targetBenefitSumsByObjectCode = targetLineBenefitAmountSumByObjectCode.get(benefitTypeCode);

                for(final String objCode : targetBenefitSumsByObjectCode.keySet()) {
                	if(targetBenefitSumsByObjectCode.containsKey(objCode)) {
                		targetAmount = targetAmount.add(targetBenefitSumsByObjectCode.get(objCode));
                	}
                }
            }

            KualiDecimal sourceAmount = KualiDecimal.ZERO;
            Map<String, KualiDecimal> sourceBenefitSumsByObjectCode = new HashMap<String, KualiDecimal>();
            if (sourceLineBenefitAmountSumByObjectCode.containsKey(benefitTypeCode)) {
                sourceBenefitSumsByObjectCode = sourceLineBenefitAmountSumByObjectCode.get(benefitTypeCode);
                
                for(final String objCode : sourceBenefitSumsByObjectCode.keySet()) {
                	if(sourceBenefitSumsByObjectCode.containsKey(objCode)) {
                		sourceAmount = sourceAmount.add(sourceBenefitSumsByObjectCode.get(objCode));
                	}
                }
            }

            final KualiDecimal clearingAmount = sourceAmount.subtract(targetAmount);
            KualiDecimal amountForObjectCode = KualiDecimal.ZERO;
            if (clearingAmount.isNonZero() && ObjectUtils.isNotNull(benefitTypeCode)) {
            	for(String objCode : sourceBenefitSumsByObjectCode.keySet()) {
            		amountForObjectCode = sourceBenefitSumsByObjectCode.get(objCode);
            		benefitClearingPendingEntries.add(((CuLaborPendingEntryConverterServiceImpl) SpringContext.getBean(LaborPendingEntryConverterService.class)).getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, benefitTypeCode, amountForObjectCode, objCode));
            	}
            	for(String objCode : targetBenefitSumsByObjectCode.keySet()) {
            		amountForObjectCode = targetBenefitSumsByObjectCode.get(objCode);
            		benefitClearingPendingEntries.add(((CuLaborPendingEntryConverterServiceImpl)SpringContext.getBean(LaborPendingEntryConverterService.class)).getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, benefitTypeCode, amountForObjectCode.negated(), objCode));
            	}
            }
        }


        //refresh nonupdateable references for financial object...
        LaborPendingEntryGenerator.refreshObjectCodeNonUpdateableReferences(benefitClearingPendingEntries);   
        
        return benefitClearingPendingEntries;
    }
    
    protected static void updateBenefitAmountSumObject(
            final Map<String, Map<String, KualiDecimal>> benefitAmountSumByBenefitType, final ExpenseTransferAccountingLine accountingLine) {
        accountingLine.refreshReferenceObject(KFSPropertyConstants.LABOR_OBJECT);
        if (ObjectUtils.isNull(accountingLine.getLaborObject())) {
            return;
        }

        final String fringeOrSalaryCode = accountingLine.getLaborObject().getFinancialObjectFringeOrSalaryCode();
        if (!LaborConstants.SalaryExpenseTransfer.LABOR_LEDGER_SALARY_CODE.equals(fringeOrSalaryCode)) {
            return;
        }

        final Integer payrollFiscalyear = accountingLine.getPayrollEndDateFiscalYear();
        final String chartOfAccountsCode = accountingLine.getChartOfAccountsCode();
        final String objectCode = accountingLine.getFinancialObjectCode();

        final String fringeBenefitObjectCode = retrieveFringeBenefitObjectCode(accountingLine, chartOfAccountsCode);
        
        final Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(payrollFiscalyear, chartOfAccountsCode, objectCode);
        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
        	final String tmpLaborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();

            positionObjectBenefit.setLaborBenefitRateCategoryCode(tmpLaborBenefitRateCategoryCode);
            final String benefitTypeCode = positionObjectBenefit.getBenefitsCalculation().getPositionBenefitTypeCode();

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
    
    private static String retrieveFringeBenefitObjectCode(
            final ExpenseTransferAccountingLine accountingLine, final String chartOfAccountsCode) {
        String fringeBenefitObjectCode = "";
        final Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(accountingLine.getPayrollEndDateFiscalYear(), chartOfAccountsCode, accountingLine.getFinancialObjectCode());

        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            final String tmpLaborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();
            final BenefitsCalculation benefitsCalculation = positionObjectBenefit.getBenefitsCalculation();
            if (ObjectUtils.isNull(benefitsCalculation)) {
                continue;
            }
            positionObjectBenefit.setLaborLedgerBenefitsCalculation(benefitsCalculation);
            positionObjectBenefit.setLaborBenefitRateCategoryCode(tmpLaborBenefitRateCategoryCode);
            fringeBenefitObjectCode = positionObjectBenefit.getBenefitsCalculation().getPositionFringeBenefitObjectCode();
        }
        return fringeBenefitObjectCode;
    }
}
