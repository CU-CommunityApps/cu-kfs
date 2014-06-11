package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.kuali.kfs.fp.document.authorization.AdvanceDepositDocumentPresentationController;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;


import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;

public class CuAdvanceDepositDocumentPresentationController extends
		AdvanceDepositDocumentPresentationController {
	   private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAdvanceDepositDocumentPresentationController.class);

	    /**
	     * @see org.kuali.rice.kns.document.authorization.DocumentPresentationControllerBase#canEdit(org.kuali.rice.kns.document.Document)
	     */
	    @Override
	    public boolean canEdit(Document document) {
	        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
	        FinancialSystemDocumentHeader documentheader = (FinancialSystemDocumentHeader) (document.getDocumentHeader());

	        if (workflowDocument.isCanceled() || documentheader.getFinancialDocumentInErrorNumber() != null) {
	            return false;
	        }

	        boolean canEdit = false;

	        if (workflowDocument.isInitiated() || workflowDocument.isSaved() || workflowDocument.isEnroute() || workflowDocument.isException()) {
	            canEdit = true;
	        }

	        return canEdit;
	    }

	    @Override
	    public Set<String> getEditModes(Document document) {
	        Set<String> editModes = super.getEditModes(document);
	        AccountingDocument accountingDocument = (AccountingDocument) document;
	        WorkflowDocument workflowDocument = accountingDocument.getDocumentHeader().getWorkflowDocument();

	        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
	            editModes.add(CUKFSAuthorizationConstants.AdvanceDepositEditMode.EDITABLE_ADVANCE_DEPOSITS);
	        }
	        return editModes;
	    }

}
