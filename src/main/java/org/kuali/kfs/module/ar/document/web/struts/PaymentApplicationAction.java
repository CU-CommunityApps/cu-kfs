/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.businessobject.NonInvoiced;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.PaymentApplicationDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDetailService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.NonAppliedHoldingService;
import org.kuali.kfs.module.ar.document.service.PaymentApplicationDocumentService;
import org.kuali.kfs.module.ar.document.validation.impl.PaymentApplicationDocumentRuleUtil;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import org.kuali.kfs.module.ar.dataaccess.InvoicePaidAppliedDao;
import org.kuali.kfs.module.ar.document.web.struts.PaymentApplicationForm;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentApplicationAction extends FinancialSystemTransactionalDocumentActionBase {

    private static final Logger LOG = LogManager.getLogger(PaymentApplicationAction.class);

    protected BusinessObjectService businessObjectService;
    protected DocumentService documentService;
    protected PaymentApplicationDocumentService paymentApplicationDocumentService;
    protected CustomerInvoiceDocumentService customerInvoiceDocumentService;
    protected CustomerInvoiceDetailService customerInvoiceDetailService;
    protected NonAppliedHoldingService nonAppliedHoldingService;
    protected InvoicePaidAppliedDao invoicePaidAppliedDao;

    public PaymentApplicationAction() {
        super();
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        documentService = SpringContext.getBean(DocumentService.class);
        paymentApplicationDocumentService = SpringContext.getBean(PaymentApplicationDocumentService.class);
        customerInvoiceDocumentService = SpringContext.getBean(CustomerInvoiceDocumentService.class);
        customerInvoiceDetailService = SpringContext.getBean(CustomerInvoiceDetailService.class);
        nonAppliedHoldingService = SpringContext.getBean(NonAppliedHoldingService.class);
        invoicePaidAppliedDao = SpringContext.getBean(InvoicePaidAppliedDao.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, ServletRequest request, ServletResponse response) throws Exception {
        PaymentApplicationForm payAppForm = (PaymentApplicationForm) form;
        if (!payAppForm.getPaymentApplicationDocument().isFinal()) {
            doApplicationOfFunds(payAppForm);
        }
        return super.execute(mapping, form, request, response);
    }

    /**
     * This is overridden in order to recalculate the invoice totals before doing the submit.
     *
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#route(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        doApplicationOfFunds((PaymentApplicationForm) form);
        manuallyAddressInvoicePaidAppliedDeletions((PaymentApplicationForm) form);
        return super.route(mapping, form, request, response);
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        doApplicationOfFunds((PaymentApplicationForm) form);
        manuallyAddressInvoicePaidAppliedDeletions((PaymentApplicationForm) form);
        return super.save(mapping, form, request, response);
    }

    public ActionForward deleteNonArLine(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm paymentApplicationForm = (PaymentApplicationForm) form;
        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();

        int deleteIndex = getLineToDelete(request);
        paymentApplicationDocument.getNonInvoiceds().remove(deleteIndex);

        Integer nonInvoicedItemNumber = 1;
        for (NonInvoiced n : paymentApplicationDocument.getNonInvoiceds()) {
            n.setFinancialDocumentLineNumber(nonInvoicedItemNumber++);
            n.refreshReferenceObject("chartOfAccounts");
            n.refreshReferenceObject("account");
            n.refreshReferenceObject("subAccount");
            n.refreshReferenceObject("financialObject");
            n.refreshReferenceObject("financialSubObject");
            n.refreshReferenceObject("project");
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Create an InvoicePaidApplied for a CustomerInvoiceDetail and validate it. If the validation succeeds the paidApplied is
     * returned. If the validation does succeed a null is returned.
     *
     * @param detailApplication
     * @param fieldName
     * @param document
     * @return
     */
    protected InvoicePaidApplied generateAndValidateNewPaidApplied(PaymentApplicationInvoiceDetailApply detailApplication, String fieldName, PaymentApplicationDocument document) {
        // generate the paidApplied
        InvoicePaidApplied paidApplied = detailApplication.generatePaidApplied();

        // validate the paidApplied, but ignore any failures (other than the error message)
        LOG.debug("Validating the generated paidApplied " + paidApplied.getDocumentNumber());
        PaymentApplicationDocumentRuleUtil.validateInvoicePaidApplied(paidApplied, fieldName, document);

        // return the generated paidApplied
        return paidApplied;
    }

    public ActionForward applyAllAmounts(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        doApplicationOfFunds((PaymentApplicationForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected void doApplicationOfFunds(PaymentApplicationForm paymentApplicationForm) throws WorkflowException {
        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();

        List<InvoicePaidApplied> invoicePaidApplieds = new ArrayList<InvoicePaidApplied>();

        // apply invoice detail entries
        invoicePaidApplieds.addAll(applyToIndividualCustomerInvoiceDetails(paymentApplicationForm));

        // quick-apply invoices
        invoicePaidApplieds.addAll(quickApplyToInvoices(paymentApplicationForm, invoicePaidApplieds));

        // re-number the paidApplieds internal sequence numbers
        int paidAppliedItemNumber = 1;
        for (InvoicePaidApplied i : invoicePaidApplieds) {
            i.setPaidAppliedItemNumber(paidAppliedItemNumber++);
        }

        // apply non-Invoiced
        NonInvoiced nonInvoiced = applyNonInvoiced(paymentApplicationForm);

        // apply non-applied holdings
        NonAppliedHolding nonAppliedHolding = applyUnapplied(paymentApplicationForm);

        // sum up the paid applieds
        KualiDecimal sumOfInvoicePaidApplieds = KualiDecimal.ZERO;
        for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
            KualiDecimal amount = invoicePaidApplied.getInvoiceItemAppliedAmount();
            if (null == amount) {
                amount = KualiDecimal.ZERO;
            }
            sumOfInvoicePaidApplieds = sumOfInvoicePaidApplieds.add(amount);
        }

        // sum up all applieds
        KualiDecimal appliedAmount = KualiDecimal.ZERO;
        appliedAmount = appliedAmount.add(sumOfInvoicePaidApplieds);
        if (null != nonInvoiced && null != nonInvoiced.getFinancialDocumentLineAmount()) {
            appliedAmount = appliedAmount.add(nonInvoiced.getFinancialDocumentLineAmount());
        }
        appliedAmount = appliedAmount.add(paymentApplicationDocument.getSumOfNonAppliedDistributions());
        appliedAmount = appliedAmount.add(paymentApplicationDocument.getSumOfNonInvoicedDistributions());
        appliedAmount = appliedAmount.add(paymentApplicationDocument.getSumOfNonInvoiceds());
        if (null != paymentApplicationDocument.getNonAppliedHoldingAmount()) {
            appliedAmount = appliedAmount.add(paymentApplicationDocument.getNonAppliedHoldingAmount());
        }

        // check that we havent applied more than our control total
        KualiDecimal controlTotalAmount = paymentApplicationForm.getTotalFromControl();

        // if the person over-applies, we dont stop them, we just complain
        if (appliedAmount.isGreaterThan(controlTotalAmount)) {
            addGlobalError(ArKeyConstants.PaymentApplicationDocumentErrors.CANNOT_APPLY_MORE_THAN_CASH_CONTROL_TOTAL_AMOUNT);
        }

        // swap out the old paidApplieds with the newly generated
        paymentApplicationDocument.getInvoicePaidApplieds().clear();
        paymentApplicationDocument.getInvoicePaidApplieds().addAll(invoicePaidApplieds);

        // NonInvoiced list management
        if (null != nonInvoiced) {
            paymentApplicationDocument.getNonInvoiceds().add(nonInvoiced);

            // re-number the non-invoiced
            Integer nonInvoicedItemNumber = 1;
            for (NonInvoiced n : paymentApplicationDocument.getNonInvoiceds()) {
                n.setFinancialDocumentLineNumber(nonInvoicedItemNumber++);
                n.refreshReferenceObject("chartOfAccounts");
                n.refreshReferenceObject("account");
                n.refreshReferenceObject("subAccount");
                n.refreshReferenceObject("financialObject");
                n.refreshReferenceObject("financialSubObject");
                n.refreshReferenceObject("project");
            }

            // make an empty new one
            paymentApplicationForm.setNonInvoicedAddLine(new NonInvoiced());
        }

        // reset the allocations, so it gets re-calculated
        paymentApplicationForm.setNonAppliedControlAllocations(null);

        // Update the doc total if it is not a CashControl generated PayApp
        if (!paymentApplicationDocument.hasCashControlDetail()) {
            paymentApplicationDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(appliedAmount);
        }
    }

    protected List<InvoicePaidApplied> applyToIndividualCustomerInvoiceDetails(PaymentApplicationForm paymentApplicationForm) {
        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();
        String applicationDocNbr = paymentApplicationDocument.getDocumentNumber();

        // Handle amounts applied at the invoice detail level
        int paidAppliedsGenerated = 1;
        int simpleInvoiceDetailApplicationCounter = 0;

        // calculate paid applieds for all invoices
        List<InvoicePaidApplied> invoicePaidApplieds = this.filterTempInvoicePaidApplieds(paymentApplicationForm);
        for (PaymentApplicationInvoiceApply invoiceApplication : paymentApplicationForm.getInvoiceApplications()) {
            for (PaymentApplicationInvoiceDetailApply detailApplication : invoiceApplication.getDetailApplications()) {

                // selectedInvoiceDetailApplications[${ctr}].amountApplied
                String fieldName = "selectedInvoiceDetailApplications[" + Integer.toString(simpleInvoiceDetailApplicationCounter) + "].amountApplied";
                simpleInvoiceDetailApplicationCounter++; // needs to be incremented even if we skip this line

                // handle the user clicking full apply
                if (detailApplication.isFullApply()) {
                    detailApplication.setAmountApplied(detailApplication.getAmountOpen());
                }
                // handle the user manually entering an amount
                else {
                    if (detailApplication.isFullApplyChanged()) { // means it went from true to false
                        detailApplication.setAmountApplied(KualiDecimal.ZERO);
                    }
                }

                // Don't add lines where the amount to apply is zero. Wouldn't make any sense to do that.
                if (KualiDecimal.ZERO.equals(detailApplication.getAmountApplied())) {
                    continue;
                }

                if (containsIdentical(detailApplication.getInvoiceDetail(), detailApplication.getAmountApplied(), invoicePaidApplieds)) {
                    continue;
                }

                // generate and validate the paidApplied, and always add it to the list, even if
                // it fails validation. Validation failures will stop routing.
                LOG.debug("Generating paid applied for detail application " + detailApplication.getInvoiceDocumentNumber());
                InvoicePaidApplied invoicePaidApplied = generateAndValidateNewPaidApplied(detailApplication, fieldName, paymentApplicationDocument);
                GlobalVariables.getMessageMap().addToErrorPath(KFSConstants.PaymentApplicationTabErrorCodes.APPLY_TO_INVOICE_DETAIL_TAB);
                GlobalVariables.getMessageMap().removeFromErrorPath(KFSConstants.PaymentApplicationTabErrorCodes.APPLY_TO_INVOICE_DETAIL_TAB);
                invoicePaidApplieds.add(invoicePaidApplied);
                paidAppliedsGenerated++;
            }
        }

        return invoicePaidApplieds;
    }

    protected List<InvoicePaidApplied> filterTempInvoicePaidApplieds(PaymentApplicationForm paymentApplicationForm) {
        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();
        List<InvoicePaidApplied> filteredInvoicePaidApplieds = new ArrayList<InvoicePaidApplied>();

        List<InvoicePaidApplied> invoicePaidApplieds = paymentApplicationDocument.getInvoicePaidApplieds(); // jira fix
        // add only entries that do not have the loaded customer number
        String currentCustomerNumber = findCustomerNumber(paymentApplicationForm);
        for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
            filteredInvoicePaidApplieds.add(invoicePaidApplied);
        }
        return filteredInvoicePaidApplieds;
    }

    /**
     * figure out the current customer Number on the form
     *
     * @param paymentApplicationForm
     * @return
     */
    protected String findCustomerNumber(PaymentApplicationForm paymentApplicationForm) {
        boolean validInvoice = this.isValidInvoice(paymentApplicationForm);
        String customerNumber = paymentApplicationForm.getSelectedCustomerNumber();
        String currentInvoiceNumber = paymentApplicationForm.getEnteredInvoiceDocumentNumber();
        // Invoice number entered, but no customer number entered
        if (StringUtils.isBlank(customerNumber) && StringUtils.isNotBlank(currentInvoiceNumber) && validInvoice) {
            Customer customer = customerInvoiceDocumentService.getCustomerByInvoiceDocumentNumber(currentInvoiceNumber);
            customerNumber = customer.getCustomerNumber();
        }
        return customerNumber;
    }

    /**
     * checks if the invoice is valid
     *
     * @param paymentApplicationForm
     * @return
     */
    protected boolean isValidInvoice(PaymentApplicationForm paymentApplicationForm) {
        boolean validInvoice = true;
        if (StringUtils.isNotBlank(paymentApplicationForm.getEnteredInvoiceDocumentNumber())) {
            Map<String, String> pkMap = new HashMap<String, String>();
            if (!SpringContext.getBean(CustomerInvoiceDocumentService.class).checkIfInvoiceNumberIsFinal(paymentApplicationForm.getEnteredInvoiceDocumentNumber())) {
                validInvoice &= false;
            }
        }
        return validInvoice;
    }

    /*
     *  test if this is already been applied
     *     InvoicePaidApplied paidApplied = new InvoicePaidApplied(payAppDocNumber, invoiceDetail.getDocumentNumber(),
                invoiceDetail.getSequenceNumber(), amountApplied, DEFAULT_PAID_APPLIED_ITEM_NUMBER);

                String documentNumber, String refInvoiceDocNumber, Integer invoiceSequenceNumber, KualiDecimal appliedAmount, Integer paidAppliedItemNumber) {
     */
    protected boolean containsIdentical(CustomerInvoiceDetail customerInvoiceDetail, KualiDecimal amountApplied, List<InvoicePaidApplied> invoicePaidApplieds) {
        boolean identicalFlag = false;
        String custRefInvoiceDocNumber = customerInvoiceDetail.getDocumentNumber();
        Integer custInvoiceSequenceNumber = customerInvoiceDetail.getInvoiceItemNumber();

        for (InvoicePaidApplied invoicePaidApplied : invoicePaidApplieds) {
            String refInvoiceDocNumber = invoicePaidApplied.getFinancialDocumentReferenceInvoiceNumber();
            Integer invoiceSequenceNumber = invoicePaidApplied.getInvoiceItemNumber();
            KualiDecimal appliedAmount = invoicePaidApplied.getInvoiceItemAppliedAmount();
            if (custRefInvoiceDocNumber.equals(refInvoiceDocNumber) && custInvoiceSequenceNumber.equals(invoiceSequenceNumber) && amountApplied.equals(appliedAmount)) {
                identicalFlag = true;
                break;
            }
        }
        return identicalFlag;
    }

    protected List<InvoicePaidApplied> quickApplyToInvoices(PaymentApplicationForm paymentApplicationForm, List<InvoicePaidApplied> appliedToIndividualDetails) {
        PaymentApplicationDocument applicationDocument = (PaymentApplicationDocument) paymentApplicationForm.getDocument();
        List<InvoicePaidApplied> invoicePaidApplieds = new ArrayList<InvoicePaidApplied>();

        // go over the selected invoices and apply full amount to each of their details
        for (PaymentApplicationInvoiceApply invoiceApplication : paymentApplicationForm.getInvoiceApplications()) {
            String invoiceDocNumber = invoiceApplication.getDocumentNumber();

            // skip the line if its not set to quick apply
            if (!invoiceApplication.isQuickApply()) {

                // if it was just flipped from True to False
                if (invoiceApplication.isQuickApplyChanged()) {
                    for (PaymentApplicationInvoiceDetailApply detailApplication : invoiceApplication.getDetailApplications()) {

                        // zero out all the details
                        detailApplication.setAmountApplied(KualiDecimal.ZERO);
                        detailApplication.setFullApply(false);

                        // remove any existing paidApplieds for this invoice
                        for (int i = appliedToIndividualDetails.size() - 1; i >= 0; i--) {
                            InvoicePaidApplied applied = appliedToIndividualDetails.get(i);
                            if (applied.getFinancialDocumentReferenceInvoiceNumber().equals(invoiceApplication.getDocumentNumber())) {
                                appliedToIndividualDetails.remove(i);
                            }
                        }
                    }
                }
                continue;
            }

            // make sure none of the invoices selected have zero open amounts, complain if so
            if (invoiceApplication.getOpenAmount().isZero()) {
                addGlobalError(ArKeyConstants.PaymentApplicationDocumentErrors.CANNOT_QUICK_APPLY_ON_INVOICE_WITH_ZERO_OPEN_AMOUNT);
                return invoicePaidApplieds;
            }

            // remove any existing paidApplieds for this invoice
            for (int i = appliedToIndividualDetails.size() - 1; i >= 0; i--) {
                InvoicePaidApplied applied = appliedToIndividualDetails.get(i);
                if (applied.getFinancialDocumentReferenceInvoiceNumber().equals(invoiceApplication.getDocumentNumber())) {
                    appliedToIndividualDetails.remove(i);
                }
            }

            // create and validate the paid applieds for each invoice detail
            String fieldName = "invoiceApplications[" + invoiceDocNumber + "].quickApply";
            for (PaymentApplicationInvoiceDetailApply detailApplication : invoiceApplication.getDetailApplications()) {
                detailApplication.setAmountApplied(detailApplication.getAmountOpen());
                detailApplication.setFullApply(true);
                InvoicePaidApplied paidApplied = generateAndValidateNewPaidApplied(detailApplication, fieldName, applicationDocument);
                if (paidApplied != null) {
                    invoicePaidApplieds.add(paidApplied);
                }
            }

            // maintain the selected doc number
            if (invoiceDocNumber.equals(paymentApplicationForm.getEnteredInvoiceDocumentNumber())) {
                paymentApplicationForm.setSelectedInvoiceDocumentNumber(invoiceDocNumber);
            }
        }

        return invoicePaidApplieds;
    }

    protected NonInvoiced applyNonInvoiced(PaymentApplicationForm payAppForm) throws WorkflowException {
        PaymentApplicationDocument applicationDocument = (PaymentApplicationDocument) payAppForm.getDocument();

        NonInvoiced nonInvoiced = payAppForm.getNonInvoicedAddLine();

        // if the line or line amount is null or zero, don't add the line. Additional validation is performed for the amount within
        // the rules
        // class, so no validation is needed here.
        //
        // NOTE: This conditional is in place because the "apply" button on the payment application document functions as a
        // universal button,
        // and therefore checks each tab where the button resides on the interface and attempts to apply values for that tab. This
        // functionality
        // causes this method to be called, regardless of if any values were entered in the "Non-AR" tab of the document. We want to
        // ignore this
        // method being called if there are no values entered in the fields.
        //
        // For the sake of this algorithm, a "Non-AR" accounting line will be ignored if it is null, or if the dollar amount entered
        // is blank or zero.
        if (ObjectUtils.isNull(payAppForm.getNonInvoicedAddLine()) || nonInvoiced.getFinancialDocumentLineAmount() == null || nonInvoiced.getFinancialDocumentLineAmount().isZero()) {
            return null;
        }

        // If we got past the above conditional, wire it up for adding
        nonInvoiced.setFinancialDocumentPostingYear(applicationDocument.getPostingYear());
        nonInvoiced.setDocumentNumber(applicationDocument.getDocumentNumber());
        nonInvoiced.setFinancialDocumentLineNumber(payAppForm.getNextNonInvoicedLineNumber());
        if (StringUtils.isNotBlank(nonInvoiced.getChartOfAccountsCode())) {
            nonInvoiced.setChartOfAccountsCode(nonInvoiced.getChartOfAccountsCode().toUpperCase());
        }

        // run the validations
        boolean isValid = PaymentApplicationDocumentRuleUtil.validateNonInvoiced(nonInvoiced, applicationDocument, payAppForm.getTotalFromControl());

        // check the validation results and return null if there were any errors
        if (!isValid) {
            return null;
        }

        return nonInvoiced;
    }

    protected NonAppliedHolding applyUnapplied(PaymentApplicationForm payAppForm) throws WorkflowException {
        PaymentApplicationDocument payAppDoc = payAppForm.getPaymentApplicationDocument();
        KualiDecimal amount = payAppForm.getNonAppliedHoldingAmount();

        // validate the customer number in the unapplied
        if (StringUtils.isNotBlank(payAppForm.getNonAppliedHoldingCustomerNumber())) {

            Map<String, String> pkMap = new HashMap<String, String>();
            pkMap.put(ArPropertyConstants.CustomerFields.CUSTOMER_NUMBER, payAppForm.getNonAppliedHoldingCustomerNumber().toUpperCase());
            int found = businessObjectService.countMatching(Customer.class, pkMap);
            if (found == 0) {
                addFieldError(KFSConstants.PaymentApplicationTabErrorCodes.UNAPPLIED_TAB, ArPropertyConstants.PaymentApplicationDocumentFields.UNAPPLIED_CUSTOMER_NUMBER, ArKeyConstants.PaymentApplicationDocumentErrors.ENTERED_INVOICE_CUSTOMER_NUMBER_INVALID);
                return null;
            }

            // force customer number to upper
            payAppForm.setNonAppliedHoldingCustomerNumber(payAppForm.getNonAppliedHoldingCustomerNumber().toUpperCase());

        }

        // validate the amount in the unapplied
        if (payAppForm.getNonAppliedHoldingAmount() != null && payAppForm.getNonAppliedHoldingAmount().isNegative()) {
            addFieldError(KFSConstants.PaymentApplicationTabErrorCodes.UNAPPLIED_TAB, ArPropertyConstants.PaymentApplicationDocumentFields.UNAPPLIED_AMOUNT, ArKeyConstants.PaymentApplicationDocumentErrors.UNAPPLIED_AMOUNT_CANNOT_BE_NEGATIVE);
            return null;
        }

        // if we dont have enough information to make an UnApplied, then do nothing
        if (StringUtils.isBlank(payAppForm.getNonAppliedHoldingCustomerNumber()) || amount == null || amount.isZero()) {
            payAppDoc.setNonAppliedHolding(null);
            return null;
        }

        // build a new NonAppliedHolding
        NonAppliedHolding nonAppliedHolding = new NonAppliedHolding();
        nonAppliedHolding.setCustomerNumber(payAppForm.getNonAppliedHoldingCustomerNumber().toUpperCase());
        nonAppliedHolding.setReferenceFinancialDocumentNumber(payAppDoc.getDocumentNumber());
        nonAppliedHolding.setFinancialDocumentLineAmount(amount);

        // set it to the document
        payAppDoc.setNonAppliedHolding(nonAppliedHolding);

        // validate it
        boolean isValid = PaymentApplicationDocumentRuleUtil.validateNonAppliedHolding(payAppDoc, payAppForm.getTotalFromControl());

        // check the validation results and return null if there were any errors
        if (!isValid) {
            return null;
        }

        return nonAppliedHolding;
    }

    /**
     * This method loads the invoices for currently selected customer
     *
     * @param payAppForm
     * @param selectedInvoiceNumber
     */
    protected void loadInvoices(PaymentApplicationForm payAppForm, String selectedInvoiceNumber) {
        PaymentApplicationDocument payAppDoc = payAppForm.getPaymentApplicationDocument();
        AccountsReceivableDocumentHeader arDocHeader = payAppDoc.getAccountsReceivableDocumentHeader();
        String currentInvoiceNumber = selectedInvoiceNumber;

        // before we do anything, validate the validity of any customerNumber or invoiceNumber
        // entered against the db, and complain to the user if either is not right.
        if (StringUtils.isNotBlank(payAppForm.getSelectedCustomerNumber())) {
            Map<String, String> pkMap = new HashMap<String, String>();
            pkMap.put(ArPropertyConstants.CustomerFields.CUSTOMER_NUMBER, payAppForm.getSelectedCustomerNumber());
            int found = businessObjectService.countMatching(Customer.class, pkMap);
            if (found == 0) {
                addFieldError(KFSConstants.PaymentApplicationTabErrorCodes.APPLY_TO_INVOICE_DETAIL_TAB, ArPropertyConstants.PaymentApplicationDocumentFields.ENTERED_INVOICE_CUSTOMER_NUMBER, ArKeyConstants.PaymentApplicationDocumentErrors.ENTERED_INVOICE_CUSTOMER_NUMBER_INVALID);
            }
        }
        boolean validInvoice = this.isValidInvoice(payAppForm);
        if (!validInvoice) {
            addFieldError(KFSConstants.PaymentApplicationTabErrorCodes.APPLY_TO_INVOICE_DETAIL_TAB, ArPropertyConstants.PaymentApplicationDocumentFields.ENTERED_INVOICE_NUMBER, ArKeyConstants.ERROR_CUSTOMER_INVOICE_DOCUMENT_NOT_FINAL);
        }

        // This handles the priority of the payapp selected customer number and the
        // ar doc header customer number. The ar doc header customer number should always
        // reflect what customer number is entered on the form for invoices. This code chunk
        // ensures that whatever the user enters always wins, but also tries to not load the form
        // with an empty customer number wherever possible.
        if (StringUtils.isBlank(payAppForm.getSelectedCustomerNumber())) {
            if (StringUtils.isBlank(arDocHeader.getCustomerNumber())) {
                if (payAppDoc.hasCashControlDetail()) {
                    payAppForm.setSelectedCustomerNumber(payAppDoc.getCashControlDetail().getCustomerNumber());
                    arDocHeader.setCustomerNumber(payAppDoc.getCashControlDetail().getCustomerNumber());
                }
            } else {
                payAppForm.setSelectedCustomerNumber(arDocHeader.getCustomerNumber());
            }
        } else {
            arDocHeader.setCustomerNumber(payAppForm.getSelectedCustomerNumber());
        }
        String customerNumber = payAppForm.getSelectedCustomerNumber();

        // Invoice number entered, but no customer number entered
        if (StringUtils.isBlank(customerNumber) && StringUtils.isNotBlank(currentInvoiceNumber) && validInvoice) {
            Customer customer = customerInvoiceDocumentService.getCustomerByInvoiceDocumentNumber(currentInvoiceNumber);
            customerNumber = customer.getCustomerNumber();
            payAppDoc.getAccountsReceivableDocumentHeader().setCustomerNumber(customerNumber);
        }

        // load up the control docs and non-applied holdings for non-cash-control payapps
        if (StringUtils.isNotBlank(customerNumber)) {
            if (!payAppDoc.hasCashControlDocument()) {
                List<PaymentApplicationDocument> nonAppliedControlDocs = new ArrayList<PaymentApplicationDocument>();
                List<NonAppliedHolding> nonAppliedControlHoldings = new ArrayList<NonAppliedHolding>();

                // if the doc is already final/approved, then we only pull the relevant control
                // documents and nonapplied holdings that this doc paid against.
                if (payAppDoc.isFinal()) {
                    nonAppliedControlDocs.addAll(payAppDoc.getPaymentApplicationDocumentsUsedAsControlDocuments());
                    nonAppliedControlHoldings.addAll(payAppDoc.getNonAppliedHoldingsUsedAsControls());
                }

                // otherwise, we pull all available non-zero non-applied holdings for
                // this customer, and make the associated docs and non-applied holdings available
                else {
                    // retrieve the set of available non-applied holdings for this customer
                    NonAppliedHoldingService nonAppliedHoldingService = SpringContext.getBean(NonAppliedHoldingService.class);
                    nonAppliedControlHoldings.addAll(nonAppliedHoldingService.getNonAppliedHoldingsForCustomer(customerNumber));

                    // get the parent list of payapp documents that they come from
                    List<String> controlDocNumbers = new ArrayList<String>();
                    for (NonAppliedHolding nonAppliedHolding : nonAppliedControlHoldings) {
                        if (nonAppliedHolding.getAvailableUnappliedAmount().isPositive()) {
                            if (!controlDocNumbers.contains(nonAppliedHolding.getReferenceFinancialDocumentNumber())) {
                                controlDocNumbers.add(nonAppliedHolding.getReferenceFinancialDocumentNumber());
                            }
                        }
                    }
                    // only try to retrieve docs if we have any to retrieve
                    if (!controlDocNumbers.isEmpty()) {
                        try {
                            List<Document> docs = documentService.getDocumentsByListOfDocumentHeaderIds(PaymentApplicationDocument.class, controlDocNumbers);
                            for (Document doc : docs) {
                                nonAppliedControlDocs.add((PaymentApplicationDocument) doc);
                            }
                        } catch (WorkflowException e) {
                            throw new RuntimeException("A runtimeException was thrown when trying to retrieve a list of documents.", e);
                        }
                    }
                }

                // set the form vars from what we've loaded up here
                payAppForm.setNonAppliedControlDocs(nonAppliedControlDocs);
                payAppForm.setNonAppliedControlHoldings(nonAppliedControlHoldings);
                payAppDoc.setNonAppliedHoldingsForCustomer(new ArrayList<NonAppliedHolding>(nonAppliedControlHoldings));
                payAppForm.setNonAppliedControlAllocations(null);
            }
        }

        // reload invoices for the selected customer number
        if (StringUtils.isNotBlank(customerNumber)) {
            Collection<CustomerInvoiceDocument> openInvoicesForCustomer;

            // we have to special case the invoices once the document is finished, because
            // at this point, we want to show the invoices it paid against, NOT the set of
            // open invoices
            if (payAppDoc.isFinal()) {
                openInvoicesForCustomer = payAppDoc.getInvoicesPaidAgainst();
            } else {
                openInvoicesForCustomer = customerInvoiceDocumentService.getOpenInvoiceDocumentsByCustomerNumber(customerNumber);
            }
            payAppForm.setInvoices(new ArrayList<CustomerInvoiceDocument>(openInvoicesForCustomer));
            payAppForm.setupInvoiceWrappers(payAppDoc.getDocumentNumber());
        }

        // if no invoice number entered than get the first invoice
        if (StringUtils.isNotBlank(customerNumber) && StringUtils.isBlank(currentInvoiceNumber)) {
            if (payAppForm.getInvoices() == null || payAppForm.getInvoices().isEmpty()) {
                currentInvoiceNumber = null;
            } else {
                currentInvoiceNumber = payAppForm.getInvoices().get(0).getDocumentNumber();
            }
        }

        // load information for the current selected invoice
        if (StringUtils.isNotBlank(currentInvoiceNumber)) {
            payAppForm.setSelectedInvoiceDocumentNumber(currentInvoiceNumber);
            payAppForm.setEnteredInvoiceDocumentNumber(currentInvoiceNumber);
        }

        // make sure all paidApplieds are synched with the PaymentApplicationInvoiceApply and
        // PaymentApplicationInvoiceDetailApply objects, so that the form reflects how it was left pre-save.
        // This is only necessary when the doc is saved, and then re-opened, as the invoice-detail wrappers
        // will no longer hold the state info. I know this is a monstrosity. Get over it.
        for (InvoicePaidApplied paidApplied : payAppDoc.getInvoicePaidApplieds()) {
            for (PaymentApplicationInvoiceApply invoiceApplication : payAppForm.getInvoiceApplications()) {
                if (paidApplied.getFinancialDocumentReferenceInvoiceNumber().equalsIgnoreCase(invoiceApplication.getDocumentNumber())) {
                    for (PaymentApplicationInvoiceDetailApply detailApplication : invoiceApplication.getDetailApplications()) {
                        if (paidApplied.getInvoiceItemNumber().equals(detailApplication.getSequenceNumber())) {

                            // if the amount applieds dont match, then have the paidApplied fill in the applied amounts
                            // for the invoiceApplication details
                            if (!paidApplied.getInvoiceItemAppliedAmount().equals(detailApplication.getAmountApplied())) {
                                detailApplication.setAmountApplied(paidApplied.getInvoiceItemAppliedAmount());
                                if (paidApplied.getInvoiceItemAppliedAmount().equals(detailApplication.getAmountOpen())) {
                                    detailApplication.setFullApply(true);
                                }
                            }
                        }
                    }
                }
            }
        }

        // clear any NonInvoiced add line information from the form vars
        payAppForm.setNonInvoicedAddLine(null);

        // load any NonAppliedHolding information into the form vars
        if (payAppDoc.getNonAppliedHolding() != null) {
            payAppForm.setNonAppliedHoldingCustomerNumber(payAppDoc.getNonAppliedHolding().getCustomerNumber());
            payAppForm.setNonAppliedHoldingAmount(payAppDoc.getNonAppliedHolding().getFinancialDocumentLineAmount());
        } else {
            // clear any NonAppliedHolding information from the form vars if it's empty
            payAppForm.setNonAppliedHoldingCustomerNumber(null);
            payAppForm.setNonAppliedHoldingAmount(null);
        }

        //Presort this list to not reload in the jsp - https://jira.kuali.org/browse/KFSCNTRB-1377
        payAppForm.setInvoiceApplications(sortInvoiceApplications(payAppForm.getInvoiceApplications()));
    }

    protected List<PaymentApplicationInvoiceApply> sortInvoiceApplications(List<PaymentApplicationInvoiceApply> invoiceApplications) {
        EntryHolderComparator entryHolderComparator = new EntryHolderComparator();
        List<EntryHolder> entryHoldings = new ArrayList<EntryHolder>();
        for (PaymentApplicationInvoiceApply paymentApplicationInvoiceApply : invoiceApplications) {
            entryHoldings.add(new EntryHolder(paymentApplicationInvoiceApply.getInvoice().getDocumentHeader().getWorkflowDocument().getDateCreated().toDate(), paymentApplicationInvoiceApply));
        }
        if (entryHoldings.size() > 0) {
            Collections.sort(entryHoldings, entryHolderComparator);
        }
        List<PaymentApplicationInvoiceApply> results = new ArrayList<PaymentApplicationInvoiceApply>();
        for (EntryHolder entryHolder : entryHoldings) {
            results.add((PaymentApplicationInvoiceApply) entryHolder.getHolder());
        }
        return results;
    }

    /**
     * An inner class to point to a specific entry in a group
     */
    protected class EntryHolder {
        private Date date;
        private Object holder;

        /**
         * Constructs a NonAppliedHolding.EntryHolder
         *
         * @param date   of doc
         * @param holder the entry to point to
         */
        public EntryHolder(Date date, Object holder) {
            this.date = date;
            this.holder = holder;
        }

        public Date getDate() {
            return this.date;
        }

        public Object getHolder() {
            return this.holder;
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
        public int compare(EntryHolder rosencrantz, EntryHolder guildenstern) {
            return rosencrantz.getDate().compareTo(guildenstern.getDate());
        }
    }

    /**
     * This method updates the customer invoice details when a new invoice is selected
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward goToInvoice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm payAppForm = (PaymentApplicationForm) form;
        loadInvoices(payAppForm, payAppForm.getSelectedInvoiceDocumentNumber());
        if (!payAppForm.getPaymentApplicationDocument().isFinal()) {
            doApplicationOfFunds(payAppForm);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method updates customer invoice details when next invoice is selected
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward goToNextInvoice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm payAppForm = (PaymentApplicationForm) form;
        loadInvoices(payAppForm, payAppForm.getNextInvoiceDocumentNumber());
        if (!payAppForm.getPaymentApplicationDocument().isFinal()) {
            doApplicationOfFunds(payAppForm);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method updates customer invoice details when previous invoice is selected
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward goToPreviousInvoice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm payAppForm = (PaymentApplicationForm) form;
        loadInvoices(payAppForm, payAppForm.getPreviousInvoiceDocumentNumber());
        if (!payAppForm.getPaymentApplicationDocument().isFinal()) {
            doApplicationOfFunds(payAppForm);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Retrieve all invoices for the selected customer.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward loadInvoices(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm pform = (PaymentApplicationForm) form;
        loadInvoices(pform, pform.getEnteredInvoiceDocumentNumber());
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Cancel the document.
     */
    @Override
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm newForm = (PaymentApplicationForm) form;
        if (null == newForm.getCashControlDocument()) {
            return super.cancel(mapping, form, request, response);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.createDocument(kualiDocumentFormBase);
        PaymentApplicationForm form = (PaymentApplicationForm) kualiDocumentFormBase;
        PaymentApplicationDocument document = form.getPaymentApplicationDocument();

        // create new accounts receivable header and set it to the payment application document
        AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService = SpringContext.getBean(AccountsReceivableDocumentHeaderService.class);
        AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = accountsReceivableDocumentHeaderService.getNewAccountsReceivableDocumentHeaderForCurrentUser();
        accountsReceivableDocumentHeader.setDocumentNumber(document.getDocumentNumber());
        document.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);
    }

    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.loadDocument(kualiDocumentFormBase);
        PaymentApplicationForm pform = (PaymentApplicationForm) kualiDocumentFormBase;
        loadInvoices(pform, pform.getEnteredInvoiceDocumentNumber());
    }

    /**
     * Get an error to display in the UI for a certain field.
     *
     * @param propertyName
     * @param errorKey
     */
    protected void addFieldError(String errorPathToAdd, String propertyName, String errorKey) {
        GlobalVariables.getMessageMap().addToErrorPath(errorPathToAdd);
        GlobalVariables.getMessageMap().putError(propertyName, errorKey);
        GlobalVariables.getMessageMap().removeFromErrorPath(errorPathToAdd);
    }

    /**
     * Get an error to display at the global level, for the whole document.
     *
     * @param errorKey
     */
    protected void addGlobalError(String errorKey) {
        GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KRADConstants.DOCUMENT_ERRORS, errorKey, "document.hiddenFieldForErrors");
    }

    public ActionForward deleteInvoicePaidApplied(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PaymentApplicationForm paymentApplicationForm = (PaymentApplicationForm) form;
        paymentApplicationForm.setManualInvoicePaidAppliedDatabaseDeletionRequired(true);

        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();
        int deleteIndex = getLineToDelete(request);

        adjustBothQuickApplyToInvoiceAndApplyToInvoiceDetailDueToDeleteInvoicePaidApplied(paymentApplicationForm, deleteIndex);
        paymentApplicationDocument.getInvoicePaidApplieds().remove(deleteIndex);
        GlobalVariables.getMessageMap().clearErrorMessages();
        doApplicationOfFunds((PaymentApplicationForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected void adjustBothQuickApplyToInvoiceAndApplyToInvoiceDetailDueToDeleteInvoicePaidApplied(PaymentApplicationForm paymentApplicationForm, int deletedAppliedFundsIndex) {
        PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();
        InvoicePaidApplied appliedFundBeingDeleted = paymentApplicationDocument.getInvoicePaidApplieds().get(deletedAppliedFundsIndex);

        String summaryOfAppliedFundsDocumentNumberBeingDeleted = appliedFundBeingDeleted.getFinancialDocumentReferenceInvoiceNumber();
        KualiDecimal summaryOfAppliedFundsAmountBeingDeleted = appliedFundBeingDeleted.getInvoiceItemAppliedAmount();

        if (StringUtils.isNotBlank(summaryOfAppliedFundsDocumentNumberBeingDeleted)) {
            for (PaymentApplicationInvoiceApply invoiceListItem : paymentApplicationForm.getInvoiceApplications()) {
                for (PaymentApplicationInvoiceDetailApply invoiceListItemDetail : invoiceListItem.getDetailApplications()) {

                    deleteCleanupOfApplyToInvoiceDetailData(invoiceListItemDetail, summaryOfAppliedFundsDocumentNumberBeingDeleted, summaryOfAppliedFundsAmountBeingDeleted);

                    deleteCleanupOfQuickApplyToInvoiceData(invoiceListItem, invoiceListItemDetail, summaryOfAppliedFundsDocumentNumberBeingDeleted);

                }
            }
        }
    }

    protected void deleteCleanupOfApplyToInvoiceDetailData(PaymentApplicationInvoiceDetailApply invoiceListItemDetail,
            String summaryOfAppliedFundsDocumentNumberBeingDeleted,
            KualiDecimal summaryOfAppliedFundsAmountBeingDeleted) {

        CustomerInvoiceDetail invoiceDetail = invoiceListItemDetail.getInvoiceDetail();
        String invoiceDetailDocumentNumber = invoiceDetail.getDocumentNumber();

        if (StringUtils.isNotBlank(invoiceDetailDocumentNumber)
             && ObjectUtils.isNotNull(summaryOfAppliedFundsAmountBeingDeleted)
             && StringUtils.equalsIgnoreCase(summaryOfAppliedFundsDocumentNumberBeingDeleted, invoiceDetailDocumentNumber)) {

            if (invoiceListItemDetail.getAmountApplied().equals(invoiceListItemDetail.getAmount())) {
                invoiceListItemDetail.setFullApply(false);
            }

            if (invoiceListItemDetail.getAmountApplied().equals(summaryOfAppliedFundsAmountBeingDeleted)) {
                invoiceListItemDetail.setAmountApplied(KualiDecimal.ZERO);
            }
        }
    }

    protected void deleteCleanupOfQuickApplyToInvoiceData(PaymentApplicationInvoiceApply invoiceListItem, PaymentApplicationInvoiceDetailApply invoiceListItemDetail, String summaryOfAppliedFundsDocumentNumberBeingDeleted) {
        if (doesQuickApplySectionInvoiceDocumentNumberMatchSummaryOfAppliedFundsDocumentNumberBeingDeleted(summaryOfAppliedFundsDocumentNumberBeingDeleted, invoiceListItemDetail.getInvoiceDocumentNumber())
            && isQuickApplyGreyModed(invoiceListItem, invoiceListItemDetail)) {
                invoiceListItemDetail.setAmountApplied(KualiDecimal.ZERO);
                invoiceListItemDetail.setInvoiceQuickApplied(false);
                invoiceListItemDetail.setFullApply(false);
                invoiceListItem.setQuickApply(false);
            }
    }

    protected boolean doesQuickApplySectionInvoiceDocumentNumberMatchSummaryOfAppliedFundsDocumentNumberBeingDeleted(String summaryOfAppliedFundsDocumentNumberBeingDeleted, String quickApplySectionInvoiceDocumentNumber) {
        return (StringUtils.equalsIgnoreCase(summaryOfAppliedFundsDocumentNumberBeingDeleted, quickApplySectionInvoiceDocumentNumber));
    }

    protected boolean isQuickApplyGreyModed(PaymentApplicationInvoiceApply invoiceListItem, PaymentApplicationInvoiceDetailApply invoiceListItemDetail) {
        return (invoiceListItem.isQuickApply() || invoiceListItemDetail.isFullApply());
    }

    protected void manuallyAddressInvoicePaidAppliedDeletions(PaymentApplicationForm paymentApplicationForm) {
        if (paymentApplicationForm.isManualInvoicePaidAppliedDatabaseDeletionRequired()) {
            PaymentApplicationDocument paymentApplicationDocument = paymentApplicationForm.getPaymentApplicationDocument();
            invoicePaidAppliedDao.deleteAllInvoicePaidApplieds(paymentApplicationDocument.getDocumentNumber());
            paymentApplicationForm.setManualInvoicePaidAppliedDatabaseDeletionRequired(false);
        }
    }

}
