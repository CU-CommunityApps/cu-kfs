package edu.cornell.kfs.rass.batch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.service.AwardService;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.module.cg.CuCGPropertyConstants;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.util.RassUtil;

public class AwardTranslationDefinition extends RassObjectTranslationDefinition<RassXmlAwardEntry, Award> {

    private AwardService awardService;

    public void setAwardService(AwardService awardService) {
        this.awardService = awardService;
    }

    @Override
    public String printPrimaryKeyValues(RassXmlAwardEntry xmlAward) {
        return xmlAward.getProposalNumber();
    }

    @Override
    public String printPrimaryKeyValues(Award award) {
        return award.getProposalNumber();
    }

    @Override
    public List<String> getKeysOfObjectUpdatesToWaitFor(RassXmlAwardEntry xmlAward) {
        List<String> objectsToWaitFor = new ArrayList<>();
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getAgencyNumber()));
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()));
        objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Award.class, xmlAward.getProposalNumber()));
        if (StringUtils.isNotBlank(xmlAward.getFederalPassThroughAgencyNumber())) {
            objectsToWaitFor.add(RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getFederalPassThroughAgencyNumber()));
        }
        return objectsToWaitFor;
    }

    @Override
    public void processCustomTranslationForBusinessObjectEdit(
            RassXmlAwardEntry xmlAward, Award oldAward, Award newAward) {
        refreshReferenceObject(newAward, KFSPropertyConstants.PROPOSAL);
        refreshReferenceObject(newAward, KFSPropertyConstants.AGENCY);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_STATUS);
        refreshReferenceObject(newAward, CuCGPropertyConstants.AWARD_PURPOSE);
        refreshReferenceObject(newAward, CuCGPropertyConstants.GRANT_DESCRIPTION);
        refreshReferenceObject(newAward, KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY);
    }

    @Override
    public Award findExistingObject(RassXmlAwardEntry xmlAward) {
        return awardService.getByPrimaryId(xmlAward.getProposalNumber());
    }

    @Override
    public Class<RassXmlAwardEntry> getXmlObjectClass() {
        return RassXmlAwardEntry.class;
    }

    @Override
    public Class<Award> getBusinessObjectClass() {
        return Award.class;
    }

}
