package edu.cornell.kfs.module.purap.rest.jsonOnjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
    
    public PurchaseOrderDetailDto() {
        purchaseOrderItems = new ArrayList<PurchaseOrderItemDto>();
    }
    
    public PurchaseOrderDetailDto(PurchaseOrderDocument po) {
        this.documentNumber = po.getDocumentNumber();
        this.purchasOrderNumber = String.valueOf(po.getPurapDocumentIdentifier());
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
        
        
    }

}
