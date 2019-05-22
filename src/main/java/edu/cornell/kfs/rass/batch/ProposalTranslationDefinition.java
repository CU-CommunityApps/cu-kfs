package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;

public class ProposalTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> {

	protected BusinessObjectService businessObjectService;
	protected RassBaseObjectTranslationDefinition<RassXMLAwardPiCoPiEntry, ProposalProjectDirector> proposalProjectDirectorDefinition;
	protected DateTimeService dateTimeService;
	
	@Override
	public Class<RassXmlAwardEntry> getXmlObjectClass() {
		return RassXmlAwardEntry.class;
	}

	@Override
	public Class<Proposal> getBusinessObjectClass() {
		return Proposal.class;
	}

	@Override
	public String printPrimaryKeyValues(RassXmlAwardEntry xmlAward) {
		return xmlAward.getProposalNumber();
	}

	@Override
	public String printPrimaryKeyValues(Proposal proposal) {
		return proposal.getProposalNumber();
	}

	@Override
	public List<String> getKeysOfObjectUpdatesToWaitFor(RassXmlAwardEntry xmlAward) {
        List<String> objectsToWaitFor = new ArrayList<>();
        objectsToWaitFor.add(
                RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()));
        return objectsToWaitFor;
	}

	@Override
	public Proposal findExistingObject(RassXmlAwardEntry xmlAward) {
	       if (StringUtils.isBlank(xmlAward.getProposalNumber())) {
	            throw new RuntimeException("Attempted to search for a Proposal with a blank Proposal Number");
	        }
	   	return getProposalByPrimaryKey(xmlAward.getProposalNumber());


	}
	
	private Proposal getProposalByPrimaryKey(String proposalNumber) {
		Map<String, String> fields = new HashMap<>();
		fields.put("proposalNumber", proposalNumber);
		Proposal proposal = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
		return proposal;
	}
	
	@Override
	public void processCustomTranslationForBusinessObjectEdit(RassXmlAwardEntry xmlObject, Proposal oldBusinessObject,
			Proposal newBusinessObject) {
		// TODO Auto-generated method stub
		super.processCustomTranslationForBusinessObjectEdit(xmlObject, oldBusinessObject, newBusinessObject);
	}
	
	@Override
	public void processCustomTranslationForBusinessObjectCreate(RassXmlAwardEntry xmlObject,
			Proposal newBusinessObject) {
		// TODO Auto-generated method stub
		super.processCustomTranslationForBusinessObjectCreate(xmlObject, newBusinessObject);
		Proposal proposal = (Proposal)newBusinessObject;
		proposal.setProposalSubmissionDate(dateTimeService.getCurrentSqlDate());
		proposal.setProposalAwardTypeCode("S");//default value for RASS
	}
	
	public void processCustomTranslationForBusinessObject(RassXmlAwardEntry proposalXmlObject,
			Proposal proposalBusinessObject) {
		
		
		proposalXmlObject.getPrincipalAndCoPrincipalInvestigators();
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public RassBaseObjectTranslationDefinition<RassXMLAwardPiCoPiEntry, ProposalProjectDirector> getProposalProjectDirectorDefinition() {
		return proposalProjectDirectorDefinition;
	}

	public void setProposalProjectDirectorDefinition(
			RassBaseObjectTranslationDefinition<RassXMLAwardPiCoPiEntry, ProposalProjectDirector> proposalProjectDirectorDefinition) {
		this.proposalProjectDirectorDefinition = proposalProjectDirectorDefinition;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

}
