package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Set;

import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    @Override
    protected boolean canSave(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
//        if (workflowDocument.stateIsInitiated()) {
//            return false;
//        }
        return super.canSave(document);
    }

    protected boolean canCopy(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.stateIsInitiated()) {
            return false;
        }
        return super.canCopy(document);
    }

    @Override
    protected boolean canCancel(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
//        if (workflowDocument.stateIsInitiated()) {
//            return false;
//        }
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
        //        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        //        if (workflowDocument.stateIsInitiated()) {
        //            return false;
        //        }
        return super.canClose(document);
    }

    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        IWantDocument iWantDocument = (IWantDocument) document;

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

        return editModes;
    }

}
