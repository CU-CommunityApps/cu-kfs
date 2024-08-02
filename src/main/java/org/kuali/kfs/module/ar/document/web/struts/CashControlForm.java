/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.ar.document.web.struts;

import edu.cornell.kfs.module.ar.CuArParameterConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.krad.service.SessionDocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArParameterConstants;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.document.CashControlDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;

import javax.servlet.http.HttpServletRequest;

public class CashControlForm extends FinancialSystemTransactionalDocumentFormBase {

    private static final Logger LOG = LogManager.getLogger();

    protected CashControlDetail newCashControlDetail;
    protected String processingChartOfAccCodeAndOrgCode;
    protected boolean cashPaymentMediumSelected;

    private FormFile detailImportFile;

    public CashControlForm() {
        super();
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return ArConstants.ArDocumentTypeCodes.CASH_CONTROL;
    }

    @Override
    public void populate(final HttpServletRequest request) {
        super.populate(request);

        if (hasDocumentId()) {
            final CashControlDocument ccDoc = getCashControlDocument();

            // apply populate to PaymentApplicationDocuments
            for (final CashControlDetail cashControlDetail : ccDoc.getCashControlDetails()) {
                if (!cashControlDetail.getReferenceFinancialDocument().getDocumentHeader().hasWorkflowDocument()) {
                    // populate workflowDocument in documentHeader, if needed
                    WorkflowDocument workflowDocument = SpringContext.getBean(SessionDocumentService.class)
                            .getDocumentFromSession(GlobalVariables.getUserSession(),
                                    cashControlDetail.getReferenceFinancialDocumentNumber());

                    if (workflowDocument == null) {
                        // gets the workflow document from doc service
                        workflowDocument = SpringContext.getBean(WorkflowDocumentService.class)
                                .loadWorkflowDocument(cashControlDetail.getReferenceFinancialDocumentNumber(),
                                        GlobalVariables.getUserSession().getPerson());
                        SpringContext.getBean(SessionDocumentService.class)
                                .addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDocument);
                        if (workflowDocument == null) {
                            throw new RuntimeException("Unable to retrieve document # " +
                                    cashControlDetail.getReferenceFinancialDocumentNumber() +
                                    " from document service getByDocumentHeaderId");
                        }
                    }

                    cashControlDetail.getReferenceFinancialDocument().getDocumentHeader()
                            .setWorkflowDocument(workflowDocument);
                }
            }
        }
    }

    public CashControlDocument getCashControlDocument() {
        return (CashControlDocument) getDocument();
    }

    public CashControlDetail getNewCashControlDetail() {
        if (newCashControlDetail == null) {
            newCashControlDetail = new CashControlDetail();
        }
        return newCashControlDetail;
    }

    public void setNewCashControlDetail(final CashControlDetail newCashControlDetail) {
        this.newCashControlDetail = newCashControlDetail;
    }

    public String getProcessingChartOfAccCodeAndOrgCode() {
        return getCashControlDocument().getAccountsReceivableDocumentHeader()
                .getProcessingChartOfAccCodeAndOrgCode();
    }

    public void setProcessingChartOfAccCodeAndOrgCode(final String processingChartOfAccCodeAndOrgCode) {
        this.processingChartOfAccCodeAndOrgCode = processingChartOfAccCodeAndOrgCode;
    }

    public boolean isCashPaymentMediumSelected() {
        return cashPaymentMediumSelected;
    }

    public void setCashPaymentMediumSelected(final boolean cashPaymentMediumSelected) {
        this.cashPaymentMediumSelected = cashPaymentMediumSelected;
    }

    public FormFile getDetailImportFile() {
        return detailImportFile;
    }

    public void setDetailImportFile(final FormFile detailImportFile) {
        this.detailImportFile = detailImportFile;
    }

    public String getDetailsImportInstructionsUrl() {
        return getParameterService().getParameterValueAsString(CashControlDocument.class,
                CuArParameterConstants.DETAILS_IMPORT
        );
    }

}