package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.ExtractionUnit;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;
import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class CuProcessPdpCancelPaidServiceImpl extends ProcessPdpCancelPaidServiceImpl {
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuProcessPdpCancelPaidServiceImpl.class);
    
    
    /**
     * @see org.kuali.kfs.module.purap.service.ProcessPdpCancelPaidService#processPdpCancels()
     */
    public void processPdpCancels() {
        LOG.debug("processPdpCancels() started");

        Date processDate = dateTimeService.getCurrentSqlDate();

        final List<ExtractionUnit> extractionUnits = getExtractionUnits();
        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedCancelledDetails(extractionUnits);
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();

            String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
            String documentNumber = paymentDetail.getCustPaymentDocNbr();

            boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
            boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(paymentDetail.getPaymentGroup().getPaymentStatusCode());
            
            //KFSPTS-2719
            boolean crCancel = false;
            PaymentDetailExtendedAttribute paymentDetailExtendedAttribute = (PaymentDetailExtendedAttribute) paymentDetail.getExtension();
            if (ObjectUtils.isNotNull(paymentDetailExtendedAttribute)) {
                crCancel = paymentDetailExtendedAttribute.getCrCancelledPayment();
            }

            if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
                ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
            }
            else {
                PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
                if (extractService != null) {
                    try {
                        PaymentSource dv = (PaymentSource)getDocumentService().getByDocumentHeaderId(documentNumber);
                        if (dv != null) {
                            if (disbursedPayment || primaryCancel) {
                                if (!crCancel) {
                                    extractService.cancelPayment(dv, processDate);
                                }
                            } else {
                                extractService.resetFromExtraction(dv);
                            }
                        }
                    } catch (WorkflowException we) {
                        throw new RuntimeException("Could not retrieve document #"+documentNumber, we);
                    }
                } else {
                    LOG.warn("processPdpCancels() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                    continue;
                }
            }

            paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
        }
   }

}