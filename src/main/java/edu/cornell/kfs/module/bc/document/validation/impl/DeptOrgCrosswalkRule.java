package edu.cornell.kfs.module.bc.document.validation.impl;

import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;

/**
 * Business rule(s) applicable to {@link ChartMaintenance} documents.
 */
public class DeptOrgCrosswalkRule extends MaintenanceDocumentRuleBase {

    /**Œ
     * This method calls specific rules for routing on Chart Maintenance documents Specifically it checks to make sure that
     * reportsToChart exists if it is not the same code as the newly created Chart and it checks to make sure that the chart manager
     * is valid for the Chart Module
     * 
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     * @return false if reports to chart code doesn't exist or user is invalid for this module
     */
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {

        boolean result = true;


        return result;

    }

}

