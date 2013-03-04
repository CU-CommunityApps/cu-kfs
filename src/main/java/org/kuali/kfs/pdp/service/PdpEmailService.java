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
package org.kuali.kfs.pdp.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;

/**
 * Defines methods for sending PDP emails.
 */
public interface PdpEmailService {

    /**
     * Sends email for a payment load has failed. Errors encountered will be printed out in message
     * 
     * @param paymentFile parsed payment file object (might not be populated completely due to errors)
     * @param errors <code>ErrorMap</code> containing <code>ErrorMessage</code> entries
     */
    public void sendErrorEmail(PaymentFileLoad paymentFile, MessageMap errors);

    /**
     * Sends email for a successful payment load. Warnings encountered will be printed out in message
     * 
     * @param paymentFile parsed payment file object
     * @param warnings <code>List</code> of <code>String</code> messages
     */
    public void sendLoadEmail(PaymentFileLoad paymentFile, List<String> warnings);

    /**
     * Sends email for a payment load that was held due to tax reasons
     * 
     * @param paymentFile parsed payment file object
     */
    public void sendTaxEmail(PaymentFileLoad paymentFile);

    /**
     * Sends email for a load done internally
     * 
     * @param batch <code>Batch</code> created by load
     */
    public void sendLoadEmail(Batch batch);

    /**
     * Sends email for a purap bundle that exceeds the maximum number of notes allowed
     * 
     * @param creditMemos list of credit memo documents in bundle
     * @param paymentRequests list of payment request documents in bundle
     * @param lineTotal total number of lines for bundle
     * @param maxNoteLines maximum number of lines allowed
     */
    public void sendExceedsMaxNotesWarningEmail(List<String> creditMemos, List<String> paymentRequests, int lineTotal, int maxNoteLines);

    /**
     * Sends summary email for an ACH extract
     * 
     * @param unitCounts Map containing payment counts for each unit
     * @param unitTotals Map containing total payment amount for each unit
     * @param extractDate date of ACH extraction
     */
    public void sendAchSummaryEmail(Map<String, Integer> unitCounts, Map<String, KualiDecimal> unitTotals, Date extractDate);
    
    /**
     * Sends advice notification email to the payee receiving an ACH payment
     * 
     * KFSPTS-1460: 
     * Deprecated this method signature due to need for refactoring to deal with both the unbundled and bundled cases. 
     * The major change is that the paymentDetail input parameter is no longer a singleton and is a list of payment details instead.
     * The caller will no longer loop through the payment detail records calling sendAchAdviceEmail but instead will pass the 
     * entire list of payment detail records and sendAchAdviceEmail will loop through them taking into account cases for
     * multiples and singletons when creating and sending the advice emails.
     * 
     * @param paymentGroup ACH payment group to send notification for
     * @param paymentDetail Payment Detail containing payment amounts
     * @param customer Pdp Customer profile for payment
     */
    @Deprecated
    public void sendAchAdviceEmail(PaymentGroup paymentGroup, PaymentDetail paymentDetail, CustomerProfile customer);

    /**
     * Send advice notification email to the payee receiving an ACH payment for both bundled and unbundled ACH payments.
     * 
     * KFSPTS-1460: 
     * New method signature due to need for refactoring to deal with both the unbundled and bundled cases. 
     * The major change is that the paymentDetail input parameter is now a list of payment details instead of being a singleton.
     * The caller will pass the entire list of payment detail records and sendAchAdviceEmail will loop through them taking into 
     * account cases for multiples and singletons when creating and sending the advice emails.
     * 
     * @param paymentGroup Payment group corresponding to the payment detail records
     * @param paymentDetails List of all payment details to process for the single advice email being sent
     * @param customer Pdp customer profile for payment
     */
    public void sendAchAdviceEmail(PaymentGroup paymentGroup, List<PaymentDetail> paymentDetails, CustomerProfile customer);
    
    /**
     * Sends Payment Cancellation Email
     * 
     * @param paymentGroup
     * @param note
     * @param user
     */
    public void sendCancelEmail(PaymentGroup paymentGroup, String note, Person user);
}
