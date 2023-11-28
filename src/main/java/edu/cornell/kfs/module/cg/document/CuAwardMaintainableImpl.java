package edu.cornell.kfs.module.cg.document;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.document.AwardMaintainableImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.module.cg.businessobject.AwardAccountExtendedAttribute;

public class CuAwardMaintainableImpl extends AwardMaintainableImpl {

    /**
     * Load related objects from the database as needed.
     *
     * @param refreshFromLookup
     */
    @Override
    protected void refreshAward(final boolean refreshFromLookup) {       
        final Award award = getAward();
        final Proposal tempProposal = getAward().getProposal();
        award.setProposal(tempProposal);
        award.clearCustomerAddressIfNecessary();
        
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
    
    @Override
    public void prepareForSave() {
        super.prepareForSave();
        if (CollectionUtils.isNotEmpty(getAward().getAwardAccounts())) {
            for (final AwardAccount awardAccount : getAward().getAwardAccounts()) {
                AwardAccountExtendedAttribute extension;
                if (ObjectUtils.isNotNull(awardAccount.getExtension())) {
                    extension = (AwardAccountExtendedAttribute) awardAccount.getExtension();
                } else {
                    extension = new AwardAccountExtendedAttribute();
                    awardAccount.setExtension(extension);
                }
                extension.setAccountNumber(awardAccount.getAccountNumber());
                extension.setChartOfAccountsCode(awardAccount.getChartOfAccountsCode());
                extension.setProposalNumber(getAward().getProposalNumber());
            }
        }
    }

}
