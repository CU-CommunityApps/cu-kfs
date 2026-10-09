package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.time.LocalDateTime;
import java.util.List;

import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class CemiLegacyPurchaseOrder {

    private String documentNumber;
    private Integer purapDocumentIdentifier;
    private String documentStatusCode;
    private LocalDateTime approvedDate;
    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private String supplierId;
    private Integer vendorContractGeneratedIdentifier;
    private String paymentTermsTypeCode;
    private String paymentTermsDescription;
    private String requestorPersonName;
    private String requestorPersonEmailAddress;
    private String deliveryToName;
    private String deliveryToEmailAddress;
    private String deliveryBuildingLine1Address;
    private String deliveryBuildingLine2Address;
    private String deliveryBuildingRoomNumber;
    private String deliveryCityName;
    private String deliveryStateCode;
    private String deliveryPostalCode;
    private String deliveryCountryName;
    private KualiDecimal totalDollarAmount;
    private KualiDecimal freightOutstandingEncumberedAmount;
    private List<CemiLegacyPurchaseOrderItem> openItems = List.of();

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

    public String getDocumentStatusCode() {
        return documentStatusCode;
    }

    public void setDocumentStatusCode(final String documentStatusCode) {
        this.documentStatusCode = documentStatusCode;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(final LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
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

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public Integer getVendorContractGeneratedIdentifier() {
        return vendorContractGeneratedIdentifier;
    }

    public void setVendorContractGeneratedIdentifier(final Integer vendorContractGeneratedIdentifier) {
        this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
    }

    public String getPaymentTermsTypeCode() {
        return paymentTermsTypeCode;
    }

    public void setPaymentTermsTypeCode(final String paymentTermsTypeCode) {
        this.paymentTermsTypeCode = paymentTermsTypeCode;
    }

    public String getPaymentTermsDescription() {
        return paymentTermsDescription;
    }

    public void setPaymentTermsDescription(final String paymentTermsDescription) {
        this.paymentTermsDescription = paymentTermsDescription;
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

    public String getDeliveryBuildingRoomNumber() {
        return deliveryBuildingRoomNumber;
    }

    public void setDeliveryBuildingRoomNumber(final String deliveryBuildingRoomNumber) {
        this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
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

    public String getDeliveryCountryName() {
        return deliveryCountryName;
    }

    public void setDeliveryCountryName(final String deliveryCountryName) {
        this.deliveryCountryName = deliveryCountryName;
    }

    public KualiDecimal getTotalDollarAmount() {
        return totalDollarAmount;
    }

    public void setTotalDollarAmount(final KualiDecimal totalDollarAmount) {
        this.totalDollarAmount = totalDollarAmount;
    }

    public KualiDecimal getFreightOutstandingEncumberedAmount() {
        return freightOutstandingEncumberedAmount;
    }

    public void setFreightOutstandingEncumberedAmount(final KualiDecimal freightOutstandingEncumberedAmount) {
        this.freightOutstandingEncumberedAmount = freightOutstandingEncumberedAmount;
    }

    /*
     * Contains only the active items with outstanding encumbrances, in ascending order by line number.
     */
    public List<CemiLegacyPurchaseOrderItem> getOpenItems() {
        return openItems;
    }

    public void setOpenItems(final List<CemiLegacyPurchaseOrderItem> openItems) {
        this.openItems = openItems;
    }

}
