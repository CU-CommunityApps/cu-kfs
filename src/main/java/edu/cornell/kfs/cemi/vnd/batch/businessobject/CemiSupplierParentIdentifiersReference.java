package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

/*
 * This class is NOT persisted to the database. 
 * It should only be used for KFS vendor parent-child business logic processing in CemiSupplierDataBuilderBase.
 */
public class CemiSupplierParentIdentifiersReference extends TransientBusinessObjectBase{
    
    private String parentSupplierId;
    private Integer parentVendorHeaderGeneratedIdentifier;
    private Integer parentVendorDetailAssignedIdentifier;
    
    public CemiSupplierParentIdentifiersReference(final String parentSupplierId, 
            final Integer parentVendorHeaderGeneratedIdentifier, final Integer parentVendorDetailAssignedIdentifier) {
        this.parentSupplierId = parentSupplierId;
        this.parentVendorHeaderGeneratedIdentifier = parentVendorHeaderGeneratedIdentifier;
        this.parentVendorDetailAssignedIdentifier = parentVendorDetailAssignedIdentifier;
    }

    public String getParentSupplierId() {
        return parentSupplierId;
    }

    public void setParentSupplierId(String parentSupplierId) {
        this.parentSupplierId = parentSupplierId;
    }

    public Integer getParentVendorHeaderGeneratedIdentifier() {
        return parentVendorHeaderGeneratedIdentifier;
    }

    public void setParentVendorHeaderGeneratedIdentifier(Integer parentVendorHeaderGeneratedIdentifier) {
        this.parentVendorHeaderGeneratedIdentifier = parentVendorHeaderGeneratedIdentifier;
    }

    public Integer getParentVendorDetailAssignedIdentifier() {
        return parentVendorDetailAssignedIdentifier;
    }

    public void setParentVendorDetailAssignedIdentifier(Integer parentVendorDetailAssignedIdentifier) {
        this.parentVendorDetailAssignedIdentifier = parentVendorDetailAssignedIdentifier;
    }

}
