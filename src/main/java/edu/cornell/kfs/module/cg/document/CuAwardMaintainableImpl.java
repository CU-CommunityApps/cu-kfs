package edu.cornell.kfs.module.cg.document;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.document.AwardMaintainableImpl;

import edu.cornell.kfs.module.cg.businessobject.AwardAccountExtendedAttribute;

public class CuAwardMaintainableImpl extends AwardMaintainableImpl {

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
