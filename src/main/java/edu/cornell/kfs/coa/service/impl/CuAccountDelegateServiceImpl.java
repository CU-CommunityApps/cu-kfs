package edu.cornell.kfs.coa.service.impl;

import org.kuali.kfs.coa.service.impl.AccountDelegateServiceImpl;
import org.springframework.transaction.annotation.Transactional;

public class CuAccountDelegateServiceImpl extends AccountDelegateServiceImpl {
    
    @Override
    @Transactional
    public void updateDelegationRole() {
        // Do nothing, this will be handled by the document requeuer
    }
}
