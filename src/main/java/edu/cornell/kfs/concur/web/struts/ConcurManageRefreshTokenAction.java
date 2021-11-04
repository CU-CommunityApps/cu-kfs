package edu.cornell.kfs.concur.web.struts;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.web.struts.form.ConcurManageRefreshTokenForm;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

@SuppressWarnings("deprecation")
public class ConcurManageRefreshTokenAction extends KualiAction {
    private static final Logger LOG = LogManager.getLogger();
    
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("start, entering");
        updateFormValues((ConcurManageRefreshTokenForm) form, false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("cancel, entering");
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);
    }
    
    public ActionForward replaceRefreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("replaceRefreshToken, entering");
        updateFormValues((ConcurManageRefreshTokenForm) form, true);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected void updateFormValues(ConcurManageRefreshTokenForm concurTokenForm, boolean displayUpdateSuccessMessage) {
        ConfigurationService configService = getConfigurationService();
        String nonProdWarningMessage = configService
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_NONPROD_WARNING);
        String refreshDateMessage = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_REFRESH_DATE),
                getRefreshTokenDate().toString());
        String updateSuccessMessage = configService
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_UPDATE_SUCCESS);
        
        concurTokenForm.setNonProdWarning(nonProdWarningMessage);
        concurTokenForm.setRefreshDateMessage(refreshDateMessage);
        concurTokenForm.setUpdateSuccessMessage(updateSuccessMessage);
        concurTokenForm.setDisplayUpdateSuccessMessage(displayUpdateSuccessMessage);
        concurTokenForm.setDispayNonProdWarning(!isProduction());
        
    }
    
    protected Timestamp getRefreshTokenDate() {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE,
                ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.GROUP_CODE);
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY,
                ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REFRESH_TOKEN);
        WebServiceCredential refreshCredential = getBusinessObjectService().findByPrimaryKey(WebServiceCredential.class,
                keyMap);
        return refreshCredential.getLastUpdatedTimestamp();
    }
    
    protected boolean isProduction() {
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }
    
    public ConfigurationService getConfigurationService() {
        return SpringContext.getBean(ConfigurationService.class);
    }
    
    public BusinessObjectService getBusinessObjectService() {
        return SpringContext.getBean(BusinessObjectService.class);
    }

}
