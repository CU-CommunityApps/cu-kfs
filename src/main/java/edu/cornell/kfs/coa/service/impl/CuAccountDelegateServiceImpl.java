package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.coa.service.impl.AccountDelegateServiceImpl;
import org.springframework.transaction.annotation.Transactional;

public class CuAccountDelegateServiceImpl extends AccountDelegateServiceImpl {
    
    /* FINP-8322:
     * Method signature changed in super class.
     * Needed to update Cornell modification to match.
     */
    @Override
    @Transactional
    public void updateDelegationRole(String docIdToIgnore) {
        // Do nothing, this will be handled by the document requeuer
    }
}
