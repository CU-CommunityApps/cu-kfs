package edu.cornell.kfs.module.cg.document;

import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.document.AwardMaintainableImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;

public class CuAwardMaintainableImpl extends AwardMaintainableImpl {

    /**
     * Load related objects from the database as needed.
     *
     * @param refreshFromLookup
     */
    protected void refreshAward(boolean refreshFromLookup) {       
        Award award = getAward();
        Proposal tempProposal = getAward().getProposal();
        award.setProposal(tempProposal);
        
        getNewCollectionLine(KFSPropertyConstants.AWARD_SUBCONTRACTORS).refreshNonUpdateableReferences();
        getNewCollectionLine(KFSPropertyConstants.AWARD_PROJECT_DIRECTORS).refreshNonUpdateableReferences();
        getNewCollectionLine(KFSPropertyConstants.AWARD_FUND_MANAGERS).refreshNonUpdateableReferences();
        getNewCollectionLine(KFSPropertyConstants.AWARD_ACCOUNTS).refreshNonUpdateableReferences();

        // the org list doesn't need any refresh
        refreshNonUpdateableReferences(award.getAwardOrganizations());
        refreshNonUpdateableReferences(award.getAwardAccounts());
        refreshNonUpdateableReferences(award.getAwardSubcontractors());
        refreshAwardProjectDirectors(refreshFromLookup);
    }

}
