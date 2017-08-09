package edu.cornell.kfs.concur.web.struts.form;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurKeyConstants;

public class ConcurManageAccessTokenForm extends KualiForm {

    private static final long serialVersionUID = -8345711992403008219L;
    
    private String accessTokenExpirationDate;
    private String refreshTokenExplanation;
    private String replaceTokenExplanation;
    
    private transient ConfigurationService configurationService;

    public String getAccessTokenExpirationDate() {
        return accessTokenExpirationDate;
    }

    public void setAccessTokenExpirationDate(String accessTokenExpirationDate) {
        this.accessTokenExpirationDate = accessTokenExpirationDate;
    }

    public String getRefreshTokenExplanation() {
        if (StringUtils.isBlank(refreshTokenExplanation)) {
            refreshTokenExplanation = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_ACTION_EXPLANATION);
        }
        return refreshTokenExplanation;
    }

    public void setRefreshTokenExplanation(String refreshTokenExplanation) {
        this.refreshTokenExplanation = refreshTokenExplanation;
    }

    public String getReplaceTokenExplanation() {
        if (StringUtils.isBlank(replaceTokenExplanation)) {
            replaceTokenExplanation = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REPLACE_TOKEN_ACTION_EXPLANATION);
        }
        return replaceTokenExplanation;
    }

    public void setReplaceTokenExplanation(String replaceTokenExplanation) {
        this.replaceTokenExplanation = replaceTokenExplanation;
    }

    public ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
    

}
