package edu.cornell.kfs.pdp.service;

import java.util.List;

import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.PdpEmailService;

public interface CuPdpEmailService extends PdpEmailService {
	
    
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
     * @param customer Pdp Customer profile for payment
     */
    @Deprecated
    public void sendAchAdviceEmail(final PaymentGroup paymentGroup, final CustomerProfile customer);

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
    

}
