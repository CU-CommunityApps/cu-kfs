package edu.cornell.kfs.module.purap.service;

import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;

public interface CuPurapAccountingService extends PurapAccountingService{
    /**
    * KFSPTS-1273 : check if the acctlines belong to this fo user
    * @param document
    * @return
    */
   public boolean isFiscalOfficersForAllAcctLines(PurchasingAccountsPayableDocument document);

}
