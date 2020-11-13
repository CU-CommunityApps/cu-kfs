package edu.cornell.kfs.tax.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxPropertyConstants;
import edu.cornell.kfs.tax.businessobject.TransactionOverride;

@SuppressWarnings("deprecation")
public class TransactionOverrideMaintenanceDocumentRule extends MaintenanceDocumentRuleBase {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean valid = super.processCustomRouteDocumentBusinessRules(document);
        TransactionOverride transactionOverride = (TransactionOverride) document.getNewMaintainableObject().getDataObject();
        
        if (StringUtils.equals(CUTaxConstants.TAX_TYPE_1099, transactionOverride.getTaxType())) {
            if (StringUtils.isNotEmpty(transactionOverride.getBoxNumber())
                    && transactionOverride.getBoxNumber().length() > CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH) {
                putFieldError(CUTaxPropertyConstants.BOX_NUMBER,
                        CUTaxKeyConstants.ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1099_BOX_LENGTH,
                        Integer.toString(CUTaxConstants.TAX_1099_MAX_BUCKET_LENGTH));
                valid = false;
            }
            if (StringUtils.isBlank(transactionOverride.getFormType())) {
                putFieldError(CUTaxPropertyConstants.FORM_TYPE,
                        CUTaxKeyConstants.ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1099_FORMTYPE_EMPTY);
                valid = false;
            }
        } else if (StringUtils.isNotBlank(transactionOverride.getFormType())) {
            putFieldError(CUTaxPropertyConstants.FORM_TYPE,
                    CUTaxKeyConstants.ERROR_DOCUMENT_TRANSACTIONOVERRIDEMAINTENANCE_1042S_FORMTYPE_NONEMPTY);
            valid = false;
        }
        
        return valid;
    }

}
