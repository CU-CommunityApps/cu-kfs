package edu.cornell.kfs.ksr.document.authorization;

import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentPresentationControllerBase;

public class SecurityRequestDocumentPresentationController
        extends FinancialSystemTransactionalDocumentPresentationControllerBase {

    private static final long serialVersionUID = -2586963561703542931L;

    @Override
    public boolean canEdit(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return workflowDocument.isInitiated() || workflowDocument.isSaved();
    }

    @Override
    public boolean canReload(Document document) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return workflowDocument.isSaved() || workflowDocument.isEnroute() || workflowDocument.isException();
    }

}
