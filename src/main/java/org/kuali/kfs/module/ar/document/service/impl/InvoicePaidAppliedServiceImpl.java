/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar.document.service.impl;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.ar.businessobject.AppliedPayment;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.service.InvoicePaidAppliedService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * CU Customization: Backported the FINP-9937 changes into this file.
 * This overlay can be removed when we upgrade to the 2023-09-06 version of financials.
 */
@Transactional
public class InvoicePaidAppliedServiceImpl implements InvoicePaidAppliedService<AppliedPayment> {
    private BusinessObjectService businessObjectService;
    private UniversityDateService universityDateService;
    private WorkflowDocumentService workflowDocumentService;

    @Override
    public void clearDocumentPaidAppliedsFromDatabase(final String documentNumber) {
        final Map<String, String> fields = new HashMap<>();
        fields.put("documentNumber", documentNumber);
        businessObjectService.deleteMatching(InvoicePaidApplied.class, fields);
    }

    @Override
    public Integer getNumberOfInvoicePaidAppliedsForInvoiceDetail(
            final String financialDocumentReferenceInvoiceNumber,
            final Integer invoiceItemNumber) {
        final Map<String, Object> criteria = new HashMap<>();
        criteria.put("financialDocumentReferenceInvoiceNumber", financialDocumentReferenceInvoiceNumber);
        criteria.put("invoiceItemNumber", invoiceItemNumber);

        return businessObjectService.countMatching(InvoicePaidApplied.class, criteria);
    }

    public Collection<InvoicePaidApplied> getInvoicePaidAppliedsFromSpecificDocument(
            final String documentNumber,
            final String referenceCustomerInvoiceDocumentNumber) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put("financialDocumentReferenceInvoiceNumber", referenceCustomerInvoiceDocumentNumber);
        criteria.put("documentNumber", documentNumber);
        return businessObjectService.findMatching(InvoicePaidApplied.class, criteria);
    }

    @Override
    public boolean doesInvoiceHaveAppliedAmounts(final CustomerInvoiceDocument document) {
        final Collection<InvoicePaidApplied> results = getActiveInvoicePaidAppliedsForInvoice(document);

        for (final InvoicePaidApplied invoicePaidApplied : results) {
            // don't include discount (the doc num and the ref num are the same document number)
            // or where applied amount is zero (could have been adjusted)
            if (!invoicePaidApplied.getDocumentNumber().equals(document.getDocumentNumber())
                    && invoicePaidApplied.getInvoiceItemAppliedAmount().isGreaterThan(KualiDecimal.ZERO)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<InvoicePaidApplied> getInvoicePaidAppliedsForInvoice(final String documentNumber) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put("financialDocumentReferenceInvoiceNumber", documentNumber);
        return businessObjectService.findMatching(InvoicePaidApplied.class, criteria);
    }

    @Override
    public Collection<InvoicePaidApplied> getInvoicePaidAppliedsForInvoice(final CustomerInvoiceDocument invoice) {
        return getInvoicePaidAppliedsForInvoice(invoice.getDocumentNumber());
    }

    @Override
    public List<InvoicePaidApplied> getActiveInvoicePaidAppliedsForInvoice(final CustomerInvoiceDocument invoice) {
        return filterInvoicePaidAppliedsToOnlyActive((List<InvoicePaidApplied>) getInvoicePaidAppliedsForInvoice(invoice));
    }

    @Override
    public List<InvoicePaidApplied> filterInvoicePaidAppliedsToOnlyActive(final List<InvoicePaidApplied> invoicePaidApplieds) {
        // ==== CU Customization: Updated the method reference below with the FINP-9937 changes. ====
        return invoicePaidApplieds.stream()
                .filter(invoicePaidApplied -> !invoicePaidApplied.isAdjusted())
                .filter(this::docIsNotCancelledAndNotDisapproved)
                .collect(Collectors.toList());
    }

    // ==== CU Customization: Renamed and updated this method with the FINP-9937 changes. ====
    private boolean docIsNotCancelledAndNotDisapproved(final InvoicePaidApplied invoicePaidApplied) {
        final DocumentRouteHeaderValue document =
                workflowDocumentService.getDocument(invoicePaidApplied.getDocumentNumber());
        return !document.isCanceled() && !document.isDisapproved();
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    public void setUniversityDateService(final UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setWorkflowDocumentService(final WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }
}
