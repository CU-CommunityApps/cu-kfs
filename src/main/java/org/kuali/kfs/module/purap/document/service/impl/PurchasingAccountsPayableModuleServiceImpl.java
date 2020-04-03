/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.CreditMemoService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;

import java.sql.Date;
import java.util.HashMap;

public class PurchasingAccountsPayableModuleServiceImpl implements PurchasingAccountsPayableModuleService {

    private static final Logger LOG = LogManager.getLogger();

    protected BusinessObjectService businessObjectService;
    protected DocumentService documentService;
    protected PaymentRequestService paymentRequestService;
    protected PurapService purapService;
    protected PurchaseOrderService purchaseOrderService;
    protected RequisitionService requisitionService;
    //CU customization change from private to protected
    protected ParameterService parameterService;
    protected CreditMemoService creditMemoService;
    private NoteService noteService;
    private AccountsPayableService accountsPayableService;

    @Override
    public void addAssignedAssetNumbers(Integer purchaseOrderNumber, String principalId, String noteText) {
        PurchaseOrderDocument document = purchaseOrderService.getCurrentPurchaseOrder(purchaseOrderNumber);

        try {
            Note assetNote = documentService.createNoteFromDocument(document, noteText);
            assetNote.setAuthorUniversalIdentifier(principalId);
            document.addNote(assetNote);
            noteService.save(assetNote);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPurchaseOrderInquiryUrl(Integer purchaseOrderNumber) {
        PurchaseOrderDocument po = purchaseOrderService.getCurrentPurchaseOrder(purchaseOrderNumber);
        if (ObjectUtils.isNotNull(po)) {
            return "purapPurchaseOrder.do?methodToCall=docHandler&docId=" + po.getDocumentNumber() +
                    "&command=displayDocSearchView";
        } else {
            return "";
        }
    }

    @Override
    public boolean isPurchasingBatchDocument(String documentTypeCode) {
        return PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)
                || PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode);
    }

