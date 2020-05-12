package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.module.cg.CuCGPropertyConstants;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;

public class ProposalTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal>
            implements InitializingBean {

	protected BusinessObjectService businessObjectService;
	protected DateTimeService dateTimeService;
	protected ParameterService parameterService;
	protected RassPropertyDefinition grantNumberMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        grantNumberMapping = getPropertyMappings().stream()
                .filter(propertyMapping -> StringUtils.equals(
                        CuCGPropertyConstants.GRANT_NUMBER, propertyMapping.getBoPropertyName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No property mapping was found for Grant Number"));
    }

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
    public String getKeyOfPrimaryObjectUpdateToWaitFor(RassXmlAwardEntry xmlAward) {
        return RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber());
    }

	@Override
	public List<String> getKeysOfUpstreamObjectUpdatesToWaitFor(RassXmlAwardEntry xmlAward) {
		List<String> objectsToWaitFor = new ArrayList<>();
		objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getAgencyNumber()));
		if (StringUtils.isNotBlank(xmlAward.getFederalPassThroughAgencyNumber())) {
		    objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getFederalPassThroughAgencyNumber()));
		}
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
    public boolean businessObjectEditIsPermitted(RassXmlAwardEntry xmlProposal, Proposal oldProposal) {
        String xmlGrantNumber = xmlProposal.getGrantNumber();
        String convertedGrantNumber = (String) grantNumberMapping.getValueConverter().convert(
                getBusinessObjectClass(), grantNumberMapping, xmlGrantNumber);
        return !StringUtils.equals(convertedGrantNumber, oldProposal.getGrantNumber());
    }

    @Override
    public boolean businessObjectCreateIsPermitted(RassXmlAwardEntry xmlObject) {
       return xmlObject.getTotalAmount() != null && xmlObject.getCostShareRequired() != null
               && (xmlObject.getTotalAmount().isNonZero() || xmlObject.getCostShareRequired());
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
