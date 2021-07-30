package edu.cornell.kfs.module.ar.document.service.impl;

import java.util.Collection;

import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.impl.InvoicePaidAppliedServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

/*
 * Back-port FINP-7572
 */
public class CuInvoicePaidAppliedServiceImpl extends InvoicePaidAppliedServiceImpl {

    @Override
    public boolean doesInvoiceHaveAppliedAmounts(CustomerInvoiceDocument document) {
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
