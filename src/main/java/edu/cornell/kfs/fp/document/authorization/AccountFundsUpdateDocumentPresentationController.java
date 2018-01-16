package edu.cornell.kfs.fp.document.authorization;

import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;
import org.kuali.rice.kew.api.WorkflowDocument;

import java.util.Set;

public class AccountFundsUpdateDocumentPresentationController extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    public boolean canEditAccountFundsUpdateDetailSection(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return workflowDocument.isInitiated() || workflowDocument.isSaved();
    }

    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);

        if (canEditAccountFundsUpdateDetailSection(document)) {
            editModes.add(CUKFSAuthorizationConstants.AccountFundsUpdateEditMode.EDITABLE_REASON);
        }

        return editModes;
    }

}
