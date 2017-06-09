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

import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.service.ConcurAccessTokenService;

@SuppressWarnings("deprecation")
public class ConcurManageAccessTokenAction extends KualiAction {

    public ActionForward requestNewToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        getConcurAccessTokenService().requestNewAccessToken();
        GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REQUEST_SUCCESS);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().isCurrentAccessTokenRevoked()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, ConcurKeyConstants.CONCUR_TOKEN_REFRESH_REVOKED);
        } else {
            getConcurAccessTokenService().refreshAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REFRESH_SUCCESS);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward revokeToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (getConcurAccessTokenService().isCurrentAccessTokenRevoked()) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, ConcurKeyConstants.CONCUR_TOKEN_ALREADY_REVOKED);
        } else {
            getConcurAccessTokenService().revokeAccessToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, ConcurKeyConstants.CONCUR_TOKEN_REVOKE_SUCCESS);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected ConcurAccessTokenService getConcurAccessTokenService() {
        return SpringContext.getBean(ConcurAccessTokenService.class);
    }

}
