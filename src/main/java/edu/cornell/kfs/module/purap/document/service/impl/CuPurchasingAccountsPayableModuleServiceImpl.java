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
    private static final Logger LOG = LogManager.getLogger();
    
   /**
    * @see org.kuali.kfs.integration.pdp.service.PurchasingAccountsPayableModuleService#handlePurchasingBatchCancels(java.lang.String)
    */
   public void handlePurchasingBatchCancels(String documentNumber, String documentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel) {
       String preqCancelNote = getParameterService().getParameterValueAsString(PaymentRequestDocument.class,
               PurapParameterConstants.PURAP_PDP_PREQ_CANCEL_NOTE);
       String preqResetNote = getParameterService().getParameterValueAsString(PaymentRequestDocument.class,
               PurapParameterConstants.PURAP_PDP_PREQ_RESET_NOTE);
       String cmCancelNote = getParameterService().getParameterValueAsString(VendorCreditMemoDocument.class,
               PurapParameterConstants.PURAP_PDP_CM_CANCEL_NOTE);
       String cmResetNote = getParameterService().getParameterValueAsString(VendorCreditMemoDocument.class,
               PurapParameterConstants.PURAP_PDP_CM_RESET_NOTE);
       
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
           VendorCreditMemoDocument cm = getCreditMemoService().getCreditMemoByDocumentNumber(documentNumber);
           if (cm != null) {
               if (disbursedPayment || primaryCancel || crCancel) {
                   if (!crCancel) {
                       getCreditMemoService().cancelExtractedCreditMemo(cm, cmCancelNote);
                   }
               }
               else {
                   getCreditMemoService().resetExtractedCreditMemo(cm, cmResetNote);
               }
           }
           else {
               LOG.error("processPdpCancels() DOES NOT EXIST, CANNOT PROCESS - Credit Memo with doc type of " + documentTypeCode + " with id " + documentNumber);
           }
       }
   }
}