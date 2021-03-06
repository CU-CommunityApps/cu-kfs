package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.ContractsGrantsInvoiceCreateDocumentService;

public interface CuContractsGrantsInvoiceCreateDocumentService extends ContractsGrantsInvoiceCreateDocumentService {
    public void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument);
}
