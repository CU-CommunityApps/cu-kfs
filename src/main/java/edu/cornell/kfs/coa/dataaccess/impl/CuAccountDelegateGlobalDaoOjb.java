package edu.cornell.kfs.coa.dataaccess.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.kuali.kfs.coa.dataaccess.impl.AccountDelegateGlobalDaoOjb;

import edu.cornell.kfs.coa.dataaccess.CuAccountDelegateGlobalDao;

public class CuAccountDelegateGlobalDaoOjb extends AccountDelegateGlobalDaoOjb implements CuAccountDelegateGlobalDao {

    @Override
    public List<String> createSearchPatternsFromLockingRepresentations(List<String> lockingRepresentations) {
        return lockingRepresentations.stream()
                .map(this::convertForLikeCriteria)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

}