    @Override
    public void handlePurchasingBatchCancels(String documentNumber, String documentTypeCode, boolean primaryCancel,
            boolean disbursedPayment) {
        LOG.info("Begin handlePurchasingBatchCancels(documentNumber=" + documentNumber + ", documentTypeCode=" +
                documentTypeCode + ", primaryCancel=" + primaryCancel + ", disbursedPayment=" + disbursedPayment);

        String preqCancelNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_CANCEL_NOTE);
        String preqResetNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_RESET_NOTE);
        String cmCancelNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_CANCEL_NOTE);
        String cmResetNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_RESET_NOTE);

        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                if (disbursedPayment || primaryCancel) {
                    paymentRequestService.cancelExtractedPaymentRequest(pr, preqCancelNote);
                } else {
                    paymentRequestService.resetExtractedPaymentRequest(pr, preqResetNote);
                }
            } else {
                LOG.error("processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Payment Request with doc type of " +
                        documentTypeCode + " with id " + documentNumber);
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                if (disbursedPayment || primaryCancel) {
                    creditMemoService.cancelExtractedCreditMemo(cm, cmCancelNote);
                } else {
                    creditMemoService.resetExtractedCreditMemo(cm, cmResetNote);
                }
            } else {
                LOG.error("processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of " +
                        documentTypeCode + " with id " + documentNumber);
            }
        }
    }

    @Override
    public void handlePurchasingBatchPaids(String documentNumber, String documentTypeCode, Date processDate) {
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                paymentRequestService.markPaid(pr, processDate);
            } else {
                LOG.error("processPdpPaids() DOES NOT EXIST, CANNOT MARK - Payment Request with doc type of " +
                        documentTypeCode + " with id " + documentNumber);
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                creditMemoService.markPaid(cm, processDate);
            } else {
                LOG.error("processPdpPaids() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of " +
                        documentTypeCode + " with id " + documentNumber);
            }
        }
    }

    @Override
    public void handlePurchasingBatchReissue(String documentNumber, String documentTypeCode) {
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            AccountsPayableDocument accountsPayableDocument = paymentRequestService
                    .getPaymentRequestByDocumentNumber(documentNumber);
            if (ObjectUtils.isNotNull(accountsPayableDocument)
                && PurapConstants.PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE.equals(
                        accountsPayableDocument.getApplicationDocumentStatus())) {
                final String preqReissueNote = parameterService.getParameterValueAsString(
                        PaymentRequestDocument.class, PurapParameterConstants.PURAP_PDP_REISSUE_NOTE);

                createNoteAndRevertToPreviousAppDocStatus(documentNumber, documentTypeCode, preqReissueNote,
                        accountsPayableDocument);
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            AccountsPayableDocument accountsPayableDocument = creditMemoService.getCreditMemoByDocumentNumber(
                    documentNumber);
            if (ObjectUtils.isNotNull(accountsPayableDocument)
                    && PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_POST_AP_APPROVE.equals(
                            accountsPayableDocument.getApplicationDocumentStatus())) {
                final String cmReissueNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                        PurapParameterConstants.PURAP_PDP_REISSUE_NOTE);

                createNoteAndRevertToPreviousAppDocStatus(documentNumber, documentTypeCode, cmReissueNote,
                        accountsPayableDocument);
            }
        }
    }

    private void createNoteAndRevertToPreviousAppDocStatus(String documentNumber, String documentTypeCode,
            String preqReissueNote, AccountsPayableDocument accountsPayableDocument) {
        if (ObjectUtils.isNotNull(accountsPayableDocument)) {
            Note cancelNote = documentService.createNoteFromDocument(accountsPayableDocument, preqReissueNote);
            cancelNote.setAuthorUniversalIdentifier(getSystemUserPrincipalId());
            accountsPayableDocument.addNote(cancelNote);
            noteService.save(cancelNote);
            accountsPayableService.revertToPreviousAppDocStatus(accountsPayableDocument);
        } else {
            LOG.error("DOCUMENT DOES NOT EXIST, CANNOT PROCESS - doc type of " + documentTypeCode + " with id " +
                    documentNumber);
        }
    }

    String getSystemUserPrincipalId() {
        return KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(KFSConstants.SYSTEM_USER)
                .getPrincipalId();
    }

    @Override
    public String determineRelatedRequisitionInitiatorPrincipalId(Document document) {
        if (document instanceof PurchasingAccountsPayableDocumentBase) {
            Integer requisitionIdentifier = null;

            if (document instanceof AccountsPayableDocument) {
                PurchaseOrderDocument purchaseOrder = ((AccountsPayableDocument) document).getPurchaseOrderDocument();
                if (ObjectUtils.isNotNull(purchaseOrder)) {
                    requisitionIdentifier = purchaseOrder.getRequisitionIdentifier();
                }
            } else if (document instanceof PurchaseOrderDocument) {
                requisitionIdentifier = ((PurchaseOrderDocument) document).getRequisitionIdentifier();
            }

            if (ObjectUtils.isNotNull(requisitionIdentifier)) {
                RequisitionDocument requisitionDocument = requisitionService.getRequisitionById(requisitionIdentifier);
                return requisitionDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
            }
        }

        return null;
    }

    @Override
    public HashMap<String, String> checkForDuplicatePaymentRequests(Integer vendorHeaderGeneratedId,
            Integer vendorDetailAssignedId, String invoiceNumber, KualiDecimal invoiceAmount, Date invoiceDate,
            String vendorToken, String specifiedSourceToken, boolean questionFormat) {
        return paymentRequestService.checkForDuplicatePaymentRequests(vendorHeaderGeneratedId, vendorDetailAssignedId,
                invoiceNumber, invoiceAmount, invoiceDate, vendorToken, specifiedSourceToken, questionFormat);
    }

    @Override
    public String getB2BUrlString() {
        return PurapConstants.B2B_URL_STRING;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setPaymentRequestService(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }

    public void setPurchaseOrderService(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    public void setRequisitionService(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setCreditMemoService(CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setAccountsPayableService(AccountsPayableService accountsPayableService) {
        this.accountsPayableService = accountsPayableService;
    }
}

