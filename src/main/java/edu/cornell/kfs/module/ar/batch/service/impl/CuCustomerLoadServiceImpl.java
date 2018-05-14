package edu.cornell.kfs.module.ar.batch.service.impl;

import edu.cornell.kfs.module.ar.document.validation.CuCustomerRule;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.module.ar.batch.report.CustomerLoadBatchErrors;
import org.kuali.kfs.module.ar.batch.service.impl.CustomerLoadServiceImpl;

public class CuCustomerLoadServiceImpl extends CustomerLoadServiceImpl {
    
    @Override
    protected boolean validateSingle(MaintenanceDocument maintDoc, CustomerLoadBatchErrors batchErrors, String customerName) {
        boolean result = true;

        //  get an instance of the business rule
        CuCustomerRule rule = new CuCustomerRule();

        //  run the business rules
        result &= rule.processRouteDocument(maintDoc);

        extractGlobalVariableErrors(batchErrors, customerName);

        return result;
    }

}
