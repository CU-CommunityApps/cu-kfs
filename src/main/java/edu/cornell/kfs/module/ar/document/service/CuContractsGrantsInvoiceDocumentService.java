package edu.cornell.kfs.module.ar.document.service;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;

public interface CuContractsGrantsInvoiceDocumentService {
    
    Account determineContractControlAccount(InvoiceAccountDetail invoiceAccountDetail);
}
