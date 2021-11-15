package edu.cornell.kfs.concur.web.struts.form;

import org.kuali.kfs.kns.web.struts.form.KualiForm;

@SuppressWarnings("deprecation")
public class ConcurManageRefreshTokenForm extends KualiForm {

    private static final long serialVersionUID = -2705859791711917748L;
    
    private boolean dispayNonProdWarning;
    private String nonProdWarning;
    
    private boolean displayUpdateRequestTokenMessage;
    private String updateRequestTokenMessage;
    private String requestTokenUpdateDate; 
    private String updateRequestTokenInstructions;
    
    private boolean displayUpdateRefreshTokenMessage;
    private String updateRefreshTokenMessage;
    private String refreshTokenUpdateDate;
    
    public boolean isDispayNonProdWarning() {
        return dispayNonProdWarning;
    }
    public void setDispayNonProdWarning(boolean dispayNonProdWarning) {
        this.dispayNonProdWarning = dispayNonProdWarning;
    }
    public String getNonProdWarning() {
        return nonProdWarning;
    }
    public void setNonProdWarning(String nonProdWarning) {
        this.nonProdWarning = nonProdWarning;
    }
    public boolean isDisplayUpdateRequestTokenMessage() {
        return displayUpdateRequestTokenMessage;
    }
    public void setDisplayUpdateRequestTokenMessage(boolean displayUpdateRequestTokenMessage) {
        this.displayUpdateRequestTokenMessage = displayUpdateRequestTokenMessage;
    }
    public String getUpdateRequestTokenMessage() {
        return updateRequestTokenMessage;
    }
    public void setUpdateRequestTokenMessage(String updateRequestTokenMessage) {
        this.updateRequestTokenMessage = updateRequestTokenMessage;
    }
    public String getRequestTokenUpdateDate() {
        return requestTokenUpdateDate;
    }
    public void setRequestTokenUpdateDate(String requestTokenUpdateDate) {
        this.requestTokenUpdateDate = requestTokenUpdateDate;
    }
    public String getUpdateRequestTokenInstructions() {
        return updateRequestTokenInstructions;
    }
    public void setUpdateRequestTokenInstructions(String updateRequestTokenInstructions) {
        this.updateRequestTokenInstructions = updateRequestTokenInstructions;
    }
    public boolean isDisplayUpdateRefreshTokenMessage() {
        return displayUpdateRefreshTokenMessage;
    }
    public void setDisplayUpdateRefreshTokenMessage(boolean displayUpdateRefreshTokenMessage) {
        this.displayUpdateRefreshTokenMessage = displayUpdateRefreshTokenMessage;
    }
    public String getUpdateRefreshTokenMessage() {
        return updateRefreshTokenMessage;
    }
    public void setUpdateRefreshTokenMessage(String updateRefreshTokenMessage) {
        this.updateRefreshTokenMessage = updateRefreshTokenMessage;
    }
    public String getRefreshTokenUpdateDate() {
        return refreshTokenUpdateDate;
    }
    public void setRefreshTokenUpdateDate(String refreshTokenUpdateDate) {
        this.refreshTokenUpdateDate = refreshTokenUpdateDate;
    } 

}
