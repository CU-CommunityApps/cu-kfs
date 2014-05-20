package edu.cornell.kfs.module.ld.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.module.ld.LaborConstants;
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
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.ld.document.service.impl.CuLaborPendingEntryConverterServiceImpl;

public class CuLaborPendingEntryGenerator extends LaborPendingEntryGenerator {
	
    public static List<LaborLedgerPendingEntry> generateOffsetPendingEntries(List<LaborLedgerPendingEntry> expenseEntries, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        List<LaborLedgerPendingEntry> offsetPendingEntries = new ArrayList<LaborLedgerPendingEntry>();
        for (LaborLedgerPendingEntry expenseEntry : expenseEntries) {
            offsetPendingEntries.addAll(((CuLaborPendingEntryConverterServiceImpl) SpringContext.getBean(LaborPendingEntryConverterService.class))
                    .getOffsetPendingEntries(expenseEntry, sequenceHelper));
        }
          
        return offsetPendingEntries;
    }
    
    public static List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(LaborLedgerPostingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, String accountNumber, String chartOfAccountsCode) {
        List<LaborLedgerPendingEntry> benefitClearingPendingEntries = new ArrayList<LaborLedgerPendingEntry>();

        Map<String, Map<String, KualiDecimal>> sourceLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = document.getSourceAccountingLines();
        for (ExpenseTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            updateBenefitAmountSumObject(sourceLineBenefitAmountSumByObjectCode, accountingLine);
        }
        
        Map<String, Map<String, KualiDecimal>> targetLineBenefitAmountSumByObjectCode = new HashMap<String, Map<String, KualiDecimal>>();
        List<ExpenseTransferTargetAccountingLine> targetAccountingLines = document.getTargetAccountingLines();
        for (ExpenseTransferTargetAccountingLine accountingLine : targetAccountingLines) {
        	updateBenefitAmountSumObject(targetLineBenefitAmountSumByObjectCode, accountingLine);
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
            		benefitClearingPendingEntries.add(((CuLaborPendingEntryConverterServiceImpl) SpringContext.getBean(LaborPendingEntryConverterService.class)).getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, benefitTypeCode, amountForObjectCode, objCode));
            	}
            	for(String objCode : targetBenefitSumsByObjectCode.keySet()) {
            		amountForObjectCode = targetBenefitSumsByObjectCode.get(objCode);
            		benefitClearingPendingEntries.add(((CuLaborPendingEntryConverterServiceImpl)SpringContext.getBean(LaborPendingEntryConverterService.class)).getBenefitClearingPendingEntry(document, sequenceHelper, accountNumber, chartOfAccountsCode, benefitTypeCode, amountForObjectCode.negated(), objCode));
            	}
            }
        }


        //refresh nonupdateable references for financial object...
        refreshObjectCodeNonUpdateableReferences(benefitClearingPendingEntries);   
        
        return benefitClearingPendingEntries;
    }
    
    protected static void updateBenefitAmountSumObject(Map<String, Map<String, KualiDecimal>> benefitAmountSumByBenefitType, ExpenseTransferAccountingLine accountingLine) {
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
        	String tmpLaborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();

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
    
    private static String retrieveFringeBenefitObjectCode(ExpenseTransferAccountingLine accountingLine, String chartOfAccountsCode) {
        String fringeBenefitObjectCode = "";
        Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(accountingLine.getPayrollEndDateFiscalYear(), chartOfAccountsCode, accountingLine.getFinancialObjectCode());

        for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
        	String tmpLaborBenefitRateCategoryCode = accountingLine.getAccount().getLaborBenefitRateCategoryCode();
        	BenefitsCalculation benefitsCalculation = positionObjectBenefit.getBenefitsCalculation(tmpLaborBenefitRateCategoryCode);
        	positionObjectBenefit.setLaborLedgerBenefitsCalculation(benefitsCalculation);
            positionObjectBenefit.setLaborBenefitRateCategoryCode(tmpLaborBenefitRateCategoryCode);
            fringeBenefitObjectCode = positionObjectBenefit.getBenefitsCalculation().getPositionFringeBenefitObjectCode();
        }
        return fringeBenefitObjectCode;
    }
}
