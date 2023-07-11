package edu.cornell.kfs.module.ar.batch.service.impl;

import edu.cornell.kfs.module.ar.document.validation.CuCustomerRule;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.module.ar.batch.report.CustomerLoadBatchErrors;
import org.kuali.kfs.module.ar.batch.service.impl.CustomerLoadServiceImpl;
import org.kuali.kfs.module.ar.document.validation.impl.CustomerRule;

public class CuCustomerLoadServiceImpl extends CustomerLoadServiceImpl {
    
    @Override
    protected boolean validateSingle(final MaintenanceDocument maintDoc, 
            final CustomerLoadBatchErrors batchErrors,
            final String customerName) {
        //  get an instance of the business rule
        final CustomerRule rule = new CuCustomerRule();

        //  run the business rules
        final boolean result = rule.processRouteDocument(maintDoc);

        extractGlobalVariableErrors(batchErrors, customerName);

        return result;
    }

}
