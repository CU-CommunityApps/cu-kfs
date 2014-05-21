package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.validation.impl.AccountsPayableBankCodeValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedRouteDocumentEvent;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;
import edu.cornell.kfs.sys.document.validation.impl.CuBankCodeValidation;

public class CuAccountsPayableBankCodeValidation extends AccountsPayableBankCodeValidation {
	
	@Override
	public boolean validate(AttributedDocumentEvent event) {
        AccountsPayableDocumentBase apDocument = (AccountsPayableDocumentBase) getAccountingDocumentForValidation();

        // check if one of the extended UA documents, if so, take the payment method into account, otherwise, revert to baseline behavior
        boolean isValid = true;
        if ( apDocument instanceof PaymentRequestDocument) {
        	if (StringUtils.isNotBlank(apDocument.getBankCode())) {
        		// PREQ bank code is not required
                isValid = CuBankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE, ((CuPaymentRequestDocument)apDocument).getPaymentMethodCode(), false, true);            
                if ( isValid ) {
                    if ( !(event instanceof AttributedRouteDocumentEvent) &&  StringUtils.isNotBlank(apDocument.getBankCode())
                        && !CuBankCodeValidation.doesBankCodeNeedToBePopulated(((CuPaymentRequestDocument)apDocument).getPaymentMethodCode()) ) {
                        apDocument.setBank(null);
                        apDocument.setBankCode(null);                
                    }
                }
        	}
        }  else if ( apDocument instanceof VendorCreditMemoDocument ) {
        	if (StringUtils.isNotBlank(apDocument.getBankCode())) {
        	     isValid = CuBankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE,  ((CuVendorCreditMemoDocument)apDocument).getPaymentMethodCode(), false, true);                        
        	     if ( isValid ) {
                // clear out the bank code on the document if not needed (per the message set by the call above)
        	          if ( StringUtils.isNotBlank(apDocument.getBankCode())
        	        		        && !CuBankCodeValidation.doesBankCodeNeedToBePopulated(((CuVendorCreditMemoDocument)apDocument).getPaymentMethodCode()) ) {
        	        	    apDocument.setBank(null);
        	        	    apDocument.setBankCode(null);                
        	        }
               }
        	}
        } else {
            isValid = CuBankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE, false, true);
        }

        return isValid;
	}

}
