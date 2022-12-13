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
    public void processPdpCancel(Date processDate, PaymentDetail paymentDetail) {
        String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        String documentNumber = paymentDetail.getCustPaymentDocNbr();

        boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
        boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(
                paymentDetail.getPaymentGroup().getPaymentStatusCode());

        //KFSPTS-2719
        boolean crCancel = false;
        PaymentDetailExtendedAttribute paymentDetailExtendedAttribute = (PaymentDetailExtendedAttribute) paymentDetail.getExtension();
        if (ObjectUtils.isNotNull(paymentDetailExtendedAttribute)) {
            crCancel = paymentDetailExtendedAttribute.getCrCancelledPayment();
        }

        if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(
                    documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
        } else {
            PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService != null) {
                PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
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
