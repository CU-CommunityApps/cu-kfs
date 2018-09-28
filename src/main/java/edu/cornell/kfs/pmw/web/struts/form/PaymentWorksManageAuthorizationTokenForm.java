package edu.cornell.kfs.pmw.web.struts.form;

import org.kuali.kfs.kns.web.struts.form.KualiForm;

public class PaymentWorksManageAuthorizationTokenForm extends KualiForm {
    public boolean isProd = true;

    public PaymentWorksManageAuthorizationTokenForm() {
        super();
    }

    public boolean isProduction() {
        return isProd;
    }

    public void setIsProduction(boolean isProd) {
        this.isProd = isProd;
    }

}
