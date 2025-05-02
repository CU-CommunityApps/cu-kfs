package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

public class PurchaseOrderDetailDto {

    private String kfsDocumentNumber;
    private String purchaseOrderNumber;
    private String purchaseOrderStatus;
    private List<PurchaseOrderInvoiceDto> purchaseOrderInvoices;
    private String vendorNumber;
    private String vendorName;
    private boolean foreignIndicator;
    private String recurringPaymentTypeCode;
    private List<PurchaseOrderVendorAddressDto> vendorAddresses;
    private List<PurchaseOrderItemDto> purchaseOrderItems;
    private String totalDollarAmount;

    public PurchaseOrderDetailDto() {
        purchaseOrderInvoices = new ArrayList<>();
        vendorAddresses = new ArrayList<>();
        purchaseOrderItems = new ArrayList<>();
    }

    public PurchaseOrderDetailDto(final PurchaseOrderDocument po, final VendorDetail vendorDetail) {
        this();
        this.kfsDocumentNumber = po.getDocumentNumber();
        this.purchaseOrderNumber = po.getPurapDocumentIdentifier() != null ? po.getPurapDocumentIdentifier().toString()
                : StringUtils.EMPTY;
        this.purchaseOrderStatus = po.getApplicationDocumentStatus();
        this.vendorNumber = po.getVendorNumber();
        this.vendorName = po.getVendorName();
        this.foreignIndicator = vendorDetail.getVendorHeader().getVendorForeignIndicator();
        this.recurringPaymentTypeCode = po.getRecurringPaymentTypeCode();
        this.totalDollarAmount = po.getTotalDollarAmount() != null ? po.getTotalDollarAmount().toString()
                : StringUtils.EMPTY;

        if (CollectionUtils.isNotEmpty(po.getRelatedViews().getRelatedPaymentRequestViews())) {
            for (PaymentRequestView reqView : po.getRelatedViews().getRelatedPaymentRequestViews()) {
                PurchaseOrderInvoiceDto dto = new PurchaseOrderInvoiceDto(reqView);
                this.purchaseOrderInvoices.add(dto);
            }
        }

        if (CollectionUtils.isNotEmpty(vendorDetail.getVendorAddresses())) {
            for (VendorAddress address : vendorDetail.getVendorAddresses()) {
                if (address.isActive()) {
                    PurchaseOrderVendorAddressDto dto = new PurchaseOrderVendorAddressDto(address);
                    this.vendorAddresses.add(dto);
                }
            }
        }
        
        if (CollectionUtils.isNotEmpty(po.getItems())) {
            for (Object itemObject : po.getItems()) {
                PurApItem purApItem = (PurApItem) itemObject;
                this.purchaseOrderItems.add(new PurchaseOrderItemDto(purApItem));
            }
        }
    }

    public String getKfsDocumentNumber() {
        return kfsDocumentNumber;
    }

    public void setKfsDocumentNumber(String kfsDocumentNumber) {
        this.kfsDocumentNumber = kfsDocumentNumber;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getPurchaseOrderStatus() {
        return purchaseOrderStatus;
    }

    public void setPurchaseOrderStatus(String purchaseOrderStatus) {
        this.purchaseOrderStatus = purchaseOrderStatus;
    }

    public List<PurchaseOrderInvoiceDto> getPurchaseOrderInvoices() {
        return purchaseOrderInvoices;
    }

    public void setPurchaseOrderInvoices(List<PurchaseOrderInvoiceDto> purchaseOrderInvoices) {
        this.purchaseOrderInvoices = purchaseOrderInvoices;
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

    public boolean isForeignIndicator() {
        return foreignIndicator;
    }

    public void setForeignIndicator(boolean foreignIndicator) {
        this.foreignIndicator = foreignIndicator;
    }

    public String getRecurringPaymentTypeCode() {
        return recurringPaymentTypeCode;
    }

    public void setRecurringPaymentTypeCode(String recurringPaymentTypeCode) {
        this.recurringPaymentTypeCode = recurringPaymentTypeCode;
    }

    public List<PurchaseOrderVendorAddressDto> getVendorAddresses() {
        return vendorAddresses;
    }

    public void setVendorAddresses(List<PurchaseOrderVendorAddressDto> vendorAddresses) {
        this.vendorAddresses = vendorAddresses;
    }

    public List<PurchaseOrderItemDto> getPurchaseOrderItems() {
        return purchaseOrderItems;
    }

    public void setPurchaseOrderItems(List<PurchaseOrderItemDto> purchaseOrderItems) {
        this.purchaseOrderItems = purchaseOrderItems;
    }

    public String getTotalDollarAmount() {
        return totalDollarAmount;
    }

    public void setTotalDollarAmount(String totalDollarAmount) {
        this.totalDollarAmount = totalDollarAmount;
    }

}
