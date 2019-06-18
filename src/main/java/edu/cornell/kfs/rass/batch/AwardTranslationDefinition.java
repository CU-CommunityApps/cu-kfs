package edu.cornell.kfs.rass.batch;

import java.util.Arrays;
import java.util.List;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.service.AwardService;

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
        return Arrays.asList(
                RassUtil.buildClassAndKeyIdentifier(Agency.class, xmlAward.getAgencyNumber()),
                RassUtil.buildClassAndKeyIdentifier(Proposal.class, xmlAward.getProposalNumber()),
                RassUtil.buildClassAndKeyIdentifier(Award.class, xmlAward.getProposalNumber())
        );
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
