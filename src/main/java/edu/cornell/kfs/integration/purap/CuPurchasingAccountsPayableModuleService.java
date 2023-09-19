package edu.cornell.kfs.integration.purap;

import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;

public interface CuPurchasingAccountsPayableModuleService extends PurchasingAccountsPayableModuleService {
    
    public void handlePurchasingBatchCancels(final String documentNumber, final String financialSystemDocumentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel);

}