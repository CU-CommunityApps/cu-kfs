package edu.cornell.kfs.cemi.vnd.batch.dto;

public class CemiSupplierChildren {
    
    private String supplierId;
    private String childSupplierId;
    
    public CemiSupplierChildren(final String childSupplierId, final String parentSupplierId) {
        this.supplierId = parentSupplierId;
        this.childSupplierId = childSupplierId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getChildSupplierId() {
        return childSupplierId;
    }

    public void setChildSupplierId(String childSupplierId) {
        this.childSupplierId = childSupplierId;
    }

}
