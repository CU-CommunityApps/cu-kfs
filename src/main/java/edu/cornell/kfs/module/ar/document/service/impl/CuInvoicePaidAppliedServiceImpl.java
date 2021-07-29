package edu.cornell.kfs.module.ar.document.service.impl;

import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.InvoicePaidAppliedServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Backport FINP-7572
 */
public class CuInvoicePaidAppliedServiceImpl extends InvoicePaidAppliedServiceImpl {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    public boolean doesInvoiceHaveAppliedAmounts(CustomerInvoiceDocument document) {
        if (LOG.isInfoEnabled()) {
            LOG.info("doesInvoiceHaveAppliedAmounts, checking document " + document.getDocumentNumber());
        }
        Collection<InvoicePaidApplied> results = getActiveInvoicePaidAppliedsForInvoice(document);

        for (InvoicePaidApplied invoicePaidApplied : results) {
            // don't include discount (the doc num and the ref num are the same document number)
            // or where applied amount is zero (could have been adjusted)
            if (!invoicePaidApplied.getDocumentNumber().equals(document.getDocumentNumber())
                    && invoicePaidApplied.getInvoiceItemAppliedAmount().isGreaterThan(KualiDecimal.ZERO)) {
                return true;
            }
        }
        return false;
    }
}
