package edu.cornell.kfs.module.ld.document.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.batch.LaborEnterpriseFeedStep;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.module.ld.document.service.impl.LaborPendingEntryConverterServiceImpl;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.rsmart.kuali.kfs.module.ld.LdConstants;

public class CuLaborPendingEntryConverterServiceImpl extends LaborPendingEntryConverterServiceImpl {

    public LaborLedgerPendingEntry getBenefitClearingPendingEntry(
            final LaborLedgerPostingDocument document, 
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper, final String accountNumber, 
            final String chartOfAccountsCode, final String benefitTypeCode, final KualiDecimal clearingAmount, final String objectCode) {
               
        final LaborLedgerPendingEntry pendingEntry = super.getBenefitClearingPendingEntry(
                                                       document, sequenceHelper,  accountNumber, chartOfAccountsCode,  benefitTypeCode,  clearingAmount); 
        
        pendingEntry.setPositionNumber(document.getLaborLedgerPendingEntry(0).getPositionNumber());
        pendingEntry.setEmplid(document.getLaborLedgerPendingEntry(0).getEmplid());
        pendingEntry.setFinancialObjectCode(objectCode);
        
        return pendingEntry;
        
    }
    
    @Override
    public LaborLedgerPendingEntry getBenefitPendingEntry(
            final LaborLedgerPostingDocument document, final ExpenseTransferAccountingLine accountingLine, 
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper, final KualiDecimal benefitAmount, final String fringeBenefitObjectCode) {
        
        final LaborLedgerPendingEntry pendingEntry = super.getBenefitPendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);
        
        pendingEntry.setPositionNumber(accountingLine.getPositionNumber());
        pendingEntry.setEmplid(accountingLine.getEmplid());
        
