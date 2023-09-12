package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.fp.document.authorization.ProcurementCardDocumentPresentationController;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;

public class CuProcurementCardDocumentPresentationController extends ProcurementCardDocumentPresentationController {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean canEditDocumentOverview(final Document document) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        final Set<String> nodeNames = workflowDocument.getCurrentNodeNames();
        
        return workflowDocument.isEnroute() && CollectionUtils.isNotEmpty(nodeNames) 
                && nodeNames.contains(KFSConstants.RouteLevelNames.ACCOUNT_REVIEW_FULL_EDIT)
                && workflowDocument.isApprovalRequested()
                && !workflowDocument.isAcknowledgeRequested();
    }
}
