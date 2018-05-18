package edu.cornell.kfs.module.cg.businessobject;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.AwardSubcontractor;
import org.kuali.kfs.module.cg.businessobject.LetterOfCreditFundGroup;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.module.cg.businessobject.ProposalSubcontractor;
import org.kuali.kfs.krad.util.ObjectUtils;

public class CuAward extends Award {

    private String letterOfCreditFundGroupCode;

    private LetterOfCreditFundGroup letterOfCreditFundGroup;
    
    private transient String invoiceLink;

    public CuAward() {
        super();
    }

    public CuAward(Proposal proposal) {
        this();
        populateFromProposal(proposal);
    }

    /**
     * This method takes all the applicable attributes from the associated proposal object and sets those attributes into their
     * corresponding award attributes.
     *
     * @param proposal The associated proposal that the award will be linked to.
     */
    public void populateFromProposal(Proposal proposal) {
        if (ObjectUtils.isNotNull(proposal)) {
            setProposalNumber(proposal.getProposalNumber());
            setAgencyNumber(proposal.getAgencyNumber());
            setAwardProjectTitle(proposal.getProposalProjectTitle());
            setAwardDirectCostAmount(proposal.getProposalDirectCostAmount());
            setAwardIndirectCostAmount(proposal.getProposalIndirectCostAmount());
            setProposalAwardTypeCode(proposal.getProposalAwardTypeCode());
            setFederalPassThroughIndicator(proposal.getProposalFederalPassThroughIndicator());
            setFederalPassThroughAgencyNumber(proposal.getFederalPassThroughAgencyNumber());
            setAwardPurposeCode(proposal.getProposalPurposeCode());

            // copy proposal organizations to award organizations
            getAwardOrganizations().clear();
            for (ProposalOrganization pOrg : proposal.getProposalOrganizations()) {
                AwardOrganization awardOrg = new AwardOrganization();
                // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                awardOrg.setNewCollectionRecord(true);
                awardOrg.setProposalNumber(pOrg.getProposalNumber());
                awardOrg.setChartOfAccountsCode(pOrg.getChartOfAccountsCode());
                awardOrg.setOrganizationCode(pOrg.getOrganizationCode());
                awardOrg.setAwardPrimaryOrganizationIndicator(pOrg.isProposalPrimaryOrganizationIndicator());
                awardOrg.setActive(pOrg.isActive());
                awardOrg.setVersionNumber(pOrg.getVersionNumber());
                getAwardOrganizations().add(awardOrg);
            }

            // copy proposal subcontractors to award subcontractors
            getAwardSubcontractors().clear();
            for (ProposalSubcontractor pSubcontractor : proposal.getProposalSubcontractors()) {
                AwardSubcontractor awardSubcontractor = new AwardSubcontractor();
                // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                awardSubcontractor.setNewCollectionRecord(true);
                awardSubcontractor.setProposalNumber(pSubcontractor.getProposalNumber());
                awardSubcontractor.setAwardSubcontractorNumber(pSubcontractor.getProposalSubcontractorNumber());
                awardSubcontractor.setSubcontractorAmount(pSubcontractor.getProposalSubcontractorAmount());
                awardSubcontractor.setAwardSubcontractorDescription(pSubcontractor.getProposalSubcontractorDescription());
                awardSubcontractor.setSubcontractorNumber(pSubcontractor.getSubcontractorNumber());
                awardSubcontractor.setActive(pSubcontractor.isActive());
                awardSubcontractor.setVersionNumber(pSubcontractor.getVersionNumber());
                getAwardSubcontractors().add(awardSubcontractor);
            }

            // copy proposal project directors to award propject directors
            getAwardProjectDirectors().clear();
            Set<String> directors = new HashSet<String>(); // use this to filter out duplicate projectdirectors
            for (ProposalProjectDirector pDirector : proposal.getProposalProjectDirectors()) {
                if (directors.add(pDirector.getPrincipalId())) {
                    AwardProjectDirector awardDirector = new AwardProjectDirector();
                    // newCollectionRecord is set to true to allow deletion of this record after being populated from proposal
                    awardDirector.setNewCollectionRecord(true);
                    awardDirector.setProposalNumber(pDirector.getProposalNumber());
                    awardDirector.setAwardPrimaryProjectDirectorIndicator(pDirector.isProposalPrimaryProjectDirectorIndicator());
                    awardDirector.setAwardProjectDirectorProjectTitle(pDirector.getProposalProjectDirectorProjectTitle());
                    awardDirector.setPrincipalId(pDirector.getPrincipalId());
                    awardDirector.setActive(pDirector.isActive());
                    awardDirector.setVersionNumber(pDirector.getVersionNumber());
                    getAwardProjectDirectors().add(awardDirector);
                }
            }
        }
    }

    public String getLetterOfCreditFundGroupCode() {
        return letterOfCreditFundGroupCode;
    }

    public void setLetterOfCreditFundGroupCode(String letterOfCreditFundGroupCode) {
        this.letterOfCreditFundGroupCode = letterOfCreditFundGroupCode;
    }

    public LetterOfCreditFundGroup getLetterOfCreditFundGroup() {
        return letterOfCreditFundGroup;
    }

    public void setLetterOfCreditFundGroup(LetterOfCreditFundGroup letterOfCreditFundGroup) {
        this.letterOfCreditFundGroup = letterOfCreditFundGroup;
    }

    public String getInvoiceLink() {
        return invoiceLink;
    }

    public void setInvoiceLink(String invoiceLink) {
        this.invoiceLink = invoiceLink;
    }

}
