package edu.cornell.kfs.module.ar.document.service.impl;

import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.document.service.impl.CustomerServiceImpl;

public class CuCustomerServiceImpl extends CustomerServiceImpl {
    
    @Override
    public String getNextCustomerNumber(final Customer newCustomer) {
        return String.valueOf(sequenceAccessorService
                .getNextAvailableSequenceNumber(CUSTOMER_NUMBER_SEQUENCE, Customer.class));
    }

}
