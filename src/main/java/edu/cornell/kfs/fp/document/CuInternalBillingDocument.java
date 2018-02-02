package edu.cornell.kfs.fp.document;

import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.fp.document.InternalBillingDocument;

public class CuInternalBillingDocument extends InternalBillingDocument {

    public CuInternalBillingDocument() {
        super();
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        DocumentHeader documentHeader = getDocumentHeader();
        if(documentHeader.getWorkflowDocument().isDisapproved()){
            String newDescription = ("Disapproved: " + documentHeader.getDocumentDescription()).substring(0, KFSConstants.getMaxLengthOfDocumentDescription());
            documentHeader.setDocumentDescription(newDescription);
        }
    }
}
