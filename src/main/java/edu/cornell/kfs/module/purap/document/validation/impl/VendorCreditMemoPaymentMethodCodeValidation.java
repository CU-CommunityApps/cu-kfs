/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.kns.util.GlobalVariables;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;


/**
 * Validates the payment method code used on credit memo document if it was created
 * from a payment request document.
 * 
 * @author jonathan
 */
public class VendorCreditMemoPaymentMethodCodeValidation extends GenericValidation {

    public boolean validate(AttributedDocumentEvent event) {
        if ( event.getDocument() instanceof VendorCreditMemoDocument ) {
            VendorCreditMemoDocument doc = (VendorCreditMemoDocument) event.getDocument();
            // check if from a PREQ document
            if ( doc.isSourceDocumentPaymentRequest() ) {
                // load the document
                PaymentRequestDocument preqDoc = doc.getPaymentRequestDocument();
                // if a UA PREQ, get the PMC
                if ( preqDoc instanceof PaymentRequestDocument ) {
                    // check if the PMC on this document is the same
                    String preqPaymentMethodCode = ((PaymentRequestDocument)preqDoc).getPaymentMethodCode();
                    if ( !StringUtils.equals(preqPaymentMethodCode, doc.getPaymentMethodCode() ) ) {
                        GlobalVariables.getMessageMap().putError(CUPurapPropertyConstants.DOCUMENT_PAYMENT_METHOD_CODE, CUPurapKeyConstants.ERROR_PAYMENTMETHODCODE_MUSTMATCHPREQ, preqPaymentMethodCode);
                        return false;
                    }
                }
            }
        }
        // if not (for some reason) the UA CM document, just return true
        return true;
    }

}
