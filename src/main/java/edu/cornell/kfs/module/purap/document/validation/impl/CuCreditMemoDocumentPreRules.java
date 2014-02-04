package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.validation.impl.CreditMemoDocumentPreRules;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;


public class CuCreditMemoDocumentPreRules extends CreditMemoDocumentPreRules {
    
    @Override
    public boolean doPrompts(Document document){

        boolean preRulesOK = true;

        AccountsPayableDocument accountsPayableDocument = (AccountsPayableDocument) document;

        // Check if the total does not match the submitted credit if the document hasn't been completed.
        if (!SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(accountsPayableDocument)) {
            preRulesOK = confirmInvoiceNoMatchOverride(accountsPayableDocument);
        }
        else if (SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(accountsPayableDocument)) {
            // if past full document entry complete, then set override to true to skip validation
            accountsPayableDocument.setUnmatchedOverride(true);
        }

        return preRulesOK;
    }
	
	@SuppressWarnings("deprecation")
	@Override
    public boolean confirmInvoiceNoMatchOverride(AccountsPayableDocument accountsPayableDocument) {
    	// Check if the total does not match the submitted credit. 
    	GlobalVariables.getMessageMap().clearErrorPath();
        GlobalVariables.getMessageMap().addToErrorPath(KFSPropertyConstants.DOCUMENT);

    	
    	 // If the values are mismatched, suppress the prompt and post the error on the screen. 
         // Do not allow to override unmatched credit.
        if (validateInvoiceTotalsAreMismatched(accountsPayableDocument)) {
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.GRAND_TOTAL, PurapKeyConstants.ERROR_CREDIT_MEMO_INVOICE_AMOUNT_NONMATCH);
            event.setActionForwardName(KFSConstants.MAPPING_BASIC);
            return false;
        }    
          return true;  
    }
  }
