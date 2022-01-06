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
package org.kuali.kfs.module.ar.document.validation.impl;

/**
 * FINP-5295: File backported. File will needed to be removed when 2021-04-29 KualiCo patch is applied.
 */

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoiceGeneralDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedRouteDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedSaveDocumentEvent;

public class ContractsGrantsInvoiceDocumentValidation extends GenericValidation {

    private ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService;
    private ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        if (contractsGrantsInvoiceDocument.isCorrectionDocument()) {
            return true;
        }

        boolean isValid = validateTransmissionDetails(event);
        isValid &= hasTemplate(event);
        isValid &= hasIncomeAndReceivableObjectCodes(event);
        isValid &= validatePositiveInvoiceTotalAmount(event);

        return isValid;
    }

    private boolean validateTransmissionDetails(AttributedDocumentEvent event) {
        if (contractsGrantsInvoiceDocument.getInvoiceAddressDetails().stream()
                .noneMatch(detail -> detail.isSendIndicator() || detail.isQueued() || detail.isSent())) {
            if (event instanceof AttributedSaveDocumentEvent) {
                GlobalVariables.getMessageMap().putWarning("document.invoiceAddressDetails[0].sendIndicator",
                        ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_ONE_TRANSMISSION_DETAIL_QUEUE_REQUIRED);
            } else {
                GlobalVariables.getMessageMap().putError("document.invoiceAddressDetails[0].sendIndicator",
                        ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_ONE_TRANSMISSION_DETAIL_QUEUE_REQUIRED);
                GlobalVariables.getMessageMap().getWarningMessages()
                        .remove("document.invoiceAddressDetails[0].sendIndicator");
                return false;
            }
        }
        return true;
    }

    private boolean hasTemplate(AttributedDocumentEvent event) {
        InvoiceGeneralDetail invoiceGeneralDetail = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail();
        if (StringUtils.isBlank(invoiceGeneralDetail.getCustomerInvoiceTemplateCode())) {
            if (event instanceof AttributedSaveDocumentEvent) {
                GlobalVariables.getMessageMap().putWarning("document.invoiceGeneralDetail.customerInvoiceTemplateCode",
                        ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_TEMPLATE_REQUIRED);
            } else {
                GlobalVariables.getMessageMap().putError("document.invoiceGeneralDetail.customerInvoiceTemplateCode",
                        ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_TEMPLATE_REQUIRED);
                GlobalVariables.getMessageMap().getWarningMessages()
                        .remove("document.invoiceGeneralDetail.customerInvoiceTemplateCode");
                return false;
            }
        }

        return true;
    }

    private boolean hasIncomeAndReceivableObjectCodes(AttributedDocumentEvent event) {
        boolean isValid = true;
        if (event instanceof AttributedRouteDocumentEvent) {
            int lineCount = contractsGrantsInvoiceDocument.getSourceAccountingLines().size();
            for (int idx = 0; idx < lineCount; idx++) {
                CustomerInvoiceDetail cid = (CustomerInvoiceDetail) contractsGrantsInvoiceDocument
                        .getSourceAccountingLines().get(idx);
                if (cid.getFinancialObjectCode() == null || cid.getAccountsReceivableObjectCode() == null) {
                    isValid = false;
                    // remove Data Dictionary required field validation error
                    // the message is displaced and would show at the bottom of the screen since the accounting lines
                    // on CINV are not rendered
                    GlobalVariables.getMessageMap().removeAllErrorMessagesForProperty("document.sourceAccountingLines["
                            + idx + "].financialObjectCode");
                }
            }
            if (!isValid) {
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        ArKeyConstants.ContractsGrantsInvoiceConstants
                                .ERROR_SUBMIT_NO_MATCHING_CONTRACT_GRANTS_INVOICE_OBJECT_CODE);
            }
        }
        return isValid;
    }

    private boolean validatePositiveInvoiceTotalAmount(AttributedDocumentEvent event) {
        boolean isValid = true;
        if (event instanceof AttributedRouteDocumentEvent) {
            if (contractsGrantsInvoiceDocument.getTotalInvoiceAmount().isPositive()
                    || getContractsGrantsInvoiceDocumentService().getInvoiceMilestoneTotal(contractsGrantsInvoiceDocument).isPositive()
                    || getContractsGrantsInvoiceDocumentService().getBillAmountTotal(contractsGrantsInvoiceDocument).isPositive()) {
                isValid = true;
            } else {
                isValid = false;
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                        ArKeyConstants.ContractsGrantsInvoiceConstants.ERROR_INVOICE_TOTAL_AMOUNT_INVALID);
            }
        }
        return isValid;
    }

    public void setContractsGrantsInvoiceDocument(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        this.contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocument;
    }

    public ContractsGrantsInvoiceDocumentService getContractsGrantsInvoiceDocumentService() {
        return contractsGrantsInvoiceDocumentService;
    }

    public void setContractsGrantsInvoiceDocumentService(ContractsGrantsInvoiceDocumentService contractsGrantsInvoiceDocumentService) {
        this.contractsGrantsInvoiceDocumentService = contractsGrantsInvoiceDocumentService;
    }
}
