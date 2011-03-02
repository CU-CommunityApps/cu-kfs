package edu.cornell.kfs.module.ezra.service;

import java.sql.Date;

import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.module.ezra.businessobject.Sponsor;

public interface EzraService {

	
	public boolean updateSponsorsSince(Date date);
	public boolean updateProposalsSince(Date date);
	public Agency createAgency(Long sponsorId);
	public void updateAgency(Agency agency, Sponsor sponsor);
	
}
