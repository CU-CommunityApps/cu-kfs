package edu.cornell.kfs.module.ar.document.validation;

import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.module.ar.document.validation.impl.CustomerRule;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.MessageMap;

public class CuCustomerRule extends CustomerRule {
    
    @Override
    public boolean checkNameIsValidLength(String customerName) {
        return true;
    }


}
