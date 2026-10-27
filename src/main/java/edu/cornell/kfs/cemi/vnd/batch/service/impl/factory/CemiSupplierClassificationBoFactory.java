package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierClassificationBo;

public class CemiSupplierClassificationBoFactory {

    private CemiSupplierClassificationBoFactory() {
        throw new UnsupportedOperationException("Direct instantiation of this factory is not supported, due to the "
                + "simplicity of the BO being generated. Please use the static createClassificationBoFrom() method to "
                + "instead generate the CemiSupplierClassificationBo instance directly.");
    }

    public static CemiSupplierClassificationBo createClassificationBoFrom(final String workdayRefId,
            final String expirationDate) {
        final CemiSupplierClassificationBo classificationBo = new CemiSupplierClassificationBo();
        classificationBo.setWorkdayRefId(workdayRefId);
        classificationBo.setExpirationDate(expirationDate);
        return classificationBo;
    }
}
