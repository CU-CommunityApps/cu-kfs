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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPdpCancel(final Date processDate, final PaymentDetail paymentDetail) {
        final String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        final String documentNumber = paymentDetail.getCustPaymentDocNbr();

        final boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
        final boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(
                paymentDetail.getPaymentGroup().getPaymentStatusCode());

        //KFSPTS-2719
        boolean crCancel = false;
        final PaymentDetailExtendedAttribute paymentDetailExtendedAttribute = (PaymentDetailExtendedAttribute) paymentDetail.getExtension();
        if (ObjectUtils.isNotNull(paymentDetailExtendedAttribute)) {
            crCancel = paymentDetailExtendedAttribute.getCrCancelledPayment();
        }

        if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(
                    documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
        } else {
            final PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService != null) {
                final PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
                if (dv != null) {
                    if (disbursedPayment || primaryCancel || crCancel) {
                        if (!crCancel) {
                            extractService.cancelPayment(dv, processDate);
                        }
                    } else {
                        extractService.resetFromExtraction(dv);
                    }
                }
            } else {
                LOG.warn(
                        "processPdpCancels() Unknown document type ({}) for document ID: {}",
                        documentTypeCode,
                        documentNumber
                );
                return;
            }
        }

        paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
    }

}
