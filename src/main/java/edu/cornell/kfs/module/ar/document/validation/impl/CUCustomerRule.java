package edu.cornell.kfs.module.ar.document.validation.impl;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.document.validation.impl.CustomerRule;
import org.kuali.kfs.sys.KFSConstants;

public class CUCustomerRule extends CustomerRule {

    @Override
    public boolean checkNameIsValidLength(String customerName) {
        boolean success = true;
        if (customerName.length() < 3) {
            success = false;
            GlobalVariables.getMessageMap().putError(
                    KFSConstants.MAINTENANCE_NEW_MAINTAINABLE + ArPropertyConstants.CustomerFields.CUSTOMER_NAME,
                    ArKeyConstants.CustomerConstants.ERROR_CUSTOMER_NAME_LESS_THAN_THREE_CHARACTERS);
        }

        return success;
    }
}
