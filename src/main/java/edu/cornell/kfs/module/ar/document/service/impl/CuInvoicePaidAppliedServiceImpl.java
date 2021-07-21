package edu.cornell.kfs.module.ar.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.impl.InvoicePaidAppliedServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuInvoicePaidAppliedServiceImpl extends InvoicePaidAppliedServiceImpl {
    
    protected CustomerInvoiceDocumentService customerInvoiceDocumentService;
    private static final Logger LOG = LogManager.getLogger();

    
    @Override
    public boolean doesInvoiceHaveAppliedAmounts(CustomerInvoiceDocument document) {
        if (document instanceof ContractsGrantsInvoiceDocument) {
            ContractsGrantsInvoiceDocument cgInvoiceDocument = (ContractsGrantsInvoiceDocument) document;
            KualiDecimal paymentAmount = customerInvoiceDocumentService.calculateAppliedPaymentAmount(cgInvoiceDocument);
            boolean hasPaymentBeenApplied = paymentAmount.isGreaterThan(KualiDecimal.ZERO);
            if (LOG.isDebugEnabled()) {
                LOG.debug("doesInvoiceHaveAppliedAmounts, got CG invoice " + cgInvoiceDocument.getDocumentNumber() + 
                        " the applied invoice amount is " + paymentAmount + " and returning hasPaymentBeenApplied: " + 
                        hasPaymentBeenApplied);
            }
            return hasPaymentBeenApplied;
        } else {
            LOG.debug("doesInvoiceHaveAppliedAmounts, calculating usering super version of function");
            return super.doesInvoiceHaveAppliedAmounts(document);
        }
    }


    public void setCustomerInvoiceDocumentService(CustomerInvoiceDocumentService customerInvoiceDocumentService) {
        this.customerInvoiceDocumentService = customerInvoiceDocumentService;
    }

}
