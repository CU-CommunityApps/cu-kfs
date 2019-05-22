package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;

public class RassOrganizationConverter extends RassValueConverterBase{

	public Object convert(Object rassInput) {
		ProposalOrganization proposalOrganization = new ProposalOrganization();
		proposalOrganization.setChartOfAccountsCode("IT");
		proposalOrganization.setOrganizationCode((String)rassInput);
		proposalOrganization.setProposalPrimaryOrganizationIndicator(true);
		List<ProposalOrganization> proposalOrganizations = new ArrayList<>();
		proposalOrganizations.add(proposalOrganization);
		return proposalOrganizations;
	}

}
