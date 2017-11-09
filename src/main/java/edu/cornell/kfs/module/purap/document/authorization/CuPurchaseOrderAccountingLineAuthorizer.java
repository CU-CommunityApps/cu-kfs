package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionStatuses;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.authorization.PurapAccountingLineAuthorizer;
import org.kuali.kfs.module.purap.document.authorization.PurchaseOrderAccountingLineAuthorizer;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;



public class CuPurchaseOrderAccountingLineAuthorizer extends PurchaseOrderAccountingLineAuthorizer {
    
        /**
         * Allow new lines to be rendered at NewUnorderedItems node
         * @see org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase#renderNewLine
         * (org.kuali.kfs.sys.document.AccountingDocument, java.lang.String)
         */
    @Override
    public boolean renderNewLine(AccountingDocument accountingDocument, String accountingGroupProperty) {
        WorkflowDocument workflowDocument = ((PurchasingAccountsPayableDocument) accountingDocument)
                .getFinancialSystemDocumentHeader().getWorkflowDocument();

        Set<String> currentRouteNodeName = workflowDocument.getCurrentNodeNames();
 
        if (accountingDocument instanceof PurchaseOrderAmendmentDocument
                && currentRouteNodeName.contains(RequisitionStatuses.NODE_ACCOUNT) 
                && SpringContext.getBean(CuPurapAccountingService.class).isFiscalOfficerForAcctLine((PurchaseOrderAmendmentDocument) accountingDocument)) {
            return true;
        }
        
        return super.renderNewLine(accountingDocument, accountingGroupProperty);
    
    }
                
    @Override
    public boolean hasEditPermissionOnAccountingLine(AccountingDocument accountingDocument, AccountingLine accountingLine,
            String accountingLineCollectionProperty, Person currentUser, boolean pageIsEditable, Set<String> currentNodes) {
        
        WorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();
        if (accountingDocument instanceof PurchaseOrderAmendmentDocument) {
            if (workflowDocument.isEnroute() && SpringContext.getBean(CuPurapAccountingService.class)
                    .isFiscalOfficerForAccountingLine(currentUser, accountingLine)) {
                return true;
            }
        }                                
        return super.hasEditPermissionOnAccountingLine(accountingDocument, accountingLine, accountingLineCollectionProperty, currentUser, pageIsEditable, currentNodes);
    }    

//    @Override
//    public boolean determineEditPermissionOnLine(AccountingDocument accountingDocument, AccountingLine accountingLine,
//            String accountingLineCollectionProperty, boolean currentUserIsDocumentInitiator, boolean pageIsEditable) {
//        WorkflowDocument workflowDocument = ((PurchasingAccountsPayableDocument) accountingDocument).getFinancialSystemDocumentHeader().getWorkflowDocument();
//
//        Set<String> currentRouteNodeName = workflowDocument.getCurrentNodeNames();
//        if (accountingDocument instanceof PurchaseOrderAmendmentDocument
//                && currentRouteNodeName.contains(RequisitionStatuses.NODE_ACCOUNT) 
//                && SpringContext.getBean(CuPurapAccountingService.class).isFiscalOfficersForAllAcctLines((PurchaseOrderAmendmentDocument) accountingDocument)) {
//            return true;
//        }
//        return super.determineEditPermissionOnLine(accountingDocument, accountingLine,
//                accountingLineCollectionProperty, currentUserIsDocumentInitiator, pageIsEditable);            
//    }
}
