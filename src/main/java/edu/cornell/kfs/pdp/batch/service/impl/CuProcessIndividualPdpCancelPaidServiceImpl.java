package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.impl.ProcessIndividualPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.document.PaymentSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;
import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class CuProcessIndividualPdpCancelPaidServiceImpl extends ProcessIndividualPdpCancelPaidServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

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

        if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            // CU Customization: Invoke CU-specific method variant that also accepts the CR-cancelled flag as input.
            ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(
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

}
