package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiPurchaseOrderHeaderBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

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

}
