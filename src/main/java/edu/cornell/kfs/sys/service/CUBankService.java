package edu.cornell.kfs.sys.service;

import org.kuali.kfs.sys.businessobject.Bank;

public interface CUBankService {

    Bank getDefaultBankByDocType(String documentTypeCode);

}
