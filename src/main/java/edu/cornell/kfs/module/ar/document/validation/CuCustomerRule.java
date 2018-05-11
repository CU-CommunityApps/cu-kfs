package edu.cornell.kfs.module.ar.document.validation;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ar.document.validation.impl.CustomerRule;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.MessageMap;

public class CuCustomerRule extends CustomerRule {
    
    /**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {

        boolean isValid = true;
        isValid &= super.processCustomRouteDocumentBusinessRules(document);
        MessageMap errorMap = GlobalVariables.getMessageMap();
        //negate the return value from hasErrors() becase when there are no errors
        //the method returns false so we need to negate the resuls otherwise
        //out validations will fail.
        isValid &= !errorMap.hasErrors();
        if (isValid) {
            initializeAttributes(document);
            isValid &= checkCustomerHasAddress(newCustomer);

            if (isValid) {
                isValid &= validateAddresses(newCustomer);
            }

            if (isValid) {
                isValid &= checkAddresses(newCustomer);
            }

            if (isValid) {
                isValid &= checkTaxNumber(newCustomer);
            }

            if (isValid) {
                isValid &= checkStopWorkReason();
            }
        }

        return isValid;
    }


}
