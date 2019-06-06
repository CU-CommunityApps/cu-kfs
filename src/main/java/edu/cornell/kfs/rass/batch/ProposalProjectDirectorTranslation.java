package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public class ProposalProjectDirectorTranslation extends RassBaseObjectTranslationDefinition<RassXMLAwardPiCoPiEntry, ProposalProjectDirector> {

	@Override
	public Class<RassXMLAwardPiCoPiEntry> getXmlObjectClass() {
		return RassXMLAwardPiCoPiEntry.class;
	}

	@Override
	public Class<ProposalProjectDirector> getBusinessObjectClass() {
		return ProposalProjectDirector.class;
	}

}
