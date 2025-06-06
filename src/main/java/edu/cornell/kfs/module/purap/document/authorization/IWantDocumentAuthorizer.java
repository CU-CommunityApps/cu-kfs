package edu.cornell.kfs.module.purap.document.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;

@SuppressWarnings("deprecation")
public class IWantDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

    private static final long serialVersionUID = 1L;
    
    private static WorkflowDocumentService workflowDocumentService;
    private static RoleService roleService; 
    
    /*
     * Customization to ensure the canSendNoteFyi action is available when the canSendNoteFyi is true. 
     * This action might have been be removed in the super call if the canSendAdHocRequests returns false.
     */
    @Override
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
    public Set<String> getEditModes(Document document, Person user, Set<String> editModes) {
        Set<String> result = super.getEditModes(document, user, editModes);
        
        if (!canViewContractTab(document, user)) {
            result.remove(CUPurapConstants.IWNT_DOC_DISPLAY_CONTRACT_TAB);
        }
        if (!canEditContractIndicator(document, user)) {
            result.remove(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR);
        }
        
        if (!isInProcurementAssistantNode(document)) {
            result.remove(CUPurapConstants.IWNT_DOC_RETURN_TO_SSC);
        }
        
        if (!canEditProcurementAssistantNetId(user)) {
            result.remove(CUPurapConstants.I_WANT_DOC_EDIT_PROCUREMENT_ASSISTANT_NET_ID);
        }
        
        return result;
    }

    /*
     * Only approvers should be able to ad hoc route for approval.
     */
    @Override
    public boolean canSendAnyTypeAdHocRequests(Document document, Person user) {
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
    
    @Override
    public boolean canSave(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canSave(document, user);
        }
    }
    
    @Override
    public boolean canReload(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canReload(document, user);
        }
    }
    
    @Override
    public boolean canClose(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canClose(document, user);
        }
    }
    
    @Override
    public boolean canCopy(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canCopy(document, user);
        }
    }
    
    private boolean isDocumentAtConfirmationStep(Document document) {
        IWantDocument iWantDocument = (IWantDocument) document;
        return CUPurapConstants.IWantDocumentSteps.CONFIRM_STEP.equalsIgnoreCase(iWantDocument.getStep());
    }
    
    public boolean canViewContractTab(Document document, Person person) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        if (!workflowDocument.isInitiated() && !workflowDocument.isSaved()) {
            return true;
        }

        return isInOrgHierarchyOrProcurementAssistantNode(document) &&
                (isCurrentUserOrgReviewer(person) || isCurrentUserProcurementContractAssistant(person));
    }

    /*
     * We restrict the editing of the Contract Tab contents on IWNT docs to enroute status at the
     * OrganizationHierarchy node and ProcurementContractAssistant node.
     */
    public boolean canEditContractIndicator(Document document, Person user) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        boolean isInApproversNode = isInApproversNode(document, user);
        
        return workflowDocument.isEnroute()
                && isInOrgHierarchyOrProcurementAssistantNode(document)
                && isInApproversNode;
    }
    
    private boolean isInApproversNode(Document document, Person currentUser) {
        
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();

        if (CollectionUtils.isNotEmpty(nodeNames)) {
            for (String nodeName : nodeNames) {
                List<ActionRequest> actionRequests = getWorkflowDocumentService().getActionRequestsForPrincipalAtNode(
                        document.getDocumentNumber(), nodeName, currentUser.getPrincipalId());
                for (ActionRequest actionRequest : actionRequests) {
                    if (actionRequest.isApproveRequest()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isInOrgHierarchyOrProcurementAssistantNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            return nodeNames.contains(KFSConstants.RouteLevelNames.ORGANIZATION_HIERARCHY)
                    || nodeNames.contains(CUPurapConstants.IWantRouteNodes.PROCUREMENT_CONTRACT_ASSISTANT);
        }
        return false;
    }
    
    private boolean isInProcurementAssistantNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            return nodeNames.contains(CUPurapConstants.IWantRouteNodes.PROCUREMENT_CONTRACT_ASSISTANT);
        }
        return false;
    }
    
    private boolean canEditProcurementAssistantNetId(Person currentUser) {
        return isCurrentUserProcurementContractAssistant(currentUser);
    }
    
    private boolean isCurrentUserInRole(String namespace, String roleName, Person currentUser) {
        List<String> roleIds = new ArrayList<String>();
        roleIds.add(getRoleService().getRoleIdByNamespaceCodeAndName(namespace, roleName));
        Map<String,String> roleQualifier = new HashMap<String,String>();
              
        return getRoleService().principalHasRole(currentUser.getPrincipalId(), roleIds, roleQualifier);
    }
    
    private boolean isCurrentUserOrgReviewer(Person currentUser) {
        return isCurrentUserInRole(KFSConstants.CoreModuleNamespaces.KFS, CUPurapConstants.IWantRoles.IWNT_ORG_AUTHORIZER, currentUser);
    }
    
    private boolean isCurrentUserProcurementContractAssistant(Person currentUser) {   
        return isCurrentUserInRole(KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE, CUPurapConstants.IWantRoles.IWNT_PROCUREMENT_CONTRACT_ASSISTANT,
                currentUser);
    }

    public static WorkflowDocumentService getWorkflowDocumentService() {
        if (workflowDocumentService == null) {
            workflowDocumentService = SpringContext.getBean(WorkflowDocumentService.class);
        }
        return workflowDocumentService;
    }

    public static RoleService getRoleService() {
        if (roleService == null) {
            roleService = KimApiServiceLocator.getRoleService();
        }
        return roleService;
    }
    
}
