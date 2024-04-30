package edu.cornell.kfs.module.purap.document.authorization;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.PurapConstants;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.sys.CUKFSConstants;

public class IWantDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean canSave(Document document) {
        return super.canSave(document);
    }

    public boolean canCopy(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(
                workflowDocument, GlobalVariables.getUserSession().getPrincipalId())) {
            return false;
        }
        
        return super.canCopy(document);
    }

    @Override
    public boolean canCancel(Document document) {
        return super.canCancel(document);
    }

    @Override
    public boolean canRoute(Document document) {
        String step = ((IWantDocument) document).getStep();
        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            return true;
        } else {
            return super.canRoute(document);
        }
    }

    @Override
    public boolean canClose(Document document) {
        return super.canClose(document);
    }

    @Override
    public boolean canReload(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if (SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(
                workflowDocument, GlobalVariables.getUserSession().getPrincipalId())) {
            return false;
        }
        
        return super.canReload(document);
    }

    /*
     * CU Customization (KFSPTS-2270): Added the ability to edit the document overview/description
     * for enroute IWNT docs; we'll use KIM permissions to restrict this to select users.
     * 
     * We restrict the editing of the doc overview to IWNT docs in enroute status at the
     * OrganizationHierarchy node, and further restrict it to non-ad-hoc users.
     */
    @Override
    public boolean canEditDocumentOverview(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        return workflowDocument.isEnroute() && CollectionUtils.isNotEmpty(nodeNames) && nodeNames.contains("OrganizationHierarchy")
                && !SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(
                        workflowDocument, GlobalVariables.getUserSession().getPrincipalId());
    }

    /* 
     * CU Customization:
     * Everyone should be able to view the Contract tab when the document is Final.
     */
    public boolean canViewContractTab(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return isInOrgHierarchyOrPurchasingAssistantNode(document) || workflowDocument.isFinal();
    }

    /*
     * CU Customization:
     * We restrict the editing of the Contract Tab contents on IWNT docs to enroute status at the
     * OrganizationHierarchy node and PurchasingContractAssistant node.
     */
    public boolean canEditContractIndicator(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        boolean isInApproversNode = isInApproversNode(document);
        
        return workflowDocument.isEnroute()
                && isInOrgHierarchyOrPurchasingAssistantNode(document)
                && isInApproversNode;
    }
    
    private boolean isInApproversNode(Document document) {
        final Person currentUser = GlobalVariables.getUserSession().getPerson();
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        WorkflowDocumentService workflowDocumentService = SpringContext.getBean(WorkflowDocumentService.class);
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            for (String nodeName : nodeNames) {
                List<ActionRequest> actionRequests = workflowDocumentService.getActionRequestsForPrincipalAtNode(
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

    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        IWantDocument iWantDocument = (IWantDocument) document;
        
        if (SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(
                workflowDocument, GlobalVariables.getUserSession().getPrincipalId())) {
            editModes.add("completeOrder");
        }

        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            editModes.add("wizard");
        }

        if (CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP.equalsIgnoreCase(iWantDocument.getStep())) {
            editModes.add(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

            //remove all others
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        }
        if (CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP.equalsIgnoreCase(iWantDocument.getStep())) {
            editModes.add(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);

            //remove all others
            editModes.remove(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        }
        if (CUPurapConstants.IWantDocumentSteps.VENDOR_STEP.equalsIgnoreCase(iWantDocument.getStep())) {
            editModes.add(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);

            //remove all others
            editModes.remove(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        }

        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(iWantDocument.getStep())) {
            editModes.add(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);

            //remove all others
            editModes.remove(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
            editModes.remove(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
        }

        // KFSPTS-2527 only display create req and create DV buttons if neither REQ not DV has been created from I Want doc
        if ((StringUtils.isBlank(iWantDocument.getReqsDocId()) && StringUtils.isBlank(iWantDocument.getDvDocId())) && !workflowDocument.isInitiated() && !workflowDocument.isSaved()) {   
            editModes.add(CUPurapConstants.IWNT_DOC_CREATE_REQ);
            editModes.add(CUPurapConstants.IWNT_DOC_CREATE_DV);

        }
        
        editModes.add(CUPurapConstants.IWNT_DOC_USE_LOOKUPS);
        
        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            editModes.add(CUPurapConstants.I_WANT_DOC_MULTIPLE_PAGE_IS_ALLOWED);
            editModes.add(CUPurapConstants.I_WANT_DOC_FULL_PAGE_IS_ALLOWED);
        }
        
        editModes.add(CUPurapConstants.I_WANT_DOC_EDIT_PROC_NET_ID);
        editModes.add(CUPurapConstants.IWNT_DOC_DISPLAY_NOTE_OPTIONS);
        
        if(isContractFunctionalityEnabled()) {
            if (canEditContractIndicator(document)) {
                editModes.add(CUPurapConstants.IWNT_DOC_DISPLAY_CONTRACT_TAB);
                editModes.add(CUPurapConstants.IWNT_DOC_EDIT_CONTRACT_INDICATOR);
            } else if (canViewContractTab(document)) {
                editModes.add(CUPurapConstants.IWNT_DOC_DISPLAY_CONTRACT_TAB);
            }
        }

        return editModes;
    }

    private boolean isContractFunctionalityEnabled() {
        return StringUtils.equalsIgnoreCase(
                getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.PURCHASING, KfsParameterConstants.DOCUMENT_COMPONENT, CUPurapConstants.IWNT_DOC_ENABLE_IWANT_CONTRACT_TAB_IND_PARM),
                KFSConstants.ParameterValues.YES);
    }
    
    private boolean isInOrgHierarchyOrPurchasingAssistantNode(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        
        if (CollectionUtils.isNotEmpty(nodeNames)) {
            return nodeNames.contains(KFSConstants.RouteLevelNames.ORGANIZATION_HIERARCHY)
                    || nodeNames.contains(CUPurapConstants.IWantRouteNodes.PURCHASING_CONTRACT_ASSISTANT);
        }
        return false;
    }

}
