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
package org.kuali.kfs.module.ar.document;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectCodeCurrent;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.BlanketApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedDistribution;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.businessobject.NonInvoiced;
import org.kuali.kfs.module.ar.businessobject.NonInvoicedDistribution;
import org.kuali.kfs.module.ar.businessobject.ReceivableCustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.document.service.AccountsReceivablePendingEntryService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.NonAppliedHoldingService;
import org.kuali.kfs.module.ar.document.service.PaymentApplicationDocumentService;
import org.kuali.kfs.module.ar.document.service.SystemInformationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AmountTotaling;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocumentBase;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.UniversityDateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentApplicationDocument extends GeneralLedgerPostingDocumentBase implements
        GeneralLedgerPendingEntrySource, PaymentApplicationAdjustableDocument, AmountTotaling {

    private static final Logger LOG = LogManager.getLogger();

    protected static final String LAUNCHED_FROM_BATCH = "LaunchedBySystemUser";

    // The documentNumber of the PaymentApplicationAdjustmentDocument which adjusts this Document. Can be null.
    private String adjusterDocumentNumber;
    protected String hiddenFieldForErrors;
    protected List<InvoicePaidApplied> invoicePaidApplieds;
    protected List<NonInvoiced> nonInvoiceds;
    protected Collection<NonInvoicedDistribution> nonInvoicedDistributions;
    protected Collection<NonAppliedDistribution> nonAppliedDistributions;
    protected List<NonAppliedHolding> nonAppliedHoldings;
    protected AccountsReceivableDocumentHeader accountsReceivableDocumentHeader;
    // this document type variable would help in differentiating Customer and CG Invoices
    protected String invoiceDocumentType;
    // To categorize the CG Invoices based on Award LOC Type
    protected String letterOfCreditCreationType;
    // For loc creation type = Award
    protected String proposalNumber;
    // for loc creation type = LOC fund
    protected String letterOfCreditFundGroupCode;
    // for loc creation type = LOC fund group
    protected String letterOfCreditFundCode;
    protected transient PaymentApplicationDocumentService paymentApplicationDocumentService;
    protected transient CashControlDetail cashControlDetail;
    protected transient FinancialSystemUserService fsUserService;
    protected transient CustomerInvoiceDocumentService invoiceDocService;
    protected transient DocumentService docService;
    protected transient NonAppliedHoldingService nonAppliedHoldingService;
    protected transient BusinessObjectService boService;
    private static transient volatile AccountsReceivablePendingEntryService accountsReceivablePendingEntryService;

    // control docs for non-cash-control payapps
    protected ArrayList<NonAppliedHolding> nonAppliedHoldingsForCustomer;

    public PaymentApplicationDocument() {
        super();
        invoicePaidApplieds = new ArrayList<>();
        nonInvoiceds = new ArrayList<>();
        nonInvoicedDistributions = new ArrayList<>();
        nonAppliedDistributions = new ArrayList<>();
        nonAppliedHoldingsForCustomer = new ArrayList<>();
        nonAppliedHoldings = new ArrayList<>();
    }

    public void setAdjusterDocumentNumber(final String adjusterDocumentNumber) {
        this.adjusterDocumentNumber = adjusterDocumentNumber;
    }

    public String getAdjusterDocumentNumber() {
        return adjusterDocumentNumber;
    }

    @Override
    public String getAdjustmentDocumentNumber() {
        return getAdjusterDocumentNumber();
    }

    public void clearAdjusterDocumentNumber() {
        adjusterDocumentNumber = null;
    }

    @Override
    public boolean isAdjusted() {
        return StringUtils.isNotBlank(adjusterDocumentNumber);
    }

    /**
     * @return CustomerPaymentMediumIdentifier from the associated CashControlDetail if one exists, otherwise null.
     */
    public String getPaymentNumber() {
        return hasCashControlDetail() ? getCashControlDetail().getCustomerPaymentMediumIdentifier() : null;
    }

    /**
     * @return
     */
    public boolean hasCashControlDocument() {
        return getCashControlDocument() != null;
    }

    /**
     * @return
     */
    public CashControlDocument getCashControlDocument() {
        final CashControlDetail cashControlDetail = getCashControlDetail();
        if (ObjectUtils.isNull(cashControlDetail)) {
            return null;
        }
        return cashControlDetail.getCashControlDocument();
    }

    /**
     * @return
     */
    public boolean hasCashControlDetail() {
        return null != getCashControlDetail();
    }

    /**
     * @return
     */
    public CashControlDetail getCashControlDetail() {
        if (cashControlDetail == null) {
            cashControlDetail = getPaymentApplicationDocumentService().getCashControlDetailForPayAppDocNumber(getDocumentNumber());
        }
        return cashControlDetail;
    }

    /**
     * @param cashControlDetail
     */
    public void setCashControlDetail(final CashControlDetail cashControlDetail) {
        this.cashControlDetail = cashControlDetail;
    }

    /**
     * This method calculates the total amount available to be applied on this document.
     *
     * @return The total from the cash control detail if this is a cash-control based payapp. Otherwise, it just
     *         returns the total available to be applied from previously unapplied holdings.
     */
    public KualiDecimal getTotalFromControl() {
        if (hasCashControlDetail()) {
            return getCashControlDetail().getFinancialDocumentLineAmount();
        } else {
            return getNonAppliedControlAvailableUnappliedAmount();
        }
    }

    /**
     * This method calculates the total amount available to be applied from previously unapplied funds for the
     * associated customer.
     *
     * @return The total amount of previously NonApplied funds available to apply to invoices and other applications
     *         on this document.
     */
    public KualiDecimal getNonAppliedControlAvailableUnappliedAmount() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for (final NonAppliedHolding nonAppliedHolding : nonAppliedHoldingsForCustomer) {
            amount = amount.add(nonAppliedHolding.getAvailableUnappliedAmount());
        }
        return amount;
    }

    /**
     * @return the sum of all invoice paid applieds.
     */
    public KualiDecimal getSumOfInvoicePaidApplieds() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for (final InvoicePaidApplied payment : getInvoicePaidApplieds()) {
            KualiDecimal invoiceItemAppliedAmount = payment.getInvoiceItemAppliedAmount();
            if (null == invoiceItemAppliedAmount) {
                invoiceItemAppliedAmount = KualiDecimal.ZERO;
            }
            amount = amount.add(invoiceItemAppliedAmount);
        }
        return amount;
    }

    /**
     * @return the sum of all non-invoiced amounts
     */
    public KualiDecimal getSumOfNonInvoiceds() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final NonInvoiced payment : getNonInvoiceds()) {
            total = total.add(payment.getFinancialDocumentLineAmount());
        }
        return total;
    }

    /**
     * @return the sum of all non-invoiced distributions
     */
    public KualiDecimal getSumOfNonInvoicedDistributions() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for (final NonInvoicedDistribution nonInvoicedDistribution : getNonInvoicedDistributions()) {
            amount = amount.add(nonInvoicedDistribution.getFinancialDocumentLineAmount());
        }
        return amount;
    }

    /**
     * @return the sum of all non-applied distributions
     */
    public KualiDecimal getSumOfNonAppliedDistributions() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for (final NonAppliedDistribution nonAppliedDistribution : getNonAppliedDistributions()) {
            amount = amount.add(nonAppliedDistribution.getFinancialDocumentLineAmount());
        }
        return amount;
    }

    /**
     * @return the non-applied holding total.
     */
    public KualiDecimal getNonAppliedHoldingAmount() {
        if (ObjectUtils.isNull(getNonAppliedHolding())) {
            return KualiDecimal.ZERO;
        }
        if (ObjectUtils.isNull(getNonAppliedHolding().getFinancialDocumentLineAmount())) {
            return KualiDecimal.ZERO;
        }
        return getNonAppliedHolding().getFinancialDocumentLineAmount();
    }

    /**
     * This method returns the total amount allocated against the cash control total.
     *
     * @return
     */
    public KualiDecimal getTotalApplied() {
        KualiDecimal amount = KualiDecimal.ZERO;
        amount = amount.add(getSumOfInvoicePaidApplieds());
        amount = amount.add(getSumOfNonInvoiceds());
        amount = amount.add(getNonAppliedHoldingAmount());
        return amount;
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        return getTotalFromControl();
    }

    /**
     * This method subtracts the sum of the invoice paid applieds, non-ar and
     * unapplied totals from the outstanding amount received via the cash control document.
     * <p>
     * NOTE this method is not useful for a non-cash control PayApp, as it
     * doesn't have access to the control documents until it is saved.  Use
     * the same named method on the Form instead.
     *
     * @return
     */
    public KualiDecimal getUnallocatedBalance() {

        KualiDecimal amount = getTotalFromControl();
        amount = amount.subtract(getTotalApplied());
        return amount;
    }

    /**
     * @return
     */
    @Override
    public KualiDecimal getNonArTotal() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final NonInvoiced item : getNonInvoiceds()) {
            total = total.add(item.getFinancialDocumentLineAmount());
        }
        return total;
    }

    /**
     * @return
     */
    public boolean isFinal() {
        return isApproved();
    }

    /**
     * @return
     */
    public boolean isApproved() {
        return getDocumentHeader().getWorkflowDocument().isApproved();
    }

    /**
     * This method is very specialized for a specific use. It retrieves the list of invoices that have been paid-applied by this
     * PayApp document. It is only used to retrieve what invoices were applied to it, when the document is being viewed in Final
     * state.
     *
     * @return
     */
    public List<CustomerInvoiceDocument> getInvoicesPaidAgainst() {
        final List<CustomerInvoiceDocument> invoices = new ArrayList<>();

        // short circuit if no paidapplieds available
        if (invoicePaidApplieds == null || invoicePaidApplieds.isEmpty()) {
            return invoices;
        }

        // get the list of invoice docnumbers from paidapplieds
        final List<String> invoiceDocNumbers = new ArrayList<>();
        for (final InvoicePaidApplied paidApplied : invoicePaidApplieds) {
            invoiceDocNumbers.add(paidApplied.getFinancialDocumentReferenceInvoiceNumber());
        }

        // attempt to retrieve all the invoices paid applied against
        for (final Document doc : getDocService().getDocumentsByListOfDocumentHeaderIds(CustomerInvoiceDocument.class, invoiceDocNumbers)) {
            invoices.add((CustomerInvoiceDocument) doc);
        }
        return invoices;
    }

    /**
     * This is a very specialized method, that is only intended to be used once the document is in a Final/Approved state. It
     * retrieves the PaymentApplication documents that were used as a control source for this document, if any, or none, if none.
     *
     * @return
     */
    public List<PaymentApplicationDocument> getPaymentApplicationDocumentsUsedAsControlDocuments() {
        final List<PaymentApplicationDocument> payApps = new ArrayList<>();

        // short circuit if no non-applied-distributions available
        if ((nonAppliedDistributions == null || nonAppliedDistributions.isEmpty())
                && (nonInvoicedDistributions == null || nonInvoicedDistributions.isEmpty())) {
            return payApps;
        }

        // get the list of payapp docnumbers from non-applied-distributions
        final List<String> payAppDocNumbers = new ArrayList<>();
        for (final NonAppliedDistribution nonAppliedDistribution : nonAppliedDistributions) {
            if (!payAppDocNumbers.contains(nonAppliedDistribution.getReferenceFinancialDocumentNumber())) {
                payAppDocNumbers.add(nonAppliedDistribution.getReferenceFinancialDocumentNumber());
            }
        }

        // get the list of payapp docnumbers from non-applied-distributions
        for (final NonInvoicedDistribution nonInvoicedDistribution : nonInvoicedDistributions) {
            if (!payAppDocNumbers.contains(nonInvoicedDistribution.getReferenceFinancialDocumentNumber())) {
                payAppDocNumbers.add(nonInvoicedDistribution.getReferenceFinancialDocumentNumber());
            }
        }

        // exit out if no results, dont even try to retrieve
        if (payAppDocNumbers.isEmpty()) {
            return payApps;
        }

        // attempt to retrieve all the invoices paid applied against
        for (final Document doc : getDocService().getDocumentsByListOfDocumentHeaderIds(PaymentApplicationDocument.class, payAppDocNumbers)) {
            payApps.add((PaymentApplicationDocument) doc);
        }
        return payApps;
    }

    /**
     * @return
     */
    public List<NonAppliedHolding> getNonAppliedHoldingsUsedAsControls() {
        final List<NonAppliedHolding> nonAppliedHoldingControls = new ArrayList<>();

        // short circuit if no non-applied-distributions available
        if ((nonAppliedDistributions == null || nonAppliedDistributions.isEmpty())
                && (nonInvoicedDistributions == null || nonInvoicedDistributions.isEmpty())) {
            return nonAppliedHoldingControls;
        }

        // get the list of payapp docnumbers from non-applied-distributions
        final List<String> payAppDocNumbers = new ArrayList<>();
        for (final NonAppliedDistribution nonAppliedDistribution : nonAppliedDistributions) {
            if (!payAppDocNumbers.contains(nonAppliedDistribution.getReferenceFinancialDocumentNumber())) {
                payAppDocNumbers.add(nonAppliedDistribution.getReferenceFinancialDocumentNumber());
            }
        }

        // get the list of non-invoiced/non-ar distro payapp doc numbers
        for (final NonInvoicedDistribution nonInvoicedDistribution : nonInvoicedDistributions) {
            if (!payAppDocNumbers.contains(nonInvoicedDistribution.getReferenceFinancialDocumentNumber())) {
                payAppDocNumbers.add(nonInvoicedDistribution.getReferenceFinancialDocumentNumber());
            }
        }

        // attempt to retrieve all the non applied holdings used as controls
        if (!payAppDocNumbers.isEmpty()) {
            nonAppliedHoldingControls.addAll(getNonAppliedHoldingService().getNonAppliedHoldingsByListOfDocumentNumbers(payAppDocNumbers));
        }
        return nonAppliedHoldingControls;
    }

    /**
     * @return
     */
    @Override
    public List<InvoicePaidApplied> getInvoicePaidApplieds() {
        return invoicePaidApplieds;
    }

    /**
     * @param appliedPayments
     */
    public void setInvoicePaidApplieds(final List<InvoicePaidApplied> appliedPayments) {
        invoicePaidApplieds = appliedPayments;
    }

    /**
     * @return
     */
    public List<NonInvoiced> getNonInvoiceds() {
        return nonInvoiceds;
    }

    /**
     * @param nonInvoiceds
     */
    public void setNonInvoiceds(final List<NonInvoiced> nonInvoiceds) {
        this.nonInvoiceds = nonInvoiceds;
    }

    /**
     * @return
     */
    public Collection<NonInvoicedDistribution> getNonInvoicedDistributions() {
        return nonInvoicedDistributions;
    }

    /**
     * @param nonInvoicedDistributions
     */
    public void setNonInvoicedDistributions(final Collection<NonInvoicedDistribution> nonInvoicedDistributions) {
        this.nonInvoicedDistributions = nonInvoicedDistributions;
    }

    /**
     * @return
     */
    public Collection<NonAppliedDistribution> getNonAppliedDistributions() {
        return nonAppliedDistributions;
    }

    /**
     * @param nonAppliedDistributions
     */
    public void setNonAppliedDistributions(final Collection<NonAppliedDistribution> nonAppliedDistributions) {
        this.nonAppliedDistributions = nonAppliedDistributions;
    }

    /**
     * @return
     */
    public NonAppliedHolding getNonAppliedHolding() {
        if (!nonAppliedHoldings.isEmpty()) {
            return IterableUtils.get(nonAppliedHoldings, 0);
        }
        return null;
    }

    /**
     * @param nonAppliedHolding
     */
    public void setNonAppliedHolding(final NonAppliedHolding nonAppliedHolding) {
        nonAppliedHoldings.clear();
        if (nonAppliedHolding != null) {
            nonAppliedHoldings.add(nonAppliedHolding);
        }
    }

    /**
     * @return
     */
    @Override
    public AccountsReceivableDocumentHeader getAccountsReceivableDocumentHeader() {
        return accountsReceivableDocumentHeader;
    }

    /**
     * @param accountsReceivableDocumentHeader
     */
    public void setAccountsReceivableDocumentHeader(final AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = accountsReceivableDocumentHeader;
    }

    /**
     * This method retrieves a specific applied payment from the list, by array index
     *
     * @param index the index of the applied payment to retrieve
     * @return an InvoicePaidApplied
     */
    public InvoicePaidApplied getInvoicePaidApplied(final int index) {

        return index < getInvoicePaidApplieds().size() ? getInvoicePaidApplieds().get(index) : new InvoicePaidApplied();
    }

    /**
     * This method retrieves a specific non invoiced payment from the list, by array index
     *
     * @param index the index of the non invoiced payment to retrieve
     * @return an NonInvoiced
     */
    public NonInvoiced getNonInvoiced(final int index) {
        return index < getNonInvoiceds().size() ? getNonInvoiceds().get(index) : new NonInvoiced();
    }

    /**
     * This method gets an ObjectCode from an invoice document.
     *
     * @param invoicePaidApplied
     * @return
     */
    protected ObjectCode getInvoiceReceivableObjectCode(final InvoicePaidApplied invoicePaidApplied) {
        final CustomerInvoiceDocument customerInvoiceDocument = invoicePaidApplied.getCustomerInvoiceDocument();
        final CustomerInvoiceDetail customerInvoiceDetail = invoicePaidApplied.getInvoiceDetail();
        final ReceivableCustomerInvoiceDetail receivableInvoiceDetail = new ReceivableCustomerInvoiceDetail(customerInvoiceDetail, customerInvoiceDocument);
        ObjectCode objectCode = null;
        if (ObjectUtils.isNotNull(receivableInvoiceDetail) && ObjectUtils.isNotNull(receivableInvoiceDetail.getFinancialObjectCode())) {
            objectCode = receivableInvoiceDetail.getObjectCode();
            if (ObjectUtils.isNull(objectCode)) {
                final Map<String, Object> fieldKeys = new HashMap<>();
                fieldKeys.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, receivableInvoiceDetail.getChartOfAccountsCode());
                fieldKeys.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, receivableInvoiceDetail.getFinancialObjectCode());
                objectCode = getBusinessObjectService().findByPrimaryKey(ObjectCodeCurrent.class, fieldKeys);
            }
        }
        return objectCode;
    }

    /**
     * @param sequenceHelper
     * @return the pending entries for the document
     */
    protected List<GeneralLedgerPendingEntry> createPendingEntries(
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {

        for (final InvoicePaidApplied ipa : getInvoicePaidApplieds()) {
            //CU Customization set Customer Invoice Detail Posting Year if null
            if (ObjectUtils.isNull(ipa.getInvoiceDetail().getPostingYear())) {
                ipa.getInvoiceDetail().setPostingYear(getPostingYear());
                getBusinessObjectService().save(ipa.getInvoiceDetail());
            }
        }

        // Collection of all generated entries
        final List<GeneralLedgerPendingEntry> generatedEntries = new ArrayList<>();

        // Get handles to the services we need
        final GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        final UniversityDateService universityDateService = SpringContext.getBean(UniversityDateService.class);
        final SystemInformationService systemInformationService = SpringContext.getBean(SystemInformationService.class);
        final OffsetDefinitionService offsetDefinitionService = SpringContext.getBean(OffsetDefinitionService.class);
        final DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);

        // Current fiscal year
        final Integer currentFiscalYear = universityDateService.getCurrentFiscalYear();

        // Document type codes
        final String paymentApplicationDocumentTypeCode = dataDictionaryService.getDocumentTypeNameByClass(
                PaymentApplicationDocument.class);

        // The processing chart and org comes from the the cash control document if there is one.
        // If the payment application document is created from scratch though, then we pull it
        // from the current user. Note that we're not checking here that the user actually belongs
        // to a billing or processing org, we're assuming that is handled elsewhere.
        final String processingChartCode;
        final String processingOrganizationCode;
        if (hasCashControlDocument()) {
            processingChartCode = getCashControlDocument().getAccountsReceivableDocumentHeader()
                    .getProcessingChartOfAccountCode();
            processingOrganizationCode = getCashControlDocument().getAccountsReceivableDocumentHeader()
                    .getProcessingOrganizationCode();
        } else {
            final Person currentUser = GlobalVariables.getUserSession().getPerson();
            final ChartOrgHolder userOrg = getFsUserService().getPrimaryOrganization(currentUser.getPrincipalId(),
                    ArConstants.AR_NAMESPACE_CODE);
            processingChartCode = userOrg.getChartOfAccountsCode();
            processingOrganizationCode = userOrg.getOrganizationCode();
        }

        // Some information comes from the cash control document
        final CashControlDocument cashControlDocument = getCashControlDocument();

        // Get the System Information
        final SystemInformation unappliedSystemInformation = systemInformationService.getByProcessingChartOrgAndFiscalYear(
                processingChartCode, processingOrganizationCode, currentFiscalYear);

        // Get the university clearing account
        unappliedSystemInformation.refreshReferenceObject("universityClearingAccount");
        final Account universityClearingAccount = unappliedSystemInformation.getUniversityClearingAccount();

        // Get the university clearing object, object type and sub-object code
        final String unappliedSubAccountNumber = unappliedSystemInformation.getUniversityClearingSubAccountNumber();
        final String unappliedObjectCode = unappliedSystemInformation.getUniversityClearingObjectCode();
        final String unappliedObjectTypeCode = unappliedSystemInformation.getUniversityClearingObject()
                .getFinancialObjectTypeCode();
        final String unappliedSubObjectCode = unappliedSystemInformation.getUniversityClearingSubObjectCode();

        // Get the object code for the university clearing account.
        final SystemInformation universityClearingAccountSystemInformation = systemInformationService
                .getByProcessingChartOrgAndFiscalYear(processingChartCode, processingOrganizationCode,
                        currentFiscalYear);
        final String universityClearingAccountObjectCode = universityClearingAccountSystemInformation
                .getUniversityClearingObjectCode();

        // Generate glpes for unapplied
        final NonAppliedHolding holding = getNonAppliedHolding();
        if (ObjectUtils.isNotNull(holding)) {
            final GeneralLedgerPendingEntry actualCreditUnapplied = new GeneralLedgerPendingEntry();
            actualCreditUnapplied.setUniversityFiscalYear(getPostingYear());
            actualCreditUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditUnapplied.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualCreditUnapplied.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualCreditUnapplied.setFinancialObjectCode(unappliedObjectCode);
            actualCreditUnapplied.setFinancialObjectTypeCode(unappliedObjectTypeCode);
            actualCreditUnapplied.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualCreditUnapplied.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualCreditUnapplied.setTransactionLedgerEntryAmount(holding.getFinancialDocumentLineAmount().abs());
            if (StringUtils.isBlank(unappliedSubAccountNumber)) {
                actualCreditUnapplied.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                actualCreditUnapplied.setSubAccountNumber(unappliedSubAccountNumber);
            }
            if (StringUtils.isBlank(unappliedSubObjectCode)) {
                actualCreditUnapplied.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                actualCreditUnapplied.setFinancialSubObjectCode(unappliedSubObjectCode);
            }
            actualCreditUnapplied.setProjectCode(KFSConstants.getDashProjectCode());
            actualCreditUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            actualCreditUnapplied.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(actualCreditUnapplied);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry offsetDebitUnapplied = new GeneralLedgerPendingEntry();
            offsetDebitUnapplied.setUniversityFiscalYear(actualCreditUnapplied.getUniversityFiscalYear());
            offsetDebitUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetDebitUnapplied.setChartOfAccountsCode(actualCreditUnapplied.getChartOfAccountsCode());
            offsetDebitUnapplied.setAccountNumber(actualCreditUnapplied.getAccountNumber());
            final OffsetDefinition offsetDebitDefinition = offsetDefinitionService.getActiveByPrimaryId(
                    getPostingYear(),
                    universityClearingAccount.getChartOfAccountsCode(),
                    paymentApplicationDocumentTypeCode,
                    KFSConstants.BALANCE_TYPE_ACTUAL
            ).orElse(null);
            if (offsetDebitDefinition == null) {
                GlobalVariables.getMessageMap().putError(
                        KFSConstants.GENERAL_LEDGER_PENDING_ENTRIES_TAB_ERRORS,
                        KFSKeyConstants.ERROR_DOCUMENT_NO_OFFSET_DEFINITION,
                        getPostingYear().toString(),
                        universityClearingAccount.getChartOfAccountsCode(),
                        paymentApplicationDocumentTypeCode,
                        KFSConstants.BALANCE_TYPE_ACTUAL
                );
                throw new RuntimeException("No active offset definition found");
            }
            offsetDebitDefinition.refreshReferenceObject("financialObject");
            offsetDebitUnapplied.setFinancialObjectCode(offsetDebitDefinition.getFinancialObjectCode());
            offsetDebitUnapplied.setFinancialObjectTypeCode(offsetDebitDefinition.getFinancialObject()
                    .getFinancialObjectTypeCode());
            offsetDebitUnapplied.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetDebitUnapplied.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetDebitUnapplied.setTransactionLedgerEntryAmount(actualCreditUnapplied.getTransactionLedgerEntryAmount().abs());
            offsetDebitUnapplied.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            offsetDebitUnapplied.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetDebitUnapplied.setProjectCode(KFSConstants.getDashProjectCode());
            offsetDebitUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            offsetDebitUnapplied.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(offsetDebitUnapplied);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry actualDebitUnapplied = new GeneralLedgerPendingEntry();
            actualDebitUnapplied.setUniversityFiscalYear(getPostingYear());
            actualDebitUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitUnapplied.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitUnapplied.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualDebitUnapplied.setFinancialObjectCode(unappliedObjectCode);
            actualDebitUnapplied.setFinancialObjectTypeCode(unappliedObjectTypeCode);
            actualDebitUnapplied.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualDebitUnapplied.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualDebitUnapplied.setTransactionLedgerEntryAmount(holding.getFinancialDocumentLineAmount().abs());
            if (StringUtils.isBlank(unappliedSubAccountNumber)) {
                actualDebitUnapplied.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                actualDebitUnapplied.setSubAccountNumber(unappliedSubAccountNumber);
            }
            actualDebitUnapplied.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            actualDebitUnapplied.setProjectCode(KFSConstants.getDashProjectCode());
            actualDebitUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            actualDebitUnapplied.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(actualDebitUnapplied);
            sequenceHelper.increment();

            // Offsets for unapplied entries are just offsets to themselves, same info.
            // So set the values into the offsets based on the values in the actuals.
            final GeneralLedgerPendingEntry offsetCreditUnapplied = new GeneralLedgerPendingEntry();
            offsetCreditUnapplied.setUniversityFiscalYear(actualDebitUnapplied.getUniversityFiscalYear());
            offsetCreditUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetCreditUnapplied.setChartOfAccountsCode(actualDebitUnapplied.getChartOfAccountsCode());
            offsetCreditUnapplied.setAccountNumber(actualDebitUnapplied.getAccountNumber());
            final OffsetDefinition offsetCreditDefinition = offsetDefinitionService.getActiveByPrimaryId(
                    getPostingYear(),
                    universityClearingAccount.getChartOfAccountsCode(),
                    paymentApplicationDocumentTypeCode,
                    KFSConstants.BALANCE_TYPE_ACTUAL
            ).orElse(null);
            if (offsetCreditDefinition == null) {
                GlobalVariables.getMessageMap().putError(
                        KFSConstants.GENERAL_LEDGER_PENDING_ENTRIES_TAB_ERRORS,
                        KFSKeyConstants.ERROR_DOCUMENT_NO_OFFSET_DEFINITION,
                        getPostingYear().toString(),
                        universityClearingAccount.getChartOfAccountsCode(),
                        paymentApplicationDocumentTypeCode,
                        KFSConstants.BALANCE_TYPE_ACTUAL
                );
                throw new RuntimeException("No active offset definition found");
            }
            offsetCreditDefinition.refreshReferenceObject("financialObject");
            offsetCreditUnapplied.setFinancialObjectCode(offsetCreditDefinition.getFinancialObjectCode());
            offsetCreditUnapplied.setFinancialObjectTypeCode(offsetCreditDefinition.getFinancialObject().getFinancialObjectTypeCode());
            offsetCreditUnapplied.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetCreditUnapplied.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetCreditUnapplied.setTransactionLedgerEntryAmount(actualDebitUnapplied.getTransactionLedgerEntryAmount().abs());
            offsetCreditUnapplied.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            offsetCreditUnapplied.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetCreditUnapplied.setProjectCode(KFSConstants.getDashProjectCode());
            offsetCreditUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            offsetCreditUnapplied.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(offsetCreditUnapplied);
            sequenceHelper.increment();
        }

        // Generate glpes for non-ar
        for (final NonInvoiced nonInvoiced : getNonInvoiceds()) {
            // Actual entries
            final GeneralLedgerPendingEntry actualCreditEntry = new GeneralLedgerPendingEntry();
            actualCreditEntry.setUniversityFiscalYear(getPostingYear());
            actualCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditEntry.setChartOfAccountsCode(nonInvoiced.getChartOfAccountsCode());
            actualCreditEntry.setAccountNumber(nonInvoiced.getAccountNumber());
            actualCreditEntry.setFinancialObjectCode(nonInvoiced.getFinancialObjectCode());
            nonInvoiced.refreshReferenceObject("financialObject");
            actualCreditEntry.setFinancialObjectTypeCode(nonInvoiced.getFinancialObject().getFinancialObjectTypeCode());
            actualCreditEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualCreditEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount().abs());
            if (StringUtils.isBlank(nonInvoiced.getSubAccountNumber())) {
                actualCreditEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                actualCreditEntry.setSubAccountNumber(nonInvoiced.getSubAccountNumber());
            }
            if (StringUtils.isBlank(nonInvoiced.getFinancialSubObjectCode())) {
                actualCreditEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                actualCreditEntry.setFinancialSubObjectCode(nonInvoiced.getFinancialSubObjectCode());
            }
            if (StringUtils.isBlank(nonInvoiced.getProjectCode())) {
                actualCreditEntry.setProjectCode(KFSConstants.getDashProjectCode());
            } else {
                actualCreditEntry.setProjectCode(nonInvoiced.getProjectCode());
            }
            actualCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            final String entryLineDescription = StringUtils.defaultString(nonInvoiced.getFinancialDocumentLineDescription(),
                    getDocumentHeader().getDocumentDescription());
            actualCreditEntry.setTransactionLedgerEntryDescription(entryLineDescription);
            generatedEntries.add(actualCreditEntry);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry actualDebitEntry = new GeneralLedgerPendingEntry();
            actualDebitEntry.setUniversityFiscalYear(getPostingYear());
            actualDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitEntry.setAccountNumber(universityClearingAccount.getAccountNumber());

            if (hasCashControlDocument()) {
                actualDebitEntry.setFinancialObjectCode(universityClearingAccountObjectCode);
                final ObjectCodeService objectCodeService = SpringContext.getBean(ObjectCodeService.class);
                final ObjectCode objectCode = objectCodeService.getByPrimaryIdWithCaching(getPostingYear(),
                        universityClearingAccountSystemInformation.getUniversityClearingChartOfAccounts()
                                .getChartOfAccountsCode(), universityClearingAccountObjectCode);
                actualDebitEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
                actualDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                actualDebitEntry.setFinancialObjectCode(unappliedObjectCode);
                actualDebitEntry.setFinancialObjectTypeCode(unappliedObjectTypeCode);
                if (StringUtils.isBlank(unappliedSubObjectCode)) {
                    actualDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                } else {
                    actualDebitEntry.setFinancialSubObjectCode(unappliedSubObjectCode);
                }
            }
            actualDebitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualDebitEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount().abs());
            if (StringUtils.isBlank(unappliedSubAccountNumber)) {
                actualDebitEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                actualDebitEntry.setSubAccountNumber(unappliedSubAccountNumber);
            }
            actualDebitEntry.setProjectCode(KFSConstants.getDashProjectCode());
            actualDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            actualDebitEntry.setTransactionLedgerEntryDescription(entryLineDescription);
            generatedEntries.add(actualDebitEntry);
            sequenceHelper.increment();

            // Offset entries
            final GeneralLedgerPendingEntry offsetDebitEntry = new GeneralLedgerPendingEntry();
            offsetDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetDebitEntry.setChartOfAccountsCode(nonInvoiced.getChartOfAccountsCode());
            offsetDebitEntry.setAccountNumber(nonInvoiced.getAccountNumber());
            offsetDebitEntry.setUniversityFiscalYear(getPostingYear());
            final OffsetDefinition debitOffsetDefinition = offsetDefinitionService.getActiveByPrimaryId(
                    getPostingYear(),
                    nonInvoiced.getChartOfAccountsCode(),
                    paymentApplicationDocumentTypeCode,
                    KFSConstants.BALANCE_TYPE_ACTUAL
            ).orElse(null);
            if (debitOffsetDefinition == null) {
                GlobalVariables.getMessageMap().putError(
                        KFSConstants.GENERAL_LEDGER_PENDING_ENTRIES_TAB_ERRORS,
                        KFSKeyConstants.ERROR_DOCUMENT_NO_OFFSET_DEFINITION,
                        getPostingYear().toString(),
                        nonInvoiced.getChartOfAccountsCode(),
                        paymentApplicationDocumentTypeCode,
                        KFSConstants.BALANCE_TYPE_ACTUAL
                );
                throw new RuntimeException("No active offset definition found");
            }
            debitOffsetDefinition.refreshReferenceObject("financialObject");
            offsetDebitEntry.setFinancialObjectCode(debitOffsetDefinition.getFinancialObjectCode());
            offsetDebitEntry.setFinancialObjectTypeCode(debitOffsetDefinition.getFinancialObject().getFinancialObjectTypeCode());
            offsetDebitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetDebitEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount().abs());
            offsetDebitEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            offsetDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetDebitEntry.setProjectCode(KFSConstants.getDashProjectCode());
            offsetDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            offsetDebitEntry.setTransactionLedgerEntryDescription(entryLineDescription);
            generatedEntries.add(offsetDebitEntry);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry offsetCreditEntry = new GeneralLedgerPendingEntry();
            offsetCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetCreditEntry.setUniversityFiscalYear(getPostingYear());
            offsetCreditEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            offsetCreditEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            final Integer fiscalYearForCreditOffsetDefinition = null == cashControlDocument ? currentFiscalYear :
                    cashControlDocument.getUniversityFiscalYear();
            final OffsetDefinition creditOffsetDefinition = offsetDefinitionService.getActiveByPrimaryId(
                    fiscalYearForCreditOffsetDefinition,
                    processingChartCode,
                    paymentApplicationDocumentTypeCode,
                    KFSConstants.BALANCE_TYPE_ACTUAL
            ).orElse(null);
            if (creditOffsetDefinition == null) {
                GlobalVariables.getMessageMap().putError(
                        KFSConstants.GENERAL_LEDGER_PENDING_ENTRIES_TAB_ERRORS,
                        KFSKeyConstants.ERROR_DOCUMENT_NO_OFFSET_DEFINITION,
                        String.valueOf(fiscalYearForCreditOffsetDefinition),
                        processingChartCode,
                        paymentApplicationDocumentTypeCode,
                        KFSConstants.BALANCE_TYPE_ACTUAL
                );
                throw new RuntimeException("No active offset definition found");
            }
            creditOffsetDefinition.refreshReferenceObject("financialObject");
            offsetCreditEntry.setFinancialObjectCode(creditOffsetDefinition.getFinancialObjectCode());
            offsetCreditEntry.setFinancialObjectTypeCode(creditOffsetDefinition.getFinancialObject().getFinancialObjectTypeCode());
            offsetCreditEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetCreditEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount().abs());
            offsetCreditEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            offsetCreditEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetCreditEntry.setProjectCode(KFSConstants.getDashProjectCode());
            offsetCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            offsetCreditEntry.setTransactionLedgerEntryDescription(entryLineDescription);
            generatedEntries.add(offsetCreditEntry);
            sequenceHelper.increment();
        }

        // Generate GLPEs for applied payments
        final List<InvoicePaidApplied> appliedPayments = getInvoicePaidApplieds();
        for (final InvoicePaidApplied ipa : appliedPayments) {

            // Skip payments for 0 dollar amount
            if (KualiDecimal.ZERO.equals(ipa.getInvoiceItemAppliedAmount())) {
                continue;
            }

            ipa.refreshNonUpdateableReferences();
            final Account billingOrganizationAccount = ipa.getInvoiceDetail().getAccount();
            final ObjectCode invoiceObjectCode = getInvoiceReceivableObjectCode(ipa);
            // Refresh
            ObjectUtils.isNull(invoiceObjectCode);
            final ObjectCode accountsReceivableObjectCode = getAccountsReceivablePendingEntryService().getAccountsReceivableObjectCode(ipa);
            final ObjectCode unappliedCashObjectCode = ipa.getSystemInformation().getUniversityClearingObject();

            final GeneralLedgerPendingEntry actualDebitEntry = new GeneralLedgerPendingEntry();
            actualDebitEntry.setUniversityFiscalYear(getPostingYear());
            actualDebitEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount().abs());
            if (hasCashControlDocument()) {
                actualDebitEntry.setFinancialObjectCode(unappliedCashObjectCode.getFinancialObjectCode());
                actualDebitEntry.setFinancialObjectTypeCode(unappliedCashObjectCode.getFinancialObjectTypeCode());
                actualDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                actualDebitEntry.setFinancialObjectCode(unappliedObjectCode);
                actualDebitEntry.setFinancialObjectTypeCode(unappliedObjectTypeCode);
                if (StringUtils.isBlank(unappliedSubObjectCode)) {
                    actualDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                } else {
                    actualDebitEntry.setFinancialSubObjectCode(unappliedSubObjectCode);
                }
            }
            if (StringUtils.isBlank(unappliedSubAccountNumber)) {
                actualDebitEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                actualDebitEntry.setSubAccountNumber(unappliedSubAccountNumber);
            }
            actualDebitEntry.setProjectCode(KFSConstants.getDashProjectCode());
            actualDebitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            actualDebitEntry.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(actualDebitEntry);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry actualCreditEntry = new GeneralLedgerPendingEntry();
            actualCreditEntry.setUniversityFiscalYear(getPostingYear());
            actualCreditEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualCreditEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount().abs());
            actualCreditEntry.setFinancialObjectCode(invoiceObjectCode.getFinancialObjectCode());
            actualCreditEntry.setFinancialObjectTypeCode(invoiceObjectCode.getFinancialObjectTypeCode());
            actualCreditEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            actualCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualCreditEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            actualCreditEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            actualCreditEntry.setProjectCode(KFSConstants.getDashProjectCode());
            glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), actualDebitEntry, sequenceHelper,
                    actualCreditEntry);
            actualCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualCreditEntry);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry offsetDebitEntry = new GeneralLedgerPendingEntry();
            offsetDebitEntry.setUniversityFiscalYear(getPostingYear());
            offsetDebitEntry.setAccountNumber(billingOrganizationAccount.getAccountNumber());
            offsetDebitEntry.setChartOfAccountsCode(billingOrganizationAccount.getChartOfAccountsCode());
            offsetDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetDebitEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount().abs());
            offsetDebitEntry.setFinancialObjectCode(invoiceObjectCode.getFinancialObjectCode());
            offsetDebitEntry.setFinancialObjectTypeCode(invoiceObjectCode.getFinancialObjectTypeCode());
            offsetDebitEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            if (StringUtils.isBlank(ipa.getInvoiceDetail().getSubAccountNumber())) {
                offsetDebitEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                offsetDebitEntry.setSubAccountNumber(ipa.getInvoiceDetail().getSubAccountNumber());
            }
            if (StringUtils.isBlank(ipa.getInvoiceDetail().getFinancialSubObjectCode())) {
                offsetDebitEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            } else {
                offsetDebitEntry.setFinancialSubObjectCode(ipa.getInvoiceDetail().getFinancialSubObjectCode());
            }
            if (StringUtils.isBlank(ipa.getInvoiceDetail().getProjectCode())) {
                offsetDebitEntry.setProjectCode(KFSConstants.getDashProjectCode());
            } else {
                offsetDebitEntry.setProjectCode(ipa.getInvoiceDetail().getProjectCode());
            }
            offsetDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            offsetDebitEntry.setTransactionLedgerEntryDescription(getDocumentHeader().getDocumentDescription());
            generatedEntries.add(offsetDebitEntry);
            sequenceHelper.increment();

            final GeneralLedgerPendingEntry offsetCreditEntry = new GeneralLedgerPendingEntry();
            offsetCreditEntry.setUniversityFiscalYear(getPostingYear());
            offsetCreditEntry.setAccountNumber(billingOrganizationAccount.getAccountNumber());
            offsetCreditEntry.setChartOfAccountsCode(billingOrganizationAccount.getChartOfAccountsCode());
            offsetCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetCreditEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount().abs());
            offsetCreditEntry.setFinancialObjectCode(accountsReceivableObjectCode.getFinancialObjectCode());
            offsetCreditEntry.setFinancialObjectTypeCode(accountsReceivableObjectCode.getFinancialObjectTypeCode());
            offsetCreditEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            offsetCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            if (StringUtils.isBlank(ipa.getInvoiceDetail().getSubAccountNumber())) {
                offsetCreditEntry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
            } else {
                offsetCreditEntry.setSubAccountNumber(ipa.getInvoiceDetail().getSubAccountNumber());
            }

            offsetCreditEntry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
            offsetCreditEntry.setProjectCode(KFSConstants.getDashProjectCode());
            offsetCreditEntry.refreshNonUpdateableReferences();
            glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), offsetDebitEntry, sequenceHelper,
                    offsetCreditEntry);
            generatedEntries.add(offsetCreditEntry);
            sequenceHelper.increment();
        }

        // Set the origination code for all entries.
        for (final GeneralLedgerPendingEntry entry : generatedEntries) {
            entry.setFinancialSystemOriginationCode("01");
            entry.setOrganizationDocumentNumber(getDocumentHeader().getOrganizationDocumentNumber());
        }

        return generatedEntries;
    }

    @Override
    public boolean generateDocumentGeneralLedgerPendingEntries(final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        try {
            final List<GeneralLedgerPendingEntry> entries = createPendingEntries(sequenceHelper);
            for (final GeneralLedgerPendingEntry entry : entries) {
                addPendingEntry(entry);
            }
        } catch (final Throwable t) {
            LOG.error("Exception encountered while generating pending entries.", t);
            return false;
        }

        return true;
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(
            final GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return true;
    }

    @Override
    public KualiDecimal getGeneralLedgerPendingEntryAmountForDetail(
            final GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {
        return null;
    }

    @Override
    public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
        return new ArrayList<>();
    }

    @Override
    public boolean isDebit(final GeneralLedgerPendingEntrySourceDetail postable) {
        return false;
    }

    /**
     * This method is used ONLY for handleRouteStatus change and other postProcessor related tasks (like
     * getWorkflowEngineDocumentIdsToLock()) and should not otherwise be used. The reason this is its own method is to make sure
     * that handleRouteStatusChange and getWorkflowEngineDocumentIdsToLock use the same method to retrieve what invoices to update.
     *
     * @return
     */
    protected List<String> getInvoiceNumbersToUpdateOnFinal() {
        final List<String> docIds = new ArrayList<>();
        for (final InvoicePaidApplied ipa : getInvoicePaidApplieds()) {
            docIds.add(ipa.getFinancialDocumentReferenceInvoiceNumber());
        }
        return docIds;
    }

    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        final List<String> docIdStrings = getInvoiceNumbersToUpdateOnFinal();
        if (docIdStrings == null || docIdStrings.isEmpty()) {
            return null;
        }
        return docIdStrings;
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        final WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isFinal()) {
            final DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

            // get the now time to stamp invoices with
            final java.sql.Date today = new java.sql.Date(dateTimeService.getCurrentDate().getTime());

            final List<String> invoiceDocNumbers = getInvoiceNumbersToUpdateOnFinal();
            for (final String invoiceDocumentNumber : invoiceDocNumbers) {
                // attempt to retrieve the invoice doc
                final CustomerInvoiceDocument invoice = (CustomerInvoiceDocument) getDocService().getByDocumentHeaderId(
                        invoiceDocumentNumber);
                if (invoice == null) {
                    throw new RuntimeException("DocumentService returned a Null CustomerInvoice Document for Doc# " +
                            invoiceDocumentNumber + ".");
                }

                // KULAR-384 - close the invoice if its open and the openAmount is zero
                if (invoice.getOpenAmount().isZero() && invoice.isOpenInvoiceIndicator()) {
                    invoice.setClosedDate(today);
                    getInvoiceDocService().addCloseNote(invoice, getDocumentHeader().getWorkflowDocument());
                    invoice.setOpenInvoiceIndicator(false);
                    getDocService().updateDocument(invoice);
                }
            }
        } else if (workflowDocument.isDisapproved()) {
            clearCashControlDetail();
            clearAnyGeneralLedgerPendingEntries();
        } else if (workflowDocument.isCanceled()) {
            clearCashControlDetail();
            getDocumentHeader().setFinancialDocumentTotalAmount(KualiDecimal.ZERO);
        }
    }

    @Override
    public List buildListOfDeletionAwareLists() {
        final List deletionAwareLists = super.buildListOfDeletionAwareLists();
        if (invoicePaidApplieds != null) {
            deletionAwareLists.add(invoicePaidApplieds);
        }
        if (nonInvoiceds != null) {
            deletionAwareLists.add(nonInvoiceds);
        }
        if (nonInvoicedDistributions != null) {
            deletionAwareLists.add(nonInvoicedDistributions);
        }
        if (nonAppliedDistributions != null) {
            deletionAwareLists.add(nonAppliedDistributions);
        }
        return deletionAwareLists;
    }

    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        super.prepareForSave(event);

        // set primary key for NonAppliedHolding if data entered
        if (ObjectUtils.isNotNull(getNonAppliedHolding())) {
            if (ObjectUtils.isNull(getNonAppliedHolding().getReferenceFinancialDocumentNumber())) {
                getNonAppliedHolding().setReferenceFinancialDocumentNumber(documentNumber);
            }
        }

        //  generate GLPEs only when routing or blanket approving
        if (event instanceof RouteDocumentEvent || event instanceof BlanketApproveDocumentEvent) {
            // if this document is not generated thru CashControl,
            // create nonApplied and nonInvoiced Distributions
            if (!hasCashControlDetail()) {
                createDistributions();
            }

            final GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
            if (!glpeService.generateGeneralLedgerPendingEntries(this)) {
                logErrors();
                throw new ValidationException("general ledger GLPE generation failed");
            }
        }

    }

    public PaymentApplicationDocumentService getPaymentApplicationDocumentService() {
        if (null == paymentApplicationDocumentService) {
            paymentApplicationDocumentService = SpringContext.getBean(PaymentApplicationDocumentService.class);
        }
        return paymentApplicationDocumentService;
    }

    protected FinancialSystemUserService getFsUserService() {
        if (fsUserService == null) {
            fsUserService = SpringContext.getBean(FinancialSystemUserService.class);
        }
        return fsUserService;
    }

    protected CustomerInvoiceDocumentService getInvoiceDocService() {
        if (invoiceDocService == null) {
            invoiceDocService = SpringContext.getBean(CustomerInvoiceDocumentService.class);
        }
        return invoiceDocService;
    }

    protected DocumentService getDocService() {
        if (docService == null) {
            docService = SpringContext.getBean(DocumentService.class);
        }
        return docService;
    }

    protected NonAppliedHoldingService getNonAppliedHoldingService() {
        if (nonAppliedHoldingService == null) {
            nonAppliedHoldingService = SpringContext.getBean(NonAppliedHoldingService.class);
        }
        return nonAppliedHoldingService;
    }

    protected BusinessObjectService getBoService() {
        if (boService == null) {
            boService = SpringContext.getBean(BusinessObjectService.class);
        }
        return boService;
    }

    public String getHiddenFieldForErrors() {
        return hiddenFieldForErrors;
    }

    public void setHiddenFieldForErrors(final String hiddenFieldForErrors) {
        this.hiddenFieldForErrors = hiddenFieldForErrors;
    }

    /**
     * Retrieves the NonApplied Holdings that are the Controls for this PaymentApplication. Note that this is dangerous to use and
     * should not be relied upon. The data is never persisted to the database, so will always be null/empty when retrieved fresh. It
     * is only populated while the document is live from the website, or while its in flight in workflow, due to the fact that it
     * has been serialized. You should probably not be using this method unless you are sure you know what you are doing.
     *
     * @return
     */
    public Collection<NonAppliedHolding> getNonAppliedHoldingsForCustomer() {
        return nonAppliedHoldingsForCustomer;
    }

    /**
     * Warning, this property is not ever persisted to the database, and is only used during workflow processing (since its been
     * serialized) and during presentation of the document on the webapp. You should probably not be using this method unless you
     * are sure you know what you are doing.
     *
     * @param nonApplieds
     */
    public void setNonAppliedHoldingsForCustomer(final ArrayList<NonAppliedHolding> nonApplieds) {
        nonAppliedHoldingsForCustomer = nonApplieds;
    }

    /**
     * Collects and returns the combined distributions from NonInvoiced/NonAr and Unapplied. This method is intended to be used only
     * when the document has gone to final, to show what control documents were issued what funds. The return value is a
     * Map<String,KualiDecimal> where the key is the NonAppliedHolding's ReferenceFinancialDocumentNumber and the value is the
     * Amount to be applied.
     *
     * @return
     */
    public Map<String, KualiDecimal> getDistributionsFromControlDocuments() {
        if (!isFinal()) {
            throw new UnsupportedOperationException("This method should only be used once the document has been approved/gone to final.");
        }

        final Map<String, KualiDecimal> distributions = new HashMap<>();

        // short circuit if no non-applied-distributions available
        if ((nonAppliedDistributions == null || nonAppliedDistributions.isEmpty()) && (nonInvoicedDistributions == null || nonInvoicedDistributions.isEmpty())) {
            return distributions;
        }

        // get the list of payapp docnumbers from non-applied-distributions
        for (final NonAppliedDistribution nonAppliedDistribution : nonAppliedDistributions) {
            final String refDocNbr = nonAppliedDistribution.getReferenceFinancialDocumentNumber();
            if (distributions.containsKey(refDocNbr)) {
                distributions.put(refDocNbr,
                        distributions.get(refDocNbr).add(nonAppliedDistribution.getFinancialDocumentLineAmount()));
            } else {
                distributions.put(refDocNbr, nonAppliedDistribution.getFinancialDocumentLineAmount());
            }
        }

        // get the list of payapp docnumbers from non-applied-distributions
        for (final NonInvoicedDistribution nonInvoicedDistribution : nonInvoicedDistributions) {
            final String refDocNbr = nonInvoicedDistribution.getReferenceFinancialDocumentNumber();
            if (distributions.containsKey(refDocNbr)) {
                distributions.put(refDocNbr,
                        distributions.get(refDocNbr).add(nonInvoicedDistribution.getFinancialDocumentLineAmount()));
            } else {
                distributions.put(refDocNbr, nonInvoicedDistribution.getFinancialDocumentLineAmount());
            }
        }

        return distributions;
    }

    /**
     * Walks through the nonAppliedHoldings passed in (the control docs) and allocates how the funding should be allocated. This
     * function is intended to be used when the document is still live, ie not for when its been finalized. The return value is a
     * Map<String,KualiDecimal> where the key is the NonAppliedHolding's ReferenceFinancialDocumentNumber and the value is the
     * Amount to be applied.
     */
    public Map<String, KualiDecimal> allocateFundsFromUnappliedControls(final List<NonAppliedHolding> nonAppliedHoldings, final KualiDecimal amountToBeApplied) {
        if (nonAppliedHoldings == null) {
            throw new IllegalArgumentException("A null value for the parameter [nonAppliedHoldings] was passed in.");
        }
        if (amountToBeApplied == null) {
            throw new IllegalArgumentException("A null ovalue for the parameter [amountToBeApplied] was passed in.");
        }
        if (isFinal()) {
            throw new UnsupportedOperationException("This method should not be used when the document has been approved/gone to final.");
        }

        // special-case the situation where the amountToBeApplied is negative, then make all allocations zero
        if (amountToBeApplied.isNegative()) {
            final Map<String, KualiDecimal> allocations = new HashMap<>();
            for (final NonAppliedHolding nonAppliedHolding : nonAppliedHoldings) {
                allocations.put(nonAppliedHolding.getReferenceFinancialDocumentNumber(), KualiDecimal.ZERO);
            }
            return allocations;
        }

        final Map<String, KualiDecimal> allocations = new HashMap<>();
        // clone it
        KualiDecimal remainingAmount = new KualiDecimal(amountToBeApplied.toString());

        // due to the way the control list is generated, this will result in applying
        // from the oldest to newest, which is the ordering desired. If this ever changes,
        // then the internal logic here should be to apply to the oldest doc first, and then
        // move forward in time until you run out of money or docs
        for (final NonAppliedHolding nonAppliedHolding : nonAppliedHoldings) {
            final String refDocNumber = nonAppliedHolding.getReferenceFinancialDocumentNumber();

            // this shouldn't ever happen, but lets sanity check it
            if (allocations.containsKey(nonAppliedHolding.getReferenceFinancialDocumentNumber())) {
                throw new RuntimeException("The same NonAppliedHolding RefDocNumber came up twice, which should never happen.");
            } else {
                allocations.put(refDocNumber, KualiDecimal.ZERO);
            }

            if (remainingAmount.isGreaterThan(KualiDecimal.ZERO)) {
                if (nonAppliedHoldings.iterator().hasNext()) {
                    if (remainingAmount.isLessEqual(nonAppliedHolding.getAvailableUnappliedAmount())) {
                        allocations.put(refDocNumber, remainingAmount);
                        remainingAmount = remainingAmount.subtract(remainingAmount);
                    } else {
                        allocations.put(refDocNumber, nonAppliedHolding.getAvailableUnappliedAmount());
                        remainingAmount = remainingAmount.subtract(nonAppliedHolding.getAvailableUnappliedAmount());
                    }
                }
            }
        }
        return allocations;
    }

    // this method is only used by Unapplied PayApp.
    // create nonApplied and nonInvoiced Distributions
    public void createDistributions() {

        // if there are non nonApplieds, then we have nothing to do
        if (nonAppliedHoldingsForCustomer == null || nonAppliedHoldingsForCustomer.isEmpty()) {
            return;
        }

        final Collection<InvoicePaidApplied> invoicePaidAppliedsForCurrentDoc = getInvoicePaidApplieds();
        final Collection<NonInvoiced> nonInvoicedsForCurrentDoc = getNonInvoiceds();

        for (final NonAppliedHolding nonAppliedHoldings : getNonAppliedHoldingsForCustomer()) {

            // check if payment has been applied to Invoices
            // create Unapplied Distribution for each PaidApplied
            KualiDecimal remainingUnappliedForDistribution = nonAppliedHoldings.getAvailableUnappliedAmount();
            for (final InvoicePaidApplied invoicePaidAppliedForCurrentDoc : invoicePaidAppliedsForCurrentDoc) {
                final KualiDecimal paidAppliedDistributionAmount = invoicePaidAppliedForCurrentDoc.getPaidAppiedDistributionAmount();
                final KualiDecimal remainingPaidAppliedForDistribution = invoicePaidAppliedForCurrentDoc.getInvoiceItemAppliedAmount().subtract(paidAppliedDistributionAmount);
                if (remainingPaidAppliedForDistribution.equals(KualiDecimal.ZERO) || remainingUnappliedForDistribution.equals(KualiDecimal.ZERO)) {
                    continue;
                }

                // set NonAppliedDistributions for the current document
                final NonAppliedDistribution nonAppliedDistribution = new NonAppliedDistribution();
                nonAppliedDistribution.setDocumentNumber(invoicePaidAppliedForCurrentDoc.getDocumentNumber());
                nonAppliedDistribution.setPaidAppliedItemNumber(invoicePaidAppliedForCurrentDoc.getPaidAppliedItemNumber());
                nonAppliedDistribution.setReferenceFinancialDocumentNumber(nonAppliedHoldings.getReferenceFinancialDocumentNumber());
                if (remainingPaidAppliedForDistribution.isLessEqual(remainingUnappliedForDistribution)) {
                    nonAppliedDistribution.setFinancialDocumentLineAmount(remainingPaidAppliedForDistribution);
                    remainingUnappliedForDistribution = remainingUnappliedForDistribution.subtract(remainingPaidAppliedForDistribution);
                    invoicePaidAppliedForCurrentDoc.setPaidAppiedDistributionAmount(paidAppliedDistributionAmount.add(remainingPaidAppliedForDistribution));
                } else {
                    nonAppliedDistribution.setFinancialDocumentLineAmount(remainingUnappliedForDistribution);
                    invoicePaidAppliedForCurrentDoc.setPaidAppiedDistributionAmount(paidAppliedDistributionAmount.add(remainingUnappliedForDistribution));
                    remainingUnappliedForDistribution = KualiDecimal.ZERO;
                }
                nonAppliedDistributions.add(nonAppliedDistribution);
            }

            // check if payment has been applied to NonAR
            // create NonAR distribution for each NonAR Applied row
            for (final NonInvoiced nonInvoicedForCurrentDoc : nonInvoicedsForCurrentDoc) {
                final KualiDecimal nonInvoicedDistributionAmount = nonInvoicedForCurrentDoc.getNonInvoicedDistributionAmount();
                final KualiDecimal remainingNonInvoicedForDistribution = nonInvoicedForCurrentDoc.getFinancialDocumentLineAmount().subtract(nonInvoicedDistributionAmount);
                if (remainingNonInvoicedForDistribution.equals(KualiDecimal.ZERO) || remainingUnappliedForDistribution.equals(KualiDecimal.ZERO)) {
                    continue;
                }

                // set NonAppliedDistributions for the current document
                final NonInvoicedDistribution nonInvoicedDistribution = new NonInvoicedDistribution();
                nonInvoicedDistribution.setDocumentNumber(nonInvoicedForCurrentDoc.getDocumentNumber());
                nonInvoicedDistribution.setFinancialDocumentLineNumber(nonInvoicedForCurrentDoc.getFinancialDocumentLineNumber());
                nonInvoicedDistribution.setReferenceFinancialDocumentNumber(nonAppliedHoldings.getReferenceFinancialDocumentNumber());
                if (remainingNonInvoicedForDistribution.isLessEqual(remainingUnappliedForDistribution)) {
                    nonInvoicedDistribution.setFinancialDocumentLineAmount(remainingNonInvoicedForDistribution);
                    remainingUnappliedForDistribution = remainingUnappliedForDistribution.subtract(remainingNonInvoicedForDistribution);
                    nonInvoicedForCurrentDoc.setNonInvoicedDistributionAmount(nonInvoicedDistributionAmount.add(remainingNonInvoicedForDistribution));
                } else {
                    nonInvoicedDistribution.setFinancialDocumentLineAmount(remainingUnappliedForDistribution);
                    nonInvoicedForCurrentDoc.setNonInvoicedDistributionAmount(nonInvoicedDistributionAmount.add(remainingUnappliedForDistribution));
                    remainingUnappliedForDistribution = KualiDecimal.ZERO;
                }
                nonInvoicedDistributions.add(nonInvoicedDistribution);
            }
        }
    }

    /**
     * @see org.kuali.kfs.sys.document.FinancialSystemTransactionalDocumentBase#answerSplitNodeQuestion(java.lang.String)
     */
    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (LAUNCHED_FROM_BATCH.equals(nodeName)) {
            return launchedFromBatch();
        }
        throw new UnsupportedOperationException("answerSplitNode('" + nodeName +
                "') was called but no handler for nodeName specified.");
    }

    // determines if the doc was launched by SYSTEM_USER, if so, then it was launched from batch
    protected boolean launchedFromBatch() {
        final Person person =
                KimApiServiceLocator.getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        return person.getPrincipalId().equalsIgnoreCase(getDocumentHeader().getWorkflowDocument()
                .getInitiatorPrincipalId());
    }

    private void clearCashControlDetail() {
        final CashControlDetail cashControlDetail = getCashControlDetail();
        if (ObjectUtils.isNotNull(cashControlDetail)) {
            cashControlDetail.setFinancialDocumentLineAmount(KualiDecimal.ZERO);
            getBusinessObjectService().save(cashControlDetail);
        }
    }

    /**
     * This method is defined to assist in the custom search implementation.
     *
     * @return
     */
    public String getUnappliedCustomerNumber() {
        if (getNonAppliedHolding() == null) {
            return "";
        }
        return getNonAppliedHolding().getCustomerNumber();
    }

    /**
     * This method is defined to assist in the custom search implementation.
     *
     * @return
     */
    public String getUnappliedCustomerName() {
        if (getNonAppliedHolding() == null) {
            return "";
        }
        return getNonAppliedHolding().getCustomer().getCustomerName();
    }

    /**
     * This method is defined to assist in the custom search implementation.
     *
     * @return
     */
    public String getInvoiceAppliedCustomerNumber() {
        return getAccountsReceivableDocumentHeader().getCustomerNumber();
    }

    /**
     * This method is defined to assist in the custom search implementation.
     *
     * @return
     */
    public String getInvoiceAppliedCustomerName() {
        return getAccountsReceivableDocumentHeader().getCustomer().getCustomerName();
    }

    public String getInvoiceDocumentType() {
        return invoiceDocumentType;
    }

    public void setInvoiceDocumentType(final String invoiceDocumentType) {
        this.invoiceDocumentType = invoiceDocumentType;
    }

    public String getLetterOfCreditCreationType() {
        return letterOfCreditCreationType;
    }

    public void setLetterOfCreditCreationType(final String letterOfCreditCreationType) {
        this.letterOfCreditCreationType = letterOfCreditCreationType;
    }

    public String getProposalNumber() {
        return proposalNumber;
    }

    public void setProposalNumber(final String proposalNumber) {
        this.proposalNumber = proposalNumber;
    }

    public String getLetterOfCreditFundGroupCode() {
        return letterOfCreditFundGroupCode;
    }

    public void setLetterOfCreditFundGroupCode(final String letterOfCreditFundGroupCode) {
        this.letterOfCreditFundGroupCode = letterOfCreditFundGroupCode;
    }

    public String getLetterOfCreditFundCode() {
        return letterOfCreditFundCode;
    }

    public void setLetterOfCreditFundCode(final String letterOfCreditFundCode) {
        this.letterOfCreditFundCode = letterOfCreditFundCode;
    }

    @Override
    public List<NonAppliedHolding> getNonAppliedHoldings() {
        return nonAppliedHoldings;
    }

    public void setNonAppliedHoldings(final List<NonAppliedHolding> nonAppliedHoldings) {
        this.nonAppliedHoldings = nonAppliedHoldings;
    }

    public static AccountsReceivablePendingEntryService getAccountsReceivablePendingEntryService() {
        if (accountsReceivablePendingEntryService == null) {
            accountsReceivablePendingEntryService = SpringContext.getBean(AccountsReceivablePendingEntryService.class);
        }
        return accountsReceivablePendingEntryService;
    }
}
