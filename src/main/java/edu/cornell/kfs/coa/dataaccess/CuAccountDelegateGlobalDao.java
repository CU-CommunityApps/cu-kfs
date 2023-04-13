package edu.cornell.kfs.coa.dataaccess;

import java.util.List;

import org.kuali.kfs.coa.dataaccess.AccountDelegateGlobalDao;

public interface CuAccountDelegateGlobalDao extends AccountDelegateGlobalDao {

    String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber);

}
