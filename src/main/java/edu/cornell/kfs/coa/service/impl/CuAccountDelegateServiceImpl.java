package edu.cornell.kfs.coa.service.impl;

import java.util.Set;

import org.kuali.kfs.coa.service.impl.AccountDelegateServiceImpl;
import org.springframework.transaction.annotation.Transactional;

public class CuAccountDelegateServiceImpl extends AccountDelegateServiceImpl {
    
    @Override
    @Transactional
    public void updateDelegationRole(
            final String docIdToIgnore,
            final Set<String> accountNumbers,
            final Set<String> documentTypes
    ) {
        // Do nothing, this will be handled by the document requeuer
    }
}
