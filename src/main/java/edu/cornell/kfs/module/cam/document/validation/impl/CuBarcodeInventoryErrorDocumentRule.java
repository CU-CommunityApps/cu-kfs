package edu.cornell.kfs.module.cam.document.validation.impl;

import org.kuali.kfs.module.cam.document.BarcodeInventoryErrorDocument;
import org.kuali.kfs.module.cam.document.validation.impl.BarcodeInventoryErrorDocumentRule;

public class CuBarcodeInventoryErrorDocumentRule extends BarcodeInventoryErrorDocumentRule {

    @Override
    public boolean validateBarcodeInventoryErrorDetail(BarcodeInventoryErrorDocument document, boolean updateStatus) {
        return true;
    }

}
