package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.kuali.kfs.sys.KFSConstants.RouteLevelNames;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentPresentationControllerBase;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;

public class AdvanceDepositDocumentPresentationController extends AccountingDocumentPresentationControllerBase {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AdvanceDepositDocumentPresentationController.class);

    /**
     * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canEdit(org.kuali.rice.kns.document.Document)
     */
    @Override
    protected boolean canEdit(Document document) {
        KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        FinancialSystemDocumentHeader documentheader = (FinancialSystemDocumentHeader) (document.getDocumentHeader());

        if (workflowDocument.stateIsCanceled() || documentheader.getFinancialDocumentInErrorNumber() != null) {
            return false;
        }

        boolean canEdit = false;

        if (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved() || workflowDocument.stateIsEnroute() || workflowDocument.stateIsException()) {
            canEdit = true;
        }

        return canEdit;
    }

    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        AccountingDocument accountingDocument = (AccountingDocument) document;
        KualiWorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();

        if (workflowDocument.stateIsInitiated() || workflowDocument.stateIsSaved()) {
            editModes.add(CUKFSAuthorizationConstants.AdvanceDepositEditMode.EDITABLE_ADVANCE_DEPOSITS);
        }
        return editModes;
    }

}
