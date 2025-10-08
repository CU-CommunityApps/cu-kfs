package edu.cornell.kfs.module.ar.service;

import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.ContractsGrantsInvoiceCreateDocumentService;
import org.kuali.kfs.module.cg.businessobject.Award;

public interface CuContractsGrantsInvoiceCreateDocumentService extends ContractsGrantsInvoiceCreateDocumentService {
    public void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument);
    
    // KFSPTS-33340
    ContractsGrantsInvoiceDocument createCINVForReport(final Award awd);
}
