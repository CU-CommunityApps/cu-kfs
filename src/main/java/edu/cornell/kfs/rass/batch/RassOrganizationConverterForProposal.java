package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;

import edu.cornell.kfs.rass.RassConstants;

public class RassOrganizationConverterForProposal extends RassValueConverterBase {

	@Override
	public Object convert(Class<? extends PersistableBusinessObject> businessObjectClass, RassPropertyDefinition propertyMapping, Object propertyValue) {
		String organizationCode = (String) propertyValue;
		ProposalOrganization proposalOrganization = new ProposalOrganization();
		proposalOrganization.setChartOfAccountsCode(RassConstants.PROPOSAL_ORG_CHART);
		proposalOrganization.setOrganizationCode(organizationCode);
		proposalOrganization.setProposalPrimaryOrganizationIndicator(true);
		List<ProposalOrganization> proposalOrganizations = new ArrayList<>();
		proposalOrganizations.add(proposalOrganization);
		return proposalOrganizations;
	}

}
