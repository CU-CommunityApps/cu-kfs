package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileChildrenTabRowBo;

public class CemiSupplierFileChildrenTabRowBoFactory {

    private CemiSupplierFileChildrenTabRowBoFactory() {
        throw new UnsupportedOperationException("Direct instantiation of this factory is not supported, due to the "
                + "simplicity of the BO being generated. Please use the static createTabRowBoFrom() method to instead "
                + "generate the CemiSupplierFileChildrenTabRowBo instance directly.");
    }

    public static CemiSupplierFileChildrenTabRowBo createTabRowBoFrom(
            final String parentSupplierId, final String childSupplierId) {
        final CemiSupplierFileChildrenTabRowBo childrenRowBo = new CemiSupplierFileChildrenTabRowBo();
        childrenRowBo.setSupplierId(parentSupplierId);
        childrenRowBo.setIncludedChildren(childSupplierId);
        return childrenRowBo;
    }

}
