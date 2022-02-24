/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.module.ar.businessobject.lookup;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.kns.web.ui.ResultRow;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.ContractsGrantsPaymentHistoryReport;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationAdjustableDocument;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.util.KfsDateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * back-port FINP-7396
 */
public class ContractsGrantsPaymentHistoryReportLookupableHelperServiceImpl extends
        ContractsGrantsReportLookupableHelperServiceImplBase {
    
    protected DateTimeService dateTimeService;
    protected DocumentService documentService;
    protected FinancialSystemDocumentService financialSystemDocumentService;

    /**
     * Overridden to validate the invoice amount and payment date fields to make sure they are parsable
     */
    @Override
    public void validateSearchParameters(Map<String, String> fieldValues) {
        if (StringUtils.isNotBlank(fieldValues.get(ArPropertyConstants.PAYMENT_DATE))) {
            validateDateField(fieldValues.get(ArPropertyConstants.PAYMENT_DATE), ArPropertyConstants.PAYMENT_DATE,
                    getDateTimeService());
        }
        if (StringUtils.isNotBlank(fieldValues.get(KFSPropertyConstants.RANGE_LOWER_BOUND_KEY_PREFIX
                + ArPropertyConstants.PAYMENT_DATE))) {
            validateDateField(fieldValues.get(KFSPropertyConstants.RANGE_LOWER_BOUND_KEY_PREFIX
                    + ArPropertyConstants.PAYMENT_DATE), KFSPropertyConstants.RANGE_LOWER_BOUND_KEY_PREFIX
                    + ArPropertyConstants.PAYMENT_DATE, getDateTimeService());
        }

        validateSearchParametersForOperatorAndValue(fieldValues, ArPropertyConstants.INVOICE_AMOUNT);
        super.validateSearchParameters(fieldValues);
    }

    /*
     * back-port FINP-7396
     */
    @Override
    public Collection<ContractsGrantsPaymentHistoryReport> performLookup(LookupForm lookupForm,
            Collection<ResultRow> resultTable, boolean bounded) {
        Map<String, String> lookupFormFields = lookupForm.getFieldsForLookup();

        setBackLocation(lookupForm.getFieldsForLookup().get(KRADConstants.BACK_LOCATION));
        setDocFormKey(lookupForm.getFieldsForLookup().get(KRADConstants.DOC_FORM_KEY));

        Collection<ContractsGrantsPaymentHistoryReport> displayList = new ArrayList<>();

        Map<String, String> invoiceAppliedLookupFields = new HashMap<>();

        if (lookupFormFields.containsKey(ArPropertyConstants.INVOICE_TYPE)) {
            invoiceAppliedLookupFields.put(ArPropertyConstants.CUSTOMER_INVOICE_DOCUMENT + "."
                            + KFSPropertyConstants.DOCUMENT_HEADER + "."
                            + KFSPropertyConstants.WORKFLOW_DOCUMENT_TYPE_NAME,
                    lookupFormFields.get(ArPropertyConstants.INVOICE_TYPE));
        }
        if (lookupFormFields.containsKey(ArPropertyConstants.PAYMENT_NUMBER)) {
            invoiceAppliedLookupFields.put(KFSPropertyConstants.DOCUMENT_NUMBER, lookupFormFields.get(
                    ArPropertyConstants.PAYMENT_NUMBER));
        }
        if (lookupFormFields.containsKey(ArPropertyConstants.CustomerFields.CUSTOMER_NUMBER)) {
            invoiceAppliedLookupFields.put(ArPropertyConstants.CUSTOMER_INVOICE_DOCUMENT + "." +
                    ArPropertyConstants.CustomerInvoiceDocumentFields.CUSTOMER_NUMBER, lookupFormFields.get(
                            ArPropertyConstants.CustomerFields.CUSTOMER_NUMBER));
        }
        if (lookupFormFields.containsKey(ArPropertyConstants.PAYMENT_AMOUNT)) {
            invoiceAppliedLookupFields.put(ArPropertyConstants.CustomerInvoiceDetailFields.INVOICE_ITEM_APPLIED_AMOUNT,
                    lookupFormFields.get(ArPropertyConstants.PAYMENT_AMOUNT));
        }
        if (lookupFormFields.containsKey(ArPropertyConstants.INVOICE_NUMBER)) {
            invoiceAppliedLookupFields.put(
                    ArPropertyConstants.CustomerInvoiceDocumentFields.FINANCIAL_DOCUMENT_REF_INVOICE_NUMBER,
                    lookupFormFields.get(ArPropertyConstants.INVOICE_NUMBER));
        }

        Collection<InvoicePaidApplied> invoicePaidApplieds = getLookupService().findCollectionBySearchHelper(
                InvoicePaidApplied.class, invoiceAppliedLookupFields, true);

        // build search result fields
        // For each Cash Control doc, get a list of payment app doc numbers
        try {
            for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
                boolean useInvoicePaidApplied = true;
                final Document doc = getDocumentService().getByDocumentHeaderId(invoicePaidApplied.getDocumentNumber());
                if (doc instanceof PaymentApplicationAdjustableDocument) {
                    final PaymentApplicationAdjustableDocument paymentApp = (PaymentApplicationAdjustableDocument) doc;
                    if (getFinancialSystemDocumentService().getUnsuccessfulDocumentStatuses()
                            .contains(paymentApp.getFinancialSystemDocumentHeader().getWorkflowDocumentStatusCode())) {
                        useInvoicePaidApplied = false;
                    }
                    if (StringUtils.isNotBlank(lookupFormFields.get(ArPropertyConstants.APPLIED_INDICATOR))) {
                        final String appliedIndicator = lookupFormFields.get(ArPropertyConstants.APPLIED_INDICATOR);
                        if (KRADConstants.YES_INDICATOR_VALUE.equals(appliedIndicator)
                                && !getFinancialSystemDocumentService().getSuccessfulDocumentStatuses()
                                .contains(paymentApp.getFinancialSystemDocumentHeader()
                                        .getWorkflowDocumentStatusCode())) {
                            useInvoicePaidApplied = false;
                        } else if (KRADConstants.NO_INDICATOR_VALUE.equals(appliedIndicator)
                                && !getFinancialSystemDocumentService().getPendingDocumentStatuses()
                                .contains(paymentApp.getFinancialSystemDocumentHeader()
                                        .getWorkflowDocumentStatusCode())) {
                            useInvoicePaidApplied = false;
                        }
                    }
                    final DateTime dateFinalized = paymentApp.getDocumentHeader().getWorkflowDocument()
                            .getDateFinalized();
                    Date paymentAppFinalDate = null;
                    if (dateFinalized != null) {
                        paymentAppFinalDate = dateFinalized.toDate();
                    }
                    if (StringUtils.isNotBlank(lookupFormFields.get(ArPropertyConstants.PAYMENT_DATE))) {
                        final Date toPaymentDate = getDateTimeService().convertToDate(
                                lookupFormFields.get(ArPropertyConstants.PAYMENT_DATE));
                        if (paymentAppFinalDate == null ||
                                !KfsDateUtils.isSameDay(paymentAppFinalDate, toPaymentDate)
                                        && toPaymentDate.before(paymentAppFinalDate)) {
                            useInvoicePaidApplied = false;
                        }
                    }
                    if (StringUtils.isNotBlank(lookupFormFields.get(KFSPropertyConstants.RANGE_LOWER_BOUND_KEY_PREFIX
                            + ArPropertyConstants.PAYMENT_DATE))) {
                        final Date fromPaymentDate = getDateTimeService().convertToDate(
                                lookupFormFields.get(KFSPropertyConstants.RANGE_LOWER_BOUND_KEY_PREFIX
                                        + ArPropertyConstants.PAYMENT_DATE));
                        if (paymentAppFinalDate == null ||
                                !KfsDateUtils.isSameDay(paymentAppFinalDate, fromPaymentDate)
                                        && fromPaymentDate.after(paymentAppFinalDate)) {
                            useInvoicePaidApplied = false;
                        }
                    }

                    final ContractsGrantsInvoiceDocument cgInvoiceDocument = getBusinessObjectService()
                            .findBySinglePrimaryKey(ContractsGrantsInvoiceDocument.class,
                                    invoicePaidApplied.getFinancialDocumentReferenceInvoiceNumber());

                    OperatorAndValue invoiceAmountOperator = buildOperatorAndValueFromField(lookupFormFields,
                            ArPropertyConstants.INVOICE_AMOUNT);
                    if (invoiceAmountOperator != null
                            && !invoiceAmountOperator.applyComparison(cgInvoiceDocument.getTotalDollarAmount())) {
                        useInvoicePaidApplied = false;
                    }

                    if (StringUtils.isNotBlank(lookupFormFields.get(KFSPropertyConstants.AWARD_NUMBER))) {
                        if (!StringUtils.equals(
                                cgInvoiceDocument.getInvoiceGeneralDetail().getAward().getProposalNumber(),
                                lookupFormFields.get(KFSPropertyConstants.AWARD_NUMBER))) {
                            useInvoicePaidApplied = false;
                        }
                    }
                    if (StringUtils.isNotBlank(lookupFormFields.get(ArPropertyConstants.REVERSED_INDICATOR))) {
                        final String reversedIndicator = lookupFormFields.get(ArPropertyConstants.REVERSED_INDICATOR);
                        if (KRADConstants.YES_INDICATOR_VALUE.equals(reversedIndicator)
                                && !cgInvoiceDocument.isInvoiceReversal()) {
                            useInvoicePaidApplied = false;
                        } else if (KRADConstants.NO_INDICATOR_VALUE.equals(reversedIndicator)
                                & cgInvoiceDocument.isInvoiceReversal()) {
                            useInvoicePaidApplied = false;
                        }
                    }

                    if (useInvoicePaidApplied) {
                        ContractsGrantsPaymentHistoryReport cgPaymentHistoryReport =
                                new ContractsGrantsPaymentHistoryReport();

                        cgPaymentHistoryReport.setPaymentNumber(invoicePaidApplied.getDocumentNumber());
                        cgPaymentHistoryReport.setPaymentDocumentType(paymentApp.getDocumentHeader()
                                .getWorkflowDocument().getDocumentTypeName());
                        FinancialSystemDocumentHeader documentHeader =
                                (FinancialSystemDocumentHeader) cgInvoiceDocument.getDocumentHeader();
                        cgPaymentHistoryReport.setInvoiceType(documentHeader.getWorkflowDocumentTypeName());

                        cgPaymentHistoryReport.setPaymentAmount(invoicePaidApplied.getInvoiceItemAppliedAmount());
                        cgPaymentHistoryReport.setInvoiceNumber(invoicePaidApplied
                                .getFinancialDocumentReferenceInvoiceNumber());

                        if (dateFinalized != null) {
                            cgPaymentHistoryReport.setPaymentDate(new java.sql.Date(
                                    dateFinalized.getMillis()));
                        }
                        cgPaymentHistoryReport.setAppliedIndicator(getFinancialSystemDocumentService()
                                .getSuccessfulDocumentStatuses().contains(paymentApp.getFinancialSystemDocumentHeader()
                                        .getWorkflowDocumentStatusCode()));

                        if (ObjectUtils.isNotNull(cgInvoiceDocument.getInvoiceGeneralDetail())) {
                            cgPaymentHistoryReport.setAwardNumber(cgInvoiceDocument.getInvoiceGeneralDetail()
                                    .getProposalNumber());
                        }

                        cgPaymentHistoryReport.setReversedIndicator(cgInvoiceDocument.isInvoiceReversal());
                        cgPaymentHistoryReport.setCustomerNumber(cgInvoiceDocument.getCustomerNumber());
                        cgPaymentHistoryReport.setCustomerName(cgInvoiceDocument.getCustomer().getCustomerName());
                        cgPaymentHistoryReport.setInvoiceAmount(cgInvoiceDocument.getTotalDollarAmount());

                        displayList.add(cgPaymentHistoryReport);
                    }
                }
            }
            buildResultTable(lookupForm, displayList, resultTable);
        } catch (ParseException pe) {
            throw new RuntimeException("I tried to validate the date and amount fields related to search, I really " +
                    "did.  But...I guess I didn't try hard enough", pe);
        }
        return displayList;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public FinancialSystemDocumentService getFinancialSystemDocumentService() {
        return financialSystemDocumentService;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }
}
