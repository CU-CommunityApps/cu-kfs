package edu.cornell.kfs.concur.web.struts.form;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.struts.form.KualiForm;

public class ConcurManageAccessTokenForm extends KualiForm {

    private static final long serialVersionUID = -8345711992403008219L;
    
    private String accessTokenExpirationDate;
    private boolean showResetTokenToEmptyStringButton;

    public String getAccessTokenExpirationDate() {
        return accessTokenExpirationDate;
    }

    public void setAccessTokenExpirationDate(String accessTokenExpirationDate) {
        this.accessTokenExpirationDate = accessTokenExpirationDate;
    }   
    
    public boolean getShowRevokeAndRefreshButtons() {
        return StringUtils.isNotEmpty(accessTokenExpirationDate);
    }
    
    public boolean getShowResetTokenToEmptyStringButton() {
        return showResetTokenToEmptyStringButton;
    }

    public void setShowResetTokenToEmptyStringButton(boolean showResetTokenToEmptyStringButton) {
        this.showResetTokenToEmptyStringButton = showResetTokenToEmptyStringButton;
    }

}
