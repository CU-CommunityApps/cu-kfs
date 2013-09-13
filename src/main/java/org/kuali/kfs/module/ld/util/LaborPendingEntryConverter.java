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

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.fp.document.YearEndDocument;
import org.kuali.kfs.fp.document.YearEndDocumentUtil;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.util.KualiDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.batch.LaborEnterpriseFeedStep;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.ObjectUtils;
import com.rsmart.kuali.kfs.module.ld.LdConstants;
import com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculationExtension;

/**
 * This class provides a set of facilities that can conver the accounting document and its accounting lines into labor pending
 * entries
 */
public class LaborPendingEntryConverter {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborPendingEntryConverter.class);

    /**
     * convert the given document and accounting line into the expense pending entries
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense pending entries
     */
    public static LaborLedgerPendingEntry getExpensePendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LaborLedgerPendingEntry pendingEntry = getDefaultPendingEntry(document, accountingLine);

        pendingEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        pendingEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));

        // year end document should post to previous fiscal year and final period
        if (document instanceof YearEndDocument) {
            pendingEntry.setUniversityFiscalYear(YearEndDocumentUtil.getPreviousFiscalYear());
            pendingEntry.setUniversityFiscalPeriodCode(YearEndDocumentUtil.getFINAL_ACCOUNTING_PERIOD());
        }

        return pendingEntry;
    }

    /**
     * convert the given document and accounting line into the expense pending entries for effort reporting
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense pending entries for effort reporting
     */
    public static LaborLedgerPendingEntry getExpenseA21PendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LaborLedgerPendingEntry pendingEntry = getExpensePendingEntry(document, accountingLine, sequenceHelper);

        pendingEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_A21);
        String debitCreditCode = DebitCreditUtil.getReverseDebitCreditCode(pendingEntry.getTransactionDebitCreditCode());
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);

        return pendingEntry;
    }

    /**
     * convert the given document and accounting line into the expense reversal pending entries for effort reporting
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense reversal pending entries for effort reporting
     */
    public static LaborLedgerPendingEntry getExpenseA21ReversalPendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LaborLedgerPendingEntry pendingEntry = getExpenseA21PendingEntry(document, accountingLine, sequenceHelper);

        pendingEntry.setUniversityFiscalYear(accountingLine.getPayrollEndDateFiscalYear());
        pendingEntry.setUniversityFiscalPeriodCode(accountingLine.getPayrollEndDateFiscalPeriodCode());

        String debitCreditCode = DebitCreditUtil.getReverseDebitCreditCode(pendingEntry.getTransactionDebitCreditCode());
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);

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
    public static LaborLedgerPendingEntry getBenefitPendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {
        LaborLedgerPendingEntry pendingEntry = getDefaultPendingEntry(document, accountingLine);

        Account account = SpringContext.getBean(AccountService.class).getByPrimaryId(accountingLine.getChartOfAccountsCode(),accountingLine.getAccountNumber());
        
        // if account doesn't accept fringe charges, use reports to account
        if (!account.isAccountsFringesBnftIndicator()) {
            pendingEntry.setChartOfAccountsCode(account.getReportsToChartOfAccountsCode());
            pendingEntry.setAccountNumber(account.getReportsToAccountNumber());
        }

        pendingEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
        pendingEntry.setFinancialObjectCode(pickValue(fringeBenefitObjectCode, KFSConstants.getDashFinancialObjectCode()));

        ObjectCode fringeObjectCode = SpringContext.getBean(ObjectCodeService.class).getByPrimaryId(accountingLine.getPayrollEndDateFiscalYear(), accountingLine.getChartOfAccountsCode(), fringeBenefitObjectCode);
        pendingEntry.setFinancialObjectTypeCode(fringeObjectCode.getFinancialObjectTypeCode());

        pendingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        pendingEntry.setTransactionLedgerEntryAmount(benefitAmount.abs());
        //  The line below this comment was pendingEntry.setPositionNumber(LaborConstants.getDashPositionNumber());
        pendingEntry.setPositionNumber(accountingLine.getPositionNumber());
        // The line below this comment was pendingEntry.setEmplid(LaborConstants.getDashEmplId());
        pendingEntry.setEmplid(accountingLine.getEmplid());
        pendingEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));

        // year end document should post to previous fiscal year and final period
        if (document instanceof YearEndDocument) {
            pendingEntry.setUniversityFiscalYear(YearEndDocumentUtil.getPreviousFiscalYear());
            pendingEntry.setUniversityFiscalPeriodCode(YearEndDocumentUtil.getFINAL_ACCOUNTING_PERIOD());
        }

        return pendingEntry;
    }

    /**
     * convert the given document and accounting line into the benefit pending entry for effort reporting
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @param benefitAmount the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit pending entries for effort reporting
     */
    public static LaborLedgerPendingEntry getBenefitA21PendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {
        LaborLedgerPendingEntry pendingEntry = getBenefitPendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);

        pendingEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_A21);
        String debitCreditCode = DebitCreditUtil.getReverseDebitCreditCode(pendingEntry.getTransactionDebitCreditCode());
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);

        return pendingEntry;
    }

    /**
     * convert the given document and accounting line into the benefit reversal pending entries for effort reporting
     * 
     * @param document the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @param benefitAmount the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit reversal pending entries for effort reporting
     */
    public static LaborLedgerPendingEntry getBenefitA21ReversalPendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {
        LaborLedgerPendingEntry pendingEntry = getBenefitA21PendingEntry(document, accountingLine, sequenceHelper, benefitAmount, fringeBenefitObjectCode);

        pendingEntry.setUniversityFiscalYear(accountingLine.getPayrollEndDateFiscalYear());
        pendingEntry.setUniversityFiscalPeriodCode(accountingLine.getPayrollEndDateFiscalPeriodCode());

        String debitCreditCode = DebitCreditUtil.getReverseDebitCreditCode(pendingEntry.getTransactionDebitCreditCode());
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);

        return pendingEntry;
    }

    /**
     * convert the given document into benefit clearing pending entries with the given account, chart, amount and benefit type
     * 
     * @param document the given accounting document
     * @param sequenceHelper the given sequence helper
     * @param accountNumber the given account number that the benefit clearing amount can be charged
     * @param chartOfAccountsCode the given chart of accounts code that the benefit clearing amount can be charged
     * @param benefitTypeCode the given benefit type code
     * @param clearingAmount the benefit clearing amount
     * @return a set of benefit clearing pending entries
     */
    public static LaborLedgerPendingEntry getBenefitClearingPendingEntry(LaborLedgerPostingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, String accountNumber, String chartOfAccountsCode, String objectCode, String benefitTypeCode, KualiDecimal clearingAmount) {
        LaborLedgerPendingEntry pendingEntry = getDefaultPendingEntry(document);

        pendingEntry.setChartOfAccountsCode(chartOfAccountsCode);
        pendingEntry.setAccountNumber(accountNumber);
        pendingEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        pendingEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);

        Integer fiscalYear = SpringContext.getBean(OptionsService.class).getCurrentYearOptions().getUniversityFiscalYear();
        BenefitsCalculation benefitsCalculation = SpringContext.getBean(LaborBenefitsCalculationService.class).getBenefitsCalculation(fiscalYear, chartOfAccountsCode, benefitTypeCode);
