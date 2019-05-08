package edu.cornell.kfs.module.ar.document.service;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.ar.businessobject.InvoiceAccountDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.ContractsGrantsInvoiceDocumentService;

public interface CuContractsGrantsInvoiceDocumentService extends ContractsGrantsInvoiceDocumentService{
    
    Account determineContractControlAccount(InvoiceAccountDetail invoiceAccountDetail);

    void setInvoiceDueDateBasedOnNetTermsAndCurrentDate(ContractsGrantsInvoiceDocument document);
}
