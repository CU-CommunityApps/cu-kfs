/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.ld.document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.module.ld.LaborParameterConstants;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.businessobject.LateAdjustment;
import org.kuali.kfs.module.ld.util.LaborPendingEntryGenerator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Labor Document Class for the Salary Expense Transfer Document.
 */
public class SalaryExpenseTransferDocument extends LaborExpenseTransferDocumentBase implements LateAdjustable {

    private static final Logger LOG = LogManager.getLogger();
    
    private transient BusinessObjectDictionaryService businessObjectDictionaryService;
    protected Map<String, KualiDecimal> approvalObjectCodeBalances;
    protected LateAdjustment lateAdjustment;

    public SalaryExpenseTransferDocument() {
        super();
        approvalObjectCodeBalances = new HashMap<>();
    }

    public Map<String, KualiDecimal> getApprovalObjectCodeBalances() {
        return approvalObjectCodeBalances;
    }

    public void setApprovalObjectCodeBalances(Map<String, KualiDecimal> approvalObjectCodeBalances) {
        this.approvalObjectCodeBalances = approvalObjectCodeBalances;
    }

    @Override
    public LateAdjustment getLateAdjustment() {
        return lateAdjustment;
    }

    @Override
    public void setLateAdjustment(LateAdjustment lateAdjustment) {
        this.lateAdjustment = lateAdjustment;
    }

    @Override
    public boolean generateLaborLedgerPendingEntries(AccountingLine accountingLine,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerPendingEntries()");

        boolean isSuccessful = true;
        ExpenseTransferAccountingLine expenseTransferAccountingLine = (ExpenseTransferAccountingLine) accountingLine;

        List<LaborLedgerPendingEntry> expensePendingEntries = LaborPendingEntryGenerator
                .generateExpensePendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (expensePendingEntries != null && !expensePendingEntries.isEmpty()) {
            isSuccessful = this.getLaborLedgerPendingEntries().addAll(expensePendingEntries);
        }

        List<LaborLedgerPendingEntry> benefitPendingEntries = LaborPendingEntryGenerator
                .generateBenefitPendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (benefitPendingEntries != null && !benefitPendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(benefitPendingEntries);
        }

        return isSuccessful;
    }

    @Override
    public boolean generateLaborLedgerBenefitClearingPendingEntries(
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerBenefitClearingPendingEntries()");

        String chartOfAccountsCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                SalaryExpenseTransferDocument.class,
                LaborParameterConstants.BENEFIT_CLEARING_CHART);
        String accountNumber = SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                SalaryExpenseTransferDocument.class,
                LaborParameterConstants.BENEFIT_CLEARING_ACCOUNT);

        List<LaborLedgerPendingEntry> benefitClearingPendingEntries =
                LaborPendingEntryGenerator.generateBenefitClearingPendingEntries(this, sequenceHelper, accountNumber,
                        chartOfAccountsCode);

        if (benefitClearingPendingEntries != null && !benefitClearingPendingEntries.isEmpty()) {
            return this.getLaborLedgerPendingEntries().addAll(benefitClearingPendingEntries);
        }

        return true;
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        // KFSMI-4606 added routeNode condition
        if (nodeName.equals(KFSConstants.REQUIRES_WORK_STUDY_REVIEW)) {
            return checkOjbectCodeForWorkstudy();
        } else {
            return super.answerSplitNodeQuestion(nodeName);
        }
    }

    /**
     * KFSMI-4606 check routeNode condition
     *
     * @return boolean
     */
    protected boolean checkOjbectCodeForWorkstudy() {
        Collection<String> workstudyRouteObjectcodes = SpringContext.getBean(ParameterService.class)
                .getParameterValuesAsString(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class,
                        KFSConstants.WORK_STUDY_ROUTE_OBJECT_CODES_PARAM_NM);

        List<SourceAccountingLine> sourceAccountingLines = getSourceAccountingLines();
        List<TargetAccountingLine> targetAccountingLines = getTargetAccountingLines();

        // check object code in source and target accounting lines
        for (SourceAccountingLine sourceLine : sourceAccountingLines) {
            if (workstudyRouteObjectcodes.contains(sourceLine.getFinancialObjectCode())) {
                return true;
            }
        }

        for (TargetAccountingLine targetLine : targetAccountingLines) {
            if (workstudyRouteObjectcodes.contains(targetLine.getFinancialObjectCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * KFSMI-4606 Set GLPE descriptions to persons name. Take care that this needs to overwrite prepareForSave so that
     * it catches pending entries generated by generateLaborLedgerPendingEntries and
     * generateLaborLedgerBenefitClearingPendingEntries.
     */
    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        super.prepareForSave(event);

//        for (Iterator<LaborLedgerPendingEntry> iterator = this.getLaborLedgerPendingEntries().iterator(); iterator.hasNext();) {
//            LaborLedgerPendingEntry laborLedgerPendingEntry = iterator.next();
//
//            String personName = SpringContext.getBean(FinancialSystemUserService.class).getPersonNameByEmployeeId(this.getEmplid());
//
//            // Get the maxlength of the description field we are setting
//             BusinessObjectEntry laborLedgerPendingEntryBusinessObjectEntry = getBusinessObjectDictionaryService()
//                    .getBusinessObjectEntry(LaborLedgerPendingEntry.class.getName());
//            AttributeDefinition laborLedgerPendingEntryAttribute = laborLedgerPendingEntryBusinessObjectEntry.getAttributeDefinition(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC);
//            int descriptionLength = laborLedgerPendingEntryAttribute.getMaxLength();
//
//            // Set the description field truncating name if necessary
//            laborLedgerPendingEntry.setTransactionLedgerEntryDescription(personName.length() > descriptionLength ? personName.substring(0, descriptionLength - 1) : personName);
//        }

        if (lateAdjustment != null) {
            lateAdjustment.setDocumentNumber(this.documentNumber);
        }
    }

    @Override
    public List getLaborLedgerPendingEntriesForSearching() {
        return super.getLaborLedgerPendingEntries();
    }
    
    public BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = KNSServiceLocator.getBusinessObjectDictionaryService();
        }
        return businessObjectDictionaryService;
    }

}