        return pendingEntry;
        
    }

    /**
     * convert the given document and accounting line into the benefit pending entries
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @param benefitAmount the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit pending entries
     */
    public List<LaborLedgerPendingEntry> getOffsetPendingEntries(
            final LaborLedgerPendingEntry pendingEntry, final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        final List<LaborLedgerPendingEntry> offsetEntries = new ArrayList<LaborLedgerPendingEntry>();
        final String benefitRateCategoryCode = SpringContext.getBean(LaborBenefitsCalculationService.class).getBenefitRateCategoryCode(pendingEntry.getChartOfAccountsCode(), pendingEntry.getAccountNumber(), pendingEntry.getSubAccountNumber());
        final Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getActivePositionObjectBenefits(pendingEntry.getUniversityFiscalYear(), pendingEntry.getChartOfAccountsCode(), pendingEntry.getFinancialObjectCode());
        
        if (positionObjectBenefits == null || positionObjectBenefits.isEmpty()) {
            return offsetEntries;
        }

        for (final PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
            final Map<String, Object> fieldValues = new HashMap<String, Object>();
            fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, pendingEntry.getUniversityFiscalYear());
            fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, pendingEntry.getChartOfAccountsCode());
            fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
            fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, benefitRateCategoryCode);
            
            final BenefitsCalculation benefitsCalculation =  SpringContext.getBean(BusinessObjectService.class).
                    findByPrimaryKey(BenefitsCalculation.class, fieldValues);

            if (ObjectUtils.isNull(benefitsCalculation)) { 
                continue;
            }

            final LaborLedgerPendingEntry offsetEntry = new LaborLedgerPendingEntry();

            // Copy values from pending entry
            offsetEntry.setReferenceFinancialDocumentNumber(pendingEntry.getReferenceFinancialDocumentNumber());
            offsetEntry.setReferenceFinancialDocumentTypeCode(pendingEntry.getReferenceFinancialDocumentTypeCode());
            offsetEntry.setFinancialDocumentReversalDate(pendingEntry.getFinancialDocumentReversalDate());
            offsetEntry.setFinancialBalanceTypeCode(pendingEntry.getFinancialBalanceTypeCode());
            offsetEntry.setFinancialObjectTypeCode(pendingEntry.getFinancialObjectTypeCode());
            offsetEntry.setReferenceFinancialSystemOriginationCode(pendingEntry.getReferenceFinancialSystemOriginationCode());
            offsetEntry.setOrganizationDocumentNumber(pendingEntry.getOrganizationDocumentNumber());
            offsetEntry.setTransactionDate(pendingEntry.getTransactionDate());
            offsetEntry.setTransactionDebitCreditCode(pendingEntry.getTransactionDebitCreditCode());
            offsetEntry.setTransactionEncumbranceUpdateCode(pendingEntry.getTransactionEncumbranceUpdateCode());
            offsetEntry.setPositionNumber(pendingEntry.getPositionNumber());
            offsetEntry.setTransactionPostingDate(pendingEntry.getTransactionPostingDate());
            offsetEntry.setPayPeriodEndDate(pendingEntry.getPayPeriodEndDate());
            offsetEntry.setTransactionTotalHours(pendingEntry.getTransactionTotalHours());
            offsetEntry.setPayrollEndDateFiscalYear(pendingEntry.getPayrollEndDateFiscalYear());
            offsetEntry.setPayrollEndDateFiscalPeriodCode(pendingEntry.getPayrollEndDateFiscalPeriodCode());
            offsetEntry.setEmplid(pendingEntry.getEmplid());
            offsetEntry.setEmployeeRecord(pendingEntry.getEmployeeRecord());
            offsetEntry.setEarnCode(pendingEntry.getEarnCode());
            offsetEntry.setPayGroup(pendingEntry.getPayGroup());
            offsetEntry.setSalaryAdministrationPlan(pendingEntry.getSalaryAdministrationPlan());
            offsetEntry.setGrade(pendingEntry.getGrade());
            offsetEntry.setRunIdentifier(pendingEntry.getRunIdentifier());
            offsetEntry.setLaborLedgerOriginalChartOfAccountsCode(pendingEntry.getLaborLedgerOriginalChartOfAccountsCode());
            offsetEntry.setLaborLedgerOriginalAccountNumber(pendingEntry.getLaborLedgerOriginalAccountNumber());
            offsetEntry.setLaborLedgerOriginalSubAccountNumber(pendingEntry.getLaborLedgerOriginalSubAccountNumber());
            offsetEntry.setLaborLedgerOriginalFinancialObjectCode(pendingEntry.getLaborLedgerOriginalFinancialObjectCode());
            offsetEntry.setLaborLedgerOriginalFinancialSubObjectCode(pendingEntry.getLaborLedgerOriginalFinancialSubObjectCode());
            offsetEntry.setHrmsCompany(pendingEntry.getHrmsCompany());
            offsetEntry.setSetid(pendingEntry.getSetid());
            offsetEntry.setTransactionEntryOffsetCode(pendingEntry.getTransactionEntryOffsetCode());
            offsetEntry.setPayrollEndDateFiscalPeriod(pendingEntry.getPayrollEndDateFiscalPeriod());

            // New offset values
            offsetEntry.setFinancialObjectCode(benefitsCalculation.getPositionFringeBenefitObjectCode());

            offsetEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));

            // calculate the offsetAmount amount (ledger amt * (benfit pct/100) )
            final KualiDecimal fringeBenefitPercent = benefitsCalculation.getPositionFringeBenefitPercent();
            KualiDecimal offsetAmount = fringeBenefitPercent.multiply(
                    pendingEntry.getTransactionLedgerEntryAmount()).divide(KFSConstants.ONE_HUNDRED.kualiDecimalValue());
            offsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());
            
            
            offsetEntry.setAccountNumber(benefitsCalculation.getAccountCodeOffset());
            offsetEntry.setFinancialObjectCode(benefitsCalculation.getObjectCodeOffset());
            
            //Set all the fields required to process through the scrubber and poster jobs
            offsetEntry.setUniversityFiscalPeriodCode(pendingEntry.getUniversityFiscalPeriodCode());
            offsetEntry.setChartOfAccountsCode(pendingEntry.getChartOfAccountsCode());
            offsetEntry.setUniversityFiscalYear(pendingEntry.getUniversityFiscalYear());
            offsetEntry.setSubAccountNumber("-----");
            offsetEntry.setFinancialSubObjectCode("---");
            offsetEntry.setOrganizationReferenceId("");
            offsetEntry.setProjectCode("");
            
            offsetEntry.setTransactionLedgerEntryDescription("GENERATED BENEFIT OFFSET");
            
            final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            
            final String originCode = parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_ORIGIN_CODE);
            
            offsetEntry.setFinancialSystemOriginationCode(originCode);
            final DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
            offsetEntry.setDocumentNumber(dateTimeService.toString(dateTimeService.getCurrentDate(), "yyyyMMddhhmmssSSS"));


            if(pendingEntry.getTransactionDebitCreditCode().equalsIgnoreCase("D")) {
                offsetAmount = offsetAmount;
            } else {
                offsetAmount = offsetAmount.multiply(new KualiDecimal(-1));
            }
            
            if(offsetAmount.isGreaterThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("C");
            } else if(offsetAmount.isLessThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("D");
            }
            
            String offsetDocTypes = null;
            if(StringUtils.isNotEmpty(parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE))) {
                offsetDocTypes = "," + parameterService.getParameterValueAsString(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE).replace(";", ",").replace("|", ",") + ",";
            }

            String docTypeCode = offsetDocTypes;
            if (offsetDocTypes.contains(",")) {
                String[] splits = offsetDocTypes.split(",");
                for(String split : splits) {
                    if(!StringUtils.isEmpty(split)) {
                        docTypeCode = split;
                        break;
                    }
                }
            }
            offsetEntry.setFinancialDocumentTypeCode(docTypeCode);


            offsetEntries.add(offsetEntry);
        }
    
        return offsetEntries;
    }
}
