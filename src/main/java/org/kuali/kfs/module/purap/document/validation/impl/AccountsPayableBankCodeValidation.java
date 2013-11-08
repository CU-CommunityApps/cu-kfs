/*
 * Copyright 2008-2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.AccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.event.AttributedRouteDocumentEvent;
import org.kuali.kfs.sys.document.validation.impl.BankCodeValidation;


public class AccountsPayableBankCodeValidation extends GenericValidation {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountsPayableBankCodeValidation.class);

    private AccountingDocument accountingDocumentForValidation;

    /**
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    // KFSPTS-1891 : not sure if need another validation class for this ?
    public boolean validate(AttributedDocumentEvent event) {
        AccountsPayableDocumentBase apDocument = (AccountsPayableDocumentBase) getAccountingDocumentForValidation();

        // check if one of the extended UA documents, if so, take the payment method into account, otherwise, revert to baseline behavior
        boolean isValid = true;
        if ( apDocument instanceof PaymentRequestDocument) {
        	if (StringUtils.isNotBlank(apDocument.getBankCode())) {
        		// PREQ bank code is not required
                isValid = BankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE, ((PaymentRequestDocument)apDocument).getPaymentMethodCode(), false, true);            
                if ( isValid ) {
                    if ( !(event instanceof AttributedRouteDocumentEvent) &&  StringUtils.isNotBlank(apDocument.getBankCode())
                        && !BankCodeValidation.doesBankCodeNeedToBePopulated(((PaymentRequestDocument)apDocument).getPaymentMethodCode()) ) {
                        apDocument.setBank(null);
                        apDocument.setBankCode(null);                
                    }
                }
        	}
        }  else if ( apDocument instanceof VendorCreditMemoDocument ) {
        	if (StringUtils.isNotBlank(apDocument.getBankCode())) {
        	     isValid = BankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE,  ((VendorCreditMemoDocument)apDocument).getPaymentMethodCode(), false, true);                        
        	     if ( isValid ) {
                // clear out the bank code on the document if not needed (per the message set by the call above)
        	          if ( StringUtils.isNotBlank(apDocument.getBankCode())
        	        		        && !BankCodeValidation.doesBankCodeNeedToBePopulated(((VendorCreditMemoDocument)apDocument).getPaymentMethodCode()) ) {
        	        	    apDocument.setBank(null);
        	        	    apDocument.setBankCode(null);                
        	        }
               }
        	}
        } else {
            isValid = BankCodeValidation.validate(apDocument.getBankCode(), "document." + PurapPropertyConstants.BANK_CODE, false, true);
        }

        return isValid;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public AccountingDocument getAccountingDocumentForValidation() {
        return accountingDocumentForValidation;
    }
}
