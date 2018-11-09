package edu.cornell.kfs.module.ar.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.validation.impl.TotalAmountBilledToDateExceedsAwardTotalSuspensionCategory;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.ar.CuArConstants;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory extends TotalAmountBilledToDateExceedsAwardTotalSuspensionCategory {
    private static final Logger LOG = LogManager.getLogger(CuTotalAmountBilledToDateExceedsAwardTotalSuspensionCategory.class);
    
    protected transient ParameterService parameterService;
    
    @Override
    public boolean shouldSuspend(ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument) {
        if (shouldValidateOnBudgetTotal()) {
            LOG.debug("shouldSuspend, validating based on award budget total");
            KualiDecimal totalAmountBilledToDate = contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getTotalAmountBilledToDate();
            
            Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
            AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
            KualiDecimal budgetTotalAmount = awardExtension.getBudgetTotalAmount();
            if (ObjectUtils.isNull(budgetTotalAmount)) {
                LOG.error("shouldSuspend, no budget amount set, setting budget amount to 0, which will cause a suspension.");
                budgetTotalAmount = KualiDecimal.ZERO;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("shouldSuspend, award proposal number: " + award.getProposalNumber() + " totalAmountBilledToDate: " + totalAmountBilledToDate + " budgetTotalAmount: " + budgetTotalAmount);
            }
            return totalAmountBilledToDate.isGreaterThan(budgetTotalAmount);
        } else {
            LOG.debug("shouldSuspend, validating based on award total");
            return super.shouldSuspend(contractsGrantsInvoiceDocument);
        }
    }
    
    protected boolean shouldValidateOnBudgetTotal() {
        Boolean validateOnBudgetTotal = parameterService.getParameterValueAsBoolean(ArConstants.AR_NAMESPACE_CODE, ArConstants.CONTRACTS_GRANTS_INVOICE_COMPONENT, 
                CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL, Boolean.TRUE);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("shouldValidateOnBudgetTotal, the value of " + CuArConstants.CG_INVOICE_AMT_BILLED_SUSPENSION_BY_BUDGET_TOTAL + " is " + validateOnBudgetTotal.booleanValue());
        }
        
        return validateOnBudgetTotal.booleanValue();
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
