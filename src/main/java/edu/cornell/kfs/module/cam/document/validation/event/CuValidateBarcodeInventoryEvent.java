package edu.cornell.kfs.module.cam.document.validation.event;

import edu.cornell.kfs.module.cam.document.validation.impl.CuBarcodeInventoryErrorDocumentRule;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.rules.rule.BusinessRule;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEventBase;
import org.kuali.kfs.module.cam.document.BarcodeInventoryErrorDocument;

public class CuValidateBarcodeInventoryEvent extends KualiDocumentEventBase {

    boolean updateStatus;

    public CuValidateBarcodeInventoryEvent(String errorPathPrefix, Document document, boolean updateStatus) {
        super("", errorPathPrefix, document);
        this.updateStatus = updateStatus;
    }

    @SuppressWarnings("unchecked")
    public Class getRuleInterfaceClass() {
        return CuBarcodeInventoryErrorDocumentRule.class;
    }

    public boolean invokeRuleMethod(BusinessRule rule) {
        BarcodeInventoryErrorDocument document = (BarcodeInventoryErrorDocument) getDocument();
        CuBarcodeInventoryErrorDocumentRule cuBarcodeInventoryErrorDocumentRule = (CuBarcodeInventoryErrorDocumentRule) rule;
        return cuBarcodeInventoryErrorDocumentRule.validateBarcodeInventoryErrorDetail(document, updateStatus);
    }

    public boolean getUpdateStatus() {
        return updateStatus;
    }
}


