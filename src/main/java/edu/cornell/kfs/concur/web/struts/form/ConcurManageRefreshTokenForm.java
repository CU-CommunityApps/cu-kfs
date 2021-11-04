package edu.cornell.kfs.concur.web.struts.form;

import org.kuali.kfs.kns.web.struts.form.KualiForm;

@SuppressWarnings("deprecation")
public class ConcurManageRefreshTokenForm extends KualiForm {

    private static final long serialVersionUID = -2705859791711917748L;
    
    private String refreshDateMessage;
    private String nonProdWarning;
    private boolean dispayNonProdWarning;
    private String updateSuccessMessage;
    private boolean displayUpdateSuccessMessage;

    public String getRefreshDateMessage() {
        return refreshDateMessage;
    }

    public void setRefreshDateMessage(String refreshDateMessage) {
        this.refreshDateMessage = refreshDateMessage;
    }

    public String getNonProdWarning() {
        return nonProdWarning;
    }

    public void setNonProdWarning(String nonProdWarning) {
        this.nonProdWarning = nonProdWarning;
    }

    public boolean isDispayNonProdWarning() {
        return dispayNonProdWarning;
    }

    public void setDispayNonProdWarning(boolean dispayNonProdWarning) {
        this.dispayNonProdWarning = dispayNonProdWarning;
    }

    public String getUpdateSuccessMessage() {
        return updateSuccessMessage;
    }

    public void setUpdateSuccessMessage(String updateSuccessMessage) {
        this.updateSuccessMessage = updateSuccessMessage;
    }

    public boolean isDisplayUpdateSuccessMessage() {
        return displayUpdateSuccessMessage;
    }

    public void setDisplayUpdateSuccessMessage(boolean displayUpdateSuccessMessage) {
        this.displayUpdateSuccessMessage = displayUpdateSuccessMessage;
    }

}
