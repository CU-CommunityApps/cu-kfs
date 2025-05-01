package edu.cornell.kfs.concur.web.struts;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurAccessTokenV2Service;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.web.struts.form.ConcurManageRefreshTokenForm;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;

@SuppressWarnings("deprecation")
public class ConcurManageRefreshTokenAction extends KualiAction {
    private static final Logger LOG = LogManager.getLogger();

    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.debug("start, entering");
        updateFormValues((ConcurManageRefreshTokenForm) form, false, false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.debug("cancel, entering");
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);
    }

    public ActionForward replaceRequestToken(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.debug("replaceRequestToken, entering");
        ConcurManageRefreshTokenForm tokenForm = (ConcurManageRefreshTokenForm) form;
        saveNewRequestToken(tokenForm.getNewRequestToken());
        updateFormValues(tokenForm, false, true);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward replaceRefreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.debug("replaceRefreshToken, entering");
        getConcurAccessTokenV2Service().retrieveAndPersistNewRefreshToken();
        updateFormValues((ConcurManageRefreshTokenForm) form, true, false);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    private void saveNewRequestToken(String newRequestTokenValue) {
        LOG.debug("saveNewRequestToken, entering");
        WebServiceCredential requestTokenCredential = findWebserviceCredential(
                ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REQUEST_TOKEN);
        requestTokenCredential.setCredentialValue(newRequestTokenValue);
        getBusinessObjectService().save(requestTokenCredential);
    }

    public WebServiceCredential findWebserviceCredential(String tokenName) {
        Map<String, String> keyMap = new HashMap<String, String>();
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_GROUP_CODE,
                ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.GROUP_CODE);
        keyMap.put(CUKFSPropertyConstants.WEB_SERVICE_CREDENTIAL_KEY, tokenName);
        WebServiceCredential refreshCredential = getBusinessObjectService().findByPrimaryKey(WebServiceCredential.class,
                keyMap);
        return refreshCredential;
    }

    protected void updateFormValues(ConcurManageRefreshTokenForm concurTokenForm,
            boolean displayUpdateRefreshTokenMessage, boolean displayUpdateRequestTokenMessage) {
        ConfigurationService configService = getConfigurationService();

        concurTokenForm.setNonProdWarning(configService
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_REFRESH_TOKEN_NONPROD_WARNING));
        concurTokenForm.setDisplayNonProdWarning(!isProduction());

        String refreshTokenUpdateDate = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_DATE), "refresh",
                getTokenUpdateTimestamp(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REFRESH_TOKEN).toString());
        String updateRefreshSuccessMessage = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_UPDATE_SUCCESS),
                "refresh");
        concurTokenForm.setRefreshTokenUpdateDate(refreshTokenUpdateDate);
        concurTokenForm.setUpdateRefreshTokenMessage(updateRefreshSuccessMessage);
        concurTokenForm.setDisplayUpdateRefreshTokenMessage(displayUpdateRefreshTokenMessage);

        String requestTokenUpdateDate = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_DATE), "request",
                getTokenUpdateTimestamp(ConcurConstants.ConcurOAuth2.WebServiceCredentialKeys.REQUEST_TOKEN).toString());
        String updateRequestSuccessMessage = MessageFormat.format(
                configService.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_UPDATE_SUCCESS),
                "request");
        concurTokenForm.setRequestTokenUpdateDate(requestTokenUpdateDate);
        concurTokenForm.setUpdateRequestTokenMessage(updateRequestSuccessMessage);
        concurTokenForm.setDisplayUpdateRequestTokenMessage(displayUpdateRequestTokenMessage);
        concurTokenForm.setUpdateRequestTokenInstructions(getConcurBatchUtilityService()
                .getConcurParameterValue(ConcurParameterConstants.CONCUR_OAUTH2_REPLACE_REQUEST_TOKEN_INSTRUCTIONS));
        concurTokenForm.setNewRequestToken(StringUtils.EMPTY);

    }

    protected boolean isProduction() {
        final Environment environment = SpringContext.getBean(Environment.class);
        boolean isProd = environment.isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }

    private Timestamp getTokenUpdateTimestamp(String tokenName) {
        WebServiceCredential refreshCredential = findWebserviceCredential(tokenName);
        return refreshCredential.getLastUpdatedTimestamp();
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
