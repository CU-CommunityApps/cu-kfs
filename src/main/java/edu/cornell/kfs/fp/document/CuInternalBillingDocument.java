package edu.cornell.kfs.fp.document;

import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kew.api.document.DocumentStatus;
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
        if (documentHeader.getWorkflowDocument().isDisapproved() && statusChangeEvent.getNewRouteStatus().equals(DocumentStatus.DISAPPROVED.getCode())) {
            String newDescription = "Disapproved: " + documentHeader.getDocumentDescription();
            if(newDescription.length() > KFSConstants.getMaxLengthOfDocumentDescription()){
                newDescription = newDescription.substring(0, KFSConstants.getMaxLengthOfDocumentDescription());
            }
            documentHeader.setDocumentDescription(newDescription);
        }
    }
}
