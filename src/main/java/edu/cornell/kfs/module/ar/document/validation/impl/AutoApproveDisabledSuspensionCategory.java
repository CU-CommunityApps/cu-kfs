package edu.cornell.kfs.module.ar.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.validation.SuspensionCategoryBase;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class AutoApproveDisabledSuspensionCategory extends SuspensionCategoryBase {
    
    private static final Logger LOG = LogManager.getLogger(AutoApproveDisabledSuspensionCategory.class);

    @Override
    public boolean shouldSuspend(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        if (award.getAutoApproveIndicator()) {
            LOG.info("shouldSuspend, auto apporve is selected so no need to suspend");
            return false;
        } else {
            AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
            LOG.info("shouldSuspend, auto apporve is NOT selected so suspend!");
            return true;
        }
    }

}
