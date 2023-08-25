package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Map;
import java.util.Set;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.doctype.RoutePath;
import org.kuali.kfs.kew.engine.node.ProcessDefinition;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;

import edu.cornell.kfs.module.purap.document.IWantDocument;

@SuppressWarnings("deprecation")
public class IWantDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

    private static final long serialVersionUID = 1L;
    
    public Set<String> getDocumentActions(Document document, Person user,
            Set<String> documentActionsFromPresentationController) {
        Set<String> documentActionsToReturn = super.getDocumentActions(document, user,
                documentActionsFromPresentationController);

        if (!documentActionsToReturn.contains(KRADConstants.KUALI_ACTION_CAN_SEND_NOTE_FYI) && canSendNoteFyi(document, user)) {
            documentActionsToReturn.add(KRADConstants.KUALI_ACTION_CAN_SEND_NOTE_FYI);
        }
        return documentActionsToReturn;
    }

    @Override
    protected void addPermissionDetails(Object dataObject, Map<String, String> attributes) {
        super.addPermissionDetails(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    @Override
    protected void addRoleQualification(Object dataObject, Map<String, String> attributes) {
        super.addRoleQualification(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    
    @Override
    public boolean canSendAnyTypeAdHocRequests(Document document, Person user) {
//        if (canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_FYI_REQ, user)) {
//            RoutePath routePath = KEWServiceLocator.getDocumentTypeService().getRoutePathForDocumentTypeName(
//                document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
//            ProcessDefinition processDefinition = routePath.getPrimaryProcess();
//            if (processDefinition != null) {
//                return processDefinition.getInitialRouteNode() != null;
//            } else {
//                return false;
//            }
//        } else if (canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, user)) {
//            return true;
//        }
        return canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_APPROVE_REQ, user);
    }
    

    /*
     * CU Customization (KFSPTS-2270): Updated authorizer to allow editing of document overview/description
     * by more users than just the initiator.
     */
    @Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document,
                KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT,
                user.getPrincipalId());
    }
}
