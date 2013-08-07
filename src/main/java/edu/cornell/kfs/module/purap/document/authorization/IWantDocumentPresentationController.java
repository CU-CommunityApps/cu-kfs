package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.rice.kew.dto.ActionRequestDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    @Override
    protected boolean canSave(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        return super.canSave(document);
    }

    protected boolean canCopy(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.stateIsInitiated()) {
            return false;
        }
        
        if(workflowDocument.isAdHocRequested()){
            return false;
        }
        
        return super.canCopy(document);
    }

    @Override
    protected boolean canCancel(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        return super.canCancel(document);
    }

    @Override
    protected boolean canRoute(Document document) {
        String step = ((IWantDocument) document).getStep();
        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            return true;
        } else
            return super.canRoute(document);
    }

    @Override
    protected boolean canClose(Document document) {

        return false;
    }
    
    @Override
    protected boolean canReload(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if(workflowDocument.isAdHocRequested()){
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
    protected boolean canEditDocumentOverview(Document document) {
    	KualiWorkflowDocument kualiWorkflowDocument = document.getDocumentHeader().getWorkflowDocument();
    	String nodeNamesStr = kualiWorkflowDocument.getCurrentRouteNodeNames();
    	List<String> nodeNames = (StringUtils.isNotBlank(nodeNamesStr)) ? Arrays.asList(nodeNamesStr.split(",\\s*")) : Collections.<String>emptyList();
    	return kualiWorkflowDocument.stateIsEnroute() && !kualiWorkflowDocument.isAdHocRequested() && nodeNames.contains("OrganizationHierarchy");
    }

    /*
     * CU Customization (KFSPTS-1996):
     * 
     * The KualiWorkflowDocument.isAdHocRequested() method is not always returning true for ad hoc
     * recipients when first opening the IWantDocument. (One possible cause is that some authorization checks
     * are somehow updating the document with an invalid workflow doc, possibly one created by the wrong user.)
     * This is preventing ad hoc approvers from viewing the "Order Completed" tab without clicking reload or
     * performing some other refresh action. To work around this without constructing a whole new
     * KualiWorkflowDocument containing the correct user, we have copied the contents of the isAdHocRequested()
     * method into the one below, and have tweaked it as necessary to obtain the correct principal ID.
     * 
     * TODO: Is there a better workaround or a fix for the workflow doc problem?
     */
    private boolean canCompleteOrderForAdHoc(Document document) {
    	boolean isAdHocRequested = false;
        Long routeHeaderId = null;
        KualiWorkflowInfo workflowInfo = null;
        try {
            routeHeaderId = Long.valueOf(document.getDocumentNumber());
            workflowInfo = KNSServiceLocator.getWorkflowInfoService();
            String principalId = GlobalVariables.getUserSession().getPrincipalId();
            ActionRequestDTO[] actionRequests = workflowInfo.getActionRequests(routeHeaderId);
            for (int actionRequestIndex = 0; actionRequestIndex < actionRequests.length; actionRequestIndex++) {
                if (actionRequests[actionRequestIndex].isActivated() && actionRequests[actionRequestIndex].isAdHocRequest()) {
                    if (actionRequests[actionRequestIndex].isUserRequest() && principalId.equals(actionRequests[actionRequestIndex].getPrincipalId())) {
                        isAdHocRequested = true;
                    }
                    else if (actionRequests[actionRequestIndex].isGroupRequest()) {
                    	if (KIMServiceLocator.getIdentityManagementService().isMemberOfGroup(principalId, actionRequests[actionRequestIndex].getGroupId())) {
                    		isAdHocRequested = true;
                    	}
                    }
                }
            }
        }
        catch (WorkflowException e) {
            throw new RuntimeException(new StringBuffer(getClass().getName()).append(" encountered an exception while attempting to get the actoins requests for routeHeaderId: ").append(routeHeaderId).toString(), e);
        }
    	return isAdHocRequested;
    }
    
    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        IWantDocument iWantDocument = (IWantDocument) document;
        
        // TODO: Is there a better way to resolve the problem mentioned in the canCompleteOrderForAdHoc() method above?
        
        //if(workflowDocument.isAdHocRequested()){
            //editModes.add("completeOrder");
        //}
        if(canCompleteOrderForAdHoc(document)){
        	editModes.add("completeOrder");
        }

        if (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved()) {
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

        if (StringUtils.isBlank(iWantDocument.getReqsDocId()) && !workflowDocument.stateIsInitiated() && !workflowDocument.stateIsSaved()) {
        	editModes.add(CUPurapConstants.IWNT_DOC_CREATE_REQ);
        }
        editModes.add(CUPurapConstants.IWNT_DOC_USE_LOOKUPS);
        
        return editModes;
    }

}
