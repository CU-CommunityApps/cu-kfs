package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;

/*
 * Lightweight implementation of PurchaseOrderDocument that retrieves the needed PUR_PO_T fields
 * but is not actually a true document. This dramatically improves the batch job's execution time.
 */
public class CemiPurchaseOrderDocumentLite extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private Integer purapDocumentIdentifier;
    private Integer requisitionIdentifier;
    private String requisitionSourceCode;
    private String purchaseOrderVendorChoiceCode;
    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private String vendorName;
    private String vendorLine1Address;
    private String vendorLine2Address;
    private String vendorCityName;
    private String vendorStateCode;
    private String vendorPostalCode;
    private String vendorCountryCode;
    private Boolean vendorRestrictedIndicator;
    private String vendorPaymentTermsCode;
    private String vendorShippingTitleCode;
    private String vendorPhoneNumber;
    private String vendorFaxNumber;
    private String vendorEmailAddress;
    private String vendorCustomerNumber;
    private Integer vendorContractGeneratedIdentifier;
    private String vendorNoteText;
    private String vendorShippingPaymentTermsCode;
    private String documentFundingSourceCode;
    private String requestorPersonName;
    private String requestorPersonEmailAddress;
    private String requestorPersonPhoneNumber;
    private String nonInstitutionFundOrgChartOfAccountsCode;
    private String nonInstitutionFundOrganizationCode;
    private String nonInstitutionFundChartOfAccountsCode;
    private String nonInstitutionFundAccountNumber;
    private String deliveryCampusCode;
    private String deliveryBuildingCode;
    private String deliveryBuildingName;
    private String deliveryBuildingRoomNumber;
    private String deliveryBuildingLine1Address;
    private String deliveryBuildingLine2Address;
    private String deliveryCityName;
    private String deliveryStateCode;
    private String deliveryPostalCode;
    private String deliveryCountryCode;
    private String deliveryToName;
    private String deliveryToEmailAddress;
    private String deliveryToPhoneNumber;
    private Date deliveryRequiredDate;
    private String deliveryInstructionText;
    private String deliveryRequiredDateReasonCode;
    private boolean deliveryBuildingOtherIndicator;
    private String chartOfAccountsCode;
    private String organizationCode;
    private Timestamp purchaseOrderCreateTimestamp;
    private Integer postingYear;
    private String purchaseOrderCostSourceCode;
    private String purchaseOrderTransmissionMethodCode;
    private KualiDecimal purchaseOrderTotalLimit;
    private Date purchaseOrderBeginDate;
    private Date purchaseOrderEndDate;
    private String recurringPaymentFrequencyCode;
    private KualiDecimal recurringPaymentAmount;
    private String recurringPaymentTypeCode;
    private Date recurringPaymentDate;
    private String institutionContactName;
    private String institutionContactPhoneNumber;
    private String institutionContactEmailAddress;
    private String billingName;
    private String billingLine1Address;
    private String billingLine2Address;
    private String billingCityName;
    private String billingStateCode;
    private String billingPostalCode;
    private String billingCountryCode;
    private String billingPhoneNumber;
    private KualiDecimal initialPaymentAmount;
    private Date initialPaymentDate;
    private KualiDecimal finalPaymentAmount;
    private Date finalPaymentDate;
    private String externalOrganizationB2bSupplierIdentifier;
    private Integer contractManagerCode;
    private boolean purchaseOrderAutomaticIndicator;
    private Timestamp purchaseOrderInitialOpenTimestamp;
    private Timestamp purchaseOrderLastTransmitTimestamp;
    private Date purchaseOrderQuoteDueDate;
    private String purchaseOrderQuoteTypeCode;
    private String purchaseOrderQuoteVendorNoteText;
    private boolean purchaseOrderConfirmedIndicator;
    private String purchaseOrderCommodityDescription;
    private Integer purchaseOrderPreviousIdentifier;
    private Integer alternateVendorHeaderGeneratedIdentifier;
    private Integer alternateVendorDetailAssignedIdentifier;
    private String alternateVendorName;
    private boolean purchaseOrderCurrentIndicator;
    private boolean pendingActionIndicator;
    private Timestamp purchaseOrderFirstTransmissionTimestamp;
    private Integer accountsPayablePurchasingDocumentLinkIdentifier;
    private String vendorAddressInternationalProvinceName;
    private boolean receivingDocumentRequiredIndicator;
    private boolean paymentRequestPositiveApprovalIndicator;
    private Date purchaseOrderQuoteInitializationDate;
    private Date purchaseOrderQuoteAwardedDate;
    private String capitalAssetSystemTypeCode;
    private String capitalAssetSystemStateCode;
    private String receivingName;
    private String receivingLine1Address;
    private String receivingLine2Address;
    private String receivingCityName;
    private String receivingStateCode;
    private String receivingPostalCode;
    private String receivingCountryCode;
    private boolean addressToVendorIndicator;
    private boolean useTaxIndicator;
    private String vendorAttentionName;
    private String assignedUserPrincipalId;
    private String accountDistributionMethod;

    private CemiPurchaseOrderIdBo purchaseOrderIdBo;
    private List<CemiPurchaseOrderItemLite> items;
    private List<CemiPurchaseOrderAccountLite> itemAccounts;

    // TODO: Remove this reference (and its getter/setter methods) once KFSPTS-39407 has been implemented.
    private PaymentTermType vendorPaymentTerms;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getPurapDocumentIdentifier() {
        return purapDocumentIdentifier;
    }

    public void setPurapDocumentIdentifier(final Integer purapDocumentIdentifier) {
        this.purapDocumentIdentifier = purapDocumentIdentifier;
    }

    public Integer getRequisitionIdentifier() {
        return requisitionIdentifier;
    }

    public void setRequisitionIdentifier(final Integer requisitionIdentifier) {
        this.requisitionIdentifier = requisitionIdentifier;
    }

    public String getRequisitionSourceCode() {
        return requisitionSourceCode;
    }

    public void setRequisitionSourceCode(final String requisitionSourceCode) {
        this.requisitionSourceCode = requisitionSourceCode;
    }

    public String getPurchaseOrderVendorChoiceCode() {
        return purchaseOrderVendorChoiceCode;
    }

    public void setPurchaseOrderVendorChoiceCode(final String purchaseOrderVendorChoiceCode) {
        this.purchaseOrderVendorChoiceCode = purchaseOrderVendorChoiceCode;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(final Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(final Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(final String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    public void setVendorLine1Address(final String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    public String getVendorLine2Address() {
        return vendorLine2Address;
    }

    public void setVendorLine2Address(final String vendorLine2Address) {
        this.vendorLine2Address = vendorLine2Address;
    }

    public String getVendorCityName() {
        return vendorCityName;
    }

    public void setVendorCityName(final String vendorCityName) {
        this.vendorCityName = vendorCityName;
    }

    public String getVendorStateCode() {
        return vendorStateCode;
    }

    public void setVendorStateCode(final String vendorStateCode) {
        this.vendorStateCode = vendorStateCode;
    }

    public String getVendorPostalCode() {
        return vendorPostalCode;
    }

    public void setVendorPostalCode(final String vendorPostalCode) {
        this.vendorPostalCode = vendorPostalCode;
    }

    public String getVendorCountryCode() {
        return vendorCountryCode;
    }

    public void setVendorCountryCode(final String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
    }

    public Boolean getVendorRestrictedIndicator() {
        return vendorRestrictedIndicator;
    }

    public void setVendorRestrictedIndicator(final Boolean vendorRestrictedIndicator) {
        this.vendorRestrictedIndicator = vendorRestrictedIndicator;
    }

    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    public void setVendorPaymentTermsCode(final String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
    }

    public String getVendorShippingTitleCode() {
        return vendorShippingTitleCode;
    }

    public void setVendorShippingTitleCode(final String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    public String getVendorPhoneNumber() {
        return vendorPhoneNumber;
    }

    public void setVendorPhoneNumber(final String vendorPhoneNumber) {
        this.vendorPhoneNumber = vendorPhoneNumber;
    }

    public String getVendorFaxNumber() {
        return vendorFaxNumber;
    }

    public void setVendorFaxNumber(final String vendorFaxNumber) {
        this.vendorFaxNumber = vendorFaxNumber;
    }

    public String getVendorEmailAddress() {
        return vendorEmailAddress;
    }

    public void setVendorEmailAddress(final String vendorEmailAddress) {
        this.vendorEmailAddress = vendorEmailAddress;
    }

    public String getVendorCustomerNumber() {
        return vendorCustomerNumber;
    }

    public void setVendorCustomerNumber(final String vendorCustomerNumber) {
        this.vendorCustomerNumber = vendorCustomerNumber;
    }

    public Integer getVendorContractGeneratedIdentifier() {
        return vendorContractGeneratedIdentifier;
    }

    public void setVendorContractGeneratedIdentifier(final Integer vendorContractGeneratedIdentifier) {
        this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
    }

    public String getVendorNoteText() {
        return vendorNoteText;
    }

    public void setVendorNoteText(final String vendorNoteText) {
        this.vendorNoteText = vendorNoteText;
    }

    public String getVendorShippingPaymentTermsCode() {
        return vendorShippingPaymentTermsCode;
    }

    public void setVendorShippingPaymentTermsCode(final String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    public String getDocumentFundingSourceCode() {
        return documentFundingSourceCode;
    }

    public void setDocumentFundingSourceCode(final String documentFundingSourceCode) {
        this.documentFundingSourceCode = documentFundingSourceCode;
    }

    public String getRequestorPersonName() {
        return requestorPersonName;
    }

    public void setRequestorPersonName(final String requestorPersonName) {
        this.requestorPersonName = requestorPersonName;
    }

    public String getRequestorPersonEmailAddress() {
        return requestorPersonEmailAddress;
    }

    public void setRequestorPersonEmailAddress(final String requestorPersonEmailAddress) {
        this.requestorPersonEmailAddress = requestorPersonEmailAddress;
    }

    public String getRequestorPersonPhoneNumber() {
        return requestorPersonPhoneNumber;
    }

    public void setRequestorPersonPhoneNumber(final String requestorPersonPhoneNumber) {
        this.requestorPersonPhoneNumber = requestorPersonPhoneNumber;
    }

    public String getNonInstitutionFundOrgChartOfAccountsCode() {
        return nonInstitutionFundOrgChartOfAccountsCode;
    }

    public void setNonInstitutionFundOrgChartOfAccountsCode(final String nonInstitutionFundOrgChartOfAccountsCode) {
        this.nonInstitutionFundOrgChartOfAccountsCode = nonInstitutionFundOrgChartOfAccountsCode;
    }

    public String getNonInstitutionFundOrganizationCode() {
        return nonInstitutionFundOrganizationCode;
    }

    public void setNonInstitutionFundOrganizationCode(final String nonInstitutionFundOrganizationCode) {
        this.nonInstitutionFundOrganizationCode = nonInstitutionFundOrganizationCode;
    }

    public String getNonInstitutionFundChartOfAccountsCode() {
        return nonInstitutionFundChartOfAccountsCode;
    }

    public void setNonInstitutionFundChartOfAccountsCode(final String nonInstitutionFundChartOfAccountsCode) {
        this.nonInstitutionFundChartOfAccountsCode = nonInstitutionFundChartOfAccountsCode;
    }

    public String getNonInstitutionFundAccountNumber() {
        return nonInstitutionFundAccountNumber;
    }

    public void setNonInstitutionFundAccountNumber(final String nonInstitutionFundAccountNumber) {
        this.nonInstitutionFundAccountNumber = nonInstitutionFundAccountNumber;
    }

    public String getDeliveryCampusCode() {
        return deliveryCampusCode;
    }

    public void setDeliveryCampusCode(final String deliveryCampusCode) {
        this.deliveryCampusCode = deliveryCampusCode;
    }

    public String getDeliveryBuildingCode() {
        return deliveryBuildingCode;
    }

    public void setDeliveryBuildingCode(final String deliveryBuildingCode) {
        this.deliveryBuildingCode = deliveryBuildingCode;
    }

    public String getDeliveryBuildingName() {
        return deliveryBuildingName;
    }

    public void setDeliveryBuildingName(final String deliveryBuildingName) {
        this.deliveryBuildingName = deliveryBuildingName;
    }

    public String getDeliveryBuildingRoomNumber() {
        return deliveryBuildingRoomNumber;
    }

    public void setDeliveryBuildingRoomNumber(final String deliveryBuildingRoomNumber) {
        this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
    }

    public String getDeliveryBuildingLine1Address() {
        return deliveryBuildingLine1Address;
    }

    public void setDeliveryBuildingLine1Address(final String deliveryBuildingLine1Address) {
        this.deliveryBuildingLine1Address = deliveryBuildingLine1Address;
    }

    public String getDeliveryBuildingLine2Address() {
        return deliveryBuildingLine2Address;
    }

    public void setDeliveryBuildingLine2Address(final String deliveryBuildingLine2Address) {
        this.deliveryBuildingLine2Address = deliveryBuildingLine2Address;
    }

    public String getDeliveryCityName() {
        return deliveryCityName;
    }

    public void setDeliveryCityName(final String deliveryCityName) {
        this.deliveryCityName = deliveryCityName;
    }

    public String getDeliveryStateCode() {
        return deliveryStateCode;
    }

    public void setDeliveryStateCode(final String deliveryStateCode) {
        this.deliveryStateCode = deliveryStateCode;
    }

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(final String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }

    public String getDeliveryCountryCode() {
        return deliveryCountryCode;
    }

    public void setDeliveryCountryCode(final String deliveryCountryCode) {
        this.deliveryCountryCode = deliveryCountryCode;
    }

    public String getDeliveryToName() {
        return deliveryToName;
    }

    public void setDeliveryToName(final String deliveryToName) {
        this.deliveryToName = deliveryToName;
    }

    public String getDeliveryToEmailAddress() {
        return deliveryToEmailAddress;
    }

    public void setDeliveryToEmailAddress(final String deliveryToEmailAddress) {
        this.deliveryToEmailAddress = deliveryToEmailAddress;
    }

    public String getDeliveryToPhoneNumber() {
        return deliveryToPhoneNumber;
    }

    public void setDeliveryToPhoneNumber(final String deliveryToPhoneNumber) {
        this.deliveryToPhoneNumber = deliveryToPhoneNumber;
    }

    public Date getDeliveryRequiredDate() {
        return deliveryRequiredDate;
    }

    public void setDeliveryRequiredDate(final Date deliveryRequiredDate) {
        this.deliveryRequiredDate = deliveryRequiredDate;
    }

    public String getDeliveryInstructionText() {
        return deliveryInstructionText;
    }

    public void setDeliveryInstructionText(final String deliveryInstructionText) {
        this.deliveryInstructionText = deliveryInstructionText;
    }

    public String getDeliveryRequiredDateReasonCode() {
        return deliveryRequiredDateReasonCode;
    }

    public void setDeliveryRequiredDateReasonCode(final String deliveryRequiredDateReasonCode) {
        this.deliveryRequiredDateReasonCode = deliveryRequiredDateReasonCode;
    }

    public boolean isDeliveryBuildingOtherIndicator() {
        return deliveryBuildingOtherIndicator;
    }

    public void setDeliveryBuildingOtherIndicator(final boolean deliveryBuildingOtherIndicator) {
        this.deliveryBuildingOtherIndicator = deliveryBuildingOtherIndicator;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(final String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public Timestamp getPurchaseOrderCreateTimestamp() {
        return purchaseOrderCreateTimestamp;
    }

    public void setPurchaseOrderCreateTimestamp(final Timestamp purchaseOrderCreateTimestamp) {
        this.purchaseOrderCreateTimestamp = purchaseOrderCreateTimestamp;
    }

    public Integer getPostingYear() {
        return postingYear;
    }

    public void setPostingYear(final Integer postingYear) {
        this.postingYear = postingYear;
    }

    public String getPurchaseOrderCostSourceCode() {
        return purchaseOrderCostSourceCode;
    }

    public void setPurchaseOrderCostSourceCode(final String purchaseOrderCostSourceCode) {
        this.purchaseOrderCostSourceCode = purchaseOrderCostSourceCode;
    }

    public String getPurchaseOrderTransmissionMethodCode() {
        return purchaseOrderTransmissionMethodCode;
    }

    public void setPurchaseOrderTransmissionMethodCode(final String purchaseOrderTransmissionMethodCode) {
        this.purchaseOrderTransmissionMethodCode = purchaseOrderTransmissionMethodCode;
    }

    public KualiDecimal getPurchaseOrderTotalLimit() {
        return purchaseOrderTotalLimit;
    }

    public void setPurchaseOrderTotalLimit(final KualiDecimal purchaseOrderTotalLimit) {
        this.purchaseOrderTotalLimit = purchaseOrderTotalLimit;
    }

    public Date getPurchaseOrderBeginDate() {
        return purchaseOrderBeginDate;
    }

    public void setPurchaseOrderBeginDate(final Date purchaseOrderBeginDate) {
        this.purchaseOrderBeginDate = purchaseOrderBeginDate;
    }

    public Date getPurchaseOrderEndDate() {
        return purchaseOrderEndDate;
    }

    public void setPurchaseOrderEndDate(final Date purchaseOrderEndDate) {
        this.purchaseOrderEndDate = purchaseOrderEndDate;
    }

    public String getRecurringPaymentFrequencyCode() {
        return recurringPaymentFrequencyCode;
    }

    public void setRecurringPaymentFrequencyCode(final String recurringPaymentFrequencyCode) {
        this.recurringPaymentFrequencyCode = recurringPaymentFrequencyCode;
    }

    public KualiDecimal getRecurringPaymentAmount() {
        return recurringPaymentAmount;
    }

    public void setRecurringPaymentAmount(final KualiDecimal recurringPaymentAmount) {
        this.recurringPaymentAmount = recurringPaymentAmount;
    }

    public String getRecurringPaymentTypeCode() {
        return recurringPaymentTypeCode;
    }

    public void setRecurringPaymentTypeCode(final String recurringPaymentTypeCode) {
        this.recurringPaymentTypeCode = recurringPaymentTypeCode;
    }

    public Date getRecurringPaymentDate() {
        return recurringPaymentDate;
    }

    public void setRecurringPaymentDate(final Date recurringPaymentDate) {
        this.recurringPaymentDate = recurringPaymentDate;
    }

    public String getInstitutionContactName() {
        return institutionContactName;
    }

    public void setInstitutionContactName(final String institutionContactName) {
        this.institutionContactName = institutionContactName;
    }

    public String getInstitutionContactPhoneNumber() {
        return institutionContactPhoneNumber;
    }

    public void setInstitutionContactPhoneNumber(final String institutionContactPhoneNumber) {
        this.institutionContactPhoneNumber = institutionContactPhoneNumber;
    }

    public String getInstitutionContactEmailAddress() {
        return institutionContactEmailAddress;
    }

    public void setInstitutionContactEmailAddress(final String institutionContactEmailAddress) {
        this.institutionContactEmailAddress = institutionContactEmailAddress;
    }

    public String getBillingName() {
        return billingName;
    }

    public void setBillingName(final String billingName) {
        this.billingName = billingName;
    }

    public String getBillingLine1Address() {
        return billingLine1Address;
    }

    public void setBillingLine1Address(final String billingLine1Address) {
        this.billingLine1Address = billingLine1Address;
    }

    public String getBillingLine2Address() {
        return billingLine2Address;
    }

    public void setBillingLine2Address(final String billingLine2Address) {
        this.billingLine2Address = billingLine2Address;
    }

    public String getBillingCityName() {
        return billingCityName;
    }

    public void setBillingCityName(final String billingCityName) {
        this.billingCityName = billingCityName;
    }

    public String getBillingStateCode() {
        return billingStateCode;
    }

    public void setBillingStateCode(final String billingStateCode) {
        this.billingStateCode = billingStateCode;
    }

    public String getBillingPostalCode() {
        return billingPostalCode;
    }

    public void setBillingPostalCode(final String billingPostalCode) {
        this.billingPostalCode = billingPostalCode;
    }

    public String getBillingCountryCode() {
        return billingCountryCode;
    }

    public void setBillingCountryCode(final String billingCountryCode) {
        this.billingCountryCode = billingCountryCode;
    }

    public String getBillingPhoneNumber() {
        return billingPhoneNumber;
    }

    public void setBillingPhoneNumber(final String billingPhoneNumber) {
        this.billingPhoneNumber = billingPhoneNumber;
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

    public String getExternalOrganizationB2bSupplierIdentifier() {
        return externalOrganizationB2bSupplierIdentifier;
    }

    public void setExternalOrganizationB2bSupplierIdentifier(final String externalOrganizationB2bSupplierIdentifier) {
        this.externalOrganizationB2bSupplierIdentifier = externalOrganizationB2bSupplierIdentifier;
    }

    public Integer getContractManagerCode() {
        return contractManagerCode;
    }

    public void setContractManagerCode(final Integer contractManagerCode) {
        this.contractManagerCode = contractManagerCode;
    }

    public boolean isPurchaseOrderAutomaticIndicator() {
        return purchaseOrderAutomaticIndicator;
    }

    public void setPurchaseOrderAutomaticIndicator(final boolean purchaseOrderAutomaticIndicator) {
        this.purchaseOrderAutomaticIndicator = purchaseOrderAutomaticIndicator;
    }

    public Timestamp getPurchaseOrderInitialOpenTimestamp() {
        return purchaseOrderInitialOpenTimestamp;
    }

    public void setPurchaseOrderInitialOpenTimestamp(final Timestamp purchaseOrderInitialOpenTimestamp) {
        this.purchaseOrderInitialOpenTimestamp = purchaseOrderInitialOpenTimestamp;
    }

    public Timestamp getPurchaseOrderLastTransmitTimestamp() {
        return purchaseOrderLastTransmitTimestamp;
    }

    public void setPurchaseOrderLastTransmitTimestamp(final Timestamp purchaseOrderLastTransmitTimestamp) {
        this.purchaseOrderLastTransmitTimestamp = purchaseOrderLastTransmitTimestamp;
    }

    public Date getPurchaseOrderQuoteDueDate() {
        return purchaseOrderQuoteDueDate;
    }

    public void setPurchaseOrderQuoteDueDate(final Date purchaseOrderQuoteDueDate) {
        this.purchaseOrderQuoteDueDate = purchaseOrderQuoteDueDate;
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

    public boolean isPurchaseOrderConfirmedIndicator() {
        return purchaseOrderConfirmedIndicator;
    }

    public void setPurchaseOrderConfirmedIndicator(final boolean purchaseOrderConfirmedIndicator) {
        this.purchaseOrderConfirmedIndicator = purchaseOrderConfirmedIndicator;
    }

    public String getPurchaseOrderCommodityDescription() {
        return purchaseOrderCommodityDescription;
    }

    public void setPurchaseOrderCommodityDescription(final String purchaseOrderCommodityDescription) {
        this.purchaseOrderCommodityDescription = purchaseOrderCommodityDescription;
    }

    public Integer getPurchaseOrderPreviousIdentifier() {
        return purchaseOrderPreviousIdentifier;
    }

    public void setPurchaseOrderPreviousIdentifier(final Integer purchaseOrderPreviousIdentifier) {
        this.purchaseOrderPreviousIdentifier = purchaseOrderPreviousIdentifier;
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

    public String getAlternateVendorName() {
        return alternateVendorName;
    }

    public void setAlternateVendorName(final String alternateVendorName) {
        this.alternateVendorName = alternateVendorName;
    }

    public boolean isPurchaseOrderCurrentIndicator() {
        return purchaseOrderCurrentIndicator;
    }

    public void setPurchaseOrderCurrentIndicator(final boolean purchaseOrderCurrentIndicator) {
        this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
    }

    public boolean isPendingActionIndicator() {
        return pendingActionIndicator;
    }

    public void setPendingActionIndicator(final boolean pendingActionIndicator) {
        this.pendingActionIndicator = pendingActionIndicator;
    }

    public Timestamp getPurchaseOrderFirstTransmissionTimestamp() {
        return purchaseOrderFirstTransmissionTimestamp;
    }

    public void setPurchaseOrderFirstTransmissionTimestamp(final Timestamp purchaseOrderFirstTransmissionTimestamp) {
        this.purchaseOrderFirstTransmissionTimestamp = purchaseOrderFirstTransmissionTimestamp;
    }

    public Integer getAccountsPayablePurchasingDocumentLinkIdentifier() {
        return accountsPayablePurchasingDocumentLinkIdentifier;
    }

    public void setAccountsPayablePurchasingDocumentLinkIdentifier(
            Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        this.accountsPayablePurchasingDocumentLinkIdentifier = accountsPayablePurchasingDocumentLinkIdentifier;
    }

    public String getVendorAddressInternationalProvinceName() {
        return vendorAddressInternationalProvinceName;
    }

    public void setVendorAddressInternationalProvinceName(final String vendorAddressInternationalProvinceName) {
        this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
    }

    public boolean isReceivingDocumentRequiredIndicator() {
        return receivingDocumentRequiredIndicator;
    }

    public void setReceivingDocumentRequiredIndicator(final boolean receivingDocumentRequiredIndicator) {
        this.receivingDocumentRequiredIndicator = receivingDocumentRequiredIndicator;
    }

    public boolean isPaymentRequestPositiveApprovalIndicator() {
        return paymentRequestPositiveApprovalIndicator;
    }

    public void setPaymentRequestPositiveApprovalIndicator(final boolean paymentRequestPositiveApprovalIndicator) {
        this.paymentRequestPositiveApprovalIndicator = paymentRequestPositiveApprovalIndicator;
    }

    public Date getPurchaseOrderQuoteInitializationDate() {
        return purchaseOrderQuoteInitializationDate;
    }

    public void setPurchaseOrderQuoteInitializationDate(final Date purchaseOrderQuoteInitializationDate) {
        this.purchaseOrderQuoteInitializationDate = purchaseOrderQuoteInitializationDate;
    }

    public Date getPurchaseOrderQuoteAwardedDate() {
        return purchaseOrderQuoteAwardedDate;
    }

    public void setPurchaseOrderQuoteAwardedDate(final Date purchaseOrderQuoteAwardedDate) {
        this.purchaseOrderQuoteAwardedDate = purchaseOrderQuoteAwardedDate;
    }

    public String getCapitalAssetSystemTypeCode() {
        return capitalAssetSystemTypeCode;
    }

    public void setCapitalAssetSystemTypeCode(final String capitalAssetSystemTypeCode) {
        this.capitalAssetSystemTypeCode = capitalAssetSystemTypeCode;
    }

    public String getCapitalAssetSystemStateCode() {
        return capitalAssetSystemStateCode;
    }

    public void setCapitalAssetSystemStateCode(final String capitalAssetSystemStateCode) {
        this.capitalAssetSystemStateCode = capitalAssetSystemStateCode;
    }

    public String getReceivingName() {
        return receivingName;
    }

    public void setReceivingName(final String receivingName) {
        this.receivingName = receivingName;
    }

    public String getReceivingLine1Address() {
        return receivingLine1Address;
    }

    public void setReceivingLine1Address(final String receivingLine1Address) {
        this.receivingLine1Address = receivingLine1Address;
    }

    public String getReceivingLine2Address() {
        return receivingLine2Address;
    }

    public void setReceivingLine2Address(final String receivingLine2Address) {
        this.receivingLine2Address = receivingLine2Address;
    }

    public String getReceivingCityName() {
        return receivingCityName;
    }

    public void setReceivingCityName(final String receivingCityName) {
        this.receivingCityName = receivingCityName;
    }

    public String getReceivingStateCode() {
        return receivingStateCode;
    }

    public void setReceivingStateCode(final String receivingStateCode) {
        this.receivingStateCode = receivingStateCode;
    }

    public String getReceivingPostalCode() {
        return receivingPostalCode;
    }

    public void setReceivingPostalCode(final String receivingPostalCode) {
        this.receivingPostalCode = receivingPostalCode;
    }

    public String getReceivingCountryCode() {
        return receivingCountryCode;
    }

    public void setReceivingCountryCode(final String receivingCountryCode) {
        this.receivingCountryCode = receivingCountryCode;
    }

    public boolean isAddressToVendorIndicator() {
        return addressToVendorIndicator;
    }

    public void setAddressToVendorIndicator(final boolean addressToVendorIndicator) {
        this.addressToVendorIndicator = addressToVendorIndicator;
    }

    public boolean isUseTaxIndicator() {
        return useTaxIndicator;
    }

    public void setUseTaxIndicator(final boolean useTaxIndicator) {
        this.useTaxIndicator = useTaxIndicator;
    }

    public String getVendorAttentionName() {
        return vendorAttentionName;
    }

    public void setVendorAttentionName(final String vendorAttentionName) {
        this.vendorAttentionName = vendorAttentionName;
    }

    public String getAssignedUserPrincipalId() {
        return assignedUserPrincipalId;
    }

    public void setAssignedUserPrincipalId(final String assignedUserPrincipalId) {
        this.assignedUserPrincipalId = assignedUserPrincipalId;
    }

    public String getAccountDistributionMethod() {
        return accountDistributionMethod;
    }

    public void setAccountDistributionMethod(final String accountDistributionMethod) {
        this.accountDistributionMethod = accountDistributionMethod;
    }

    public CemiPurchaseOrderIdBo getPurchaseOrderIdBo() {
        return purchaseOrderIdBo;
    }

    public void setPurchaseOrderIdBo(final CemiPurchaseOrderIdBo purchaseOrderIdBo) {
        this.purchaseOrderIdBo = purchaseOrderIdBo;
    }

    public List<CemiPurchaseOrderItemLite> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(final List<CemiPurchaseOrderItemLite> items) {
        this.items = items;
    }

    public List<CemiPurchaseOrderAccountLite> getItemAccounts() {
        if (itemAccounts == null) {
            itemAccounts = new ArrayList<>();
        }
        return itemAccounts;
    }

    public void setItemAccounts(final List<CemiPurchaseOrderAccountLite> itemAccounts) {
        this.itemAccounts = itemAccounts;
    }

    public PaymentTermType getVendorPaymentTerms() {
        return vendorPaymentTerms;
    }

    public void setVendorPaymentTerms(final PaymentTermType vendorPaymentTerms) {
        this.vendorPaymentTerms = vendorPaymentTerms;
    }

}
