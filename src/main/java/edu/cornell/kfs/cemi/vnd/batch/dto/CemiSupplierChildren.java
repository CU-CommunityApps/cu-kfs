package edu.cornell.kfs.cemi.vnd.batch.dto;

import edu.cornell.kfs.cemi.sys.batch.dto.CemiDtoWithDateAndIndex;
import edu.cornell.kfs.cemi.sys.util.CemiDtoIndexer;

public class CemiSupplierChildren extends CemiDtoWithDateAndIndex {
    
    private String supplierId;
    private String childSupplierId;
    
    public CemiSupplierChildren(final CemiDtoIndexer indexer,
            final String childSupplierId, final String parentSupplierId) {
        super(indexer);
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
