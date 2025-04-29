package edu.cornell.kfs.module.purap.rest.jsonOnjects;

public class PurchaseOrderDetail {
    
    private String documentNumber;
    private String purchasOrderNumber;
    private String purchasOrderStatus;
    private String invoiceNumber;
    private String invoiceDate;
    private String vendorNumber;
    private String vendorName;
    private boolean foreign;
    private String paymentType;
    private List<VendorAddress> vendorAddresses;

}
