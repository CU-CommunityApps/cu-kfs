package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.ExtractionUnit;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;
import edu.cornell.kfs.pdp.batch.service.CuProcessPdpCancelPaidService;
import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class CuProcessPdpCancelPaidServiceImpl extends ProcessPdpCancelPaidServiceImpl implements CuProcessPdpCancelPaidService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuProcessPdpCancelPaidServiceImpl.class);

    /**
     * Overridden to process each payment detail in its own transaction.
     * This implementation forces the current service to call a proxied version of itself,
     * in order for Spring to handle the transactions properly on the per-payment handler method.
     * 
     * @see org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl#processPdpCancels()
     */
    @Override
    public void processPdpCancels() {
        LOG.debug("processPdpCancels() started");

        CuProcessPdpCancelPaidService proxiedProcessPdpCancelPaidService = getProxiedProcessPdpCancelPaidService();
        Date processDate = dateTimeService.getCurrentSqlDate();
        List<ExtractionUnit> extractionUnits = getExtractionUnits();
        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedCancelledDetails(extractionUnits);
        
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();
            proxiedProcessPdpCancelPaidService.processPdpCancel(paymentDetail, processDate);
        }
    }

    /**
     * Overridden to process each payment detail in its own transaction.
     * This implementation forces the current service to call a proxied version of itself,
     * in order for Spring to handle the transactions properly on the per-payment handler method.
     * 
     * @see org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl#processPdpPaids()
     */
    @Override
    public void processPdpPaids() {
        LOG.debug("processPdpPaids() started");

        CuProcessPdpCancelPaidService proxiedProcessPdpCancelPaidService = getProxiedProcessPdpCancelPaidService();
        Date processDate = dateTimeService.getCurrentSqlDate();
        List<ExtractionUnit> extractionUnits = getExtractionUnits();
        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedPaidDetails(extractionUnits);
        
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();
            proxiedProcessPdpCancelPaidService.processPdpPaid(paymentDetail, processDate);
        }
    }

    /**
     * Default implementation uses most of the "while" loop contents from the ProcessPdpCancelPaidServiceImpl.processPdpCancels method,
     * with additional CU-related changes as needed. This implementation also runs within its own transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processPdpCancel(PaymentDetail paymentDetail, Date processDate) {
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

        /*if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(
                    documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
        } else {
            PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService != null) {
                try {
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
                } catch (WorkflowException we) {
                    throw new RuntimeException("Could not retrieve document #" + documentNumber, we);
                }
            } else {
                LOG.warn("processPdpCancel() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                return;
            }
        }

        paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);*/
    }

    /**
     * Default implementation uses most of the "while" loop contents from the ProcessPdpCancelPaidServiceImpl.processPdpPaids method,
     * with additional CU-related changes as needed. This implementation also runs within its own transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processPdpPaid(PaymentDetail paymentDetail, Date processDate) {
        String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        String documentNumber = paymentDetail.getCustPaymentDocNbr();

        /*if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            purchasingAccountsPayableModuleService.handlePurchasingBatchPaids(documentNumber, documentTypeCode, processDate);
        } else {
            PaymentSourceToExtractService<PaymentSource> extractService = getPaymentSourceToExtractService(paymentDetail);
            if (extractService != null) {
                try {
                    PaymentSource dv = (PaymentSource) documentService.getByDocumentHeaderId(documentNumber);
                    extractService.markAsPaid(dv, processDate);
                } catch (WorkflowException we) {
                    throw new RuntimeException("Could not retrieve document #" + documentNumber, we);
                }
            } else {
                LOG.warn("processPdpPaids() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                return;
            }
        }

        paymentGroupService.processPaidGroup(paymentDetail.getPaymentGroup(), processDate);*/
    }

    protected CuProcessPdpCancelPaidService getProxiedProcessPdpCancelPaidService() {
        return SpringContext.getBean(CuProcessPdpCancelPaidService.class);
    }

}