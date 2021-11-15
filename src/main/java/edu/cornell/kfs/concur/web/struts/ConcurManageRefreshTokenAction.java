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
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
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
        updateFormValues((ConcurManageRefreshTokenForm) form, false, false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("cancel, entering");
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);
    }
    
    public ActionForward replaceRequestToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("replaceRequestToken, entering");
        updateFormValues((ConcurManageRefreshTokenForm) form, false, true);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward replaceRefreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        LOG.info("replaceRefreshToken, entering");
        getConcurAccessTokenV2Service().retrieveAndPersistNewRefreshToken();
        updateFormValues((ConcurManageRefreshTokenForm) form, true, false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected void updateFormValues(ConcurManageRefreshTokenForm concurTokenForm, boolean displayUpdateRefreshTokenMessage, 
            boolean displayUpdateRequestTokenMessage) {
        ConfigurationService configService = getConfigurationService();
        
        concurTokenForm.setNonProdWarning(configService.getPropertyValueAsString(
                ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_NONPROD_WARNING));
        concurTokenForm.setDispayNonProdWarning(!isProduction());
        
        String refreshTokenUpdateDate = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_DATE),
                "refresh", getRefreshTokenDate().toString());
        String updateRefeshSuccessMessage = MessageFormat.format(configService
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_UPDATE_SUCCESS), "refresh");
        concurTokenForm.setRefreshTokenUpdateDate(refreshTokenUpdateDate); 
        concurTokenForm.setUpdateRefreshTokenMessage(updateRefeshSuccessMessage);
        concurTokenForm.setDisplayUpdateRefreshTokenMessage(displayUpdateRefreshTokenMessage);
        
        String requestTokenUpdateDate = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_DATE),
                "request", getRequestTokenDate().toString());
        String updateRequestSuccessMessage = MessageFormat.format(configService
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_UPDATE_SUCCESS), "request");
        concurTokenForm.setRequestTokenUpdateDate(requestTokenUpdateDate);
        concurTokenForm.setUpdateRequestTokenMessage(updateRequestSuccessMessage);
        concurTokenForm.setDisplayUpdateRequestTokenMessage(displayUpdateRequestTokenMessage);
        concurTokenForm.setUpdateRequestTokenInstructions(getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_OAUTH2_REPLACE_REQUEST_TOKEN_INSTRUCTIONS));
        
    }
    
    protected Timestamp getRefreshTokenDate() {
        String refreshTokenName = ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REFRESH_TOKEN;
        return getTokenUpdateTimestamp(refreshTokenName);
    }

    private Timestamp getTokenUpdateTimestamp(String tokenName) {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE,
                ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.GROUP_CODE);
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY,
                tokenName);
        WebServiceCredential refreshCredential = getBusinessObjectService().findByPrimaryKey(WebServiceCredential.class,
                keyMap);
        return refreshCredential.getLastUpdatedTimestamp();
    }
    
    protected Timestamp getRequestTokenDate() {
        String requestTokenName = ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REQUEST_TOKEN;
        return getTokenUpdateTimestamp(requestTokenName);
    }
    
    protected boolean isProduction() {
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }
    
    protected ConfigurationService getConfigurationService() {
        return SpringContext.getBean(ConfigurationService.class);
    }
    
    protected BusinessObjectService getBusinessObjectService() {
        return SpringContext.getBean(BusinessObjectService.class);
    }
    
    protected ConcurAccessTokenV2Service getConcurAccessTokenV2Service() {
        return SpringContext.getBean(ConcurAccessTokenV2Service.class);
    }
    
    protected ConcurBatchUtilityService getConcurBatchUtilityService() {
        return SpringContext.getBean(ConcurBatchUtilityService.class);
    }

}
