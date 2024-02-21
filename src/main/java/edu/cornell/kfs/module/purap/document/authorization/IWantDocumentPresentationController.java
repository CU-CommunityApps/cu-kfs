package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;

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

        return editModes;
    }

}
