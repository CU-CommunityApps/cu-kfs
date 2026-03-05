package edu.cornell.kfs.vnd.batch.dto;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

public class CemiSupplierChildren {
    
    private String supplierId;
    private String childSupplierId;
    private boolean outputParentChildRelationship = false;
    
    private boolean resetParent;
    
    public CemiSupplierChildren(final VendorDetail currentVendor, final String currentSupplierId, final VendorDetail parentVendor, final String parentSupplierId) {
        setIdentifiersWhenParentChildRelationshipExists(currentVendor, currentSupplierId, parentVendor, parentSupplierId);
        this.resetParent = determineIfParentShouldBeReset(currentVendor, currentSupplierId);
    }
    
    private void setIdentifiersWhenParentChildRelationshipExists(VendorDetail currentVendor, String currentSupplierId, VendorDetail parentVendor, String parentSupplierId) {
        if (ObjectUtils.isNotNull(parentVendor)
            && !currentVendor.isVendorParentIndicator()
            && currentVendor.getVendorHeaderGeneratedIdentifier().equals(parentVendor.getVendorHeaderGeneratedIdentifier())
            && !currentVendor.getVendorDetailAssignedIdentifier().equals(parentVendor.getVendorDetailAssignedIdentifier())) {
            
            setSupplierId(parentSupplierId);
            setChildSupplierId(currentSupplierId);
            setOutputParentChildRelationship(true);
        }
    }
    
    /*
     * Method changeParentIdentifiersWhenProcessedSupplierChildrenIndicatesResetParent performs the actual 
     * re-assignments of the variables to the appropriate values. This occurs during the for-loop processing
     * in method writeSupplierDataToIntermediateStorage when method writeAllSupplierChildrenRowsFor is called
     * All of the processing utilizing this flag is located in class CemiSupplierDataBuilderBase.
     */
    private boolean determineIfParentShouldBeReset(VendorDetail currentVendor, String currentSupplierId) {
        if (currentVendor.isVendorParentIndicator()) {
            return true;
        }
        return false;
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

    public boolean isResetParent() {
        return resetParent;
    }

    public void setResetParent(boolean resetParent) {
        this.resetParent = resetParent;
    }

    public boolean isOutputParentChildRelationship() {
        return outputParentChildRelationship;
    }

    public void setOutputParentChildRelationship(boolean outputParentChildRelationship) {
        this.outputParentChildRelationship = outputParentChildRelationship;
    }

}
