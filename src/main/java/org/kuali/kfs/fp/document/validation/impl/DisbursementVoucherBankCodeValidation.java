/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.fp.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.document.validation.impl.BankCodeValidation;

// KFSPTS-1891 : change in this class
public class DisbursementVoucherBankCodeValidation extends GenericValidation {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherBankCodeValidation.class);

    private AccountingDocument accountingDocumentForValidation;

    /**
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        LOG.debug("validate start");
        
        DisbursementVoucherDocument dvDocument = (DisbursementVoucherDocument) accountingDocumentForValidation;
        
        boolean isValid = BankCodeValidation.validate(dvDocument.getDisbVchrBankCode(), "document." + KFSPropertyConstants.DISB_VCHR_BANK_CODE, dvDocument.getDisbVchrPaymentMethodCode(), false, true);
        // KFSPTS-1891
        // TODO : this is code from 1891, it caused problem to wipe out required bank cd while FO approve.
        // KFSPTS-2582
//        if ( isValid ) {
//            // clear out the bank code on the document if not needed (per the message set by the call above)
//            if ( StringUtils.isNotBlank(dvDocument.getDisbVchrBankCode())
//                    && !BankCodeValidation.doesBankCodeNeedToBePopulated(dvDocument.getDisbVchrPaymentMethodCode()) ) {
//                dvDocument.setBank(null);
//                dvDocument.setDisbVchrBankCode(null);                
//            }
//        }
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
