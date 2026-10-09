package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrder;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderHeaderBo;
import edu.cornell.kfs.cemi.module.purap.batch.service.impl.CemiPurchaseOrderEmployeeIdLookup;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderHeaderBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private CemiLegacyPurchaseOrder purchaseOrderDocument;
    private CemiPurchaseOrderEmployeeIdLookup employeeIdLookup;
    private String supplierJobRunDateString;

    public CemiPurchaseOrderHeaderBoFactory(final CemiLegacyPurchaseOrder purchaseOrderDocument,
            final CemiPurchaseOrderEmployeeIdLookup employeeIdLookup, final String supplierJobRunDateString) {
        Validate.notNull(purchaseOrderDocument, "purchaseOrderDocument cannot be null");
        Validate.notNull(employeeIdLookup, "employeeIdLookup cannot be null");
        Validate.notBlank(supplierJobRunDateString, "supplierJobRunDateString cannot be blank");
        this.purchaseOrderDocument = purchaseOrderDocument;
        this.employeeIdLookup = employeeIdLookup;
        this.supplierJobRunDateString = supplierJobRunDateString;
    }

    public static CemiPurchaseOrderHeaderBo createHeaderBoFrom(final CemiLegacyPurchaseOrder purchaseOrderDocument,
            final CemiPurchaseOrderEmployeeIdLookup employeeIdLookup, final String supplierJobRunDateString) {
        final CemiPurchaseOrderHeaderBoFactory factory = new CemiPurchaseOrderHeaderBoFactory(
                purchaseOrderDocument, employeeIdLookup, supplierJobRunDateString);
        return factory.createCemiPurchaseOrderHeaderBo();
    }

    public CemiPurchaseOrderHeaderBo createCemiPurchaseOrderHeaderBo() {
        final CemiPurchaseOrderHeaderBo headerBo = new CemiPurchaseOrderHeaderBo();

        final String purchaseOrderId = purchaseOrderDocument.getPurapDocumentIdentifier().toString();
        final String requestorEmployeeId = determineRequestorEmployeeId();
        final String deliveryRecipientEmployeeId = determineDeliveryRecipientEmployeeId();

        headerBo.setKfsDocumentNumber(purchaseOrderDocument.getDocumentNumber());
        headerBo.setKfsPurchaseOrderId(purchaseOrderDocument.getPurapDocumentIdentifier());

        headerBo.setSpreadsheetKey(purchaseOrderId);
        headerBo.setAddOnly(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingPurchaseOrderDocumentNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setOrderTypeReference(CemiBaseConstants.EMPTY_STRING);
        headerBo.setAutoComplete(CemiBaseConstants.YES);
        headerBo.setComment(CemiBaseConstants.EMPTY_STRING);
        headerBo.setWorker(requestorEmployeeId);
        headerBo.setPurchaseOrderId(purchaseOrderId);
        headerBo.setSubmit(CemiBaseConstants.YES);
        headerBo.setLockedInWorkday(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentNumber(purchaseOrderId);
        headerBo.setInvoiceStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPaymentStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setReceivingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShippingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setTrackingStatus(CemiBaseConstants.EMPTY_STRING);
        headerBo.setCompany(determineCompanyId());
        headerBo.setSupplier(determineSupplierId());
        headerBo.setPurchaseOrderType(determinePurchaseOrderType());
        headerBo.setExternalPoNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setOrderFromSupplierConnection(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentDate(determineDocumentDate());
        headerBo.setTaxAmount(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFreightAmount(determineFreightAmount());
        headerBo.setOtherCharges(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPaymentTerms(determinePaymentTerms());
        headerBo.setOverridePaymentType(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcurementCreditCard(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShippingTerms(determineShippingTerms());
        headerBo.setShippingMethod(determineShippingMethod());
        headerBo.setShippingInstruction(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDueDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierContract(determineSupplierContractNumber());
        headerBo.setExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        headerBo.setCurrency(CemiPurchaseOrderConstants.CURRENCY_USD);
        headerBo.setAcknowledgementExpected(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxOption(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDefaultTaxCode(CemiBaseConstants.EMPTY_STRING);
        headerBo.setIssueOption(CemiPurchaseOrderConstants.PO_ISSUE_OPTION_PRINT);
        headerBo.setEmailRowId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEmailAddress(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBuyer(determineBuyerEmployeeId(requestorEmployeeId));
        headerBo.setBillToContact(CemiBaseConstants.EMPTY_STRING);
        headerBo.setBillToContactDetail(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExistingBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewBillToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setShipToContact(determineShipToContactEmployeeId(deliveryRecipientEmployeeId, requestorEmployeeId));
        headerBo.setShipToContactDetail(determineShipToContactDetail(deliveryRecipientEmployeeId));
        headerBo.setExistingShipToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNewShipToAddressId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDocumentLink(CemiBaseConstants.EMPTY_STRING);
        headerBo.setMemoForSupplier(determineMemoForSupplier());
        headerBo.setInternalMemo(CemiPurchaseOrderConstants.LEGACY_PO_CONVERSION_LABEL);
        headerBo.setPrepaid(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPrepaymentReleaseType(CemiBaseConstants.EMPTY_STRING);
        headerBo.setExpectedReleaseDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setFrequency(CemiBaseConstants.EMPTY_STRING);
        headerBo.setNumberOfPrepaymentInstallments(CemiBaseConstants.EMPTY_STRING);
        headerBo.setUseInvoiceDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSpecifiedDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setUsePrepaidPostingRulesForReceiptAccruals(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPercentToRetain(CemiBaseConstants.EMPTY_STRING);
        headerBo.setEstimatedRetentionReleaseDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setXmlname3rdPartyRetention(CemiBaseConstants.EMPTY_STRING);
        headerBo.setRetentionMemo(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentAmount(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentPercentage(CemiBaseConstants.EMPTY_STRING);
        headerBo.setDownPaymentMemo(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedureDate(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedure(CemiBaseConstants.EMPTY_STRING);
        headerBo.setProcedureNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPatientId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setMedicalRecordNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setPhysicianId(CemiBaseConstants.EMPTY_STRING);
        headerBo.setVerifiedBy(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierRepresentative(CemiBaseConstants.EMPTY_STRING);
        headerBo.setSupplierSalesOrderNumber(CemiBaseConstants.EMPTY_STRING);
        headerBo.setAdditionalProcedureDetails(CemiBaseConstants.EMPTY_STRING);

        return headerBo;
    }

    private String determineRequestorEmployeeId() {
        return employeeIdLookup.getEmployeeIdByNameAndEmail(purchaseOrderDocument.getRequestorPersonName(),
                purchaseOrderDocument.getRequestorPersonEmailAddress(), CemiPurchaseOrderConstants.REQUESTOR_LABEL,
                purchaseOrderDocument.getDocumentNumber());
    }

    private String determineDeliveryRecipientEmployeeId() {
        return employeeIdLookup.getEmployeeIdByNameAndEmail(purchaseOrderDocument.getDeliveryToName(),
                purchaseOrderDocument.getDeliveryToEmailAddress(), CemiPurchaseOrderConstants.DELIVERY_RECIPIENT_LABEL,
                purchaseOrderDocument.getDocumentNumber());
    }

    private String determineCompanyId() {
        return CemiPurchaseOrderConstants.DEFAULT_ITHACA_COMPANY;
    }

    private String determineSupplierId() {
        final String supplierId = StringUtils.defaultString(purchaseOrderDocument.getSupplierId());
        if (StringUtils.isBlank(supplierId)) {
            LOG.error("determineSupplierId, Could not find a Supplier ID for Vendor {}-{} and run date \"{}\"; "
                    + "either the wrong run date was used or the wrong Supplier scope was processed upstream. "
                    + "Will output an empty Supplier ID for now, which will need correcting.",
                    purchaseOrderDocument.getVendorHeaderGeneratedIdentifier(),
                    purchaseOrderDocument.getVendorDetailAssignedIdentifier(), supplierJobRunDateString);
        }
        return supplierId;
    }

    // TODO: At a future date, implement logic for deriving the Workday PO Type from the legacy PO Type.
    private String determinePurchaseOrderType() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private String determineDocumentDate() {
        final String documentStatusCode = purchaseOrderDocument.getDocumentStatusCode();
        Validate.validState(StringUtils.equalsAny(documentStatusCode,
                        DocumentStatus.FINAL.getCode(), DocumentStatus.PROCESSED.getCode()),
                "PO Document Number %s should have been in PROCESSED or FINAL status",
                purchaseOrderDocument.getDocumentNumber());
        Validate.validState(purchaseOrderDocument.getApprovedDate() != null,
                "PO Document Number %s should have had a Last Approved Date",
                purchaseOrderDocument.getDocumentNumber());
        return CemiPurchaseOrderUtils.formatAsDate(purchaseOrderDocument.getApprovedDate());
    }

    private String determineFreightAmount() {
        final KualiDecimal remainingFreightAmount = purchaseOrderDocument.getFreightOutstandingEncumberedAmount();
        return (remainingFreightAmount != null && remainingFreightAmount.isGreaterThan(KualiDecimal.ZERO))
                ? CemiPurchaseOrderUtils.formatAmount(remainingFreightAmount) : CemiBaseConstants.EMPTY_STRING;
    }

    // Copied and modified the related logic from the Supplier extract.
    private String determinePaymentTerms() {
        if (StringUtils.isNotBlank(purchaseOrderDocument.getPaymentTermsTypeCode())) {
            String paymentTermsDescription = purchaseOrderDocument.getPaymentTermsDescription();
            if (StringUtils.isBlank(paymentTermsDescription)) {
                return paymentTermsDescription;
            }
            return paymentTermsDescription
                    .trim()
                    .replaceAll("[^a-zA-Z0-9]+", "_") // replace spans of special chars/spaces with _
                    .replaceAll("^_|_$", ""); // strip leading/trailing underscores
        }
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, implement shipping term derivation logic.
    private String determineShippingTerms() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, implement shipping method derivation logic.
    private String determineShippingMethod() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private String determineSupplierContractNumber() {
        final Integer vendorContractId = purchaseOrderDocument.getVendorContractGeneratedIdentifier();
        return (vendorContractId != null) ? vendorContractId.toString() : CemiBaseConstants.EMPTY_STRING;
    }

    private String determineBuyerEmployeeId(final String requestorEmployeeId) {
        if (StringUtils.isNotBlank(requestorEmployeeId)) {
            return requestorEmployeeId;
        } else {
            return employeeIdLookup.getDefaultBuyerEmployeeId();
        }
    }

    private String determineShipToContactEmployeeId(final String deliveryRecipientEmployeeId,
            final String requestorEmployeeId) {
        if (StringUtils.isNotBlank(deliveryRecipientEmployeeId)) {
            return deliveryRecipientEmployeeId;
        } else if (StringUtils.isNotBlank(requestorEmployeeId)) {
            return requestorEmployeeId;
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    private String determineShipToContactDetail(final String deliveryRecipientEmployeeId) {
        final String[] deliveryAddressLines = {
            getDeliveryRecipientLine(deliveryRecipientEmployeeId),
            getDeliveryLine1Address(),
            purchaseOrderDocument.getDeliveryBuildingLine2Address(),
            getDeliveryCityStatePostalCodeLine(),
            purchaseOrderDocument.getDeliveryCountryName()
        };

        return Arrays.stream(deliveryAddressLines)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(KFSConstants.NEWLINE));
    }

    private String getDeliveryRecipientLine(final String deliveryRecipientEmployeeId) {
        if (StringUtils.isNotBlank(deliveryRecipientEmployeeId)) {
            return purchaseOrderDocument.getDeliveryToName();
        } else if (StringUtils.isNotBlank(purchaseOrderDocument.getRequestorPersonName())) {
            return purchaseOrderDocument.getRequestorPersonName();
        } else {
            return purchaseOrderDocument.getDeliveryToName();
        }
    }

    private String getDeliveryLine1Address() {
        return StringUtils.join(purchaseOrderDocument.getDeliveryBuildingLine1Address(), CUKFSConstants.COMMA_AND_SPACE,
                CemiPurchaseOrderConstants.ROOM_NUMBER_SEGMENT_PREFIX,
                purchaseOrderDocument.getDeliveryBuildingRoomNumber());
    }

    private String getDeliveryCityStatePostalCodeLine() {
        return StringUtils.join(purchaseOrderDocument.getDeliveryCityName(), CUKFSConstants.COMMA_AND_SPACE,
                purchaseOrderDocument.getDeliveryStateCode(), KFSConstants.BLANK_SPACE,
                purchaseOrderDocument.getDeliveryPostalCode());
    }

    private String determineMemoForSupplier() {
        final String originalPurchaseOrderTotalAmount = CemiPurchaseOrderUtils.formatAmount(
                purchaseOrderDocument.getTotalDollarAmount());
        return StringUtils.join(
                CemiPurchaseOrderConstants.ORIGINAL_PO_AMOUNT_MEMO_PREFIX, originalPurchaseOrderTotalAmount);
    }

}
