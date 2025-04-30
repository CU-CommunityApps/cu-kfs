package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;

public class PurchaseOrderDetailDto {
    
    private String documentNumber;
    private String purchasOrderNumber;
    private String purchasOrderStatus;
    private String invoiceNumber;
    private String invoiceDate;
    private String vendorNumber;
    private String vendorName;
    private boolean foreign;
    private String paymentType;
    private String vendorAddressLine1;
    private String vendorAddressLine2;
    private String vendorCity;
    private String vendorState;
    private String vendorPostalCode;
    private String vendorEmail;
    private List<PurchaseOrderItemDto> purchaseOrderItems;
    private String shipping;
    private String miscellaneous;
    private String grandTotal;
    
    public PurchaseOrderDetailDto() {
        purchaseOrderItems = new ArrayList<PurchaseOrderItemDto>();
    }
    
    public PurchaseOrderDetailDto(final PurchaseOrderDocument po) {
        this();
        this.documentNumber = po.getDocumentNumber();
        this.purchasOrderNumber = po.getPurapDocumentIdentifier() != null ? po.getPurapDocumentIdentifier().toString() : StringUtils.EMPTY;
        this.purchasOrderStatus = po.getApplicationDocumentStatus();
        //this.invoiceNumber = po.get
        //this.invoiceDate
        this.vendorNumber = po.getVendorNumber();
        this.vendorName = po.getVendorName();
        //this.foreign = 
        this.paymentType = po.getRecurringPaymentTypeCode();
        this.vendorAddressLine1 = po.getVendorLine1Address();
        this.vendorAddressLine2 = po.getVendorLine2Address();
        this.vendorCity = po.getVendorCityName();
        this.vendorPostalCode = po.getVendorPostalCode();
        this.vendorEmail = po.getVendorEmailAddress();
        if (CollectionUtils.isNotEmpty(po.getItems())) {
            for (Object itemObject : po.getItems()) {
                PurApItem purApItem = (PurApItem)itemObject;
                this.purchaseOrderItems.add(new PurchaseOrderItemDto(purApItem));
            }
        }
        //this.shipping =
        //this.miscellaneous       
        this.grandTotal = po.getTotalDollarAmount().toString();
        
        
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPurchasOrderNumber() {
        return purchasOrderNumber;
    }

    public void setPurchasOrderNumber(String purchasOrderNumber) {
        this.purchasOrderNumber = purchasOrderNumber;
    }

    public String getPurchasOrderStatus() {
        return purchasOrderStatus;
    }

    public void setPurchasOrderStatus(String purchasOrderStatus) {
        this.purchasOrderStatus = purchasOrderStatus;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public boolean isForeign() {
        return foreign;
    }

    public void setForeign(boolean foreign) {
        this.foreign = foreign;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getVendorAddressLine1() {
        return vendorAddressLine1;
    }

    public void setVendorAddressLine1(String vendorAddressLine1) {
        this.vendorAddressLine1 = vendorAddressLine1;
    }

    public String getVendorAddressLine2() {
        return vendorAddressLine2;
    }

    public void setVendorAddressLine2(String vendorAddressLine2) {
        this.vendorAddressLine2 = vendorAddressLine2;
    }

    public String getVendorCity() {
        return vendorCity;
    }

    public void setVendorCity(String vendorCity) {
        this.vendorCity = vendorCity;
    }

    public String getVendorState() {
        return vendorState;
    }

    public void setVendorState(String vendorState) {
        this.vendorState = vendorState;
    }

    public String getVendorPostalCode() {
        return vendorPostalCode;
    }

    public void setVendorPostalCode(String vendorPostalCode) {
        this.vendorPostalCode = vendorPostalCode;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public List<PurchaseOrderItemDto> getPurchaseOrderItems() {
        return purchaseOrderItems;
    }

    public void setPurchaseOrderItems(List<PurchaseOrderItemDto> purchaseOrderItems) {
        this.purchaseOrderItems = purchaseOrderItems;
    }

    public String getShipping() {
        return shipping;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public String getMiscellaneous() {
        return miscellaneous;
    }

    public void setMiscellaneous(String miscellaneous) {
        this.miscellaneous = miscellaneous;
    }

    public String getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(String grandTotal) {
        this.grandTotal = grandTotal;
    }

}
