package edu.cornell.kfs.concur.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;
import edu.cornell.kfs.concur.web.struts.form.ConcurManageAccessTokenForm;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

@SuppressWarnings("deprecation")
public class ConcurManageAccessTokenAction extends KualiAction {
	private static final Logger LOG = LogManager.getLogger(ConcurManageAccessTokenAction.class);
    
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("start, entering");
        updateFormValues((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward replaceToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().currentAccessTokenExists()) {
            getConcurAccessTokenService().revokeAndReplaceAccessToken();
            LOG.debug("replaceToken, revoke and replace was successful");
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS);
        } else {
            getConcurAccessTokenService().requestNewAccessToken();
            LOG.debug("replaceToken, replace was successful, there was no existing token to revoke");
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REPLACE_SUCCESS);
        }
        updateFormValues((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        getConcurAccessTokenService().refreshAccessToken();
        LOG.debug("refreshToken, refresh was successful");
        GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REFRESH_SUCCESS);
        updateFormValues((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward revokeToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().currentAccessTokenExists()) {
            getConcurAccessTokenService().revokeAccessToken();
            LOG.debug("revokeToken, revoke was successful");
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REVOKE_SUCCESS);
        } else {
            LOG.debug("revokeToken, no existing token to revoke");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.ERROR_CONCUR_TOKEN_REVOKE_NO_TOKEN);
        }
        updateFormValues((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward resetTokenToEmptyString(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!isProduction()) {
            getConcurAccessTokenService().resetTokenToEmptyStringInDatabase();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_RESET_SUCCESS);
        } else {
            LOG.error("resetTokenToEmptyString, we are in production, should not reset the token to an empty string.");
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.ERROR_CONCUR_TOKEN_RESET_IN_PRODUCTION);
        }
        updateFormValues((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected void updateFormValues(ConcurManageAccessTokenForm concurTokenForm) {
        String accessTokenExpirationDate = getWebServiceCredentialService().getWebServiceCredentialValue(
                ConcurConstants.CONCUR_WEB_SERVICE_GROUP_CODE,
                ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE
        );
        concurTokenForm.setAccessTokenExpirationDate(accessTokenExpirationDate);
        concurTokenForm.setShowResetTokenToEmptyStringButton(!isProduction());
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateFormValues, accessTokenExpirationDate: " + accessTokenExpirationDate);
        }
    }
    
    protected boolean isProduction() {
        final Environment environment = SpringContext.getBean(Environment.class);
        boolean isProd = environment.isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }

    protected ConcurAccessTokenService getConcurAccessTokenService() {
        return SpringContext.getBean(ConcurAccessTokenService.class);
    }
    
    protected WebServiceCredentialService getWebServiceCredentialService() {
        return SpringContext.getBean(WebServiceCredentialService.class);
    }

}
