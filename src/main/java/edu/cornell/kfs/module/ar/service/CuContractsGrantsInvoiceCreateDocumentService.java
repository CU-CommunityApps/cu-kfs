package edu.cornell.kfs.module.ar.service;

import java.sql.Date;
import java.util.List;

import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAwardAccount;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsLetterOfCreditReviewDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.service.ContractsGrantsInvoiceCreateDocumentService;

public interface CuContractsGrantsInvoiceCreateDocumentService extends ContractsGrantsInvoiceCreateDocumentService {
    public void populateDocumentDescription(ContractsGrantsInvoiceDocument cgInvoiceDocument);
}
