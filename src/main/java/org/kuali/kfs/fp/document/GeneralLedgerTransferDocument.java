/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.fp.document;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferSourceAccountingLine;
import org.kuali.kfs.fp.document.service.GeneralLedgerTransferService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.krad.rules.rule.event.BlanketApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.service.OptionsService;

import java.sql.Timestamp;

/*
 * Backported KualiCo fix:
 * Added the FINP-7382 fix from the 2021-03-11 financials patch. This can be removed once we upgrade to this release.
 */
public class GeneralLedgerTransferDocument extends CapitalAccountingLinesDocumentBase implements AmountTotaling,
    CapitalAssetEditable {

    private static final String FULL_ROUTING_SPLIT = "DoFullRoutingSplit";

    private String expenditureDescription;
    private String expenditureProjectBenefit;
    private String lateAdjustmentDescription;
    private String lateAdjustmentReason;
    private String lateAdjustmentActionDescription;
    private Timestamp batchProcessedDate;

    private transient Integer universityFiscalYear;
    private transient String chartOfAccountsCode;
    private transient String accountNumber;
    private transient String financialObjectCode;
    private transient String lookupDocumentNumber;

    private transient AccountService accountService;
    private transient ChartService chartService;
    private transient ObjectCodeService objectCodeService;
    private transient OptionsService optionsService;
    private transient GeneralLedgerTransferService generalLedgerTransferService;

    public GeneralLedgerTransferDocument() {
        setUniversityFiscalYear(getOptionsService().getCurrentYearOptions().getUniversityFiscalYear());
    }

    @Override
    public Class getSourceAccountingLineClass() {
        return GeneralLedgerTransferSourceAccountingLine.class;
    }

    @Override
    public void postProcessSave(KualiDocumentEvent event) {
        super.postProcessSave(event);

        if (event instanceof RouteDocumentEvent || event instanceof BlanketApproveDocumentEvent) {
            getGeneralLedgerTransferService().updateGeneralLedgerTransferEntry(getSourceAccountingLines(), documentNumber);
        }
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        if (statusChangeRequiringGeneralLedgerEntryDocumentNumberRemoval(workflowDocument, statusChangeEvent)) {
            getGeneralLedgerTransferService().updateGeneralLedgerTransferEntry(getSourceAccountingLines(), null);
        }
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(FULL_ROUTING_SPLIT)) {
            return getGeneralLedgerTransferService().newAccountOnTarget(getSourceAccountingLines(), getTargetAccountingLines())
                || getGeneralLedgerTransferService().newObjectCodeOnTarget(getSourceAccountingLines(), getTargetAccountingLines())
                || getGeneralLedgerTransferService().costSharePresent(getSourceAccountingLines(), getTargetAccountingLines());
        }

        return super.answerSplitNodeQuestion(nodeName);
    }

    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        AccountingLineBase accountingLine = (AccountingLineBase) postable;

        return KFSConstants.GL_DEBIT_CODE.equals(accountingLine.getDebitCreditCode());
    }

    public String getExpenditureDescription() {
        return expenditureDescription;
    }

    public void setExpenditureDescription(String expenditureDescription) {
        this.expenditureDescription = expenditureDescription;
    }

    public String getExpenditureProjectBenefit() {
        return expenditureProjectBenefit;
    }

    public void setExpenditureProjectBenefit(String expenditureProjectBenefit) {
        this.expenditureProjectBenefit = expenditureProjectBenefit;
    }

    public String getLateAdjustmentDescription() {
        return lateAdjustmentDescription;
    }

    public void setLateAdjustmentDescription(String lateAdjustmentDescription) {
        this.lateAdjustmentDescription = lateAdjustmentDescription;
    }

    public String getLateAdjustmentReason() {
        return lateAdjustmentReason;
    }

    public void setLateAdjustmentReason(String lateAdjustmentReason) {
        this.lateAdjustmentReason = lateAdjustmentReason;
    }

    public String getLateAdjustmentActionDescription() {
        return lateAdjustmentActionDescription;
    }

    public void setLateAdjustmentActionDescription(String lateAdjustmentActionDescription) {
        this.lateAdjustmentActionDescription = lateAdjustmentActionDescription;
    }

    public Timestamp getBatchProcessedDate() {
        return batchProcessedDate;
    }

    public void setBatchProcessedDate(Timestamp batchProcessedDate) {
        this.batchProcessedDate = batchProcessedDate;
    }

    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }

    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }

    public String getChartOfAccountsCode() {
        if (!getAccountService().accountsCanCrossCharts() && accountNumber != null) {
            Account account = getAccount();
            chartOfAccountsCode = ObjectUtils.isNotNull(account) ? account.getChartOfAccountsCode() : null;
        }

        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getLookupDocumentNumber() {
        return lookupDocumentNumber;
    }

    public void setLookupDocumentNumber(String lookupDocumentNumber) {
        this.lookupDocumentNumber = lookupDocumentNumber;
    }

    public Chart getChart() {
        return getChartService().getByPrimaryId(chartOfAccountsCode);
    }

    public Account getAccount() {
        if (!getAccountService().accountsCanCrossCharts()) {
            return getAccountService().getUniqueAccountForAccountNumber(accountNumber);
        } else {
            return getAccountService().getByPrimaryId(chartOfAccountsCode, accountNumber);
        }
    }

    public ObjectCode getObjectCode() {
        return getObjectCodeService().getByPrimaryId(universityFiscalYear, chartOfAccountsCode, financialObjectCode);
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = SpringContext.getBean(AccountService.class);
        }

        return accountService;
    }

    public ChartService getChartService() {
        if (chartService == null) {
            chartService = SpringContext.getBean(ChartService.class);
        }

        return chartService;
    }

    public ObjectCodeService getObjectCodeService() {
        if (objectCodeService == null) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }

        return objectCodeService;
    }

    public OptionsService getOptionsService() {
        if (optionsService == null) {
            optionsService = SpringContext.getBean(OptionsService.class);
        }

        return optionsService;
    }

    public GeneralLedgerTransferService getGeneralLedgerTransferService() {
        if (generalLedgerTransferService == null) {
            generalLedgerTransferService = SpringContext.getBean(GeneralLedgerTransferService.class);
        }

        return generalLedgerTransferService;
    }

    protected boolean statusChangeRequiringGeneralLedgerEntryDocumentNumberRemoval(WorkflowDocument workflowDocument,
            DocumentRouteStatusChange statusChangeEvent) {
        return workflowDocument.isCanceled() || workflowDocument.isDisapproved() || workflowDocument.isRecalled()
                || workflowDocument.isException() || documentIsBeingRecalled(statusChangeEvent);
    }

    private boolean documentIsBeingRecalled(DocumentRouteStatusChange eventStatusChange) {
        return eventStatusChange.getOldRouteStatus().equals(DocumentStatus.ENROUTE.getCode())
                && eventStatusChange.getNewRouteStatus().equals(DocumentStatus.SAVED.getCode());
    }
}
