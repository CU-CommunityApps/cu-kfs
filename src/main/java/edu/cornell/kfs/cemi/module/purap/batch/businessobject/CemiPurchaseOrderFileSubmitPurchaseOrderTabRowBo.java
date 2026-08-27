package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiPurchaseOrderFileSubmitPurchaseOrderTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = -5362750185798569318L;

    private String kfsDocumentNumber;
    private Integer kfsPurchaseOrderId;

    private String spreadsheetKey;
    private String addOnly;
    private String existingPurchaseOrderDocumentNumber;
    private String orderTypeReference;
    private String autoComplete;
    private String comment;
    private String worker;
    private String purchaseOrderId;
    private String submit;
    private String lockedInWorkday;
    private String documentNumber;
    private String invoiceStatus;
    private String paymentStatus;
    private String receivingStatus;
    private String shippingStatus;
    private String trackingStatus;
    private String company;
    private String supplier;
    private String purchaseOrderType;
    private String externalPoNumber;
    private String orderFromSupplierConnection;
    private String documentDate;
    private String taxAmount;
    private String freightAmount;
    private String otherCharges;
    private String paymentTerms;
    private String overridePaymentType;
    private String procurementCreditCard;
    private String shippingTerms;
    private String shippingMethod;
    private String shippingInstruction;
    private String dueDate;
    private String supplierContract;
    private String externalSupplierInvoiceSource;
    private String currency;
    private String acknowledgementExpected;
    private String defaultTaxOption;
    private String defaultTaxCode;
    private String issueOption;
    private String emailRowId;
    private String emailId;
    private String emailAddress;
    private String buyer;
    private String billToContact;
    private String billToContactDetail;
    private String existingBillToAddressId;
    private String newBillToAddressId;
    private String shipToContact;
    private String shipToContactDetail;
    private String existingShipToAddressId;
    private String newShipToAddressId;
    private String documentLink;
    private String memoForSupplier;
    private String internalMemo;
    private String prepaid;
    private String prepaymentReleaseType;
    private String expectedReleaseDate;
    private String frequency;
    private String numberOfPrepaymentInstallments;
    private String useInvoiceDate;
    private String specifiedDate;
    private String usePrepaidPostingRulesForReceiptAccruals;
    private String percentToRetain;
    private String estimatedRetentionReleaseDate;
    private String xmlname3rdPartyRetention;
    private String retentionMemo;
    private String downPaymentAmount;
    private String downPaymentPercentage;
    private String downPaymentMemo;
    private String procedureDate;
    private String procedure;
    private String procedureNumber;
    private String patientId;
    private String medicalRecordNumber;
    private String physicianId;
    private String verifiedBy;
    private String supplierRepresentative;
    private String supplierSalesOrderNumber;
    private String additionalProcedureDetails;
    private String goodsRowId;
    private String goodsCatalogItem;
    private String goodsPurchaseOrderLineId;
    private String goodsLineNumber;
    private String goodsLineCompany;
    private String goodsSupplierItemIdentifier;
    private String goodsSupplierPartId;
    private String goodsSupplierPartAuxiliaryId;
    private String goodsUnspscCode;
    private String goodsItemDescription;
    private String goodsSupplierContractLine;
    private String goodsAlternateSupplierContract;
    private String goodsCommodityCode;
    private String goodsCommodityCodeType;
    private String goodsPaymentStatus;
    private String goodsInvoiceStatus;
    private String goodsReceivingStatus;
    private String goodsShippingStatus;
    private String goodsTrackingStatus;
    private String goodsResourceCategory;
    private String goodsTaxApplicability;
    private String goodsTaxCode;
    private String goodsTaxRate1;
    private String goodsTaxRecoverability1;
    private String goodsTaxOption1;
    private String goodsTaxRate2;
    private String goodsTaxRecoverability2;
    private String goodsTaxOption2;
    private String goodsTaxRate3;
    private String goodsTaxRecoverability3;
    private String goodsTaxOption3;
    private String goodsTaxRate4;
    private String goodsTaxRecoverability4;
    private String goodsTaxOption4;
    private String goodsTaxRate5;
    private String goodsTaxRecoverability5;
    private String goodsTaxOption5;
    private String goodsTaxRate6;
    private String goodsTaxRecoverability6;
    private String goodsTaxOption6;
    private String goodsPackagingString;
    private String goodsQuantity;
    private String goodsUnitOfMeasure;
    private String goodsUnitCost;
    private String goodsRequestedAsNoCharge;
    private String goodsExtendedAmount;
    private String goodsLotSerialInformation;
    private String goodsLotNumber;
    private String goodsSerialNumber;
    private String goodsDueDate;
    private String goodsDeliveryType;
    private String goodsPrepaid;
    private String goodsDownPayment;
    private String goodsRetention;
    private String goodsRequestedDeliveryDate;
    private String goodsBudgetDate;
    private String goodsMemo;
    private String goodsShipToAddress;
    private String goodsShipToGlobalLocationNumber;
    private String goodsShipToLocationIdentifier;
    private String goodsShipToContact;
    private String goodsRequester;
    private String goodsDeliverToLocation;
    private String goodsDeliverToLocationGln;
    private String goodsDeliverToLocationIdentifier;
    private String goodsSupplierContract;
    private String goodsExternalSupplierInvoiceSource;
    private String goodsRequisitionLine;
    private String goodsStorageLocation;
    private String goodsCostCenter;
    private String goodsCostCenterExternalSupplierInvoiceSource;
    private String goodsProject;
    private String goodsProjectExternalSupplierInvoiceSource;
    private String goodsGrant;
    private String goodsGrantExternalSupplierInvoiceSource;
    private String goodsGift;
    private String goodsGiftExternalSupplierInvoiceSource;
    private String goodsFund;
    private String goodsFundExternalSupplierInvoiceSource;
    private String goodsLineSplitRowId;
    private String goodsExistingBusinessDocumentLineSplitId;
    private String goodsNewBusinessDocumentLineSplitId;
    private String goodsLineSplitQuantity;
    private String goodsLineSplitExtendedAmount;
    private String goodsLineSplitBudgetDate;
    private String goodsLineSplitMemo;
    private String goodsLineSplitAllocation;
    private String goodsLineSplitCostCenter;
    private String goodsLineSplitCostCenterExternalSupplierInvoiceSource;
    private String goodsLineSplitProject;
    private String goodsLineSplitProjectExternalSupplierInvoiceSource;
    private String goodsLineSplitGrant;
    private String goodsLineSplitGrantExternalSupplierInvoiceSource;
    private String goodsLineSplitGift;
    private String goodsLineSplitGiftExternalSupplierInvoiceSource;
    private String goodsLineSplitFund;
    private String goodsLineSplitFundExternalSupplierInvoiceSource;
    private String goodsAlternateItemIdentifierRowId;
    private String goodsAlternateItemIdentifierType;
    private String goodsAlternateItemIdentifierValue;
    private String goodsAlternateItemIdentifierUnitOfMeasure;
    private String goodsAlternateItemIdentifierManufacturer;
    private String goodsItemTag;
    private String goodsCloseStatus;
    private String goodsClosedRowId;
    private String goodsClosedOn;
    private String goodsClosedBy;
    private String goodsClosedReasonCode;
    private String serviceRowId;
    private String serviceCatalogItem;
    private String serviceOrderLineId;
    private String serviceLineNumber;
    private String serviceLineCompany;
    private String serviceDescription;
    private String serviceSupplierContractLine;
    private String serviceAlternateSupplierContract;
    private String serviceCommodityCode;
    private String serviceCommodityCodeType;
    private String servicePaymentStatus;
    private String serviceInvoiceStatus;
    private String serviceReceivingStatus;
    private String serviceResourceCategory;
    private String serviceTaxApplicability;
    private String serviceTaxCode;
    private String serviceTaxRate1;
    private String serviceTaxRecoverability1;
    private String serviceTaxOption1;
    private String serviceTaxRate2;
    private String serviceTaxRecoverability2;
    private String serviceTaxOption2;
    private String serviceTaxRate3;
    private String serviceTaxRecoverability3;
    private String serviceTaxOption3;
    private String serviceTaxRate4;
    private String serviceTaxRecoverability4;
    private String serviceTaxOption4;
    private String serviceTaxRate5;
    private String serviceTaxRecoverability5;
    private String serviceTaxOption5;
    private String serviceTaxRate6;
    private String serviceTaxRecoverability6;
    private String serviceTaxOption6;
    private String serviceExtendedAmount;
    private String serviceDueDate;
    private String serviceStartDate;
    private String serviceEndDate;
    private String servicePrepaid;
    private String serviceDownPayment;
    private String serviceRetention;
    private String serviceBudgetDate;
    private String serviceMemo;
    private String serviceShipToAddress;
    private String serviceShipToContact;
    private String serviceRequester;
    private String serviceDeliverToLocation;
    private String serviceRequisitionLine;
    private String serviceSupplierContract;
    private String serviceSupplierContractExternalSupplierInvoiceSource;
    private String serviceStorageLocation;
    private String serviceCostCenter;
    private String serviceCostCenterExternalSupplierInvoiceSource;
    private String serviceProject;
    private String serviceProjectExternalSupplierInvoiceSource;
    private String serviceGrant;
    private String serviceGrantExternalSupplierInvoiceSource;
    private String serviceGift;
    private String serviceGiftExternalSupplierInvoiceSource;
    private String serviceFund;
    private String serviceFundExternalSupplierInvoiceSource;
    private String serviceLineSplitRowId;
    private String serviceExistingBusinessDocumentLineSplitId;
    private String serviceNewBusinessDocumentLineSplitId;
    private String serviceLineSplitQuantity;
    private String serviceLineSplitExtendedAmount;
    private String serviceLineSplitBudgetDate;
    private String serviceLineSplitMemo;
    private String serviceLineSplitAllocation;
    private String serviceLineSplitCostCenter;
    private String serviceLineSplitCostCenterExternalSupplierInvoiceSource;
    private String serviceLineSplitProject;
    private String serviceLineSplitProjectExternalSupplierInvoiceSource;
    private String serviceLineSplitGrant;
    private String serviceLineSplitGrantExternalSupplierInvoiceSource;
    private String serviceLineSplitGift;
    private String serviceLineSplitGiftExternalSupplierInvoiceSource;
    private String serviceLineSplitFund;
    private String serviceLineSplitFundExternalSupplierInvoiceSource;
    private String serviceCloseStatus;
    private String serviceClosedRowId;
    private String serviceClosedOn;
    private String serviceClosedBy;
    private String serviceClosedReasonCode;
    private String deliverablesRowId;
    private String deliverablesOrderLineId;
    private String deliverablesLineNumber;
    private String deliverablesLineCompany;
    private String deliverablesProject;
    private String deliverablesSupplierContractLine;
    private String deliverablesAlternateSupplierContract;
    private String deliverablesReceivingStatus;
    private String deliverablesPaymentStatus;
    private String deliverablesInvoiceStatus;
    private String deliverablesPrepaid;
    private String deliverablesDownPayment;
    private String deliverablesRetention;
    private String deliverablesBudgetDate;
    private String deliverablesMemo;
    private String deliverablesRequisitionLine;
    private String deliverablesResourceCategory;
    private String deliverablesProjectPlanPhaseRowId;
    private String deliverablesProjectPlanPhase;
    private String deliverablesProjectPlanTaskRowId;
    private String deliverablesProjectPlanTask;
    private String deliverablesProjectSubtaskRowId;
    private String deliverablesProjectSubtaskDescription;
    private String deliverablesProjectSubtaskAmount;
    private String deliverablesExtendedAmount;
    private String deliverablesWorktags;
    private String deliverablesWorktagsExternalSupplierInvoiceSource;
    private String deliverablesSupplierContract;
    private String deliverablesSupplierContractExternalSupplierInvoiceSource;
    private String deliverablesLineSplitRowId;
    private String deliverablesExistingBusinessDocumentLineSplitId;
    private String deliverablesNewBusinessDocumentLineSplitId;
    private String deliverablesLineSplitQuantity;
    private String deliverablesLineSplitExtendedAmount;
    private String deliverablesLineSplitBudgetDate;
    private String deliverablesLineSplitMemo;
    private String deliverablesLineSplitAllocation;
    private String deliverablesLineSplitWorktag;
    private String deliverablesLineSplitWorktagExternalSupplierInvoiceSource;
    private String deliverablesCloseStatus;
    private String deliverablesClosedRowId;
    private String deliverablesClosedOn;
    private String deliverablesClosedBy;
    private String deliverablesClosedReasonCode;
    private String taxCodeRowId;
    private String taxCodeApplicability;
    private String taxCode;
    private String taxCodeAmount;
    private String taxRateRowId;
    private String taxRate;
    private String taxRateAmount;
    private String taxRateRecoverability;
    private String taxRateType;
    private String taxRatePointDateType;
    private String taxRatePointDate;
    private String attachmentRowId;
    private String attachmentContentType;
    private String attachmentFilename;
    private String attachmentEncoding;
    private String attachmentCompressed;
    private String attachmentFileContent;
    private String attachmentComment;
    private String attachmentExternal;
    private String attachmentCategory;
    private String amortizationRowId;
    private String amortizationFrequency;
    private String amortizationNumberOfPrepaymentInstallments;
    private String amortizationUseInvoiceDate;
    private String amortizationSpecifiedDate;
    private String amortizationIncludeAllAvailablePrepaidLines;
    private String amortizationPurchaseOrderLineNumber;

    public String getKfsDocumentNumber() {
        return kfsDocumentNumber;
    }

    public void setKfsDocumentNumber(final String kfsDocumentNumber) {
        this.kfsDocumentNumber = kfsDocumentNumber;
    }

    public Integer getKfsPurchaseOrderId() {
        return kfsPurchaseOrderId;
    }

    public void setKfsPurchaseOrderId(final Integer kfsPurchaseOrderId) {
        this.kfsPurchaseOrderId = kfsPurchaseOrderId;
    }

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(final String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getAddOnly() {
        return addOnly;
    }

    public void setAddOnly(final String addOnly) {
        this.addOnly = addOnly;
    }

    public String getExistingPurchaseOrderDocumentNumber() {
        return existingPurchaseOrderDocumentNumber;
    }

    public void setExistingPurchaseOrderDocumentNumber(final String existingPurchaseOrderDocumentNumber) {
        this.existingPurchaseOrderDocumentNumber = existingPurchaseOrderDocumentNumber;
    }

    public String getOrderTypeReference() {
        return orderTypeReference;
    }

    public void setOrderTypeReference(final String orderTypeReference) {
        this.orderTypeReference = orderTypeReference;
    }

    public String getAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(final String autoComplete) {
        this.autoComplete = autoComplete;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(final String worker) {
        this.worker = worker;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(final String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(final String submit) {
        this.submit = submit;
    }

    public String getLockedInWorkday() {
        return lockedInWorkday;
    }

    public void setLockedInWorkday(final String lockedInWorkday) {
        this.lockedInWorkday = lockedInWorkday;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(final String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(final String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getReceivingStatus() {
        return receivingStatus;
    }

    public void setReceivingStatus(final String receivingStatus) {
        this.receivingStatus = receivingStatus;
    }

    public String getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(final String shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public String getTrackingStatus() {
        return trackingStatus;
    }

    public void setTrackingStatus(final String trackingStatus) {
        this.trackingStatus = trackingStatus;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(final String supplier) {
        this.supplier = supplier;
    }

    public String getPurchaseOrderType() {
        return purchaseOrderType;
    }

    public void setPurchaseOrderType(final String purchaseOrderType) {
        this.purchaseOrderType = purchaseOrderType;
    }

    public String getExternalPoNumber() {
        return externalPoNumber;
    }

    public void setExternalPoNumber(final String externalPoNumber) {
        this.externalPoNumber = externalPoNumber;
    }

    public String getOrderFromSupplierConnection() {
        return orderFromSupplierConnection;
    }

    public void setOrderFromSupplierConnection(final String orderFromSupplierConnection) {
        this.orderFromSupplierConnection = orderFromSupplierConnection;
    }

    public String getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(final String documentDate) {
        this.documentDate = documentDate;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(final String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getFreightAmount() {
        return freightAmount;
    }

    public void setFreightAmount(final String freightAmount) {
        this.freightAmount = freightAmount;
    }

    public String getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(final String otherCharges) {
        this.otherCharges = otherCharges;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(final String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getOverridePaymentType() {
        return overridePaymentType;
    }

    public void setOverridePaymentType(final String overridePaymentType) {
        this.overridePaymentType = overridePaymentType;
    }

    public String getProcurementCreditCard() {
        return procurementCreditCard;
    }

    public void setProcurementCreditCard(final String procurementCreditCard) {
        this.procurementCreditCard = procurementCreditCard;
    }

    public String getShippingTerms() {
        return shippingTerms;
    }

    public void setShippingTerms(final String shippingTerms) {
        this.shippingTerms = shippingTerms;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getShippingInstruction() {
        return shippingInstruction;
    }

    public void setShippingInstruction(final String shippingInstruction) {
        this.shippingInstruction = shippingInstruction;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(final String dueDate) {
        this.dueDate = dueDate;
    }

    public String getSupplierContract() {
        return supplierContract;
    }

    public void setSupplierContract(final String supplierContract) {
        this.supplierContract = supplierContract;
    }

    public String getExternalSupplierInvoiceSource() {
        return externalSupplierInvoiceSource;
    }

    public void setExternalSupplierInvoiceSource(final String externalSupplierInvoiceSource) {
        this.externalSupplierInvoiceSource = externalSupplierInvoiceSource;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getAcknowledgementExpected() {
        return acknowledgementExpected;
    }

    public void setAcknowledgementExpected(final String acknowledgementExpected) {
        this.acknowledgementExpected = acknowledgementExpected;
    }

    public String getDefaultTaxOption() {
        return defaultTaxOption;
    }

    public void setDefaultTaxOption(final String defaultTaxOption) {
        this.defaultTaxOption = defaultTaxOption;
    }

    public String getDefaultTaxCode() {
        return defaultTaxCode;
    }

    public void setDefaultTaxCode(final String defaultTaxCode) {
        this.defaultTaxCode = defaultTaxCode;
    }

    public String getIssueOption() {
        return issueOption;
    }

    public void setIssueOption(final String issueOption) {
        this.issueOption = issueOption;
    }

    public String getEmailRowId() {
        return emailRowId;
    }

    public void setEmailRowId(final String emailRowId) {
        this.emailRowId = emailRowId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(final String buyer) {
        this.buyer = buyer;
    }

    public String getBillToContact() {
        return billToContact;
    }

    public void setBillToContact(final String billToContact) {
        this.billToContact = billToContact;
    }

    public String getBillToContactDetail() {
        return billToContactDetail;
    }

    public void setBillToContactDetail(final String billToContactDetail) {
        this.billToContactDetail = billToContactDetail;
    }

    public String getExistingBillToAddressId() {
        return existingBillToAddressId;
    }

    public void setExistingBillToAddressId(final String existingBillToAddressId) {
        this.existingBillToAddressId = existingBillToAddressId;
    }

    public String getNewBillToAddressId() {
        return newBillToAddressId;
    }

    public void setNewBillToAddressId(final String newBillToAddressId) {
        this.newBillToAddressId = newBillToAddressId;
    }

    public String getShipToContact() {
        return shipToContact;
    }

    public void setShipToContact(final String shipToContact) {
        this.shipToContact = shipToContact;
    }

    public String getShipToContactDetail() {
        return shipToContactDetail;
    }

    public void setShipToContactDetail(final String shipToContactDetail) {
        this.shipToContactDetail = shipToContactDetail;
    }

    public String getExistingShipToAddressId() {
        return existingShipToAddressId;
    }

    public void setExistingShipToAddressId(final String existingShipToAddressId) {
        this.existingShipToAddressId = existingShipToAddressId;
    }

    public String getNewShipToAddressId() {
        return newShipToAddressId;
    }

    public void setNewShipToAddressId(final String newShipToAddressId) {
        this.newShipToAddressId = newShipToAddressId;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(final String documentLink) {
        this.documentLink = documentLink;
    }

    public String getMemoForSupplier() {
        return memoForSupplier;
    }

    public void setMemoForSupplier(final String memoForSupplier) {
        this.memoForSupplier = memoForSupplier;
    }

    public String getInternalMemo() {
        return internalMemo;
    }

    public void setInternalMemo(final String internalMemo) {
        this.internalMemo = internalMemo;
    }

    public String getPrepaid() {
        return prepaid;
    }

    public void setPrepaid(final String prepaid) {
        this.prepaid = prepaid;
    }

    public String getPrepaymentReleaseType() {
        return prepaymentReleaseType;
    }

    public void setPrepaymentReleaseType(final String prepaymentReleaseType) {
        this.prepaymentReleaseType = prepaymentReleaseType;
    }

    public String getExpectedReleaseDate() {
        return expectedReleaseDate;
    }

    public void setExpectedReleaseDate(final String expectedReleaseDate) {
        this.expectedReleaseDate = expectedReleaseDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    public String getNumberOfPrepaymentInstallments() {
        return numberOfPrepaymentInstallments;
    }

    public void setNumberOfPrepaymentInstallments(final String numberOfPrepaymentInstallments) {
        this.numberOfPrepaymentInstallments = numberOfPrepaymentInstallments;
    }

    public String getUseInvoiceDate() {
        return useInvoiceDate;
    }

    public void setUseInvoiceDate(final String useInvoiceDate) {
        this.useInvoiceDate = useInvoiceDate;
    }

    public String getSpecifiedDate() {
        return specifiedDate;
    }

    public void setSpecifiedDate(final String specifiedDate) {
        this.specifiedDate = specifiedDate;
    }

    public String getUsePrepaidPostingRulesForReceiptAccruals() {
        return usePrepaidPostingRulesForReceiptAccruals;
    }

    public void setUsePrepaidPostingRulesForReceiptAccruals(final String usePrepaidPostingRulesForReceiptAccruals) {
        this.usePrepaidPostingRulesForReceiptAccruals = usePrepaidPostingRulesForReceiptAccruals;
    }

    public String getPercentToRetain() {
        return percentToRetain;
    }

    public void setPercentToRetain(final String percentToRetain) {
        this.percentToRetain = percentToRetain;
    }

    public String getEstimatedRetentionReleaseDate() {
        return estimatedRetentionReleaseDate;
    }

    public void setEstimatedRetentionReleaseDate(final String estimatedRetentionReleaseDate) {
        this.estimatedRetentionReleaseDate = estimatedRetentionReleaseDate;
    }

    public String getXmlname3rdPartyRetention() {
        return xmlname3rdPartyRetention;
    }

    public void setXmlname3rdPartyRetention(final String xmlname3rdPartyRetention) {
        this.xmlname3rdPartyRetention = xmlname3rdPartyRetention;
    }

    public String getRetentionMemo() {
        return retentionMemo;
    }

    public void setRetentionMemo(final String retentionMemo) {
        this.retentionMemo = retentionMemo;
    }

    public String getDownPaymentAmount() {
        return downPaymentAmount;
    }

    public void setDownPaymentAmount(final String downPaymentAmount) {
        this.downPaymentAmount = downPaymentAmount;
    }

    public String getDownPaymentPercentage() {
        return downPaymentPercentage;
    }

    public void setDownPaymentPercentage(final String downPaymentPercentage) {
        this.downPaymentPercentage = downPaymentPercentage;
    }

    public String getDownPaymentMemo() {
        return downPaymentMemo;
    }

    public void setDownPaymentMemo(final String downPaymentMemo) {
        this.downPaymentMemo = downPaymentMemo;
    }

    public String getProcedureDate() {
        return procedureDate;
    }

    public void setProcedureDate(final String procedureDate) {
        this.procedureDate = procedureDate;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(final String procedure) {
        this.procedure = procedure;
    }

    public String getProcedureNumber() {
        return procedureNumber;
    }

    public void setProcedureNumber(final String procedureNumber) {
        this.procedureNumber = procedureNumber;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(final String patientId) {
        this.patientId = patientId;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public void setMedicalRecordNumber(final String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }

    public String getPhysicianId() {
        return physicianId;
    }

    public void setPhysicianId(final String physicianId) {
        this.physicianId = physicianId;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(final String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getSupplierRepresentative() {
        return supplierRepresentative;
    }

    public void setSupplierRepresentative(final String supplierRepresentative) {
        this.supplierRepresentative = supplierRepresentative;
    }

    public String getSupplierSalesOrderNumber() {
        return supplierSalesOrderNumber;
    }

    public void setSupplierSalesOrderNumber(final String supplierSalesOrderNumber) {
        this.supplierSalesOrderNumber = supplierSalesOrderNumber;
    }

    public String getAdditionalProcedureDetails() {
        return additionalProcedureDetails;
    }

    public void setAdditionalProcedureDetails(final String additionalProcedureDetails) {
        this.additionalProcedureDetails = additionalProcedureDetails;
    }

    public String getGoodsRowId() {
        return goodsRowId;
    }

    public void setGoodsRowId(final String goodsRowId) {
        this.goodsRowId = goodsRowId;
    }

    public String getGoodsCatalogItem() {
        return goodsCatalogItem;
    }

    public void setGoodsCatalogItem(final String goodsCatalogItem) {
        this.goodsCatalogItem = goodsCatalogItem;
    }

    public String getGoodsPurchaseOrderLineId() {
        return goodsPurchaseOrderLineId;
    }

    public void setGoodsPurchaseOrderLineId(final String goodsPurchaseOrderLineId) {
        this.goodsPurchaseOrderLineId = goodsPurchaseOrderLineId;
    }

    public String getGoodsLineNumber() {
        return goodsLineNumber;
    }

    public void setGoodsLineNumber(final String goodsLineNumber) {
        this.goodsLineNumber = goodsLineNumber;
    }

    public String getGoodsLineCompany() {
        return goodsLineCompany;
    }

    public void setGoodsLineCompany(final String goodsLineCompany) {
        this.goodsLineCompany = goodsLineCompany;
    }

    public String getGoodsSupplierItemIdentifier() {
        return goodsSupplierItemIdentifier;
    }

    public void setGoodsSupplierItemIdentifier(final String goodsSupplierItemIdentifier) {
        this.goodsSupplierItemIdentifier = goodsSupplierItemIdentifier;
    }

    public String getGoodsSupplierPartId() {
        return goodsSupplierPartId;
    }

    public void setGoodsSupplierPartId(final String goodsSupplierPartId) {
        this.goodsSupplierPartId = goodsSupplierPartId;
    }

    public String getGoodsSupplierPartAuxiliaryId() {
        return goodsSupplierPartAuxiliaryId;
    }

    public void setGoodsSupplierPartAuxiliaryId(final String goodsSupplierPartAuxiliaryId) {
        this.goodsSupplierPartAuxiliaryId = goodsSupplierPartAuxiliaryId;
    }

    public String getGoodsUnspscCode() {
        return goodsUnspscCode;
    }

    public void setGoodsUnspscCode(final String goodsUnspscCode) {
        this.goodsUnspscCode = goodsUnspscCode;
    }

    public String getGoodsItemDescription() {
        return goodsItemDescription;
    }

    public void setGoodsItemDescription(final String goodsItemDescription) {
        this.goodsItemDescription = goodsItemDescription;
    }

    public String getGoodsSupplierContractLine() {
        return goodsSupplierContractLine;
    }

    public void setGoodsSupplierContractLine(final String goodsSupplierContractLine) {
        this.goodsSupplierContractLine = goodsSupplierContractLine;
    }

    public String getGoodsAlternateSupplierContract() {
        return goodsAlternateSupplierContract;
    }

    public void setGoodsAlternateSupplierContract(final String goodsAlternateSupplierContract) {
        this.goodsAlternateSupplierContract = goodsAlternateSupplierContract;
    }

    public String getGoodsCommodityCode() {
        return goodsCommodityCode;
    }

    public void setGoodsCommodityCode(final String goodsCommodityCode) {
        this.goodsCommodityCode = goodsCommodityCode;
    }

    public String getGoodsCommodityCodeType() {
        return goodsCommodityCodeType;
    }

    public void setGoodsCommodityCodeType(final String goodsCommodityCodeType) {
        this.goodsCommodityCodeType = goodsCommodityCodeType;
    }

    public String getGoodsPaymentStatus() {
        return goodsPaymentStatus;
    }

    public void setGoodsPaymentStatus(final String goodsPaymentStatus) {
        this.goodsPaymentStatus = goodsPaymentStatus;
    }

    public String getGoodsInvoiceStatus() {
        return goodsInvoiceStatus;
    }

    public void setGoodsInvoiceStatus(final String goodsInvoiceStatus) {
        this.goodsInvoiceStatus = goodsInvoiceStatus;
    }

    public String getGoodsReceivingStatus() {
        return goodsReceivingStatus;
    }

    public void setGoodsReceivingStatus(final String goodsReceivingStatus) {
        this.goodsReceivingStatus = goodsReceivingStatus;
    }

    public String getGoodsShippingStatus() {
        return goodsShippingStatus;
    }

    public void setGoodsShippingStatus(final String goodsShippingStatus) {
        this.goodsShippingStatus = goodsShippingStatus;
    }

    public String getGoodsTrackingStatus() {
        return goodsTrackingStatus;
    }

    public void setGoodsTrackingStatus(final String goodsTrackingStatus) {
        this.goodsTrackingStatus = goodsTrackingStatus;
    }

    public String getGoodsResourceCategory() {
        return goodsResourceCategory;
    }

    public void setGoodsResourceCategory(final String goodsResourceCategory) {
        this.goodsResourceCategory = goodsResourceCategory;
    }

    public String getGoodsTaxApplicability() {
        return goodsTaxApplicability;
    }

    public void setGoodsTaxApplicability(final String goodsTaxApplicability) {
        this.goodsTaxApplicability = goodsTaxApplicability;
    }

    public String getGoodsTaxCode() {
        return goodsTaxCode;
    }

    public void setGoodsTaxCode(final String goodsTaxCode) {
        this.goodsTaxCode = goodsTaxCode;
    }

    public String getGoodsTaxRate1() {
        return goodsTaxRate1;
    }

    public void setGoodsTaxRate1(final String goodsTaxRate1) {
        this.goodsTaxRate1 = goodsTaxRate1;
    }

    public String getGoodsTaxRecoverability1() {
        return goodsTaxRecoverability1;
    }

    public void setGoodsTaxRecoverability1(final String goodsTaxRecoverability1) {
        this.goodsTaxRecoverability1 = goodsTaxRecoverability1;
    }

    public String getGoodsTaxOption1() {
        return goodsTaxOption1;
    }

    public void setGoodsTaxOption1(final String goodsTaxOption1) {
        this.goodsTaxOption1 = goodsTaxOption1;
    }

    public String getGoodsTaxRate2() {
        return goodsTaxRate2;
    }

    public void setGoodsTaxRate2(final String goodsTaxRate2) {
        this.goodsTaxRate2 = goodsTaxRate2;
    }

    public String getGoodsTaxRecoverability2() {
        return goodsTaxRecoverability2;
    }

    public void setGoodsTaxRecoverability2(final String goodsTaxRecoverability2) {
        this.goodsTaxRecoverability2 = goodsTaxRecoverability2;
    }

    public String getGoodsTaxOption2() {
        return goodsTaxOption2;
    }

    public void setGoodsTaxOption2(final String goodsTaxOption2) {
        this.goodsTaxOption2 = goodsTaxOption2;
    }

    public String getGoodsTaxRate3() {
        return goodsTaxRate3;
    }

    public void setGoodsTaxRate3(final String goodsTaxRate3) {
        this.goodsTaxRate3 = goodsTaxRate3;
    }

    public String getGoodsTaxRecoverability3() {
        return goodsTaxRecoverability3;
    }

    public void setGoodsTaxRecoverability3(final String goodsTaxRecoverability3) {
        this.goodsTaxRecoverability3 = goodsTaxRecoverability3;
    }

    public String getGoodsTaxOption3() {
        return goodsTaxOption3;
    }

    public void setGoodsTaxOption3(final String goodsTaxOption3) {
        this.goodsTaxOption3 = goodsTaxOption3;
    }

    public String getGoodsTaxRate4() {
        return goodsTaxRate4;
    }

    public void setGoodsTaxRate4(final String goodsTaxRate4) {
        this.goodsTaxRate4 = goodsTaxRate4;
    }

    public String getGoodsTaxRecoverability4() {
        return goodsTaxRecoverability4;
    }

    public void setGoodsTaxRecoverability4(final String goodsTaxRecoverability4) {
        this.goodsTaxRecoverability4 = goodsTaxRecoverability4;
    }

    public String getGoodsTaxOption4() {
        return goodsTaxOption4;
    }

    public void setGoodsTaxOption4(final String goodsTaxOption4) {
        this.goodsTaxOption4 = goodsTaxOption4;
    }

    public String getGoodsTaxRate5() {
        return goodsTaxRate5;
    }

    public void setGoodsTaxRate5(final String goodsTaxRate5) {
        this.goodsTaxRate5 = goodsTaxRate5;
    }

    public String getGoodsTaxRecoverability5() {
        return goodsTaxRecoverability5;
    }

    public void setGoodsTaxRecoverability5(final String goodsTaxRecoverability5) {
        this.goodsTaxRecoverability5 = goodsTaxRecoverability5;
    }

    public String getGoodsTaxOption5() {
        return goodsTaxOption5;
    }

    public void setGoodsTaxOption5(final String goodsTaxOption5) {
        this.goodsTaxOption5 = goodsTaxOption5;
    }

    public String getGoodsTaxRate6() {
        return goodsTaxRate6;
    }

    public void setGoodsTaxRate6(final String goodsTaxRate6) {
        this.goodsTaxRate6 = goodsTaxRate6;
    }

    public String getGoodsTaxRecoverability6() {
        return goodsTaxRecoverability6;
    }

    public void setGoodsTaxRecoverability6(final String goodsTaxRecoverability6) {
        this.goodsTaxRecoverability6 = goodsTaxRecoverability6;
    }

    public String getGoodsTaxOption6() {
        return goodsTaxOption6;
    }

    public void setGoodsTaxOption6(final String goodsTaxOption6) {
        this.goodsTaxOption6 = goodsTaxOption6;
    }

    public String getGoodsPackagingString() {
        return goodsPackagingString;
    }

    public void setGoodsPackagingString(final String goodsPackagingString) {
        this.goodsPackagingString = goodsPackagingString;
    }

    public String getGoodsQuantity() {
        return goodsQuantity;
    }

    public void setGoodsQuantity(final String goodsQuantity) {
        this.goodsQuantity = goodsQuantity;
    }

    public String getGoodsUnitOfMeasure() {
        return goodsUnitOfMeasure;
    }

    public void setGoodsUnitOfMeasure(final String goodsUnitOfMeasure) {
        this.goodsUnitOfMeasure = goodsUnitOfMeasure;
    }

    public String getGoodsUnitCost() {
        return goodsUnitCost;
    }

    public void setGoodsUnitCost(final String goodsUnitCost) {
        this.goodsUnitCost = goodsUnitCost;
    }

    public String getGoodsRequestedAsNoCharge() {
        return goodsRequestedAsNoCharge;
    }

    public void setGoodsRequestedAsNoCharge(final String goodsRequestedAsNoCharge) {
        this.goodsRequestedAsNoCharge = goodsRequestedAsNoCharge;
    }

    public String getGoodsExtendedAmount() {
        return goodsExtendedAmount;
    }

    public void setGoodsExtendedAmount(final String goodsExtendedAmount) {
        this.goodsExtendedAmount = goodsExtendedAmount;
    }

    public String getGoodsLotSerialInformation() {
        return goodsLotSerialInformation;
    }

    public void setGoodsLotSerialInformation(final String goodsLotSerialInformation) {
        this.goodsLotSerialInformation = goodsLotSerialInformation;
    }

    public String getGoodsLotNumber() {
        return goodsLotNumber;
    }

    public void setGoodsLotNumber(final String goodsLotNumber) {
        this.goodsLotNumber = goodsLotNumber;
    }

    public String getGoodsSerialNumber() {
        return goodsSerialNumber;
    }

    public void setGoodsSerialNumber(final String goodsSerialNumber) {
        this.goodsSerialNumber = goodsSerialNumber;
    }

    public String getGoodsDueDate() {
        return goodsDueDate;
    }

    public void setGoodsDueDate(final String goodsDueDate) {
        this.goodsDueDate = goodsDueDate;
    }

    public String getGoodsDeliveryType() {
        return goodsDeliveryType;
    }

    public void setGoodsDeliveryType(final String goodsDeliveryType) {
        this.goodsDeliveryType = goodsDeliveryType;
    }

    public String getGoodsPrepaid() {
        return goodsPrepaid;
    }

    public void setGoodsPrepaid(final String goodsPrepaid) {
        this.goodsPrepaid = goodsPrepaid;
    }

    public String getGoodsDownPayment() {
        return goodsDownPayment;
    }

    public void setGoodsDownPayment(final String goodsDownPayment) {
        this.goodsDownPayment = goodsDownPayment;
    }

    public String getGoodsRetention() {
        return goodsRetention;
    }

    public void setGoodsRetention(final String goodsRetention) {
        this.goodsRetention = goodsRetention;
    }

    public String getGoodsRequestedDeliveryDate() {
        return goodsRequestedDeliveryDate;
    }

    public void setGoodsRequestedDeliveryDate(final String goodsRequestedDeliveryDate) {
        this.goodsRequestedDeliveryDate = goodsRequestedDeliveryDate;
    }

    public String getGoodsBudgetDate() {
        return goodsBudgetDate;
    }

    public void setGoodsBudgetDate(final String goodsBudgetDate) {
        this.goodsBudgetDate = goodsBudgetDate;
    }

    public String getGoodsMemo() {
        return goodsMemo;
    }

    public void setGoodsMemo(final String goodsMemo) {
        this.goodsMemo = goodsMemo;
    }

    public String getGoodsShipToAddress() {
        return goodsShipToAddress;
    }

    public void setGoodsShipToAddress(final String goodsShipToAddress) {
        this.goodsShipToAddress = goodsShipToAddress;
    }

    public String getGoodsShipToGlobalLocationNumber() {
        return goodsShipToGlobalLocationNumber;
    }

    public void setGoodsShipToGlobalLocationNumber(final String goodsShipToGlobalLocationNumber) {
        this.goodsShipToGlobalLocationNumber = goodsShipToGlobalLocationNumber;
    }

    public String getGoodsShipToLocationIdentifier() {
        return goodsShipToLocationIdentifier;
    }

    public void setGoodsShipToLocationIdentifier(final String goodsShipToLocationIdentifier) {
        this.goodsShipToLocationIdentifier = goodsShipToLocationIdentifier;
    }

    public String getGoodsShipToContact() {
        return goodsShipToContact;
    }

    public void setGoodsShipToContact(final String goodsShipToContact) {
        this.goodsShipToContact = goodsShipToContact;
    }

    public String getGoodsRequester() {
        return goodsRequester;
    }

    public void setGoodsRequester(final String goodsRequester) {
        this.goodsRequester = goodsRequester;
    }

    public String getGoodsDeliverToLocation() {
        return goodsDeliverToLocation;
    }

    public void setGoodsDeliverToLocation(final String goodsDeliverToLocation) {
        this.goodsDeliverToLocation = goodsDeliverToLocation;
    }

    public String getGoodsDeliverToLocationGln() {
        return goodsDeliverToLocationGln;
    }

    public void setGoodsDeliverToLocationGln(final String goodsDeliverToLocationGln) {
        this.goodsDeliverToLocationGln = goodsDeliverToLocationGln;
    }

    public String getGoodsDeliverToLocationIdentifier() {
        return goodsDeliverToLocationIdentifier;
    }

    public void setGoodsDeliverToLocationIdentifier(final String goodsDeliverToLocationIdentifier) {
        this.goodsDeliverToLocationIdentifier = goodsDeliverToLocationIdentifier;
    }

    public String getGoodsSupplierContract() {
        return goodsSupplierContract;
    }

    public void setGoodsSupplierContract(final String goodsSupplierContract) {
        this.goodsSupplierContract = goodsSupplierContract;
    }

    public String getGoodsExternalSupplierInvoiceSource() {
        return goodsExternalSupplierInvoiceSource;
    }

    public void setGoodsExternalSupplierInvoiceSource(final String goodsExternalSupplierInvoiceSource) {
        this.goodsExternalSupplierInvoiceSource = goodsExternalSupplierInvoiceSource;
    }

    public String getGoodsRequisitionLine() {
        return goodsRequisitionLine;
    }

    public void setGoodsRequisitionLine(final String goodsRequisitionLine) {
        this.goodsRequisitionLine = goodsRequisitionLine;
    }

    public String getGoodsStorageLocation() {
        return goodsStorageLocation;
    }

    public void setGoodsStorageLocation(final String goodsStorageLocation) {
        this.goodsStorageLocation = goodsStorageLocation;
    }

    public String getGoodsCostCenter() {
        return goodsCostCenter;
    }

    public void setGoodsCostCenter(final String goodsCostCenter) {
        this.goodsCostCenter = goodsCostCenter;
    }

    public String getGoodsCostCenterExternalSupplierInvoiceSource() {
        return goodsCostCenterExternalSupplierInvoiceSource;
    }

    public void setGoodsCostCenterExternalSupplierInvoiceSource(final String goodsCostCenterExternalSupplierInvoiceSource) {
        this.goodsCostCenterExternalSupplierInvoiceSource = goodsCostCenterExternalSupplierInvoiceSource;
    }

    public String getGoodsProject() {
        return goodsProject;
    }

    public void setGoodsProject(final String goodsProject) {
        this.goodsProject = goodsProject;
    }

    public String getGoodsProjectExternalSupplierInvoiceSource() {
        return goodsProjectExternalSupplierInvoiceSource;
    }

    public void setGoodsProjectExternalSupplierInvoiceSource(final String goodsProjectExternalSupplierInvoiceSource) {
        this.goodsProjectExternalSupplierInvoiceSource = goodsProjectExternalSupplierInvoiceSource;
    }

    public String getGoodsGrant() {
        return goodsGrant;
    }

    public void setGoodsGrant(final String goodsGrant) {
        this.goodsGrant = goodsGrant;
    }

    public String getGoodsGrantExternalSupplierInvoiceSource() {
        return goodsGrantExternalSupplierInvoiceSource;
    }

    public void setGoodsGrantExternalSupplierInvoiceSource(final String goodsGrantExternalSupplierInvoiceSource) {
        this.goodsGrantExternalSupplierInvoiceSource = goodsGrantExternalSupplierInvoiceSource;
    }

    public String getGoodsGift() {
        return goodsGift;
    }

    public void setGoodsGift(final String goodsGift) {
        this.goodsGift = goodsGift;
    }

    public String getGoodsGiftExternalSupplierInvoiceSource() {
        return goodsGiftExternalSupplierInvoiceSource;
    }

    public void setGoodsGiftExternalSupplierInvoiceSource(final String goodsGiftExternalSupplierInvoiceSource) {
        this.goodsGiftExternalSupplierInvoiceSource = goodsGiftExternalSupplierInvoiceSource;
    }

    public String getGoodsFund() {
        return goodsFund;
    }

    public void setGoodsFund(final String goodsFund) {
        this.goodsFund = goodsFund;
    }

    public String getGoodsFundExternalSupplierInvoiceSource() {
        return goodsFundExternalSupplierInvoiceSource;
    }

    public void setGoodsFundExternalSupplierInvoiceSource(final String goodsFundExternalSupplierInvoiceSource) {
        this.goodsFundExternalSupplierInvoiceSource = goodsFundExternalSupplierInvoiceSource;
    }

    public String getGoodsLineSplitRowId() {
        return goodsLineSplitRowId;
    }

    public void setGoodsLineSplitRowId(final String goodsLineSplitRowId) {
        this.goodsLineSplitRowId = goodsLineSplitRowId;
    }

    public String getGoodsExistingBusinessDocumentLineSplitId() {
        return goodsExistingBusinessDocumentLineSplitId;
    }

    public void setGoodsExistingBusinessDocumentLineSplitId(final String goodsExistingBusinessDocumentLineSplitId) {
        this.goodsExistingBusinessDocumentLineSplitId = goodsExistingBusinessDocumentLineSplitId;
    }

    public String getGoodsNewBusinessDocumentLineSplitId() {
        return goodsNewBusinessDocumentLineSplitId;
    }

    public void setGoodsNewBusinessDocumentLineSplitId(final String goodsNewBusinessDocumentLineSplitId) {
        this.goodsNewBusinessDocumentLineSplitId = goodsNewBusinessDocumentLineSplitId;
    }

    public String getGoodsLineSplitQuantity() {
        return goodsLineSplitQuantity;
    }

    public void setGoodsLineSplitQuantity(final String goodsLineSplitQuantity) {
        this.goodsLineSplitQuantity = goodsLineSplitQuantity;
    }

    public String getGoodsLineSplitExtendedAmount() {
        return goodsLineSplitExtendedAmount;
    }

    public void setGoodsLineSplitExtendedAmount(final String goodsLineSplitExtendedAmount) {
        this.goodsLineSplitExtendedAmount = goodsLineSplitExtendedAmount;
    }

    public String getGoodsLineSplitBudgetDate() {
        return goodsLineSplitBudgetDate;
    }

    public void setGoodsLineSplitBudgetDate(final String goodsLineSplitBudgetDate) {
        this.goodsLineSplitBudgetDate = goodsLineSplitBudgetDate;
    }

    public String getGoodsLineSplitMemo() {
        return goodsLineSplitMemo;
    }

    public void setGoodsLineSplitMemo(final String goodsLineSplitMemo) {
        this.goodsLineSplitMemo = goodsLineSplitMemo;
    }

    public String getGoodsLineSplitAllocation() {
        return goodsLineSplitAllocation;
    }

    public void setGoodsLineSplitAllocation(final String goodsLineSplitAllocation) {
        this.goodsLineSplitAllocation = goodsLineSplitAllocation;
    }

    public String getGoodsLineSplitCostCenter() {
        return goodsLineSplitCostCenter;
    }

    public void setGoodsLineSplitCostCenter(final String goodsLineSplitCostCenter) {
        this.goodsLineSplitCostCenter = goodsLineSplitCostCenter;
    }

    public String getGoodsLineSplitCostCenterExternalSupplierInvoiceSource() {
        return goodsLineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public void setGoodsLineSplitCostCenterExternalSupplierInvoiceSource(
            String goodsLineSplitCostCenterExternalSupplierInvoiceSource) {
        this.goodsLineSplitCostCenterExternalSupplierInvoiceSource = goodsLineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public String getGoodsLineSplitProject() {
        return goodsLineSplitProject;
    }

    public void setGoodsLineSplitProject(final String goodsLineSplitProject) {
        this.goodsLineSplitProject = goodsLineSplitProject;
    }

    public String getGoodsLineSplitProjectExternalSupplierInvoiceSource() {
        return goodsLineSplitProjectExternalSupplierInvoiceSource;
    }

    public void setGoodsLineSplitProjectExternalSupplierInvoiceSource(
            String goodsLineSplitProjectExternalSupplierInvoiceSource) {
        this.goodsLineSplitProjectExternalSupplierInvoiceSource = goodsLineSplitProjectExternalSupplierInvoiceSource;
    }

    public String getGoodsLineSplitGrant() {
        return goodsLineSplitGrant;
    }

    public void setGoodsLineSplitGrant(final String goodsLineSplitGrant) {
        this.goodsLineSplitGrant = goodsLineSplitGrant;
    }

    public String getGoodsLineSplitGrantExternalSupplierInvoiceSource() {
        return goodsLineSplitGrantExternalSupplierInvoiceSource;
    }

    public void setGoodsLineSplitGrantExternalSupplierInvoiceSource(
            String goodsLineSplitGrantExternalSupplierInvoiceSource) {
        this.goodsLineSplitGrantExternalSupplierInvoiceSource = goodsLineSplitGrantExternalSupplierInvoiceSource;
    }

    public String getGoodsLineSplitGift() {
        return goodsLineSplitGift;
    }

    public void setGoodsLineSplitGift(final String goodsLineSplitGift) {
        this.goodsLineSplitGift = goodsLineSplitGift;
    }

    public String getGoodsLineSplitGiftExternalSupplierInvoiceSource() {
        return goodsLineSplitGiftExternalSupplierInvoiceSource;
    }

    public void setGoodsLineSplitGiftExternalSupplierInvoiceSource(
            final String goodsLineSplitGiftExternalSupplierInvoiceSource) {
        this.goodsLineSplitGiftExternalSupplierInvoiceSource = goodsLineSplitGiftExternalSupplierInvoiceSource;
    }

    public String getGoodsLineSplitFund() {
        return goodsLineSplitFund;
    }

    public void setGoodsLineSplitFund(final String goodsLineSplitFund) {
        this.goodsLineSplitFund = goodsLineSplitFund;
    }

    public String getGoodsLineSplitFundExternalSupplierInvoiceSource() {
        return goodsLineSplitFundExternalSupplierInvoiceSource;
    }

    public void setGoodsLineSplitFundExternalSupplierInvoiceSource(
            final String goodsLineSplitFundExternalSupplierInvoiceSource) {
        this.goodsLineSplitFundExternalSupplierInvoiceSource = goodsLineSplitFundExternalSupplierInvoiceSource;
    }

    public String getGoodsAlternateItemIdentifierRowId() {
        return goodsAlternateItemIdentifierRowId;
    }

    public void setGoodsAlternateItemIdentifierRowId(final String goodsAlternateItemIdentifierRowId) {
        this.goodsAlternateItemIdentifierRowId = goodsAlternateItemIdentifierRowId;
    }

    public String getGoodsAlternateItemIdentifierType() {
        return goodsAlternateItemIdentifierType;
    }

    public void setGoodsAlternateItemIdentifierType(final String goodsAlternateItemIdentifierType) {
        this.goodsAlternateItemIdentifierType = goodsAlternateItemIdentifierType;
    }

    public String getGoodsAlternateItemIdentifierValue() {
        return goodsAlternateItemIdentifierValue;
    }

    public void setGoodsAlternateItemIdentifierValue(final String goodsAlternateItemIdentifierValue) {
        this.goodsAlternateItemIdentifierValue = goodsAlternateItemIdentifierValue;
    }

    public String getGoodsAlternateItemIdentifierUnitOfMeasure() {
        return goodsAlternateItemIdentifierUnitOfMeasure;
    }

    public void setGoodsAlternateItemIdentifierUnitOfMeasure(final String goodsAlternateItemIdentifierUnitOfMeasure) {
        this.goodsAlternateItemIdentifierUnitOfMeasure = goodsAlternateItemIdentifierUnitOfMeasure;
    }

    public String getGoodsAlternateItemIdentifierManufacturer() {
        return goodsAlternateItemIdentifierManufacturer;
    }

    public void setGoodsAlternateItemIdentifierManufacturer(final String goodsAlternateItemIdentifierManufacturer) {
        this.goodsAlternateItemIdentifierManufacturer = goodsAlternateItemIdentifierManufacturer;
    }

    public String getGoodsItemTag() {
        return goodsItemTag;
    }

    public void setGoodsItemTag(final String goodsItemTag) {
        this.goodsItemTag = goodsItemTag;
    }

    public String getGoodsCloseStatus() {
        return goodsCloseStatus;
    }

    public void setGoodsCloseStatus(final String goodsCloseStatus) {
        this.goodsCloseStatus = goodsCloseStatus;
    }

    public String getGoodsClosedRowId() {
        return goodsClosedRowId;
    }

    public void setGoodsClosedRowId(final String goodsClosedRowId) {
        this.goodsClosedRowId = goodsClosedRowId;
    }

    public String getGoodsClosedOn() {
        return goodsClosedOn;
    }

    public void setGoodsClosedOn(final String goodsClosedOn) {
        this.goodsClosedOn = goodsClosedOn;
    }

    public String getGoodsClosedBy() {
        return goodsClosedBy;
    }

    public void setGoodsClosedBy(final String goodsClosedBy) {
        this.goodsClosedBy = goodsClosedBy;
    }

    public String getGoodsClosedReasonCode() {
        return goodsClosedReasonCode;
    }

    public void setGoodsClosedReasonCode(final String goodsClosedReasonCode) {
        this.goodsClosedReasonCode = goodsClosedReasonCode;
    }

    public String getServiceRowId() {
        return serviceRowId;
    }

    public void setServiceRowId(final String serviceRowId) {
        this.serviceRowId = serviceRowId;
    }

    public String getServiceCatalogItem() {
        return serviceCatalogItem;
    }

    public void setServiceCatalogItem(final String serviceCatalogItem) {
        this.serviceCatalogItem = serviceCatalogItem;
    }

    public String getServiceOrderLineId() {
        return serviceOrderLineId;
    }

    public void setServiceOrderLineId(final String serviceOrderLineId) {
        this.serviceOrderLineId = serviceOrderLineId;
    }

    public String getServiceLineNumber() {
        return serviceLineNumber;
    }

    public void setServiceLineNumber(final String serviceLineNumber) {
        this.serviceLineNumber = serviceLineNumber;
    }

    public String getServiceLineCompany() {
        return serviceLineCompany;
    }

    public void setServiceLineCompany(final String serviceLineCompany) {
        this.serviceLineCompany = serviceLineCompany;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(final String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getServiceSupplierContractLine() {
        return serviceSupplierContractLine;
    }

    public void setServiceSupplierContractLine(final String serviceSupplierContractLine) {
        this.serviceSupplierContractLine = serviceSupplierContractLine;
    }

    public String getServiceAlternateSupplierContract() {
        return serviceAlternateSupplierContract;
    }

    public void setServiceAlternateSupplierContract(final String serviceAlternateSupplierContract) {
        this.serviceAlternateSupplierContract = serviceAlternateSupplierContract;
    }

    public String getServiceCommodityCode() {
        return serviceCommodityCode;
    }

    public void setServiceCommodityCode(final String serviceCommodityCode) {
        this.serviceCommodityCode = serviceCommodityCode;
    }

    public String getServiceCommodityCodeType() {
        return serviceCommodityCodeType;
    }

    public void setServiceCommodityCodeType(final String serviceCommodityCodeType) {
        this.serviceCommodityCodeType = serviceCommodityCodeType;
    }

    public String getServicePaymentStatus() {
        return servicePaymentStatus;
    }

    public void setServicePaymentStatus(final String servicePaymentStatus) {
        this.servicePaymentStatus = servicePaymentStatus;
    }

    public String getServiceInvoiceStatus() {
        return serviceInvoiceStatus;
    }

    public void setServiceInvoiceStatus(final String serviceInvoiceStatus) {
        this.serviceInvoiceStatus = serviceInvoiceStatus;
    }

    public String getServiceReceivingStatus() {
        return serviceReceivingStatus;
    }

    public void setServiceReceivingStatus(final String serviceReceivingStatus) {
        this.serviceReceivingStatus = serviceReceivingStatus;
    }

    public String getServiceResourceCategory() {
        return serviceResourceCategory;
    }

    public void setServiceResourceCategory(final String serviceResourceCategory) {
        this.serviceResourceCategory = serviceResourceCategory;
    }

    public String getServiceTaxApplicability() {
        return serviceTaxApplicability;
    }

    public void setServiceTaxApplicability(final String serviceTaxApplicability) {
        this.serviceTaxApplicability = serviceTaxApplicability;
    }

    public String getServiceTaxCode() {
        return serviceTaxCode;
    }

    public void setServiceTaxCode(final String serviceTaxCode) {
        this.serviceTaxCode = serviceTaxCode;
    }

    public String getServiceTaxRate1() {
        return serviceTaxRate1;
    }

    public void setServiceTaxRate1(final String serviceTaxRate1) {
        this.serviceTaxRate1 = serviceTaxRate1;
    }

    public String getServiceTaxRecoverability1() {
        return serviceTaxRecoverability1;
    }

    public void setServiceTaxRecoverability1(final String serviceTaxRecoverability1) {
        this.serviceTaxRecoverability1 = serviceTaxRecoverability1;
    }

    public String getServiceTaxOption1() {
        return serviceTaxOption1;
    }

    public void setServiceTaxOption1(final String serviceTaxOption1) {
        this.serviceTaxOption1 = serviceTaxOption1;
    }

    public String getServiceTaxRate2() {
        return serviceTaxRate2;
    }

    public void setServiceTaxRate2(final String serviceTaxRate2) {
        this.serviceTaxRate2 = serviceTaxRate2;
    }

    public String getServiceTaxRecoverability2() {
        return serviceTaxRecoverability2;
    }

    public void setServiceTaxRecoverability2(final String serviceTaxRecoverability2) {
        this.serviceTaxRecoverability2 = serviceTaxRecoverability2;
    }

    public String getServiceTaxOption2() {
        return serviceTaxOption2;
    }

    public void setServiceTaxOption2(final String serviceTaxOption2) {
        this.serviceTaxOption2 = serviceTaxOption2;
    }

    public String getServiceTaxRate3() {
        return serviceTaxRate3;
    }

    public void setServiceTaxRate3(final String serviceTaxRate3) {
        this.serviceTaxRate3 = serviceTaxRate3;
    }

    public String getServiceTaxRecoverability3() {
        return serviceTaxRecoverability3;
    }

    public void setServiceTaxRecoverability3(final String serviceTaxRecoverability3) {
        this.serviceTaxRecoverability3 = serviceTaxRecoverability3;
    }

    public String getServiceTaxOption3() {
        return serviceTaxOption3;
    }

    public void setServiceTaxOption3(final String serviceTaxOption3) {
        this.serviceTaxOption3 = serviceTaxOption3;
    }

    public String getServiceTaxRate4() {
        return serviceTaxRate4;
    }

    public void setServiceTaxRate4(final String serviceTaxRate4) {
        this.serviceTaxRate4 = serviceTaxRate4;
    }

    public String getServiceTaxRecoverability4() {
        return serviceTaxRecoverability4;
    }

    public void setServiceTaxRecoverability4(final String serviceTaxRecoverability4) {
        this.serviceTaxRecoverability4 = serviceTaxRecoverability4;
    }

    public String getServiceTaxOption4() {
        return serviceTaxOption4;
    }

    public void setServiceTaxOption4(final String serviceTaxOption4) {
        this.serviceTaxOption4 = serviceTaxOption4;
    }

    public String getServiceTaxRate5() {
        return serviceTaxRate5;
    }

    public void setServiceTaxRate5(final String serviceTaxRate5) {
        this.serviceTaxRate5 = serviceTaxRate5;
    }

    public String getServiceTaxRecoverability5() {
        return serviceTaxRecoverability5;
    }

    public void setServiceTaxRecoverability5(final String serviceTaxRecoverability5) {
        this.serviceTaxRecoverability5 = serviceTaxRecoverability5;
    }

    public String getServiceTaxOption5() {
        return serviceTaxOption5;
    }

    public void setServiceTaxOption5(final String serviceTaxOption5) {
        this.serviceTaxOption5 = serviceTaxOption5;
    }

    public String getServiceTaxRate6() {
        return serviceTaxRate6;
    }

    public void setServiceTaxRate6(final String serviceTaxRate6) {
        this.serviceTaxRate6 = serviceTaxRate6;
    }

    public String getServiceTaxRecoverability6() {
        return serviceTaxRecoverability6;
    }

    public void setServiceTaxRecoverability6(final String serviceTaxRecoverability6) {
        this.serviceTaxRecoverability6 = serviceTaxRecoverability6;
    }

    public String getServiceTaxOption6() {
        return serviceTaxOption6;
    }

    public void setServiceTaxOption6(final String serviceTaxOption6) {
        this.serviceTaxOption6 = serviceTaxOption6;
    }

    public String getServiceExtendedAmount() {
        return serviceExtendedAmount;
    }

    public void setServiceExtendedAmount(final String serviceExtendedAmount) {
        this.serviceExtendedAmount = serviceExtendedAmount;
    }

    public String getServiceDueDate() {
        return serviceDueDate;
    }

    public void setServiceDueDate(final String serviceDueDate) {
        this.serviceDueDate = serviceDueDate;
    }

    public String getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(final String serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    public String getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(final String serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    public String getServicePrepaid() {
        return servicePrepaid;
    }

    public void setServicePrepaid(final String servicePrepaid) {
        this.servicePrepaid = servicePrepaid;
    }

    public String getServiceDownPayment() {
        return serviceDownPayment;
    }

    public void setServiceDownPayment(final String serviceDownPayment) {
        this.serviceDownPayment = serviceDownPayment;
    }

    public String getServiceRetention() {
        return serviceRetention;
    }

    public void setServiceRetention(final String serviceRetention) {
        this.serviceRetention = serviceRetention;
    }

    public String getServiceBudgetDate() {
        return serviceBudgetDate;
    }

    public void setServiceBudgetDate(final String serviceBudgetDate) {
        this.serviceBudgetDate = serviceBudgetDate;
    }

    public String getServiceMemo() {
        return serviceMemo;
    }

    public void setServiceMemo(final String serviceMemo) {
        this.serviceMemo = serviceMemo;
    }

    public String getServiceShipToAddress() {
        return serviceShipToAddress;
    }

    public void setServiceShipToAddress(final String serviceShipToAddress) {
        this.serviceShipToAddress = serviceShipToAddress;
    }

    public String getServiceShipToContact() {
        return serviceShipToContact;
    }

    public void setServiceShipToContact(final String serviceShipToContact) {
        this.serviceShipToContact = serviceShipToContact;
    }

    public String getServiceRequester() {
        return serviceRequester;
    }

    public void setServiceRequester(final String serviceRequester) {
        this.serviceRequester = serviceRequester;
    }

    public String getServiceDeliverToLocation() {
        return serviceDeliverToLocation;
    }

    public void setServiceDeliverToLocation(final String serviceDeliverToLocation) {
        this.serviceDeliverToLocation = serviceDeliverToLocation;
    }

    public String getServiceRequisitionLine() {
        return serviceRequisitionLine;
    }

    public void setServiceRequisitionLine(final String serviceRequisitionLine) {
        this.serviceRequisitionLine = serviceRequisitionLine;
    }

    public String getServiceSupplierContract() {
        return serviceSupplierContract;
    }

    public void setServiceSupplierContract(final String serviceSupplierContract) {
        this.serviceSupplierContract = serviceSupplierContract;
    }

    public String getServiceSupplierContractExternalSupplierInvoiceSource() {
        return serviceSupplierContractExternalSupplierInvoiceSource;
    }

    public void setServiceSupplierContractExternalSupplierInvoiceSource(
            String serviceSupplierContractExternalSupplierInvoiceSource) {
        this.serviceSupplierContractExternalSupplierInvoiceSource = serviceSupplierContractExternalSupplierInvoiceSource;
    }

    public String getServiceStorageLocation() {
        return serviceStorageLocation;
    }

    public void setServiceStorageLocation(final String serviceStorageLocation) {
        this.serviceStorageLocation = serviceStorageLocation;
    }

    public String getServiceCostCenter() {
        return serviceCostCenter;
    }

    public void setServiceCostCenter(final String serviceCostCenter) {
        this.serviceCostCenter = serviceCostCenter;
    }

    public String getServiceCostCenterExternalSupplierInvoiceSource() {
        return serviceCostCenterExternalSupplierInvoiceSource;
    }

    public void setServiceCostCenterExternalSupplierInvoiceSource(final String serviceCostCenterExternalSupplierInvoiceSource) {
        this.serviceCostCenterExternalSupplierInvoiceSource = serviceCostCenterExternalSupplierInvoiceSource;
    }

    public String getServiceProject() {
        return serviceProject;
    }

    public void setServiceProject(final String serviceProject) {
        this.serviceProject = serviceProject;
    }

    public String getServiceProjectExternalSupplierInvoiceSource() {
        return serviceProjectExternalSupplierInvoiceSource;
    }

    public void setServiceProjectExternalSupplierInvoiceSource(final String serviceProjectExternalSupplierInvoiceSource) {
        this.serviceProjectExternalSupplierInvoiceSource = serviceProjectExternalSupplierInvoiceSource;
    }

    public String getServiceGrant() {
        return serviceGrant;
    }

    public void setServiceGrant(final String serviceGrant) {
        this.serviceGrant = serviceGrant;
    }

    public String getServiceGrantExternalSupplierInvoiceSource() {
        return serviceGrantExternalSupplierInvoiceSource;
    }

    public void setServiceGrantExternalSupplierInvoiceSource(final String serviceGrantExternalSupplierInvoiceSource) {
        this.serviceGrantExternalSupplierInvoiceSource = serviceGrantExternalSupplierInvoiceSource;
    }

    public String getServiceGift() {
        return serviceGift;
    }

    public void setServiceGift(final String serviceGift) {
        this.serviceGift = serviceGift;
    }

    public String getServiceGiftExternalSupplierInvoiceSource() {
        return serviceGiftExternalSupplierInvoiceSource;
    }

    public void setServiceGiftExternalSupplierInvoiceSource(final String serviceGiftExternalSupplierInvoiceSource) {
        this.serviceGiftExternalSupplierInvoiceSource = serviceGiftExternalSupplierInvoiceSource;
    }

    public String getServiceFund() {
        return serviceFund;
    }

    public void setServiceFund(final String serviceFund) {
        this.serviceFund = serviceFund;
    }

    public String getServiceFundExternalSupplierInvoiceSource() {
        return serviceFundExternalSupplierInvoiceSource;
    }

    public void setServiceFundExternalSupplierInvoiceSource(final String serviceFundExternalSupplierInvoiceSource) {
        this.serviceFundExternalSupplierInvoiceSource = serviceFundExternalSupplierInvoiceSource;
    }

    public String getServiceLineSplitRowId() {
        return serviceLineSplitRowId;
    }

    public void setServiceLineSplitRowId(final String serviceLineSplitRowId) {
        this.serviceLineSplitRowId = serviceLineSplitRowId;
    }

    public String getServiceExistingBusinessDocumentLineSplitId() {
        return serviceExistingBusinessDocumentLineSplitId;
    }

    public void setServiceExistingBusinessDocumentLineSplitId(final String serviceExistingBusinessDocumentLineSplitId) {
        this.serviceExistingBusinessDocumentLineSplitId = serviceExistingBusinessDocumentLineSplitId;
    }

    public String getServiceNewBusinessDocumentLineSplitId() {
        return serviceNewBusinessDocumentLineSplitId;
    }

    public void setServiceNewBusinessDocumentLineSplitId(final String serviceNewBusinessDocumentLineSplitId) {
        this.serviceNewBusinessDocumentLineSplitId = serviceNewBusinessDocumentLineSplitId;
    }

    public String getServiceLineSplitQuantity() {
        return serviceLineSplitQuantity;
    }

    public void setServiceLineSplitQuantity(final String serviceLineSplitQuantity) {
        this.serviceLineSplitQuantity = serviceLineSplitQuantity;
    }

    public String getServiceLineSplitExtendedAmount() {
        return serviceLineSplitExtendedAmount;
    }

    public void setServiceLineSplitExtendedAmount(final String serviceLineSplitExtendedAmount) {
        this.serviceLineSplitExtendedAmount = serviceLineSplitExtendedAmount;
    }

    public String getServiceLineSplitBudgetDate() {
        return serviceLineSplitBudgetDate;
    }

    public void setServiceLineSplitBudgetDate(final String serviceLineSplitBudgetDate) {
        this.serviceLineSplitBudgetDate = serviceLineSplitBudgetDate;
    }

    public String getServiceLineSplitMemo() {
        return serviceLineSplitMemo;
    }

    public void setServiceLineSplitMemo(final String serviceLineSplitMemo) {
        this.serviceLineSplitMemo = serviceLineSplitMemo;
    }

    public String getServiceLineSplitAllocation() {
        return serviceLineSplitAllocation;
    }

    public void setServiceLineSplitAllocation(final String serviceLineSplitAllocation) {
        this.serviceLineSplitAllocation = serviceLineSplitAllocation;
    }

    public String getServiceLineSplitCostCenter() {
        return serviceLineSplitCostCenter;
    }

    public void setServiceLineSplitCostCenter(final String serviceLineSplitCostCenter) {
        this.serviceLineSplitCostCenter = serviceLineSplitCostCenter;
    }

    public String getServiceLineSplitCostCenterExternalSupplierInvoiceSource() {
        return serviceLineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public void setServiceLineSplitCostCenterExternalSupplierInvoiceSource(
            String serviceLineSplitCostCenterExternalSupplierInvoiceSource) {
        this.serviceLineSplitCostCenterExternalSupplierInvoiceSource = serviceLineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public String getServiceLineSplitProject() {
        return serviceLineSplitProject;
    }

    public void setServiceLineSplitProject(final String serviceLineSplitProject) {
        this.serviceLineSplitProject = serviceLineSplitProject;
    }

    public String getServiceLineSplitProjectExternalSupplierInvoiceSource() {
        return serviceLineSplitProjectExternalSupplierInvoiceSource;
    }

    public void setServiceLineSplitProjectExternalSupplierInvoiceSource(
            String serviceLineSplitProjectExternalSupplierInvoiceSource) {
        this.serviceLineSplitProjectExternalSupplierInvoiceSource = serviceLineSplitProjectExternalSupplierInvoiceSource;
    }

    public String getServiceLineSplitGrant() {
        return serviceLineSplitGrant;
    }

    public void setServiceLineSplitGrant(final String serviceLineSplitGrant) {
        this.serviceLineSplitGrant = serviceLineSplitGrant;
    }

    public String getServiceLineSplitGrantExternalSupplierInvoiceSource() {
        return serviceLineSplitGrantExternalSupplierInvoiceSource;
    }

    public void setServiceLineSplitGrantExternalSupplierInvoiceSource(
            String serviceLineSplitGrantExternalSupplierInvoiceSource) {
        this.serviceLineSplitGrantExternalSupplierInvoiceSource = serviceLineSplitGrantExternalSupplierInvoiceSource;
    }

    public String getServiceLineSplitGift() {
        return serviceLineSplitGift;
    }

    public void setServiceLineSplitGift(final String serviceLineSplitGift) {
        this.serviceLineSplitGift = serviceLineSplitGift;
    }

    public String getServiceLineSplitGiftExternalSupplierInvoiceSource() {
        return serviceLineSplitGiftExternalSupplierInvoiceSource;
    }

    public void setServiceLineSplitGiftExternalSupplierInvoiceSource(
            String serviceLineSplitGiftExternalSupplierInvoiceSource) {
        this.serviceLineSplitGiftExternalSupplierInvoiceSource = serviceLineSplitGiftExternalSupplierInvoiceSource;
    }

    public String getServiceLineSplitFund() {
        return serviceLineSplitFund;
    }

    public void setServiceLineSplitFund(final String serviceLineSplitFund) {
        this.serviceLineSplitFund = serviceLineSplitFund;
    }

    public String getServiceLineSplitFundExternalSupplierInvoiceSource() {
        return serviceLineSplitFundExternalSupplierInvoiceSource;
    }

    public void setServiceLineSplitFundExternalSupplierInvoiceSource(
            String serviceLineSplitFundExternalSupplierInvoiceSource) {
        this.serviceLineSplitFundExternalSupplierInvoiceSource = serviceLineSplitFundExternalSupplierInvoiceSource;
    }

    public String getServiceCloseStatus() {
        return serviceCloseStatus;
    }

    public void setServiceCloseStatus(final String serviceCloseStatus) {
        this.serviceCloseStatus = serviceCloseStatus;
    }

    public String getServiceClosedRowId() {
        return serviceClosedRowId;
    }

    public void setServiceClosedRowId(final String serviceClosedRowId) {
        this.serviceClosedRowId = serviceClosedRowId;
    }

    public String getServiceClosedOn() {
        return serviceClosedOn;
    }

    public void setServiceClosedOn(final String serviceClosedOn) {
        this.serviceClosedOn = serviceClosedOn;
    }

    public String getServiceClosedBy() {
        return serviceClosedBy;
    }

    public void setServiceClosedBy(final String serviceClosedBy) {
        this.serviceClosedBy = serviceClosedBy;
    }

    public String getServiceClosedReasonCode() {
        return serviceClosedReasonCode;
    }

    public void setServiceClosedReasonCode(final String serviceClosedReasonCode) {
        this.serviceClosedReasonCode = serviceClosedReasonCode;
    }

    public String getDeliverablesRowId() {
        return deliverablesRowId;
    }

    public void setDeliverablesRowId(final String deliverablesRowId) {
        this.deliverablesRowId = deliverablesRowId;
    }

    public String getDeliverablesOrderLineId() {
        return deliverablesOrderLineId;
    }

    public void setDeliverablesOrderLineId(final String deliverablesOrderLineId) {
        this.deliverablesOrderLineId = deliverablesOrderLineId;
    }

    public String getDeliverablesLineNumber() {
        return deliverablesLineNumber;
    }

    public void setDeliverablesLineNumber(final String deliverablesLineNumber) {
        this.deliverablesLineNumber = deliverablesLineNumber;
    }

    public String getDeliverablesLineCompany() {
        return deliverablesLineCompany;
    }

    public void setDeliverablesLineCompany(final String deliverablesLineCompany) {
        this.deliverablesLineCompany = deliverablesLineCompany;
    }

    public String getDeliverablesProject() {
        return deliverablesProject;
    }

    public void setDeliverablesProject(final String deliverablesProject) {
        this.deliverablesProject = deliverablesProject;
    }

    public String getDeliverablesSupplierContractLine() {
        return deliverablesSupplierContractLine;
    }

    public void setDeliverablesSupplierContractLine(final String deliverablesSupplierContractLine) {
        this.deliverablesSupplierContractLine = deliverablesSupplierContractLine;
    }

    public String getDeliverablesAlternateSupplierContract() {
        return deliverablesAlternateSupplierContract;
    }

    public void setDeliverablesAlternateSupplierContract(final String deliverablesAlternateSupplierContract) {
        this.deliverablesAlternateSupplierContract = deliverablesAlternateSupplierContract;
    }

    public String getDeliverablesReceivingStatus() {
        return deliverablesReceivingStatus;
    }

    public void setDeliverablesReceivingStatus(final String deliverablesReceivingStatus) {
        this.deliverablesReceivingStatus = deliverablesReceivingStatus;
    }

    public String getDeliverablesPaymentStatus() {
        return deliverablesPaymentStatus;
    }

    public void setDeliverablesPaymentStatus(final String deliverablesPaymentStatus) {
        this.deliverablesPaymentStatus = deliverablesPaymentStatus;
    }

    public String getDeliverablesInvoiceStatus() {
        return deliverablesInvoiceStatus;
    }

    public void setDeliverablesInvoiceStatus(final String deliverablesInvoiceStatus) {
        this.deliverablesInvoiceStatus = deliverablesInvoiceStatus;
    }

    public String getDeliverablesPrepaid() {
        return deliverablesPrepaid;
    }

    public void setDeliverablesPrepaid(final String deliverablesPrepaid) {
        this.deliverablesPrepaid = deliverablesPrepaid;
    }

    public String getDeliverablesDownPayment() {
        return deliverablesDownPayment;
    }

    public void setDeliverablesDownPayment(final String deliverablesDownPayment) {
        this.deliverablesDownPayment = deliverablesDownPayment;
    }

    public String getDeliverablesRetention() {
        return deliverablesRetention;
    }

    public void setDeliverablesRetention(final String deliverablesRetention) {
        this.deliverablesRetention = deliverablesRetention;
    }

    public String getDeliverablesBudgetDate() {
        return deliverablesBudgetDate;
    }

    public void setDeliverablesBudgetDate(final String deliverablesBudgetDate) {
        this.deliverablesBudgetDate = deliverablesBudgetDate;
    }

    public String getDeliverablesMemo() {
        return deliverablesMemo;
    }

    public void setDeliverablesMemo(final String deliverablesMemo) {
        this.deliverablesMemo = deliverablesMemo;
    }

    public String getDeliverablesRequisitionLine() {
        return deliverablesRequisitionLine;
    }

    public void setDeliverablesRequisitionLine(final String deliverablesRequisitionLine) {
        this.deliverablesRequisitionLine = deliverablesRequisitionLine;
    }

    public String getDeliverablesResourceCategory() {
        return deliverablesResourceCategory;
    }

    public void setDeliverablesResourceCategory(final String deliverablesResourceCategory) {
        this.deliverablesResourceCategory = deliverablesResourceCategory;
    }

    public String getDeliverablesProjectPlanPhaseRowId() {
        return deliverablesProjectPlanPhaseRowId;
    }

    public void setDeliverablesProjectPlanPhaseRowId(final String deliverablesProjectPlanPhaseRowId) {
        this.deliverablesProjectPlanPhaseRowId = deliverablesProjectPlanPhaseRowId;
    }

    public String getDeliverablesProjectPlanPhase() {
        return deliverablesProjectPlanPhase;
    }

    public void setDeliverablesProjectPlanPhase(final String deliverablesProjectPlanPhase) {
        this.deliverablesProjectPlanPhase = deliverablesProjectPlanPhase;
    }

    public String getDeliverablesProjectPlanTaskRowId() {
        return deliverablesProjectPlanTaskRowId;
    }

    public void setDeliverablesProjectPlanTaskRowId(final String deliverablesProjectPlanTaskRowId) {
        this.deliverablesProjectPlanTaskRowId = deliverablesProjectPlanTaskRowId;
    }

    public String getDeliverablesProjectPlanTask() {
        return deliverablesProjectPlanTask;
    }

    public void setDeliverablesProjectPlanTask(final String deliverablesProjectPlanTask) {
        this.deliverablesProjectPlanTask = deliverablesProjectPlanTask;
    }

    public String getDeliverablesProjectSubtaskRowId() {
        return deliverablesProjectSubtaskRowId;
    }

    public void setDeliverablesProjectSubtaskRowId(final String deliverablesProjectSubtaskRowId) {
        this.deliverablesProjectSubtaskRowId = deliverablesProjectSubtaskRowId;
    }

    public String getDeliverablesProjectSubtaskDescription() {
        return deliverablesProjectSubtaskDescription;
    }

    public void setDeliverablesProjectSubtaskDescription(final String deliverablesProjectSubtaskDescription) {
        this.deliverablesProjectSubtaskDescription = deliverablesProjectSubtaskDescription;
    }

    public String getDeliverablesProjectSubtaskAmount() {
        return deliverablesProjectSubtaskAmount;
    }

    public void setDeliverablesProjectSubtaskAmount(final String deliverablesProjectSubtaskAmount) {
        this.deliverablesProjectSubtaskAmount = deliverablesProjectSubtaskAmount;
    }

    public String getDeliverablesExtendedAmount() {
        return deliverablesExtendedAmount;
    }

    public void setDeliverablesExtendedAmount(final String deliverablesExtendedAmount) {
        this.deliverablesExtendedAmount = deliverablesExtendedAmount;
    }

    public String getDeliverablesWorktags() {
        return deliverablesWorktags;
    }

    public void setDeliverablesWorktags(final String deliverablesWorktags) {
        this.deliverablesWorktags = deliverablesWorktags;
    }

    public String getDeliverablesWorktagsExternalSupplierInvoiceSource() {
        return deliverablesWorktagsExternalSupplierInvoiceSource;
    }

    public void setDeliverablesWorktagsExternalSupplierInvoiceSource(
            String deliverablesWorktagsExternalSupplierInvoiceSource) {
        this.deliverablesWorktagsExternalSupplierInvoiceSource = deliverablesWorktagsExternalSupplierInvoiceSource;
    }

    public String getDeliverablesSupplierContract() {
        return deliverablesSupplierContract;
    }

    public void setDeliverablesSupplierContract(final String deliverablesSupplierContract) {
        this.deliverablesSupplierContract = deliverablesSupplierContract;
    }

    public String getDeliverablesSupplierContractExternalSupplierInvoiceSource() {
        return deliverablesSupplierContractExternalSupplierInvoiceSource;
    }

    public void setDeliverablesSupplierContractExternalSupplierInvoiceSource(
            String deliverablesSupplierContractExternalSupplierInvoiceSource) {
        this.deliverablesSupplierContractExternalSupplierInvoiceSource
                = deliverablesSupplierContractExternalSupplierInvoiceSource;
    }

    public String getDeliverablesLineSplitRowId() {
        return deliverablesLineSplitRowId;
    }

    public void setDeliverablesLineSplitRowId(final String deliverablesLineSplitRowId) {
        this.deliverablesLineSplitRowId = deliverablesLineSplitRowId;
    }

    public String getDeliverablesExistingBusinessDocumentLineSplitId() {
        return deliverablesExistingBusinessDocumentLineSplitId;
    }

    public void setDeliverablesExistingBusinessDocumentLineSplitId(
            final String deliverablesExistingBusinessDocumentLineSplitId) {
        this.deliverablesExistingBusinessDocumentLineSplitId = deliverablesExistingBusinessDocumentLineSplitId;
    }

    public String getDeliverablesNewBusinessDocumentLineSplitId() {
        return deliverablesNewBusinessDocumentLineSplitId;
    }

    public void setDeliverablesNewBusinessDocumentLineSplitId(final String deliverablesNewBusinessDocumentLineSplitId) {
        this.deliverablesNewBusinessDocumentLineSplitId = deliverablesNewBusinessDocumentLineSplitId;
    }

    public String getDeliverablesLineSplitQuantity() {
        return deliverablesLineSplitQuantity;
    }

    public void setDeliverablesLineSplitQuantity(final String deliverablesLineSplitQuantity) {
        this.deliverablesLineSplitQuantity = deliverablesLineSplitQuantity;
    }

    public String getDeliverablesLineSplitExtendedAmount() {
        return deliverablesLineSplitExtendedAmount;
    }

    public void setDeliverablesLineSplitExtendedAmount(final String deliverablesLineSplitExtendedAmount) {
        this.deliverablesLineSplitExtendedAmount = deliverablesLineSplitExtendedAmount;
    }

    public String getDeliverablesLineSplitBudgetDate() {
        return deliverablesLineSplitBudgetDate;
    }

    public void setDeliverablesLineSplitBudgetDate(final String deliverablesLineSplitBudgetDate) {
        this.deliverablesLineSplitBudgetDate = deliverablesLineSplitBudgetDate;
    }

    public String getDeliverablesLineSplitMemo() {
        return deliverablesLineSplitMemo;
    }

    public void setDeliverablesLineSplitMemo(final String deliverablesLineSplitMemo) {
        this.deliverablesLineSplitMemo = deliverablesLineSplitMemo;
    }

    public String getDeliverablesLineSplitAllocation() {
        return deliverablesLineSplitAllocation;
    }

    public void setDeliverablesLineSplitAllocation(final String deliverablesLineSplitAllocation) {
        this.deliverablesLineSplitAllocation = deliverablesLineSplitAllocation;
    }

    public String getDeliverablesLineSplitWorktag() {
        return deliverablesLineSplitWorktag;
    }

    public void setDeliverablesLineSplitWorktag(final String deliverablesLineSplitWorktag) {
        this.deliverablesLineSplitWorktag = deliverablesLineSplitWorktag;
    }

    public String getDeliverablesLineSplitWorktagExternalSupplierInvoiceSource() {
        return deliverablesLineSplitWorktagExternalSupplierInvoiceSource;
    }

    public void setDeliverablesLineSplitWorktagExternalSupplierInvoiceSource(
            String deliverablesLineSplitWorktagExternalSupplierInvoiceSource) {
        this.deliverablesLineSplitWorktagExternalSupplierInvoiceSource
                = deliverablesLineSplitWorktagExternalSupplierInvoiceSource;
    }

    public String getDeliverablesCloseStatus() {
        return deliverablesCloseStatus;
    }

    public void setDeliverablesCloseStatus(final String deliverablesCloseStatus) {
        this.deliverablesCloseStatus = deliverablesCloseStatus;
    }

    public String getDeliverablesClosedRowId() {
        return deliverablesClosedRowId;
    }

    public void setDeliverablesClosedRowId(final String deliverablesClosedRowId) {
        this.deliverablesClosedRowId = deliverablesClosedRowId;
    }

    public String getDeliverablesClosedOn() {
        return deliverablesClosedOn;
    }

    public void setDeliverablesClosedOn(final String deliverablesClosedOn) {
        this.deliverablesClosedOn = deliverablesClosedOn;
    }

    public String getDeliverablesClosedBy() {
        return deliverablesClosedBy;
    }

    public void setDeliverablesClosedBy(final String deliverablesClosedBy) {
        this.deliverablesClosedBy = deliverablesClosedBy;
    }

    public String getDeliverablesClosedReasonCode() {
        return deliverablesClosedReasonCode;
    }

    public void setDeliverablesClosedReasonCode(final String deliverablesClosedReasonCode) {
        this.deliverablesClosedReasonCode = deliverablesClosedReasonCode;
    }

    public String getTaxCodeRowId() {
        return taxCodeRowId;
    }

    public void setTaxCodeRowId(final String taxCodeRowId) {
        this.taxCodeRowId = taxCodeRowId;
    }

    public String getTaxCodeApplicability() {
        return taxCodeApplicability;
    }

    public void setTaxCodeApplicability(final String taxCodeApplicability) {
        this.taxCodeApplicability = taxCodeApplicability;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(final String taxCode) {
        this.taxCode = taxCode;
    }

    public String getTaxCodeAmount() {
        return taxCodeAmount;
    }

    public void setTaxCodeAmount(final String taxCodeAmount) {
        this.taxCodeAmount = taxCodeAmount;
    }

    public String getTaxRateRowId() {
        return taxRateRowId;
    }

    public void setTaxRateRowId(final String taxRateRowId) {
        this.taxRateRowId = taxRateRowId;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(final String taxRate) {
        this.taxRate = taxRate;
    }

    public String getTaxRateAmount() {
        return taxRateAmount;
    }

    public void setTaxRateAmount(final String taxRateAmount) {
        this.taxRateAmount = taxRateAmount;
    }

    public String getTaxRateRecoverability() {
        return taxRateRecoverability;
    }

    public void setTaxRateRecoverability(final String taxRateRecoverability) {
        this.taxRateRecoverability = taxRateRecoverability;
    }

    public String getTaxRateType() {
        return taxRateType;
    }

    public void setTaxRateType(final String taxRateType) {
        this.taxRateType = taxRateType;
    }

    public String getTaxRatePointDateType() {
        return taxRatePointDateType;
    }

    public void setTaxRatePointDateType(final String taxRatePointDateType) {
        this.taxRatePointDateType = taxRatePointDateType;
    }

    public String getTaxRatePointDate() {
        return taxRatePointDate;
    }

    public void setTaxRatePointDate(final String taxRatePointDate) {
        this.taxRatePointDate = taxRatePointDate;
    }

    public String getAttachmentRowId() {
        return attachmentRowId;
    }

    public void setAttachmentRowId(final String attachmentRowId) {
        this.attachmentRowId = attachmentRowId;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public void setAttachmentContentType(final String attachmentContentType) {
        this.attachmentContentType = attachmentContentType;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    public void setAttachmentFilename(final String attachmentFilename) {
        this.attachmentFilename = attachmentFilename;
    }

    public String getAttachmentEncoding() {
        return attachmentEncoding;
    }

    public void setAttachmentEncoding(final String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
    }

    public String getAttachmentCompressed() {
        return attachmentCompressed;
    }

    public void setAttachmentCompressed(final String attachmentCompressed) {
        this.attachmentCompressed = attachmentCompressed;
    }

    public String getAttachmentFileContent() {
        return attachmentFileContent;
    }

    public void setAttachmentFileContent(final String attachmentFileContent) {
        this.attachmentFileContent = attachmentFileContent;
    }

    public String getAttachmentComment() {
        return attachmentComment;
    }

    public void setAttachmentComment(final String attachmentComment) {
        this.attachmentComment = attachmentComment;
    }

    public String getAttachmentExternal() {
        return attachmentExternal;
    }

    public void setAttachmentExternal(final String attachmentExternal) {
        this.attachmentExternal = attachmentExternal;
    }

    public String getAttachmentCategory() {
        return attachmentCategory;
    }

    public void setAttachmentCategory(final String attachmentCategory) {
        this.attachmentCategory = attachmentCategory;
    }

    public String getAmortizationRowId() {
        return amortizationRowId;
    }

    public void setAmortizationRowId(final String amortizationRowId) {
        this.amortizationRowId = amortizationRowId;
    }

    public String getAmortizationFrequency() {
        return amortizationFrequency;
    }

    public void setAmortizationFrequency(final String amortizationFrequency) {
        this.amortizationFrequency = amortizationFrequency;
    }

    public String getAmortizationNumberOfPrepaymentInstallments() {
        return amortizationNumberOfPrepaymentInstallments;
    }

    public void setAmortizationNumberOfPrepaymentInstallments(final String amortizationNumberOfPrepaymentInstallments) {
        this.amortizationNumberOfPrepaymentInstallments = amortizationNumberOfPrepaymentInstallments;
    }

    public String getAmortizationUseInvoiceDate() {
        return amortizationUseInvoiceDate;
    }

    public void setAmortizationUseInvoiceDate(final String amortizationUseInvoiceDate) {
        this.amortizationUseInvoiceDate = amortizationUseInvoiceDate;
    }

    public String getAmortizationSpecifiedDate() {
        return amortizationSpecifiedDate;
    }

    public void setAmortizationSpecifiedDate(final String amortizationSpecifiedDate) {
        this.amortizationSpecifiedDate = amortizationSpecifiedDate;
    }

    public String getAmortizationIncludeAllAvailablePrepaidLines() {
        return amortizationIncludeAllAvailablePrepaidLines;
    }

    public void setAmortizationIncludeAllAvailablePrepaidLines(final String amortizationIncludeAllAvailablePrepaidLines) {
        this.amortizationIncludeAllAvailablePrepaidLines = amortizationIncludeAllAvailablePrepaidLines;
    }

    public String getAmortizationPurchaseOrderLineNumber() {
        return amortizationPurchaseOrderLineNumber;
    }

    public void setAmortizationPurchaseOrderLineNumber(final String amortizationPurchaseOrderLineNumber) {
        this.amortizationPurchaseOrderLineNumber = amortizationPurchaseOrderLineNumber;
    }

}
