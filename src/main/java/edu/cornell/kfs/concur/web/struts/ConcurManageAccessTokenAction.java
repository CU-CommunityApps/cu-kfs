package edu.cornell.kfs.concur.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
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
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurManageAccessTokenAction.class);
    
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("start, entering");
        updateAccessTokenExpirationDateOnForm((ConcurManageAccessTokenForm) form);
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
        updateAccessTokenExpirationDateOnForm((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        getConcurAccessTokenService().refreshAccessToken();
        LOG.debug("refreshToken, refresh was successful");
        GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REFRESH_SUCCESS);
        updateAccessTokenExpirationDateOnForm((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward revokeToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().currentAccessTokenExists()) {
            getConcurAccessTokenService().revokeAccessToken();
            LOG.debug("revokeToken, revoke was successful");
            //GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS);
        } else {
            LOG.debug("revokeToken, no existing token to revoke");
            //GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REPLACE_SUCCESS);
        }
        updateAccessTokenExpirationDateOnForm((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    public ActionForward deleteToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        getConcurAccessTokenService().deleteTokensFromDatabase();
        //GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS);
        updateAccessTokenExpirationDateOnForm((ConcurManageAccessTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected void updateAccessTokenExpirationDateOnForm(ConcurManageAccessTokenForm concurTokenForm) {
        String accessTokenExpirationDate = getWebServiceCredentialService().getWebServiceCredentialValue(ConcurConstants.CONCUR_ACCESS_TOKEN_EXPIRATION_DATE);
        concurTokenForm.setAccessTokenExpirationDate(accessTokenExpirationDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateAccessTokenExpirationDateOnForm, accessTokenExpirationDate: " + accessTokenExpirationDate);
        }
    }

    protected ConcurAccessTokenService getConcurAccessTokenService() {
        return SpringContext.getBean(ConcurAccessTokenService.class);
    }
    
    protected WebServiceCredentialService getWebServiceCredentialService() {
        return SpringContext.getBean(WebServiceCredentialService.class);
    }

}
