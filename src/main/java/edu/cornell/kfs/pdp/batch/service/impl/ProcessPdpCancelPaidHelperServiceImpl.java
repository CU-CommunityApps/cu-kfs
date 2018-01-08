package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.batch.service.PaymentSourceToExtractService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;
import edu.cornell.kfs.pdp.batch.service.ProcessPdpCancelPaidHelperService;
import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class ProcessPdpCancelPaidHelperServiceImpl implements ProcessPdpCancelPaidHelperService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcessPdpCancelPaidHelperServiceImpl.class);

    protected PaymentGroupService paymentGroupService;
    protected PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService;
    protected DocumentService documentService;

    protected volatile List<PaymentSourceToExtractService<PaymentSource>> paymentSourceToExtractServices;

    /**
     * Default implementation uses most of the "while" loop contents from the ProcessPdpCancelPaidServiceImpl.processPdpCancels method,
     * with additional CU-related changes as needed.
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

        if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
            ((CuPurchasingAccountsPayableModuleService) purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(
                    documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
        }
        else {
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

        paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
    }

    /**
     * Default implementation uses most of the "while" loop contents from the ProcessPdpCancelPaidServiceImpl.processPdpPaids method,
     * with additional CU-related changes as needed.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processPdpPaid(PaymentDetail paymentDetail, Date processDate) {
        String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
        String documentNumber = paymentDetail.getCustPaymentDocNbr();

        if (purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
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

        paymentGroupService.processPaidGroup(paymentDetail.getPaymentGroup(), processDate);
    }

    // Copied from ProcessPdpCancelPaidServiceImpl
    protected PaymentSourceToExtractService<PaymentSource> getPaymentSourceToExtractService(PaymentDetail paymentDetail) {
        for (PaymentSourceToExtractService<PaymentSource> extractionService : getPaymentSourceToExtractServices()) {
            if (extractionService.handlesAchCheckDocumentType(paymentDetail.getFinancialDocumentTypeCode())) {
                return extractionService;
            }
        }
        return null;
    }

    // Copied from ProcessPdpCancelPaidServiceImpl
    protected List<PaymentSourceToExtractService<PaymentSource>> getPaymentSourceToExtractServices() {
        if (paymentSourceToExtractServices == null) {
            paymentSourceToExtractServices = new ArrayList<PaymentSourceToExtractService<PaymentSource>>();
            Map<String, PaymentSourceToExtractService> extractionServices = SpringContext.getBeansOfType(PaymentSourceToExtractService.class);
            for (PaymentSourceToExtractService<PaymentSource> extractionService : extractionServices.values()) {
                paymentSourceToExtractServices.add(extractionService);
            }
        }
        return paymentSourceToExtractServices;
    }

    public void setPaymentGroupService(PaymentGroupService paymentGroupService) {
        this.paymentGroupService = paymentGroupService;
    }

    public void setPurchasingAccountsPayableModuleService(PurchasingAccountsPayableModuleService purchasingAccountsPayableModuleService) {
        this.purchasingAccountsPayableModuleService = purchasingAccountsPayableModuleService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
