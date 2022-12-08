package edu.cornell.kfs.fp.service;

import org.kuali.kfs.pdp.service.PaymentMaintenanceService;
import org.kuali.kfs.kim.impl.identity.Person;

public interface RecurringDisbursementVoucherPaymentMaintenanceService {
    
    /**
     * Determines if the passed in user has the authority to cancel payments from a recurring DV.
     * @param user
     * @return
     */
    boolean hasCancelPermission(Person user);
    
    /**
     * This method cancels the pending payment of the given payment id if the following rules apply. -
     * Payment status must be: "open", "held", or "pending/ACH".
     * @param paymentGroupId Primary key of the PaymentGroup that the Payment Detail to be canceled belongs to.
     * @param paymentDetailId Primary key of the PaymentDetail that was actually canceled.
     * @param note Change note text entered by user.
     * @param user The user that cancels the payment
     * @return true if cancel payment succesful, false otherwise
     */
    public boolean cancelPendingPayment(Integer paymentGroupId, Integer paymentDetailId, String note, Person user);

}
