package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAliasBo;

public class CemiSupplierAliasBoFactory {

    private CemiSupplierAliasBoFactory() {
        throw new UnsupportedOperationException("Direct instantiation of this factory is not supported, due to the "
                + "simplicity of the BO being generated. Please use the static createAliasBoFrom() method to instead "
                + "generate the CemiSupplierAliasBo instance directly.");
    }

    public static CemiSupplierAliasBo createAliasBoFrom(final String aliasName, final String aliasUsage) {
        final CemiSupplierAliasBo aliasBo = new CemiSupplierAliasBo();
        aliasBo.setAliasName(aliasName);
        aliasBo.setAliasUsage(aliasUsage);
        return aliasBo;
    }

}
