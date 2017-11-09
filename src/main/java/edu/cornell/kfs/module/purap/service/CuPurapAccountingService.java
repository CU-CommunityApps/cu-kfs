package edu.cornell.kfs.module.purap.service;

import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.kim.api.identity.Person;

public interface CuPurapAccountingService extends PurapAccountingService{
    /**
    * KFSPTS-1273 : check if the acctlines belong to this fo user
    * @param document
    * @return
    */
   public boolean isFiscalOfficersForAllAcctLines(PurchasingAccountsPayableDocument document);
   
   boolean isFiscalOfficerForAccountingLine(Person currentUser, AccountingLine accountingLine);

}
