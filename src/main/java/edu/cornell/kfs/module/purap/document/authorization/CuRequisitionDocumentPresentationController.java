package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Iterator;
import java.util.Set;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.RequisitionEditMode;
import org.kuali.kfs.module.purap.RequisitionStatuses;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.authorization.RequisitionDocumentPresentationController;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CURequisitionEditMode;
import edu.cornell.kfs.module.purap.service.CuPurapAccountingService;

public class CuRequisitionDocumentPresentationController extends RequisitionDocumentPresentationController {

    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);
        RequisitionDocument reqDocument = (RequisitionDocument) document;
        
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isEnroute()) {
            Set<String> nodeNames = workflowDocument.getNodeNames();
            for (Iterator<String> iterator = nodeNames.iterator(); iterator.hasNext();) {
                String nodeNamesNode = iterator.next();
                if (RequisitionStatuses.NODE_ACCOUNT.equals(nodeNamesNode)) {
                 // KFSPTS-1792 : Should check whetehr object code is capital asset code ?
                    editModes.add(CURequisitionEditMode.ENABLE_CAPITAL_ASSET);
                }             
            }
            if (SpringContext.getBean(CuPurapAccountingService.class).isFiscalOfficersForAllAcctLines(reqDocument)) {
                editModes.remove(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION);
                editModes.remove(RequisitionEditMode.DISABLE_REMOVE_ACCTS);
            }
        }
        if (document instanceof RequisitionDocument && !editModes.contains(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION)
                && !hasEmptyAcctline((RequisitionDocument) document)) {
            editModes.add(RequisitionEditMode.DISABLE_SETUP_ACCT_DISTRIBUTION);
        }
        
        return editModes;

    }
}
