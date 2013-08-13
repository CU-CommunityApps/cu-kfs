package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import  edu.cornell.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean canSave(Document document) {
        return super.canSave(document);
    }

    public boolean canCopy(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated()) {
            return false;
        }
        
        if(SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(workflowDocument, GlobalVariables.getUserSession().getPrincipalId())){
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
        } else
            return super.canRoute(document);
    }

    @Override
    public boolean canClose(Document document) {
        return false;
    }
    
    @Override
	public boolean canReload(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        
        if(SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(workflowDocument, GlobalVariables.getUserSession().getPrincipalId())){
            return false;
        }
        
        return super.canReload(document);
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
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

    	return SpringContext.getBean(FinancialSystemWorkflowHelperService.class).isAdhocApprovalRequestedForPrincipal(workflowDocument, GlobalVariables.getUserSession().getPrincipalId());
    }
    
    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        IWantDocument iWantDocument = (IWantDocument) document;
        
        // TODO: Is there a better way to resolve the problem mentioned in the canCompleteOrderForAdHoc() method above?
        
        //if(workflowDocument.isAdHocRequested()){
            //editModes.add("completeOrder");
        //}
        if(canCompleteOrderForAdHoc(document)){
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

        editModes.add(CUPurapConstants.IWNT_DOC_USE_LOOKUPS);
        
        return editModes;
    }

}
