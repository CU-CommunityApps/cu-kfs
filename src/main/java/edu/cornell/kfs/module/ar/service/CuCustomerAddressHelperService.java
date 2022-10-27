package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;

/*
 * CU Customization: KFSPTS-26393
 */
public interface CuCustomerAddressHelperService {
    boolean awardCustomerMatchesAddressCustomer(ContractsAndGrantsBillingAward award,
                ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType);
}
