package edu.cornell.kfs.coa.service;

import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.rice.krad.bo.Note;

public interface CuAccountService extends AccountService{
	
	public List<Note> getAccountNotes(Account account);

}
