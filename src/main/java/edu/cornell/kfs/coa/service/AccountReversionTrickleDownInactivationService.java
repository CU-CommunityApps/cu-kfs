package edu.cornell.kfs.coa.service;

import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;

public interface AccountReversionTrickleDownInactivationService {

    /**
     * Generates the trickle down maintenance locks.
     * 
     * @param inactivatedAccount
     * @param documentNumber
     * @return a list of maintenance locks
     */
    public List<MaintenanceLock> generateTrickleDownMaintenanceLocks(Account inactivatedAccount, String documentNumber);

    /**
     * Inactivates all related AccountReversion rules. If an AccountReversion rule has the account, cash account or budget
     * account equal the account being inactivated then the AccountReversion rule is also inactivated.
     * 
     * @param inactivatedAccount
     * @param documentNumber
     */
    public void trickleDownInactivateAccountReversions(Account inactivatedAccount, String documentNumber);

}
