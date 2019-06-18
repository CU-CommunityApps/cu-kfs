package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;

public class ProposalTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> {

	protected BusinessObjectService businessObjectService;
	protected DateTimeService dateTimeService;
	protected ParameterService parameterService;

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
		objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()));
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
		Proposal proposal = (Proposal) businessObjectService.findByPrimaryKey(Proposal.class, fields);
		return proposal;
	}

	@Override
	public boolean businessObjectEditIsPermitted(RassXmlAwardEntry xmlObject, Proposal oldBusinessObject) {
		return false;
	}

	@Override
	public void processCustomTranslationForBusinessObjectCreate(RassXmlAwardEntry xmlObject,
			Proposal newBusinessObject) {
		super.processCustomTranslationForBusinessObjectCreate(xmlObject, newBusinessObject);
		Proposal proposal = (Proposal) newBusinessObject;
		proposal.setProposalSubmissionDate(dateTimeService.getCurrentSqlDate());
		String rassProposalAwardType = parameterService.getParameterValueAsString(RassStep.class,
				RassConstants.RASS_DEFAULT_PROPOSAL_AWARD_TYPE_PARAMETER);
		proposal.setProposalAwardTypeCode(rassProposalAwardType);
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

}
