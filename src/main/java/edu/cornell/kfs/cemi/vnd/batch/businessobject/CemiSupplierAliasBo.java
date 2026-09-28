package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiSupplierAliasBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String aliasName;
    private String aliasUsage;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(final String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAliasUsage() {
        return aliasUsage;
    }

    public void setAliasUsage(final String aliasUsage) {
        this.aliasUsage = aliasUsage;
    }

}
