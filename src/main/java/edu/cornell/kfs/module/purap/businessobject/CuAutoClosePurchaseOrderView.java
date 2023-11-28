package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.module.purap.businessobject.AutoClosePurchaseOrderView;

public class CuAutoClosePurchaseOrderView extends AutoClosePurchaseOrderView {

    private static final long serialVersionUID = 1L;
    
    private String appDocStatus;

    public String getAppDocStatus() {
        return appDocStatus;
    }

    public void setAppDocStatus(final String appDocStatus) {
        this.appDocStatus = appDocStatus;
    }

}
