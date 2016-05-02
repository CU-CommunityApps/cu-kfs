package edu.cornell.kfs.tax.document.validation.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

@SuppressWarnings("deprecation")
public class TransactionOverrideMaintenanceDocumentRule extends MaintenanceDocumentRuleBase {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        
        // For 1099 transaction overrides, make sure tax bucket value is not too long.
        TransactionOverride transactionOverride = (TransactionOverride) document.getNewMaintainableObject().getDataObject();
        if (CUTaxConstants.TAX_TYPE_1099.equals(transactionOverride.getTaxType()) && StringUtils.isNotEmpty(transactionOverride.getBoxNumber())
                && transactionOverride.getBoxNumber().length() > CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH) {
            putFieldError("boxNumber", CUTaxKeyConstants.ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1099_BOX_LENGTH,
                    new String[] { Integer.toString(CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH) });
            valid = false;
        }
        
        return valid;
    }

}
