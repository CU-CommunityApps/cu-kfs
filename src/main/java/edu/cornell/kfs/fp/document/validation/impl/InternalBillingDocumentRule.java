package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.validation.impl.AccountingRuleEngineRuleBase;
import org.kuali.rice.kew.api.document.DocumentStatus;

public class InternalBillingDocumentRule extends AccountingRuleEngineRuleBase {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        boolean result = super.processCustomRouteDocumentBusinessRules(document);

        DocumentHeader documentHeader = document.getDocumentHeader();
        if (result && documentHeader.getWorkflowDocument().getStatus().equals(DocumentStatus.DISAPPROVED)) {
            String newDescription = ("Disapproved: " + documentHeader.getDocumentDescription()).substring(0, KFSConstants.getMaxLengthOfDocumentDescription());
            documentHeader.setDocumentDescription(newDescription);
        }

        return result;
    }
}
