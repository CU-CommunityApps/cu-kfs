package edu.cornell.kfs.pmw.web.struts.form;

import org.kuali.kfs.kns.web.struts.form.KualiForm;

public class PaymentWorksManageAuthorizationTokenForm extends KualiForm {
    private boolean isProduction = true;
    private String refreshWarning;

    public PaymentWorksManageAuthorizationTokenForm() {
        super();
    }

    public void setRefreshWarning(String refreshWarning) {
        this.refreshWarning = refreshWarning;
    }

    public String getRefreshWarning() {
        return refreshWarning;
    }

    public boolean isProduction() {
        return isProduction;
    }

    public void setIsProduction(boolean isProduction) {
        this.isProduction = isProduction;
    }

}
