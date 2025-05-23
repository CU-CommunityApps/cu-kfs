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
package org.kuali.kfs.module.ar.document.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.kns.web.ui.HeaderField;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.businessobject.NonInvoiced;
import org.kuali.kfs.module.ar.businessobject.NonInvoicedDistribution;
import org.kuali.kfs.module.ar.document.CashControlDocument;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationDocument;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentFormBase;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class PaymentApplicationForm extends FinancialSystemTransactionalDocumentFormBase {

    private static final Logger LOG = LogManager.getLogger();

    private static final String ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY = "methodToCall.adjust";

    protected String selectedInvoiceDocumentNumber;
    protected String enteredInvoiceDocumentNumber;
    protected String selectedCustomerNumber;
    protected KualiDecimal unappliedCustomerAmount;
    protected PaymentApplicationInvoiceApply selectedInvoiceApplication;
    protected NonInvoiced nonInvoicedAddLine = new NonInvoiced();
    protected Integer nextNonInvoicedLineNumber;
    protected KualiDecimal nonAppliedHoldingAmount;
    protected String nonAppliedHoldingCustomerNumber;

    protected List<PaymentApplicationInvoiceApply> invoiceApplications = new ArrayList<>();
    protected List<CustomerInvoiceDocument> invoices = new ArrayList<>();

    // used for non-cash-control pay app docs
    protected List<PaymentApplicationDocument> nonAppliedControlDocs = new ArrayList<>();
    protected List<NonAppliedHolding> nonAppliedControlHoldings = new ArrayList<>();
    protected Map<String, KualiDecimal> nonAppliedControlAllocations = new HashMap<>();
    protected Map<String, KualiDecimal> distributionsFromControlDocs = new HashMap<>();

    private DateTimeService dateTimeService;

    @Override
    public List<ExtraButton> getExtraButtons() {
        // We are depending on a side-affect here.
        super.getExtraButtons();

        if (canAdjust()) {
            final Map<String, ExtraButton> buttonsMap = createButtonsMap();
            extraButtons.add(buttonsMap.get(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY));
        }

        return extraButtons;
    }

    /**
     * Creates a Map of all the buttons to appear on the Payment Application Form.
     *
     * @return A {@code Map} whose keys are the extraButtonProperty and whose values are @{code ExtraButton}s.
     */
    protected static Map<String, ExtraButton> createButtonsMap() {

        final ExtraButton adjustButton = new ExtraButton();
        adjustButton.setExtraButtonProperty(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY);
        // TODO: Where does .gif come from?
        final String extraButtonSource =
                "${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_adjust.gif";
        adjustButton.setExtraButtonSource(extraButtonSource);
        adjustButton.setExtraButtonAltText("Adjust");

        return Map.of(ADJUST_BUTTON_EXTRA_BUTTON_PROPERTY, adjustButton);
    }

    /**
     * A Payment Application document can be adjusted if:
     * - The user has permission to initiate an APPA document.
     * - AND the PaymentApplication is Approved (i.e. Final or Processed)
     * - AND the PaymentApplication document does not have any previous OR pending adjustments
     * - AND all of the following are Approved (i.e. Final or Processed) or Canceled:
     *    - the CashControl document associated with the PaymentApplication document, if any
     *    - other PaymentApplication documents associated with the CashControl document, if any
     * - AND the PaymentApplicationAdjustment document has a non-zero amount applied to invoices and/or unapplied
     *
     * @return {@code true} if the document can be adjusted; otherwise, {@code false}.
     */
    protected boolean canAdjust() {
        final boolean canAdjust =
                userCanInitiateAppAdjustment()
                && appIsApproved()
                && appHasNoPreviousOrPendingAdjustments()
                && cashControlAssociatedWithAppIsApprovedOrCanceled()
                && otherAppsAssociatedWithTheCashControlAssociatedWithTheAppAreApprovedOrCanceled()
                && appHasInvoiceAppliedsOrNonApplieds();
        LOG.debug("canAdjust() - Exit : canAdjust={}", canAdjust);
        return canAdjust;
    }

    protected boolean appHasNoPreviousOrPendingAdjustments() {
        final String adjusterDocumentNumber = getPaymentApplicationDocument().getAdjusterDocumentNumber();
        if (adjusterDocumentNumber == null) {
            return true;
        }
        final DocumentHeader documentHeader =
                getPaymentApplicationDocument().getDocumentHeaderService().getDocumentHeaderById(adjusterDocumentNumber);
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
        return workflowDocument.isDisapproved();
    }

    private boolean appHasInvoiceAppliedsOrNonApplieds() {
        final List<InvoicePaidApplied> invoicePaidApplieds = getPaymentApplicationDocument().getInvoicePaidApplieds();
        final List<NonAppliedHolding> nonAppliedHoldings = getPaymentApplicationDocument().getNonAppliedHoldings();
        return !(invoicePaidApplieds.isEmpty() && nonAppliedHoldings.isEmpty());
    }

    private boolean appIsApproved() {
        final PaymentApplicationDocument appDocument = getPaymentApplicationDocument();
        final WorkflowDocument appWorkflowDocument = extractWorkFlowDocument(appDocument);
        return appWorkflowDocument.isApproved();
    }

    private boolean cashControlAssociatedWithAppIsApprovedOrCanceled() {
        final PaymentApplicationDocument appDocument = getPaymentApplicationDocument();
        if (appDocument.hasCashControlDetail()) {
            final CashControlDocument cashControlDocument = appDocument.getCashControlDocument();
            final WorkflowDocument ccWorkflowDocument = extractWorkFlowDocument(cashControlDocument);
            return ccWorkflowDocument.isApproved() || ccWorkflowDocument.isCanceled();
        }

        return true;
    }

    private boolean otherAppsAssociatedWithTheCashControlAssociatedWithTheAppAreApprovedOrCanceled() {
        final Collection<WorkflowDocument> workflowDocuments = new LinkedList<>();

        final PaymentApplicationDocument appDocument = getPaymentApplicationDocument();

        if (appDocument.hasCashControlDetail()) {
            final CashControlDocument cashControlDocument = appDocument.getCashControlDocument();
            final List<CashControlDetail> cashControlDetails = cashControlDocument.getCashControlDetails();
            for (final CashControlDetail cashControlDetail : cashControlDetails) {
                final PaymentApplicationDocument otherAppDocument = cashControlDetail.getReferenceFinancialDocument();

                if (otherAppDocument.getDocumentNumber().equals(appDocument.getDocumentNumber())) {
                    // Do not add this Document again; it was added above
                    continue;
                }

                final WorkflowDocument otherAppWorkflowDocument = extractWorkFlowDocument(otherAppDocument);
                workflowDocuments.add(otherAppWorkflowDocument);
            }
        }

        return workflowDocuments
                .stream()
                .allMatch(wfDocument -> wfDocument.isApproved() || wfDocument.isCanceled());
    }

    private static WorkflowDocument extractWorkFlowDocument(final Document document) {
        final var documentHeader = document.getDocumentHeader();
        return documentHeader.getWorkflowDocument();
    }

    private boolean userCanInitiateAppAdjustment() {
        final Document document = getDocument();
        final DocumentAuthorizer documentAuthorizer =
                SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(document);
        final Person person = GlobalVariables.getUserSession().getPerson();
        final boolean userCanInitiate = documentAuthorizer.canInitiate(
                ArConstants.ArDocumentTypeCodes.PAYMENT_APPLICATION_ADJUSTMENT_DOCUMENT_TYPE_CODE,
                person
        );
        LOG.debug("userCanInitiateAppAdjustment() - Exit : userCanInitiate={}", userCanInitiate);
        return userCanInitiate;
    }

    @Override
    protected List<HeaderField> getStandardHeaderFields(final WorkflowDocument workflowDocument) {
        final List<HeaderField> standardHeaderFields = super.getStandardHeaderFields(workflowDocument);

        getAdjusterHeaderField().ifPresent(standardHeaderFields::add);

        return standardHeaderFields;
    }

    private Optional<HeaderField> getAdjusterHeaderField() {
        return getDocumentAdjusterDocumentNumber()
                .map(adjusterDocumentNumber -> {
                    final String ddAttributeEntryName =
                            "DataDictionary.PaymentApplicationDocument.attributes.adjusterDocumentNumber";
                    final String nonLookupValue =
                            buildHtmlLink(getDocumentHandlerUrl(adjusterDocumentNumber), adjusterDocumentNumber);
                    return new HeaderField(
                            KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_ADJUSTER,
                            ddAttributeEntryName,
                            adjusterDocumentNumber,
                            nonLookupValue
                    );
                });
    }

    // When "adjusting", this method will get called multiple times. Sometimes getDocument() will return the APP being
    // adjusted, in which case, it's adjusterDocumentNumber should be returned; other times, it will return the newly
    // created APPA, in which case, returning NULL is fine because the APP header isn't going to be displayed anyway.
    // Something's getting called unnecessarily...but this is Struts code, so...<shrug>.
    private Optional<String> getDocumentAdjusterDocumentNumber() {
        String adjusterDocumentNumber = null;
        if (getDocument() instanceof PaymentApplicationDocument) {
            adjusterDocumentNumber = getPaymentApplicationDocument().getAdjusterDocumentNumber();
        }
        return Optional.ofNullable(adjusterDocumentNumber);
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return ArConstants.ArDocumentTypeCodes.PAYMENT_APPLICATION_DOCUMENT_TYPE_CODE;
    }

    @Override
    public void reset(final ActionMapping mapping, final ServletRequest request) {
        super.reset(mapping, request);
        for (final PaymentApplicationInvoiceApply application : invoiceApplications) {
            application.setQuickApply(false);
        }
    }

    @Override
    public void populate(final HttpServletRequest request) {
        super.populate(request);

        // Set the next non-invoiced line number
        final PaymentApplicationDocument paymentApplicationDocument = getPaymentApplicationDocument();
        if (ObjectUtils.isNotNull(paymentApplicationDocument.getNonInvoicedDistributions())) {
            for (final NonInvoicedDistribution u : paymentApplicationDocument.getNonInvoicedDistributions()) {
                if (null == getNextNonInvoicedLineNumber()) {
                    setNextNonInvoicedLineNumber(u.getFinancialDocumentLineNumber());
                } else if (u.getFinancialDocumentLineNumber() > getNextNonInvoicedLineNumber()) {
                    setNextNonInvoicedLineNumber(u.getFinancialDocumentLineNumber());
                }
            }
        }

        if (null == getNextNonInvoicedLineNumber()) {
            setNextNonInvoicedLineNumber(1);
        }

        // This step doesn't affect anything persisted to the database. It allows proper calculation
        // of amounts for the display.
        String customerNumber = null;
        String docId = getDocument().getDocumentNumber();
        if (ObjectUtils.isNotNull(request.getParameter(KFSConstants.PARAMETER_DOC_ID))
                && ObjectUtils.isNull(getDocument().getDocumentNumber())) {
            // The document hasn't yet been set on the form. Let's look it up manually so that we can get the
            // customer number.
            docId = request.getParameter(KFSConstants.PARAMETER_DOC_ID).trim();
            final DocumentService documentService = SpringContext.getBean(DocumentService.class);
            final Document d = documentService.getByDocumentHeaderId(docId);
            final PaymentApplicationDocument pDocument = (PaymentApplicationDocument) d;
            final AccountsReceivableDocumentHeader arHeader = pDocument.getAccountsReceivableDocumentHeader();
            if (ObjectUtils.isNotNull(arHeader)) {
                customerNumber = arHeader.getCustomerNumber();
            }
        }

        if (ObjectUtils.isNull(getSelectedInvoiceApplication())) {
            if (ObjectUtils.isNull(invoices) || invoices.isEmpty()) {
                if (ObjectUtils.isNotNull(customerNumber)) {
                    // get open invoices for the current customer
                    final CustomerInvoiceDocumentService customerInvoiceDocumentService =
                            SpringContext.getBean(CustomerInvoiceDocumentService.class);
                    final Collection<CustomerInvoiceDocument> openInvoicesForCustomer =
                            customerInvoiceDocumentService.getOpenInvoiceDocumentsByCustomerNumber(customerNumber);
                    setInvoices(new ArrayList<>(openInvoicesForCustomer));
                    if (invoices != null && !invoices.isEmpty()) {
                        setSelectedInvoiceDocumentNumber(invoices.get(0).getDocumentNumber());
                    }
                    setupInvoiceWrappers(docId);
                }
            }
        }
    }

    /**
     * @param payAppDocNumber
     */
    protected void setupInvoiceWrappers(final String payAppDocNumber) {
        if (StringUtils.isBlank(payAppDocNumber)) {
            throw new IllegalArgumentException("The payAppDocNumber parameter passed in was null or blank.");
        }

        // clear any existing
        invoiceApplications.clear();

        if (invoices == null || invoices.isEmpty()) {
            return;
        }

        for (final CustomerInvoiceDocument invoice : invoices) {
            final PaymentApplicationInvoiceApply invoiceApplication = new PaymentApplicationInvoiceApply(payAppDocNumber,
                    invoice);
            addInvoiceApplication(invoiceApplication);
        }
    }

    public Map<String, PaymentApplicationInvoiceApply> getInvoiceApplicationsByDocumentNumber() {
        final Map<String, PaymentApplicationInvoiceApply> m = new HashMap<>();
        for (final PaymentApplicationInvoiceApply i : invoiceApplications) {
            m.put(i.getDocumentNumber(), i);
        }
        return m;
    }

    public Integer getNextNonInvoicedLineNumber() {
        return nextNonInvoicedLineNumber;
    }

    public void setNextNonInvoicedLineNumber(final Integer nextNonInvoicedLineNumber) {
        this.nextNonInvoicedLineNumber = nextNonInvoicedLineNumber;
    }

    public KualiDecimal getNonArTotal() {
        return null == getPaymentApplicationDocument() ? KualiDecimal.ZERO :
                getPaymentApplicationDocument().getNonArTotal();
    }

    public PaymentApplicationDocument getPaymentApplicationDocument() {
        return (PaymentApplicationDocument) getDocument();
    }

    /**
     * For a given invoiceDocNumber and invoiceItemNumber, this method will return any paidApplieds that match those
     * two fields, if any exists. Otherwise it will return null.
     */
    public InvoicePaidApplied getPaidAppliedForInvoiceDetail(final String invoiceDocNumber, final Integer invoiceItemNumber) {
        if (StringUtils.isBlank(invoiceDocNumber)) {
            throw new IllegalArgumentException("The parameter [invoiceDocNumber] passed in was blank or null.");
        }
        if (invoiceItemNumber == null || invoiceItemNumber < 1) {
            throw new IllegalArgumentException("The parameter [invoiceItemNumber] passed in was blank, zero or negative.");
        }
        final PaymentApplicationDocument payAppDoc = getPaymentApplicationDocument();
        final List<InvoicePaidApplied> paidApplieds = payAppDoc.getInvoicePaidApplieds();
        for (final InvoicePaidApplied paidApplied : paidApplieds) {
            if (invoiceDocNumber.equalsIgnoreCase(paidApplied.getFinancialDocumentReferenceInvoiceNumber())) {
                if (invoiceItemNumber.equals(paidApplied.getInvoiceItemNumber())) {
                    return paidApplied;
                }
            }
        }
        return null;
    }

    public String getSelectedInvoiceDocumentNumber() {
        return selectedInvoiceDocumentNumber;
    }

    public void setSelectedInvoiceDocumentNumber(final String selectedInvoiceDocumentNumber) {
        this.selectedInvoiceDocumentNumber = selectedInvoiceDocumentNumber;
    }

    public KualiDecimal getUnappliedCustomerAmount() {
        return unappliedCustomerAmount;
    }

    public void setUnappliedCustomerAmount(final KualiDecimal unappliedCustomerAmount) {
        this.unappliedCustomerAmount = unappliedCustomerAmount;
    }

    public List<PaymentApplicationInvoiceDetailApply> getSelectedInvoiceDetailApplications() {
        final PaymentApplicationInvoiceApply invoiceApplication = getSelectedInvoiceApplication();
        List<PaymentApplicationInvoiceDetailApply> detailApplications = null;
        if (ObjectUtils.isNotNull(invoiceApplication)) {
            detailApplications = invoiceApplication.getDetailApplications();
            if (null == detailApplications) {
                detailApplications = new ArrayList<>();
            }
        }
        return detailApplications;
    }

    public List<PaymentApplicationInvoiceApply> getNonSelectedInvoiceApplications() {
        final String selectedInvoiceNumber = getSelectedInvoiceApplication().getDocumentNumber();

        final List<PaymentApplicationInvoiceApply> nonSelectedInvoiceApplications = new ArrayList<>();
        for (final PaymentApplicationInvoiceApply invoiceApplication : invoiceApplications) {
            if (!invoiceApplication.getDocumentNumber().equalsIgnoreCase(selectedInvoiceNumber)) {
                nonSelectedInvoiceApplications.add(invoiceApplication);
            }
        }
        return nonSelectedInvoiceApplications;
    }

    //https://jira.kuali.org/browse/KFSCNTRB-1377
    //Turn into a simple getter and setter to prevent refetching and presort collection on load
    public List<PaymentApplicationInvoiceApply> getInvoiceApplications() {
        return invoiceApplications;
    }

    public void setInvoiceApplications(final List<PaymentApplicationInvoiceApply> invoiceApplications) {
        this.invoiceApplications = invoiceApplications;
    }

    public PaymentApplicationInvoiceApply getSelectedInvoiceApplication() {
        // It seems like this should be selectedInvoiceDocumentNumber instead of enteredInvoiceDocumentNumber, however,
        // in the case where an invoice with multiple details has been loaded and then the user selects another invoice
        // with fewer details, the form is populated from the request and if it loads the new invoice document, there
        // are fewer details, and we get an ArrayIndexOutOfBoundsException which clears the request and cascades into
        // a missing CSRF token and a 403 Unauthorized response. This works around that issue, and then the
        // enteredInvoiceDocumentNumber is changed to the same value as selectedInvoiceDocumentNumber before the
        // invoice is loaded and the form is displayed and life is good.
        final String docNumber = enteredInvoiceDocumentNumber;
        if (ObjectUtils.isNotNull(docNumber)) {
            return getInvoiceApplicationsByDocumentNumber().get(docNumber);
        } else {
            final List<PaymentApplicationInvoiceApply> i = invoiceApplications;
            if (i.isEmpty()) {
                return null;
            } else {
                return invoiceApplications.get(0);
            }
        }
    }

    public List<CustomerInvoiceDocument> getInvoices() {
        return invoices;
    }

    public void setInvoices(final ArrayList<CustomerInvoiceDocument> invoices) {
        this.invoices = invoices;
    }

    public String getEnteredInvoiceDocumentNumber() {
        return enteredInvoiceDocumentNumber;
    }

    public void setEnteredInvoiceDocumentNumber(final String enteredInvoiceDocumentNumber) {
        this.enteredInvoiceDocumentNumber = enteredInvoiceDocumentNumber;
    }

    /**
     * This special casing for negative applieds is a display issue. We basically dont want to ever display that they
     * applied a negative amount, even while they may have an unsaved document with negative applications that are
     * failing validations.
     *
     * @return
     */
    public KualiDecimal getTotalApplied() {
        final KualiDecimal totalApplied = getPaymentApplicationDocument().getTotalApplied();
        if (totalApplied.isPositive()) {
            return totalApplied;
        } else {
            return KualiDecimal.ZERO;
        }
    }

    public KualiDecimal getUnallocatedBalance() {
        return getTotalFromControl().subtract(getTotalApplied());
    }

    /**
     * @return the control total available for this document, whether its a cash-control style payapp, or a nonapplied
     *         style payapp.
     */
    public KualiDecimal getTotalFromControl() {
        final PaymentApplicationDocument payAppDoc = (PaymentApplicationDocument) getDocument();
        if (payAppDoc.hasCashControlDetail()) {
            return payAppDoc.getTotalFromControl();
        } else {
            return getNonAppliedControlAvailableUnappliedAmount();
        }
    }

    /**
     * This method retrieves a specific customer invoice detail from the list, by array index
     *
     * @param index the index of the customer invoice detail to retrieve
     * @return a CustomerInvoiceDetail
     */
    public PaymentApplicationInvoiceDetailApply getInvoiceDetailApplication(final int index) {
        final List<PaymentApplicationInvoiceDetailApply> details = getSelectedInvoiceDetailApplications();
        return details.get(index);
    }

    /**
     * This method retrieves a specific customer invoice from the list, by array index
     *
     * @param index the index of the customer invoice to retrieve
     * @return a CustomerInvoiceDocument
     */
    public PaymentApplicationInvoiceApply getInvoiceApplication(final int index) {
        return invoiceApplications.get(index);
    }

    /**
     * @param documentNumber
     * @return
     */
    public PaymentApplicationInvoiceApply getInvoiceApplication(final String documentNumber) {
        if (StringUtils.isBlank(documentNumber)) {
            throw new RuntimeException("The parameter passed in [documentNumber] was null or blank.");
        }
        for (final PaymentApplicationInvoiceApply invoiceApplication : invoiceApplications) {
            if (documentNumber.equalsIgnoreCase(invoiceApplication.getDocumentNumber())) {
                return invoiceApplication;
            }
        }
        return null;
    }

    public void setInvoiceDetailApplication(final int key, final PaymentApplicationInvoiceDetailApply value) {
        getSelectedInvoiceDetailApplications().set(key, value);
    }

    public KualiDecimal getSelectedInvoiceBalance() {
        final PaymentApplicationInvoiceApply invoiceApplication = getSelectedInvoiceApplication();
        return invoiceApplication.getOpenAmount();
    }

    public KualiDecimal getSelectedInvoiceTotalAmount() {
        final PaymentApplicationInvoiceApply invoiceApplication = getSelectedInvoiceApplication();
        return invoiceApplication.getInvoice().getSourceTotal();
    }

    public KualiDecimal getAmountAppliedDirectlyToInvoice() {
        final PaymentApplicationInvoiceApply invoiceApplicationToFind = getSelectedInvoiceApplication();
        KualiDecimal amount = new KualiDecimal(0);
        for (final PaymentApplicationInvoiceApply invoiceApplication : invoiceApplications) {
            if (invoiceApplicationToFind.getDocumentNumber().equalsIgnoreCase(invoiceApplication.getDocumentNumber())) {
                amount = amount.add(invoiceApplication.getAmountToApply());
            }
        }
        return amount;
    }

    public String getPreviousInvoiceDocumentNumber() {
        CustomerInvoiceDocument previousInvoiceDocument = null;

        final PaymentApplicationInvoiceApply invoiceApplication = getSelectedInvoiceApplication();
        final CustomerInvoiceDocument selectedInvoiceDocument = invoiceApplication == null ? null :
                invoiceApplication.getInvoice();
        if (null != selectedInvoiceDocument && 2 <= invoices.size()) {
            final Iterator<CustomerInvoiceDocument> iterator = invoices.iterator();
            CustomerInvoiceDocument customerInvoiceDocument = iterator.next();
            final String selectedInvoiceDocumentNumber = selectedInvoiceDocument.getDocumentNumber();
            if (null != selectedInvoiceDocumentNumber
                    && selectedInvoiceDocumentNumber.equals(customerInvoiceDocument.getDocumentNumber())) {
                previousInvoiceDocument = null;
            } else {
                while (iterator.hasNext()) {
                    final CustomerInvoiceDocument currentInvoiceDocument = iterator.next();
                    final String currentInvoiceDocumentNumber = currentInvoiceDocument.getDocumentNumber();
                    if (null != currentInvoiceDocumentNumber
                            && currentInvoiceDocumentNumber.equals(selectedInvoiceDocument.getDocumentNumber())) {
                        previousInvoiceDocument = customerInvoiceDocument;
                    } else {
                        customerInvoiceDocument = currentInvoiceDocument;
                    }
                }
            }
        }

        return null == previousInvoiceDocument ? "" : previousInvoiceDocument.getDocumentNumber();
    }

    public String getNextInvoiceDocumentNumber() {
        CustomerInvoiceDocument nextInvoiceDocument = null;

        final PaymentApplicationInvoiceApply invoiceApplication = getSelectedInvoiceApplication();
        final CustomerInvoiceDocument selectedInvoiceDocument = invoiceApplication == null ? null :
                invoiceApplication.getInvoice();
        if (null != selectedInvoiceDocument && 2 <= invoices.size()) {
            final Iterator<CustomerInvoiceDocument> iterator = invoices.iterator();
            while (iterator.hasNext()) {
                final CustomerInvoiceDocument currentInvoiceDocument = iterator.next();
                final String currentInvoiceDocumentNumber = currentInvoiceDocument.getDocumentNumber();
                if (currentInvoiceDocumentNumber.equals(selectedInvoiceDocument.getDocumentNumber())) {
                    if (iterator.hasNext()) {
                        nextInvoiceDocument = iterator.next();
                    } else {
                        nextInvoiceDocument = null;
                    }
                }
            }
        }

        return null == nextInvoiceDocument ? "" : nextInvoiceDocument.getDocumentNumber();
    }

    /**
     * This method gets the Cash Control document for the payment application document
     *
     * @return the cash control document
     */
    public CashControlDocument getCashControlDocument() {
        return getPaymentApplicationDocument().getCashControlDocument();
    }

    public NonInvoiced getNonInvoicedAddLine() {
        return nonInvoicedAddLine;
    }

    public void setNonInvoicedAddLine(final NonInvoiced nonInvoicedAddLine) {
        this.nonInvoicedAddLine = nonInvoicedAddLine;
    }

    public Integer getNonInvoicedAddLineItemNumber() {
        Integer number = 0;
        if (null != getPaymentApplicationDocument()) {
            final Collection<NonInvoiced> items = getPaymentApplicationDocument().getNonInvoiceds();
            for (final NonInvoiced item : items) {
                final Integer i = item.getFinancialDocumentLineNumber();
                if (i > number) {
                    number = i;
                }
            }
        }
        return number + 1;
    }

    /**
     * @param invoiceApplicationToAdd
     */
    public void addInvoiceApplication(final PaymentApplicationInvoiceApply invoiceApplicationToAdd) {
        if (invoiceApplicationToAdd == null) {
            throw new RuntimeException("The parameter passed in [invoiceApplicationToAdd] was null.");
        }
        for (int i = 0; i < invoiceApplications.size(); i++) {
            final PaymentApplicationInvoiceApply invoiceApplication = invoiceApplications.get(i);
            if (invoiceApplicationToAdd.getDocumentNumber().equalsIgnoreCase(invoiceApplication.getDocumentNumber())) {
                invoiceApplications.set(i, invoiceApplicationToAdd);
            }
        }
        invoiceApplications.add(invoiceApplicationToAdd);
    }

    public String getSelectedCustomerNumber() {
        return selectedCustomerNumber;
    }

    public void setSelectedCustomerNumber(final String selectedCustomerNumber) {
        this.selectedCustomerNumber = StringUtils.isBlank(selectedCustomerNumber) ? null : selectedCustomerNumber.toUpperCase(Locale.US);
    }

    public KualiDecimal getNonAppliedHoldingAmount() {
        return nonAppliedHoldingAmount;
    }

    public void setNonAppliedHoldingAmount(final KualiDecimal nonAppliedHoldingAmount) {
        this.nonAppliedHoldingAmount = nonAppliedHoldingAmount;
    }

    public String getNonAppliedHoldingCustomerNumber() {
        return nonAppliedHoldingCustomerNumber;
    }

    public void setNonAppliedHoldingCustomerNumber(final String nonAppliedHoldingCustomerNumber) {
        this.nonAppliedHoldingCustomerNumber = nonAppliedHoldingCustomerNumber;
    }

    public List<PaymentApplicationDocument> getNonAppliedControlDocs() {
        return nonAppliedControlDocs;
    }

    public void setNonAppliedControlDocs(final List<PaymentApplicationDocument> nonAppliedControlDocs) {
        this.nonAppliedControlDocs = nonAppliedControlDocs;
    }

    /**
     * Returns the total amount of previously NonApplied funds available to apply to invoices and other applications on this
     * document.
     *
     * @return
     */
    public KualiDecimal getNonAppliedControlAvailableUnappliedAmount() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for (final NonAppliedHolding nonAppliedHolding : nonAppliedControlHoldings) {
            amount = amount.add(nonAppliedHolding.getAvailableUnappliedAmount());
        }
        return amount;
    }

    public List<NonAppliedHolding> getNonAppliedControlHoldings() {
        final EntryHolderComparator entryHolderComparator = new EntryHolderComparator();
        final List<EntryHolder> entryHoldings = new ArrayList<>();
        for (final NonAppliedHolding nonAppliedControlHolding : nonAppliedControlHoldings) {
            final Date dateCreated = getDateTimeService().getUtilDate(nonAppliedControlHolding
                    .getDocumentHeader()
                    .getWorkflowDocument()
                    .getDateCreated());
            entryHoldings.add(new EntryHolder(dateCreated, nonAppliedControlHolding));
        }
        if (entryHoldings.size() > 0) {
            entryHoldings.sort(entryHolderComparator);
        }
        final List<NonAppliedHolding> results = new ArrayList<>();
        for (final EntryHolder entryHolder : entryHoldings) {
            results.add((NonAppliedHolding) entryHolder.getHolder());
        }
        return results;
    }

    public void setNonAppliedControlHoldings(final List<NonAppliedHolding> nonAppliedControlHoldings) {
        this.nonAppliedControlHoldings = nonAppliedControlHoldings;
    }

    /**
     * Used for when the doc is final, to show the control docs section.
     *
     * @return
     */
    public Map<String, KualiDecimal> getDistributionsFromControlDocs() {
        if (distributionsFromControlDocs == null || distributionsFromControlDocs.isEmpty()) {
            distributionsFromControlDocs = getPaymentApplicationDocument().getDistributionsFromControlDocuments();
        }
        return distributionsFromControlDocs;
    }

    /**
     * Used for when the doc is live, to show the control docs section.
     *
     * @return
     */
    public Map<String, KualiDecimal> getNonAppliedControlAllocations() {
        if (nonAppliedControlAllocations == null || nonAppliedControlAllocations.isEmpty()) {
            nonAppliedControlAllocations = getPaymentApplicationDocument()
                    .allocateFundsFromUnappliedControls(nonAppliedControlHoldings, getTotalApplied());
        }
        return nonAppliedControlAllocations;
    }

    public void setNonAppliedControlAllocations(final Map<String, KualiDecimal> nonAppliedControlAllocations) {
        this.nonAppliedControlAllocations = nonAppliedControlAllocations;
    }

    /**
     * @param documentNumber
     * @return
     */
    public KualiDecimal getNonAppliedControlAllocation(final String documentNumber) {
        if (!getNonAppliedControlAllocations().containsKey(documentNumber)) {
            return KualiDecimal.ZERO;
        }
        return getNonAppliedControlAllocations().get(documentNumber);
    }

    @Override
    protected void populateFalseCheckboxes(final HttpServletRequest request) {
        final Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.get("checkboxToReset") != null) {
            final String[] checkboxesToReset = request.getParameterValues("checkboxToReset");
            if (checkboxesToReset != null && checkboxesToReset.length > 0) {
                for (final String propertyName : checkboxesToReset) {
                    if (StringUtils.isNotBlank(propertyName) && parameterMap.get(propertyName) == null) {
                        populateForProperty(propertyName, KimConstants.KIM_ATTRIBUTE_BOOLEAN_FALSE_STR_VALUE_DISPLAY,
                                parameterMap);
                    } else if (StringUtils.isNotBlank(propertyName)
                            && parameterMap.get(propertyName) != null
                            && parameterMap.get(propertyName).length >= 1
                            && parameterMap.get(propertyName)[0].equalsIgnoreCase("on")) {
                        populateForProperty(propertyName, KimConstants.KIM_ATTRIBUTE_BOOLEAN_TRUE_STR_VALUE_DISPLAY,
                                parameterMap);
                    }
                }
            }
        }
    }

    // CU Customization (KFSPTS-13246): Add helper property for invoice-paid-applied deletion enhancement.

    //Used to prevent optimistic lock exception generation for table AR_INV_PD_APLD_T
    //when user has previously saved the edoc but subsequently deletes one or more
    //invoicePaidApplieds items and attempts a save or submit/route.
    protected transient boolean manualInvoicePaidAppliedDatabaseDeletionRequired = false;

    public boolean isManualInvoicePaidAppliedDatabaseDeletionRequired() {
        return manualInvoicePaidAppliedDatabaseDeletionRequired;
    }

    public void setManualInvoicePaidAppliedDatabaseDeletionRequired(
            boolean manualInvoicePaidAppliedDatabaseDeletionRequired) {
        this.manualInvoicePaidAppliedDatabaseDeletionRequired = manualInvoicePaidAppliedDatabaseDeletionRequired;
    }

    // End CU Customization

    public DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    /**
     * An inner class to point to a specific entry in a group
     */
    protected class EntryHolder {
        private final Date date;
        private final Object holder;

        public EntryHolder(final Date date, final Object holder) {
            this.date = date;
            this.holder = holder;
        }

        public Date getDate() {
            return date;
        }

        public Object getHolder() {
            return holder;
        }
    }

    /**
     * This comparator is used internally for sorting the list of invoices
     */
    protected static class EntryHolderComparator implements Comparator<EntryHolder> {

        /**
         * Compares two Objects based on their creation date
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(final EntryHolder rosencrantz, final EntryHolder guildenstern) {
            return rosencrantz.getDate().compareTo(guildenstern.getDate());
        }
    }
}
