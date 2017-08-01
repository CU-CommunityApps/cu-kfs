package edu.cornell.kfs.integration.purap;

import org.kuali.kfs.integration.purap.PurchasingAccountsPayableModuleService;
import org.kuali.kfs.krad.document.Document;

public interface CuPurchasingAccountsPayableModuleService extends PurchasingAccountsPayableModuleService {
    
    public void handlePurchasingBatchCancels(String documentNumber, String financialSystemDocumentTypeCode, boolean primaryCancel, boolean disbursedPayment, boolean crCancel);
    
    public void createAndSaveReasonNote(Document purchasingDocument, String reasonToChange);

}