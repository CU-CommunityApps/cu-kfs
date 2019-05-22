package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public class RassProjectDirectorConverter extends RassValueConverterBase {
	
	
	@Override
	public Object convert(Object rassInput) {
		List<RassXMLAwardPiCoPiEntry> rassAwardPiCoPiEntries = (List<RassXMLAwardPiCoPiEntry>) rassInput;
		List<ProposalProjectDirector> projectDirectors = new ArrayList<>();
		if(rassAwardPiCoPiEntries !=null && rassAwardPiCoPiEntries.size()>0) {
			for(RassXMLAwardPiCoPiEntry rassAwardPiCoPi : rassAwardPiCoPiEntries) {
				ProposalProjectDirector projectDirector = new ProposalProjectDirector();
				String principalId = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(rassAwardPiCoPi.getProjectDirectorPrincipalName()).getPrincipalId();
				projectDirector.setPrincipalId(principalId);
				projectDirector.setProposalPrimaryProjectDirectorIndicator(rassAwardPiCoPi.getPrimary());
				projectDirectors.add(projectDirector);
			}
		}
		
		return projectDirectors;
	}

}
