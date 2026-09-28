package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiSupplierFileChildrenTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 5731295022008870012L;

    private String supplierId;
    private String includedChildren;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getIncludedChildren() {
        return includedChildren;
    }

    public void setIncludedChildren(final String includedChildren) {
        this.includedChildren = includedChildren;
    }

}
