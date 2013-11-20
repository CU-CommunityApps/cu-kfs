package edu.cornell.kfs.integration.purap;

import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;

public interface CuPurchasingAccountsPayableModuleService extends PurchasingAccountsPayableModuleService {
    
    public void handlePurchasingBatchCancels(String documentNumber, String financialSystemDocumentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel);

}
