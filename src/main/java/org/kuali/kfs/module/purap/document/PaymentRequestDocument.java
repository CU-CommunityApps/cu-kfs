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
package org.kuali.kfs.module.purap.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.framework.postprocessor.ActionTakenEvent;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurapDocTypeCodes;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.RecurringPaymentType;
import org.kuali.kfs.module.purap.document.service.AccountsPayableDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.AccountsPayableService;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedContinuePurapEvent;
import org.kuali.kfs.module.purap.service.PdpExtractService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.module.purap.util.ExpiredOrClosedAccountEntry;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.businessobject.PaymentSourceWireTransfer;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.PaymentSource;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.PurchaseOrderCostSource;
import org.kuali.kfs.vnd.businessobject.ShippingPaymentTerms;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Payment Request Document Business Object. Contains the fields associated with the main document table.
 */
public class PaymentRequestDocument extends AccountsPayableDocumentBase implements PaymentSource {

    private static final Logger LOG = LogManager.getLogger();

    protected Date invoiceDate;
    protected String invoiceNumber;
    protected KualiDecimal vendorInvoiceAmount;
    protected String vendorPaymentTermsCode;
    protected String vendorShippingPaymentTermsCode;
    protected Date paymentRequestPayDate;
    protected String paymentRequestCostSourceCode;
    protected boolean paymentRequestedCancelIndicator;
    protected boolean paymentAttachmentIndicator;
    protected boolean immediatePaymentIndicator;
    protected String specialHandlingInstructionLine1Text;
    protected String specialHandlingInstructionLine2Text;
    protected String specialHandlingInstructionLine3Text;
    protected Timestamp paymentPaidTimestamp;
    protected boolean paymentRequestElectronicInvoiceIndicator;
    protected String accountsPayableRequestCancelIdentifier;
    protected Integer originalVendorHeaderGeneratedIdentifier;
    protected Integer originalVendorDetailAssignedIdentifier;
    protected Integer alternateVendorHeaderGeneratedIdentifier;
    protected Integer alternateVendorDetailAssignedIdentifier;
    protected String purchaseOrderNotes;
    protected String recurringPaymentTypeCode;
    protected boolean receivingDocumentRequiredIndicator;
    protected boolean paymentRequestPositiveApprovalIndicator;
    private boolean achSignUpStatusFlag;

    //KFSCNTRB-1207 - UMD - Muddu -- start
    //the indicator which tells if the preq has been auto approved and this value will be used
    //by the doRouteStatus method to change the app doc status.
    protected boolean autoApprovedIndicator;
    //KFSCNTRB-1207 - UMD - Muddu -- end

    // TAX EDIT AREA FIELDS
    protected String taxClassificationCode;
    protected String taxCountryCode;
    protected String taxNQIId;
    // number is in whole form so 5% is 5.00
    protected BigDecimal taxFederalPercent;
    // number is in whole form so 5% is 5.00
    protected BigDecimal taxStatePercent;
    protected KualiDecimal taxSpecialW4Amount;
    protected Boolean taxGrossUpIndicator;
    protected Boolean taxExemptTreatyIndicator;
    protected Boolean taxForeignSourceIndicator;
    protected Boolean taxUSAIDPerDiemIndicator;
    protected Boolean taxOtherExemptIndicator;

    protected String justification;

    private String paymentMethodCode;

    // NOT PERSISTED IN DB
    protected String vendorShippingTitleCode;
    protected Date purchaseOrderEndDate;
    protected String primaryVendorName;

    // BELOW USED BY GL ENTRY CREATION
    private boolean generateExternalEntries;
    private boolean generateWireTransferEntries;

    // BELOW USED BY ROUTING
    protected Integer requisitionIdentifier;

    private transient PaymentSourceHelperService paymentSourceHelperService;
    private transient PdpExtractService pdpExtractService;
    private transient PurapGeneralLedgerService purapGeneralLedgerService;

    // REFERENCE OBJECTS
    protected PaymentTermType vendorPaymentTerms;
    protected ShippingPaymentTerms vendorShippingPaymentTerms;
    protected PurchaseOrderCostSource paymentRequestCostSource;
    protected RecurringPaymentType recurringPaymentType;

    private PaymentMethod paymentMethod;

    private PaymentSourceWireTransfer wireTransfer;

    public PaymentRequestDocument() {
        setAutoApprovedIndicator(false);

        wireTransfer = new PaymentSourceWireTransfer();
    }

    public boolean isBoNotesSupport() {
        return true;
    }

    public Integer getPostingYearPriorOrCurrent() {
        if (SpringContext.getBean(PaymentRequestService.class).allowBackpost(this)) {
            // allow prior; use it
            return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear() - 1;
        }
        // don't allow prior; use CURRENT
        return SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
    }

    /**
     * Overrides the method in PurchasingAccountsPayableDocumentBase to add the criteria specific to Payment Request
     * Document.
     */
    @Override
    public boolean isInquiryRendered() {
        return !isPostingYearPrior()
               || !getApplicationDocumentStatus().equals(PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED)
                 && !getApplicationDocumentStatus().equals(PaymentRequestStatuses.APPDOC_AUTO_APPROVED)
                 && !getApplicationDocumentStatus().equals(PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE)
                 && !getApplicationDocumentStatus().equals(PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS);
    }

    @Override
    public void toCopy() {
        super.toCopy();

        wireTransfer.setWireTransferFeeWaiverIndicator(false);
    }

    public Integer getRequisitionIdentifier() {
        return getPurchaseOrderDocument().getRequisitionIdentifier();
    }

    public void setRequisitionIdentifier(final Integer requisitionIdentifier) {
        this.requisitionIdentifier = requisitionIdentifier;
    }

    @Override
    public void populateDocumentForRouting() {
        setRequisitionIdentifier(getPurchaseOrderDocument().getRequisitionIdentifier());
        super.populateDocumentForRouting();
    }

