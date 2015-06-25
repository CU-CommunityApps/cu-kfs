package edu.cornell.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.document.validation.impl.CapitalAccountingLinesAccessibleValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 * Custom subclass of CapitalAccountingLinesAccessibleValidation that includes the "newSourceLine"
 * and "newTargetLine" property translation fixes from the unused KFS class
 * ProcurementCardAccountAccessibilityValidation. Since the line-accessible validation for PCDO docs
 * in KFS 5.3.1 uses CapitalAccountingLinesAccessibleValidation, we had to create an extension of
 * that class instead of using ProcurementCardAccountAccessibilityValidation directly.
 */
public class CuProcurementCardAccountAccessibilityValidation extends CapitalAccountingLinesAccessibleValidation {

    /**
     * ====
     * CU Customization: Copied the method below from ProcurementCardAccountAccessibilityValidation.
     * ====
     * 
     * KFSCNTRB-1677
     * Overrides to deal with the special case with PCDO on source and target accounting lines, where the propertyName is
     * something like "newTargetLines[i]", instead of "newTargetLines".
     * @return the accounting line collection property
     */
    @Override
    protected String getAccountingLineCollectionProperty() {
        String propertyName = null;
        if (GlobalVariables.getMessageMap().getErrorPath().size() > 0) {
            propertyName = GlobalVariables.getMessageMap().getErrorPath().get(0).replaceFirst(".*?document\\.", "");
        } else {
            propertyName = accountingLineForValidation.isSourceAccountingLine()
                    ? KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value : KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        }
        if (propertyName.startsWith("newSourceLine")) {
            return KFSConstants.PermissionAttributeValue.SOURCE_ACCOUNTING_LINES.value;
        }
        if (propertyName.startsWith("newTargetLine")) {
            return KFSConstants.PermissionAttributeValue.TARGET_ACCOUNTING_LINES.value;
        }
        return propertyName;
    }

}
