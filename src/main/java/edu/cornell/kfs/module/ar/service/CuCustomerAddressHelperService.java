package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;
import org.kuali.kfs.module.cg.businessobject.Award;

/*
 * CU Customization: KFSPTS-26393
 */
public interface CuCustomerAddressHelperService {
    boolean agencyCustomerMatchesAwardCustomer(Award award,
                ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType);
    
    boolean invoicingCustomerAddressExists(Award award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType);
}
