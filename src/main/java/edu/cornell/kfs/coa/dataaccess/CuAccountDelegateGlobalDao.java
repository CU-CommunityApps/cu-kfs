package edu.cornell.kfs.coa.dataaccess;

import java.util.List;

import org.kuali.kfs.coa.dataaccess.AccountDelegateGlobalDao;

public interface CuAccountDelegateGlobalDao extends AccountDelegateGlobalDao {

    List<String> createSearchPatternsFromLockingRepresentations(List<String> lockingRepresentations);

}
