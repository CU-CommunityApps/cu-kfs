package edu.cornell.kfs.module.ar.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.TotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl extends TotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CuTotalAmountBilledToDateExceedsTotalBudgetSuspensionCategoryServiceImpl.class);
    
    @Override
    public boolean shouldSuspend(final ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        LOG.debug("shouldSuspend, validating based on award budget total");
        final KualiDecimal totalAmountBilledToDate = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getTotalAmountBilledToDate();
        
        final Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        final AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        KualiDecimal budgetTotalAmount = awardExtension.getBudgetTotalAmount();
        if (ObjectUtils.isNull(budgetTotalAmount)) {
            LOG.error("shouldSuspend, no budget amount set, setting budget amount to 0, which will cause a suspension.");
            budgetTotalAmount = KualiDecimal.ZERO;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("shouldSuspend, award proposal number: " + award.getProposalNumber() + " totalAmountBilledToDate: " + totalAmountBilledToDate + " budgetTotalAmount: " + budgetTotalAmount);
        }
        return totalAmountBilledToDate.isGreaterThan(budgetTotalAmount);
    }

}
