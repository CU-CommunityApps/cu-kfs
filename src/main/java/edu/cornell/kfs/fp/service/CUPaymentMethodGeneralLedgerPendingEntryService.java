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
package edu.cornell.kfs.fp.service;

import java.util.Map;

import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/**
 * Helper service to generate GL entries for FP and PURAP documents which are specific
 * to certain payment method codes.
 * 
 * @author jonathan
 */
public interface CUPaymentMethodGeneralLedgerPendingEntryService {

    /**
     * Get the default bank for the payment method.  Returns null if the payment method code does not use a bank.
     * 
     * @param paymentMethodCode
     * @return Bank from PaymentMethod record. null if paymentMethodCode is null, bankCode is null, or the bank does not exist. 
     */
    Bank getBankForPaymentMethod( String paymentMethodCode );

    /**
     * Generates the appropriate GL pending entries for the given payment method based on the payment method code.
     * 
     * @param document
     * @param paymentMethodCode
     * @param bankCode
     * @param bankCodePropertyName
     * @param documentTypeCode Document type code to use on the GL pending entries.
     * @param templatePendingEntry
     * @param feesWaived
     * @param reverseCharge
     * @param sequenceHelper
     * @return
     */
    boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
            AccountingDocument document,
            String paymentMethodCode,
            String bankCode, 
            String bankCodePropertyName, // for error messages
            GeneralLedgerPendingEntry templatePendingEntry,
            boolean feesWaived,
            boolean reverseCharge,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper);
    
    /**
     * Special version of the method to handle cases of changes in the document amount
     * when the GL entries can not be completely regenerated.  (As on the PREQ - when modify)
     * 
     * @return
     */
    boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
            AccountingDocument document,
            String paymentMethodCode,
            String bankCode, 
            String bankCodePropertyName, // for error messages
            GeneralLedgerPendingEntry templatePendingEntry,
            boolean feesWaived,
            boolean reverseCharge,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            KualiDecimal bankOffsetAmount,
            Map<String,KualiDecimal> actualTotalsByChart );
    
    /**
     * Returns whether the given payment method should be processed using the PDP component. 
     * 
     * @param paymentMethodCode
     * @return
     */
    boolean isPaymentMethodProcessedUsingPdp( String paymentMethodCode );
    

    public void generateFinalEntriesForPRNC(PaymentRequestDocument document);
    
    /**
     * Generates the bank offsets.
     * 
     * @param document
     * @param bankCode
     * @param bankCodePropertyName
     * @param documentTypeCode
     * @param sequenceHelper
     * @param bankOffsetAmount
     * @return
     */
    public boolean generateDocumentBankOffsetEntries(AccountingDocument document, String bankCode, String bankCodePropertyName, String documentTypeCode, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal bankOffsetAmount );
        
    
}
