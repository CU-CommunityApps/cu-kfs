/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.service.SufficientFundsService;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystem;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.action.ActionRequestType;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteLevelChange;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.dao.DocumentDao;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.CreditMemoStatuses;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.QuoteTypeDescriptions;
import org.kuali.kfs.module.purap.PurapConstants.RequisitionSources;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.CapitalAssetSystem;
import org.kuali.kfs.module.purap.businessobject.CreditMemoView;
import org.kuali.kfs.module.purap.businessobject.ItemType;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderCapitalAssetSystem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItemUseTax;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderSensitiveData;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderVendorChoice;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderVendorQuote;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderVendorStipulation;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderView;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.businessobject.RecurringPaymentFrequency;
import org.kuali.kfs.module.purap.businessobject.RequisitionCapitalAssetItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.dataaccess.PurchaseOrderDao;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.module.purap.document.service.PurchasingDocumentSpecificService;
import org.kuali.kfs.module.purap.document.service.RequisitionService;
import org.kuali.kfs.module.purap.service.PurapAccountingService;
import org.kuali.kfs.module.purap.service.PurapGeneralLedgerService;
import org.kuali.kfs.module.purap.util.PurApItemUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.SufficientFundsItem;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.MultiselectableDocSearchConversion;
import org.kuali.kfs.sys.document.validation.event.AccountingDocumentSaveWithNoLedgerEntryGenerationEvent;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.ContractManager;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.ShippingPaymentTerms;
import org.kuali.kfs.vnd.businessobject.ShippingTitle;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PurchaseOrderDocument extends PurchasingDocumentBase implements MultiselectableDocSearchConversion {

    private static final Logger LOG = LogManager.getLogger();

    protected Timestamp purchaseOrderCreateTimestamp;
    protected Integer requisitionIdentifier;
    protected String purchaseOrderVendorChoiceCode;
    protected String recurringPaymentFrequencyCode;
    protected KualiDecimal recurringPaymentAmount;
    protected Date recurringPaymentDate;
    protected KualiDecimal initialPaymentAmount;
    protected Date initialPaymentDate;
    protected KualiDecimal finalPaymentAmount;
    protected Date finalPaymentDate;
    protected Timestamp purchaseOrderInitialOpenTimestamp;
    protected Timestamp purchaseOrderLastTransmitTimestamp;
    protected Date purchaseOrderQuoteDueDate;
    protected String purchaseOrderQuoteTypeCode;
    protected String purchaseOrderQuoteVendorNoteText;
    protected boolean purchaseOrderConfirmedIndicator;
    protected String purchaseOrderCommodityDescription;
    protected Integer purchaseOrderPreviousIdentifier;
    protected Integer alternateVendorHeaderGeneratedIdentifier;
    protected Integer alternateVendorDetailAssignedIdentifier;
    protected Integer newQuoteVendorHeaderGeneratedIdentifier;
    protected Integer newQuoteVendorDetailAssignedIdentifier;
    protected String alternateVendorName;
    protected boolean purchaseOrderCurrentIndicator;
    protected boolean pendingActionIndicator;
    protected Timestamp purchaseOrderFirstTransmissionTimestamp;
    protected Integer contractManagerCode;
    protected Date purchaseOrderQuoteInitializationDate;
    protected Date purchaseOrderQuoteAwardedDate;
    protected String assignedUserPrincipalId;

    // COLLECTIONS
    protected List<PurchaseOrderVendorStipulation> purchaseOrderVendorStipulations;
    protected List<PurchaseOrderVendorQuote> purchaseOrderVendorQuotes;

    // NOT PERSISTED IN DB
    protected String statusChange;
    protected String alternateVendorNumber;
    protected String purchaseOrderRetransmissionMethodCode;
    protected String retransmitHeader;
    protected Integer purchaseOrderQuoteListIdentifier;
    protected KualiDecimal internalPurchasingLimit;
    // Needed for authorization
    protected boolean pendingSplit;
    // Check box on Split PO tab
    protected boolean copyingNotesWhenSplitting;
    // whether the form is currently used for assigning sensitive data to the PO
    protected boolean assigningSensitiveData;
    protected List<PurchaseOrderSensitiveData> purchaseOrderSensitiveData;
    // this serves as a temporary holder before validation is done
    protected String assignedUserPrincipalName;

    //this is a holder for the accountingLines for GL purposes only; used only for PO change docs
    protected List<SourceAccountingLine> glOnlySourceAccountingLines;

    // REFERENCE OBJECTS
    protected PurchaseOrderVendorChoice purchaseOrderVendorChoice;
    protected PaymentTermType vendorPaymentTerms;
    protected ShippingTitle vendorShippingTitle;
    protected ShippingPaymentTerms vendorShippingPaymentTerms;
    protected RecurringPaymentFrequency recurringPaymentFrequency;
    protected ContractManager contractManager;

    public PurchaseOrderDocument() {
        super();
        purchaseOrderVendorStipulations = new ArrayList<>();
        purchaseOrderVendorQuotes = new ArrayList<>();
    }

    @Override
    public PurchasingDocumentSpecificService getDocumentSpecificService() {
        return SpringContext.getBean(PurchaseOrderService.class);
    }

    /**
     * Overrides the method in PurchasingAccountsPayableDocumentBase to add the criteria specific to Purchase Order
     * Document.
     */
    @Override
    public boolean isInquiryRendered() {
        final String applicationDocumentStatus = getApplicationDocumentStatus();

        return !isPostingYearPrior()
               || !PurchaseOrderStatuses.APPDOC_CLOSED.equals(applicationDocumentStatus)
                 && !PurchaseOrderStatuses.APPDOC_CANCELLED.equals(applicationDocumentStatus)
                 && !PurchaseOrderStatuses.APPDOC_VOID.equals(applicationDocumentStatus);
    }

    @Override
    public String getDocumentTitle() {
        if (SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(PurchaseOrderDocument.class,
                PurapParameterConstants.DOCUMENT_TITLE_IND
        )) {
            return getCustomDocumentTitle();
        }

        return buildDocumentTitle(super.getDocumentTitle());
    }

    /**
     * Returns a custom document title based on the workflow document title. Depending on what route level the
     * document is currently in, various info may be added to the documents title.
     *
     * @return Customized document title text dependent upon route level.
     */
    protected String getCustomDocumentTitle() {
        final String poNumber = getPurapDocumentIdentifier().toString();
        final String cmCode = getContractManagerCode().toString();
        final String vendorName = StringUtils.trimToEmpty(getVendorName());
        final String totalAmount = getTotalDollarAmount().toString();
        final PurApAccountingLine accountingLine = getFirstAccount();
        final String chartAcctCode = accountingLine != null ? accountingLine.getChartOfAccountsCode() : "";
        final String accountNumber = accountingLine != null ? accountingLine.getAccountNumber() : "";
        final String chartCode = getChartOfAccountsCode();
        final String orgCode = getOrganizationCode();
        final String deliveryCampus = getDeliveryCampus() != null ? getDeliveryCampus().getCampus().getShortName() : "";
        String documentTitle = "";

        final Set<String> nodeNames = getDocumentHeader().getWorkflowDocument().getCurrentNodeNames();

        String routeLevel = "";
        if (CollectionUtils.isNotEmpty(nodeNames) && nodeNames.size() >= 1) {
            routeLevel = nodeNames.iterator().next();
        }

        if (StringUtils.equals(getApplicationDocumentStatus(), PurchaseOrderStatuses.APPDOC_OPEN)) {
            documentTitle = super.getDocumentTitle();
        } else if (routeLevel.equals(PurchaseOrderStatuses.NODE_BUDGET_OFFICE_REVIEW)
                || routeLevel.equals(PurchaseOrderStatuses.NODE_CONTRACTS_AND_GRANTS_REVIEW)) {
            // Budget & C&G approval levels
            documentTitle = "PO: " + poNumber + " Account Number: " + chartAcctCode + "-" + accountNumber + " Dept: " + chartCode + "-" + orgCode + " Delivery Campus: " + deliveryCampus;
        }
        // KFSUPGRADE-348
//            else if (routeLevel.equals(PurchaseOrderStatuses.NODE_VENDOR_TAX_REVIEW)) {
//            // Tax approval level
//            documentTitle = "Vendor: " + vendorName + " PO: " + poNumber + " Account Number: " + chartCode + "-" + accountNumber + " Dept: " + chartCode + "-" + orgCode + " Delivery Campus: " + deliveryCampus;
//        }
            else {
                documentTitle += "PO: " + poNumber + " Contract Manager: " + cmCode + " Vendor: " + vendorName + " Amount: " + totalAmount;
            }

        return documentTitle;
    }

    @Override
    public Class getSourceAccountingLineClass() {
        //NOTE: do not do anything with this method as it is used by routing etc!
        return super.getSourceAccountingLineClass();
    }

    /**
     * @return the first PO item's first accounting line (assuming the item list is sequentially ordered).
     */
    protected PurApAccountingLine getFirstAccount() {
        // loop through items, and pick the first item with non-empty accounting lines
        if (getItems() != null && !getItems().isEmpty()) {
            for (final Object anItem : getItems()) {
                final PurchaseOrderItem item = (PurchaseOrderItem) anItem;
                if (item.isConsideredEntered() && item.getSourceAccountingLines() != null
                        && !item.getSourceAccountingLines().isEmpty()) {
                    // accounting lines are not empty so pick the first account
                    final PurApAccountingLine accountingLine = item.getSourceAccountingLine(0);
                    accountingLine.refreshNonUpdateableReferences();
                    return accountingLine;
                }
            }
        }
        return null;
    }

    public String getAssignedUserPrincipalId() {
        return assignedUserPrincipalId;
    }

    public void setAssignedUserPrincipalId(final String assignedUserPrincipalId) {
        this.assignedUserPrincipalId = assignedUserPrincipalId;
    }

    public String getAssignedUserPrincipalName() {
        // init this field when PO is first loaded and assigned user exists in PO
        if (assignedUserPrincipalName == null && assignedUserPrincipalId != null) {
            final Person assignedUser = KimApiServiceLocator.getPersonService().getPerson(assignedUserPrincipalId);
            assignedUserPrincipalName = assignedUser.getPrincipalName();
        }
        // otherwise return its current value directly
        return assignedUserPrincipalName;
    }

    public void setAssignedUserPrincipalName(final String assignedUserPrincipalName) {
        this.assignedUserPrincipalName = assignedUserPrincipalName;
        // each time this field changes we need to update the assigned user ID and ref obj to keep consistent
        // this code can be moved to where PO is saved and with validation too, which may be more appropriate
        Person assignedUser = null;
        if (assignedUserPrincipalName != null) {
            assignedUser = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(assignedUserPrincipalName);
        }
        if (assignedUser != null) {
            assignedUserPrincipalId = assignedUser.getPrincipalId();
        } else {
            assignedUserPrincipalId = null;
        }
    }

    public boolean getAssigningSensitiveData() {
        return assigningSensitiveData;
    }

    public void setAssigningSensitiveData(final boolean assigningSensitiveData) {
        this.assigningSensitiveData = assigningSensitiveData;
    }

    public List<PurchaseOrderSensitiveData> getPurchaseOrderSensitiveData() {
        final Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put(PurapPropertyConstants.PURAP_DOC_ID, getPurapDocumentIdentifier());
        return new ArrayList<>(SpringContext.getBean(BusinessObjectService.class).findMatching(
                PurchaseOrderSensitiveData.class, fieldValues));
    }

    public void setPurchaseOrderSensitiveData(final List<PurchaseOrderSensitiveData> purchaseOrderSensitiveData) {
        this.purchaseOrderSensitiveData = purchaseOrderSensitiveData;
    }

    public ContractManager getContractManager() {
        if (ObjectUtils.isNull(contractManager)) {
            refreshReferenceObject(PurapPropertyConstants.CONTRACT_MANAGER);
        }
        return contractManager;
    }

    public void setContractManager(final ContractManager contractManager) {
        this.contractManager = contractManager;
    }

    public Integer getContractManagerCode() {
        return contractManagerCode;
    }

    public void setContractManagerCode(final Integer contractManagerCode) {
        this.contractManagerCode = contractManagerCode;
    }

    @Override
    public List buildListOfDeletionAwareLists() {
        final List managedLists = super.buildListOfDeletionAwareLists();
        managedLists.add(getGeneralLedgerPendingEntries());
        if (allowDeleteAwareCollection) {
            managedLists.add(getPurchaseOrderVendorQuotes());
            managedLists.add(getPurchaseOrderVendorStipulations());
        }
        return managedLists;
    }

    @Override
    public Boolean getOverrideWorkflowButtons() {
        if (ObjectUtils.isNull(super.getOverrideWorkflowButtons())) {
            // should only be null on the first call... never after
            setOverrideWorkflowButtons(Boolean.TRUE);
        }
        return super.getOverrideWorkflowButtons();
    }

    @Override
    public void customPrepareForSave(final KualiDocumentEvent event) {
        super.customPrepareForSave(event);
        if (ObjectUtils.isNull(getPurapDocumentIdentifier())) {
            // need retrieve the next available PO id to save in GL entries (only do if purap id is null which should
            // be on first save)
            final SequenceAccessorService sas = SpringContext.getBean(SequenceAccessorService.class);
            final Long poSequenceNumber = sas.getNextAvailableSequenceNumber("PO_ID", getClass());
            setPurapDocumentIdentifier(poSequenceNumber.intValue());
        }

        // Set outstanding encumbered quantity/amount on items
        for (final Object anItem : getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) anItem;

            // Set quantities
            item.setItemOutstandingEncumberedQuantity(item.getItemQuantity());
            if (item.getItemInvoicedTotalQuantity() == null) {
                item.setItemInvoicedTotalQuantity(KualiDecimal.ZERO);
            }
            if (item.getItemInvoicedTotalAmount() == null) {
                item.setItemInvoicedTotalAmount(KualiDecimal.ZERO);
            }

            item.setItemOutstandingEncumberedAmount(item.getTotalAmount() == null ? KualiDecimal.ZERO :
                    item.getTotalAmount());

            final List accounts = item.getSourceAccountingLines();
            Collections.sort(accounts);

            for (final Object anAcct : accounts) {
                final PurchaseOrderAccount account = (PurchaseOrderAccount) anAcct;
                if (!account.isEmpty()) {
                    account.setItemAccountOutstandingEncumbranceAmount(account.getAmount());
                }
            }
        }

        setSourceAccountingLines(SpringContext.getBean(PurapAccountingService.class)
                .generateSummaryWithNoZeroTotals(getItems()));
    }

    @Override
    public void prepareForSave(final KualiDocumentEvent event) {
        final WorkflowDocument workFlowDocument = getDocumentHeader().getWorkflowDocument();
        final String documentType = workFlowDocument.getDocumentTypeName();

        if (documentType.equals(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT) ||
            documentType.equals(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_SPLIT_DOCUMENT)) {
            if (workFlowDocument.isCanceled()) {
                // if doc is FINAL or canceled, saving should not be creating GL entries
                setGeneralLedgerPendingEntries(new ArrayList<>());
            } else if (!workFlowDocument.isFinal()) {
                final KualiDocumentEvent adjustedEvent = adjustEventForPrepareForSave(event);
                super.prepareForSave(adjustedEvent);
            }
        }
    }

    private KualiDocumentEvent adjustEventForPrepareForSave(final KualiDocumentEvent event) {
        if (!(event instanceof DocumentSystemSaveEvent)) {
            return event;
        }
        if (event instanceof AccountingDocumentSaveWithNoLedgerEntryGenerationEvent) {
            return event;
        }
        if (CollectionUtils.isEmpty(generalLedgerPendingEntries)) {
            return event;
        }
        // if there are GLPEs, do not regenerate if they are approved
        if (!StringUtils.equals(
                KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED,
                generalLedgerPendingEntries.get(0).getFinancialDocumentApprovedCode())) {
            return event;
        }
        final AccountingDocumentSaveWithNoLedgerEntryGenerationEvent newEvent =
                new AccountingDocumentSaveWithNoLedgerEntryGenerationEvent(
                        event.getErrorPathPrefix(),
                        event.getDocument());
        BeanUtils.copyProperties(event, newEvent);
        return newEvent;
    }

    /**
     * Sets default values for APO.
     */
    public void setDefaultValuesForAPO() {
        setPurchaseOrderAutomaticIndicator(Boolean.TRUE);
        if (!RequisitionSources.B2B.equals(getRequisitionSourceCode())) {
            final String paramValue = SpringContext.getBean(ParameterService.class)
                    .getParameterValueAsString(PurchaseOrderDocument.class, PurapParameterConstants.APO_VENDOR_CHOICE);
            setPurchaseOrderVendorChoiceCode(paramValue);
        }
    }

    /**
     * Populates this Purchase Order from the related Requisition Document.
     *
     * @param requisitionDocument the Requisition Document from which field values are copied.
     */
    public void populatePurchaseOrderFromRequisition(final RequisitionDocument requisitionDocument) {
        setPurchaseOrderCreateTimestamp(SpringContext.getBean(DateTimeService.class).getCurrentTimestamp());
        getDocumentHeader().setOrganizationDocumentNumber(requisitionDocument.getDocumentHeader()
                .getOrganizationDocumentNumber());
        getDocumentHeader().setDocumentDescription(requisitionDocument.getDocumentHeader()
                .getDocumentDescription());
        getDocumentHeader().setExplanation(requisitionDocument.getDocumentHeader().getExplanation());

        setBillingName(requisitionDocument.getBillingName());
        setBillingLine1Address(requisitionDocument.getBillingLine1Address());
        setBillingLine2Address(requisitionDocument.getBillingLine2Address());
        setBillingCityName(requisitionDocument.getBillingCityName());
        setBillingStateCode(requisitionDocument.getBillingStateCode());
        setBillingPostalCode(requisitionDocument.getBillingPostalCode());
        setBillingCountryCode(requisitionDocument.getBillingCountryCode());
        setBillingPhoneNumber(requisitionDocument.getBillingPhoneNumber());
        setBillingEmailAddress(requisitionDocument.getBillingEmailAddress());

        setReceivingName(requisitionDocument.getReceivingName());
        setReceivingCityName(requisitionDocument.getReceivingCityName());
        setReceivingLine1Address(requisitionDocument.getReceivingLine1Address());
        setReceivingLine2Address(requisitionDocument.getReceivingLine2Address());
        setReceivingStateCode(requisitionDocument.getReceivingStateCode());
        setReceivingPostalCode(requisitionDocument.getReceivingPostalCode());
        setReceivingCountryCode(requisitionDocument.getReceivingCountryCode());
        setAddressToVendorIndicator(requisitionDocument.getAddressToVendorIndicator());

        setDeliveryBuildingCode(requisitionDocument.getDeliveryBuildingCode());
        setDeliveryBuildingRoomNumber(requisitionDocument.getDeliveryBuildingRoomNumber());
        setDeliveryBuildingName(requisitionDocument.getDeliveryBuildingName());
        setDeliveryCampusCode(requisitionDocument.getDeliveryCampusCode());
        setDeliveryCityName(requisitionDocument.getDeliveryCityName());
        setDeliveryCountryCode(requisitionDocument.getDeliveryCountryCode());
        setDeliveryInstructionText(requisitionDocument.getDeliveryInstructionText());
        setDeliveryBuildingLine1Address(requisitionDocument.getDeliveryBuildingLine1Address());
        setDeliveryBuildingLine2Address(requisitionDocument.getDeliveryBuildingLine2Address());
        setDeliveryPostalCode(requisitionDocument.getDeliveryPostalCode());
        setDeliveryRequiredDate(requisitionDocument.getDeliveryRequiredDate());
        setDeliveryRequiredDateReasonCode(requisitionDocument.getDeliveryRequiredDateReasonCode());
        setDeliveryStateCode(requisitionDocument.getDeliveryStateCode());
        setDeliveryToEmailAddress(requisitionDocument.getDeliveryToEmailAddress());
        setDeliveryToName(requisitionDocument.getDeliveryToName());
        setDeliveryToPhoneNumber(requisitionDocument.getDeliveryToPhoneNumber());
        setDeliveryBuildingOtherIndicator(requisitionDocument.isDeliveryBuildingOtherIndicator());

        setPurchaseOrderBeginDate(requisitionDocument.getPurchaseOrderBeginDate());
        setPurchaseOrderCostSourceCode(requisitionDocument.getPurchaseOrderCostSourceCode());
        setPostingYear(requisitionDocument.getPostingYear());
        setPurchaseOrderEndDate(requisitionDocument.getPurchaseOrderEndDate());
        setChartOfAccountsCode(requisitionDocument.getChartOfAccountsCode());
        setInstitutionContactEmailAddress(requisitionDocument.getInstitutionContactEmailAddress());
        setInstitutionContactName(requisitionDocument.getInstitutionContactName());
        setInstitutionContactPhoneNumber(requisitionDocument.getInstitutionContactPhoneNumber());
        setNonInstitutionFundAccountNumber(requisitionDocument.getNonInstitutionFundAccountNumber());
        setNonInstitutionFundChartOfAccountsCode(requisitionDocument.getNonInstitutionFundChartOfAccountsCode());
        setNonInstitutionFundOrgChartOfAccountsCode(
                requisitionDocument.getNonInstitutionFundOrgChartOfAccountsCode());
        setNonInstitutionFundOrganizationCode(requisitionDocument.getNonInstitutionFundOrganizationCode());
        setOrganizationCode(requisitionDocument.getOrganizationCode());
        setRecurringPaymentTypeCode(requisitionDocument.getRecurringPaymentTypeCode());
        setRequestorPersonEmailAddress(requisitionDocument.getRequestorPersonEmailAddress());
        setRequestorPersonName(requisitionDocument.getRequestorPersonName());
        setRequestorPersonPhoneNumber(requisitionDocument.getRequestorPersonPhoneNumber());
        setRequisitionIdentifier(requisitionDocument.getPurapDocumentIdentifier());
        setPurchaseOrderTotalLimit(requisitionDocument.getPurchaseOrderTotalLimit());
        setPurchaseOrderTransmissionMethodCode(requisitionDocument.getPurchaseOrderTransmissionMethodCode());
        setUseTaxIndicator(requisitionDocument.isUseTaxIndicator());

        setVendorCityName(requisitionDocument.getVendorCityName());
        setVendorContractGeneratedIdentifier(requisitionDocument.getVendorContractGeneratedIdentifier());
        setVendorCountryCode(requisitionDocument.getVendorCountryCode());
        setVendorCustomerNumber(requisitionDocument.getVendorCustomerNumber());
        setVendorAttentionName(requisitionDocument.getVendorAttentionName());
        setVendorDetailAssignedIdentifier(requisitionDocument.getVendorDetailAssignedIdentifier());
        setVendorFaxNumber(requisitionDocument.getVendorFaxNumber());
        setVendorHeaderGeneratedIdentifier(requisitionDocument.getVendorHeaderGeneratedIdentifier());
        setVendorLine1Address(requisitionDocument.getVendorLine1Address());
        setVendorLine2Address(requisitionDocument.getVendorLine2Address());
        setVendorAddressInternationalProvinceName(
                requisitionDocument.getVendorAddressInternationalProvinceName());
        setVendorName(requisitionDocument.getVendorName());
        setVendorNoteText(requisitionDocument.getVendorNoteText());
        setVendorPhoneNumber(requisitionDocument.getVendorPhoneNumber());
        setVendorPostalCode(requisitionDocument.getVendorPostalCode());
        setVendorStateCode(requisitionDocument.getVendorStateCode());
        setVendorRestrictedIndicator(requisitionDocument.getVendorRestrictedIndicator());
        setJustification(requisitionDocument.getJustification());
        // KFSPTS-1458, KFSPTS-16990: Also copy vendorEmailAddress from Requisition to PO
        setVendorEmailAddress(requisitionDocument.getVendorEmailAddress());

        setExternalOrganizationB2bSupplierIdentifier(
                requisitionDocument.getExternalOrganizationB2bSupplierIdentifier());
        setRequisitionSourceCode(requisitionDocument.getRequisitionSourceCode());
        setAccountsPayablePurchasingDocumentLinkIdentifier(
                requisitionDocument.getAccountsPayablePurchasingDocumentLinkIdentifier());
        setReceivingDocumentRequiredIndicator(requisitionDocument.isReceivingDocumentRequiredIndicator());
        setPaymentRequestPositiveApprovalIndicator(
                requisitionDocument.isPaymentRequestPositiveApprovalIndicator());

        setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_IN_PROCESS);
        setAccountDistributionMethod(requisitionDocument.getAccountDistributionMethod());
        // Copy items from requisition (which will copy the item's accounts and capital assets)
        final List<PurchaseOrderItem> items = new ArrayList<>();
        for (final PurApItem reqItem : ((PurchasingAccountsPayableDocument) requisitionDocument).getItems()) {
            final RequisitionCapitalAssetItem reqCamsItem = (RequisitionCapitalAssetItem) requisitionDocument
                    .getPurchasingCapitalAssetItemByItemIdentifier(reqItem.getItemIdentifier());
            items.add(new PurchaseOrderItem((RequisitionItem) reqItem, this, reqCamsItem));
        }
        setItems(items);

        // Copy capital asset information that is directly off the document.
        setCapitalAssetSystemTypeCode(requisitionDocument.getCapitalAssetSystemTypeCode());
        setCapitalAssetSystemStateCode(requisitionDocument.getCapitalAssetSystemStateCode());
        for (final CapitalAssetSystem capitalAssetSystem : requisitionDocument.getPurchasingCapitalAssetSystems()) {
            getPurchasingCapitalAssetSystems().add(new PurchaseOrderCapitalAssetSystem(capitalAssetSystem));
        }

        fixItemReferences();
		// KFSUPGRADE-346/KITI-727
        if (ObjectUtils.isNull(getPurapDocumentIdentifier())) {
            // need retrieve the next available PO id to save in GL entries (only do if purap id is null which should be on first
            // save)
            SequenceAccessorService sas = SpringContext.getBean(SequenceAccessorService.class);
            Long poSequenceNumber = sas.getNextAvailableSequenceNumber("PO_ID", this.getClass());
            setPurapDocumentIdentifier(poSequenceNumber.intValue());
        }
		// KFSUPGRADE-337/KITI-2414 : checking for duplicate sensitive data types
        Set<String> currentSensitiveDataCodes = new HashSet<String>();
        if (purchaseOrderSensitiveData != null) {
        	for (PurchaseOrderSensitiveData sensitiveData : purchaseOrderSensitiveData) {
        		currentSensitiveDataCodes.add(sensitiveData.getSensitiveDataCode());
        	}
        }
        if (requisitionDocument.isSensitive()) {
        	for ( PurchasingItemBase pib : (List<PurchasingItemBase>)requisitionDocument.getItems()) {
        		CommodityCode cc = pib.getCommodityCode(); 
        		if (cc != null && cc.getSensitiveDataCode() != null && currentSensitiveDataCodes.add(cc.getSensitiveDataCode())) { 
        			if (purchaseOrderSensitiveData == null) {
        				boolean addNewData = true;
        				purchaseOrderSensitiveData = getPurchaseOrderSensitiveData();
        				for (PurchaseOrderSensitiveData sensitiveData : purchaseOrderSensitiveData) {
        					if (!currentSensitiveDataCodes.add(sensitiveData.getSensitiveDataCode())) {
        						addNewData = false;
        					}
        				}
        				if (addNewData) {
        					purchaseOrderSensitiveData.add(new PurchaseOrderSensitiveData(getPurapDocumentIdentifier(),getRequisitionIdentifier(), cc.getSensitiveDataCode()));
        				}
        			} else {
        				purchaseOrderSensitiveData.add(new PurchaseOrderSensitiveData(getPurapDocumentIdentifier(),getRequisitionIdentifier(), cc.getSensitiveDataCode()));
        			}
        		}
        	}
        	
        }
        
        if (purchaseOrderSensitiveData != null) {
        	SpringContext.getBean(BusinessObjectService.class).save(purchaseOrderSensitiveData);
        }
        
    }

    /**
     * Returns the Vendor Stipulation at the specified index in this Purchase Order.
     *
     * @param index the specified index.
     * @return the Vendor Stipulation at the specified index.
     */
    public PurchaseOrderVendorStipulation getPurchaseOrderVendorStipulation(final int index) {
        while (getPurchaseOrderVendorStipulations().size() <= index) {
            getPurchaseOrderVendorStipulations().add(new PurchaseOrderVendorStipulation());
        }
        return purchaseOrderVendorStipulations.get(index);
    }

    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        final List<String> docIdStrings = new ArrayList<>();
        docIdStrings.add(getDocumentNumber());
        final String currentDocumentTypeName = getDocumentHeader().getWorkflowDocument()
                .getDocumentTypeName();

        final List<PurchaseOrderView> relatedPoViews = getRelatedViews().getRelatedPurchaseOrderViews();
        for (final PurchaseOrderView poView : relatedPoViews) {
            //don't lock related PO's if this is a split PO that's in process
            if (!((PurchaseOrderStatuses.APPDOC_IN_PROCESS.equals(getApplicationDocumentStatus())
                    || PurchaseOrderStatuses.APPDOC_IN_PROCESS.equals(
                            getApplicationDocumentStatus()))
                    && PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_SPLIT_DOCUMENT.equals(
                            currentDocumentTypeName))) {
                docIdStrings.add(poView.getDocumentNumber());
            }
        }
        LOG.debug("***** getWorkflowEngineDocumentIdsToLock({}) = '{}'", documentNumber, docIdStrings);
        return docIdStrings;
    }

    @Override
    public void doRouteStatusChange(final DocumentRouteStatusChange statusChangeEvent) {
        LOG.debug("doRouteStatusChange() started");
        super.doRouteStatusChange(statusChangeEvent);

        final String currentDocumentTypeName = getDocumentHeader().getWorkflowDocument()
                .getDocumentTypeName();
        // child classes need to call super, but we don't want to inherit the post-processing done by this PO class
        // other than to the Split
        if (PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT.equals(currentDocumentTypeName)
                || PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_SPLIT_DOCUMENT.equals(currentDocumentTypeName)) {
            if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
                SpringContext.getBean(PurchaseOrderService.class).completePurchaseOrder(this);
                SpringContext.getBean(WorkflowDocumentService.class).saveRoutingData(
                        getDocumentHeader().getWorkflowDocument());
            } else if (getDocumentHeader().getWorkflowDocument().isDisapproved()) {
                // DOCUMENT DISAPPROVED
                final String nodeName = SpringContext.getBean(WorkflowDocumentService.class).getCurrentRouteLevelName(
                        getDocumentHeader().getWorkflowDocument());
                final String disapprovalStatus = findDisapprovalStatus(nodeName);

                if (ObjectUtils.isNotNull(disapprovalStatus)) {
                    //update the appDocStatus and save the workflow data
                    updateAndSaveAppDocStatus(disapprovalStatus);
                } else {
                    logAndThrowRuntimeException("No status found to set for document being disapproved in node '" +
                            nodeName + "'");
                }

            } else if (getDocumentHeader().getWorkflowDocument().isCanceled()) {
                // DOCUMENT CANCELED
                updateAndSaveAppDocStatus(PurchaseOrderStatuses.APPDOC_CANCELLED);
            }
        }

        if (shouldAdhocFyi()) {

            SpringContext.getBean(WorkflowDocumentService.class).saveRoutingData(
                    getDocumentHeader().getWorkflowDocument());
            SpringContext.getBean(PurchaseOrderService.class).sendAdhocFyi(this);
        }
    }

    protected String findDisapprovalStatus(final String nodeName) {
        return PurchaseOrderStatuses.getPurchaseOrderAppDocDisapproveStatuses().get(nodeName);
    }

    protected boolean shouldAdhocFyi() {
        Collection<String> excludeList = new ArrayList<>();
        if (SpringContext.getBean(ParameterService.class).parameterExists(PurchaseOrderDocument.class,
                PurapParameterConstants.PO_NOTIFY_EXCLUSIONS)) {
            excludeList = SpringContext.getBean(ParameterService.class).getParameterValuesAsString(
                    PurchaseOrderDocument.class, PurapParameterConstants.PO_NOTIFY_EXCLUSIONS);
        }
        if (getDocumentHeader().getWorkflowDocument().isDisapproved()
                || getDocumentHeader().getWorkflowDocument().isCanceled()) {
            return true;
        }

        return getDocumentHeader().getWorkflowDocument().isFinal()
                && !excludeList.contains(getRequisitionSourceCode())
                && !PurchaseOrderStatuses.APPDOC_PENDING_PRINT.equals(getApplicationDocumentStatus());
    }

    /**
     * Returns the name of the current route node.
     *
     * @param wd the current workflow document.
     * @return the name of the current route node.
     */
    protected String getCurrentRouteNodeName(final WorkflowDocument wd) {
        final ArrayList<String> nodeNames = new ArrayList(wd.getCurrentNodeNames());
        if (nodeNames.size() == 0) {
            return null;
        } else {
            return nodeNames.get(0);
        }
    }

    /**
     * Sends FYI workflow request to the given user on this document.
     *
     * @param workflowDocument the associated workflow document.
     * @param routePrincipalId the network ID of the user to be sent to.
     * @param annotation       the annotation notes contained in this document.
     * @param responsibility   the responsibility specified in the request.
     */
    public void appSpecificRouteDocumentToUser(
            final WorkflowDocument workflowDocument, final String routePrincipalId,
            final String annotation, final String responsibility) {
        if (ObjectUtils.isNotNull(workflowDocument)) {
            final boolean isActiveUser = isActiveUser(routePrincipalId);
            final Map<String, String> permissionDetails = new HashMap<>();
            permissionDetails.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME,
                    workflowDocument.getDocumentTypeName());
            permissionDetails.put(KimConstants.AttributeConstants.ACTION_REQUEST_CD,
                    KewApiConstants.ACTION_REQUEST_FYI_REQ);
            final boolean canReceiveAdHocRequest = KimApiServiceLocator.getPermissionService()
                    .isAuthorizedByTemplate(routePrincipalId, KFSConstants.CoreModuleNamespaces.WORKFLOW,
                            KewApiConstants.AD_HOC_REVIEW_PERMISSION, permissionDetails, new HashMap<>());
            if (!isActiveUser || !canReceiveAdHocRequest) {
                final String principalName = SpringContext.getBean(PersonService.class).getPerson(
                        routePrincipalId).getName();
                final String errorText = "cannot send FYI to the user: " + principalName + "; Annotation: " + annotation;
                LOG.info(errorText);
                final Note note = SpringContext.getBean(DocumentService.class).createNoteFromDocument(this, errorText);
                addNote(SpringContext.getBean(NoteService.class).save(note));
            } else {
                final String annotationNote = ObjectUtils.isNull(annotation) ? "" : annotation;
                final String responsibilityNote = ObjectUtils.isNull(responsibility) ? "" : responsibility;
                final String currentNodeName = getCurrentRouteNodeName(workflowDocument);
                workflowDocument.adHocToPrincipal(ActionRequestType.FYI, currentNodeName, annotationNote,
                        routePrincipalId, responsibilityNote, true);
            }
        }
    }

    protected boolean isActiveUser(final String principalId) {
        final Person principal = KimApiServiceLocator.getPersonService().getPerson(principalId);
        return ObjectUtils.isNotNull(principal) && principal.isActive();
    }

    @Override
    public void doRouteLevelChange(final DocumentRouteLevelChange levelChangeEvent) {
        LOG.debug("handleRouteLevelChange() started");
        super.doRouteLevelChange(levelChangeEvent);
    }

    /**
     * @return the list of all active items in this Purchase Order.
     */
    public List getItemsActiveOnly() {
        final List<PurchaseOrderItem> returnList = new ArrayList<>();
        for (final Object anItem : getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) anItem;
            if (item.isItemActiveIndicator()) {
                returnList.add(item);
            }
        }
        return returnList;
    }

    /**
     * Gets the active items in this Purchase Order, and sets up the alternate amount for GL entry creation.
     *
     * @return the list of all active items in this Purchase Order.
     */
    public List getItemsActiveOnlySetupAlternateAmount() {
        final List<PurchaseOrderItem> returnList = new ArrayList<>();
        for (final Object anItem : getItems()) {
            final PurchaseOrderItem item = (PurchaseOrderItem) anItem;
            if (item.isItemActiveIndicator()) {
                for (final PurApAccountingLine purApAccountingLine : item.getSourceAccountingLines()) {
                    final PurchaseOrderAccount account = (PurchaseOrderAccount) purApAccountingLine;
                    account.setAlternateAmountForGLEntryCreation(
                            account.getItemAccountOutstandingEncumbranceAmount());
                }
                returnList.add(item);
            }
        }
        return returnList;
    }

    public Integer getAlternateVendorDetailAssignedIdentifier() {
        return alternateVendorDetailAssignedIdentifier;
    }

    public void setAlternateVendorDetailAssignedIdentifier(final Integer alternateVendorDetailAssignedIdentifier) {
        this.alternateVendorDetailAssignedIdentifier = alternateVendorDetailAssignedIdentifier;
    }

    public Integer getAlternateVendorHeaderGeneratedIdentifier() {
        return alternateVendorHeaderGeneratedIdentifier;
    }

    public void setAlternateVendorHeaderGeneratedIdentifier(final Integer alternateVendorHeaderGeneratedIdentifier) {
        this.alternateVendorHeaderGeneratedIdentifier = alternateVendorHeaderGeneratedIdentifier;
    }

    public String getAlternateVendorName() {
        return alternateVendorName;
    }

    public void setAlternateVendorName(final String alternateVendorName) {
        this.alternateVendorName = alternateVendorName;
    }

    public KualiDecimal getFinalPaymentAmount() {
        return finalPaymentAmount;
    }

    public void setFinalPaymentAmount(final KualiDecimal finalPaymentAmount) {
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public Date getFinalPaymentDate() {
        return finalPaymentDate;
    }

    public void setFinalPaymentDate(final Date finalPaymentDate) {
        this.finalPaymentDate = finalPaymentDate;
    }

    public KualiDecimal getInitialPaymentAmount() {
        return initialPaymentAmount;
    }

    public void setInitialPaymentAmount(final KualiDecimal initialPaymentAmount) {
        this.initialPaymentAmount = initialPaymentAmount;
    }

    public Date getInitialPaymentDate() {
        return initialPaymentDate;
    }

    public void setInitialPaymentDate(final Date initialPaymentDate) {
        this.initialPaymentDate = initialPaymentDate;
    }

    public String getPurchaseOrderCommodityDescription() {
        return purchaseOrderCommodityDescription;
    }

    public void setPurchaseOrderCommodityDescription(final String purchaseOrderCommodityDescription) {
        this.purchaseOrderCommodityDescription = purchaseOrderCommodityDescription;
    }

    public boolean isPurchaseOrderConfirmedIndicator() {
        return purchaseOrderConfirmedIndicator;
    }

    public void setPurchaseOrderConfirmedIndicator(final boolean purchaseOrderConfirmedIndicator) {
        this.purchaseOrderConfirmedIndicator = purchaseOrderConfirmedIndicator;
    }

    public Timestamp getPurchaseOrderCreateTimestamp() {
        return purchaseOrderCreateTimestamp;
    }

    public void setPurchaseOrderCreateTimestamp(final Timestamp purchaseOrderCreateTimestamp) {
        this.purchaseOrderCreateTimestamp = purchaseOrderCreateTimestamp;
    }

    public Timestamp getPurchaseOrderInitialOpenTimestamp() {
        return purchaseOrderInitialOpenTimestamp;
    }

    public void setPurchaseOrderInitialOpenTimestamp(final Timestamp purchaseOrderInitialOpenDate) {
        purchaseOrderInitialOpenTimestamp = purchaseOrderInitialOpenDate;
    }

    public Timestamp getPurchaseOrderLastTransmitTimestamp() {
        return purchaseOrderLastTransmitTimestamp;
    }

    public void setPurchaseOrderLastTransmitTimestamp(final Timestamp PurchaseOrderLastTransmitTimestamp) {
        purchaseOrderLastTransmitTimestamp = PurchaseOrderLastTransmitTimestamp;
    }

    public Integer getPurchaseOrderPreviousIdentifier() {
        return purchaseOrderPreviousIdentifier;
    }

    public void setPurchaseOrderPreviousIdentifier(final Integer purchaseOrderPreviousIdentifier) {
        this.purchaseOrderPreviousIdentifier = purchaseOrderPreviousIdentifier;
    }

    public Date getPurchaseOrderQuoteDueDate() {
        return purchaseOrderQuoteDueDate;
    }

    public void setPurchaseOrderQuoteDueDate(final Date purchaseOrderQuoteDueDate) {
        this.purchaseOrderQuoteDueDate = purchaseOrderQuoteDueDate;
    }

    public String getPurchaseOrderQuoteTypeDescription() {
        String description = purchaseOrderQuoteTypeCode;
        if (PurapConstants.QuoteTypes.COMPETITIVE.equals(purchaseOrderQuoteTypeCode)) {
            description = QuoteTypeDescriptions.COMPETITIVE;
        } else if (PurapConstants.QuoteTypes.PRICE_CONFIRMATION.equals(purchaseOrderQuoteTypeCode)) {
            description = QuoteTypeDescriptions.PRICE_CONFIRMATION;
        }
        return description;
    }

    public String getPurchaseOrderQuoteTypeCode() {
        return purchaseOrderQuoteTypeCode;
    }

    public void setPurchaseOrderQuoteTypeCode(final String purchaseOrderQuoteTypeCode) {
        this.purchaseOrderQuoteTypeCode = purchaseOrderQuoteTypeCode;
    }

    public String getPurchaseOrderQuoteVendorNoteText() {
        return purchaseOrderQuoteVendorNoteText;
    }

    public void setPurchaseOrderQuoteVendorNoteText(final String purchaseOrderQuoteVendorNoteText) {
        this.purchaseOrderQuoteVendorNoteText = purchaseOrderQuoteVendorNoteText;
    }

    public String getPurchaseOrderVendorChoiceCode() {
        return purchaseOrderVendorChoiceCode;
    }

    public void setPurchaseOrderVendorChoiceCode(final String purchaseOrderVendorChoiceCode) {
        this.purchaseOrderVendorChoiceCode = purchaseOrderVendorChoiceCode;
    }

    public KualiDecimal getRecurringPaymentAmount() {
        return recurringPaymentAmount;
    }

    public void setRecurringPaymentAmount(final KualiDecimal recurringPaymentAmount) {
        this.recurringPaymentAmount = recurringPaymentAmount;
    }

    public Date getRecurringPaymentDate() {
        return recurringPaymentDate;
    }

    public void setRecurringPaymentDate(final Date recurringPaymentDate) {
        this.recurringPaymentDate = recurringPaymentDate;
    }

    public String getRecurringPaymentFrequencyCode() {
        return recurringPaymentFrequencyCode;
    }

    public void setRecurringPaymentFrequencyCode(final String recurringPaymentFrequencyCode) {
        this.recurringPaymentFrequencyCode = recurringPaymentFrequencyCode;
    }

    public Integer getRequisitionIdentifier() {
        return requisitionIdentifier;
    }

    public void setRequisitionIdentifier(final Integer requisitionIdentifier) {
        this.requisitionIdentifier = requisitionIdentifier;
    }

    public PurchaseOrderVendorChoice getPurchaseOrderVendorChoice() {
        return purchaseOrderVendorChoice;
    }

    public void setPurchaseOrderVendorChoice(final PurchaseOrderVendorChoice purchaseOrderVendorChoice) {
        this.purchaseOrderVendorChoice = purchaseOrderVendorChoice;
    }

    public RecurringPaymentFrequency getRecurringPaymentFrequency() {
        return recurringPaymentFrequency;
    }

    public void setRecurringPaymentFrequency(final RecurringPaymentFrequency recurringPaymentFrequency) {
        this.recurringPaymentFrequency = recurringPaymentFrequency;
    }

    public PaymentTermType getVendorPaymentTerms() {
        return vendorPaymentTerms;
    }

    public void setVendorPaymentTerms(final PaymentTermType vendorPaymentTerms) {
        this.vendorPaymentTerms = vendorPaymentTerms;
    }

    public ShippingPaymentTerms getVendorShippingPaymentTerms() {
        return vendorShippingPaymentTerms;
    }

    public void setVendorShippingPaymentTerms(final ShippingPaymentTerms vendorShippingPaymentTerms) {
        this.vendorShippingPaymentTerms = vendorShippingPaymentTerms;
    }

    public ShippingTitle getVendorShippingTitle() {
        if (ObjectUtils.isNull(vendorShippingTitle)) {
            refreshReferenceObject("vendorShippingTitle");
        }

        return vendorShippingTitle;
    }

    public void setVendorShippingTitle(final ShippingTitle vendorShippingTitle) {
        this.vendorShippingTitle = vendorShippingTitle;
    }

    public List getPurchaseOrderVendorStipulations() {
        return purchaseOrderVendorStipulations;
    }

    public String getStatusChange() {
        return statusChange;
    }

    public void setPurchaseOrderVendorStipulations(final List purchaseOrderVendorStipulations) {
        this.purchaseOrderVendorStipulations = purchaseOrderVendorStipulations;
    }

    public List<PurchaseOrderVendorQuote> getPurchaseOrderVendorQuotes() {
        return purchaseOrderVendorQuotes;
    }

    public void setPurchaseOrderVendorQuotes(final List<PurchaseOrderVendorQuote> purchaseOrderVendorQuotes) {
        this.purchaseOrderVendorQuotes = purchaseOrderVendorQuotes;
    }

    public PurchaseOrderVendorQuote getPurchaseOrderVendorQuote(final int index) {
        while (getPurchaseOrderVendorQuotes().size() <= index) {
            getPurchaseOrderVendorQuotes().add(new PurchaseOrderVendorQuote());
        }
        return purchaseOrderVendorQuotes.get(index);
    }

    public void setStatusChange(final String statusChange) {
        this.statusChange = statusChange;
    }

    public String getPurchaseOrderRetransmissionMethodCode() {
        return purchaseOrderRetransmissionMethodCode;
    }

    public void setPurchaseOrderRetransmissionMethodCode(final String purchaseOrderRetransmissionMethodCode) {
        this.purchaseOrderRetransmissionMethodCode = purchaseOrderRetransmissionMethodCode;
    }

    public String getRetransmitHeader() {
        return retransmitHeader;
    }

    public void setRetransmitHeader(final String retransmitHeader) {
        this.retransmitHeader = retransmitHeader;
    }

    public boolean isPendingActionIndicator() {
        return pendingActionIndicator;
    }

    public void setPendingActionIndicator(final boolean pendingActionIndicator) {
        this.pendingActionIndicator = pendingActionIndicator;
    }

    public boolean isPurchaseOrderCurrentIndicator() {
        return purchaseOrderCurrentIndicator;
    }

    public void setPurchaseOrderCurrentIndicator(final boolean purchaseOrderCurrentIndicator) {
        this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
    }

    public Timestamp getPurchaseOrderFirstTransmissionTimestamp() {
        return purchaseOrderFirstTransmissionTimestamp;
    }

    public void setPurchaseOrderFirstTransmissionTimestamp(final Timestamp purchaseOrderFirstTransmissionTimestamp) {
        this.purchaseOrderFirstTransmissionTimestamp = purchaseOrderFirstTransmissionTimestamp;
    }

    public Date getPurchaseOrderQuoteAwardedDate() {
        return purchaseOrderQuoteAwardedDate;
    }

    public void setPurchaseOrderQuoteAwardedDate(final Date purchaseOrderQuoteAwardedDate) {
        this.purchaseOrderQuoteAwardedDate = purchaseOrderQuoteAwardedDate;
    }

    public Date getPurchaseOrderQuoteInitializationDate() {
        return purchaseOrderQuoteInitializationDate;
    }

    public void setPurchaseOrderQuoteInitializationDate(final Date purchaseOrderQuoteInitializationDate) {
        this.purchaseOrderQuoteInitializationDate = purchaseOrderQuoteInitializationDate;
    }

    public String getAlternateVendorNumber() {
        String hdrGenId = "";
        String detAssgndId = "";
        String vendorNumber = "";
        if (alternateVendorHeaderGeneratedIdentifier != null) {
            hdrGenId = alternateVendorHeaderGeneratedIdentifier.toString();
        }
        if (alternateVendorDetailAssignedIdentifier != null) {
            detAssgndId = alternateVendorDetailAssignedIdentifier.toString();
        }
        if (StringUtils.isNotEmpty(hdrGenId) && StringUtils.isNotEmpty(detAssgndId)) {
            vendorNumber = hdrGenId + VendorConstants.DASH + detAssgndId;
        }
        return vendorNumber;
    }

    public void setAlternateVendorNumber(final String vendorNumber) {
        if (StringUtils.isNotEmpty(vendorNumber)) {
            final int dashInd = vendorNumber.indexOf(VendorConstants.DASH);
            if (vendorNumber.length() >= dashInd) {
                final String vndrHdrGenId = vendorNumber.substring(0, dashInd);
                final String vndrDetailAssignedId = vendorNumber.substring(dashInd + 1);
                if (StringUtils.isNotEmpty(vndrHdrGenId) && StringUtils.isNotEmpty(vndrDetailAssignedId)) {
                    alternateVendorHeaderGeneratedIdentifier = Integer.valueOf(vndrHdrGenId);
                    alternateVendorDetailAssignedIdentifier = Integer.valueOf(vndrDetailAssignedId);
                }
            }
        } else {
            alternateVendorNumber = vendorNumber;
        }
    }

    public void templateAlternateVendor(final VendorDetail vendorDetail) {
        if (vendorDetail == null) {
            return;
        }
        setAlternateVendorNumber(vendorDetail.getVendorHeaderGeneratedIdentifier() + VendorConstants.DASH +
                vendorDetail.getVendorDetailAssignedIdentifier());
        setAlternateVendorName(vendorDetail.getVendorName());
    }

    @Override
    public Class getItemClass() {
        return PurchaseOrderItem.class;
    }

    @Override
    public Class getItemUseTaxClass() {
        return PurchaseOrderItemUseTax.class;
    }

    @Override
    public RequisitionDocument getPurApSourceDocumentIfPossible() {
        RequisitionDocument sourceDoc = null;
        if (ObjectUtils.isNotNull(getRequisitionIdentifier())) {
            sourceDoc = SpringContext.getBean(RequisitionService.class).getRequisitionById(getRequisitionIdentifier());
        }
        return sourceDoc;
    }

    @Override
    public String getPurApSourceDocumentLabelIfPossible() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentLabelByTypeName(
                PurapConstants.PurapDocTypeCodes.REQUISITION_DOCUMENT_TYPE);
    }

    public Integer getNewQuoteVendorDetailAssignedIdentifier() {
        return newQuoteVendorDetailAssignedIdentifier;
    }

    public void setNewQuoteVendorDetailAssignedIdentifier(final Integer newQuoteVendorDetailAssignedIdentifier) {
        this.newQuoteVendorDetailAssignedIdentifier = newQuoteVendorDetailAssignedIdentifier;
    }

    public Integer getNewQuoteVendorHeaderGeneratedIdentifier() {
        return newQuoteVendorHeaderGeneratedIdentifier;
    }

    public void setNewQuoteVendorHeaderGeneratedIdentifier(final Integer newQuoteVendorHeaderGeneratedIdentifier) {
        this.newQuoteVendorHeaderGeneratedIdentifier = newQuoteVendorHeaderGeneratedIdentifier;
    }

    public Integer getPurchaseOrderQuoteListIdentifier() {
        return purchaseOrderQuoteListIdentifier;
    }

    public void setPurchaseOrderQuoteListIdentifier(final Integer purchaseOrderQuoteListIdentifier) {
        this.purchaseOrderQuoteListIdentifier = purchaseOrderQuoteListIdentifier;
    }

    /**
     * @return true if a vendor has been awarded for this Purchase Order.
     */
    public boolean isPurchaseOrderAwarded() {
        return getAwardedVendorQuote() != null;
    }

    /**
     * @return the quote from the awarded vendor.
     */
    public PurchaseOrderVendorQuote getAwardedVendorQuote() {
        for (final PurchaseOrderVendorQuote vendorQuote : purchaseOrderVendorQuotes) {
            if (vendorQuote.getPurchaseOrderQuoteAwardTimestamp() != null) {
                return vendorQuote;
            }
        }
        return null;
    }

    @Override
    public KualiDecimal getTotalDollarAmount() {
        // return total without inactive and with below the line
        return getTotalDollarAmount(false, true);
    }

    /**
     * Gets the total dollar amount for this Purchase Order.
     *
     * @param includeInactive     indicates whether inactive items shall be included.
     * @param includeBelowTheLine indicates whether below the line items shall be included.
     * @return the total dollar amount for this Purchase Order.
     */
    public KualiDecimal getTotalDollarAmount(final boolean includeInactive, final boolean includeBelowTheLine) {
        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (final PurApItem item : (List<PurApItem>) getItems()) {

            if (item.getPurapDocument() == null) {
                item.setPurapDocument(this);
            }
            final ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator()) && (includeInactive
                    || PurApItemUtils.checkItemActive(item))) {
                final KualiDecimal totalAmount = item.getTotalAmount();
                final KualiDecimal itemTotal = totalAmount != null ? totalAmount : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public KualiDecimal getTotalDollarAmountAboveLineItems() {
        return getTotalDollarAmount(false, false);
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmount() {
        // return total without inactive and with below the line
        return getTotalPreTaxDollarAmount(false, true);
    }

    /**
     * Gets the pre tax total dollar amount for this Purchase Order.
     *
     * @param includeInactive     indicates whether inactive items shall be included.
     * @param includeBelowTheLine indicates whether below the line items shall be included.
     * @return the total dollar amount for this Purchase Order.
     */
    public KualiDecimal getTotalPreTaxDollarAmount(final boolean includeInactive, final boolean includeBelowTheLine) {
        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (final PurchaseOrderItem item : (List<PurchaseOrderItem>) getItems()) {
            final ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator())
                    && (includeInactive || item.isItemActiveIndicator())) {
                final KualiDecimal extendedPrice = item.getExtendedPrice();
                final KualiDecimal itemTotal = extendedPrice != null ? extendedPrice : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public KualiDecimal getTotalPreTaxDollarAmountAboveLineItems() {
        return getTotalPreTaxDollarAmount(false, false);
    }

    @Override
    public KualiDecimal getTotalTaxAmount() {
        // return total without inactive and with below the line
        return getTotalTaxAmount(false, true);
    }

    /**
     * Gets the tax total amount for this Purchase Order.
     *
     * @param includeInactive     indicates whether inactive items shall be included.
     * @param includeBelowTheLine indicates whether below the line items shall be included.
     * @return the total dollar amount for this Purchase Order.
     */
    public KualiDecimal getTotalTaxAmount(final boolean includeInactive, final boolean includeBelowTheLine) {
        KualiDecimal total = new KualiDecimal(BigDecimal.ZERO);
        for (final PurchaseOrderItem item : (List<PurchaseOrderItem>) getItems()) {
            final ItemType it = item.getItemType();
            if ((includeBelowTheLine || it.isLineItemIndicator())
                    && (includeInactive || item.isItemActiveIndicator())) {
                final KualiDecimal taxAmount = item.getItemTaxAmount();
                final KualiDecimal itemTotal = taxAmount != null ? taxAmount : KualiDecimal.ZERO;
                total = total.add(itemTotal);
            }
        }
        return total;
    }

    @Override
    public KualiDecimal getTotalTaxAmountAboveLineItems() {
        return getTotalTaxAmount(false, false);
    }

    /**
     * @return true if this Purchase Order contains unpaid items in the Payment Request or Credit Memo.
     */
    public boolean getContainsUnpaidPaymentRequestsOrCreditMemos() {
        if (getRelatedViews().getRelatedPaymentRequestViews() != null) {
            for (final PaymentRequestView element : getRelatedViews().getRelatedPaymentRequestViews()) {
                // If the PREQ is neither cancelled nor voided, check whether the PREQ has been paid.
                // If it has not been paid, then this method will return true.
                if (!PaymentRequestStatuses.CANCELLED_STATUSES.contains(
                        element.getApplicationDocumentStatus())) {
                    if (element.getPaymentPaidTimestamp() == null) {
                        return true;
                    }
                }
            }
        }
        if (getRelatedViews().getRelatedCreditMemoViews() != null) {
            for (final CreditMemoView element : getRelatedViews().getRelatedCreditMemoViews()) {
                // If the CM is cancelled, check whether the CM has been paid.
                // If it has not been paid, then this method will return true.
                if (!CreditMemoStatuses.CANCELLED_STATUSES.contains(element.getApplicationDocumentStatus())) {
                    if (element.getCreditMemoPaidTimestamp() == null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean getAdditionalChargesExist() {
        final List<PurchaseOrderItem> items = getItems();
        for (final PurchaseOrderItem item : items) {
            if (item != null && item.getItemType() != null && item.getItemType().isAdditionalChargeIndicator()
                    && item.getExtendedPrice() != null && !KualiDecimal.ZERO.equals(item.getExtendedPrice())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used for routing only.
     */
    @Deprecated
    public String getContractManagerName() {
        return "";
    }

    /**
     * Used for routing only.
     */
    @Deprecated
    public void setContractManagerName(final String contractManagerName) {
    }

    public KualiDecimal getInternalPurchasingLimit() {
        // FIXME need the following because at places this field remains null because contract manager is not
        // refreshed and null

        if (internalPurchasingLimit == null) {
            setInternalPurchasingLimit(SpringContext.getBean(PurchaseOrderService.class)
                    .getInternalPurchasingDollarLimit(this));
        }
        return internalPurchasingLimit;
    }

    public void setInternalPurchasingLimit(final KualiDecimal internalPurchasingLimit) {
        this.internalPurchasingLimit = internalPurchasingLimit;
    }

    public boolean isPendingSplit() {
        return pendingSplit;
    }

    public void setPendingSplit(final boolean pendingSplit) {
        this.pendingSplit = pendingSplit;
    }

    public boolean isCopyingNotesWhenSplitting() {
        return copyingNotesWhenSplitting;
    }

    public void setCopyingNotesWhenSplitting(final boolean copyingNotesWhenSplitting) {
        this.copyingNotesWhenSplitting = copyingNotesWhenSplitting;
    }

    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(
            final GeneralLedgerPendingEntrySourceDetail postable,
            final GeneralLedgerPendingEntry explicitEntry) {
        super.customizeExplicitGeneralLedgerPendingEntry(postable, explicitEntry);

        SpringContext.getBean(PurapGeneralLedgerService.class).customizeGeneralLedgerPendingEntry(this,
                (AccountingLine) postable, explicitEntry, getPurapDocumentIdentifier(), KFSConstants.GL_DEBIT_CODE,
                PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT, true);

        final KualiDecimal accountTotalGLEntryAmount;

        // KFSMI-9842: if the entry's financial document type is POA (or POC or POR - KFSMI-9879) then generate GLPEs
        // only for the updated amount or new items, not for the entire items' accounts.
        if (PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_AMENDMENT_DOCUMENT.equals(
                explicitEntry.getFinancialDocumentTypeCode())
                || PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_CLOSE_DOCUMENT.equals(
                        explicitEntry.getFinancialDocumentTypeCode())
                || PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_REOPEN_DOCUMENT.equals(
                        explicitEntry.getFinancialDocumentTypeCode())) {
            accountTotalGLEntryAmount = explicitEntry.getTransactionLedgerEntryAmount();
        } else {
            accountTotalGLEntryAmount = getAccountTotalGLEntryAmount((AccountingLine) postable);
        }

        explicitEntry.setTransactionLedgerEntryAmount(accountTotalGLEntryAmount);
        handleNegativeEntryAmount(explicitEntry);

        // don't think i should have to override this, but default isn't getting the right PO doc
        explicitEntry.setFinancialDocumentTypeCode(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_DOCUMENT);
    }

    protected void handleNegativeEntryAmount(final GeneralLedgerPendingEntry explicitEntry) {
        if (explicitEntry.getTransactionLedgerEntryAmount().doubleValue() < 0) {
            explicitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            explicitEntry.setTransactionLedgerEntryAmount(explicitEntry.getTransactionLedgerEntryAmount().abs());
        } else {
            explicitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }
    }

    @Override
    public Class getPurchasingCapitalAssetItemClass() {
        return PurchaseOrderCapitalAssetItem.class;
    }

    @Override
    public Class getPurchasingCapitalAssetSystemClass() {
        return PurchaseOrderCapitalAssetSystem.class;
    }

    /**
     * Validates whether we can indeed close the PO. Return false and give error if
     * the outstanding encumbrance amount of the trade in item is less than 0.
     *
     * @return
     */
    public boolean canClosePOForTradeIn() {
        for (final PurchaseOrderItem item : (List<PurchaseOrderItem>) getItems()) {
            if (item.getItemTypeCode().equals(PurapConstants.ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)
                    && item.getItemOutstandingEncumberedAmount().isLessThan(new KualiDecimal(0))) {
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERROR_PROPERTY,
                        PurapKeyConstants.ERROR_ITEM_TRADE_IN_OUTSTANDING_ENCUMBERED_AMOUNT_NEGATIVE, "amend the PO");
                return false;
            }
        }
        return true;
    }

    /**
     * Provides answers to the following splits:
     * RequiresContractManagementReview
     * RequiresBudgetReview
     * VendorIsEmployeeOrNonresident
     * TransmissionMethodIsPrint
     */
    @Override
    public boolean answerSplitNodeQuestion(final String nodeName) throws UnsupportedOperationException {
        if (nodeName.equals(PurapWorkflowConstants.CONTRACT_MANAGEMENT_REVIEW_REQUIRED)) {
            return isContractManagementReviewRequired();
        }
        if (nodeName.equals(PurapWorkflowConstants.AWARD_REVIEW_REQUIRED)) {
            return isAwardReviewRequired();
        }
        if (nodeName.equals(PurapWorkflowConstants.BUDGET_REVIEW_REQUIRED)) {
            return isBudgetReviewRequired();
        }
        if (nodeName.equals(PurapWorkflowConstants.VENDOR_IS_EMPLOYEE_OR_NONRESIDENT) ||
                "VendorIsEmployeeOrNonResidentAlien".equals(nodeName)) {
            return isVendorEmployeeOrNonresident();
        }
        return super.answerSplitNodeQuestion(nodeName);
    }

    protected boolean isContractManagementReviewRequired() {
        final KualiDecimal internalPurchasingLimit = SpringContext.getBean(PurchaseOrderService.class)
                .getInternalPurchasingDollarLimit(this);
        return ObjectUtils.isNull(internalPurchasingLimit)
                || internalPurchasingLimit.compareTo(getTotalDollarAmount()) < 0;

    }

    protected boolean isAwardReviewRequired() {
        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        boolean objectCodeAllowed = true;

        for (final PurApItem item : (List<PurApItem>) getItems()) {
            for (final PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {

                objectCodeAllowed = isObjectCodeAllowedForAwardRouting(accountingLine, parameterService);
                // We should return true as soon as we have at least one objectCodeAllowed=true so that the PO will
                // stop at Award level.
                if (objectCodeAllowed) {
                    return objectCodeAllowed;
                }
            }
        }
        return objectCodeAllowed;
    }

    protected boolean isObjectCodeAllowedForAwardRouting(
            final PurApAccountingLine accountingLine,
            final ParameterService parameterService) {
        if (ObjectUtils.isNull(accountingLine.getObjectCode())) {
            return false;
        }

        // make sure object code is active
        if (!accountingLine.getObjectCode().isFinancialObjectActiveCode()) {
            return false;
        }

        final String chartCode = accountingLine.getChartOfAccountsCode();
        // check object level is in permitted list for award routing
        boolean objectCodeAllowed = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class)
                .getParameterEvaluator(PurchaseOrderDocument.class,
                        PurapParameterConstants.CG_ROUTE_OBJECT_LEVELS_BY_CHART,
                        PurapParameterConstants.NO_CG_ROUTE_OBJECT_LEVELS_BY_CHART, chartCode,
                        accountingLine.getObjectCode().getFinancialObjectLevelCode()).evaluationSucceeds();

        if (!objectCodeAllowed) {
            // If the object level is not permitting for award routing, then we need to also
            // check object code is in permitted list for award routing
            objectCodeAllowed = /*REFACTORME*/SpringContext.getBean(ParameterEvaluatorService.class).getParameterEvaluator(
                    PurchaseOrderDocument.class, PurapParameterConstants.CG_ROUTE_OBJECT_CODES_BY_CHART,
                    PurapParameterConstants.NO_CG_ROUTE_OBJECT_CODES_BY_CHART, chartCode,
                    accountingLine.getFinancialObjectCode()).evaluationSucceeds();
        }
        return objectCodeAllowed;
    }

    protected boolean isBudgetReviewRequired() {
        // if document's fiscal year is less than or equal to the current fiscal year
        if (SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear().compareTo(getPostingYear()) >= 0) {
            final List<SufficientFundsItem> fundsItems = SpringContext.getBean(SufficientFundsService.class)
                    .checkSufficientFunds(getPendingLedgerEntriesForSufficientFundsChecking());

            return fundsItems != null && fundsItems.size() > 0;
        }

        return false;
    }

    protected boolean isVendorEmployeeOrNonresident() {
        if (ObjectUtils.isNull(getVendorHeaderGeneratedIdentifier())) {
            // no vendor header id so can't check for proper tax routing
            return false;
        }
        final String vendorHeaderGeneratedId = getVendorHeaderGeneratedIdentifier().toString();
        final VendorService vendorService = SpringContext.getBean(VendorService.class);
        final boolean routeDocumentAsEmployeeVendor = vendorService.isVendorInstitutionEmployee(
                Integer.valueOf(vendorHeaderGeneratedId));
        final boolean routeDocumentAsForeignVendor = vendorService.isVendorForeign(Integer.valueOf(vendorHeaderGeneratedId));
        return routeDocumentAsEmployeeVendor || routeDocumentAsForeignVendor;
    }

    public List<Account> getAccountsForAwardRouting() {
        final List<Account> accounts = new ArrayList<>();

        final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        for (final PurApItem item : (List<PurApItem>) getItems()) {
            for (final PurApAccountingLine accountingLine : item.getSourceAccountingLines()) {
                if (isObjectCodeAllowedForAwardRouting(accountingLine, parameterService)) {
                    if (ObjectUtils.isNull(accountingLine.getAccount())) {
                        accountingLine.refreshReferenceObject("account");
                    }
                    if (accountingLine.getAccount() != null && !accounts.contains(accountingLine.getAccount())) {
                        accounts.add(accountingLine.getAccount());
                    }
                }
            }
        }
        return accounts;
    }

    @Override
    public DocumentSearchCriteria convertSelections(final DocumentSearchCriteria searchCriteria) {
        return searchCriteria;
    }

    /**
     * @return the purchase order current indicator
     */
    public boolean getPurchaseOrderCurrentIndicatorForSearching() {
        return purchaseOrderCurrentIndicator;
    }

    public String getDocumentTitleForResult() {
        return KEWServiceLocator.getDocumentTypeService().getDocumentTypeByName(
                getDocumentHeader().getWorkflowDocument().getDocumentTypeName()).getLabel();
    }

    /**
     * @return true if the purchase order needs a warning; false otherwise.
     */
    public boolean getNeedWarning() {
        return getPurchaseOrderInitialOpenTimestamp() == null;
    }

    public List<SourceAccountingLine> getGlOnlySourceAccountingLines() {
        return glOnlySourceAccountingLines;
    }

    public void setGlOnlySourceAccountingLines(final List<SourceAccountingLine> glOnlySourceAccountingLines) {
        this.glOnlySourceAccountingLines = glOnlySourceAccountingLines;
    }

    @Override
    public PersistableBusinessObject getNoteTarget() {
        final PurchaseOrderDao purchaseOrderDao = SpringContext.getBean(PurchaseOrderDao.class);
        final DocumentDao docDao = KRADServiceLocatorInternal.getDocumentDao();

        final PurchaseOrderDocument oldest = docDao.findByDocumentHeaderId(PurchaseOrderDocument.class,
                purchaseOrderDao.getOldestPurchaseOrderDocumentNumber(getPurapDocumentIdentifier()));

        //KFSMI-9746: added this for null safe checking.
        // CU Customization: Use the document itself as the note target, instead of its document header.
        return Objects.requireNonNullElse(oldest, this);

    }

    @Override
    public NoteType getNoteType() {
        return NoteType.BUSINESS_OBJECT;
    }
}
