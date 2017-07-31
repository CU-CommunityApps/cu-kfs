package edu.cornell.kfs.integration.purap;

import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.module.purap.document.web.struts.PurchasingFormBase;

public interface CuPurchasingAccountsPayableModuleService extends PurchasingAccountsPayableModuleService {
    
    public void handlePurchasingBatchCancels(String documentNumber, String financialSystemDocumentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel);
    
    public void createAndSaveReasonNote(PurchasingFormBase purForm);

}