package edu.cornell.kfs.module.ar.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentAction;
import org.kuali.kfs.module.ar.document.web.struts.ContractsGrantsInvoiceDocumentForm;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuContractsGrantsInvoiceDocumentAction extends ContractsGrantsInvoiceDocumentAction {
    private static final Logger LOG = LogManager.getLogger(CuContractsGrantsInvoiceDocumentAction.class);
    
    @Override
    public ActionForward prorateBill(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm = (ContractsGrantsInvoiceDocumentForm) form;
        KualiDecimal budgetTotalAmount = findAwardBudgetTotal(contractsGrantsInvoiceDocumentForm);
        
        if (budgetTotalAmount == null || budgetTotalAmount.isLessEqual(KualiDecimal.ZERO)) {
            String budgetTotalAmountString = budgetTotalAmount != null ? budgetTotalAmount.toString() : "0"; 
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, 
                    CUKFSKeyConstants.ERROR_DOCUMENT_CONTRACT_GRANT_INVOICE_PRORATE_NO_AWARD_BUDGET_TOTAL, budgetTotalAmountString);
            
            LOG.error("prorateBill, Prorate is not valid as the budgetTotalAmount is " + budgetTotalAmount);

            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        
        return super.prorateBill(mapping, form, request, response);
    }

    private KualiDecimal findAwardBudgetTotal(ContractsGrantsInvoiceDocumentForm contractsGrantsInvoiceDocumentForm) {
        ContractsGrantsInvoiceDocument contractsGrantsInvoiceDocument = contractsGrantsInvoiceDocumentForm.getContractsGrantsInvoiceDocument();
        Award award = (Award) contractsGrantsInvoiceDocument.getInvoiceGeneralDetail().getAward();
        AwardExtendedAttribute awardExtension = (AwardExtendedAttribute) award.getExtension();
        KualiDecimal budgetTotalAmount = null;
        if (ObjectUtils.isNotNull(awardExtension)) {
            budgetTotalAmount = awardExtension.getBudgetTotalAmount();
        } else {
            LOG.error("findAwardBudgetTotal, there is no award extension object on award " + award.getProposalNumber());
        }
        
        return budgetTotalAmount;
    }

}
