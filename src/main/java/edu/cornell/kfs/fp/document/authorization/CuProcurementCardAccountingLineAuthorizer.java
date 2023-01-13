package edu.cornell.kfs.fp.document.authorization;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.fp.document.authorization.ProcurementCardAccountingLineAuthorizer;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.sys.KFSConstants.RouteLevelNames;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kim.impl.identity.Person;

public class CuProcurementCardAccountingLineAuthorizer extends ProcurementCardAccountingLineAuthorizer {

    @Override
    protected boolean determineEditPermissionByFieldName(AccountingDocument accountingDocument,
            AccountingLine accountingLine, String fieldName, Person currentUser, Set<String> currentNodes) {
        WorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();

        List<ActionRequest> actionRequests = workflowDocument.getRootActionRequests();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
       boolean isAddHocRoute = CollectionUtils.isNotEmpty(nodeNames) 
                && nodeNames.contains(PurapWorkflowConstants.DOC_ADHOC_NODE_NAME);
        if (!isAddHocRoute) {
            for (ActionRequest actionRequest : actionRequests) {
                isAddHocRoute = actionRequest.getActionTaken() == null && StringUtils.startsWith(actionRequest.getAnnotation(), "Ad Hoc Routed by")
                        && StringUtils.equals(actionRequest.getPrincipalId(), currentUser.getPrincipalId());
                if (isAddHocRoute) {
                    return false;
                }
            }
        } else {
            return false;
        }
        // 1. If this method is called, we know it's a PCDO document here.
        // 2. Check that the document is at AccountFullEdit route node
        if (accountingDocument.getDocumentHeader() != null &&
                accountingDocument.getDocumentHeader().getWorkflowDocument() != null) {
            if (currentNodes != null && currentNodes.contains(RouteLevelNames.ACCOUNT_REVIEW_FULL_EDIT)) {
                // 3. Check that the current user has the permission to edit the document, which means in this case he
                // can edit the accounting line
                if (getDocumentAuthorizer(accountingDocument).canEdit(accountingDocument, currentUser)) {
                    // if above conditions satisfy, then we can skip further validation on permission checking, since
                    // any user that can edit the accounting lines will be able to add/change it to any other account
                    return true;
                }
            }
        }

        return super.determineEditPermissionByFieldName(accountingDocument, accountingLine, fieldName, currentUser, currentNodes);
    }

}
