package edu.cornell.kfs.module.ezra.service;

import java.sql.Date;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;

public interface EzraService {

	public Agency createAgency();
	public Award createAward();
	public Proposal createProposal();
	
	public boolean updateSponsorsSince(Date date);
	
}
