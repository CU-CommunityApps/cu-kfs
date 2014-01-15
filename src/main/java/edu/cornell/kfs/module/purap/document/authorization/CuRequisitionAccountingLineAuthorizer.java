package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.authorization.RequisitionAccountingLineAuthorizer;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.WorkflowDocument;

import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;

public class CuRequisitionAccountingLineAuthorizer extends RequisitionAccountingLineAuthorizer {

    @Override
    public boolean renderNewLine(AccountingDocument accountingDocument, String accountingGroupProperty) {
        WorkflowDocument workflowDocument = ((PurchasingAccountsPayableDocument) accountingDocument).getFinancialSystemDocumentHeader().getWorkflowDocument();

        Set<String> currentNodes = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentNodes) && (currentNodes.contains(RequisitionAccountingLineAuthorizer.INITIATOR_NODE)
                || currentNodes.contains(RequisitionAccountingLineAuthorizer.CONTENT_REVIEW_NODE))
                || (currentNodes.contains(RequisitionStatuses.NODE_ACCOUNT)                 
                && SpringContext.getBean(CuPurapAccountingService.class).isFiscalOfficersForAllAcctLines((RequisitionDocument) accountingDocument))) {
            return true;
        }
        return super.renderNewLine(accountingDocument, accountingGroupProperty);
    }
    

}
