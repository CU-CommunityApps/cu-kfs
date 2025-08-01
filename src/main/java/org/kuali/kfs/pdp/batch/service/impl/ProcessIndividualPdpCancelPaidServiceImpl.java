/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.pdp.batch.service.impl;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.CreditMemoService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.ProcessIndividualPdpCancelPaidService;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.PaymentSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// CU customization: make method protected
public class ProcessIndividualPdpCancelPaidServiceImpl implements ProcessIndividualPdpCancelPaidService {

    private static final Logger LOG = LogManager.getLogger();

    protected DocumentService documentService;
    protected PaymentGroupService paymentGroupService;
    private CreditMemoService creditMemoService;
    private ParameterService parameterService;
    private PaymentRequestService paymentRequestService;

    protected volatile List<PaymentSourceToExtractService<PaymentSource>> paymentSourceToExtractServices;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPdpCancel(final Date processDate, final PaymentDetail paymentDetail) {
        final String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        final String documentNumber = paymentDetail.getCustPaymentDocNbr();

        final boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
        final boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_DISBURSEMENT.equals(
                paymentDetail.getPaymentGroup().getPaymentStatusCode());

        if (isPurchasingBatchDocument(documentTypeCode)) {
            handlePurchasingBatchCancels(documentNumber, documentTypeCode, primaryCancel, disbursedPayment);
        } else {
            final PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService == null) {
                return;
            }
            final PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
            if (dv != null) {
                if (disbursedPayment || primaryCancel) {
                    extractService.cancelPayment(dv, processDate);
                } else {
                    extractService.resetFromExtraction(dv);
                }
            }
        }

        paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
    }

    // CU customization: change from private to protected
    protected static boolean isPurchasingBatchDocument(final String documentTypeCode) {
        return PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)
               || PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode);
    }

    void handlePurchasingBatchCancels(
            final String documentNumber,
            final String documentTypeCode,
            final boolean primaryCancel,
            final boolean disbursedPayment
    ) {
        LOG.info(
                "Begin handlePurchasingBatchCancels(documentNumber={}, documentTypeCode={}, primaryCancel={}, "
                + "disbursedPayment={}",
                documentNumber,
                documentTypeCode,
                primaryCancel,
                disbursedPayment
        );

        final String preqCancelNote = parameterService.getParameterValueAsString(
                PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_CANCEL_NOTE);
        final String preqResetNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_RESET_NOTE);
        final String cmCancelNote = parameterService.getParameterValueAsString(
                VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_CANCEL_NOTE);
        final String cmResetNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_RESET_NOTE);

        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            final PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                if (disbursedPayment || primaryCancel) {
                    paymentRequestService.cancelExtractedPaymentRequest(pr, preqCancelNote);
                } else {
                    paymentRequestService.resetExtractedPaymentRequest(pr, preqResetNote);
                }
            } else {
                LOG.error(
                        "processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Payment Request with doc type of {} "
                        + "with id {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            final VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                if (disbursedPayment || primaryCancel) {
                    creditMemoService.cancelExtractedCreditMemo(cm, cmCancelNote);
                } else {
                    creditMemoService.resetExtractedCreditMemo(cm, cmResetNote);
                }
            } else {
                LOG.error(
                        "processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of {} with id"
                        + " {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPdpPaid(final Date processDate, final PaymentDetail paymentDetail) {
        final String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        final String documentNumber = paymentDetail.getCustPaymentDocNbr();

        if (isPurchasingBatchDocument(documentTypeCode)) {
            handlePurchasingBatchPaids(documentNumber, documentTypeCode, processDate);
        } else {
            final PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService == null) {
                return;
            }
            final PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
            extractService.markAsPaid(dv, processDate);
        }

        // If there is a CM along with PREQs for a Vendor, the Payment Group may contain multiple Payment Details
        // in which case if we process the paid group with the first Payment Detail, and then an Exception occurs
        // (typically we're seeing OLEs) before all the Payment Details in the Payment Group are processed,
        // some Payment Details may not be marked paid. This requires subsequent manual cleanup. This check to wait
        // until all Payment Details are processed should avoid that issue. It may also fix the OLEs, but if not, we
        // could add a call to refresh reference objects (the Payment Group is what needs to be refreshed) on the
        // Payment Detail before saving it.
        if (allPaymentDetailsAreProcessed(paymentDetail.getPaymentGroup())) {
            paymentGroupService.processPaidGroup(paymentDetail.getPaymentGroup(), processDate);
        }
    }

    void handlePurchasingBatchPaids(
            final String documentNumber,
            final String documentTypeCode,
            final Date processDate
    ) {
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            final PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                paymentRequestService.markPaid(pr, processDate);
            } else {
                LOG.error(
                        "processPdpPaids() DOES NOT EXIST, CANNOT MARK - Payment Request with doc type of {} with id {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            final VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                creditMemoService.markPaid(cm, processDate);
            } else {
                LOG.error(
                        "processPdpPaids() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of {} with id {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        }
    }

    private boolean allPaymentDetailsAreProcessed(final PaymentGroup paymentGroup) {
        final List<PaymentDetail> paymentDetails = paymentGroup.getPaymentDetails();
        if (paymentDetails.size() < 2) {
            return true;
        }
        for (final PaymentDetail paymentDetail: paymentDetails) {
            if (!isDocumentPaid(paymentDetail.getCustPaymentDocNbr(), paymentDetail.getFinancialDocumentTypeCode())) {
                return false;
            }
        }
        return true;
    }

    private boolean isDocumentPaid(final String documentNumber, final String financialSystemDocumentTypeCode) {
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(financialSystemDocumentTypeCode)) {
            final PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                return pr.getPaymentPaidTimestamp() != null;
            }
        } else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(financialSystemDocumentTypeCode)) {
            final VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                return cm.getCreditMemoPaidTimestamp() != null;
            }
        }
        return false;
    }

    /**
     * Looks up the PaymentSourceToExtractService which can act upon the given PaymentDetail, based on the
     * PaymentDetail's document type
     *
     * @param paymentDetail the payment detail to find an extraction service to act upon
     * @return the matching PaymentSourceToExtractService, or null if a matching service could not be found (which
     *         would be weird, because _something_ created this PaymentDetail, but...whatever)
     */
    protected PaymentSourceToExtractService<PaymentSource> getPaymentSourceToExtractService(final PaymentDetail paymentDetail) {
        for (final PaymentSourceToExtractService<PaymentSource> extractionService : getPaymentSourceToExtractServices()) {
            if (extractionService.handlesAchCheckDocumentType(paymentDetail.getFinancialDocumentTypeCode())) {
                return extractionService;
            }
        }
        return null;
    }

    @Override
    public List<PaymentSourceToExtractService<PaymentSource>> getPaymentSourceToExtractServices() {
        if (paymentSourceToExtractServices == null) {
            paymentSourceToExtractServices = new ArrayList<>();
            final Map<String, PaymentSourceToExtractService> extractionServices =
                    SpringContext.getBeansOfType(PaymentSourceToExtractService.class);
            for (final PaymentSourceToExtractService<PaymentSource> extractionService : extractionServices.values()) {
                paymentSourceToExtractServices.add(extractionService);
            }
        }
        return paymentSourceToExtractServices;
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setPaymentGroupService(final PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setCreditMemoService(final CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPaymentRequestService(final PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }
}