//        String objectCode = benefitsCalculation.getPositionFringeBenefitObjectCode();
        pendingEntry.setFinancialObjectCode(objectCode);

        ObjectCode oc = SpringContext.getBean(ObjectCodeService.class).getByPrimaryId(fiscalYear, chartOfAccountsCode, objectCode);
        pendingEntry.setFinancialObjectTypeCode(oc.getFinancialObjectTypeCode());

        pendingEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        pendingEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));

        String debitCreditCode = DebitCreditUtil.getDebitCreditCode(clearingAmount, false);
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);
        pendingEntry.setTransactionLedgerEntryAmount(clearingAmount.abs());

        pendingEntry.setProjectCode(KFSConstants.getDashProjectCode());
        
        //pendingEntry.setPositionNumber(LaborConstants.getDashPositionNumber());
        //pendingEntry.setEmplid(LaborConstants.getDashEmplId());
        // Per KFSPTS-285, KITI-2608, the desire is to put the actual employee id and position number values in the clearing account pending entry
        //   therefore, we can use the document object to get the first labor ledger pending entry just created and use it to obtain both
        //   the employee id and the position number for this particular document as follows:
        String myEmplId = document.getLaborLedgerPendingEntry(0).getEmplid();
        String myPositionNumber = document.getLaborLedgerPendingEntry(0).getPositionNumber();
        pendingEntry.setPositionNumber(myPositionNumber);
        pendingEntry.setEmplid(myEmplId);
        pendingEntry.setTransactionTotalHours(null);

        // year end document should post to previous fiscal year and final period
        if (document instanceof YearEndDocument) {
            pendingEntry.setUniversityFiscalYear(YearEndDocumentUtil.getPreviousFiscalYear());
            pendingEntry.setUniversityFiscalPeriodCode(YearEndDocumentUtil.getFINAL_ACCOUNTING_PERIOD());
        }

        return pendingEntry;
    }

    /**
     * construct a LaborLedgerPendingEntry object based on the information in the given document and accounting line. The object can
     * be used as a template
     * 
     * @param document the given document
     * @param accountingLine the given accounting line
     * @return a LaborLedgerPendingEntry object based on the information in the given document and accounting line
     */
    public static LaborLedgerPendingEntry getDefaultPendingEntry(LaborLedgerPostingDocument document, ExpenseTransferAccountingLine accountingLine) {
        LaborLedgerPendingEntry pendingEntry = getDefaultPendingEntry(document);

        pendingEntry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
        pendingEntry.setAccountNumber(accountingLine.getAccountNumber());
        pendingEntry.setFinancialObjectCode(accountingLine.getFinancialObjectCode());

        String subAccountNumber = pickValue(accountingLine.getSubAccountNumber(), KFSConstants.getDashSubAccountNumber());
        pendingEntry.setSubAccountNumber(subAccountNumber);

        String subObjectCode = pickValue(accountingLine.getFinancialSubObjectCode(), KFSConstants.getDashFinancialSubObjectCode());
        pendingEntry.setFinancialSubObjectCode(subObjectCode);

        String projectCode = pickValue(accountingLine.getProjectCode(), KFSConstants.getDashProjectCode());
        pendingEntry.setProjectCode(projectCode);

        accountingLine.refreshReferenceObject(KFSPropertyConstants.OBJECT_CODE);
        String objectTypeCode = accountingLine.getObjectCode().getFinancialObjectTypeCode();
        pendingEntry.setFinancialObjectTypeCode(objectTypeCode);

        KualiDecimal transactionAmount = accountingLine.getAmount();
        String debitCreditCode = DebitCreditUtil.getDebitCreditCodeForExpenseDocument(accountingLine);
        pendingEntry.setTransactionDebitCreditCode(debitCreditCode);
        pendingEntry.setTransactionLedgerEntryAmount(transactionAmount.abs());

        pendingEntry.setPositionNumber(accountingLine.getPositionNumber());
        pendingEntry.setEmplid(accountingLine.getEmplid());
        pendingEntry.setPayrollEndDateFiscalYear(accountingLine.getPayrollEndDateFiscalYear());
        pendingEntry.setPayrollEndDateFiscalPeriodCode(accountingLine.getPayrollEndDateFiscalPeriodCode());
        pendingEntry.setTransactionTotalHours(accountingLine.getPayrollTotalHours());
        pendingEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());

        return pendingEntry;
    }

    /**
     * construct a LaborLedgerPendingEntry object based on the information in the given document. The object can be used as a
     * template
     * 
     * @param document the given document
     * @return a LaborLedgerPendingEntry object based on the information in the given document
     */
    public static LaborLedgerPendingEntry getDefaultPendingEntry(LaborLedgerPostingDocument document) {
        LaborLedgerPendingEntry pendingEntry = getSimpleDefaultPendingEntry();
        DocumentHeader documentHeader = document.getDocumentHeader();

        String documentTypeCode = SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(document.getClass());
        pendingEntry.setFinancialDocumentTypeCode(documentTypeCode);

        pendingEntry.setDocumentNumber(documentHeader.getDocumentNumber());
        pendingEntry.setTransactionLedgerEntryDescription(documentHeader.getDocumentDescription());
        pendingEntry.setOrganizationDocumentNumber(documentHeader.getOrganizationDocumentNumber());

        return pendingEntry;
    }

    /**
     * construct a LaborLedgerPendingEntry object based on the information in the given document and accounting line. The object can
     * be used as a template
     * 
     * @param document the given document
     * @param accountingLine the given accounting line
     * @return a LaborLedgerPendingEntry object based on the information in the given document and accounting line
     */
    public static LaborLedgerPendingEntry getSimpleDefaultPendingEntry() {
        LaborLedgerPendingEntry pendingEntry = new LaborLedgerPendingEntry();

        pendingEntry.setUniversityFiscalYear(null);
        pendingEntry.setUniversityFiscalPeriodCode(null);

        String originationCode = SpringContext.getBean(HomeOriginationService.class).getHomeOrigination().getFinSystemHomeOriginationCode();
        pendingEntry.setFinancialSystemOriginationCode(originationCode);

        Date transactionDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        pendingEntry.setTransactionDate(transactionDate);

        pendingEntry.setFinancialDocumentReversalDate(null);
        pendingEntry.setReferenceFinancialSystemOriginationCode(null);
        pendingEntry.setReferenceFinancialDocumentNumber(null);
        pendingEntry.setReferenceFinancialDocumentTypeCode(null);

        return pendingEntry;
    }

    /**
     * Pick one from target and backup values based on the availability of target value
     * 
     * @param targetValue the given target value
     * @param backupValue the backup value of the target value
     * @return target value if it is not null; otherwise, return its backup
     */
    protected static String pickValue(String targetValue, String backupValue) {
        return StringUtils.isNotBlank(targetValue) ? targetValue : backupValue;
    }

    /**
     * This method gets the next sequence number and increments.
     * 
     * @param sequenceHelper the given sequence helper
     * @return the next sequence number and increments
     */
    protected static Integer getNextSequenceNumber(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        Integer nextSequenceNumber = sequenceHelper.getSequenceCounter();
        sequenceHelper.increment();

        return nextSequenceNumber;
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
        public static List<LaborLedgerPendingEntry> getOffsetPendingEntries(LaborLedgerPendingEntry pendingEntry, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
            List<LaborLedgerPendingEntry> offsetEntries = new ArrayList<LaborLedgerPendingEntry>();
            String benefitRateCategoryCode = SpringContext.getBean(LaborBenefitsCalculationService.class).getBenefitRateCategoryCode(pendingEntry.getChartOfAccountsCode(), pendingEntry.getAccountNumber(), pendingEntry.getSubAccountNumber());
            Collection<PositionObjectBenefit> positionObjectBenefits = SpringContext.getBean(LaborPositionObjectBenefitService.class).getPositionObjectBenefits(pendingEntry.getUniversityFiscalYear(), pendingEntry.getChartOfAccountsCode(), pendingEntry.getFinancialObjectCode());
            
            if (positionObjectBenefits == null || positionObjectBenefits.isEmpty()) {
                return offsetEntries;
            }
    
            for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
                Map<String, Object> fieldValues = new HashMap<String, Object>();
                fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, pendingEntry.getUniversityFiscalYear());
                fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, pendingEntry.getChartOfAccountsCode());
                fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
               fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, benefitRateCategoryCode);
                
                org.kuali.kfs.module.ld.businessobject.BenefitsCalculation benefitsCalculation = (org.kuali.kfs.module.ld.businessobject.BenefitsCalculation) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation.class, fieldValues);
                
                BenefitsCalculationExtension extension = (BenefitsCalculationExtension) benefitsCalculation.getExtension();
                
                String offsetAccount = extension.getAccountCodeOffset();
    
                if(ObjectUtils.isNull(benefitsCalculation)) { 
                    continue;
                }
    
                LaborLedgerPendingEntry offsetEntry = new LaborLedgerPendingEntry();
    
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
                KualiDecimal fringeBenefitPercent = benefitsCalculation.getPositionFringeBenefitPercent();
                KualiDecimal offsetAmount = fringeBenefitPercent.multiply(
                pendingEntry.getTransactionLedgerEntryAmount()).divide(KFSConstants.ONE_HUNDRED.kualiDecimalValue());
                offsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());
                
                
                offsetEntry.setAccountNumber(extension.getAccountCodeOffset());
                offsetEntry.setFinancialObjectCode(extension.getObjectCodeOffset());
                
                //Set all the fields required to process through the scrubber and poster jobs
                offsetEntry.setUniversityFiscalPeriodCode(pendingEntry.getUniversityFiscalPeriodCode());
                offsetEntry.setChartOfAccountsCode(pendingEntry.getChartOfAccountsCode());
                offsetEntry.setUniversityFiscalYear(pendingEntry.getUniversityFiscalYear());
                offsetEntry.setSubAccountNumber("-----");
                offsetEntry.setFinancialSubObjectCode("---");
                offsetEntry.setOrganizationReferenceId("");
                offsetEntry.setProjectCode("");
                
                offsetEntry.setTransactionLedgerEntryDescription("GENERATED BENEFIT OFFSET");
                
                ParameterService parameterService = SpringContext.getBean(ParameterService.class);
                
                String originCode = parameterService.getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_ORIGIN_CODE);
                
                offsetEntry.setFinancialSystemOriginationCode(originCode);
                DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
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
                if(StringUtils.isNotEmpty(parameterService.getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE))) {
                	offsetDocTypes = "," + parameterService.getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE).replace(";", ",").replace("|", ",") + ",";
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
