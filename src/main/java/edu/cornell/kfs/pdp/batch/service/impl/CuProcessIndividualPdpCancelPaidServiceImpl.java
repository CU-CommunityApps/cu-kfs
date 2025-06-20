package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.CreditMemoService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.impl.ProcessIndividualPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.document.PaymentSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class CuProcessIndividualPdpCancelPaidServiceImpl extends ProcessIndividualPdpCancelPaidServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    private CreditMemoService creditMemoService;
    private ParameterService parameterService;
    protected PaymentRequestService paymentRequestService;

    /**
     * Overridden to include the KFSPTS-2719 Check-Recon-related logic regarding cancelled payments.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPdpCancel(final Date processDate, final PaymentDetail paymentDetail) {
        final String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        final String documentNumber = paymentDetail.getCustPaymentDocNbr();

        final boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
        final boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(
                paymentDetail.getPaymentGroup().getPaymentStatusCode());

        // CU Customization: Retrieve custom property indicating whether the payment was cancelled in Check Recon.
        boolean crCancel = false;
        final PaymentDetailExtendedAttribute paymentDetailExtendedAttribute = (PaymentDetailExtendedAttribute) paymentDetail.getExtension();
        if (ObjectUtils.isNotNull(paymentDetailExtendedAttribute)) {
            crCancel = paymentDetailExtendedAttribute.getCrCancelledPayment();
        }

        if (isPurchasingBatchDocument(documentTypeCode)) {
            // CU Customization: Invoke CU-specific method variant that also accepts the CR-cancelled flag as input.
            handlePurchasingBatchCancels(
                    documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
        } else {
            final PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService == null) {
                return;
            }
            final PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
            // CU Customization: Skip the cancel/reset operations for CR-cancelled payments.
            if (dv != null && !crCancel) {
                if (disbursedPayment || primaryCancel) {
                    extractService.cancelPayment(dv, processDate);
                } else {
                    extractService.resetFromExtraction(dv);
                }
            }
        }

        paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
    }
    
    // locally customized method that also supports cr cancel
    public void handlePurchasingBatchCancels(
            final String documentNumber, final String documentTypeCode, 
            final boolean primaryCancel, final boolean disbursedPayment, final boolean crCancel) {
        LOG.info(
                "Begin handlePurchasingBatchCancels(documentNumber={}, documentTypeCode={}, primaryCancel={}, "
                + "disbursedPayment={}",
                documentNumber,
                documentTypeCode,
                primaryCancel,
                disbursedPayment
        );
        final String preqCancelNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_CANCEL_NOTE);
        final String preqResetNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_PDP_PREQ_RESET_NOTE);
        final String cmCancelNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_CANCEL_NOTE);
        final String cmResetNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class,
                PurapParameterConstants.PURAP_PDP_CM_RESET_NOTE);
        
        if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
            final PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
            if (pr != null) {
                if (disbursedPayment || primaryCancel || crCancel) {
                    if (!crCancel) {
                        paymentRequestService.cancelExtractedPaymentRequest(pr, preqCancelNote);
                    }
                }
                else {
                    paymentRequestService.resetExtractedPaymentRequest(pr, preqResetNote);
                }
            }
            else {
                LOG.error(
                        "processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Payment Request with doc type of {} "
                        + "with id {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        }
        else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
            final VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
            if (cm != null) {
                if (disbursedPayment || primaryCancel || crCancel) {
                    if (!crCancel) {
                        creditMemoService.cancelExtractedCreditMemo(cm, cmCancelNote);
                    }
                }
                else {
                    creditMemoService.resetExtractedCreditMemo(cm, cmResetNote);
                }
            }
            else {
                LOG.error(
                        "processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of {} with id"
                        + " {}",
                        documentTypeCode,
                        documentNumber
                );
            }
        }
    }

    public void setCreditMemoService(CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPaymentRequestService(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

}
