package edu.cornell.kfs.coa.dataaccess;

import java.util.List;

public interface AccountDelegateGlobalLockDao {

    String getAnyLockingDocumentNumber(List<String> lockingRepresentations, String documentNumber);

}
