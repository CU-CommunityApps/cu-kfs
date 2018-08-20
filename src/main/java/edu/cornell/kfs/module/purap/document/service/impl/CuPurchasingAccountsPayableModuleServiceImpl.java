package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.CreditMemoService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.impl.PurchasingAccountsPayableModuleServiceImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.integration.purap.CuPurchasingAccountsPayableModuleService;

public class CuPurchasingAccountsPayableModuleServiceImpl extends PurchasingAccountsPayableModuleServiceImpl implements CuPurchasingAccountsPayableModuleService {
    private static final Logger LOG = LogManager.getLogger(CuPurchasingAccountsPayableModuleServiceImpl.class);
    
   /**
    * @see org.kuali.kfs.integration.pdp.service.PurchasingAccountsPayableModuleService#handlePurchasingBatchCancels(java.lang.String)
    */
   public void handlePurchasingBatchCancels(String documentNumber, String documentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel) {
       ParameterService parameterService = SpringContext.getBean(ParameterService.class);
       PaymentRequestService paymentRequestService = SpringContext.getBean(PaymentRequestService.class);
       CreditMemoService creditMemoService = SpringContext.getBean(CreditMemoService.class);

       String preqCancelNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PDP_PREQ_CANCEL_NOTE);
       String preqResetNote = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PDP_PREQ_RESET_NOTE);
       String cmCancelNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class, PurapParameterConstants.PURAP_PDP_CM_CANCEL_NOTE);
       String cmResetNote = parameterService.getParameterValueAsString(VendorCreditMemoDocument.class, PurapParameterConstants.PURAP_PDP_CM_RESET_NOTE);

       if (PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT.equals(documentTypeCode)) {
           PaymentRequestDocument pr = paymentRequestService.getPaymentRequestByDocumentNumber(documentNumber);
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
               LOG.error("processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Payment Request with doc type of " + documentTypeCode + " with id " + documentNumber);
           }
       }
       else if (PurapConstants.PurapDocTypeCodes.CREDIT_MEMO_DOCUMENT.equals(documentTypeCode)) {
           VendorCreditMemoDocument cm = creditMemoService.getCreditMemoByDocumentNumber(documentNumber);
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
               LOG.error("processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of " + documentTypeCode + " with id " + documentNumber);
           }
       }
   }
}