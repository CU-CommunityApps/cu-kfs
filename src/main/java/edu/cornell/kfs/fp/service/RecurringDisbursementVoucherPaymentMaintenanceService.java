package edu.cornell.kfs.fp.service;

import org.kuali.kfs.pdp.service.PaymentMaintenanceService;
import org.kuali.rice.kim.api.identity.Person;

public interface RecurringDisbursementVoucherPaymentMaintenanceService extends PaymentMaintenanceService {
    
    /**
     * Determines if the passed in user has the authority to cancel payments from a recurring DV.
     * @param user
     * @return
     */
    boolean hasCancelPermission(Person user);
}
