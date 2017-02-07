/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2017 Kuali, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
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

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.businessobject.ErrorCertification;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
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
public class SalaryExpenseTransferDocument extends LaborExpenseTransferDocumentBase implements ErrorCertifiable {
    protected static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(SalaryExpenseTransferDocument.class);

    protected Map<String, KualiDecimal> approvalObjectCodeBalances;
    protected ErrorCertification errorCertification;

    /**
     * Default Constructor.
     */
    public SalaryExpenseTransferDocument() {
        super();
        approvalObjectCodeBalances = new HashMap<String, KualiDecimal>();
    }

    /**
     * Gets the approvalObjectCodeBalances attribute.
     *
     * @return Returns the approvalObjectCodeBalances.
     */
    public Map<String, KualiDecimal> getApprovalObjectCodeBalances() {
        return approvalObjectCodeBalances;
    }

    /**
     * Sets the approvalObjectCodeBalances attribute value.
     *
     * @param approvalObjectCodeBalances The approvalObjectCodeBalances to set.
     */
    public void setApprovalObjectCodeBalances(Map<String, KualiDecimal> approvalObjectCodeBalances) {
        this.approvalObjectCodeBalances = approvalObjectCodeBalances;
    }

    /**
     * Gets the errorCertification attribute.
     *
     * @return Returns the errorCertification.
     */
    @Override
    public ErrorCertification getErrorCertification() {
        return errorCertification;
    }

    /**
     * Sets the errorCertification attribute value.
     *
     * @param errorCertification The errorCertification to set.
     */
    @Override
    public void setErrorCertification(ErrorCertification errorCertification) {
        this.errorCertification = errorCertification;
    }

    /**
     * @see org.kuali.kfs.module.ld.document.LaborExpenseTransferDocumentBase#generateLaborLedgerPendingEntries(org.kuali.kfs.sys.businessobject.AccountingLine,
     *      org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean generateLaborLedgerPendingEntries(AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerPendingEntries()");

        boolean isSuccessful = true;
        ExpenseTransferAccountingLine expenseTransferAccountingLine = (ExpenseTransferAccountingLine) accountingLine;

        List<LaborLedgerPendingEntry> expensePendingEntries = LaborPendingEntryGenerator.generateExpensePendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (expensePendingEntries != null && !expensePendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(expensePendingEntries);
        }

        List<LaborLedgerPendingEntry> benefitPendingEntries = LaborPendingEntryGenerator.generateBenefitPendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (benefitPendingEntries != null && !benefitPendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(benefitPendingEntries);
        }

        return isSuccessful;
    }

    /**
     * @see org.kuali.kfs.module.ld.document.LaborExpenseTransferDocumentBase#generateLaborLedgerBenefitClearingPendingEntries(org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean generateLaborLedgerBenefitClearingPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerBenefitClearingPendingEntries()");

        String chartOfAccountsCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(SalaryExpenseTransferDocument.class, LaborConstants.SalaryExpenseTransfer.BENEFIT_CLEARING_CHART_PARM_NM);
        String accountNumber = SpringContext.getBean(ParameterService.class).getParameterValueAsString(SalaryExpenseTransferDocument.class, LaborConstants.SalaryExpenseTransfer.BENEFIT_CLEARING_ACCOUNT_PARM_NM);

        List<LaborLedgerPendingEntry> benefitClearingPendingEntries = LaborPendingEntryGenerator.generateBenefitClearingPendingEntries(this, sequenceHelper, accountNumber, chartOfAccountsCode);

        if (benefitClearingPendingEntries != null && !benefitClearingPendingEntries.isEmpty()) {
            return this.getLaborLedgerPendingEntries().addAll(benefitClearingPendingEntries);
        }

        return true;
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        // KFSMI-4606 added routeNode condition
        if (nodeName.equals(KFSConstants.REQUIRES_WORKSTUDY_REVIEW)) {
            return checkOjbectCodeForWorkstudy();
        }
        else {
            return super.answerSplitNodeQuestion(nodeName);
        }
    }

    /**
     * KFSMI-4606 check routeNode condition
     *
     * @return boolean
     */
    protected boolean checkOjbectCodeForWorkstudy(){
        Collection<String> workstudyRouteObjectcodes = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, KFSConstants.WORKSTUDY_ROUTE_OBJECT_CODES_PARM_NM);

        List<SourceAccountingLine> sourceAccountingLines = getSourceAccountingLines();
        List<TargetAccountingLine> targetAccountingLines = getTargetAccountingLines();

        // check object code in source and target accounting lines
        for (SourceAccountingLine sourceLine : sourceAccountingLines){
            if (workstudyRouteObjectcodes.contains(sourceLine.getFinancialObjectCode())) {
                return true;
            }
        }

        for (TargetAccountingLine targetLine : targetAccountingLines){
            if (workstudyRouteObjectcodes.contains(targetLine.getFinancialObjectCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * KFSMI-4606 Set GLPE descriptions to persons name. Take care that this needs to overwrite prepareForSave so that it
     * catches pending entries generated by generateLaborLedgerPendingEntries and generateLaborLedgerBenefitClearingPendingEntries.
     *
     * @see org.kuali.kfs.module.ld.document.LaborLedgerPostingDocumentBase#prepareForSave(org.kuali.kfs.kns.rule.event.KualiDocumentEvent)
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
//            BusinessObjectEntry laborLedgerPendingEntryBusinessObjectEntry = getDataDictionaryService().getDataDictionary().getBusinessObjectEntry(LaborLedgerPendingEntry.class.getName());
//            AttributeDefinition laborLedgerPendingEntryAttribute = laborLedgerPendingEntryBusinessObjectEntry.getAttributeDefinition(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC);
//            int descriptionLength = laborLedgerPendingEntryAttribute.getMaxLength();
//
//            // Set the description field truncating name if necessary
//            laborLedgerPendingEntry.setTransactionLedgerEntryDescription(personName.length() > descriptionLength ? personName.substring(0, descriptionLength - 1) : personName);
//        }

        // KFSCNTRB-846 Need to set doc number on Error Certification object because it's the primary key; otherwise OJB complains
        if (errorCertification != null) {
            errorCertification.setDocumentNumber(this.documentNumber);
        }
    }

    @Override
    public List getLaborLedgerPendingEntriesForSearching() {
        return super.getLaborLedgerPendingEntries();
    }

}