    /**
     * Decides whether receivingDocumentRequiredIndicator functionality shall be enabled according to the controlling
     * parameter.
     */
    public boolean isEnableReceivingDocumentRequiredIndicator() {
        return SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                KfsParameterConstants.PURCHASING_DOCUMENT.class,
                PurapParameterConstants.RECEIVING_REQUIRED_IND
        );
    }

    /**
     * Decides whether paymentRequestPositiveApprovalIndicator functionality shall be enabled according to the
     * controlling parameter.
     */
    public boolean isEnablePaymentRequestPositiveApprovalIndicator() {
        return SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(
                KfsParameterConstants.PURCHASING_DOCUMENT.class,
                PurapParameterConstants.POSITIVE_APPROVAL_IND);
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(final Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        if (StringUtils.isNotEmpty(invoiceNumber)) {
            this.invoiceNumber = invoiceNumber.toUpperCase(Locale.US);
        } else {
            this.invoiceNumber = invoiceNumber;
        }
    }

    public KualiDecimal getVendorInvoiceAmount() {
        return vendorInvoiceAmount;
    }

    public void setVendorInvoiceAmount(final KualiDecimal vendorInvoiceAmount) {
        this.vendorInvoiceAmount = vendorInvoiceAmount;
    }

    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    public void setVendorPaymentTermsCode(final String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
        refreshReferenceObject("vendorPaymentTerms");
    }

    public PaymentTermType getVendorPaymentTerms() {
        if (ObjectUtils.isNull(vendorPaymentTerms)
                || !StringUtils.equalsIgnoreCase(getVendorPaymentTermsCode(),
                    vendorPaymentTerms.getVendorPaymentTermsCode())) {
            refreshReferenceObject(VendorPropertyConstants.VENDOR_PAYMENT_TERMS);
        }
        return vendorPaymentTerms;
    }

    public void setVendorPaymentTerms(final PaymentTermType vendorPaymentTerms) {
        this.vendorPaymentTerms = vendorPaymentTerms;
    }

    public String getVendorShippingPaymentTermsCode() {
        if (ObjectUtils.isNull(vendorPaymentTerms)) {
            refreshReferenceObject(VendorPropertyConstants.VENDOR_SHIPPING_PAYMENT_TERMS);
        }
        return vendorShippingPaymentTermsCode;
    }

    public void setVendorShippingPaymentTermsCode(final String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    public Date getPaymentRequestPayDate() {
        return paymentRequestPayDate;
    }

    public void setPaymentRequestPayDate(final Date paymentRequestPayDate) {
        this.paymentRequestPayDate = paymentRequestPayDate;
    }

    public String getPaymentRequestCostSourceCode() {
        return paymentRequestCostSourceCode;
    }

    public void setPaymentRequestCostSourceCode(final String paymentRequestCostSourceCode) {
        this.paymentRequestCostSourceCode = paymentRequestCostSourceCode;
    }

    public boolean getPaymentRequestedCancelIndicator() {
        return paymentRequestedCancelIndicator;
    }

    public boolean isPaymentRequestedCancelIndicator() {
        return paymentRequestedCancelIndicator;
    }

    public void setPaymentRequestedCancelIndicator(final boolean paymentRequestedCancelIndicator) {
        this.paymentRequestedCancelIndicator = paymentRequestedCancelIndicator;
    }

    public boolean getPaymentAttachmentIndicator() {
        return paymentAttachmentIndicator;
    }

    public void setPaymentAttachmentIndicator(final boolean paymentAttachmentIndicator) {
        this.paymentAttachmentIndicator = paymentAttachmentIndicator;
    }

    @Override
    public boolean hasAttachment() {
        return paymentAttachmentIndicator;
    }

    public boolean getImmediatePaymentIndicator() {
        return immediatePaymentIndicator;
    }

    public void setImmediatePaymentIndicator(final boolean immediatePaymentIndicator) {
        this.immediatePaymentIndicator = immediatePaymentIndicator;
    }

    public String getSpecialHandlingInstructionLine1Text() {
        return specialHandlingInstructionLine1Text;
    }

    public void setSpecialHandlingInstructionLine1Text(final String specialHandlingInstructionLine1Text) {
        this.specialHandlingInstructionLine1Text = specialHandlingInstructionLine1Text;
    }

    public String getSpecialHandlingInstructionLine2Text() {
        return specialHandlingInstructionLine2Text;
    }

    public void setSpecialHandlingInstructionLine2Text(final String specialHandlingInstructionLine2Text) {
        this.specialHandlingInstructionLine2Text = specialHandlingInstructionLine2Text;
    }

    public String getSpecialHandlingInstructionLine3Text() {
        return specialHandlingInstructionLine3Text;
    }

    public void setSpecialHandlingInstructionLine3Text(final String specialHandlingInstructionLine3Text) {
        this.specialHandlingInstructionLine3Text = specialHandlingInstructionLine3Text;
    }

    public Timestamp getPaymentPaidTimestamp() {
        return paymentPaidTimestamp;
    }

    public void setPaymentPaidTimestamp(final Timestamp paymentPaidTimestamp) {
        this.paymentPaidTimestamp = paymentPaidTimestamp;
    }

    public boolean getPaymentRequestElectronicInvoiceIndicator() {
        return paymentRequestElectronicInvoiceIndicator;
    }

    public void setPaymentRequestElectronicInvoiceIndicator(final boolean paymentRequestElectronicInvoiceIndicator) {
        this.paymentRequestElectronicInvoiceIndicator = paymentRequestElectronicInvoiceIndicator;
    }

    public String getAccountsPayableRequestCancelIdentifier() {
        return accountsPayableRequestCancelIdentifier;
    }

    public void setAccountsPayableRequestCancelIdentifier(final String accountsPayableRequestCancelIdentifier) {
        this.accountsPayableRequestCancelIdentifier = accountsPayableRequestCancelIdentifier;
    }

    public Integer getOriginalVendorHeaderGeneratedIdentifier() {
        return originalVendorHeaderGeneratedIdentifier;
    }

    public void setOriginalVendorHeaderGeneratedIdentifier(final Integer originalVendorHeaderGeneratedIdentifier) {
        this.originalVendorHeaderGeneratedIdentifier = originalVendorHeaderGeneratedIdentifier;
    }

    public Integer getOriginalVendorDetailAssignedIdentifier() {
        return originalVendorDetailAssignedIdentifier;
    }

    public void setOriginalVendorDetailAssignedIdentifier(final Integer originalVendorDetailAssignedIdentifier) {
        this.originalVendorDetailAssignedIdentifier = originalVendorDetailAssignedIdentifier;
    }

    public Integer getAlternateVendorHeaderGeneratedIdentifier() {
        return alternateVendorHeaderGeneratedIdentifier;
    }

    public void setAlternateVendorHeaderGeneratedIdentifier(final Integer alternateVendorHeaderGeneratedIdentifier) {
        this.alternateVendorHeaderGeneratedIdentifier = alternateVendorHeaderGeneratedIdentifier;
    }

    public Integer getAlternateVendorDetailAssignedIdentifier() {
        return alternateVendorDetailAssignedIdentifier;
    }

    public void setAlternateVendorDetailAssignedIdentifier(final Integer alternateVendorDetailAssignedIdentifier) {
        this.alternateVendorDetailAssignedIdentifier = alternateVendorDetailAssignedIdentifier;
    }

    public ShippingPaymentTerms getVendorShippingPaymentTerms() {
        return vendorShippingPaymentTerms;
    }

    public void setVendorShippingPaymentTerms(final ShippingPaymentTerms vendorShippingPaymentTerms) {
        this.vendorShippingPaymentTerms = vendorShippingPaymentTerms;
    }

    public String getVendorShippingTitleCode() {
        if (ObjectUtils.isNotNull(getPurchaseOrderDocument())) {
            return getPurchaseOrderDocument().getVendorShippingTitleCode();
        }
        return vendorShippingTitleCode;
    }

    public void setVendorShippingTitleCode(final String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    public Date getPurchaseOrderEndDate() {
        return purchaseOrderEndDate;
    }

    public void setPurchaseOrderEndDate(final Date purchaseOrderEndDate) {
        this.purchaseOrderEndDate = purchaseOrderEndDate;
    }

    public boolean isPaymentRequestPositiveApprovalIndicator() {
        return paymentRequestPositiveApprovalIndicator;
    }

    public void setPaymentRequestPositiveApprovalIndicator(final boolean paymentRequestPositiveApprovalIndicator) {
        // if paymentRequestPositiveApprovalIndicator functionality is disabled, always set it to false, overriding the passed-in value
        if (!isEnablePaymentRequestPositiveApprovalIndicator()) {
            this.paymentRequestPositiveApprovalIndicator = false;
        } else {
            this.paymentRequestPositiveApprovalIndicator = paymentRequestPositiveApprovalIndicator;
        }
    }

    public boolean isReceivingDocumentRequiredIndicator() {
        return receivingDocumentRequiredIndicator;
    }

    public void setReceivingDocumentRequiredIndicator(final boolean receivingDocumentRequiredIndicator) {
        // if receivingDocumentRequiredIndicator functionality is disabled, always set it to false, overriding the passed-in value
        if (!isEnableReceivingDocumentRequiredIndicator()) {
            this.receivingDocumentRequiredIndicator = false;
        } else {
            this.receivingDocumentRequiredIndicator = receivingDocumentRequiredIndicator;
        }
    }

    @Override
    public String getCampusCode() {
        return getProcessingCampusCode();
    }

    /**
     * Perform logic needed to initiate PREQ Document
     */
    public void initiateDocument() {
        LOG.debug("initiateDocument() started");
        final Person currentUser = GlobalVariables.getUserSession().getPerson();
        updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_INITIATE);
        setAccountsPayableProcessorIdentifier(currentUser.getPrincipalId());
        setProcessingCampusCode(currentUser.getCampusCode());
        refreshNonUpdateableReferences();
    }

    /**
     * Perform logic needed to clear the initial fields on a PREQ Document
     */
    public void clearInitFields() {
        LOG.debug("clearDocument() started");
        // Clearing document overview fields
        getDocumentHeader().setDocumentDescription(null);
        getDocumentHeader().setExplanation(null);
        getDocumentHeader().setFinancialDocumentTotalAmount(null);
        getDocumentHeader().setOrganizationDocumentNumber(null);

        // Clearing document Init fields
        setPurchaseOrderIdentifier(null);
        setInvoiceNumber(null);
        setInvoiceDate(null);
        setVendorInvoiceAmount(null);
        setSpecialHandlingInstructionLine1Text(null);
        setSpecialHandlingInstructionLine2Text(null);
        setSpecialHandlingInstructionLine3Text(null);
    }

    /**
     * Populates a preq from a PO - delegate method
     *
     * @param po
     */
    public void populatePaymentRequestFromPurchaseOrder(final PurchaseOrderDocument po) {
        populatePaymentRequestFromPurchaseOrder(po, new HashMap<>());
    }

    /**
     * Populates a preq from a PO
     *
     * @param po                         Purchase Order Document used for populating the PREQ
     * @param expiredOrClosedAccountList a list of closed or expired accounts
     */
    public void populatePaymentRequestFromPurchaseOrder(
            final PurchaseOrderDocument po, final HashMap<String,
            ExpiredOrClosedAccountEntry> expiredOrClosedAccountList) {
        setPurchaseOrderIdentifier(po.getPurapDocumentIdentifier());
        getDocumentHeader().setOrganizationDocumentNumber(po.getDocumentHeader().getOrganizationDocumentNumber());
        setPostingYear(po.getPostingYear());
        setReceivingDocumentRequiredIndicator(po.isReceivingDocumentRequiredIndicator());
        setUseTaxIndicator(po.isUseTaxIndicator());
        setPaymentRequestPositiveApprovalIndicator(po.isPaymentRequestPositiveApprovalIndicator());
        setVendorCustomerNumber(po.getVendorCustomerNumber());
        setAccountDistributionMethod(po.getAccountDistributionMethod());

        if (po.getPurchaseOrderCostSource() != null) {
            setPaymentRequestCostSource(po.getPurchaseOrderCostSource());
            setPaymentRequestCostSourceCode(po.getPurchaseOrderCostSourceCode());
        }

        if (po.getVendorShippingPaymentTerms() != null) {
            setVendorShippingPaymentTerms(po.getVendorShippingPaymentTerms());
            setVendorShippingPaymentTermsCode(po.getVendorShippingPaymentTermsCode());
        }

        if (po.getVendorPaymentTerms() != null) {
            setVendorPaymentTermsCode(po.getVendorPaymentTermsCode());
            setVendorPaymentTerms(po.getVendorPaymentTerms());
        }

        if (po.getRecurringPaymentType() != null) {
            setRecurringPaymentType(po.getRecurringPaymentType());
            setRecurringPaymentTypeCode(po.getRecurringPaymentTypeCode());
        }

        setVendorHeaderGeneratedIdentifier(po.getVendorHeaderGeneratedIdentifier());
        setVendorDetailAssignedIdentifier(po.getVendorDetailAssignedIdentifier());
        setVendorCustomerNumber(po.getVendorCustomerNumber());
        setVendorName(po.getVendorName());

        // set original vendor
        setOriginalVendorHeaderGeneratedIdentifier(po.getVendorHeaderGeneratedIdentifier());
        setOriginalVendorDetailAssignedIdentifier(po.getVendorDetailAssignedIdentifier());

        // set alternate vendor info as well
        setAlternateVendorHeaderGeneratedIdentifier(po.getAlternateVendorHeaderGeneratedIdentifier());
        setAlternateVendorDetailAssignedIdentifier(po.getAlternateVendorDetailAssignedIdentifier());

        // populate preq vendor address with the default remit address type for the vendor if found
        final String userCampus = GlobalVariables.getUserSession().getPerson().getCampusCode();
        final VendorAddress vendorAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
                po.getVendorHeaderGeneratedIdentifier(), po.getVendorDetailAssignedIdentifier(),
                VendorConstants.AddressTypes.REMIT, userCampus);
        if (vendorAddress != null) {
            templateVendorAddress(vendorAddress);
            setVendorAddressGeneratedIdentifier(vendorAddress.getVendorAddressGeneratedIdentifier());
            setVendorAttentionName(StringUtils.defaultString(vendorAddress.getVendorAttentionName()));
        } else {
            // set address from PO
            setVendorAddressGeneratedIdentifier(po.getVendorAddressGeneratedIdentifier());
            setVendorLine1Address(po.getVendorLine1Address());
            setVendorLine2Address(po.getVendorLine2Address());
            setVendorCityName(po.getVendorCityName());
            setVendorAddressInternationalProvinceName(po.getVendorAddressInternationalProvinceName());
            setVendorStateCode(po.getVendorStateCode());
            setVendorPostalCode(po.getVendorPostalCode());
            setVendorCountryCode(po.getVendorCountryCode());

            final boolean blankAttentionLine = StringUtils.equalsIgnoreCase("Y",
                    SpringContext.getBean(ParameterService.class).getParameterValueAsString(
                            PurapConstants.PURAP_NAMESPACE, "Document",
                            PurapParameterConstants.CLEAR_ATTENTION_LINE_IND
                    ));

            if (blankAttentionLine) {
                setVendorAttentionName(StringUtils.EMPTY);
            } else {
                setVendorAttentionName(StringUtils.defaultString(po.getVendorAttentionName()));
            }
        }

        setPaymentRequestPayDate(SpringContext.getBean(PaymentRequestService.class).calculatePayDate(
                getInvoiceDate(), getVendorPaymentTerms()));

        if (SpringContext.getBean(PaymentRequestService.class).encumberedItemExistsForInvoicing(po)) {
            for (final PurchaseOrderItem poi : (List<PurchaseOrderItem>) po.getItems()) {
                // check to make sure it's eligible for payment (i.e. active and has encumbrance available
                if (getDocumentSpecificService().poItemEligibleForAp(this, poi)) {
                    final PaymentRequestItem paymentRequestItem = new PaymentRequestItem(poi, this,
                            expiredOrClosedAccountList);
                    getItems().add(paymentRequestItem);
                    final PurchasingCapitalAssetItem purchasingCAMSItem = po.getPurchasingCapitalAssetItemByItemIdentifier(
                            poi.getItemIdentifier());
                    if (purchasingCAMSItem != null) {
                        paymentRequestItem.setCapitalAssetTransactionTypeCode(
                                purchasingCAMSItem.getCapitalAssetTransactionTypeCode());
                    }
                }
            }
        }

        // add missing below the line
        SpringContext.getBean(PurapService.class).addBelowLineItems(this);
        setAccountsPayablePurchasingDocumentLinkIdentifier(po.getAccountsPayablePurchasingDocumentLinkIdentifier());

        //fix up below the line items
        SpringContext.getBean(PaymentRequestService.class).removeIneligibleAdditionalCharges(this);

        fixItemReferences();
        refreshNonUpdateableReferences();
    }

    @Override
    public String getDocumentTitle() {
        if (SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(PaymentRequestDocument.class,
                PurapParameterConstants.PURAP_OVERRIDE_PREQ_DOC_TITLE)) {
            return getCustomDocumentTitle();
        }
        return buildDocumentTitle(super.getDocumentTitle());
    }

    /**
     * Returns a custom document title based on the workflow document title. Depending on what route level the
     * document is currently in, the PO, vendor, amount, account number, dept, campus may be added to the documents
     * title.
     *
     * @return Customized document title text dependent upon route level.
     */
    protected String getCustomDocumentTitle() {
        // set the workflow document title
        final String poNumber = getPurchaseOrderIdentifier().toString();
        final String vendorName = StringUtils.trimToEmpty(getVendorName());
        final String preqAmount = getGrandTotal().toString();

        String documentTitle = "";
        final Set<String> nodeNames = getDocumentHeader().getWorkflowDocument().getCurrentNodeNames();

        // if this doc is final or will be final
        if (CollectionUtils.isEmpty(nodeNames) || getDocumentHeader().getWorkflowDocument()
                .isFinal()) {
            documentTitle = "PO: " + poNumber + " Vendor: " + vendorName + " Amount: " + preqAmount;
        } else {
            final PurApAccountingLine theAccount = getFirstAccount();
            final String accountNumber = theAccount != null ? StringUtils.trimToEmpty(theAccount.getAccountNumber()) :
                    "n/a";
            final String subAccountNumber = theAccount != null ?
                    StringUtils.trimToEmpty(theAccount.getSubAccountNumber()) : "";
            final String accountChart = theAccount != null ? theAccount.getChartOfAccountsCode() : "";
            final String payDate = getDateTimeService().toDateString(getPaymentRequestPayDate());
            final String indicator = getTitleIndicator();
            // set title to: PO# - VendorName - Chart/Account - total amt - Pay Date - Indicator (ie Hold, Request
            // Cancel)
            documentTitle = "PO: " + poNumber + " Vendor: " + vendorName + " Account: " + accountChart + " " +
                    accountNumber + " " + subAccountNumber + " Amount: " + preqAmount + " Pay Date: " + payDate +
                    " " + indicator;
        }
        return documentTitle;
    }

    /**
     * Returns the first payment item's first account (assuming the item list is sequentially ordered).
     *
     * @return Accounting Line object for first account of first payment item.
     */
    public PurApAccountingLine getFirstAccount() {
        // loop through items, and pick the first item
        if (getItems() != null && !getItems().isEmpty()) {
            for (final Object anItem : getItems()) {
                final PaymentRequestItem item = (PaymentRequestItem) anItem;
                if (item.isConsideredEntered() && item.getSourceAccountingLines() != null
                    && !item.getSourceAccountingLines().isEmpty()) {
                    // accounting lines are not empty so pick the first account
                    final PurApAccountingLine accountLine = item.getSourceAccountingLine(0);
                    accountLine.refreshNonUpdateableReferences();
                    return accountLine;
                }
            }
        }
        return null;
    }

    /**
     * Determines the indicator text that will appear in the workflow document title
     *
     * @return Text of hold or request cancel
     */
    protected String getTitleIndicator() {
        if (isHoldIndicator()) {
            return PurapConstants.PaymentRequestIndicatorText.HOLD;
        } else if (isPaymentRequestedCancelIndicator()) {
            return PurapConstants.PaymentRequestIndicatorText.REQUEST_CANCEL;
        }
        return "";
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        LOG.debug("doRouteStatusChange() started");

        super.doRouteStatusChange(statusChangeEvent);
        //KFSCNTRB-1207 - UMD - Muddu -- start
        // if the document was processed but approved by the auto approve payment request job then all we want to
        // do is to change the application document status to auto-approved by looking at the
        // autoApprovedIndicator on the preq.
        // DOCUEMNT PROCESSED BY THE AUTOAPPROVEPAYMENTREQUEST JOB...
        if (isAutoApprovedIndicator()) {
            updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_AUTO_APPROVED);
            // DOCUMENT PROCESSED .. //KFSCNTRB-1207 - UMD - Muddu -- end
        }
        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            if (!PaymentRequestStatuses.APPDOC_AUTO_APPROVED.equals(getApplicationDocumentStatus())) {
                populateDocumentForRouting();
                updateAndSaveAppDocStatus(PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED);
            }
            getPurapGeneralLedgerService().generateEntriesProcessedPaymentRequest(this);
            final boolean isExternal =
                    KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_EXTERNAL.equals(paymentMethodCode);
            final boolean isWireTransfer =
                    KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE.equals(paymentMethodCode);
            if (isExternal || isWireTransfer || immediatePaymentIndicator) {
                LOG.debug(
                        "doRouteStatusChange(...) - Extracting PREQ to PDP : documentNumber={}; paymentMethodCode={}; "
                            + "immediatePaymentIndicator={}",
                        documentNumber,
                        paymentMethodCode,
                        immediatePaymentIndicator
                );
                getPdpExtractService().extractPaymentRequestDocument(this);
            }
        } else if (getDocumentHeader().getWorkflowDocument().isDisapproved()) {
            // DOCUMENT DISAPPROVED
            final String nodeName = SpringContext.getBean(WorkflowDocumentService.class).getCurrentRouteLevelName(
                    getDocumentHeader().getWorkflowDocument());
            String disapprovalStatus = PaymentRequestStatuses
                    .getPaymentRequestAppDocDisapproveStatuses().get(nodeName);

            if (ObjectUtils.isNotNull(nodeName)) {
                if (StringUtils.isBlank(disapprovalStatus)) {
                    final String applicationDocumentStatus = getApplicationDocumentStatus();
                    if (PaymentRequestStatuses.APPDOC_INITIATE.equals(applicationDocumentStatus)
                            || PaymentRequestStatuses.APPDOC_IN_PROCESS.equals(applicationDocumentStatus)) {
                        disapprovalStatus = PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS;
                    } else if (PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW.equals(
                            applicationDocumentStatus)) {
                        disapprovalStatus = PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE;
                    }
                }
                if (StringUtils.isNotBlank(disapprovalStatus)) {
                    SpringContext.getBean(AccountsPayableService.class).cancelAccountsPayableDocument(this, nodeName);
                }
            } else {
                logAndThrowRuntimeException("No status found to set for document being disapproved in node '" +
                        nodeName + "'");
            }
        } else if (getDocumentHeader().getWorkflowDocument().isCanceled()) {
            // DOCUMENT CANCELED
            final String currentNodeName = SpringContext.getBean(WorkflowDocumentService.class)
                    .getCurrentRouteLevelName(getDocumentHeader().getWorkflowDocument());
            String cancelledStatus = PaymentRequestStatuses
                    .getPaymentRequestAppDocDisapproveStatuses().get(currentNodeName);

            if (StringUtils.isBlank(cancelledStatus)
                    && StringUtils.isBlank(PaymentRequestStatuses
                        .getPaymentRequestAppDocDisapproveStatuses().get(currentNodeName))
                    && (PaymentRequestStatuses.APPDOC_INITIATE.equals(getApplicationDocumentStatus())
                    || PaymentRequestStatuses.APPDOC_IN_PROCESS.equals(getApplicationDocumentStatus()))) {
                cancelledStatus = PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS;
            }

            if (ObjectUtils.isNotNull(cancelledStatus)) {
                SpringContext.getBean(AccountsPayableService.class).cancelAccountsPayableDocument(this,
                        currentNodeName);
                updateAndSaveAppDocStatus(cancelledStatus);
            } else {
                logAndThrowRuntimeException("No status found to set for document being canceled in node '" +
                        currentNodeName + "'");
            }
        }
    }

    /**
     * Generates correcting entries to the GL if accounts are modified.
     */
    @Override
    public void doActionTaken(final ActionTakenEvent event) {
        super.doActionTaken(event);
        final WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        String currentNode = null;
        final Set<String> currentNodes = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentNodes)) {
            final Object[] names = currentNodes.toArray();
            if (names.length > 0) {
                currentNode = (String) names[0];
            }
        }

        // everything in the below list requires correcting entries to be written to the GL
        if (PaymentRequestStatuses.getNodesRequiringCorrectingGeneralLedgerEntries().contains(currentNode)) {
            getPurapGeneralLedgerService().generateEntriesModifyPaymentRequest(this);
        }
    }

    @Override
    public boolean processNodeChange(final String newNodeName, final String oldNodeName) {
        if (PaymentRequestStatuses.APPDOC_AUTO_APPROVED.equals(getApplicationDocumentStatus())) {
            // do nothing for an auto approval
            return false;
        }
        if (PaymentRequestStatuses.NODE_ADHOC_REVIEW.equals(oldNodeName)) {
            SpringContext.getBean(AccountsPayableService.class).performLogicForFullEntryCompleted(this);
        }
        return true;
    }

    @Override
    public void saveDocumentFromPostProcessing() {
        final PurapService purapService = SpringContext.getBean(PurapService.class);
        purapService.saveDocumentNoValidation(this);

        // if we've hit full entry completed then close/reopen po
        if (purapService.isFullDocumentEntryCompleted(this) && isClosePurchaseOrderIndicator()) {
            purapService.performLogicForCloseReopenPO(this);
        }
    }

    @Override
    public Class getItemClass() {
        return PaymentRequestItem.class;
    }

    @Override
    public Class getItemUseTaxClass() {
        return PaymentRequestItemUseTax.class;
    }

    @Override
    public PurchaseOrderDocument getPurApSourceDocumentIfPossible() {
        return getPurchaseOrderDocument();
    }

    @Override
    public String getPurApSourceDocumentLabelIfPossible() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentLabelByTypeName(
                PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT);
    }

    public String getPurchaseOrderNotes() {
        final ArrayList poNotes = (ArrayList) getPurchaseOrderDocument().getNotes();

        if (poNotes.size() > 0) {
            return "Yes";
        }
        return "No";
    }

    public void setPurchaseOrderNotes(final String purchaseOrderNotes) {
        this.purchaseOrderNotes = purchaseOrderNotes;
    }

    public String getRecurringPaymentTypeCode() {
        return recurringPaymentTypeCode;
    }

    public void setRecurringPaymentTypeCode(final String recurringPaymentTypeCode) {
        this.recurringPaymentTypeCode = recurringPaymentTypeCode;
    }

    /**
     * Returns the total encumbered amount from the purchase order excluding below the line.
     *
     * @return Total cost excluding below the line
     */
    public KualiDecimal getItemTotalPoEncumbranceAmount() {
        // get total from po excluding below the line and inactive
        return getPurchaseOrderDocument().getTotalDollarAmount(false, false);
    }

    public KualiDecimal getItemTotalPoEncumbranceAmountRelieved() {
        return getItemTotalPoEncumbranceAmountRelieved(false);
    }

    public KualiDecimal getItemTotalPoEncumbranceAmountRelieved(final boolean includeBelowTheLine) {
        KualiDecimal total = KualiDecimal.ZERO;

        for (final PurchaseOrderItem item : (List<PurchaseOrderItem>) getPurchaseOrderDocument().getItems()) {
            final ItemType it = item.getItemType();
            if (includeBelowTheLine || it.isLineItemIndicator()) {
                total = total.add(item.getItemEncumbranceRelievedAmount());
            }
        }
        return total;
    }

    public KualiDecimal getLineItemTotal() {
        return getTotalDollarAmountAboveLineItems();
    }

    public KualiDecimal getLineItemPreTaxTotal() {
        return getTotalPreTaxDollarAmountAboveLineItems();
    }

    public KualiDecimal getLineItemTaxAmount() {
        return getTotalTaxAmountAboveLineItems();
    }

    @Override
    public KualiDecimal getGrandTotal() {
        return getTotalDollarAmount();
    }

    public KualiDecimal getGrandTotalExcludingDiscount() {
        final String[] discountCode = new String[]{PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE};
        return getTotalDollarAmountWithExclusions(discountCode, true);
    }

    /**
     * This method is here due to a setter requirement by the htmlControlAttribute
     *
     * @param amount Grand total for document, excluding discount
     */
    public void setGrandTotalExcludingDiscount(final KualiDecimal amount) {
        // do nothing
    }

    public KualiDecimal getGrandPreTaxTotal() {
        return getTotalPreTaxDollarAmount();
    }

    public KualiDecimal getGrandPreTaxTotalExcludingDiscount() {
        final String[] discountCode = new String[]{PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE};
        return getTotalPreTaxDollarAmountWithExclusions(discountCode, true);
    }

    public KualiDecimal getGrandTaxAmount() {
        return getTotalTaxAmount();
    }

    public KualiDecimal getGrandTaxAmountExcludingDiscount() {
        final String[] discountCode = new String[]{PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE};
        return getTotalTaxAmountWithExclusions(discountCode, true);
    }

    public boolean isDiscount() {
        return SpringContext.getBean(PaymentRequestService.class).hasDiscountItem(this);
    }

    /**
     * @return total paid on the po excluding below the line
     */
    public KualiDecimal getItemTotalPoPaidAmount() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (final PurchaseOrderItem item : (List<PurchaseOrderItem>) getPurchaseOrderDocument().getItems()) {
            final ItemType iT = item.getItemType();
            if (iT.isLineItemIndicator()) {
                final KualiDecimal itemPaid = item.getItemPaidAmount();
                total = total.add(itemPaid);
            }
        }
        return total;
    }

    /**
     * @return name of who requested cancel.
     */
    public String getAccountsPayableRequestCancelPersonName() {
        final String personName;
        final Person user = SpringContext.getBean(PersonService.class).getPerson(getAccountsPayableRequestCancelIdentifier());
        if (user != null) {
            personName = user.getName();
        } else {
            personName = "";
        }

        return personName;
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param amount total po amount paid
     */
    public void setItemTotalPoPaidAmount(final KualiDecimal amount) {
        // do nothing
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param amount total po encumbrance
     */
    public void setItemTotalPoEncumbranceAmount(final KualiDecimal amount) {
        // do nothing
    }

    /**
     * Exists due to a setter requirement by the htmlControlAttribute
     *
     * @param amount total po encumbrance amount relieved
     */
    public void setItemTotalPoEncumbranceAmountRelieved(final KualiDecimal amount) {
        // do nothing
    }

    /**
     * Determines the route levels for a given document.
     *
     * @param workflowDocument work flow document
     * @return list of route levels
     */
    protected List<String> getCurrentRouteLevels(final WorkflowDocument workflowDocument) {
        final Set<String> names = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(names)) {
            return new ArrayList<>(names);
        }
        return null;
    }

    public RecurringPaymentType getRecurringPaymentType() {
        if (ObjectUtils.isNull(recurringPaymentType)) {
            refreshReferenceObject(PurapPropertyConstants.RECURRING_PAYMENT_TYPE);
        }
        return recurringPaymentType;
    }

    public void setRecurringPaymentType(final RecurringPaymentType recurringPaymentType) {
        this.recurringPaymentType = recurringPaymentType;
    }

    public PurchaseOrderCostSource getPaymentRequestCostSource() {
        return paymentRequestCostSource;
    }

    public void setPaymentRequestCostSource(final PurchaseOrderCostSource paymentRequestCostSource) {
        this.paymentRequestCostSource = paymentRequestCostSource;
    }

    @Override
    public String getPoDocumentTypeForAccountsPayableDocumentCancel() {
        return PurapDocTypeCodes.PURCHASE_ORDER_REOPEN_DOCUMENT;
    }

    @Override
    public KualiDecimal getInitialAmount() {
        return getVendorInvoiceAmount();
    }

    /**
     * Populates the payment request document, then continues with preparing for save.
     */
    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        final WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        final String workflowDocumentTitle = buildDocumentTitle(workflowDocument.getTitle());

        getDocumentHeader().getWorkflowDocument().setTitle(workflowDocumentTitle);

        // first populate, then call super
        if (event instanceof AttributedContinuePurapEvent) {
            SpringContext.getBean(PaymentRequestService.class).populatePaymentRequest(this);
        }
        super.prepareForSave(event);
    }

    @Override
    public void prepareForSave() {
        super.prepareForSave();

        if (wireTransfer != null) {
            wireTransfer.setDocumentNumber(documentNumber);
        }
    }

    @Override
    protected boolean isAttachmentRequired() {
        if (getPaymentRequestElectronicInvoiceIndicator()) {
            return false;
        }
        return StringUtils.equalsIgnoreCase("Y", SpringContext.getBean(ParameterService.class)
                .getParameterValueAsString(PaymentRequestDocument.class,
                        PurapParameterConstants.PURAP_PREQ_REQUIRE_ATTACHMENT));
    }

    @Override
    public AccountsPayableDocumentSpecificService getDocumentSpecificService() {
        return SpringContext.getBean(PaymentRequestService.class);
    }

    @Override
    public PurApItem getItem(final int pos) {
        final PaymentRequestItem item = (PaymentRequestItem) super.getItem(pos);
        if (item.getPaymentRequest() == null) {
            item.setPaymentRequest(this);
        }
        return item;
    }

    public String getPrimaryVendorName() {
        if (primaryVendorName == null) {
            final VendorDetail vd = SpringContext.getBean(VendorService.class).getVendorDetail(
                    getOriginalVendorHeaderGeneratedIdentifier(),
                    getOriginalVendorDetailAssignedIdentifier());

            if (vd != null) {
                primaryVendorName = vd.getVendorName();
            }
        }

        return primaryVendorName;
    }

    @Deprecated
    public void setPrimaryVendorName(final String primaryVendorName) {
    }

    /**
     * Forces general ledger entries to be approved, does not wait for payment request document final approval.
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(
            final GeneralLedgerPendingEntrySourceDetail postable,
            final GeneralLedgerPendingEntry explicitEntry) {
        super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);

        getPurapGeneralLedgerService().customizeGeneralLedgerPendingEntry(this,
                (AccountingLine) postable, explicitEntry, getPurchaseOrderIdentifier(),
                getDebitCreditCodeForGLEntries(), PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT,
                isGenerateEncumbranceEntries());

        // PREQs do not wait for document final approval to post GL entries; here we are forcing them to be APPROVED
        explicitEntry.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);

        if (generateExternalEntries) {
            explicitEntry.setFinancialDocumentTypeCode(PurapDocTypeCodes.PAYMENT_REQUEST_EXTERNAL);
        } else if (generateWireTransferEntries) {
            explicitEntry.setFinancialDocumentTypeCode(PurapDocTypeCodes.PAYMENT_REQUEST_WIRE_TRANSFER);
        }
    }

    @Override
    public boolean generateGeneralLedgerPendingEntries(
            final GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper
    ) {
        final boolean glpesGenerated = super.generateGeneralLedgerPendingEntries(glpeSourceDetail, sequenceHelper);

        // CU customization: remove code that generates bank offsets as this will be generated for PRNC
//        final boolean bankOffsetsOk;
//        if (generateExternalEntries) {
//            bankOffsetsOk = generateBankOffsetGeneralLedgerPendingEntries(
//                    sequenceHelper,
//                    PurapDocTypeCodes.PAYMENT_REQUEST_EXTERNAL);
//        } else if (generateWireTransferEntries) {
//            bankOffsetsOk = generateBankOffsetGeneralLedgerPendingEntries(
//                    sequenceHelper,
//                    PurapDocTypeCodes.PAYMENT_REQUEST_WIRE_TRANSFER);
//        } else {
//            bankOffsetsOk = true;
//        }

        return glpesGenerated;
    }

    private boolean generateBankOffsetGeneralLedgerPendingEntries(
            final GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            final String glpeDocumentType
    ) {
        final int existingSequenceNumber = sequenceHelper.getSequenceCounter();
        sequenceHelper.increment();
        final boolean success = getPaymentSourceHelperService().generateDocumentBankOffsetEntries(
                this,
                sequenceHelper,
                glpeDocumentType);
        generalLedgerPendingEntries.forEach(glpe -> {
            if (glpe.getTransactionLedgerEntrySequenceNumber() > existingSequenceNumber) {
                glpe.setFinancialDocumentApprovedCode(
                        KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);
            }
        });
        return success;
    }

    /**
     * Provides answers to the following splits: PurchaseWasReceived VendorIsEmployeeOrNonresident
     */
    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.REQUIRES_IMAGE_ATTACHMENT)) {
            return requiresAccountsPayableReviewRouting();
        }
        if (nodeName.equals(PurapWorkflowConstants.PURCHASE_WAS_RECEIVED)) {
            return shouldWaitForReceiving();
        }
        if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NONRESIDENT) ||
                "VendorIsEmployeeOrNonResidentAlien".equals(nodeName)) {
            return isVendorEmployeeOrNonresident();
        }
        if (nodeName.equals(PurapWorkflowConstants.IS_DOCUMENT_AUTO_APPROVED)) {
            return isAutoApprovedIndicator();
        }
        throw new UnsupportedOperationException("Cannot answer split question for this node you call \"" + nodeName +
                "\"");
    }

    protected boolean isVendorEmployeeOrNonresident() {
        final String vendorHeaderGeneratedId = getVendorHeaderGeneratedIdentifier().toString();
        if (StringUtils.isBlank(vendorHeaderGeneratedId)) {
            // no vendor header id so can't check for proper tax routing
            return false;
        }
        final VendorService vendorService = SpringContext.getBean(VendorService.class);
        final boolean routeDocumentAsEmployeeVendor = vendorService.isVendorInstitutionEmployee(Integer.valueOf(
                vendorHeaderGeneratedId));
        final boolean routeDocumentAsForeignVendor = vendorService.isVendorForeign(Integer.valueOf(vendorHeaderGeneratedId));
        return routeDocumentAsEmployeeVendor || routeDocumentAsForeignVendor;
    }

    /**
     * Payment Request needs to wait for receiving if the receiving requirements have NOT been met.
     *
     * @return
     */
    protected boolean shouldWaitForReceiving() {
        // only require if PO was marked to require receiving
        if (isReceivingDocumentRequiredIndicator()) {
            return !isReceivingRequirementMet();
        }

        //receiving is not required or has already been fulfilled, no need to stop for routing
        return false;
    }

    /**
     * Determine if the receiving requirement has been met for all items on the payment request. If any item does not
     * have receiving requirements met, return false. Receiving requirement has NOT been met if the quantity invoiced
     * on the Payment Request is greater than the quantity of "unpaid and received" items determined by
     * (poQtyReceived - (poQtyInvoiced - preqQtyInvoiced)). We have to subtract preqQtyInvoiced from the poQtyInvoiced
     * because this payment request has already updated the totals on the po.
     *
     * @return boolean return true if the receiving requirement has been met for all items on the payment request;
     *         false if requirement has not been met
     */
    public boolean isReceivingRequirementMet() {
        for (final Object item : getItems()) {
            final PaymentRequestItem preqItem = (PaymentRequestItem) item;

            if (preqItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
                final PurchaseOrderItem poItem = preqItem.getPurchaseOrderItem();
                final KualiDecimal preqQuantityInvoiced =
                        preqItem.getItemQuantity() == null ? KualiDecimal.ZERO : preqItem.getItemQuantity();
                final KualiDecimal poQuantityReceived = poItem.getItemReceivedTotalQuantity() == null ? KualiDecimal.ZERO :
                        poItem.getItemReceivedTotalQuantity();
                final KualiDecimal poQuantityInvoiced = poItem.getItemInvoicedTotalQuantity() == null ? KualiDecimal.ZERO :
                        poItem.getItemInvoicedTotalQuantity();

                // receiving has NOT been met if preqQtyInvoiced is greater than
                // (poQtyReceived & (poQtyInvoiced & preqQtyInvoiced))
                if (preqQuantityInvoiced.compareTo(poQuantityReceived.subtract(
                        poQuantityInvoiced.subtract(preqQuantityInvoiced))) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Date getTransactionTaxDate() {
        return getInvoiceDate();
    }

    public String getTaxClassificationCode() {
        return taxClassificationCode;
    }

    public void setTaxClassificationCode(final String taxClassificationCode) {
        this.taxClassificationCode = taxClassificationCode;
    }

    public KualiDecimal getTaxFederalPercentShort() {
        return new KualiDecimal(taxFederalPercent);
    }

    public BigDecimal getTaxFederalPercent() {
        return taxFederalPercent;
    }

    public void setTaxFederalPercent(final BigDecimal taxFederalPercent) {
        this.taxFederalPercent = taxFederalPercent;
    }

    public KualiDecimal getTaxStatePercentShort() {
        return new KualiDecimal(taxStatePercent);
    }

    public BigDecimal getTaxStatePercent() {
        return taxStatePercent;
    }

    public void setTaxStatePercent(final BigDecimal taxStatePercent) {
        this.taxStatePercent = taxStatePercent;
    }

    public String getTaxCountryCode() {
        return taxCountryCode;
    }

    public void setTaxCountryCode(final String taxCountryCode) {
        this.taxCountryCode = taxCountryCode;
    }

    public Boolean getTaxGrossUpIndicator() {
        return taxGrossUpIndicator;
    }

    public void setTaxGrossUpIndicator(final Boolean taxGrossUpIndicator) {
        this.taxGrossUpIndicator = taxGrossUpIndicator;
    }

    public Boolean getTaxExemptTreatyIndicator() {
        return taxExemptTreatyIndicator;
    }

    public void setTaxExemptTreatyIndicator(final Boolean taxExemptTreatyIndicator) {
        this.taxExemptTreatyIndicator = taxExemptTreatyIndicator;
    }

    public Boolean getTaxForeignSourceIndicator() {
        return taxForeignSourceIndicator;
    }

    public void setTaxForeignSourceIndicator(final Boolean taxForeignSourceIndicator) {
        this.taxForeignSourceIndicator = taxForeignSourceIndicator;
    }

    public KualiDecimal getTaxSpecialW4Amount() {
        return taxSpecialW4Amount;
    }

    public void setTaxSpecialW4Amount(final KualiDecimal taxSpecialW4Amount) {
        this.taxSpecialW4Amount = taxSpecialW4Amount;
    }

    public Boolean getTaxUSAIDPerDiemIndicator() {
        return taxUSAIDPerDiemIndicator;
    }

    public void setTaxUSAIDPerDiemIndicator(final Boolean taxUSAIDPerDiemIndicator) {
        this.taxUSAIDPerDiemIndicator = taxUSAIDPerDiemIndicator;
    }

    public Boolean getTaxOtherExemptIndicator() {
        return taxOtherExemptIndicator;
    }

    public void setTaxOtherExemptIndicator(final Boolean taxOtherExemptIndicator) {
        this.taxOtherExemptIndicator = taxOtherExemptIndicator;
    }

    public String getTaxNQIId() {
        return taxNQIId;
    }

    public void setTaxNQIId(final String taxNQIId) {
        this.taxNQIId = taxNQIId;
    }

    public boolean isPaymentRequestedCancelIndicatorForSearching() {
        return paymentRequestedCancelIndicator;
    }

    public boolean getPaymentRequestPositiveApprovalIndicatorForSearching() {
        return paymentRequestPositiveApprovalIndicator;
    }

    public boolean isAchSignUpStatusFlag() {
        return achSignUpStatusFlag;
    }

    public void setAchSignUpStatusFlag(final boolean achSignUpStatusFlag) {
        this.achSignUpStatusFlag = achSignUpStatusFlag;
    }

    public boolean getReceivingDocumentRequiredIndicatorForSearching() {
        return receivingDocumentRequiredIndicator;
    }

    public String getRequestCancelIndicatorForResult() {
        return isPaymentRequestedCancelIndicator() ? "Yes" : "No";
    }

    public String getPaidIndicatorForResult() {
        return getPaymentPaidTimestamp() != null ? "Yes" : "No";
    }

    public Date getAccountsPayableApprovalDateForSearching() {
        if (getAccountsPayableApprovalTimestamp() == null) {
            return null;
        }
        try {
            final Date date = SpringContext.getBean(DateTimeService.class).convertToSqlDate(
                    getAccountsPayableApprovalTimestamp());
            LOG.debug("getAccountsPayableApprovalDateForSearching() returns {}", date);
            return date;
        } catch (final Exception e) {
            return new Date(getAccountsPayableApprovalTimestamp().getTime());
        }
    }

    /**
     * Checks all documents notes for attachments.
     *
     * @return true if document does not have an image attached, false otherwise
     */
    @Override
    public boolean documentHasNoImagesAttached() {
        final List boNotes = getNotes();
        if (ObjectUtils.isNotNull(boNotes)) {
            for (final Object obj : boNotes) {
                final Note note = (Note) obj;

                note.refreshReferenceObject("attachment");
                if (ObjectUtils.isNotNull(note.getAttachment())
                        && PurapConstants.AttachmentTypeCodes.ATTACHMENT_TYPE_INVOICE_IMAGE.equals(
                                note.getAttachment().getAttachmentTypeCode())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isGenerateExternalEntries() {
        return generateExternalEntries;
    }

    public void setGenerateExternalEntries(final boolean generateExternalEntries) {
        this.generateExternalEntries = generateExternalEntries;
    }

    public boolean isGenerateWireTransferEntries() {
        return generateWireTransferEntries;
    }

    public void setGenerateWireTransferEntries(final boolean generateWireTransferEntries) {
        this.generateWireTransferEntries = generateWireTransferEntries;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    @Override
    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(final String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public PaymentSourceWireTransfer getWireTransfer() {
        return wireTransfer;
    }

    public void setWireTransfer(final PaymentSourceWireTransfer wireTransfer) {
        this.wireTransfer = wireTransfer;
    }

    public boolean isAutoApprovedIndicator() {
        return autoApprovedIndicator;
    }

    public void setAutoApprovedIndicator(final boolean autoApprovedIndicator) {
        this.autoApprovedIndicator = autoApprovedIndicator;
    }

    @Override
    public void processAfterRetrieve() {
        super.processAfterRetrieve();

        // KFSMI-9022 : JHK : This is a bit of a hack, but it seems that the document header is not being loaded
        // properly from within the post-processor and causes problems.
        if (ObjectUtils.isNull(getDocumentHeader()) || StringUtils.isBlank(getDocumentHeader().getDocumentNumber())) {
            WorkflowDocument workflowDocument = null;
            if (getDocumentHeader().hasWorkflowDocument()) {
                workflowDocument = getDocumentHeader().getWorkflowDocument();
            }
            refreshReferenceObject(KFSPropertyConstants.DOCUMENT_HEADER);
            if (ObjectUtils.isNotNull(getDocumentHeader())) {
                getDocumentHeader().setWorkflowDocument(workflowDocument);
            }
        }
    }

    private PaymentSourceHelperService getPaymentSourceHelperService() {
        if (paymentSourceHelperService == null) {
            paymentSourceHelperService = SpringContext.getBean(PaymentSourceHelperService.class);
        }
        return paymentSourceHelperService;
    }

    private PdpExtractService getPdpExtractService() {
        if (pdpExtractService == null) {
            pdpExtractService = SpringContext.getBean(PdpExtractService.class);
        }
        return pdpExtractService;
    }

    private PurapGeneralLedgerService getPurapGeneralLedgerService() {
        if (purapGeneralLedgerService == null) {
            purapGeneralLedgerService = SpringContext.getBean(PurapGeneralLedgerService.class);
        }
        return purapGeneralLedgerService;
    }
}
