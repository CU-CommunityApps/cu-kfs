package edu.cornell.kfs.pdp.batch.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.fp.batch.service.DisbursementVoucherExtractService;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.batch.service.impl.ProcessPdpCancelPaidServiceImpl;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.pdp.service.PaymentGroupService;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;
import edu.cornell.kfs.pdp.businessobject.PaymentDetailExtendedAttribute;

public class CuProcessPdpCancelPaidServiceImpl extends ProcessPdpCancelPaidServiceImpl {
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuProcessPdpCancelPaidServiceImpl.class);
    
    
    /**
     * @see org.kuali.kfs.module.purap.service.ProcessPdpCancelPaidService#processPdpCancels()
     */
    public void processPdpCancels() {
        LOG.debug("processPdpCancels() started");

        Date processDate = dateTimeService.getCurrentSqlDate();

        String organization = parameterService.getParameterValue(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_ORG_CODE);
        String purapSubUnit = parameterService.getParameterValue(KfsParameterConstants.PURCHASING_BATCH.class, KFSParameterKeyConstants.PurapPdpParameterConstants.PURAP_PDP_SUB_UNIT_CODE);
        String dvSubUnit = parameterService.getParameterValue(DisbursementVoucherDocument.class, DisbursementVoucherConstants.DvPdpExtractGroup.DV_PDP_SBUNT_CODE);

        List<String> subUnits = new ArrayList<String>();
        subUnits.add(purapSubUnit);
        subUnits.add(dvSubUnit);

        Iterator<PaymentDetail> details = paymentDetailService.getUnprocessedCancelledDetails(organization, subUnits);
        while (details.hasNext()) {
            PaymentDetail paymentDetail = details.next();

            String documentTypeCode = paymentDetail.getFinancialDocumentTypeCode();
            String documentNumber = paymentDetail.getCustPaymentDocNbr();

            boolean primaryCancel = paymentDetail.getPrimaryCancelledPayment();
            boolean disbursedPayment = PdpConstants.PaymentStatusCodes.CANCEL_PAYMENT.equals(paymentDetail.getPaymentGroup().getPaymentStatusCode());
            //KFSPTS-2719
            boolean crCancel = ((PaymentDetailExtendedAttribute)paymentDetail.getExtension()).getCrCancelledPayment();

            if(purchasingAccountsPayableModuleService.isPurchasingBatchDocument(documentTypeCode)) {
                ((CuPurchasingAccountsPayableModuleService)purchasingAccountsPayableModuleService).handlePurchasingBatchCancels(documentNumber, documentTypeCode, primaryCancel, disbursedPayment, crCancel);
            }
            else if (DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH.equals(documentTypeCode)) {
                DisbursementVoucherDocument dv = dvExtractService.getDocumentById(documentNumber);
                if (dv != null) {
                    if (disbursedPayment || primaryCancel || crCancel) {
                        if (!crCancel) {
                            dvExtractService.cancelExtractedDisbursementVoucher(dv, processDate);
                        }
                    } else {
                        dvExtractService.resetExtractedDisbursementVoucher(dv, processDate);
                    }
                }
            }
            else {
                LOG.warn("processPdpCancels() Unknown document type (" + documentTypeCode + ") for document ID: " + documentNumber);
                continue;
            }

            paymentGroupService.processCancelledGroup(paymentDetail.getPaymentGroup(), processDate);
        }
    }

}
