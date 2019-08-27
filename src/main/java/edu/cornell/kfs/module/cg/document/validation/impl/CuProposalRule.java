package edu.cornell.kfs.module.cg.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cg.document.validation.impl.ProposalRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

@SuppressWarnings("deprecation")
public class CuProposalRule extends ProposalRule {
    private static final Logger LOG = LogManager.getLogger(CuProposalRule.class);
    
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument documentCopy) {
        LOG.debug("Entering ProposalRule.processCustomRouteDocumentBusinessRules");
        boolean success = checkEndAfterBegin(newProposalCopy.getProposalBeginningDate(),
                newProposalCopy.getProposalEndingDate(), KFSPropertyConstants.PROPOSAL_ENDING_DATE);
        success &= checkPrimary(newProposalCopy.getProposalOrganizations(), ProposalOrganization.class,
                KFSPropertyConstants.PROPOSAL_ORGANIZATIONS, Proposal.class);
        success &= checkPrimary(newProposalCopy.getProposalProjectDirectors(), ProposalProjectDirector.class,
                KFSPropertyConstants.PROPOSAL_PROJECT_DIRECTORS, Proposal.class);
        success &= checkProjectDirectorsAreDirectors(newProposalCopy.getProposalProjectDirectors(),
                ProposalProjectDirector.class, KFSPropertyConstants.PROPOSAL_PROJECT_DIRECTORS);
        success &= checkProjectDirectorsExist(newProposalCopy.getProposalProjectDirectors(),
                ProposalProjectDirector.class, KFSPropertyConstants.PROPOSAL_PROJECT_DIRECTORS);
        success &= checkProjectDirectorsStatuses(newProposalCopy.getProposalProjectDirectors(),
                ProposalProjectDirector.class, KFSPropertyConstants.PROPOSAL_PROJECT_DIRECTORS);
        success &= checkFederalPassThrough();
        success &= checkAgencyNotEqualToFederalPassThroughAgency(newProposalCopy.getAgency(),
                newProposalCopy.getFederalPassThroughAgency(), KFSPropertyConstants.AGENCY_NUMBER,
                KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER);
        LOG.info("Leaving ProposalRule.processCustomRouteDocumentBusinessRules");
        return success;
    }
    
    /**
     * Verifies the required federal pass through agency number is filled in when the
     * federal pass through indicator is set.
     * 
     * This method matches the AwardExtensionRule class method of the same name with the same signature. 
     * This logic had to be duplicated because both basecode classes ProposalRule and AwardRule extended
     * basecode class CGMaintenanceDocumentRuleBase with Cornell needing to locally customize both basecode
     * extended implementations.
     */
    protected boolean checkFederalPassThrough() {
        boolean success = true;

        boolean federalPassThroughIndicator = newProposalCopy.getProposalFederalPassThroughIndicator();
        String federalPassThroughAgencyNumber = newProposalCopy.getFederalPassThroughAgencyNumber();

        if (federalPassThroughIndicator && StringUtils.isBlank(federalPassThroughAgencyNumber)) {
            putFieldError(KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER,
                    KFSKeyConstants.ERROR_FPT_AGENCY_NUMBER_REQUIRED);
            success = false;
        }

        return success;
    }
    
}